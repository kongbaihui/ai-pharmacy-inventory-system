package com.ruoyi.system.service.impl;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.ai.tool.AiInventoryTools;
import com.ruoyi.system.domain.ai.AiInventoryMonthlyMetrics;
import com.ruoyi.system.domain.ai.AiReportStreamEvent;
import com.ruoyi.system.domain.ai.AiErrorCode;
import com.ruoyi.system.mapper.AiInventoryMapper;
import com.ruoyi.system.service.IAiInventoryReportService;
import reactor.core.publisher.Flux;

/**
 * 先聚合确定性指标，再让模型解释月报。
 */
@Service
public class AiInventoryReportServiceImpl implements IAiInventoryReportService
{
    private static final Logger log = LoggerFactory.getLogger(AiInventoryReportServiceImpl.class);
    private static final String METRIC_BASIS =
            "库存流量按med_stock_flow.flow_time归属月份，采用[月初,下月月初)；风险指标为报告生成时实时快照";
    private static final String REPORT_SYSTEM_PROMPT = """
            你是医院药品库存分析助手。只能解释用户提供的JSON指标，不得新增、推测或修改数字。
            报告固定包含：概览、进销情况、库存风险、重点药品、补货建议、过期清理建议、结论。
            若hasBusinessData为false，明确写“本月无业务数据”，但可以说明当前风险快照。
            使用简洁、合法的中文Markdown；每个标题必须单独成行并写成“# 标题”或“## 标题”，
            标题、正文、列表和表格之间各保留一个空行，列表标记后保留空格，表格每行独占一行。
            不要输出HTML；不得声称已执行采购、清理或库存修改。
            """;

    private final ChatClient reportClient;
    private final AiInventoryMapper inventoryMapper;
    private final AiInventoryTools inventoryTools;
    private final ObjectMapper objectMapper;

    public AiInventoryReportServiceImpl(ObjectProvider<ChatClient.Builder> builderProvider,
            AiInventoryMapper inventoryMapper, AiInventoryTools inventoryTools, ObjectMapper objectMapper)
    {
        ChatClient.Builder builder = builderProvider.getIfAvailable();
        this.reportClient = builder == null ? null : builder.defaultSystem(REPORT_SYSTEM_PROMPT).build();
        this.inventoryMapper = inventoryMapper;
        this.inventoryTools = inventoryTools;
        this.objectMapper = objectMapper;
    }

    @Override
    public AiInventoryMonthlyMetrics getMonthlyMetrics(YearMonth month)
    {
        try
        {
            LocalDateTime start = month.atDay(1).atStartOfDay();
            LocalDateTime end = month.plusMonths(1).atDay(1).atStartOfDay();
            AiInventoryMonthlyMetrics metrics = inventoryMapper.selectMonthlyMetrics(start, end);
            if (metrics == null)
            {
                metrics = new AiInventoryMonthlyMetrics();
            }
            metrics.setMonth(month.toString());
            metrics.setPeriodStart(start);
            metrics.setPeriodEndExclusive(end);
            metrics.setGeneratedAt(LocalDateTime.now());
            metrics.setMetricBasis(METRIC_BASIS);
            metrics.setHasBusinessData(value(metrics.getMovementCount()) > 0);
            metrics.setTopOutbound(safeList(inventoryMapper.selectMonthlyTopOutbound(start, end, 5)));
            metrics.setExpiredCleanupCandidates(inventoryTools.listExpiredCleanupCandidates(10));
            metrics.setReplenishmentAdvice(inventoryTools.listReplenishmentAdvice(30, 10));
            return metrics;
        }
        catch (RuntimeException exception)
        {
            log.warn("Monthly inventory metrics failed month={} type={}", month,
                    exception.getClass().getSimpleName());
            throw new ServiceException(AiErrorCode.REPORT_DATA_ERROR.getMessage(),
                    AiErrorCode.REPORT_DATA_ERROR.getCode());
        }
    }

    @Override
    public Flux<AiReportStreamEvent> streamReport(YearMonth month)
    {
        String requestId = UUID.randomUUID().toString();
        AiInventoryMonthlyMetrics metrics = getMonthlyMetrics(month);
        AiReportStreamEvent start = event("start", null, requestId, null);
        start.setMetrics(metrics);
        if (reportClient == null)
        {
            return Flux.just(start, event("error", "AI 服务未启用，请联系管理员完成配置",
                    requestId, "AI_NOT_CONFIGURED"));
        }
        try
        {
            String input = objectMapper.writeValueAsString(metrics);
            Flux<AiReportStreamEvent> deltas = reportClient.prompt()
                    .user("请根据以下确定性指标生成" + month + "库存月报：\n" + input)
                    .stream().content()
                    .filter(content -> content != null && !content.isEmpty())
                    .map(content -> event("delta", content, requestId, null));
            return Flux.concat(Flux.just(start), deltas,
                    Flux.just(event("done", null, requestId, null)))
                    .onErrorResume(exception ->
                    {
                        log.warn("AI monthly report failed requestId={} type={}", requestId,
                                exception.getClass().getSimpleName());
                        return Flux.just(event("error", "月报生成暂不可用，请稍后重试",
                                requestId, "MODEL_UNAVAILABLE"));
                    });
        }
        catch (JsonProcessingException e)
        {
            return Flux.just(start, event("error", "月报指标序列化失败", requestId, "REPORT_DATA_ERROR"));
        }
    }

    private <T> List<T> safeList(List<T> items)
    {
        return items == null ? List.of() : items;
    }

    private long value(Long number)
    {
        return number == null ? 0 : number;
    }

    private AiReportStreamEvent event(String type, String content, String requestId, String code)
    {
        AiReportStreamEvent event = new AiReportStreamEvent();
        event.setType(type);
        event.setContent(content);
        event.setRequestId(requestId);
        event.setCode(code);
        return event;
    }
}
