package com.ruoyi.system.service;

import com.ruoyi.system.domain.ai.AiChatRequest;
import com.ruoyi.system.domain.ai.AiChatResponse;

/**
 * AI 对话服务。
 */
public interface IAiChatService
{
    AiChatResponse chat(AiChatRequest request);
}
