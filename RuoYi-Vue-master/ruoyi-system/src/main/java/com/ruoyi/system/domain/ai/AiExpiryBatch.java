package com.ruoyi.system.domain.ai;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * AI 查询使用的效期批次只读视图。
 */
public class AiExpiryBatch
{
    private Long medId;
    private String medCode;
    private String medName;
    private String medSpec;
    private String unit;
    private String batchNo;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date expireDate;
    private Long remainDays;
    private Long remainQty;
    private String supplierName;

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
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public Date getExpireDate() { return expireDate; }
    public void setExpireDate(Date expireDate) { this.expireDate = expireDate; }
    public Long getRemainDays() { return remainDays; }
    public void setRemainDays(Long remainDays) { this.remainDays = remainDays; }
    public Long getRemainQty() { return remainQty; }
    public void setRemainQty(Long remainQty) { this.remainQty = remainQty; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
}
