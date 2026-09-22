package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.ai.KnowledgeBuildResult;
import com.ruoyi.system.domain.ai.KnowledgeImportRequest;
import com.ruoyi.system.domain.ai.KnowledgeImportResult;
import com.ruoyi.system.domain.ai.KnowledgeImportedDocument;

/**
 * AI 知识语料构建服务。
 */
public interface IAiKnowledgeService
{
    KnowledgeBuildResult rebuild();

    KnowledgeBuildResult getCurrentStatus();

    KnowledgeImportResult importDocument(KnowledgeImportRequest request);

    List<KnowledgeImportedDocument> listImportedDocuments();
}
