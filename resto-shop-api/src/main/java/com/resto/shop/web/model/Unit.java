package com.resto.shop.web.model;

import java.util.List;

/**
 * Created by KONATA on 2016/9/11.
 */
public class Unit {

    private String id;

    private String name;


    private String shopId;

    private List<UnitFamily> families;


    final public String getShopId() {
        return shopId;
    }

    final public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    final public String getId() {
        return id;
    }

    final public void setId(String id) {
        this.id = id;
    }

    final public String getName() {
        return name;
    }

    final public void setName(String name) {
        this.name = name;
    }

    final public List<UnitFamily> getFamilies() {
        return families;
    }

    final public void setFamilies(List<UnitFamily> families) {
        this.families = families;
    }
}
