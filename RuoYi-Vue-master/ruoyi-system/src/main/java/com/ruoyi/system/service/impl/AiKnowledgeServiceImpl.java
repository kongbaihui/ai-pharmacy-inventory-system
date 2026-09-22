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
import java.util.ArrayList;
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
import com.ruoyi.system.domain.ai.KnowledgeImportRequest;
import com.ruoyi.system.domain.ai.KnowledgeImportResult;
import com.ruoyi.system.domain.ai.KnowledgeImportedDocument;
import com.ruoyi.system.domain.ai.KnowledgeSource;
import com.ruoyi.system.domain.ai.AiErrorCode;
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
    private static final TypeReference<List<KnowledgeImportedDocument>> IMPORT_LIST_TYPE = new TypeReference<>() { };
    private static final Set<String> IMPORT_EXTENSIONS = Set.of(".pdf", ".docx", ".txt", ".md");

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
            List<BuildSource> sources = loadSources();
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
                for (BuildSource buildSource : sources)
                {
                    KnowledgeSource source = buildSource.source();
                    try
                    {
                        DownloadedSource downloaded = buildSource.localPath() == null
                                ? download(source, rawDir)
                                : copyImported(buildSource, rawDir);
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
                        + "，最低要求 " + properties.getMinimumSourceCount(),
                        AiErrorCode.KNOWLEDGE_NOT_READY.getCode());
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
            throw new ServiceException("知识库构建失败：" + safeError(e),
                    AiErrorCode.TOOL_FAILURE.getCode());
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
            throw new ServiceException("知识库状态读取失败", AiErrorCode.KNOWLEDGE_NOT_READY.getCode());
        }
    }

    @Override
    public synchronized KnowledgeImportResult importDocument(KnowledgeImportRequest request)
    {
        validateImport(request);
        try
        {
            String filename = safeFilename(request.getOriginalFilename());
            String extension = extension(filename);
            String checksum = sha256(request.getContent());
            Path root = knowledgeRoot();
            Path importDir = root.resolve("imports").normalize();
            Path filesDir = importDir.resolve("files").normalize();
            ensureChildPath(root, filesDir);
            Files.createDirectories(filesDir);

            List<KnowledgeImportedDocument> documents = readImportCatalog(importDir);
            KnowledgeImportedDocument existing = documents.stream()
                    .filter(item -> checksum.equals(item.getChecksum()))
                    .findFirst().orElse(null);
            if (existing != null)
            {
                return importResult(existing, true);
            }

            String sourceId = "local-" + checksum.substring(0, 16);
            String storageFilename = sourceId + extension;
            Path target = filesDir.resolve(storageFilename).normalize();
            ensureChildPath(filesDir, target);
            Files.write(target, request.getContent());

            KnowledgeImportedDocument document = new KnowledgeImportedDocument();
            document.setSourceId(sourceId);
            document.setStorageFilename(storageFilename);
            document.setOriginalFilename(filename);
            document.setTitle(request.getTitle().trim());
            document.setAuthority(request.getAuthority().trim());
            document.setCategory(request.getCategory().trim());
            document.setSourceUrl(StringUtils.isBlank(request.getSourceUrl()) ? "" : request.getSourceUrl().trim());
            document.setChecksum(checksum);
            document.setImportedAt(OffsetDateTime.now());
            documents.add(document);
            writeJsonAtomic(importDir.resolve("catalog.json"), documents);
            return importResult(document, false);
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ServiceException("知识文件导入失败", AiErrorCode.TOOL_FAILURE.getCode());
        }
    }

    @Override
    public synchronized List<KnowledgeImportedDocument> listImportedDocuments()
    {
        try
        {
            return List.copyOf(readImportCatalog(knowledgeRoot().resolve("imports")));
        }
        catch (IOException e)
        {
            throw new ServiceException("知识文件目录读取失败", AiErrorCode.TOOL_FAILURE.getCode());
        }
    }

    private List<BuildSource> loadSources() throws IOException
    {
        List<BuildSource> sources = new ArrayList<>();
        if (properties.isRemoteSourcesEnabled())
        {
            List<KnowledgeSource> remoteSources;
            try (InputStream input = new ClassPathResource("ai/knowledge-sources.json").getInputStream())
            {
                remoteSources = objectMapper.readValue(input, SOURCE_LIST_TYPE);
            }
            for (KnowledgeSource source : remoteSources)
            {
                validateUri(URI.create(source.getUrl()));
                sources.add(new BuildSource(source, null, null));
            }
        }

        Path root = knowledgeRoot();
        Path filesDir = root.resolve("imports").resolve("files").normalize();
        for (KnowledgeImportedDocument document : readImportCatalog(root.resolve("imports")))
        {
            Path localPath = filesDir.resolve(document.getStorageFilename()).normalize();
            ensureChildPath(filesDir, localPath);
            if (!Files.isRegularFile(localPath))
            {
                throw new IOException("导入文件不存在：" + document.getOriginalFilename());
            }
            KnowledgeSource source = new KnowledgeSource();
            source.setId(document.getSourceId());
            source.setTitle(document.getTitle());
            source.setAuthority(document.getAuthority());
            source.setUrl(document.getSourceUrl());
            source.setLanguage("zh-CN");
            source.setCategory(document.getCategory());
            sources.add(new BuildSource(source, localPath, document.getChecksum()));
        }

        Set<String> ids = new HashSet<>();
        for (BuildSource buildSource : sources)
        {
            KnowledgeSource source = buildSource.source();
            if (StringUtils.isBlank(source.getId()) || !source.getId().matches("[a-z0-9-]+")
                    || StringUtils.isBlank(source.getTitle()) || StringUtils.isBlank(source.getAuthority()))
            {
                throw new IOException("知识来源定义不完整");
            }
            if (!ids.add(source.getId()))
            {
                throw new IOException("知识来源ID重复：" + source.getId());
            }
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

    private DownloadedSource copyImported(BuildSource buildSource, Path rawDir) throws Exception
    {
        Path sourcePath = buildSource.localPath();
        long size = Files.size(sourcePath);
        if (size <= 0 || size > properties.getMaxFileSize())
        {
            throw new IOException("导入文件大小无效");
        }
        byte[] bytes = Files.readAllBytes(sourcePath);
        String checksum = sha256(bytes);
        if (!checksum.equals(buildSource.checksum()))
        {
            throw new IOException("导入文件校验失败，请重新上传");
        }
        Path target = rawDir.resolve(buildSource.source().getId() + extension(sourcePath.getFileName().toString()))
                .normalize();
        ensureChildPath(rawDir, target);
        Files.copy(sourcePath, target, StandardCopyOption.REPLACE_EXISTING);
        return new DownloadedSource(target, checksum);
    }

    private String extractText(Path path) throws IOException
    {
        String suffix = extension(path.getFileName().toString());
        if (".txt".equals(suffix) || ".md".equals(suffix))
        {
            return Files.readString(path, StandardCharsets.UTF_8);
        }
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

    private void writeJsonAtomic(Path path, Object value) throws IOException
    {
        Files.createDirectories(path.getParent());
        Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(temporary.toFile(), value);
        try
        {
            Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        }
        catch (AtomicMoveNotSupportedException e)
        {
            Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private List<KnowledgeImportedDocument> readImportCatalog(Path importDir) throws IOException
    {
        Path root = knowledgeRoot();
        Path catalog = importDir.resolve("catalog.json").normalize();
        ensureChildPath(root, catalog);
        if (!Files.isRegularFile(catalog))
        {
            return new ArrayList<>();
        }
        List<KnowledgeImportedDocument> documents = objectMapper.readValue(catalog.toFile(), IMPORT_LIST_TYPE);
        if (documents == null)
        {
            return new ArrayList<>();
        }
        for (KnowledgeImportedDocument document : documents)
        {
            if (StringUtils.isBlank(document.getSourceId())
                    || !document.getSourceId().matches("local-[a-f0-9]{16}")
                    || StringUtils.isBlank(document.getStorageFilename())
                    || !document.getStorageFilename().matches("local-[a-f0-9]{16}\\.(pdf|docx|txt|md)"))
            {
                throw new IOException("导入知识目录包含无效记录");
            }
        }
        return new ArrayList<>(documents);
    }

    private void validateImport(KnowledgeImportRequest request)
    {
        if (request == null || request.getContent() == null || request.getContent().length == 0)
        {
            throw importError("知识文件不能为空");
        }
        if (request.getContent().length > properties.getMaxFileSize())
        {
            throw importError("知识文件超过大小限制");
        }
        String filename = safeFilename(request.getOriginalFilename());
        if (!IMPORT_EXTENSIONS.contains(extension(filename)))
        {
            throw importError("知识文件仅支持PDF、DOCX、TXT或Markdown格式");
        }
        validateText(request.getTitle(), 200, "资料标题");
        validateText(request.getAuthority(), 200, "发布机构");
        validateText(request.getCategory(), 100, "资料分类");
        if (StringUtils.isNotBlank(request.getSourceUrl()))
        {
            if (request.getSourceUrl().length() > 1000)
            {
                throw importError("来源链接过长");
            }
            URI uri;
            try
            {
                uri = URI.create(request.getSourceUrl().trim());
            }
            catch (IllegalArgumentException e)
            {
                throw importError("来源链接格式无效");
            }
            if (!"https".equalsIgnoreCase(uri.getScheme()) || StringUtils.isBlank(uri.getHost()))
            {
                throw importError("来源链接必须是有效的HTTPS地址");
            }
        }
    }

    private void validateText(String value, int maxLength, String label)
    {
        if (StringUtils.isBlank(value) || value.trim().length() > maxLength)
        {
            throw importError(label + "不能为空且不能超过" + maxLength + "个字符");
        }
    }

    private ServiceException importError(String message)
    {
        return new ServiceException(message, AiErrorCode.PARAMETER_ERROR.getCode());
    }

    private String safeFilename(String value)
    {
        if (StringUtils.isBlank(value))
        {
            throw importError("知识文件名不能为空");
        }
        String normalized = value.replace('\\', '/');
        String filename = normalized.substring(normalized.lastIndexOf('/') + 1).trim();
        if (filename.isEmpty() || filename.length() > 240)
        {
            throw importError("知识文件名无效");
        }
        return filename;
    }

    private String extension(String filename)
    {
        int dot = filename.lastIndexOf('.');
        return dot < 0 ? "" : filename.substring(dot).toLowerCase(Locale.ROOT);
    }

    private Path knowledgeRoot()
    {
        return Path.of(properties.getWorkDir()).toAbsolutePath().normalize();
    }

    private KnowledgeImportResult importResult(KnowledgeImportedDocument document, boolean duplicate)
    {
        KnowledgeImportResult result = new KnowledgeImportResult();
        result.setSourceId(document.getSourceId());
        result.setTitle(document.getTitle());
        result.setOriginalFilename(document.getOriginalFilename());
        result.setChecksum(document.getChecksum());
        result.setDuplicate(duplicate);
        return result;
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

    private record BuildSource(KnowledgeSource source, Path localPath, String checksum) { }
}
