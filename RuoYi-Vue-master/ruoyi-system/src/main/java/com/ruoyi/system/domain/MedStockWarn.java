package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 库存预警对象 med_stock_warn
 * 
 * @author ruoyi
 * @date 2026-09-15
 */
public class MedStockWarn extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 预警ID */
    private Long warnId;

    /** 药品ID */
    private Long medId;

    /** 批次ID */
    private Long batchId;

    /** 生产批号 */
    @Excel(name = "生产批号")
    private String batchNo;

    /** 预警类型（0库存不足 1库存积压 2近效期 3已过期） */
    @Excel(name = "预警类型", readConverterExp = "0=库存不足,1=库存积压,2=近效期,3=已过期")
    private String warnType;

    /** 预警级别（0提示 1警告 2严重） */
    @Excel(name = "预警级别", readConverterExp = "0=提示,1=警告,2=严重")
    private String warnLevel;

    /** 当前库存数量 */
    @Excel(name = "当前库存数量")
    private Long currentQty;

    /** 预警阈值 */
    @Excel(name = "预警阈值")
    private Long warnQty;

    /** 预警内容 */
    @Excel(name = "预警内容")
    private String warnContent;

    /** 预警时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "预警时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date warnTime;

    /** 处理状态（0未处理 1已处理 2已忽略） */
    @Excel(name = "处理状态", readConverterExp = "0=未处理,1=已处理,2=已忽略")
    private String handleStatus;

    /** 处理人 */
    @Excel(name = "处理人")
    private String handleUser;

    /** 处理时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date handleTime;

    /** 处理说明 */
    @Excel(name = "处理说明")
    private String handleRemark;

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

    /** 批量处理的预警ID集合 */
    private Long[] warnIds;

    public void setWarnId(Long warnId) 
    {
        this.warnId = warnId;
    }

    public Long getWarnId() 
    {
        return warnId;
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

    public void setWarnType(String warnType) 
    {
        this.warnType = warnType;
    }

    public String getWarnType() 
    {
        return warnType;
    }

    public void setWarnLevel(String warnLevel) 
    {
        this.warnLevel = warnLevel;
    }

    public String getWarnLevel() 
    {
        return warnLevel;
    }

    public void setCurrentQty(Long currentQty) 
    {
        this.currentQty = currentQty;
    }

    public Long getCurrentQty() 
    {
        return currentQty;
    }

    public void setWarnQty(Long warnQty) 
    {
        this.warnQty = warnQty;
    }

    public Long getWarnQty() 
    {
        return warnQty;
    }

    public void setWarnContent(String warnContent) 
    {
        this.warnContent = warnContent;
    }

    public String getWarnContent() 
    {
        return warnContent;
    }

    public void setWarnTime(Date warnTime) 
    {
        this.warnTime = warnTime;
    }

    public Date getWarnTime() 
    {
        return warnTime;
    }

    public void setHandleStatus(String handleStatus) 
    {
        this.handleStatus = handleStatus;
    }

    public String getHandleStatus() 
    {
        return handleStatus;
    }

    public void setHandleUser(String handleUser) 
    {
        this.handleUser = handleUser;
    }

    public String getHandleUser() 
    {
        return handleUser;
    }

    public void setHandleTime(Date handleTime) 
    {
        this.handleTime = handleTime;
    }

    public Date getHandleTime() 
    {
        return handleTime;
    }

    public void setHandleRemark(String handleRemark) 
    {
        this.handleRemark = handleRemark;
    }

    public String getHandleRemark() 
    {
        return handleRemark;
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

    public Long[] getWarnIds() 
    {
        return warnIds;
    }

    public void setWarnIds(Long[] warnIds) 
    {
        this.warnIds = warnIds;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("warnId", getWarnId())
            .append("medId", getMedId())
            .append("batchId", getBatchId())
            .append("batchNo", getBatchNo())
            .append("warnType", getWarnType())
            .append("warnLevel", getWarnLevel())
            .append("currentQty", getCurrentQty())
            .append("warnQty", getWarnQty())
            .append("warnContent", getWarnContent())
            .append("warnTime", getWarnTime())
            .append("handleStatus", getHandleStatus())
            .append("handleUser", getHandleUser())
            .append("handleTime", getHandleTime())
            .append("handleRemark", getHandleRemark())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .toString();
    }
}
