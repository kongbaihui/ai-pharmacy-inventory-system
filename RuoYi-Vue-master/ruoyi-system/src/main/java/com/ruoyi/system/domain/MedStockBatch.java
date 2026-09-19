package com.ruoyi.system.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 药品库存批次对象 med_stock_batch
 * 
 * @author ruoyi
 * @date 2026-09-15
 */
public class MedStockBatch extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 批次ID */
    private Long batchId;

    /** 药品ID */
    private Long medId;

    /** 供应商ID */
    private Long supplierId;

    /** 生产批号 */
    @Excel(name = "生产批号")
    private String batchNo;

    /** 生产日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "生产日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date produceDate;

    /** 有效期至 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "有效期至", width = 30, dateFormat = "yyyy-MM-dd")
    private Date expireDate;

    /** 批次入库数量 */
    @Excel(name = "批次入库数量")
    private Long batchQty;

    /** 批次剩余数量 */
    @Excel(name = "批次剩余数量")
    private Long remainQty;

    /** 批次进价 */
    @Excel(name = "批次进价")
    private BigDecimal purchasePrice;

    /** 入库时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "入库时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date inTime;

    /** 批次状态（0正常 1临期 2过期 3已清理） */
    @Excel(name = "批次状态", readConverterExp = "0=正常,1=临期,2=过期,3=已清理")
    private String batchStatus;

    /** 药品编码（关联查询） */
    @Excel(name = "药品编码")
    private String medCode;

    /** 药品名称（关联查询） */
    @Excel(name = "药品名称")
    private String medName;

    /** 规格（关联查询） */
    @Excel(name = "规格")
    private String medSpec;

    /** 计量单位（关联查询） */
    @Excel(name = "计量单位")
    private String unit;

    /** 药品分类（查询条件） */
    private Long categoryId;

    /** 供应商名称（关联查询） */
    @Excel(name = "供应商")
    private String supplierName;

    /** 剩余有效天数（关联查询） */
    @Excel(name = "剩余有效天数")
    private Long remainDays;

    /** 近效期预警天数（关联查询） */
    private Long warnDays;

    /** 是否已过期（查询条件，1已过期 2未过期） */
    private String expireFlag;

    /** 批次剩余数量合计（关联查询，用于库存总量重算） */
    private Long batchQtySum;

    /** 库存主表总量（关联查询，用于库存总量重算） */
    private Long stockQty;

    /** 数量差异（关联查询，批次合计减库存总量） */
    private Long diffQty;

    public void setBatchId(Long batchId) 
    {
        this.batchId = batchId;
    }

    public Long getBatchId() 
    {
        return batchId;
    }

    public void setMedId(Long medId) 
    {
        this.medId = medId;
    }

    public Long getMedId() 
    {
        return medId;
    }

    public void setSupplierId(Long supplierId) 
    {
        this.supplierId = supplierId;
    }

    public Long getSupplierId() 
    {
        return supplierId;
    }

    public void setBatchNo(String batchNo) 
    {
        this.batchNo = batchNo;
    }

    public String getBatchNo() 
    {
        return batchNo;
    }

    public void setProduceDate(Date produceDate) 
    {
        this.produceDate = produceDate;
    }

    public Date getProduceDate() 
    {
        return produceDate;
    }

    public void setExpireDate(Date expireDate) 
    {
        this.expireDate = expireDate;
    }

    public Date getExpireDate() 
    {
        return expireDate;
    }

    public void setBatchQty(Long batchQty) 
    {
        this.batchQty = batchQty;
    }

    public Long getBatchQty() 
    {
        return batchQty;
    }

    public void setRemainQty(Long remainQty) 
    {
        this.remainQty = remainQty;
    }

    public Long getRemainQty() 
    {
        return remainQty;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) 
    {
        this.purchasePrice = purchasePrice;
    }

    public BigDecimal getPurchasePrice() 
    {
        return purchasePrice;
    }

    public void setInTime(Date inTime) 
    {
        this.inTime = inTime;
    }

    public Date getInTime() 
    {
        return inTime;
    }

    public void setBatchStatus(String batchStatus) 
    {
        this.batchStatus = batchStatus;
    }

    public String getBatchStatus() 
    {
        return batchStatus;
    }

    public void setMedCode(String medCode) 
    {
        this.medCode = medCode;
    }

    public String getMedCode() 
    {
        return medCode;
    }

    public void setMedName(String medName) 
    {
        this.medName = medName;
    }

    public String getMedName() 
    {
        return medName;
    }

    public void setMedSpec(String medSpec) 
    {
        this.medSpec = medSpec;
    }

    public String getMedSpec() 
    {
        return medSpec;
    }

    public void setUnit(String unit) 
    {
        this.unit = unit;
    }

    public String getUnit() 
    {
        return unit;
    }

    public void setCategoryId(Long categoryId) 
    {
        this.categoryId = categoryId;
    }

    public Long getCategoryId() 
    {
        return categoryId;
    }

    public void setSupplierName(String supplierName) 
    {
        this.supplierName = supplierName;
    }

    public String getSupplierName() 
    {
        return supplierName;
    }

    public void setRemainDays(Long remainDays) 
    {
        this.remainDays = remainDays;
    }

    public Long getRemainDays() 
    {
        return remainDays;
    }

    public void setWarnDays(Long warnDays) 
    {
        this.warnDays = warnDays;
    }

    public Long getWarnDays() 
    {
        return warnDays;
    }

    public void setExpireFlag(String expireFlag) 
    {
        this.expireFlag = expireFlag;
    }

    public String getExpireFlag() 
    {
        return expireFlag;
    }

    public void setBatchQtySum(Long batchQtySum) 
    {
        this.batchQtySum = batchQtySum;
    }

    public Long getBatchQtySum() 
    {
        return batchQtySum;
    }

    public void setStockQty(Long stockQty) 
    {
        this.stockQty = stockQty;
    }

    public Long getStockQty() 
    {
        return stockQty;
    }

    public void setDiffQty(Long diffQty) 
    {
        this.diffQty = diffQty;
    }

    public Long getDiffQty() 
    {
        return diffQty;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("batchId", getBatchId())
            .append("medId", getMedId())
            .append("supplierId", getSupplierId())
            .append("batchNo", getBatchNo())
            .append("produceDate", getProduceDate())
            .append("expireDate", getExpireDate())
            .append("batchQty", getBatchQty())
            .append("remainQty", getRemainQty())
            .append("purchasePrice", getPurchasePrice())
            .append("inTime", getInTime())
            .append("batchStatus", getBatchStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
