package com.resto.shop.web.posDto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by KONATA on 2017/8/16.
 */
public class OrderItemDto implements Serializable {
    private static final long serialVersionUID = -1364031233735055169L;
    //状态
    private Integer status;
    //主键
    private String id;
    //创建时间
    private Date createTime;
    //原始数量
    private Integer orginCount;
    //餐盒数量
    private Integer mealFeeNumber;
    //类型
    private Integer type;
    //排序
    private Integer sort;
    //折扣
    private String remark;
    //修改的数量
    private Integer changeCount;
    //最终金额
    private BigDecimal finalPrice;
    //菜品id
    private String articleId;
    //当前数量
    private Integer count;
    //订单id
    private String orderId;
    //原价
    private BigDecimal originalPrice;
    //菜品名称
    private String articleName;
    //退菜数量
    private Integer refundCount;
    //单价
    private BigDecimal unitPrice;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getOrginCount() {
        return orginCount;
    }

    public void setOrginCount(Integer orginCount) {
        this.orginCount = orginCount;
    }

    public Integer getMealFeeNumber() {
        return mealFeeNumber;
    }

    public void setMealFeeNumber(Integer mealFeeNumber) {
        this.mealFeeNumber = mealFeeNumber;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getChangeCount() {
        return changeCount;
    }

    public void setChangeCount(Integer changeCount) {
        this.changeCount = changeCount;
    }

    public BigDecimal getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(BigDecimal finalPrice) {
        this.finalPrice = finalPrice;
    }

    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public String getArticleName() {
        return articleName;
    }

    public void setArticleName(String articleName) {
        this.articleName = articleName;
    }

    public Integer getRefundCount() {
        return refundCount;
    }

    public void setRefundCount(Integer refundCount) {
        this.refundCount = refundCount;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
}
