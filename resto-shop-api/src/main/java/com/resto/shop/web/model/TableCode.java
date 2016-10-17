package com.resto.shop.web.model;

import java.util.Date;

public class TableCode {
    private String id;

    //编号名称
    private String name;

    //编号
    private String codeNumber;

    //创建时间
    private Date createTime;

    //最后修改时间
    private Date endTime;

    //最大人数
    private Integer maxNumber;

    //最小人数
    private Integer minNumber;

    //是否启用
    private Byte isUsed;

    private  String shopDetailId;

    private  String brandId;

    //等待位数
    private Integer waitNumber;

    final public Integer getWaitNumber() {
        return waitNumber;
    }

    final public void setWaitNumber(Integer waitNumber) {
        this.waitNumber = waitNumber;
    }

    public String getShopDetailId() {
        return shopDetailId;
    }

    public void setShopDetailId(String shopDetailId) {
        this.shopDetailId = shopDetailId;
    }

    public String getBrandId() {
        return brandId;
    }

    public void setBrandId(String brandId) {
        this.brandId = brandId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getCodeNumber() {
        return codeNumber;
    }

    public void setCodeNumber(String codeNumber) {
        this.codeNumber = codeNumber == null ? null : codeNumber.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Integer getMaxNumber() {
        return maxNumber;
    }

    public void setMaxNumber(Integer maxNumber) {
        this.maxNumber = maxNumber;
    }

    public Integer getMinNumber() {
        return minNumber;
    }

    public void setMinNumber(Integer minNumber) {
        this.minNumber = minNumber;
    }

    public Byte getIsUsed() {
        return isUsed;
    }

    public void setIsUsed(Byte isUsed) {
        this.isUsed = isUsed;
    }
}