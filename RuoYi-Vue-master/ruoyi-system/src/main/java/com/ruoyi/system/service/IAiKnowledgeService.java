package com.ruoyi.system.service;

import com.ruoyi.system.domain.ai.KnowledgeBuildResult;

/**
 * AI 知识语料构建服务。
 */
public interface IAiKnowledgeService
{
    KnowledgeBuildResult rebuild();

    KnowledgeBuildResult getCurrentStatus();
}
