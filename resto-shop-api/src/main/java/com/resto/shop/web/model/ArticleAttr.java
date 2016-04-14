package com.resto.shop.web.model;

import java.util.List;

public class ArticleAttr {
    private Integer id;

    private String name;

    private Integer sort;

    private String shopDetailId;
    
    private String state;
    
    /**
     * 规格集合
     */
    private String[] units;
    
    /**
     * 规格 排序 集合
     */
    private String[] unitSorts;
    
    /**
     * 用于 查询时 保存 规格的集合
     */
    private List<ArticleUnit> articleUnits;

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

    public String getShopDetailId() {
        return shopDetailId;
    }

    public void setShopDetailId(String shopDetailId) {
        this.shopDetailId = shopDetailId == null ? null : shopDetailId.trim();
    }

	public String[] getUnits() {
		return units;
	}

	public void setUnits(String[] units) {
		this.units = units;
	}

	public String[] getUnitSorts() {
		return unitSorts;
	}

	public void setUnitSorts(String[] unitSorts) {
		this.unitSorts = unitSorts;
	}

	public List<ArticleUnit> getArticleUnits() {
		return articleUnits;
	}

	public void setArticleUnits(List<ArticleUnit> articleUnits) {
		this.articleUnits = articleUnits;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
	
	
}