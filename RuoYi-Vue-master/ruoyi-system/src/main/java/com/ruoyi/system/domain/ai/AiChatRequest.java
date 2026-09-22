package com.ruoyi.system.domain.ai;

import java.util.ArrayList;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * AI 对话请求。
 */
public class AiChatRequest
{
    @NotBlank(message = "问题不能为空")
    @Size(max = 2000, message = "问题不能超过2000个字符")
    private String message;

    @Size(max = 20, message = "历史消息不能超过20条")
    private List<@NotNull(message = "历史消息不能为空") @Valid AiChatMessage> history = new ArrayList<>();

    @Size(max = 64, message = "会话标识不能超过64个字符")
    @Pattern(regexp = "[A-Za-z0-9_-]*", message = "会话标识只能包含字母、数字、-和_")
    private String sessionId;

    @JsonIgnore
    @AssertTrue(message = "历史消息总字符不能超过16000")
    public boolean isHistoryWithinTotalLimit()
    {
        if (history == null)
        {
            return true;
        }
        long total = history.stream()
                .filter(item -> item != null && item.getContent() != null)
                .mapToLong(item -> item.getContent().length())
                .sum();
        return total <= 16000;
    }

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
