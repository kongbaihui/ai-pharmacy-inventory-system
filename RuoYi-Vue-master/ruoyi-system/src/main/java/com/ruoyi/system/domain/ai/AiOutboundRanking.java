package com.ruoyi.system.domain.ai;

/**
 * 指定时间窗口内的药品出库排行。
 */
public class AiOutboundRanking
{
    private Long medId;
    private String medCode;
    private String medName;
    private String medSpec;
    private String unit;
    private Long outboundQty;
    private Long outboundTransactions;

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
    public Long getOutboundQty() { return outboundQty; }
    public void setOutboundQty(Long outboundQty) { this.outboundQty = outboundQty; }
    public Long getOutboundTransactions() { return outboundTransactions; }
    public void setOutboundTransactions(Long outboundTransactions) { this.outboundTransactions = outboundTransactions; }
}
