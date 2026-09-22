package com.ruoyi.system.domain.ai;

import java.util.List;

/**
 * AI 对话响应。
 */
public class AiChatResponse
{
    private final String reply;

    private final String sessionId;

    private final String requestId;

    private final List<AiCitationSource> sources;

    public AiChatResponse(String reply, String sessionId, String requestId, List<AiCitationSource> sources)
    {
        this.reply = reply;
        this.sessionId = sessionId;
        this.requestId = requestId;
        this.sources = sources;
    }

    public String getReply()
    {
        return reply;
    }

    public String getSessionId()
    {
        return sessionId;
    }

    public String getRequestId() { return requestId; }

    public List<AiCitationSource> getSources() { return sources; }
}
