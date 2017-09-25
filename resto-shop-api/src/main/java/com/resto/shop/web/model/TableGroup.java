package com.resto.shop.web.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by carl on 2017/9/25.
 */
public class TableGroup implements Serializable {

    private Long id;

    private String tableNumber;

    private String groupId;

    private Date createTime;

    private Integer state;     //0 正常 1已付款 2已释放

    private String createCustomnerId;     //创建者id

    private String orderId;     //该组的主订单

    private String shopDetailId;

    private String brandId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getCreateCustomnerId() {
        return createCustomnerId;
    }

    public void setCreateCustomnerId(String createCustomnerId) {
        this.createCustomnerId = createCustomnerId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
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
}
