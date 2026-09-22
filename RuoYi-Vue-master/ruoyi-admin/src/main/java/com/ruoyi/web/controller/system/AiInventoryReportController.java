package com.ruoyi.web.controller.system;

import java.time.YearMonth;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.domain.ai.AiMonthlyReportRequest;
import com.ruoyi.system.domain.ai.AiReportStreamEvent;
import com.ruoyi.system.service.IAiInventoryReportService;
import reactor.core.publisher.Flux;

/**
 * AI 月度库存分析接口。
 */
@Validated
@RestController
@RequestMapping("/system/ai/report")
public class AiInventoryReportController
{
    @Autowired
    private IAiInventoryReportService reportService;

    @PreAuthorize("@ss.hasPermi('system:ai:report')")
    @GetMapping("/data")
    public AjaxResult data(@RequestParam
            @Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])", message = "月份格式必须为yyyy-MM") String month)
    {
        return AjaxResult.success(reportService.getMonthlyMetrics(YearMonth.parse(month)));
    }

    @PreAuthorize("@ss.hasPermi('system:ai:report')")
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<AiReportStreamEvent>> stream(@Valid @RequestBody AiMonthlyReportRequest request)
    {
        return reportService.streamReport(YearMonth.parse(request.getMonth()))
                .map(event -> ServerSentEvent.<AiReportStreamEvent>builder()
                        .event(event.getType())
                        .data(event)
                        .build());
    }
}
