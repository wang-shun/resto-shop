package com.resto.shop.web.model;

import java.util.List;

/**
 * Created by KONATA on 2016/9/8.
 */
public class ArticleRecommend {

    private String id;

    private String name;

    private int count;

    private int isUsed;

    private int sort;

    private List<ArticleRecommendPrice> articles;

    final public List<ArticleRecommendPrice> getArticles() {
        return articles;
    }

    final public void setArticles(List<ArticleRecommendPrice> articles) {
        this.articles = articles;
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

    final public int getCount() {
        return count;
    }

    final public void setCount(int count) {
        this.count = count;
    }

    final public int getIsUsed() {
        return isUsed;
    }

    final public void setIsUsed(int isUsed) {
        this.isUsed = isUsed;
    }

    final public int getSort() {
        return sort;
    }

    final public void setSort(int sort) {
        this.sort = sort;
    }
}
