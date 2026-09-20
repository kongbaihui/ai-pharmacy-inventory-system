package com.ruoyi.system.domain.ai;

import java.math.BigDecimal;

/**
 * 基于实时可用库存和近30天出库量计算的补货建议。
 */
public class AiReplenishmentAdvice
{
    private Long medId;
    private String medCode;
    private String medName;
    private String medSpec;
    private String unit;
    private Integer currentQty;
    private Integer availableQty;
    private Integer stockMin;
    private Integer stockMax;
    private Integer outboundQty30d;
    private BigDecimal averageDailyOutbound;
    private BigDecimal daysOfSupply;
    private Integer targetStock;
    private Integer suggestedOrderQty;
    private String urgency;
    private String reason;

    public Long getMedId() { return medId; }
    public void setMedId(Long medId) { this.medId = medId; }
    public String getMedCode() { return medCode; }
    public void setMedCode(String medCode) { this.medCode = medCode; }
    public String getMedName() { return medName; }
    public void setMedName(String medName) { this.medName = medName; }
    public String getMedSpec() { return medSpec; }
    public void setMedSpec(String medSpec) { this.medSpec = medSpec; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public Integer getCurrentQty() { return currentQty; }
    public void setCurrentQty(Integer currentQty) { this.currentQty = currentQty; }
    public Integer getAvailableQty() { return availableQty; }
    public void setAvailableQty(Integer availableQty) { this.availableQty = availableQty; }
    public Integer getStockMin() { return stockMin; }
    public void setStockMin(Integer stockMin) { this.stockMin = stockMin; }
    public Integer getStockMax() { return stockMax; }
    public void setStockMax(Integer stockMax) { this.stockMax = stockMax; }
    public Integer getOutboundQty30d() { return outboundQty30d; }
    public void setOutboundQty30d(Integer outboundQty30d) { this.outboundQty30d = outboundQty30d; }
    public BigDecimal getAverageDailyOutbound() { return averageDailyOutbound; }
    public void setAverageDailyOutbound(BigDecimal averageDailyOutbound) { this.averageDailyOutbound = averageDailyOutbound; }
    public BigDecimal getDaysOfSupply() { return daysOfSupply; }
    public void setDaysOfSupply(BigDecimal daysOfSupply) { this.daysOfSupply = daysOfSupply; }
    public Integer getTargetStock() { return targetStock; }
    public void setTargetStock(Integer targetStock) { this.targetStock = targetStock; }
    public Integer getSuggestedOrderQty() { return suggestedOrderQty; }
    public void setSuggestedOrderQty(Integer suggestedOrderQty) { this.suggestedOrderQty = suggestedOrderQty; }
    public String getUrgency() { return urgency; }
    public void setUrgency(String urgency) { this.urgency = urgency; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
