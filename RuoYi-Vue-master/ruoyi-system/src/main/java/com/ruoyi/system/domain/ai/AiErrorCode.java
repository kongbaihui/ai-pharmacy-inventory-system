package com.ruoyi.system.domain.ai;

import java.util.Arrays;

/**
 * AI 接口稳定错误码。
 */
public enum AiErrorCode
{
    PARAMETER_ERROR(5100, "PARAMETER_ERROR", "请求参数不正确"),
    AI_NOT_CONFIGURED(5101, "AI_NOT_CONFIGURED", "AI 服务未启用，请联系管理员完成配置"),
    MODEL_TIMEOUT(5102, "MODEL_TIMEOUT", "AI 服务响应超时，请稍后重试"),
    MODEL_UNAVAILABLE(5103, "MODEL_UNAVAILABLE", "AI 服务暂不可用，请稍后重试"),
    TOOL_FAILURE(5104, "TOOL_FAILURE", "业务数据查询失败，请稍后重试"),
    KNOWLEDGE_NOT_READY(5105, "KNOWLEDGE_NOT_READY", "知识库尚未就绪，请联系管理员构建"),
    TOOL_CALL_LIMIT(5106, "TOOL_CALL_LIMIT", "本轮工具调用次数过多，请缩小问题范围后重试"),
    EMPTY_RESPONSE(5107, "EMPTY_RESPONSE", "AI 服务未返回有效内容，请稍后重试"),
    REPORT_DATA_ERROR(5108, "REPORT_DATA_ERROR", "月报数据查询失败，请稍后重试");

    private final int code;
    private final String key;
    private final String message;

    AiErrorCode(int code, String key, String message)
    {
        this.code = code;
        this.key = key;
        this.message = message;
    }

    public int getCode() { return code; }
    public String getKey() { return key; }
    public String getMessage() { return message; }

    public static AiErrorCode fromCode(Integer code)
    {
        return code == null ? null : Arrays.stream(values())
                .filter(item -> item.code == code)
                .findFirst().orElse(null);
    }
}
