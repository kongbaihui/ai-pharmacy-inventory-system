package com.ruoyi.system.ai.tool;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import com.ruoyi.system.domain.ai.AiCitationCollector;
import com.ruoyi.system.domain.ai.KnowledgeSearchResult;
import com.ruoyi.system.service.IAiKnowledgeSearchService;

/**
 * 提供给 AI 的只读权威知识检索工具。
 */
@Component
public class AiKnowledgeTools
{
    private static final Logger log = LoggerFactory.getLogger(AiKnowledgeTools.class);
    private static final int DEFAULT_LIMIT = 4;
    private static final int MAX_LIMIT = 6;

    private final IAiKnowledgeSearchService searchService;

    public AiKnowledgeTools(IAiKnowledgeSearchService searchService)
    {
        this.searchService = searchService;
    }

    @Tool(description = "检索已构建的政府、WHO和NIH权威药学资料；回答法规、储存、配送、合理用药或药品信息问题前应调用，并引用返回的机构和原始链接")
    public List<KnowledgeSearchResult> searchAuthoritativeKnowledge(
            @ToolParam(description = "需要检索的完整问题或关键词") String query,
            @ToolParam(description = "返回条数，范围1到6，默认4", required = false) Integer limit,
            ToolContext toolContext)
    {
        long startedAt = System.nanoTime();
        int safeLimit = limit == null ? DEFAULT_LIMIT : Math.max(1, Math.min(limit, MAX_LIMIT));
        List<KnowledgeSearchResult> results = searchService.search(query, safeLimit);
        Object value = toolContext == null ? null
                : toolContext.getContext().get(AiCitationCollector.CONTEXT_KEY);
        if (value instanceof AiCitationCollector collector)
        {
            collector.record(results);
        }
        log.info("AI tool completed requestId={} tool=searchAuthoritativeKnowledge durationMs={} resultCount={}",
                requestId(toolContext), elapsedMillis(startedAt), results.size());
        return results;
    }

    private String requestId(ToolContext context)
    {
        Object value = context == null ? null : context.getContext().get("requestId");
        return value == null ? "direct" : String.valueOf(value);
    }

    private long elapsedMillis(long startedAt)
    {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
