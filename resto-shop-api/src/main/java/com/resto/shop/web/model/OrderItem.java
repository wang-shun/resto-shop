package com.resto.shop.web.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.resto.brand.core.util.DateUtil;

@JsonInclude(Include.NON_EMPTY)
public class OrderItem {
    private String id;

    private String articleName;

    private String articleDesignation;

    private Integer count;

    private BigDecimal originalPrice;

    private BigDecimal unitPrice;

    private BigDecimal finalPrice;

    private String remark;

    private Integer sort;

    private Integer status;

    private String orderId;

    private String articleId;

    private Integer type;
    
    private Integer[] mealItems;
    
    private String parentId;

    private Date createTime;
    
    private List<OrderItem> children;
    
    private Integer articleSum;
    
    //关联菜品类别
    private ArticleFamily articleFamily;
    
    //关联店铺ID 用于中间数据库 报表问题
    private String shopId;
    //关联   用户电话 用于中间数据库 报表问题
    private String telephone;


    private String name;

    private BigDecimal price;

    private Integer mealItemId;

    final public Integer getMealItemId() {
        return mealItemId;
    }

    final public void setMealItemId(Integer mealItemId) {
        this.mealItemId = mealItemId;
    }

    final public BigDecimal getPrice() {
        return price;
    }

    final public void setPrice(BigDecimal price) {
        this.price = price;
    }

    final public String getName() {
        return name;
    }

    final public void setName(String name) {
        this.name = name;
    }

    public ArticleFamily getArticleFamily() {
		return articleFamily;
	}

	public void setArticleFamily(ArticleFamily articleFamily) {
		this.articleFamily = articleFamily;
	}

	public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getArticleName() {
        return articleName;
    }

    public void setArticleName(String articleName) {
        this.articleName = articleName == null ? null : articleName.trim();
    }

    public String getArticleDesignation() {
        return articleDesignation;
    }

    public void setArticleDesignation(String articleDesignation) {
        this.articleDesignation = articleDesignation == null ? null : articleDesignation.trim();
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(BigDecimal finalPrice) {
        this.finalPrice = finalPrice;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId == null ? null : orderId.trim();
    }

    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId == null ? null : articleId.trim();
    }

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Integer[] getMealItems() {
		return mealItems;
	}

	public void setMealItems(Integer[] mealItems) {
		this.mealItems = mealItems;
	}

	public String getParentId() {
		return parentId;
	}

	public String getCreateTime() {
		return DateUtil.formatDate(this.createTime, "yyyy-MM-dd HH:mm:ss");
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public List<OrderItem> getChildren() {
		return children;
	}

	public void setChildren(List<OrderItem> children) {
		this.children = children;
	}

	public Integer getArticleSum() {
		return articleSum;
	}

	public void setArticleSum(Integer articleSum) {
		this.articleSum = articleSum;
	}

	public String getShopId() {
		return shopId;
	}

	public void setShopId(String shopId) {
		this.shopId = shopId;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}
	
}