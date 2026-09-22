package com.ruoyi.system.service.impl;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.ai.knowledge.KnowledgeTextChunker;
import com.ruoyi.system.config.AiKnowledgeProperties;
import com.ruoyi.system.domain.ai.KnowledgeBuildResult;
import com.ruoyi.system.domain.ai.KnowledgeImportRequest;
import com.ruoyi.system.domain.ai.KnowledgeImportResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AiKnowledgeImportServiceTest
{
    @TempDir
    Path workDir;

    @Test
    void shouldImportDeduplicateRebuildAndRetrieveMarkdownDocument()
    {
        AiKnowledgeServiceImpl service = service();
        String text = "# 冷链管理制度\n胰岛素必须在二至八摄氏度冷藏运输，不得冷冻。".repeat(12);
        KnowledgeImportRequest request = request("冷链制度.md", text.getBytes(StandardCharsets.UTF_8));

        KnowledgeImportResult first = service.importDocument(request);
        KnowledgeImportResult duplicate = service.importDocument(request);
        KnowledgeBuildResult build = service.rebuild();
        AiKnowledgeSearchServiceImpl search = new AiKnowledgeSearchServiceImpl(properties(), objectMapper());

        assertThat(first.isDuplicate()).isFalse();
        assertThat(duplicate.isDuplicate()).isTrue();
        assertThat(service.listImportedDocuments()).hasSize(1);
        assertThat(build.getSuccessCount()).isEqualTo(1);
        assertThat(search.search("胰岛素冷链运输温度", 3)).isNotEmpty();
        assertThat(search.search("胰岛素冷链运输温度", 3).get(0).getTitle()).isEqualTo("冷链制度");
    }

    @Test
    void shouldAcceptRequiredDocumentTypesAndRejectExecutableFiles()
    {
        AiKnowledgeServiceImpl service = service();
        byte[] content = "用于验证文件类型的药房资料内容".getBytes(StandardCharsets.UTF_8);

        service.importDocument(request("制度.txt", content));
        service.importDocument(request("说明书.pdf", "pdf".getBytes(StandardCharsets.UTF_8)));
        service.importDocument(request("说明书.docx", "docx".getBytes(StandardCharsets.UTF_8)));

        assertThat(service.listImportedDocuments()).hasSize(3);
        assertThatThrownBy(() -> service.importDocument(request("脚本.exe", content)))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("PDF、DOCX、TXT或Markdown");
    }

    private AiKnowledgeServiceImpl service()
    {
        AiKnowledgeProperties properties = properties();
        return new AiKnowledgeServiceImpl(properties, new KnowledgeTextChunker(properties), objectMapper());
    }

    private AiKnowledgeProperties properties()
    {
        AiKnowledgeProperties properties = new AiKnowledgeProperties();
        properties.setWorkDir(workDir.toString());
        properties.setRemoteSourcesEnabled(false);
        properties.setMinimumSourceCount(1);
        return properties;
    }

    private ObjectMapper objectMapper()
    {
        return new ObjectMapper().findAndRegisterModules();
    }

    private KnowledgeImportRequest request(String fileName, byte[] content)
    {
        KnowledgeImportRequest request = new KnowledgeImportRequest();
        request.setOriginalFilename(fileName);
        request.setContent(content);
        request.setTitle(fileName.substring(0, fileName.lastIndexOf('.')));
        request.setAuthority("示例医院药学部");
        request.setCategory("药房制度");
        request.setSourceUrl("https://example.org/pharmacy-policy");
        return request;
    }
}
