package com.resto.shop.web.model;

import java.util.List;

public class MealAttr {
    private Integer id;

    private String name;

    private Integer sort;

    private String articleId;

    private List<MealItem> mealItems;
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
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

	public List<MealItem> getMealItems() {
		return mealItems;
	}

	public void setMealItems(List<MealItem> mealItems) {
		this.mealItems = mealItems;
	}
}