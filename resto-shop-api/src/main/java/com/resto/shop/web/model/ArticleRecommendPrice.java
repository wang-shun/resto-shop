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

    private int maxCount;

    private String articleName;

    private int sort;

    private Date createTime;

    private BigDecimal articlePrice;

    final public BigDecimal getArticlePrice() {
        return articlePrice;
    }

    final public void setArticlePrice(BigDecimal articlePrice) {
        this.articlePrice = articlePrice;
    }

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

    final public int getMaxCount() {
        return maxCount;
    }

    final public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    final public String getArticleName() {
        return articleName;
    }

    final public void setArticleName(String articleName) {
        this.articleName = articleName;
    }

    final public int getSort() {
        return sort;
    }

    final public void setSort(int sort) {
        this.sort = sort;
    }

    final public Date getCreateTime() {
        return createTime;
    }

    final public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
