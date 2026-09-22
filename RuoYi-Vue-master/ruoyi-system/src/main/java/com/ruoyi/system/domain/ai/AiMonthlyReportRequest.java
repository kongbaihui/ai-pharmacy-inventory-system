package com.ruoyi.system.domain.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 月报生成请求。
 */
public class AiMonthlyReportRequest
{
    @NotBlank(message = "月份不能为空")
    @Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])", message = "月份格式必须为yyyy-MM")
    private String month;

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }
}
