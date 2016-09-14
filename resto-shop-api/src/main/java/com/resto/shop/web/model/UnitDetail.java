package com.resto.shop.web.model;

import java.math.BigDecimal;

/**
 * Created by KONATA on 2016/9/11.
 */
public class UnitDetail {
    private String id;

    private String name;

    private BigDecimal spread;

    private Integer sort;

    private Boolean click;

    final public Boolean getClick() {
        return click;
    }

    final public void setClick(Boolean click) {
        this.click = click;
    }

    final public Integer getSort() {
        return sort;
    }

    final public void setSort(Integer sort) {
        this.sort = sort;
    }

    final public BigDecimal getSpread() {
        return spread;
    }

    final public void setSpread(BigDecimal spread) {
        this.spread = spread;
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
