package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 库存盘点明细对象 med_stock_check_item
 * 
 * @author ruoyi
 * @date 2026-09-15
 */
public class MedStockCheckItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 盘点明细ID */
    private Long itemId;

    /** 盘点ID */
    private Long checkId;

    /** 药品ID */
    private Long medId;

    /** 批次ID */
    private Long batchId;

    /** 生产批号 */
    @Excel(name = "生产批号")
    private String batchNo;

    /** 账面数量 */
    @Excel(name = "账面数量")
    private Long bookQty;

    /** 实盘数量 */
    @Excel(name = "实盘数量")
    private Long realQty;

    /** 盈亏数量（实盘-账面） */
    @Excel(name = "盈亏数量")
    private Long diffQty;

    /** 盈亏类型（0正常 1盘盈 2盘亏） */
    @Excel(name = "盈亏类型", readConverterExp = "0=正常,1=盘盈,2=盘亏")
    private String diffType;

    /** 盈亏原因 */
    @Excel(name = "盈亏原因")
    private String diffReason;

    /** 明细创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 药品名称（关联查询） */
    @Excel(name = "药品名称")
    private String medName;

    /** 规格（关联查询） */
    @Excel(name = "规格")
    private String medSpec;

    /** 计量单位（关联查询） */
    @Excel(name = "计量单位")
    private String unit;

    public void setItemId(Long itemId) 
    {
        this.itemId = itemId;
    }

    public Long getItemId() 
    {
        return itemId;
    }

    public void setCheckId(Long checkId) 
    {
        this.checkId = checkId;
    }

    public Long getCheckId() 
    {
        return checkId;
    }

    public void setMedId(Long medId) 
    {
        this.medId = medId;
    }

    public Long getMedId() 
    {
        return medId;
    }

    public void setBatchId(Long batchId) 
    {
        this.batchId = batchId;
    }

    public Long getBatchId() 
    {
        return batchId;
    }

    public void setBatchNo(String batchNo) 
    {
        this.batchNo = batchNo;
    }

    public String getBatchNo() 
    {
        return batchNo;
    }

    public void setBookQty(Long bookQty) 
    {
        this.bookQty = bookQty;
    }

    public Long getBookQty() 
    {
        return bookQty;
    }

    public void setRealQty(Long realQty) 
    {
        this.realQty = realQty;
    }

    public Long getRealQty() 
    {
        return realQty;
    }

    public void setDiffQty(Long diffQty) 
    {
        this.diffQty = diffQty;
    }

    public Long getDiffQty() 
    {
        return diffQty;
    }

    public void setDiffType(String diffType) 
    {
        this.diffType = diffType;
    }

    public String getDiffType() 
    {
        return diffType;
    }

    public void setDiffReason(String diffReason) 
    {
        this.diffReason = diffReason;
    }

    public String getDiffReason() 
    {
        return diffReason;
    }

    public void setCreateTime(Date createTime) 
    {
        this.createTime = createTime;
    }

    public Date getCreateTime() 
    {
        return createTime;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("itemId", getItemId())
            .append("checkId", getCheckId())
            .append("medId", getMedId())
            .append("batchId", getBatchId())
            .append("batchNo", getBatchNo())
            .append("bookQty", getBookQty())
            .append("realQty", getRealQty())
            .append("diffQty", getDiffQty())
            .append("diffType", getDiffType())
            .append("diffReason", getDiffReason())
            .append("remark", getRemark())
            .toString();
    }
}
