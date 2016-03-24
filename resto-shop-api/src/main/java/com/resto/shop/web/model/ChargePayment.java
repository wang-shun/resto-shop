package com.resto.shop.web.model;

import java.math.BigDecimal;
import java.util.Date;

public class ChargePayment {
    private String id;

    private BigDecimal paymentMoney;

    private Date createTime;

    private String tbChargeOrderId;

    private String payData;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public BigDecimal getPaymentMoney() {
        return paymentMoney;
    }

    public void setPaymentMoney(BigDecimal paymentMoney) {
        this.paymentMoney = paymentMoney;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getTbChargeOrderId() {
        return tbChargeOrderId;
    }

    public void setTbChargeOrderId(String tbChargeOrderId) {
        this.tbChargeOrderId = tbChargeOrderId == null ? null : tbChargeOrderId.trim();
    }

    public String getPayData() {
        return payData;
    }

    public void setPayData(String payData) {
        this.payData = payData == null ? null : payData.trim();
    }
}