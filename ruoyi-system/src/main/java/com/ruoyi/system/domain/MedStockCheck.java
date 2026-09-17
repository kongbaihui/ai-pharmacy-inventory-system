package com.ruoyi.system.domain;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 库存盘点对象 med_stock_check
 * 
 * @author ruoyi
 * @date 2026-09-15
 */
public class MedStockCheck extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 盘点ID */
    private Long checkId;

    /** 盘点单号 */
    @Excel(name = "盘点单号")
    private String checkNo;

    /** 盘点名称 */
    @Excel(name = "盘点名称")
    private String checkName;

    /** 盘点类型（0全盘 1抽盘 2重点盘点） */
    @Excel(name = "盘点类型", readConverterExp = "0=全盘,1=抽盘,2=重点盘点")
    private String checkType;

    /** 盘点状态（0待盘点 1盘点中 2已盘点 3已审核） */
    @Excel(name = "盘点状态", readConverterExp = "0=待盘点,1=盘点中,2=已盘点,3=已审核")
    private String checkStatus;

    /** 盘点日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "盘点日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date checkDate;

    /** 盘点人 */
    @Excel(name = "盘点人")
    private String checkUser;

    /** 盘点时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "盘点时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date checkTime;

    /** 审核人 */
    @Excel(name = "审核人")
    private String auditBy;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "审核时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

    /** 药品分类（生成盘点明细时的筛选条件） */
    private Long categoryId;

    /** 药品（生成盘点明细时的筛选条件） */
    private Long medId;

    /** 库存盘点明细信息 */
    private List<MedStockCheckItem> medStockCheckItemList;

    public void setCheckId(Long checkId) 
    {
        this.checkId = checkId;
    }

    public Long getCheckId() 
    {
        return checkId;
    }

    public void setCheckNo(String checkNo) 
    {
        this.checkNo = checkNo;
    }

    public String getCheckNo() 
    {
        return checkNo;
    }

    public void setCheckName(String checkName) 
    {
        this.checkName = checkName;
    }

    public String getCheckName() 
    {
        return checkName;
    }

    public void setCheckType(String checkType) 
    {
        this.checkType = checkType;
    }

    public String getCheckType() 
    {
        return checkType;
    }

    public void setCheckStatus(String checkStatus) 
    {
        this.checkStatus = checkStatus;
    }

    public String getCheckStatus() 
    {
        return checkStatus;
    }

    public void setCheckDate(Date checkDate) 
    {
        this.checkDate = checkDate;
    }

    public Date getCheckDate() 
    {
        return checkDate;
    }

    public void setCheckUser(String checkUser) 
    {
        this.checkUser = checkUser;
    }

    public String getCheckUser() 
    {
        return checkUser;
    }

    public void setCheckTime(Date checkTime) 
    {
        this.checkTime = checkTime;
    }

    public Date getCheckTime() 
    {
        return checkTime;
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

    public void setCategoryId(Long categoryId) 
    {
        this.categoryId = categoryId;
    }

    public Long getCategoryId() 
    {
        return categoryId;
    }

    public void setMedId(Long medId) 
    {
        this.medId = medId;
    }

    public Long getMedId() 
    {
        return medId;
    }

    public List<MedStockCheckItem> getMedStockCheckItemList()
    {
        return medStockCheckItemList;
    }

    public void setMedStockCheckItemList(List<MedStockCheckItem> medStockCheckItemList)
    {
        this.medStockCheckItemList = medStockCheckItemList;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("checkId", getCheckId())
            .append("checkNo", getCheckNo())
            .append("checkName", getCheckName())
            .append("checkType", getCheckType())
            .append("checkStatus", getCheckStatus())
            .append("checkDate", getCheckDate())
            .append("checkUser", getCheckUser())
            .append("checkTime", getCheckTime())
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
