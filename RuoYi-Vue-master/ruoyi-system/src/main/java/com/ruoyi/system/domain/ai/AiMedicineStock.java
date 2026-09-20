package com.ruoyi.system.domain.ai;

/**
 * AI 查询使用的药品库存只读视图。
 */
public class AiMedicineStock
{
    private Long medId;
    private String medCode;
    private String medName;
    private String tradeName;
    private String medSpec;
    private String unit;
    private String categoryName;
    private String manufacturer;
    private String approvalNo;
    private String storageCond;
    private Long stockQty;
    private Long availableQty;
    private Long stockMin;
    private Long stockMax;
    private String stockStatus;

    public Long getMedId() { return medId; }
    public void setMedId(Long medId) { this.medId = medId; }
    public String getMedCode() { return medCode; }
    public void setMedCode(String medCode) { this.medCode = medCode; }
    public String getMedName() { return medName; }
    public void setMedName(String medName) { this.medName = medName; }
    public String getTradeName() { return tradeName; }
    public void setTradeName(String tradeName) { this.tradeName = tradeName; }
    public String getMedSpec() { return medSpec; }
    public void setMedSpec(String medSpec) { this.medSpec = medSpec; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    public String getApprovalNo() { return approvalNo; }
    public void setApprovalNo(String approvalNo) { this.approvalNo = approvalNo; }
    public String getStorageCond() { return storageCond; }
    public void setStorageCond(String storageCond) { this.storageCond = storageCond; }
    public Long getStockQty() { return stockQty; }
    public void setStockQty(Long stockQty) { this.stockQty = stockQty; }
    public Long getAvailableQty() { return availableQty; }
    public void setAvailableQty(Long availableQty) { this.availableQty = availableQty; }
    public Long getStockMin() { return stockMin; }
    public void setStockMin(Long stockMin) { this.stockMin = stockMin; }
    public Long getStockMax() { return stockMax; }
    public void setStockMax(Long stockMax) { this.stockMax = stockMax; }
    public String getStockStatus() { return stockStatus; }
    public void setStockStatus(String stockStatus) { this.stockStatus = stockStatus; }
}
