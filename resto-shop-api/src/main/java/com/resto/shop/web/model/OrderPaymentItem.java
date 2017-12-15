package com.resto.shop.web.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class OrderPaymentItem implements Serializable {
    private String id;

    private Date payTime;

    private BigDecimal payValue;

    private String remark;

    private Integer paymentModeId;

    /**
     * 用于保存支付方式
     */
    private String paymentModeVal;

    private String orderId;

    private String resultData;

    //新增字段用来存营收总额
    private  BigDecimal factIncome;

    //新增用来关联店铺id
    private BigDecimal originalAmount;

    private  String shopDetailId;

    private Integer isUseBonus;

    private String toPayId;

    public String getToPayId() {
        return toPayId;
    }

    public void setToPayId(String toPayId) {
        this.toPayId = toPayId;
    }

    public Integer getIsUseBonus() {
        return isUseBonus;
    }

    public void setIsUseBonus(Integer isUseBonus) {
        this.isUseBonus = isUseBonus;
    }

    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(BigDecimal originalAmount) {
        this.originalAmount = originalAmount;
    }

    public String getShopDetailId() {
        return shopDetailId;
    }

    public void setShopDetailId(String shopDetailId) {
        this.shopDetailId = shopDetailId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public Date getPayTime() {
        return payTime;
    }

    public void setPayTime(Date payTime) {
        this.payTime = payTime;
    }

    public BigDecimal getPayValue() {
        return payValue;
    }

    public void setPayValue(BigDecimal payValue) {
        this.payValue = payValue;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public Integer getPaymentModeId() {
        return paymentModeId;
    }

    public void setPaymentModeId(Integer paymentModeId) {
        this.paymentModeId = paymentModeId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId == null ? null : orderId.trim();
    }

    public String getResultData() {
        return resultData;
    }

    public void setResultData(String resultData) {
        this.resultData = resultData == null ? null : resultData.trim();
    }

    public String getPaymentModeVal() {
        return paymentModeVal;
    }

    public void setPaymentModeVal(String paymentModeVal) {
        this.paymentModeVal = paymentModeVal;
    }

    public OrderPaymentItem(BigDecimal payValue, Integer paymentModeId, String paymentModeVal) {
        super();
        this.payValue = payValue;
        this.paymentModeId = paymentModeId;
        this.paymentModeVal = paymentModeVal;
    }

    public OrderPaymentItem(){

    }

    public  OrderPaymentItem(BigDecimal factIncome){
        super();
        this.factIncome = factIncome;
    }


}