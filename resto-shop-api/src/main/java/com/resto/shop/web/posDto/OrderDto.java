package com.resto.shop.web.posDto;

import com.resto.shop.web.model.Order;
import com.resto.shop.web.model.OrderRefundRemark;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by KONATA on 2017/8/11.
 */
public class OrderDto implements Serializable {
    private static final long serialVersionUID = -343174307546088227L;

    public OrderDto() {
    }

    public OrderDto(Order order) {
        this.id = order.getId() == null ? "" : order.getId();
        this.tableNumber = order.getTableNumber() == null ? "" : order.getTableNumber();
        this.customerCount = order.getCustomerCount() == null ? 0 : order.getCustomerCount();
        this.accountingTime = order.getAccountingTime() == null ? new Date() : order.getAccountingTime();
        this.orderState = order.getOrderState() == null ? 0 : order.getOrderState();
        this.productionStatus = order.getProductionStatus() == 0 ? 1 : order.getProductionStatus();
        this.originalAmount = order.getOriginalAmount() == null ? BigDecimal.valueOf(0) : order.getOriginalAmount();
        this.orderMoney = order.getOrderMoney() == null ? BigDecimal.valueOf(0) : order.getOrderMoney();
        this.articleCount = order.getArticleCount() == null ? 0 : order.getArticleCount();
        this.serialNumber = order.getSerialNumber() == null ? "" : order.getSerialNumber();
        this.allowCancel = order.getAllowCancel() == null ? 0 : order.getAllowCancel() ? 1 : 0;
        this.closed = order.getClosed() == null ? 0 : order.getClosed() ? 1 : 0;
        this.createTime = order.getCreateTime() == null ? new Date().getTime() : order.getCreateTime().getTime();
        this.pushOrderTime = order.getPushOrderTime() == null ? new Date().getTime() : order.getPushOrderTime().getTime();
        this.printOrderTime = order.getPrintOrderTime() == null ? new Date().getTime() : order.getPrintOrderTime().getTime();
        this.remark = order.getRemark() == null ? "" : order.getRemark();
        this.distributionModeId = order.getDistributionModeId() == null ? 0 : order.getDistributionModeId();
        this.amountWithChildren = order.getAmountWithChildren() == null ? BigDecimal.valueOf(0) : order.getAmountWithChildren();
        this.parentOrderId = order.getParentOrderId() == null ? "" : order.getParentOrderId();
        this.servicePrice = order.getServicePrice() == null ? BigDecimal.valueOf(0) : order.getServicePrice();
        this.shopDetailId = order.getShopDetailId() == null ? "" : order.getShopDetailId();
        this.payType = order.getPayType() == null ? 0 : order.getPayType();
        this.countWithChild = order.getCountWithChild() == null ? 0 : order.getCountWithChild();
        this.allowContinueOrder = order.getAllowContinueOrder() == null ? 0 : order.getAllowContinueOrder() ? 1 : 0;
        this.paymentAmount = order.getPaymentAmount() == null ? BigDecimal.valueOf(0) : order.getPaymentAmount();
        this.customerId = order.getCustomerId() == null ? "0" : order.getCustomerId();
        this.customerAddressId = order.getCustomerAddressId() == null ? "" : order.getCustomerAddressId();
        this.verCode = order.getVerCode() == null ? "" : order.getVerCode();
        this.payMode = order.getPayMode();
        this.mealAllNumber = order.getMealAllNumber();
        this.mealFeePrice = order.getMealFeePrice();
        this.isPosPay = order.getIsPosPay();
        this.allowAppraise = order.getAllowAppraise();
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
    private Integer allowCancel;
    //订单是否结束流程
    private Integer closed;
    //订单创建时间
    private Long createTime;
    //订单推送时间
    private Long pushOrderTime;
    //订单打印时间
    private Long printOrderTime;
    //备注
    private String remark;
    //订单类型 1堂吃 2 外卖 3 外带
    private Integer distributionModeId;
    //加菜后主订单的订单总金额
    private BigDecimal amountWithChildren;
    //父订单id
    private String parentOrderId;
    //服务费
    private BigDecimal servicePrice;
    //店铺id
    private String shopDetailId;
    //支付类型
    private Integer payType;
    //加菜后主订单的菜品总数量
    private Integer countWithChild;
    //是否允许加菜
    private Integer allowContinueOrder;
    //应付金额（扣除优惠券+余额）
    private BigDecimal paymentAmount;
    //用户id
    private String customerId;
    //是否是Pos支付
    private Integer isPosPay;

    private String customerAddressId;

    private List<OrderItemDto> orderItem;

    private List<OrderPaymentDto> orderPayment;

    private String verCode;

    private Integer payMode;

    private Integer mealAllNumber;

    private BigDecimal mealFeePrice;

    private Boolean allowAppraise;

    private List<OrderDto> childrenOrders;

    private List<OrderRefundRemark> orderRefundRemarks;

    public Integer getMealAllNumber() {
        return mealAllNumber;
    }

    public void setMealAllNumber(Integer mealAllNumber) {
        this.mealAllNumber = mealAllNumber;
    }

    public BigDecimal getMealFeePrice() {
        return mealFeePrice;
    }

    public void setMealFeePrice(BigDecimal mealFeePrice) {
        this.mealFeePrice = mealFeePrice;
    }

    public Integer getPayMode() {
        return payMode;
    }

    public void setPayMode(Integer payMode) {
        this.payMode = payMode;
    }

    public String getVerCode() {
        return verCode;
    }

    public void setVerCode(String verCode) {
        this.verCode = verCode;
    }

    public String getCustomerAddressId() {
        return customerAddressId;
    }

    public void setCustomerAddressId(String customerAddressId) {
        this.customerAddressId = customerAddressId;
    }

    public List<OrderPaymentDto> getOrderPayment() {
        return orderPayment;
    }

    public void setOrderPayment(List<OrderPaymentDto> orderPayment) {
        this.orderPayment = orderPayment;
    }

    public List<OrderItemDto> getOrderItem() {
        return orderItem;
    }

    public void setOrderItem(List<OrderItemDto> orderItem) {
        this.orderItem = orderItem;
    }

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

    public Integer getAllowCancel() {
        return allowCancel;
    }

    public void setAllowCancel(Integer allowCancel) {
        this.allowCancel = allowCancel;
    }

    public Integer getClosed() {
        return closed;
    }

    public void setClosed(Integer closed) {
        this.closed = closed;
    }

    public Integer getAllowContinueOrder() {
        return allowContinueOrder;
    }

    public void setAllowContinueOrder(Integer allowContinueOrder) {
        this.allowContinueOrder = allowContinueOrder;
    }

    public Date getAccountingTime() {
        return accountingTime;
    }

    public void setAccountingTime(Date accountingTime) {
        this.accountingTime = accountingTime;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getPushOrderTime() {
        return pushOrderTime;
    }

    public void setPushOrderTime(Long pushOrderTime) {
        this.pushOrderTime = pushOrderTime;
    }

    public Long getPrintOrderTime() {
        return printOrderTime;
    }

    public void setPrintOrderTime(Long printOrderTime) {
        this.printOrderTime = printOrderTime;
    }

    public Integer getIsPosPay() {
        return isPosPay;
    }

    public void setIsPosPay(Integer isPosPay) {
        this.isPosPay = isPosPay;
    }

    public List<OrderRefundRemark> getOrderRefundRemarks() {
        return orderRefundRemarks;
    }

    public void setOrderRefundRemarks(List<OrderRefundRemark> orderRefundRemarks) {
        this.orderRefundRemarks = orderRefundRemarks;
    }

    public List<OrderDto> getChildrenOrders() {
        return childrenOrders;
    }

    public void setChildrenOrders(List<OrderDto> childrenOrders) {
        this.childrenOrders = childrenOrders;
    }

    public Boolean getAllowAppraise() {
        return allowAppraise;
    }

    public void setAllowAppraise(Boolean allowAppraise) {
        this.allowAppraise = allowAppraise;
    }
}
