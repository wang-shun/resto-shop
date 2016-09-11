package com.resto.shop.web.model;

import java.math.BigDecimal;

/**
 * Created by KONATA on 2016/9/11.
 */
public class UnitDetail {
    private Integer id;

    private String name;

    private BigDecimal spread;

    final public BigDecimal getSpread() {
        return spread;
    }

    final public void setSpread(BigDecimal spread) {
        this.spread = spread;
    }

    final public Integer getId() {
        return id;
    }

    final public void setId(Integer id) {
        this.id = id;
    }

    final public String getName() {
        return name;
    }

    final public void setName(String name) {
        this.name = name;
    }
}
