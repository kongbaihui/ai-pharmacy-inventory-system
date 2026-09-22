package com.ruoyi.system.domain.ai;

/**
 * 月报流式事件。
 */
public class AiReportStreamEvent
{
    private String type;
    private String content;
    private String requestId;
    private String code;
    private AiInventoryMonthlyMetrics metrics;

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public AiInventoryMonthlyMetrics getMetrics() { return metrics; }
    public void setMetrics(AiInventoryMonthlyMetrics metrics) { this.metrics = metrics; }
}
