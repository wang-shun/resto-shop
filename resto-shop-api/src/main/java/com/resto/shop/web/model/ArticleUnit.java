package com.resto.shop.web.model;

import java.math.BigDecimal;

public class ArticleUnit {
    private Integer id;

    private String name;

    private BigDecimal sort;

    private Integer tbArticleAttrId;

    //构造方法
    public ArticleUnit(String name, BigDecimal sort, Integer tbArticleAttrId) {
		super();
		this.name = name;
		this.sort = sort;
		this.tbArticleAttrId = tbArticleAttrId;
	}
    //默认构造方法
    public ArticleUnit() {}

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

    public BigDecimal getSort() {
        return sort;
    }

    public void setSort(BigDecimal sort) {
        this.sort = sort;
    }

    public Integer getTbArticleAttrId() {
        return tbArticleAttrId;
    }

    public void setTbArticleAttrId(Integer tbArticleAttrId) {
        this.tbArticleAttrId = tbArticleAttrId;
    }
}