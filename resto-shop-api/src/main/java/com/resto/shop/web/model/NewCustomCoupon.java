package com.resto.shop.web.model;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

public class NewCustomCoupon {
    private Long id;

    private String name;

    private BigDecimal couponValue;

    private Integer couponValiday;
    
    
    private Integer couponNumber;

    private Date createTime;

    private Byte useWithAccount;

    private String couponName;
    
    private BigDecimal couponMinMoney;
    
    @DateTimeFormat(pattern=("HH:mm"))
    private Date beginTime;
    
    @DateTimeFormat(pattern=("HH:mm"))
    private Date endTime;

    private Byte isActivty;

    private String brandId;

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

    public Byte getUseWithAccount() {
        return useWithAccount;
    }

    public void setUseWithAccount(Byte useWithAccount) {
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

    public Byte getIsActivty() {
        return isActivty;
    }

    public void setIsActivty(Byte isActivty) {
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