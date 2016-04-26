package com.resto.shop.web.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Article {
    private String id;

    private String name;

    private String nameAlias;

    private String nameShort;

    private String photoBig;

    private String photoSmall;

    private String ingredients;

    private String description;

    private Boolean isEmpty;

    private Integer sort;

    private Byte activated;

    private Boolean state;

    private Integer remainNumber;

    private Long saleNumber;

    private Long showSaleNumber;

    private Date updateTime;

    private Date createTime;

    private String shopDetailId;

    private String articleFamilyId;

    private String createUserId;

    private String updateUserId;

    private BigDecimal price;

    private BigDecimal fansPrice;

    private Boolean hasMultPrice;
    
    private String hasUnit;
    
    private String peference;
    
    private String unit;
    
    private ArticleFamily articleFamily;
    
    public ArticleFamily getArticleFamily() {
		return articleFamily;
	}

	public void setArticleFamily(ArticleFamily articleFamily) {
		this.articleFamily = articleFamily;
	}

	/**
     * 用于保存 类型名称
     */
    private String articleFamilyName;
    
    private List<ArticlePrice> articlePrices= new ArrayList<>();
    private Integer [] supportTimes;
    private Integer [] kitchenList;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getNameAlias() {
        return nameAlias;
    }

    public void setNameAlias(String nameAlias) {
        this.nameAlias = nameAlias == null ? null : nameAlias.trim();
    }

    public String getNameShort() {
        return nameShort;
    }

    public void setNameShort(String nameShort) {
        this.nameShort = nameShort == null ? null : nameShort.trim();
    }

    public String getPhotoBig() {
        return photoBig;
    }

    public void setPhotoBig(String photoBig) {
        this.photoBig = photoBig == null ? null : photoBig.trim();
    }

    public String getPhotoSmall() {
        return photoSmall;
    }

    public void setPhotoSmall(String photoSmall) {
        this.photoSmall = photoSmall == null ? null : photoSmall.trim();
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients == null ? null : ingredients.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public Boolean getIsEmpty() {
        return isEmpty;
    }

    public void setIsEmpty(Boolean isEmpty) {
        this.isEmpty = isEmpty;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Byte getActivated() {
        return activated;
    }

    public void setActivated(Byte activated) {
        this.activated = activated;
    }

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public Integer getRemainNumber() {
        return remainNumber;
    }

    public void setRemainNumber(Integer remainNumber) {
        this.remainNumber = remainNumber;
    }

    public Long getSaleNumber() {
        return saleNumber;
    }

    public void setSaleNumber(Long saleNumber) {
        this.saleNumber = saleNumber;
    }

    public Long getShowSaleNumber() {
        return showSaleNumber;
    }

    public List<ArticlePrice> getArticlePrices() {
		return articlePrices;
	}

	public void setArticlePrices(List<ArticlePrice> articlePrices) {
		this.articlePrices = articlePrices;
	}

	public void setShowSaleNumber(Long showSaleNumber) {
        this.showSaleNumber = showSaleNumber;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getShopDetailId() {
        return shopDetailId;
    }

    public void setShopDetailId(String shopDetailId) {
        this.shopDetailId = shopDetailId == null ? null : shopDetailId.trim();
    }

    public String getArticleFamilyId() {
        return articleFamilyId;
    }

    public void setArticleFamilyId(String articleFamilyId) {
        this.articleFamilyId = articleFamilyId == null ? null : articleFamilyId.trim();
    }

    public String getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(String createUserId) {
        this.createUserId = createUserId == null ? null : createUserId.trim();
    }

    public String getUpdateUserId() {
        return updateUserId;
    }

    public void setUpdateUserId(String updateUserId) {
        this.updateUserId = updateUserId == null ? null : updateUserId.trim();
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

    public Boolean getHasMultPrice() {
        return hasMultPrice;
    }

    public void setHasMultPrice(Boolean hasMultPrice) {
        this.hasMultPrice = hasMultPrice;
    }

	public String getHasUnit() {
		return hasUnit;
	}

	public void setHasUnit(String hasUnit) {
		this.hasUnit = hasUnit;
	}

	public Integer[] getSupportTimes() {
		return supportTimes;
	}

	public void setSupportTimes(Integer[] supportTimes) {
		this.supportTimes = supportTimes;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getPeference() {
		return peference;
	}

	public void setPeference(String peference) {
		this.peference = peference;
	}

	public Integer[] getKitchenList() {
		return kitchenList;
	}

	public void setKitchenList(Integer[] kitchenList) {
		this.kitchenList = kitchenList;
	}

	public String getArticleFamilyName() {
		return articleFamilyName;
	}

	public void setArticleFamilyName(String articleFamilyName) {
		this.articleFamilyName = articleFamilyName;
	}
	
	
}