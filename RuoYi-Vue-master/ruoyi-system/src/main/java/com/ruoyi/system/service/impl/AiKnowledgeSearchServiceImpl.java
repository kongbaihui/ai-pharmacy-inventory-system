package com.ruoyi.system.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.config.AiKnowledgeProperties;
import com.ruoyi.system.domain.ai.KnowledgeChunk;
import com.ruoyi.system.domain.ai.KnowledgeSearchResult;
import com.ruoyi.system.service.IAiKnowledgeSearchService;

/**
 * 面向千级本地语料的中文字符二元组 BM25 检索。
 */
@Service
public class AiKnowledgeSearchServiceImpl implements IAiKnowledgeSearchService
{
    private static final Pattern TOKEN_PATTERN = Pattern.compile("[\\p{IsHan}]+|[a-z0-9]+");
    private static final Pattern BUILD_ID_PATTERN = Pattern.compile("[0-9]{14}-[a-f0-9]{8}");
    private static final int MAX_CHUNK_COUNT = 10000;
    private static final int MAX_CHUNK_LENGTH = 10000;
    private static final int MAX_SNIPPET_LENGTH = 700;
    private static final double K1 = 1.2;
    private static final double B = 0.75;

    private final AiKnowledgeProperties properties;
    private final ObjectMapper objectMapper;
    private volatile SearchIndex cachedIndex;

    public AiKnowledgeSearchServiceImpl(AiKnowledgeProperties properties, ObjectMapper objectMapper)
    {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<KnowledgeSearchResult> search(String query, int limit)
    {
        if (StringUtils.isBlank(query))
        {
            throw new ServiceException("知识检索关键词不能为空");
        }
        List<String> queryTerms = tokenize(query);
        if (queryTerms.isEmpty())
        {
            return List.of();
        }

        SearchIndex index = currentIndex();
        Set<String> uniqueTerms = new HashSet<>(queryTerms);
        return index.chunks().stream()
                .map(chunk -> new ScoredChunk(chunk, score(chunk, uniqueTerms, index)))
                .filter(item -> item.score() > 0)
                .sorted(Comparator.comparingDouble(ScoredChunk::score).reversed()
                        .thenComparing(item -> item.chunk().id()))
                .limit(Math.max(1, limit))
                .map(this::toResult)
                .toList();
    }

    private SearchIndex currentIndex()
    {
        Path root = Path.of(properties.getWorkDir()).toAbsolutePath().normalize();
        String buildId = readCurrentBuildId(root);
        SearchIndex current = cachedIndex;
        if (current != null && current.buildId().equals(buildId))
        {
            return current;
        }
        synchronized (this)
        {
            current = cachedIndex;
            if (current == null || !current.buildId().equals(buildId))
            {
                current = loadIndex(root, buildId);
                cachedIndex = current;
            }
            return current;
        }
    }

    private String readCurrentBuildId(Path root)
    {
        Path pointer = root.resolve("current.json");
        if (!Files.isRegularFile(pointer))
        {
            throw new ServiceException("知识库尚未构建，请先执行知识库重建");
        }
        try
        {
            Map<String, String> current = objectMapper.readValue(pointer.toFile(), new TypeReference<>() { });
            String buildId = current.get("buildId");
            if (StringUtils.isBlank(buildId) || !BUILD_ID_PATTERN.matcher(buildId).matches())
            {
                throw new IOException("知识库版本号无效");
            }
            return buildId;
        }
        catch (IOException e)
        {
            throw new ServiceException("知识库版本信息不可用");
        }
    }

    private SearchIndex loadIndex(Path root, String buildId)
    {
        Path chunksFile = root.resolve("builds").resolve(buildId).resolve("chunks.jsonl").normalize();
        if (!chunksFile.startsWith(root) || !Files.isRegularFile(chunksFile))
        {
            throw new ServiceException("知识库切片文件不存在，请重新构建知识库");
        }

        List<IndexedChunk> chunks = new ArrayList<>();
        Map<String, Integer> documentFrequency = new HashMap<>();
        long totalLength = 0;
        try (BufferedReader reader = Files.newBufferedReader(chunksFile, StandardCharsets.UTF_8))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                if (chunks.size() >= MAX_CHUNK_COUNT)
                {
                    throw new IOException("知识库切片数量超过限制");
                }
                KnowledgeChunk source = objectMapper.readValue(line, KnowledgeChunk.class);
                IndexedChunk chunk = indexChunk(source);
                chunks.add(chunk);
                totalLength += chunk.length();
                chunk.termFrequency().keySet().forEach(term -> documentFrequency.merge(term, 1, Integer::sum));
            }
        }
        catch (IOException e)
        {
            throw new ServiceException("知识库索引加载失败，请重新构建知识库");
        }
        if (chunks.isEmpty())
        {
            throw new ServiceException("知识库没有可检索内容，请重新构建知识库");
        }
        return new SearchIndex(buildId, List.copyOf(chunks), Map.copyOf(documentFrequency),
                (double) totalLength / chunks.size());
    }

    private IndexedChunk indexChunk(KnowledgeChunk source) throws IOException
    {
        if (StringUtils.isBlank(source.getId()) || StringUtils.isBlank(source.getContent())
                || source.getContent().length() > MAX_CHUNK_LENGTH)
        {
            throw new IOException("知识切片内容无效");
        }
        Map<String, Integer> frequency = new HashMap<>();
        addTerms(frequency, source.getContent(), 1);
        addTerms(frequency, metadata(source, "title"), 3);
        addTerms(frequency, metadata(source, "authority"), 2);
        addTerms(frequency, metadata(source, "category"), 2);
        int length = frequency.values().stream().mapToInt(Integer::intValue).sum();
        return new IndexedChunk(source.getId(), source.getContent(), Map.copyOf(source.getMetadata()),
                Map.copyOf(frequency), Math.max(1, length));
    }

    private void addTerms(Map<String, Integer> frequency, String value, int weight)
    {
        for (String term : tokenize(value))
        {
            frequency.merge(term, weight, Integer::sum);
        }
    }

    private double score(IndexedChunk chunk, Set<String> queryTerms, SearchIndex index)
    {
        double score = 0;
        for (String term : queryTerms)
        {
            int frequency = chunk.termFrequency().getOrDefault(term, 0);
            if (frequency == 0)
            {
                continue;
            }
            int documentFrequency = index.documentFrequency().getOrDefault(term, 0);
            double idf = Math.log(1 + (index.chunks().size() - documentFrequency + 0.5)
                    / (documentFrequency + 0.5));
            double denominator = frequency + K1 * (1 - B + B * chunk.length() / index.averageLength());
            score += idf * frequency * (K1 + 1) / denominator;
        }
        return score;
    }

    private KnowledgeSearchResult toResult(ScoredChunk scored)
    {
        IndexedChunk chunk = scored.chunk();
        KnowledgeSearchResult result = new KnowledgeSearchResult();
        result.setSourceId(metadata(chunk.metadata(), "sourceId"));
        result.setCitationId(chunk.id());
        result.setContent(snippet(chunk.content()));
        result.setTitle(metadata(chunk.metadata(), "title"));
        result.setAuthority(metadata(chunk.metadata(), "authority"));
        result.setUrl(metadata(chunk.metadata(), "url"));
        result.setCategory(metadata(chunk.metadata(), "category"));
        result.setRelevance(Math.round(scored.score() * 1000.0) / 1000.0);
        return result;
    }

    private String snippet(String content)
    {
        String normalized = content.replaceAll("\\s+", " ").trim();
        return normalized.length() <= MAX_SNIPPET_LENGTH
                ? normalized : normalized.substring(0, MAX_SNIPPET_LENGTH) + "…";
    }

    private String metadata(KnowledgeChunk chunk, String key)
    {
        return metadata(chunk.getMetadata(), key);
    }

    private String metadata(Map<String, Object> metadata, String key)
    {
        Object value = metadata == null ? null : metadata.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    static List<String> tokenize(String value)
    {
        if (StringUtils.isBlank(value))
        {
            return List.of();
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);
        Matcher matcher = TOKEN_PATTERN.matcher(normalized);
        List<String> terms = new ArrayList<>();
        while (matcher.find())
        {
            String token = matcher.group();
            if (token.codePoints().allMatch(AiKnowledgeSearchServiceImpl::isHan))
            {
                int[] codePoints = token.codePoints().toArray();
                if (codePoints.length == 1)
                {
                    terms.add(token);
                }
                else
                {
                    for (int i = 0; i < codePoints.length - 1; i++)
                    {
                        terms.add(new String(codePoints, i, 2));
                    }
                }
            }
            else if (token.length() > 1)
            {
                terms.add(token);
            }
        }
        return terms;
    }

    private static boolean isHan(int codePoint)
    {
        return Character.UnicodeScript.of(codePoint) == Character.UnicodeScript.HAN;
    }

    private record IndexedChunk(String id, String content, Map<String, Object> metadata,
            Map<String, Integer> termFrequency, int length) { }

    private record SearchIndex(String buildId, List<IndexedChunk> chunks,
            Map<String, Integer> documentFrequency, double averageLength) { }

    private record ScoredChunk(IndexedChunk chunk, double score) { }
}
