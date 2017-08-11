package com.resto.shop.web.posDto;

import com.resto.shop.web.model.Order;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by KONATA on 2017/8/11.
 */
public class OrderDto implements Serializable{
    private static final long serialVersionUID = -343174307546088227L;

    public OrderDto() {
    }

    public OrderDto(Order order) {
        this.id = order.getId();
        this.tableNumber = order.getTableNumber();
        this.customerCount = order.getCustomerCount();
        this.accountingTime = order.getAccountingTime();
        this.orderState = order.getOrderState();
        this.productionStatus = order.getProductionStatus();
        this.originalAmount = order.getOriginalAmount();
        this.orderMoney = order.getOrderMoney();
        this.articleCount = order.getArticleCount();
        this.serialNumber = order.getSerialNumber();
        this.allowCancel = order.getAllowCancel();
        this.closed = order.getClosed();
        this.createTime = order.getCreateTime();
        this.pushOrderTime = order.getPushOrderTime();
        this.printOrderTime = order.getPrintOrderTime();
        this.remark = order.getRemark();
        this.distributionModeId = order.getDistributionModeId();
        this.amountWithChildren = order.getAmountWithChildren();
        this.parentOrderId = order.getParentOrderId();
        this.servicePrice = order.getServicePrice();
        this.shopDetailId = order.getShopDetailId();
        this.payType = order.getPayType();
        this.countWithChild = order.getCountWithChild();
        this.allowContinueOrder = order.getAllowContinueOrder();
        this.paymentAmount = order.getPaymentAmount();
        this.customerId = order.getCustomerId();
    }

    //订单id
    private String id;
    //桌号
    private String tableNumber;
    //人数
    private Integer customerCount;
    //订单创建日期
    private Date accountingTime;
    //订单状态
    private Integer orderState;
    //生产状态
    private Integer productionStatus;
    //订单原价
    private BigDecimal originalAmount;
    //单比订单总金额
    private BigDecimal orderMoney;
    //单比订单菜品总数量
    private Integer articleCount;
    //订单序列号
    private String serialNumber;
    //是否允许取消订单
    private Boolean allowCancel;
    //订单是否结束流程
    private Boolean  closed;
    //订单创建时间
    private Date  createTime;
    //订单推送时间
    private Date pushOrderTime;
    //订单打印时间
    private Date printOrderTime;
    //备注
    private String remark;
    //订单类型 1堂吃 2 外卖 3 外带
    private Integer  distributionModeId;
    //加菜后主订单的订单总金额
    private BigDecimal amountWithChildren;
    //父订单id
    private  String parentOrderId;
    //服务费
    private BigDecimal servicePrice;
    //店铺id
    private String  shopDetailId;
    //支付类型
    private Integer payType;
    //加菜后主订单的菜品总数量
    private Integer countWithChild;
    //是否允许加菜
    private Boolean allowContinueOrder;
    //应付金额（扣除优惠券+余额）
    private BigDecimal paymentAmount;
    //用户id
    private String customerId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    public Integer getCustomerCount() {
        return customerCount;
    }

    public void setCustomerCount(Integer customerCount) {
        this.customerCount = customerCount;
    }

    public Date getAccountingTime() {
        return accountingTime;
    }

    public void setAccountingTime(Date accountingTime) {
        this.accountingTime = accountingTime;
    }

    public Integer getOrderState() {
        return orderState;
    }

    public void setOrderState(Integer orderState) {
        this.orderState = orderState;
    }

    public Integer getProductionStatus() {
        return productionStatus;
    }

    public void setProductionStatus(Integer productionStatus) {
        this.productionStatus = productionStatus;
    }

    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(BigDecimal originalAmount) {
        this.originalAmount = originalAmount;
    }

    public BigDecimal getOrderMoney() {
        return orderMoney;
    }

    public void setOrderMoney(BigDecimal orderMoney) {
        this.orderMoney = orderMoney;
    }

    public Integer getArticleCount() {
        return articleCount;
    }

    public void setArticleCount(Integer articleCount) {
        this.articleCount = articleCount;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public Boolean getAllowCancel() {
        return allowCancel;
    }

    public void setAllowCancel(Boolean allowCancel) {
        this.allowCancel = allowCancel;
    }

    public Boolean getClosed() {
        return closed;
    }

    public void setClosed(Boolean closed) {
        this.closed = closed;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getPushOrderTime() {
        return pushOrderTime;
    }

    public void setPushOrderTime(Date pushOrderTime) {
        this.pushOrderTime = pushOrderTime;
    }

    public Date getPrintOrderTime() {
        return printOrderTime;
    }

    public void setPrintOrderTime(Date printOrderTime) {
        this.printOrderTime = printOrderTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getDistributionModeId() {
        return distributionModeId;
    }

    public void setDistributionModeId(Integer distributionModeId) {
        this.distributionModeId = distributionModeId;
    }

    public BigDecimal getAmountWithChildren() {
        return amountWithChildren;
    }

    public void setAmountWithChildren(BigDecimal amountWithChildren) {
        this.amountWithChildren = amountWithChildren;
    }

    public String getParentOrderId() {
        return parentOrderId;
    }

    public void setParentOrderId(String parentOrderId) {
        this.parentOrderId = parentOrderId;
    }

    public BigDecimal getServicePrice() {
        return servicePrice;
    }

    public void setServicePrice(BigDecimal servicePrice) {
        this.servicePrice = servicePrice;
    }

    public String getShopDetailId() {
        return shopDetailId;
    }

    public void setShopDetailId(String shopDetailId) {
        this.shopDetailId = shopDetailId;
    }

    public Integer getPayType() {
        return payType;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }

    public Integer getCountWithChild() {
        return countWithChild;
    }

    public void setCountWithChild(Integer countWithChild) {
        this.countWithChild = countWithChild;
    }

    public Boolean getAllowContinueOrder() {
        return allowContinueOrder;
    }

    public void setAllowContinueOrder(Boolean allowContinueOrder) {
        this.allowContinueOrder = allowContinueOrder;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }


}
