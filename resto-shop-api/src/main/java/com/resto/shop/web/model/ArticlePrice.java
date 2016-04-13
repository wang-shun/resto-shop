package com.resto.shop.web.model;

import java.math.BigDecimal;

public class ArticlePrice {
    private String id;

    private BigDecimal price;

    private BigDecimal fansPrice;

    private String name;

    private String peference;

    private Integer sort;

    private String articleId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getFansPrice() {
        return fansPrice;
    }

    public void setFansPrice(BigDecimal fansPrice) {
        this.fansPrice = fansPrice;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getPeference() {
        return peference;
    }

    public void setPeference(String peference) {
        this.peference = peference == null ? null : peference.trim();
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId == null ? null : articleId.trim();
    }
}