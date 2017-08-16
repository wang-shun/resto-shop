package com.resto.shop.web.posDto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by KONATA on 2017/8/16.
 */
public class OrderPaymentDto implements Serializable {
    private static final long serialVersionUID = -1503612841257492498L;

    //主键
    private String id;
    //付款金额
    private BigDecimal payValue;
    //备注
    private String remark;
    //付款时间
    private Date payTime;
    //订单id
    private String orderId;
    //付款类型
    private Integer paymentModeId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
        this.remark = remark;
    }

    public Date getPayTime() {
        return payTime;
    }

    public void setPayTime(Date payTime) {
        this.payTime = payTime;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Integer getPaymentModeId() {
        return paymentModeId;
    }

    public void setPaymentModeId(Integer paymentModeId) {
        this.paymentModeId = paymentModeId;
    }
}
