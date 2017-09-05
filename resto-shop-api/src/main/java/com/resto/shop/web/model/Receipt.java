package com.resto.shop.web.model;

import java.util.Date;

public class Receipt {
    private Long id;

    private String orderNumber;

    private Date payTime;

    private Long orderMoney;

    private Long receiptMoney;

    private Long receiptTitleId;

    private Integer state;

    private Date createTime;

    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber == null ? null : orderNumber.trim();
    }

    public Date getPayTime() {
        return payTime;
    }

    public void setPayTime(Date payTime) {
        this.payTime = payTime;
    }

    public Long getOrderMoney() {
        return orderMoney;
    }

    public void setOrderMoney(Long orderMoney) {
        this.orderMoney = orderMoney;
    }

    public Long getReceiptMoney() {
        return receiptMoney;
    }

    public void setReceiptMoney(Long receiptMoney) {
        this.receiptMoney = receiptMoney;
    }

    public Long getReceiptTitleId() {
        return receiptTitleId;
    }

    public void setReceiptTitleId(Long receiptTitleId) {
        this.receiptTitleId = receiptTitleId;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}