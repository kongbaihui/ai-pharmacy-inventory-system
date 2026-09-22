package com.ruoyi.web.controller.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.domain.ai.KnowledgeImportRequest;
import com.ruoyi.system.service.IAiKnowledgeService;

/**
 * AI 权威知识语料管理接口。
 */
@RestController
@RequestMapping("/system/ai/knowledge")
public class AiKnowledgeController
{
    @Autowired
    private IAiKnowledgeService knowledgeService;

    @PreAuthorize("@ss.hasPermi('system:ai:chat')")
    @GetMapping("/status")
    public AjaxResult status()
    {
        return AjaxResult.success(knowledgeService.getCurrentStatus());
    }

    @PreAuthorize("@ss.hasPermi('system:ai:knowledge')")
    @PostMapping("/rebuild")
    public AjaxResult rebuild()
    {
        return AjaxResult.success(knowledgeService.rebuild());
    }

    @PreAuthorize("@ss.hasPermi('system:ai:knowledge')")
    @GetMapping("/imports")
    public AjaxResult imports()
    {
        return AjaxResult.success(knowledgeService.listImportedDocuments());
    }

    @PreAuthorize("@ss.hasPermi('system:ai:knowledge')")
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AjaxResult importDocument(@RequestParam("file") MultipartFile file,
            @RequestParam String title, @RequestParam String authority,
            @RequestParam String category,
            @RequestParam(required = false, defaultValue = "") String sourceUrl) throws Exception
    {
        KnowledgeImportRequest request = new KnowledgeImportRequest();
        request.setOriginalFilename(file.getOriginalFilename());
        request.setContent(file.getBytes());
        request.setTitle(title);
        request.setAuthority(authority);
        request.setCategory(category);
        request.setSourceUrl(sourceUrl);
        return AjaxResult.success(knowledgeService.importDocument(request));
    }
}
