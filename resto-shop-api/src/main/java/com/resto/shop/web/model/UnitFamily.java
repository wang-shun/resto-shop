package com.resto.shop.web.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by KONATA on 2016/9/11.
 */
public class UnitFamily {
    private String id;

    private String name;

    private Integer sort;

    private Integer type;



    private List<UnitDetail> detailList;


    final public Integer getType() {
        return type;
    }

    final public void setType(Integer type) {
        this.type = type;
    }

    final public List<UnitDetail> getDetailList() {
        return detailList;
    }

    final public void setDetailList(List<UnitDetail> detailList) {
        this.detailList = detailList;
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

    final public Integer getSort() {
        return sort;
    }

    final public void setSort(Integer sort) {
        this.sort = sort;
    }


}
