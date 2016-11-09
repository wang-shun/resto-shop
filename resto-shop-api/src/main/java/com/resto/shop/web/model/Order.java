package com.resto.shop.web.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Order implements Serializable{
    private String id;

    private String tableNumber;

    private Integer customerCount;

    private Date accountingTime;

    private Integer orderState;

    private Integer productionStatus;

    private BigDecimal originalAmount;

    private BigDecimal reductionAmount;

    private BigDecimal paymentAmount;

    private BigDecimal orderMoney;

    private Integer articleCount;

    private String serialNumber;

    private Date confirmTime;

    private Integer printTimes;

    private Boolean allowCancel;

    private Boolean allowAppraise;

    private Boolean closed;

    private String remark;

    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
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

    private boolean useAccount;

    private String useCoupon;

    private Date pushOrderTime;

    private Date printOrderTime;

    private Date callNumberTime;

    private Integer orderMode;

    private String brandId;

    private BigDecimal amountWithChildren;

    private String parentOrderId;

    private Boolean allowContinueOrder;

    private Integer countWithChild;

    private Date lastOrderTime;

    //顾客
    private Customer customer;

    //评价
    private Appraise appraise;

    private int personCount;

    private Integer payMode;



    private Long employeeId;

    /**
     * 用于保存 订单的 菜品名称（查询时使用）
     */
    private List<String> articleNames;

    /**
     * 用于保存 店铺的名称
     */
    private String shopName;

    private Boolean timeOut;

    //
    private String telephone;


    private BigDecimal payValue;

    private int paymentModeId;

    private int orderCount;

    private BigDecimal orderTotal;

    //等位红包
    private BigDecimal waitMoney;

    private String waitId;

    final public String getWaitId() {
        return waitId;
    }

    final public void setWaitId(String waitId) {
        this.waitId = waitId;
    }

    final public BigDecimal getWaitMoney() {
        return waitMoney;
    }

    final public void setWaitMoney(BigDecimal waitMoney) {
        this.waitMoney = waitMoney;
    }

    final public Integer getPayMode() {
        return payMode;
    }

    final public void setPayMode(Integer payMode) {
        this.payMode = payMode;
    }

    private List<OrderPaymentItem> orderPaymentItems;

    private BigDecimal servicePrice;

    final public BigDecimal getServicePrice() {
        return servicePrice;
    }

    final public void setServicePrice(BigDecimal servicePrice) {
        this.servicePrice = servicePrice;
    }
    final public Long getEmployeeId() {
        return employeeId;
    }

    final public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    final public int getPersonCount() {
        return personCount;
    }

    final public void setPersonCount(int personCount) {
        this.personCount = personCount;
    }

    final public String getId() {
        return id;
    }

    final public void setId(String id) {
        this.id = id;
    }

    final public String getTableNumber() {
        return tableNumber;
    }

    final public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    final public Integer getCustomerCount() {
        return customerCount;
    }

    final public void setCustomerCount(Integer customerCount) {
        this.customerCount = customerCount;
    }

    final public Date getAccountingTime() {
        return accountingTime;
    }

    final public void setAccountingTime(Date accountingTime) {
        this.accountingTime = accountingTime;
    }

    final public Integer getOrderState() {
        return orderState;
    }

    final public void setOrderState(Integer orderState) {
        this.orderState = orderState;
    }

    final public Integer getProductionStatus() {
        return productionStatus;
    }

    final public void setProductionStatus(Integer productionStatus) {
        this.productionStatus = productionStatus;
    }

    final public BigDecimal getOriginalAmount() {
        return originalAmount;
    }

    final public void setOriginalAmount(BigDecimal originalAmount) {
        this.originalAmount = originalAmount;
    }

    final public BigDecimal getReductionAmount() {
        return reductionAmount;
    }

    final public void setReductionAmount(BigDecimal reductionAmount) {
        this.reductionAmount = reductionAmount;
    }

    final public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    final public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    final public BigDecimal getOrderMoney() {
        return orderMoney;
    }

    final public void setOrderMoney(BigDecimal orderMoney) {
        this.orderMoney = orderMoney;
    }

    final public Integer getArticleCount() {
        return articleCount;
    }

    final public void setArticleCount(Integer articleCount) {
        this.articleCount = articleCount;
    }

    final public String getSerialNumber() {
        return serialNumber;
    }

    final public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    final public Date getConfirmTime() {
        return confirmTime;
    }

    final public void setConfirmTime(Date confirmTime) {
        this.confirmTime = confirmTime;
    }

    final public Integer getPrintTimes() {
        return printTimes;
    }

    final public void setPrintTimes(Integer printTimes) {
        this.printTimes = printTimes;
    }

    final public Boolean getAllowCancel() {
        return allowCancel;
    }

    final public void setAllowCancel(Boolean allowCancel) {
        this.allowCancel = allowCancel;
    }

    final public Boolean getAllowAppraise() {
        return allowAppraise;
    }

    final public void setAllowAppraise(Boolean allowAppraise) {
        this.allowAppraise = allowAppraise;
    }

    final public Boolean getClosed() {
        return closed;
    }

    final public void setClosed(Boolean closed) {
        this.closed = closed;
    }

    final public String getRemark() {
        return remark;
    }

    final public void setRemark(String remark) {
        this.remark = remark;
    }

    final public Date getCreateTime() {
        return createTime;
    }

    final public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    final public String getOperatorId() {
        return operatorId;
    }

    final public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    final public String getCustomerId() {
        return customerId;
    }

    final public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    final public Date getDistributionDate() {
        return distributionDate;
    }

    final public void setDistributionDate(Date distributionDate) {
        this.distributionDate = distributionDate;
    }

    final public Integer getDistributionTimeId() {
        return distributionTimeId;
    }

    final public void setDistributionTimeId(Integer distributionTimeId) {
        this.distributionTimeId = distributionTimeId;
    }

    final public Integer getDeliveryPointId() {
        return deliveryPointId;
    }

    final public void setDeliveryPointId(Integer deliveryPointId) {
        this.deliveryPointId = deliveryPointId;
    }

    final public String getShopDetailId() {
        return shopDetailId;
    }

    final public void setShopDetailId(String shopDetailId) {
        this.shopDetailId = shopDetailId;
    }

    final public Integer getDistributionModeId() {
        return distributionModeId;
    }

    final public void setDistributionModeId(Integer distributionModeId) {
        this.distributionModeId = distributionModeId;
    }

    final public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    final public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    final public String getVerCode() {
        return verCode;
    }

    final public void setVerCode(String verCode) {
        this.verCode = verCode;
    }


    public String getUseCoupon() {
        return useCoupon;
    }

    public void setUseCoupon(String useCoupon) {

        this.useCoupon = useCoupon;
    }

    final public Date getPushOrderTime() {
        return pushOrderTime;
    }

    final public void setPushOrderTime(Date pushOrderTime) {
        this.pushOrderTime = pushOrderTime;
    }

    final public Date getPrintOrderTime() {
        return printOrderTime;
    }

    final public void setPrintOrderTime(Date printOrderTime) {
        this.printOrderTime = printOrderTime;
    }

    final public Date getCallNumberTime() {
        return callNumberTime;
    }

    final public void setCallNumberTime(Date callNumberTime) {
        this.callNumberTime = callNumberTime;
    }

    final public Integer getOrderMode() {
        return orderMode;
    }

    final public void setOrderMode(Integer orderMode) {
        this.orderMode = orderMode;
    }

    final public String getBrandId() {
        return brandId;
    }

    final public void setBrandId(String brandId) {
        this.brandId = brandId;
    }

    final public BigDecimal getAmountWithChildren() {
        return amountWithChildren;
    }

    final public void setAmountWithChildren(BigDecimal amountWithChildren) {
        this.amountWithChildren = amountWithChildren;
    }

    final public String getParentOrderId() {
        return parentOrderId;
    }

    final public void setParentOrderId(String parentOrderId) {
        this.parentOrderId = parentOrderId;
    }

    final public Boolean getAllowContinueOrder() {
        return allowContinueOrder;
    }

    final public void setAllowContinueOrder(Boolean allowContinueOrder) {
        this.allowContinueOrder = allowContinueOrder;
    }

    final public Integer getCountWithChild() {
        return countWithChild;
    }

    final public void setCountWithChild(Integer countWithChild) {
        this.countWithChild = countWithChild;
    }

    final public Date getLastOrderTime() {
        return lastOrderTime;
    }

    final public void setLastOrderTime(Date lastOrderTime) {
        this.lastOrderTime = lastOrderTime;
    }

    final public Customer getCustomer() {
        return customer;
    }

    final public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    final public Appraise getAppraise() {
        return appraise;
    }

    final public void setAppraise(Appraise appraise) {
        this.appraise = appraise;
    }

    final public List<String> getArticleNames() {
        return articleNames;
    }

    final public void setArticleNames(List<String> articleNames) {
        this.articleNames = articleNames;
    }

    final public String getShopName() {
        return shopName;
    }

    final public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    final public Boolean getTimeOut() {
        return timeOut;
    }

    final public void setTimeOut(Boolean timeOut) {
        this.timeOut = timeOut;
    }

    final public String getTelephone() {
        return telephone;
    }

    final public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    final public BigDecimal getPayValue() {
        return payValue;
    }

    final public void setPayValue(BigDecimal payValue) {
        this.payValue = payValue;
    }

    final public int getPaymentModeId() {
        return paymentModeId;
    }

    final public void setPaymentModeId(int paymentModeId) {
        this.paymentModeId = paymentModeId;
    }

    final public int getOrderCount() {
        return orderCount;
    }

    final public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }

    final public BigDecimal getOrderTotal() {
        return orderTotal == null ? new BigDecimal(0) : orderTotal;
    }

    final public void setOrderTotal(BigDecimal orderTotal) {
        this.orderTotal = orderTotal;
    }

    final public List<OrderPaymentItem> getOrderPaymentItems() {
        return orderPaymentItems;
    }

    final public void setOrderPaymentItems(List<OrderPaymentItem> orderPaymentItems) {
        this.orderPaymentItems = orderPaymentItems;
    }

    public boolean isUseAccount() {
        return useAccount;
    }

    public void setUseAccount(boolean useAccount) {
        this.useAccount = useAccount;
    }


}