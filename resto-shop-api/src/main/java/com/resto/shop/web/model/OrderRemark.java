package com.resto.shop.web.model;

import java.util.Date;

public class OrderRemark {

    private String id;
    private String remarkName;
    private Integer sort;
    private Integer state;
    private Date createTime;
    private String shopDetailId;
    private String brandId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRemarkName() {
        return remarkName;
    }

    public void setRemarkName(String remarkName) {
        this.remarkName = remarkName;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getShopDetailId() {
        return shopDetailId;
    }

    public void setShopDetailId(String shopDetailId) {
        this.shopDetailId = shopDetailId;
    }

    public String getBrandId() {
        return brandId;
    }

    public void setBrandId(String brandId) {
        this.brandId = brandId;
    }

    public OrderRemark() {
    }

    public OrderRemark(String id, String remarkName, Integer sort, Integer state, Date createTime, String shopDetailId, String brandId) {
        this.id = id;
        this.remarkName = remarkName;
        this.sort = sort;
        this.state = state;
        this.createTime = createTime;
        this.shopDetailId = shopDetailId;
        this.brandId = brandId;
    }
}
