package com.ruoyi.system.domain.ai;

/**
 * AI 查询使用的库存总览只读视图。
 */
public class AiInventoryOverview
{
    private Long medicineCount;
    private Long stockQty;
    private Long lowStockCount;
    private Long overstockCount;
    private Long expiringBatchCount;
    private Long expiredBatchCount;

    public Long getMedicineCount() { return medicineCount; }
    public void setMedicineCount(Long medicineCount) { this.medicineCount = medicineCount; }
    public Long getStockQty() { return stockQty; }
    public void setStockQty(Long stockQty) { this.stockQty = stockQty; }
    public Long getLowStockCount() { return lowStockCount; }
    public void setLowStockCount(Long lowStockCount) { this.lowStockCount = lowStockCount; }
    public Long getOverstockCount() { return overstockCount; }
    public void setOverstockCount(Long overstockCount) { this.overstockCount = overstockCount; }
    public Long getExpiringBatchCount() { return expiringBatchCount; }
    public void setExpiringBatchCount(Long expiringBatchCount) { this.expiringBatchCount = expiringBatchCount; }
    public Long getExpiredBatchCount() { return expiredBatchCount; }
    public void setExpiredBatchCount(Long expiredBatchCount) { this.expiredBatchCount = expiredBatchCount; }
}
