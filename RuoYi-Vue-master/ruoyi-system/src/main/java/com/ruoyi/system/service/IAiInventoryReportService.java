package com.ruoyi.system.service;

import java.time.YearMonth;
import com.ruoyi.system.domain.ai.AiInventoryMonthlyMetrics;
import com.ruoyi.system.domain.ai.AiReportStreamEvent;
import reactor.core.publisher.Flux;

/**
 * AI 月度库存分析服务。
 */
public interface IAiInventoryReportService
{
    AiInventoryMonthlyMetrics getMonthlyMetrics(YearMonth month);

    Flux<AiReportStreamEvent> streamReport(YearMonth month);
}
