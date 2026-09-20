package com.ruoyi.system.domain.ai;

/**
 * 补货计算所需的只读库存与近30天出库快照。
 */
public class AiDemandSnapshot
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
}
