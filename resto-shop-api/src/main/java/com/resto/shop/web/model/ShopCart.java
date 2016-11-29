package com.resto.shop.web.model;

import java.util.List;

public class ShopCart {
    private Integer id;

    private Integer number;

    private String customerId;

    private String articleId;

    private String shopDetailId;

    private Integer distributionModeId;

    private String userId;

    private String shopType;

    private Integer pid;

    private List<ShopCart> currentItem;

    private Integer attrId;

    private String recommendId;

    private String recommendArticleId;

    public String getRecommendArticleId() {
        return recommendArticleId;
    }

    public void setRecommendArticleId(String recommendArticleId) {
        this.recommendArticleId = recommendArticleId;
    }

    public String getRecommendId() {
        return recommendId;
    }

    public void setRecommendId(String recommendId) {
        this.recommendId = recommendId;
    }

    public Integer getAttrId() {
        return attrId;
    }

    public void setAttrId(Integer attrId) {
        this.attrId = attrId;
    }

    public String getUserId() { return userId; }

    public void setUserId(String userId) { this.userId = userId; }

    public List<ShopCart> getCurrentItem() { return currentItem; }

    public void setCurrentItem(List<ShopCart> currentItem) { this.currentItem = currentItem; }

    public String getShopType() { return shopType; }

    public void setShopType(String shopType) { this.shopType = shopType; }

    public Integer getPid() { return pid; }

    public void setPid(Integer pid) { this.pid = pid; }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId == null ? null : customerId.trim();
    }

    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId == null ? null : articleId.trim();
    }

    public String getShopDetailId() {
        return shopDetailId;
    }

    public void setShopDetailId(String shopDetailId) {
        this.shopDetailId = shopDetailId == null ? null : shopDetailId.trim();
    }

    public Integer getDistributionModeId() {
        return distributionModeId;
    }

    public void setDistributionModeId(Integer distributionModeId) {
        this.distributionModeId = distributionModeId;
    }
}