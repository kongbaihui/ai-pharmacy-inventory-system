package com.ruoyi.system.domain.ai;

/**
 * AI 流式对话事件。
 */
public class AiChatStreamEvent
{
    private String type;
    private String content;
    private String sessionId;

    public AiChatStreamEvent()
    {
    }

    public AiChatStreamEvent(String type, String content, String sessionId)
    {
        this.type = type;
        this.content = content;
        this.sessionId = sessionId;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
}
