package com.resto.shop.web.model;

/**
 * Created by KONATA on 2016/9/11.
 */
public class UnitDetail {
    private String id;

    private String name;



    private Integer sort;




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
