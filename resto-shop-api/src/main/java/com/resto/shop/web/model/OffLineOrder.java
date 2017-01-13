package com.resto.shop.web.model;

import java.math.BigDecimal;
import java.util.Date;

public class OffLineOrder {
    private String id;

    private String shopDetailId;

    private String brandId;

    private Integer resource;

    private BigDecimal enterTotal;

    private Integer enterCount;


    private Integer numGuest;

    private Date createTime;

    private Integer state;


    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getShopDetailId() {
        return shopDetailId;
    }

    public void setShopDetailId(String shopDetailId) {
        this.shopDetailId = shopDetailId == null ? null : shopDetailId.trim();
    }

    public String getBrandId() {
        return brandId;
    }

    public void setBrandId(String brandId) {
        this.brandId = brandId == null ? null : brandId.trim();
    }

    public Integer getResource() {
        return resource;
    }

    public void setResource(Integer resource) {
        this.resource = resource;
    }


    public BigDecimal getEnterTotal() {
        return enterTotal;
    }

    public void setEnterTotal(BigDecimal enterTotal) {
        this.enterTotal = enterTotal;
    }

    public Integer getEnterCount() {
        return enterCount;
    }

    public void setEnterCount(Integer enterCount) {
        this.enterCount = enterCount;
    }

    public Integer getNumGuest() {
        return numGuest;
    }

    public void setNumGuest(Integer numGuest) {
        this.numGuest = numGuest;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}