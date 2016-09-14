package com.resto.shop.web.model;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by KONATA on 2016/9/8.
 */
public class ArticleRecommendPrice {

    private String id;

    private String recommendId;

    private String articleId;

    private Integer maxCount;

    private String articleName;

    private Integer sort;

    private Date createTime;

    private BigDecimal price;


    final public String getId() {
        return id;
    }

    final public void setId(String id) {
        this.id = id;
    }

    final public String getRecommendId() {
        return recommendId;
    }

    final public void setRecommendId(String recommendId) {
        this.recommendId = recommendId;
    }

    final public String getArticleId() {
        return articleId;
    }

    final public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    final public Integer getMaxCount() {
        return maxCount;
    }

    final public void setMaxCount(Integer maxCount) {
        this.maxCount = maxCount;
    }

    final public String getArticleName() {
        return articleName;
    }

    final public void setArticleName(String articleName) {
        this.articleName = articleName;
    }

    final public Integer getSort() {
        return sort;
    }

    final public void setSort(Integer sort) {
        this.sort = sort;
    }

    final public Date getCreateTime() {
        return createTime;
    }

    final public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    final public BigDecimal getPrice() {
        return price;
    }

    final public void setPrice(BigDecimal price) {
        this.price = price;
    }
}