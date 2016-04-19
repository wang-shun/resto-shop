package com.resto.shop.web.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class Order {
    private String id;

    private String tableNumber;

    private Integer customerCount;

    private Date accountingTime;

    private Byte orderState;

    private Byte productionStatus;

    private BigDecimal originalAmount;

    private BigDecimal reductionAmount;

    private BigDecimal paymentAmount;

    private BigDecimal orderMoney;

    private Integer articleCount;

    private String serialNumber;

    private Date confirmTime;

    private Integer printTimes;

    private Byte allowCancel;

    private Byte closed;

    private String remark;

    private Date createTime;

    private String operatorId;

    private String customerId;

    private Date distributionDate;

    private Integer distributionTimeId;

    private Integer deliveryPointId;

    private String shopDetailId;

    private Integer distributionModeId;
    
    private List<OrderItem> orderItems;
    
    private String verCode;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber == null ? null : tableNumber.trim();
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

    public Byte getOrderState() {
        return orderState;
    }

    public void setOrderState(Byte orderState) {
        this.orderState = orderState;
    }

    public Byte getProductionStatus() {
        return productionStatus;
    }

    public void setProductionStatus(Byte productionStatus) {
        this.productionStatus = productionStatus;
    }

    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(BigDecimal originalAmount) {
        this.originalAmount = originalAmount;
    }

    public BigDecimal getReductionAmount() {
        return reductionAmount;
    }

    public void setReductionAmount(BigDecimal reductionAmount) {
        this.reductionAmount = reductionAmount;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
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
        this.serialNumber = serialNumber == null ? null : serialNumber.trim();
    }

    public Date getConfirmTime() {
        return confirmTime;
    }

    public void setConfirmTime(Date confirmTime) {
        this.confirmTime = confirmTime;
    }

    public Integer getPrintTimes() {
        return printTimes;
    }

    public void setPrintTimes(Integer printTimes) {
        this.printTimes = printTimes;
    }

    public Byte getAllowCancel() {
        return allowCancel;
    }

    public void setAllowCancel(Byte allowCancel) {
        this.allowCancel = allowCancel;
    }

    public Byte getClosed() {
        return closed;
    }

    public void setClosed(Byte closed) {
        this.closed = closed;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId == null ? null : operatorId.trim();
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId == null ? null : customerId.trim();
    }

    public Date getDistributionDate() {
        return distributionDate;
    }

    public void setDistributionDate(Date distributionDate) {
        this.distributionDate = distributionDate;
    }

    public Integer getDistributionTimeId() {
        return distributionTimeId;
    }

    public void setDistributionTimeId(Integer distributionTimeId) {
        this.distributionTimeId = distributionTimeId;
    }

    public Integer getDeliveryPointId() {
        return deliveryPointId;
    }

    public void setDeliveryPointId(Integer deliveryPointId) {
        this.deliveryPointId = deliveryPointId;
    }

    public String getShopDetailId() {
        return shopDetailId;
    }

    public void setShopDetailId(String shopDetailId) {
        this.shopDetailId = shopDetailId == null ? null : shopDetailId.trim();
    }

    public Integer getDistributionModeId() {
        return distributionModeId;
    }

    public void setDistributionModeId(Integer distributionModeId) {
        this.distributionModeId = distributionModeId;
    }

	public List<OrderItem> getOrderItems() {
		return orderItems;
	}

	public void setOrderItems(List<OrderItem> orderItems) {
		this.orderItems = orderItems == null ? null : orderItems;
	}

	public String getVerCode() {
		return verCode;
	}

	public void setVerCode(String verCode) {
		this.verCode = verCode;
	}
    
    
}