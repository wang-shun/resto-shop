package com.resto.shop.web.model;

import org.springframework.format.annotation.DateTimeFormat;

public class SupportTime {
    private Integer id;

    private String name;
    
    @DateTimeFormat(pattern="HH:mm")
    private String beginTime;
    
    @DateTimeFormat(pattern="HH:mm")
    private String endTime;

    private Integer supportWeekBin;

    private String remark;

    private String shopDetailId;
    
    private String shopName;
    
    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime == null ? null : beginTime.trim();
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime == null ? null : endTime.trim();
    }

    public Integer getSupportWeekBin() {
        return supportWeekBin;
    }

    public void setSupportWeekBin(Integer supportWeekBin) {
        this.supportWeekBin = supportWeekBin;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public String getShopDetailId() {
        return shopDetailId;
    }

    public void setShopDetailId(String shopDetailId) {
        this.shopDetailId = shopDetailId == null ? null : shopDetailId.trim();
    }
}