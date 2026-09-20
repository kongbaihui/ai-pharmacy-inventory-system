package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.ai.KnowledgeSearchResult;

/**
 * 本地权威知识检索服务。
 */
public interface IAiKnowledgeSearchService
{
    List<KnowledgeSearchResult> search(String query, int limit);
}
