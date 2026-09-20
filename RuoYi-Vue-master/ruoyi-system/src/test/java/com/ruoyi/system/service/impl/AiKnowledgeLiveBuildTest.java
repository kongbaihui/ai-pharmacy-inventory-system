package com.ruoyi.system.service.impl;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.system.ai.knowledge.KnowledgeTextChunker;
import com.ruoyi.system.config.AiKnowledgeProperties;
import com.ruoyi.system.domain.ai.KnowledgeBuildResult;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 需要外网，仅在显式开启时验证全部权威来源的真实下载与文本抽取。
 */
class AiKnowledgeLiveBuildTest
{
    @Test
    @EnabledIfEnvironmentVariable(named = "RUN_KNOWLEDGE_INGESTION", matches = "true")
    void shouldBuildLocalCorpusFromOfficialSources()
    {
        AiKnowledgeProperties properties = new AiKnowledgeProperties();
        properties.setWorkDir("./runtime/ai/knowledge");
        KnowledgeTextChunker chunker = new KnowledgeTextChunker(properties);
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        AiKnowledgeServiceImpl service = new AiKnowledgeServiceImpl(properties, chunker, objectMapper);

        KnowledgeBuildResult result = service.rebuild();

        assertThat(result.getStatus()).isIn("ready", "partial");
        assertThat(result.getSuccessCount()).isGreaterThanOrEqualTo(properties.getMinimumSourceCount());
        assertThat(result.getChunkCount()).isGreaterThan(result.getSuccessCount());
        assertThat(Files.isRegularFile(Path.of(properties.getWorkDir(), "current.json"))).isTrue();
    }
}
