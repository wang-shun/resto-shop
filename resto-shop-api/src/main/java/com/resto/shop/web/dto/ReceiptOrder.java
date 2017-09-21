package com.resto.shop.web.dto;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by xielc on 2017/9/5.
 * 开发票订单dto
 */
public class ReceiptOrder {

    private String orderNumber;

    private Date payTime;

    private BigDecimal orderMoney;

    private BigDecimal receiptMoney;

    private Integer state;

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Date getPayTime() {
        return payTime;
    }

    public void setPayTime(Date payTime) {
        this.payTime = payTime;
    }

    public BigDecimal getOrderMoney() {
        return orderMoney;
    }

    public void setOrderMoney(BigDecimal orderMoney) {
        this.orderMoney = orderMoney;
    }

    public BigDecimal getReceiptMoney() {
        return receiptMoney;
    }

    public void setReceiptMoney(BigDecimal receiptMoney) {
        this.receiptMoney = receiptMoney;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }
}
