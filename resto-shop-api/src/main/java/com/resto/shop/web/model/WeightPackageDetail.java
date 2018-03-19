package com.resto.shop.web.model;

import java.io.Serializable;
import java.util.Date;

public class WeightPackageDetail implements Serializable {

    private Long id;

    private String name;

    private Integer sort;

    private Date createTime;

    private Integer isUsed;

    private Long weightPackageId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getIsUsed() {
        return isUsed;
    }

    public void setIsUsed(Integer isUsed) {
        this.isUsed = isUsed;
    }

    public Long getWeightPackageId() {
        return weightPackageId;
    }

    public void setWeightPackageId(Long weightPackageId) {
        this.weightPackageId = weightPackageId;
    }
}
