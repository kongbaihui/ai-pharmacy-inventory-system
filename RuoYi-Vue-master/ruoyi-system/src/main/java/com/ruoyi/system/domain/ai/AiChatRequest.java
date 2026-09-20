package com.ruoyi.system.domain.ai;

import java.util.ArrayList;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * AI 对话请求。
 */
public class AiChatRequest
{
    @NotBlank(message = "问题不能为空")
    @Size(max = 2000, message = "问题不能超过2000个字符")
    private String message;

    @Valid
    @Size(max = 100, message = "历史消息不能超过100条")
    private List<AiChatMessage> history = new ArrayList<>();

    @Size(max = 100, message = "会话标识不能超过100个字符")
    private String sessionId;

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public List<AiChatMessage> getHistory()
    {
        return history;
    }

    public void setHistory(List<AiChatMessage> history)
    {
        this.history = history;
    }

    public String getSessionId()
    {
        return sessionId;
    }

    public void setSessionId(String sessionId)
    {
        this.sessionId = sessionId;
    }
}
