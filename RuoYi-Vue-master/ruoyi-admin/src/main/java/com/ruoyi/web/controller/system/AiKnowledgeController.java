package com.ruoyi.web.controller.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.AjaxResult;
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
}
