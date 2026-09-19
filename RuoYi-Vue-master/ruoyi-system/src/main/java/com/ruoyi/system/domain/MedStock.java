package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 药品库存对象 med_stock
 * 
 * @author ruoyi
 * @date 2026-09-15
 */
public class MedStock extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 库存ID */
    private Long stockId;

    /** 药品ID */
    private Long medId;

    /** 库存总数量 */
    private Long totalQty;

    /** 锁定数量 */
    private Long lockQty;

    /** 最近入库时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastInTime;

    /** 最近出库时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastOutTime;

    public void setStockId(Long stockId) 
    {
        this.stockId = stockId;
    }

    public Long getStockId() 
    {
        return stockId;
    }

    public void setMedId(Long medId) 
    {
        this.medId = medId;
    }

    public Long getMedId() 
    {
        return medId;
    }

    public void setTotalQty(Long totalQty) 
    {
        this.totalQty = totalQty;
    }

    public Long getTotalQty() 
    {
        return totalQty;
    }

    public void setLockQty(Long lockQty) 
    {
        this.lockQty = lockQty;
    }

    public Long getLockQty() 
    {
        return lockQty;
    }

    public void setLastInTime(Date lastInTime) 
    {
        this.lastInTime = lastInTime;
    }

    public Date getLastInTime() 
    {
        return lastInTime;
    }

    public void setLastOutTime(Date lastOutTime) 
    {
        this.lastOutTime = lastOutTime;
    }

    public Date getLastOutTime() 
    {
        return lastOutTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("stockId", getStockId())
            .append("medId", getMedId())
            .append("totalQty", getTotalQty())
            .append("lockQty", getLockQty())
            .append("lastInTime", getLastInTime())
            .append("lastOutTime", getLastOutTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
