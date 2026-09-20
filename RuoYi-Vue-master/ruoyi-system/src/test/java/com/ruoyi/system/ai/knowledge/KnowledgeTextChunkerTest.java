package com.ruoyi.system.ai.knowledge;

import java.util.List;
import org.junit.jupiter.api.Test;
import com.ruoyi.system.config.AiKnowledgeProperties;

import static org.assertj.core.api.Assertions.assertThat;

class KnowledgeTextChunkerTest
{
    @Test
    void shouldNormalizeWhitespaceAndPreferSentenceBoundaries()
    {
        AiKnowledgeProperties properties = new AiKnowledgeProperties();
        properties.setChunkSize(300);
        properties.setChunkOverlap(30);
        KnowledgeTextChunker chunker = new KnowledgeTextChunker(properties);
        String text = ("第一段包含连续空格。  第二段包含制表符\t并且有内容。\r\n"
                + "第三段继续说明药品储存、运输和质量管理要求。").repeat(12);

        List<String> chunks = chunker.split(text);

        assertThat(chunks).hasSizeGreaterThan(1);
        assertThat(chunks).allMatch(chunk -> chunk.length() <= 300);
        assertThat(chunks).allMatch(chunk -> !chunk.contains("\r") && !chunk.contains("\t"));
        assertThat(chunks.get(0)).endsWith("。");
    }

    @Test
    void shouldReturnNoChunksForBlankText()
    {
        KnowledgeTextChunker chunker = new KnowledgeTextChunker(new AiKnowledgeProperties());

        assertThat(chunker.split(" \r\n\t ")).isEmpty();
        assertThat(chunker.split(null)).isEmpty();
    }
}
