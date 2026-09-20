package com.ruoyi.web.controller.system;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.domain.ai.AiChatRequest;
import com.ruoyi.system.domain.ai.AiChatStreamEvent;
import com.ruoyi.system.service.IAiChatService;
import reactor.core.publisher.Flux;

/**
 * AI 对话接口。
 */
@RestController
@RequestMapping("/system/ai")
public class AiChatController
{
    @Autowired
    private IAiChatService aiChatService;

    @PreAuthorize("@ss.hasPermi('system:ai:chat')")
    @PostMapping("/chat")
    public AjaxResult chat(@Valid @RequestBody AiChatRequest request)
    {
        return AjaxResult.success(aiChatService.chat(request));
    }

    @PreAuthorize("@ss.hasPermi('system:ai:chat')")
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<AiChatStreamEvent>> stream(@Valid @RequestBody AiChatRequest request)
    {
        return aiChatService.stream(request)
                .map(event -> ServerSentEvent.<AiChatStreamEvent>builder()
                        .event(event.getType())
                        .data(event)
                        .build());
    }
}
