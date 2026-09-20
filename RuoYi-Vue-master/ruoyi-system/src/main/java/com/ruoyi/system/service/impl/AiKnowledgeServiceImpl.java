package com.ruoyi.system.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.ai.knowledge.KnowledgeTextChunker;
import com.ruoyi.system.config.AiKnowledgeProperties;
import com.ruoyi.system.domain.ai.KnowledgeBuildResult;
import com.ruoyi.system.domain.ai.KnowledgeChunk;
import com.ruoyi.system.domain.ai.KnowledgeSource;
import com.ruoyi.system.service.IAiKnowledgeService;

/**
 * 下载、校验并切分白名单中的权威资料。
 */
@Service
public class AiKnowledgeServiceImpl implements IAiKnowledgeService
{
    private static final Set<String> ALLOWED_HOSTS = Set.of(
            "www.nhc.gov.cn", "nhc.gov.cn",
            "www.samr.gov.cn", "samr.gov.cn", "sjfg.samr.gov.cn",
            "www.gov.cn", "gov.cn",
            "english.nmpa.gov.cn", "nmpa.gov.cn", "www.nmpa.gov.cn",
            "zjjcmspublic.oss-cn-hangzhou-zwynet-d01-a.internet.cloud.zj.gov.cn",
            "cdn.who.int", "iris.who.int", "www.who.int", "who.int",
            "medlineplus.gov", "www.medlineplus.gov");

    private static final TypeReference<List<KnowledgeSource>> SOURCE_LIST_TYPE = new TypeReference<>() { };

    private final AiKnowledgeProperties properties;
    private final KnowledgeTextChunker chunker;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public AiKnowledgeServiceImpl(AiKnowledgeProperties properties, KnowledgeTextChunker chunker,
            ObjectMapper objectMapper)
    {
        this.properties = properties;
        this.chunker = chunker;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Override
    public synchronized KnowledgeBuildResult rebuild()
    {
        KnowledgeBuildResult result = new KnowledgeBuildResult();
        result.setBuildId(createBuildId());
        result.setStatus("building");
        result.setStartedAt(OffsetDateTime.now());

        try
        {
            List<KnowledgeSource> sources = loadSources();
            result.setSourceCount(sources.size());
            Path root = Path.of(properties.getWorkDir()).toAbsolutePath().normalize();
            Path buildDir = root.resolve("builds").resolve(result.getBuildId()).normalize();
            ensureChildPath(root, buildDir);
            Path rawDir = buildDir.resolve("raw");
            Files.createDirectories(rawDir);
            Path chunksFile = buildDir.resolve("chunks.jsonl");

            int chunkCount = 0;
            try (var writer = Files.newBufferedWriter(chunksFile, StandardCharsets.UTF_8))
            {
                for (KnowledgeSource source : sources)
                {
                    try
                    {
                        DownloadedSource downloaded = download(source, rawDir);
                        String text = extractText(downloaded.path());
                        if (text.length() < 200)
                        {
                            throw new IOException("提取文本过短，可能被来源站点拦截");
                        }
                        List<String> pieces = chunker.split(text);
                        for (int index = 0; index < pieces.size(); index++)
                        {
                            KnowledgeChunk chunk = toChunk(source, downloaded.sha256(), pieces.get(index), index);
                            writer.write(objectMapper.writeValueAsString(chunk));
                            writer.newLine();
                            chunkCount++;
                        }
                        result.setSuccessCount(result.getSuccessCount() + 1);
                    }
                    catch (Exception e)
                    {
                        result.getFailures().put(source.getId(), safeError(e));
                    }
                }
            }

            result.setChunkCount(chunkCount);
            result.setFailedCount(result.getSourceCount() - result.getSuccessCount());
            result.setFinishedAt(OffsetDateTime.now());
            if (result.getSuccessCount() < properties.getMinimumSourceCount())
            {
                result.setStatus("failed");
                writeJson(buildDir.resolve("manifest.json"), result);
                throw new ServiceException("知识库有效来源不足：成功 " + result.getSuccessCount()
                        + "，最低要求 " + properties.getMinimumSourceCount());
            }

            result.setStatus(result.getFailedCount() == 0 ? "ready" : "partial");
            writeJson(buildDir.resolve("manifest.json"), result);
            publishCurrent(root, result.getBuildId());
            return result;
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ServiceException("知识库构建失败：" + safeError(e));
        }
    }

    @Override
    public KnowledgeBuildResult getCurrentStatus()
    {
        Path root = Path.of(properties.getWorkDir()).toAbsolutePath().normalize();
        Path pointer = root.resolve("current.json");
        if (!Files.isRegularFile(pointer))
        {
            KnowledgeBuildResult empty = new KnowledgeBuildResult();
            empty.setStatus("not_built");
            return empty;
        }
        try
        {
            Map<String, String> current = objectMapper.readValue(pointer.toFile(), new TypeReference<>() { });
            String buildId = current.get("buildId");
            Path manifest = root.resolve("builds").resolve(buildId).resolve("manifest.json").normalize();
            ensureChildPath(root, manifest);
            return objectMapper.readValue(manifest.toFile(), KnowledgeBuildResult.class);
        }
        catch (Exception e)
        {
            throw new ServiceException("知识库状态读取失败");
        }
    }

    private List<KnowledgeSource> loadSources() throws IOException
    {
        List<KnowledgeSource> sources;
        try (InputStream input = new ClassPathResource("ai/knowledge-sources.json").getInputStream())
        {
            sources = objectMapper.readValue(input, SOURCE_LIST_TYPE);
        }
        Set<String> ids = new HashSet<>();
        for (KnowledgeSource source : sources)
        {
            if (StringUtils.isBlank(source.getId()) || !source.getId().matches("[a-z0-9-]+")
                    || StringUtils.isBlank(source.getTitle()) || StringUtils.isBlank(source.getAuthority()))
            {
                throw new IOException("知识来源定义不完整");
            }
            if (!ids.add(source.getId()))
            {
                throw new IOException("知识来源ID重复：" + source.getId());
            }
            validateUri(URI.create(source.getUrl()));
        }
        return sources;
    }

    private DownloadedSource download(KnowledgeSource source, Path rawDir) throws Exception
    {
        URI uri = URI.create(source.getUrl());
        validateUri(uri);
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(90))
                .header("User-Agent", "ai-pharmacy-knowledge-builder/1.0")
                .GET()
                .build();
        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        validateUri(response.uri());
        if (response.statusCode() < 200 || response.statusCode() >= 300)
        {
            response.body().close();
            throw new IOException("下载返回HTTP " + response.statusCode());
        }
        long contentLength = response.headers().firstValueAsLong("Content-Length").orElse(-1);
        if (contentLength > properties.getMaxFileSize())
        {
            response.body().close();
            throw new IOException("来源文件超过大小限制");
        }

        byte[] bytes;
        try (InputStream input = response.body())
        {
            bytes = input.readNBytes(properties.getMaxFileSize() + 1);
        }
        if (bytes.length > properties.getMaxFileSize())
        {
            throw new IOException("来源文件超过大小限制");
        }

        String contentType = response.headers().firstValue("Content-Type").orElse("").toLowerCase(Locale.ROOT);
        String suffix = contentType.contains("pdf") || uri.getPath().toLowerCase(Locale.ROOT).endsWith(".pdf")
                ? ".pdf" : ".html";
        Path target = rawDir.resolve(source.getId() + suffix).normalize();
        ensureChildPath(rawDir, target);
        Files.write(target, bytes);
        return new DownloadedSource(target, sha256(bytes));
    }

    private String extractText(Path path)
    {
        List<Document> documents = new TikaDocumentReader(new FileSystemResource(path)).read();
        return documents.stream()
                .map(Document::getText)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining("\n\n"));
    }

    private KnowledgeChunk toChunk(KnowledgeSource source, String sourceChecksum, String content, int index)
    {
        KnowledgeChunk chunk = new KnowledgeChunk();
        chunk.setId(source.getId() + "-" + String.format("%04d", index));
        chunk.setContent(content);
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("sourceId", source.getId());
        metadata.put("title", source.getTitle());
        metadata.put("authority", source.getAuthority());
        metadata.put("url", source.getUrl());
        metadata.put("language", source.getLanguage());
        metadata.put("category", source.getCategory());
        metadata.put("sourceChecksum", sourceChecksum);
        metadata.put("chunkIndex", index);
        chunk.setMetadata(metadata);
        return chunk;
    }

    private void publishCurrent(Path root, String buildId) throws IOException
    {
        Files.createDirectories(root);
        Path temporary = root.resolve("current.json.tmp");
        writeJson(temporary, Map.of("buildId", buildId));
        try
        {
            Files.move(temporary, root.resolve("current.json"), StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        }
        catch (AtomicMoveNotSupportedException e)
        {
            Files.move(temporary, root.resolve("current.json"), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void writeJson(Path path, Object value) throws IOException
    {
        Files.createDirectories(path.getParent());
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), value);
    }

    private void validateUri(URI uri) throws IOException
    {
        String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
        if (!"https".equalsIgnoreCase(uri.getScheme()) || !ALLOWED_HOSTS.contains(host))
        {
            throw new IOException("知识来源不在HTTPS白名单：" + host);
        }
    }

    private void ensureChildPath(Path root, Path child) throws IOException
    {
        if (!child.toAbsolutePath().normalize().startsWith(root.toAbsolutePath().normalize()))
        {
            throw new IOException("知识库路径越界");
        }
    }

    private String createBuildId()
    {
        return DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(OffsetDateTime.now())
                + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String sha256(byte[] value) throws Exception
    {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(value);
        StringBuilder result = new StringBuilder(digest.length * 2);
        for (byte item : digest)
        {
            result.append(String.format("%02x", item));
        }
        return result.toString();
    }

    private String safeError(Exception exception)
    {
        String message = StringUtils.isBlank(exception.getMessage())
                ? exception.getClass().getSimpleName() : exception.getMessage();
        return message.length() > 180 ? message.substring(0, 180) : message;
    }

    private record DownloadedSource(Path path, String sha256) { }
}
