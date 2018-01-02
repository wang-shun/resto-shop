package com.resto.shop.web.posDto;

import com.resto.shop.web.model.OrderPaymentItem;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by KONATA on 2017/8/16.
 */
public class OrderPaymentDto implements Serializable {
    private static final long serialVersionUID = -1503612841257492498L;
    public OrderPaymentDto(){}
    public OrderPaymentDto(OrderPaymentItem orderPaymentItem){
        this.id = orderPaymentItem.getId() == null ? "" : orderPaymentItem.getId();
        this.payValue = orderPaymentItem.getPayValue() == null ? BigDecimal.valueOf(0) : orderPaymentItem.getPayValue();
        this.remark = orderPaymentItem.getRemark() == null ? "" : orderPaymentItem.getRemark();
        this.payTime = orderPaymentItem.getPayTime().getTime() ;
        this.orderId = orderPaymentItem.getOrderId() == null ? "" : orderPaymentItem.getOrderId();
        this.paymentModeId = orderPaymentItem.getPaymentModeId() == null ? 0 : orderPaymentItem.getPaymentModeId();
        this.resultData = orderPaymentItem.getResultData() == null ? "" : orderPaymentItem.getResultData();
    }

    //主键
    private String id;
    //付款金额
    private BigDecimal payValue;
    //备注
    private String remark;
    //付款时间
    private Long payTime;
    //订单id
    private String orderId;
    //付款类型
    private Integer paymentModeId;

    private String resultData;

    public String getResultData() {
        return resultData;
    }

    public void setResultData(String resultData) {
        this.resultData = resultData;
    }

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

    public Long getPayTime() {
        return payTime;
    }

    public void setPayTime(Long payTime) {
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
