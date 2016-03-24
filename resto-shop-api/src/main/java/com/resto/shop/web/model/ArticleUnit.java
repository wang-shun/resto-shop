package com.resto.shop.web.model;

import java.math.BigDecimal;

public class ArticleUnit {
    private Integer id;

    private String ogPrice;

    private BigDecimal price;

    private String unit;

    private String articleId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOgPrice() {
        return ogPrice;
    }

    public void setOgPrice(String ogPrice) {
        this.ogPrice = ogPrice == null ? null : ogPrice.trim();
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit == null ? null : unit.trim();
    }

    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId == null ? null : articleId.trim();
    }
}