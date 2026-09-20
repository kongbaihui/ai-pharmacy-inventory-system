package com.ruoyi.system.ai.tool;

import org.junit.jupiter.api.Test;
import com.ruoyi.system.service.IAiKnowledgeSearchService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AiKnowledgeToolsTest
{
    private final IAiKnowledgeSearchService searchService = mock(IAiKnowledgeSearchService.class);
    private final AiKnowledgeTools tools = new AiKnowledgeTools(searchService);

    @Test
    void shouldUseDefaultLimit()
    {
        tools.searchAuthoritativeKnowledge("药品储存", null);

        verify(searchService).search("药品储存", 4);
    }

    @Test
    void shouldClampLimit()
    {
        tools.searchAuthoritativeKnowledge("合理用药", 100);

        verify(searchService).search("合理用药", 6);
    }
}
