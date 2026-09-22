package com.ruoyi.system.domain.ai;

import java.time.LocalDate;

/**
 * 已过期且仍有库存的清理候选批次。
 */
public class AiExpiredCleanupCandidate
{
    private Long medId;
    private String medCode;
    private String medName;
    private String medSpec;
    private String unit;
    private Long batchId;
    private String batchNo;
    private LocalDate expireDate;
    private Integer expiredDays;
    private Integer remainQty;
    private String supplierName;
    private Boolean hasPendingClean;
    private Long pendingCleanId;
    private String suggestedAction;

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
    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public LocalDate getExpireDate() { return expireDate; }
    public void setExpireDate(LocalDate expireDate) { this.expireDate = expireDate; }
    public Integer getExpiredDays() { return expiredDays; }
    public void setExpiredDays(Integer expiredDays) { this.expiredDays = expiredDays; }
    public Integer getRemainQty() { return remainQty; }
    public void setRemainQty(Integer remainQty) { this.remainQty = remainQty; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public Boolean getHasPendingClean() { return hasPendingClean; }
    public void setHasPendingClean(Boolean hasPendingClean) { this.hasPendingClean = hasPendingClean; }
    public Long getPendingCleanId() { return pendingCleanId; }
    public void setPendingCleanId(Long pendingCleanId) { this.pendingCleanId = pendingCleanId; }
    public String getSuggestedAction() { return suggestedAction; }
    public void setSuggestedAction(String suggestedAction) { this.suggestedAction = suggestedAction; }
}
