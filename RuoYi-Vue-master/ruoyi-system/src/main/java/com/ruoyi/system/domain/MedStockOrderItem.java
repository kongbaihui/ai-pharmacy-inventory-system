package com.ruoyi.system.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/** 库存业务单明细对象 med_stock_order_item */
public class MedStockOrderItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long itemId;
    private Long orderId;
    private Long medId;
    private Long batchId;
    private String batchNo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date produceDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date expireDate;

    private Long quantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private String medCode;
    private String medName;
    private String medSpec;
    private String unit;
    private Long remainQty;

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getMedId() { return medId; }
    public void setMedId(Long medId) { this.medId = medId; }
    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public Date getProduceDate() { return produceDate; }
    public void setProduceDate(Date produceDate) { this.produceDate = produceDate; }
    public Date getExpireDate() { return expireDate; }
    public void setExpireDate(Date expireDate) { this.expireDate = expireDate; }
    public Long getQuantity() { return quantity; }
    public void setQuantity(Long quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getMedCode() { return medCode; }
    public void setMedCode(String medCode) { this.medCode = medCode; }
    public String getMedName() { return medName; }
    public void setMedName(String medName) { this.medName = medName; }
    public String getMedSpec() { return medSpec; }
    public void setMedSpec(String medSpec) { this.medSpec = medSpec; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public Long getRemainQty() { return remainQty; }
    public void setRemainQty(Long remainQty) { this.remainQty = remainQty; }
}
