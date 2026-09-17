package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 库存流水对象 med_stock_flow
 * 
 * @author ruoyi
 * @date 2026-09-15
 */
public class MedStockFlow extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 流水ID */
    private Long flowId;

    /** 药品ID */
    private Long medId;

    /** 批次ID */
    private Long batchId;

    /** 业务类型（1入库 2出库 3退库 4盘点调整 5过期清理） */
    @Excel(name = "业务类型", readConverterExp = "1=入库,2=出库,3=退库,4=盘点调整,5=过期清理")
    private String flowType;

    /** 变动数量（正数为增加，负数为减少） */
    @Excel(name = "变动数量")
    private Long changeQty;

    /** 变动前库存 */
    @Excel(name = "变动前库存")
    private Long beforeQty;

    /** 变动后库存 */
    @Excel(name = "变动后库存")
    private Long afterQty;

    /** 业务单号 */
    @Excel(name = "业务单号")
    private String bizNo;

    /** 业务主键 */
    private Long bizId;

    /** 操作人 */
    @Excel(name = "操作人")
    private String operator;

    /** 流水时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "流水时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date flowTime;

    /** 药品名称（关联查询） */
    @Excel(name = "药品名称")
    private String medName;

    public void setFlowId(Long flowId) 
    {
        this.flowId = flowId;
    }

    public Long getFlowId() 
    {
        return flowId;
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

    public void setFlowType(String flowType) 
    {
        this.flowType = flowType;
    }

    public String getFlowType() 
    {
        return flowType;
    }

    public void setChangeQty(Long changeQty) 
    {
        this.changeQty = changeQty;
    }

    public Long getChangeQty() 
    {
        return changeQty;
    }

    public void setBeforeQty(Long beforeQty) 
    {
        this.beforeQty = beforeQty;
    }

    public Long getBeforeQty() 
    {
        return beforeQty;
    }

    public void setAfterQty(Long afterQty) 
    {
        this.afterQty = afterQty;
    }

    public Long getAfterQty() 
    {
        return afterQty;
    }

    public void setBizNo(String bizNo) 
    {
        this.bizNo = bizNo;
    }

    public String getBizNo() 
    {
        return bizNo;
    }

    public void setBizId(Long bizId) 
    {
        this.bizId = bizId;
    }

    public Long getBizId() 
    {
        return bizId;
    }

    public void setOperator(String operator) 
    {
        this.operator = operator;
    }

    public String getOperator() 
    {
        return operator;
    }

    public void setFlowTime(Date flowTime) 
    {
        this.flowTime = flowTime;
    }

    public Date getFlowTime() 
    {
        return flowTime;
    }

    public void setMedName(String medName) 
    {
        this.medName = medName;
    }

    public String getMedName() 
    {
        return medName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("flowId", getFlowId())
            .append("medId", getMedId())
            .append("batchId", getBatchId())
            .append("flowType", getFlowType())
            .append("changeQty", getChangeQty())
            .append("beforeQty", getBeforeQty())
            .append("afterQty", getAfterQty())
            .append("bizNo", getBizNo())
            .append("bizId", getBizId())
            .append("operator", getOperator())
            .append("flowTime", getFlowTime())
            .append("remark", getRemark())
            .toString();
    }
}
