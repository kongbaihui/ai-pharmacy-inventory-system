package com.ruoyi.system.domain.ai;

import java.util.List;

/**
 * AI 流式对话事件。
 */
public class AiChatStreamEvent
{
    private String type;
    private String content;
    private String sessionId;

    private String requestId;

    private String code;

    private List<AiCitationSource> sources;

    public AiChatStreamEvent()
    {
    }

    public AiChatStreamEvent(String type, String content, String sessionId, String requestId)
    {
        this.type = type;
        this.content = content;
        this.sessionId = sessionId;
        this.requestId = requestId;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public List<AiCitationSource> getSources() { return sources; }
    public void setSources(List<AiCitationSource> sources) { this.sources = sources; }
}
