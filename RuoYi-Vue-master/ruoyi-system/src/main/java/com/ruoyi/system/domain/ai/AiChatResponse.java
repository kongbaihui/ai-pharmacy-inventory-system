package com.ruoyi.system.domain.ai;

/**
 * AI 对话响应。
 */
public class AiChatResponse
{
    private final String reply;

    private final String sessionId;

    public AiChatResponse(String reply, String sessionId)
    {
        this.reply = reply;
        this.sessionId = sessionId;
    }

    public String getReply()
    {
        return reply;
    }

    public String getSessionId()
    {
        return sessionId;
    }
}
