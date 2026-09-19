package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 过期药品清理对象 med_expired_clean
 * 
 * @author ruoyi
 * @date 2026-09-15
 */
public class MedExpiredClean extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 清理ID */
    private Long cleanId;

    /** 清理单号 */
    @Excel(name = "清理单号")
    private String cleanNo;

    /** 药品ID */
    private Long medId;

    /** 批次ID */
    private Long batchId;

    /** 生产批号 */
    @Excel(name = "生产批号")
    private String batchNo;

    /** 有效期至 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "有效期至", width = 30, dateFormat = "yyyy-MM-dd")
    private Date expireDate;

    /** 清理数量 */
    @Excel(name = "清理数量")
    private Long cleanQty;

    /** 清理方式（0退货 1销毁 2报损） */
    @Excel(name = "清理方式", readConverterExp = "0=退货,1=销毁,2=报损")
    private String cleanType;

    /** 清理原因 */
    @Excel(name = "清理原因")
    private String cleanReason;

    /** 清理状态（0待审核 1已确认 2已驳回） */
    @Excel(name = "清理状态", readConverterExp = "0=待审核,1=已确认,2=已驳回")
    private String cleanStatus;

    /** 申请人 */
    @Excel(name = "申请人")
    private String cleanUser;

    /** 申请时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "申请时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date cleanTime;

    /** 确认人 */
    @Excel(name = "确认人")
    private String auditBy;

    /** 确认时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "确认时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

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

    /** 批次剩余数量（关联查询） */
    private Long remainQty;

    /** 药品分类（查询条件） */
    private Long categoryId;

    public void setCleanId(Long cleanId) 
    {
        this.cleanId = cleanId;
    }

    public Long getCleanId() 
    {
        return cleanId;
    }

    public void setCleanNo(String cleanNo) 
    {
        this.cleanNo = cleanNo;
    }

    public String getCleanNo() 
    {
        return cleanNo;
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

    public void setExpireDate(Date expireDate) 
    {
        this.expireDate = expireDate;
    }

    public Date getExpireDate() 
    {
        return expireDate;
    }

    public void setCleanQty(Long cleanQty) 
    {
        this.cleanQty = cleanQty;
    }

    public Long getCleanQty() 
    {
        return cleanQty;
    }

    public void setCleanType(String cleanType) 
    {
        this.cleanType = cleanType;
    }

    public String getCleanType() 
    {
        return cleanType;
    }

    public void setCleanReason(String cleanReason) 
    {
        this.cleanReason = cleanReason;
    }

    public String getCleanReason() 
    {
        return cleanReason;
    }

    public void setCleanStatus(String cleanStatus) 
    {
        this.cleanStatus = cleanStatus;
    }

    public String getCleanStatus() 
    {
        return cleanStatus;
    }

    public void setCleanUser(String cleanUser) 
    {
        this.cleanUser = cleanUser;
    }

    public String getCleanUser() 
    {
        return cleanUser;
    }

    public void setCleanTime(Date cleanTime) 
    {
        this.cleanTime = cleanTime;
    }

    public Date getCleanTime() 
    {
        return cleanTime;
    }

    public void setAuditBy(String auditBy) 
    {
        this.auditBy = auditBy;
    }

    public String getAuditBy() 
    {
        return auditBy;
    }

    public void setAuditTime(Date auditTime) 
    {
        this.auditTime = auditTime;
    }

    public Date getAuditTime() 
    {
        return auditTime;
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

    public void setRemainQty(Long remainQty) 
    {
        this.remainQty = remainQty;
    }

    public Long getRemainQty() 
    {
        return remainQty;
    }

    public void setCategoryId(Long categoryId) 
    {
        this.categoryId = categoryId;
    }

    public Long getCategoryId() 
    {
        return categoryId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("cleanId", getCleanId())
            .append("cleanNo", getCleanNo())
            .append("medId", getMedId())
            .append("batchId", getBatchId())
            .append("batchNo", getBatchNo())
            .append("expireDate", getExpireDate())
            .append("cleanQty", getCleanQty())
            .append("cleanType", getCleanType())
            .append("cleanReason", getCleanReason())
            .append("cleanStatus", getCleanStatus())
            .append("cleanUser", getCleanUser())
            .append("cleanTime", getCleanTime())
            .append("auditBy", getAuditBy())
            .append("auditTime", getAuditTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
