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
//    private List<VirtualProductsAndKitchen> virtualProductsAndKitchens;//关系表集合
    private List<Kitchen> kitchens;//厨房集合

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

//    public List<VirtualProductsAndKitchen> getVirtualProductsAndKitchens() {
//        return virtualProductsAndKitchens;
//    }
//
//    public void setVirtualProductsAndKitchens(List<VirtualProductsAndKitchen> virtualProductsAndKitchens) {
//        this.virtualProductsAndKitchens = virtualProductsAndKitchens;
//    }

    public List<Kitchen> getKitchens() {
        return kitchens;
    }

    public void setKitchens(List<Kitchen> kitchens) {
        this.kitchens = kitchens;
    }
}
