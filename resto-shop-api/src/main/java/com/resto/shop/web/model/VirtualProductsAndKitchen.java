package com.resto.shop.web.model;

/**
 * Created by yangwei on 2017/2/22.
 */
public class VirtualProductsAndKitchen {
    private int id;
    private int virtualId;
    private int kitchenId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVirtualId() {
        return virtualId;
    }

    public void setVirtualId(int virtualId) {
        this.virtualId = virtualId;
    }

    public int getKitchenId() {
        return kitchenId;
    }

    public void setKitchenId(int kitchenId) {
        this.kitchenId = kitchenId;
    }
}
