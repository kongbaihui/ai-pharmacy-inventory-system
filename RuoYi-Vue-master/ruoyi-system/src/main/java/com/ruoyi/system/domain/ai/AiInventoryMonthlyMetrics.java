package com.ruoyi.system.domain.ai;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 可独立核验的月度库存指标。模型只负责解释这些确定性数据。
 */
public class AiInventoryMonthlyMetrics
{
    private String month;
    private LocalDateTime periodStart;
    private LocalDateTime periodEndExclusive;
    private LocalDateTime generatedAt;
    private String metricBasis;
    private boolean hasBusinessData;
    private Long inboundQty;
    private Long outboundQty;
    private Long returnQty;
    private Long adjustmentNetQty;
    private Long expiredCleanQty;
    private Long movementCount;
    private Long lowStockCount;
    private Long expiringBatchCount;
    private Long expiredBatchCount;
    private List<AiOutboundRanking> topOutbound = new ArrayList<>();
    private List<AiExpiredCleanupCandidate> expiredCleanupCandidates = new ArrayList<>();
    private List<AiReplenishmentAdvice> replenishmentAdvice = new ArrayList<>();

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }
    public LocalDateTime getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDateTime periodStart) { this.periodStart = periodStart; }
    public LocalDateTime getPeriodEndExclusive() { return periodEndExclusive; }
    public void setPeriodEndExclusive(LocalDateTime periodEndExclusive) { this.periodEndExclusive = periodEndExclusive; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    public String getMetricBasis() { return metricBasis; }
    public void setMetricBasis(String metricBasis) { this.metricBasis = metricBasis; }
    public boolean isHasBusinessData() { return hasBusinessData; }
    public void setHasBusinessData(boolean hasBusinessData) { this.hasBusinessData = hasBusinessData; }
    public Long getInboundQty() { return inboundQty; }
    public void setInboundQty(Long inboundQty) { this.inboundQty = inboundQty; }
    public Long getOutboundQty() { return outboundQty; }
    public void setOutboundQty(Long outboundQty) { this.outboundQty = outboundQty; }
    public Long getReturnQty() { return returnQty; }
    public void setReturnQty(Long returnQty) { this.returnQty = returnQty; }
    public Long getAdjustmentNetQty() { return adjustmentNetQty; }
    public void setAdjustmentNetQty(Long adjustmentNetQty) { this.adjustmentNetQty = adjustmentNetQty; }
    public Long getExpiredCleanQty() { return expiredCleanQty; }
    public void setExpiredCleanQty(Long expiredCleanQty) { this.expiredCleanQty = expiredCleanQty; }
    public Long getMovementCount() { return movementCount; }
    public void setMovementCount(Long movementCount) { this.movementCount = movementCount; }
    public Long getLowStockCount() { return lowStockCount; }
    public void setLowStockCount(Long lowStockCount) { this.lowStockCount = lowStockCount; }
    public Long getExpiringBatchCount() { return expiringBatchCount; }
    public void setExpiringBatchCount(Long expiringBatchCount) { this.expiringBatchCount = expiringBatchCount; }
    public Long getExpiredBatchCount() { return expiredBatchCount; }
    public void setExpiredBatchCount(Long expiredBatchCount) { this.expiredBatchCount = expiredBatchCount; }
    public List<AiOutboundRanking> getTopOutbound() { return topOutbound; }
    public void setTopOutbound(List<AiOutboundRanking> topOutbound) { this.topOutbound = topOutbound; }
    public List<AiExpiredCleanupCandidate> getExpiredCleanupCandidates() { return expiredCleanupCandidates; }
    public void setExpiredCleanupCandidates(List<AiExpiredCleanupCandidate> expiredCleanupCandidates) { this.expiredCleanupCandidates = expiredCleanupCandidates; }
    public List<AiReplenishmentAdvice> getReplenishmentAdvice() { return replenishmentAdvice; }
    public void setReplenishmentAdvice(List<AiReplenishmentAdvice> replenishmentAdvice) { this.replenishmentAdvice = replenishmentAdvice; }
}
