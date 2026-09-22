package com.ruoyi.system.ai.tool;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.ai.AiExpiryBatch;
import com.ruoyi.system.domain.ai.AiExpiredCleanupCandidate;
import com.ruoyi.system.domain.ai.AiDemandSnapshot;
import com.ruoyi.system.domain.ai.AiInventoryOverview;
import com.ruoyi.system.domain.ai.AiInventoryOperationsBrief;
import com.ruoyi.system.domain.ai.AiMedicineStock;
import com.ruoyi.system.domain.ai.AiReplenishmentAdvice;
import com.ruoyi.system.mapper.AiInventoryMapper;

/**
 * 提供给 AI 的只读库存查询工具。
 */
@Component
public class AiInventoryTools
{
    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 20;
    private static final int DEFAULT_EXPIRY_DAYS = 90;
    private static final int MAX_EXPIRY_DAYS = 365;
    private static final int DEFAULT_COVERAGE_DAYS = 30;
    private static final int MIN_COVERAGE_DAYS = 7;
    private static final int MAX_COVERAGE_DAYS = 90;
    private static final int DEMAND_LOOKBACK_DAYS = 30;
    private static final int MAX_DEMAND_CANDIDATES = 500;
    private static final int DEFAULT_ANALYSIS_DAYS = 30;
    private static final int MAX_ANALYSIS_DAYS = 90;
    private static final int DEFAULT_RANKING_LIMIT = 5;
    private static final int MAX_RANKING_LIMIT = 10;

    private final AiInventoryMapper inventoryMapper;

    public AiInventoryTools(AiInventoryMapper inventoryMapper)
    {
        this.inventoryMapper = inventoryMapper;
    }

    @Tool(description = "按药品编码、通用名、商品名或批准文号查询当前库存；仅查询，不会修改库存")
    public List<AiMedicineStock> searchMedicineStock(
            @ToolParam(description = "查询关键词；为空时返回部分在用药品", required = false) String keyword,
            @ToolParam(description = "返回条数，范围1到20", required = false) Integer limit)
    {
        String safeKeyword = StringUtils.isBlank(keyword) ? null : keyword.trim();
        return inventoryMapper.searchMedicineStock(safeKeyword, normalizeLimit(limit));
    }

    @Tool(description = "查询当前库存数量低于库存下限的药品；仅查询，不会生成采购单")
    public List<AiMedicineStock> listLowStock(
            @ToolParam(description = "返回条数，范围1到20", required = false) Integer limit)
    {
        return inventoryMapper.selectLowStock(normalizeLimit(limit));
    }

    @Tool(description = "查询从今天起指定天数内到期且仍有剩余库存的批次，不包含已经过期的批次")
    public List<AiExpiryBatch> listExpiringBatches(
            @ToolParam(description = "未来天数，范围1到365，默认90", required = false) Integer days,
            @ToolParam(description = "返回条数，范围1到20", required = false) Integer limit)
    {
        int safeDays = days == null ? DEFAULT_EXPIRY_DAYS : Math.max(1, Math.min(days, MAX_EXPIRY_DAYS));
        return inventoryMapper.selectExpiringBatches(safeDays, normalizeLimit(limit));
    }

    @Tool(description = "查询已经过期且仍有剩余库存的批次及待审核清理申请；仅生成清理提醒，不会创建或确认清理单")
    public List<AiExpiredCleanupCandidate> listExpiredCleanupCandidates(
            @ToolParam(description = "返回条数，范围1到20", required = false) Integer limit)
    {
        return inventoryMapper.selectExpiredCleanupCandidates(normalizeLimit(limit));
    }

    @Tool(description = "获取在用药品数、库存总量、低库存数、积压数、临期批次数和过期批次数的实时总览")
    public AiInventoryOverview getInventoryOverview()
    {
        return inventoryMapper.selectInventoryOverview();
    }

    @Tool(description = "生成指定时间窗口的结构化库存运营简报，包括出入库量、退库、盘点调整、过期清理、当前风险和出库排行")
    public AiInventoryOperationsBrief getInventoryOperationsBrief(
            @ToolParam(description = "统计过去天数，范围1到90，默认30", required = false) Integer days,
            @ToolParam(description = "出库排行条数，范围1到10，默认5", required = false) Integer rankingLimit)
    {
        int safeDays = days == null ? DEFAULT_ANALYSIS_DAYS : Math.max(1, Math.min(days, MAX_ANALYSIS_DAYS));
        int safeRankingLimit = rankingLimit == null ? DEFAULT_RANKING_LIMIT
                : Math.max(1, Math.min(rankingLimit, MAX_RANKING_LIMIT));
        AiInventoryOperationsBrief brief = inventoryMapper.selectOperationsBrief(safeDays);
        brief.setAnalysisDays(safeDays);
        brief.setTopOutbound(inventoryMapper.selectTopOutbound(safeDays, safeRankingLimit));
        return brief;
    }

    @Tool(description = "根据实时可用库存、库存上下限和近30天实际出库量计算补货建议；仅提供建议，不会创建采购或入库单")
    public List<AiReplenishmentAdvice> listReplenishmentAdvice(
            @ToolParam(description = "希望库存覆盖的未来天数，范围7到90，默认30", required = false) Integer coverageDays,
            @ToolParam(description = "返回条数，范围1到20", required = false) Integer limit)
    {
        int safeCoverageDays = coverageDays == null ? DEFAULT_COVERAGE_DAYS
                : Math.max(MIN_COVERAGE_DAYS, Math.min(coverageDays, MAX_COVERAGE_DAYS));
        return inventoryMapper.selectDemandSnapshots(MAX_DEMAND_CANDIDATES).stream()
                .map(snapshot -> calculateAdvice(snapshot, safeCoverageDays))
                .filter(advice -> advice.getSuggestedOrderQty() > 0)
                .sorted(Comparator.comparingInt(this::urgencyRank)
                        .thenComparing(AiReplenishmentAdvice::getSuggestedOrderQty, Comparator.reverseOrder())
                        .thenComparing(AiReplenishmentAdvice::getMedId))
                .limit(normalizeLimit(limit))
                .toList();
    }

    AiReplenishmentAdvice calculateAdvice(AiDemandSnapshot snapshot, int coverageDays)
    {
        int currentQty = nonNegative(snapshot.getCurrentQty());
        int availableQty = nonNegative(snapshot.getAvailableQty());
        int stockMin = nonNegative(snapshot.getStockMin());
        int stockMax = nonNegative(snapshot.getStockMax());
        int outboundQty = nonNegative(snapshot.getOutboundQty30d());
        BigDecimal dailyOutbound = BigDecimal.valueOf(outboundQty)
                .divide(BigDecimal.valueOf(DEMAND_LOOKBACK_DAYS), 2, RoundingMode.HALF_UP);
        int demandTarget = BigDecimal.valueOf(outboundQty)
                .multiply(BigDecimal.valueOf(coverageDays))
                .divide(BigDecimal.valueOf(DEMAND_LOOKBACK_DAYS), 0, RoundingMode.CEILING)
                .intValue();
        int targetStock = Math.max(stockMin, demandTarget);
        if (stockMax >= stockMin && stockMax > 0)
        {
            targetStock = Math.min(targetStock, stockMax);
        }
        int suggestedQty = Math.max(targetStock - availableQty, 0);
        BigDecimal daysOfSupply = outboundQty == 0 ? null
                : BigDecimal.valueOf(availableQty)
                        .multiply(BigDecimal.valueOf(DEMAND_LOOKBACK_DAYS))
                        .divide(BigDecimal.valueOf(outboundQty), 1, RoundingMode.HALF_UP);

        AiReplenishmentAdvice advice = new AiReplenishmentAdvice();
        advice.setMedId(snapshot.getMedId());
        advice.setMedCode(snapshot.getMedCode());
        advice.setMedName(snapshot.getMedName());
        advice.setMedSpec(snapshot.getMedSpec());
        advice.setUnit(snapshot.getUnit());
        advice.setCurrentQty(currentQty);
        advice.setAvailableQty(availableQty);
        advice.setStockMin(stockMin);
        advice.setStockMax(stockMax);
        advice.setOutboundQty30d(outboundQty);
        advice.setAverageDailyOutbound(dailyOutbound);
        advice.setDaysOfSupply(daysOfSupply);
        advice.setTargetStock(targetStock);
        advice.setSuggestedOrderQty(suggestedQty);
        advice.setUrgency(urgency(availableQty, stockMin, daysOfSupply));
        advice.setReason(reason(outboundQty, coverageDays, stockMax, stockMin, demandTarget));
        return advice;
    }

    private String urgency(int availableQty, int stockMin, BigDecimal daysOfSupply)
    {
        if (availableQty == 0 || daysOfSupply != null && daysOfSupply.compareTo(BigDecimal.valueOf(7)) <= 0)
        {
            return "紧急";
        }
        if (availableQty < stockMin || daysOfSupply != null && daysOfSupply.compareTo(BigDecimal.valueOf(14)) <= 0)
        {
            return "高";
        }
        return "常规";
    }

    private String reason(int outboundQty, int coverageDays, int stockMax, int stockMin, int demandTarget)
    {
        if (outboundQty == 0)
        {
            return "近30天无出库记录，按库存下限补足";
        }
        if (stockMax >= stockMin && stockMax > 0 && demandTarget > stockMax)
        {
            return "按近30天日均出库估算" + coverageDays + "天需求，并受库存上限约束";
        }
        return "按近30天日均出库估算" + coverageDays + "天需求";
    }

    private int urgencyRank(AiReplenishmentAdvice advice)
    {
        return switch (advice.getUrgency())
        {
            case "紧急" -> 0;
            case "高" -> 1;
            default -> 2;
        };
    }

    private int nonNegative(Integer value)
    {
        return value == null ? 0 : Math.max(value, 0);
    }

    private int normalizeLimit(Integer limit)
    {
        return limit == null ? DEFAULT_LIMIT : Math.max(1, Math.min(limit, MAX_LIMIT));
    }
}
