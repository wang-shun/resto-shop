package com.resto.shop.web.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by KONATA on 2016/9/11.
 */
public class UnitDetail implements Serializable {
    private String id;

    private String name;



    private Integer sort;

    private Integer isUsed;

    private BigDecimal price ;

    final public BigDecimal getPrice() {
        return price;
    }

    final public void setPrice(BigDecimal price) {
        this.price = price;
    }

    final public Integer getIsUsed() {
        return isUsed;
    }

    final public void setIsUsed(Integer isUsed) {
        this.isUsed = isUsed;
    }

    final public Integer getSort() {
        return sort;
    }

    final public void setSort(Integer sort) {
        this.sort = sort;
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
}
