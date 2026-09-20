package com.ruoyi.system.domain.ai;

import java.util.ArrayList;
import java.util.List;

/**
 * 指定时间窗口的库存运营结构化简报。
 */
public class AiInventoryOperationsBrief
{
    private Integer analysisDays;
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

    public Integer getAnalysisDays() { return analysisDays; }
    public void setAnalysisDays(Integer analysisDays) { this.analysisDays = analysisDays; }
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
}
