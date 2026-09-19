package com.ruoyi.system.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 库存业务单对象 med_stock_order（1入库、2出库、3退库）
 */
public class MedStockOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long orderId;

    @Excel(name = "业务单号")
    private String orderNo;

    @Excel(name = "业务类型", readConverterExp = "1=入库,2=出库,3=退库")
    private String orderType;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "业务日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date orderDate;

    private Long supplierId;

    @Excel(name = "供应商")
    private String supplierName;

    @Excel(name = "领用/退回科室")
    private String department;

    @Excel(name = "状态", readConverterExp = "0=草稿,1=已确认")
    private String orderStatus;

    @Excel(name = "总数量")
    private Long totalQty;

    @Excel(name = "总金额")
    private BigDecimal totalAmount;

    @Excel(name = "经办人")
    private String operator;

    private String confirmBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date confirmTime;

    private List<MedStockOrderItem> itemList;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }
    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    public Long getTotalQty() { return totalQty; }
    public void setTotalQty(Long totalQty) { this.totalQty = totalQty; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public String getConfirmBy() { return confirmBy; }
    public void setConfirmBy(String confirmBy) { this.confirmBy = confirmBy; }
    public Date getConfirmTime() { return confirmTime; }
    public void setConfirmTime(Date confirmTime) { this.confirmTime = confirmTime; }
    public List<MedStockOrderItem> getItemList() { return itemList; }
    public void setItemList(List<MedStockOrderItem> itemList) { this.itemList = itemList; }
}
