package com.resto.shop.web.model;

import java.math.BigDecimal;
import java.util.Date;

public class Appraise {
    private String id;

    private String pictureUrl;

    private Byte level;

    private Date createTime;

    private String content;

    private Byte status;

    private Byte type;

    private String feedback;

    private BigDecimal redMoney;

    private String customerId;

    private String orderId;

    private String articleId;

    private String shopDetailId;
    
    /**
     * 用于保存 当前店铺 的所有评论的条数
     */
    private Integer APPRAISE_COUNT;
    
    /**
     * 用于保存 当前店铺的 所有评论的平均分
     */
    private Double AVG_SCORE;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl == null ? null : pictureUrl.trim();
    }

    public Byte getLevel() {
        return level;
    }

    public void setLevel(Byte level) {
        this.level = level;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content == null ? null : content.trim();
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public Byte getType() {
        return type;
    }

    public void setType(Byte type) {
        this.type = type;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback == null ? null : feedback.trim();
    }

    public BigDecimal getRedMoney() {
        return redMoney;
    }

    public void setRedMoney(BigDecimal redMoney) {
        this.redMoney = redMoney;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId == null ? null : customerId.trim();
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

    public String getShopDetailId() {
        return shopDetailId;
    }

    public void setShopDetailId(String shopDetailId) {
        this.shopDetailId = shopDetailId == null ? null : shopDetailId.trim();
    }

	public Integer getAPPRAISE_COUNT() {
		return APPRAISE_COUNT;
	}

	public void setAPPRAISE_COUNT(Integer aPPRAISE_COUNT) {
		APPRAISE_COUNT = aPPRAISE_COUNT;
	}

	public Double getAVG_SCORE() {
		return AVG_SCORE;
	}

	public void setAVG_SCORE(Double aVG_SCORE) {
		AVG_SCORE = aVG_SCORE;
	}
}