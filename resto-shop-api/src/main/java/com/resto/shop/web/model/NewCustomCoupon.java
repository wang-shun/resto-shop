package com.resto.shop.web.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;

public class NewCustomCoupon {
    private Long id;

    private String name;

    //@NumberFormat(style=Style.NUMBER,pattern="#.##")
    @NotNull(message="优惠券的价值不能为空")
    @Min(message="优惠券的价值最小值0" ,value=0)
    private BigDecimal couponValue;//优惠券的价值
    
    @NotNull(message="优惠券的有效期不能为空")
    private Integer couponValiday;
    
    @NotNull(message="优惠券的个数不能为空")
    private Integer couponNumber;

    private Date createTime;

    private Boolean useWithAccount;

    private String couponName;
    
    @NotNull(message="优惠券的最低消费额度不能为空")
    @Min(message="最低消费额度为0",value=0)
    private BigDecimal couponMinMoney;
    
    @DateTimeFormat(pattern=("HH:mm"))
    private Date beginTime;
    
    @DateTimeFormat(pattern=("HH:mm"))
    private Date endTime;
    
    @NotNull(message="是否启用不能为空")
    private Boolean isActivty;

    private String brandId;
    
    @NotNull(message="配送模式不能为空")
    private Integer distributionModeId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public BigDecimal getCouponValue() {
        return couponValue;
    }

    public void setCouponValue(BigDecimal couponValue) {
        this.couponValue = couponValue;
    }

    public Integer getCouponValiday() {
        return couponValiday;
    }

    public void setCouponValiday(Integer couponValiday) {
        this.couponValiday = couponValiday;
    }

    public Integer getCouponNumber() {
        return couponNumber;
    }

    public void setCouponNumber(Integer couponNumber) {
        this.couponNumber = couponNumber;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Boolean getUseWithAccount() {
        return useWithAccount;
    }

    public void setUseWithAccount(Boolean useWithAccount) {
        this.useWithAccount = useWithAccount;
    }

    public String getCouponName() {
        return couponName;
    }

    public void setCouponName(String couponName) {
        this.couponName = couponName == null ? null : couponName.trim();
    }

    public BigDecimal getCouponMinMoney() {
        return couponMinMoney;
    }

    public void setCouponMinMoney(BigDecimal couponMinMoney) {
        this.couponMinMoney = couponMinMoney;
    }

    public Date getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Boolean getIsActivty() {
        return isActivty;
    }

    public void setIsActivty(Boolean isActivty) {
        this.isActivty = isActivty;
    }

    public String getBrandId() {
        return brandId;
    }

    public void setBrandId(String brandId) {
        this.brandId = brandId == null ? null : brandId.trim();
    }

    public Integer getDistributionModeId() {
        return distributionModeId;
    }

    public void setDistributionModeId(Integer distributionModeId) {
        this.distributionModeId = distributionModeId;
    }
}