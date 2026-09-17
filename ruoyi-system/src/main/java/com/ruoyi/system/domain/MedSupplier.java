package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 供应商信息对象 med_supplier
 * 
 * @author ruoyi
 * @date 2026-09-15
 */
public class MedSupplier extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 供应商ID */
    private Long supplierId;

    /** 供应商编码 */
    @Excel(name = "供应商编码")
    private String supplierCode;

    /** 供应商名称 */
    @Excel(name = "供应商名称")
    private String supplierName;

    /** 联系人 */
    @Excel(name = "联系人")
    private String contactPerson;

    /** 联系电话 */
    @Excel(name = "联系电话")
    private String contactPhone;

    /** 供应商地址 */
    @Excel(name = "供应商地址")
    private String address;

    /** 经营许可证号 */
    @Excel(name = "经营许可证号")
    private String licenseNo;

    /** 许可证有效期至 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "许可证有效期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date licenseDate;

    /** 资质状态（0过期 1有效 2临近到期） */
    @Excel(name = "资质状态", readConverterExp = "0=过期,1=有效,2=临近到期")
    private String gspStatus;

    /** 信用等级（A优 B良 C一般 D差） */
    @Excel(name = "信用等级")
    private String creditLevel;

    /** 合作状态（0合作中 1暂停合作 2已终止） */
    @Excel(name = "合作状态", readConverterExp = "0=合作中,1=暂停合作,2=已终止")
    private String coopStatus;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public void setSupplierId(Long supplierId) 
    {
        this.supplierId = supplierId;
    }

    public Long getSupplierId() 
    {
        return supplierId;
    }

    public void setSupplierCode(String supplierCode) 
    {
        this.supplierCode = supplierCode;
    }

    public String getSupplierCode() 
    {
        return supplierCode;
    }

    public void setSupplierName(String supplierName) 
    {
        this.supplierName = supplierName;
    }

    public String getSupplierName() 
    {
        return supplierName;
    }

    public void setContactPerson(String contactPerson) 
    {
        this.contactPerson = contactPerson;
    }

    public String getContactPerson() 
    {
        return contactPerson;
    }

    public void setContactPhone(String contactPhone) 
    {
        this.contactPhone = contactPhone;
    }

    public String getContactPhone() 
    {
        return contactPhone;
    }

    public void setAddress(String address) 
    {
        this.address = address;
    }

    public String getAddress() 
    {
        return address;
    }

    public void setLicenseNo(String licenseNo) 
    {
        this.licenseNo = licenseNo;
    }

    public String getLicenseNo() 
    {
        return licenseNo;
    }

    public void setLicenseDate(Date licenseDate) 
    {
        this.licenseDate = licenseDate;
    }

    public Date getLicenseDate() 
    {
        return licenseDate;
    }

    public void setGspStatus(String gspStatus) 
    {
        this.gspStatus = gspStatus;
    }

    public String getGspStatus() 
    {
        return gspStatus;
    }

    public void setCreditLevel(String creditLevel) 
    {
        this.creditLevel = creditLevel;
    }

    public String getCreditLevel() 
    {
        return creditLevel;
    }

    public void setCoopStatus(String coopStatus) 
    {
        this.coopStatus = coopStatus;
    }

    public String getCoopStatus() 
    {
        return coopStatus;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("supplierId", getSupplierId())
            .append("supplierCode", getSupplierCode())
            .append("supplierName", getSupplierName())
            .append("contactPerson", getContactPerson())
            .append("contactPhone", getContactPhone())
            .append("address", getAddress())
            .append("licenseNo", getLicenseNo())
            .append("licenseDate", getLicenseDate())
            .append("gspStatus", getGspStatus())
            .append("creditLevel", getCreditLevel())
            .append("coopStatus", getCoopStatus())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
