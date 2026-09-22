package com.ruoyi.system.ai.tool;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ToolContext;
import com.ruoyi.system.domain.ai.AiCitationCollector;
import com.ruoyi.system.domain.ai.KnowledgeSearchResult;
import com.ruoyi.system.service.IAiKnowledgeSearchService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiKnowledgeToolsTest
{
    private final IAiKnowledgeSearchService searchService = mock(IAiKnowledgeSearchService.class);
    private final AiKnowledgeTools tools = new AiKnowledgeTools(searchService);

    @Test
    void shouldUseDefaultLimit()
    {
        tools.searchAuthoritativeKnowledge("药品储存", null, new ToolContext(java.util.Map.of()));

        verify(searchService).search("药品储存", 4);
    }

    @Test
    void shouldClampLimit()
    {
        tools.searchAuthoritativeKnowledge("合理用药", 100, new ToolContext(java.util.Map.of()));

        verify(searchService).search("合理用药", 6);
    }

    @Test
    void shouldCollectOnlySourcesActuallyReturnedBySearch()
    {
        KnowledgeSearchResult result = new KnowledgeSearchResult();
        result.setSourceId("who-storage");
        result.setCitationId("who-storage-0001");
        result.setTitle("药品储存规范");
        result.setAuthority("WHO");
        result.setUrl("https://www.who.int/example");
        result.setCategory("储存与配送");
        when(searchService.search("冷链", 4)).thenReturn(List.of(result, result));
        AiCitationCollector collector = new AiCitationCollector();

        tools.searchAuthoritativeKnowledge("冷链", null,
                new ToolContext(java.util.Map.of(AiCitationCollector.CONTEXT_KEY, collector)));

        assertThat(collector.snapshot()).hasSize(1);
        assertThat(collector.snapshot().get(0).getSourceId()).isEqualTo("who-storage");
        assertThat(collector.snapshot().get(0).getCitationIds()).containsExactly("who-storage-0001");
    }
}
