package com.resto.shop.web.model;

import java.util.Date;
import java.util.List;

/**
 * Created by yangwei on 2017/2/22.
 */
public class VirtualProducts {
    private int id;
    private String name;//虚拟餐品名称
    private Integer isUsed;//虚拟餐品是否启用
    private Date createTime;//虚拟餐品创建时间
    private String shopDetailId;//店铺ID
    private List<Kitchen> kitchen;//厨房集合

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getIsUsed() {
        return isUsed;
    }

    public void setIsUsed(Integer isUsed) {
        this.isUsed = isUsed;
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

    public List<Kitchen> getKitchen() {
        return kitchen;
    }

    public void setKitchen(List<Kitchen> kitchen) {
        this.kitchen = kitchen;
    }
}
