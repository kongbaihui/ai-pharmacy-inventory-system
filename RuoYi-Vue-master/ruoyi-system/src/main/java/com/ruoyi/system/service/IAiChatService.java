package com.ruoyi.system.service;

import reactor.core.publisher.Flux;
import com.ruoyi.system.domain.ai.AiChatRequest;
import com.ruoyi.system.domain.ai.AiChatResponse;
import com.ruoyi.system.domain.ai.AiChatStreamEvent;

/**
 * AI 对话服务。
 */
public interface IAiChatService
{
    AiChatResponse chat(AiChatRequest request);

    Flux<AiChatStreamEvent> stream(AiChatRequest request);
}
