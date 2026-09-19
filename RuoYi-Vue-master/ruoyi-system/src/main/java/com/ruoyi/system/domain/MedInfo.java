package com.ruoyi.system.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 药品信息对象 med_info
 * 
 * @author ruoyi
 * @date 2026-09-15
 */
public class MedInfo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 药品ID */
    private Long medId;

    /** 药品编码 */
    @Excel(name = "药品编码")
    private String medCode;

    /** 药品通用名 */
    @Excel(name = "药品通用名")
    private String medName;

    /** 商品名 */
    @Excel(name = "商品名")
    private String tradeName;

    /** 药品分类ID */
    private Long categoryId;

    /** 默认供应商ID */
    private Long supplierId;

    /** 规格 */
    @Excel(name = "规格")
    private String medSpec;

    /** 剂型 */
    @Excel(name = "剂型")
    private String dosageForm;

    /** 计量单位 */
    @Excel(name = "计量单位")
    private String unit;

    /** 生产厂家 */
    @Excel(name = "生产厂家")
    private String manufacturer;

    /** 批准文号 */
    @Excel(name = "批准文号")
    private String approvalNo;

    /** 存储条件 */
    @Excel(name = "存储条件")
    private String storageCond;

    /** 参考进价 */
    @Excel(name = "参考进价")
    private BigDecimal purchasePrice;

    /** 零售价 */
    @Excel(name = "零售价")
    private BigDecimal salePrice;

    /** 库存下限 */
    @Excel(name = "库存下限")
    private Long stockMin;

    /** 库存上限 */
    @Excel(name = "库存上限")
    private Long stockMax;

    /** 近效期预警天数 */
    @Excel(name = "近效期预警天数")
    private Long warnDays;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 分类名称（关联查询） */
    @Excel(name = "药品分类")
    private String categoryName;

    /** 供应商名称（关联查询） */
    @Excel(name = "默认供应商")
    private String supplierName;

    /** 当前库存数量（关联查询） */
    @Excel(name = "当前库存")
    private Long stockQty;

    public void setMedId(Long medId) 
    {
        this.medId = medId;
    }

    public Long getMedId() 
    {
        return medId;
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

    public void setTradeName(String tradeName) 
    {
        this.tradeName = tradeName;
    }

    public String getTradeName() 
    {
        return tradeName;
    }

    public void setCategoryId(Long categoryId) 
    {
        this.categoryId = categoryId;
    }

    public Long getCategoryId() 
    {
        return categoryId;
    }

    public void setSupplierId(Long supplierId) 
    {
        this.supplierId = supplierId;
    }

    public Long getSupplierId() 
    {
        return supplierId;
    }

    public void setMedSpec(String medSpec) 
    {
        this.medSpec = medSpec;
    }

    public String getMedSpec() 
    {
        return medSpec;
    }

    public void setDosageForm(String dosageForm) 
    {
        this.dosageForm = dosageForm;
    }

    public String getDosageForm() 
    {
        return dosageForm;
    }

    public void setUnit(String unit) 
    {
        this.unit = unit;
    }

    public String getUnit() 
    {
        return unit;
    }

    public void setManufacturer(String manufacturer) 
    {
        this.manufacturer = manufacturer;
    }

    public String getManufacturer() 
    {
        return manufacturer;
    }

    public void setApprovalNo(String approvalNo) 
    {
        this.approvalNo = approvalNo;
    }

    public String getApprovalNo() 
    {
        return approvalNo;
    }

    public void setStorageCond(String storageCond) 
    {
        this.storageCond = storageCond;
    }

    public String getStorageCond() 
    {
        return storageCond;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) 
    {
        this.purchasePrice = purchasePrice;
    }

    public BigDecimal getPurchasePrice() 
    {
        return purchasePrice;
    }

    public void setSalePrice(BigDecimal salePrice) 
    {
        this.salePrice = salePrice;
    }

    public BigDecimal getSalePrice() 
    {
        return salePrice;
    }

    public void setStockMin(Long stockMin) 
    {
        this.stockMin = stockMin;
    }

    public Long getStockMin() 
    {
        return stockMin;
    }

    public void setStockMax(Long stockMax) 
    {
        this.stockMax = stockMax;
    }

    public Long getStockMax() 
    {
        return stockMax;
    }

    public void setWarnDays(Long warnDays) 
    {
        this.warnDays = warnDays;
    }

    public Long getWarnDays() 
    {
        return warnDays;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setCategoryName(String categoryName) 
    {
        this.categoryName = categoryName;
    }

    public String getCategoryName() 
    {
        return categoryName;
    }

    public void setSupplierName(String supplierName) 
    {
        this.supplierName = supplierName;
    }

    public String getSupplierName() 
    {
        return supplierName;
    }

    public void setStockQty(Long stockQty) 
    {
        this.stockQty = stockQty;
    }

    public Long getStockQty() 
    {
        return stockQty;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("medId", getMedId())
            .append("medCode", getMedCode())
            .append("medName", getMedName())
            .append("tradeName", getTradeName())
            .append("categoryId", getCategoryId())
            .append("supplierId", getSupplierId())
            .append("medSpec", getMedSpec())
            .append("dosageForm", getDosageForm())
            .append("unit", getUnit())
            .append("manufacturer", getManufacturer())
            .append("approvalNo", getApprovalNo())
            .append("storageCond", getStorageCond())
            .append("purchasePrice", getPurchasePrice())
            .append("salePrice", getSalePrice())
            .append("stockMin", getStockMin())
            .append("stockMax", getStockMax())
            .append("warnDays", getWarnDays())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
