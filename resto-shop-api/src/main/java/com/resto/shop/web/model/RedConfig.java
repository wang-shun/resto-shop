package com.resto.shop.web.model;

import java.math.BigDecimal;

public class RedConfig {
    private Long id;

    private Integer delay;

    private Integer minRatio;

    private Integer maxRatio;

    private BigDecimal maxSingleRed;

    private String title;

    private String remark;

    private BigDecimal minSignleRed;

    private Byte isAddRatio;

    private BigDecimal minTranslateMoney;

    private Integer isActivity;

    private String shopDetailId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getDelay() {
        return delay;
    }

    public void setDelay(Integer delay) {
        this.delay = delay;
    }

    public Integer getMinRatio() {
        return minRatio;
    }

    public void setMinRatio(Integer minRatio) {
        this.minRatio = minRatio;
    }

    public Integer getMaxRatio() {
        return maxRatio;
    }

    public void setMaxRatio(Integer maxRatio) {
        this.maxRatio = maxRatio;
    }

    public BigDecimal getMaxSingleRed() {
        return maxSingleRed;
    }

    public void setMaxSingleRed(BigDecimal maxSingleRed) {
        this.maxSingleRed = maxSingleRed;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? null : title.trim();
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public BigDecimal getMinSignleRed() {
        return minSignleRed;
    }

    public void setMinSignleRed(BigDecimal minSignleRed) {
        this.minSignleRed = minSignleRed;
    }

    public Byte getIsAddRatio() {
        return isAddRatio;
    }

    public void setIsAddRatio(Byte isAddRatio) {
        this.isAddRatio = isAddRatio;
    }

    public BigDecimal getMinTranslateMoney() {
        return minTranslateMoney;
    }

    public void setMinTranslateMoney(BigDecimal minTranslateMoney) {
        this.minTranslateMoney = minTranslateMoney;
    }

    public Integer getIsActivity() {
        return isActivity;
    }

    public void setIsActivity(Integer isActivity) {
        this.isActivity = isActivity;
    }

    public String getShopDetailId() {
        return shopDetailId;
    }

    public void setShopDetailId(String shopDetailId) {
        this.shopDetailId = shopDetailId == null ? null : shopDetailId.trim();
    }
}