package com.ruoyi.system.service.impl;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.config.AiKnowledgeProperties;
import com.ruoyi.system.domain.ai.KnowledgeChunk;
import com.ruoyi.system.domain.ai.KnowledgeSearchResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AiKnowledgeSearchServiceImplTest
{
    private static final String BUILD_ONE = "20260920120000-aaaaaaaa";
    private static final String BUILD_TWO = "20260920130000-bbbbbbbb";

    @TempDir
    Path workDir;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void shouldRankRelevantChineseChunkAndReturnCitation() throws Exception
    {
        writeBuild(BUILD_ONE, List.of(
                chunk("storage-1", "冷链药品运输应持续监测温度，并保存温度记录。",
                        "药品冷链管理规范", "国家药品监督管理部门", "储存与配送", "https://example.gov.cn/cold"),
                chunk("prescription-1", "医师开具处方和药师调剂处方应当遵循安全、有效、经济原则。",
                        "处方管理办法", "原卫生部", "合理用药", "https://example.gov.cn/rx")));
        AiKnowledgeSearchServiceImpl service = service();

        List<KnowledgeSearchResult> results = service.search("冷链运输如何监测温度", 2);

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getTitle()).isEqualTo("药品冷链管理规范");
        assertThat(results.get(0).getAuthority()).isEqualTo("国家药品监督管理部门");
        assertThat(results.get(0).getUrl()).isEqualTo("https://example.gov.cn/cold");
        assertThat(results.get(0).getRelevance()).isPositive();
    }

    @Test
    void shouldReloadIndexWhenPublishedBuildChanges() throws Exception
    {
        writeBuild(BUILD_ONE, List.of(chunk("old-1", "药品储存温度管理。", "旧版", "机构", "储存", "https://example.gov.cn/old")));
        AiKnowledgeSearchServiceImpl service = service();
        assertThat(service.search("药品储存", 1).get(0).getTitle()).isEqualTo("旧版");

        writeBuild(BUILD_TWO, List.of(chunk("new-1", "药品召回分为主动召回和责令召回。", "药品召回管理办法",
                "国家药品监督管理局", "召回管理", "https://example.gov.cn/new")));

        assertThat(service.search("药品召回", 1).get(0).getTitle()).isEqualTo("药品召回管理办法");
    }

    @Test
    void shouldFailClearlyWhenKnowledgeBaseHasNotBeenBuilt()
    {
        assertThatThrownBy(() -> service().search("药品储存", 1))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("尚未构建");
    }

    @Test
    void shouldTokenizeChineseBigramsAndNormalizedEnglish()
    {
        assertThat(AiKnowledgeSearchServiceImpl.tokenize("冷链运输 WHO-2025"))
                .contains("冷链", "链运", "运输", "who", "2025");
    }

    private AiKnowledgeSearchServiceImpl service()
    {
        AiKnowledgeProperties properties = new AiKnowledgeProperties();
        properties.setWorkDir(workDir.toString());
        return new AiKnowledgeSearchServiceImpl(properties, objectMapper);
    }

    private void writeBuild(String buildId, List<KnowledgeChunk> chunks) throws Exception
    {
        Path buildDir = workDir.resolve("builds").resolve(buildId);
        Files.createDirectories(buildDir);
        StringBuilder jsonLines = new StringBuilder();
        for (KnowledgeChunk chunk : chunks)
        {
            jsonLines.append(objectMapper.writeValueAsString(chunk)).append(System.lineSeparator());
        }
        Files.writeString(buildDir.resolve("chunks.jsonl"), jsonLines, StandardCharsets.UTF_8);
        Files.writeString(workDir.resolve("current.json"), objectMapper.writeValueAsString(Map.of("buildId", buildId)),
                StandardCharsets.UTF_8);
    }

    private KnowledgeChunk chunk(String id, String content, String title, String authority,
            String category, String url)
    {
        KnowledgeChunk chunk = new KnowledgeChunk();
        chunk.setId(id);
        chunk.setContent(content);
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("title", title);
        metadata.put("authority", authority);
        metadata.put("category", category);
        metadata.put("url", url);
        chunk.setMetadata(metadata);
        return chunk;
    }
}
