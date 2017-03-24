package com.resto.shop.web.model;

import com.resto.brand.core.util.ApplicationUtils;

import java.math.BigDecimal;

public class PlatformOrderDetail {
    private String id;

    private String platformOrderId;

    private String name;

    private BigDecimal price;

    private Integer quantity;

    private String showName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getPlatformOrderId() {
        return platformOrderId;
    }

    public void setPlatformOrderId(String platformOrderId) {
        this.platformOrderId = platformOrderId == null ? null : platformOrderId.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getShowName() {
        return showName;
    }

    public void setShowName(String showName) {
        this.showName = showName == null ? null : showName.trim();
    }

    public PlatformOrderDetail() {
    }

    public PlatformOrderDetail(HungerOrderDetail detail) {
        id = ApplicationUtils.randomUUID();
        platformOrderId = detail.getOrderId();
        name = detail.getName();
        price = detail.getPrice();
        quantity = detail.getQuantity();
        showName = detail.getName() + detail.getSpecs();
    }
}