package com.resto.shop.web.model;

import java.math.BigDecimal;
import java.util.Date;

public class AccountLog {
    private String id;

    private BigDecimal money;

    private Date createTime;

    private Byte paymentType;

    private BigDecimal remain;

    private String remark;

    private String tbAccountId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public BigDecimal getMoney() {    
        return money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Byte getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(Byte paymentType) {
        this.paymentType = paymentType;
    }

    public BigDecimal getRemain() {
        return remain;
    }

    public void setRemain(BigDecimal remain) {
        this.remain = remain;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public String getTbAccountId() {
        return tbAccountId;
    }

    public void setTbAccountId(String tbAccountId) {
        this.tbAccountId = tbAccountId == null ? null : tbAccountId.trim();
    }
}