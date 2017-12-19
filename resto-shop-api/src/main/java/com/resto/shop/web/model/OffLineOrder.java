package com.resto.shop.web.model;

import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class OffLineOrder implements Serializable {
    private String id;

    private String shopDetailId;

    private String brandId;

    private Integer resource;

    private BigDecimal enterTotal;

    private Integer enterCount;

    private Integer deliveryOrders;
    
    private BigDecimal orderBooks;

    private Integer numGuest;

    private Date createTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date createDate;

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
    
    public Integer getDeliveryOrders() {
		return deliveryOrders;
	}

	public void setDeliveryOrders(Integer deliveryOrders) {
		this.deliveryOrders = deliveryOrders;
	}

	public BigDecimal getOrderBooks() {
		return orderBooks;
	}

	public void setOrderBooks(BigDecimal orderBooks) {
		this.orderBooks = orderBooks;
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

    public OffLineOrder(String id, String shopDetailId, String brandId, Integer resource, BigDecimal enterTotal, Integer enterCount, Integer deliveryOrders, BigDecimal orderBooks, Integer numGuest, Date createTime, Date createDate, Integer state) {
        this.id = id;
        this.shopDetailId = shopDetailId;
        this.brandId = brandId;
        this.resource = resource;
        this.enterTotal = enterTotal;
        this.enterCount = enterCount;
        this.deliveryOrders = deliveryOrders;
        this.orderBooks = orderBooks;
        this.numGuest = numGuest;
        this.createTime = createTime;
        this.createDate = createDate;
        this.state = state;
    }

    public OffLineOrder() {
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
}