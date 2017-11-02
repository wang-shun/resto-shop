package com.resto.shop.web.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.resto.brand.web.model.RefundRemark;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Order implements Serializable{

    private Integer tag;//不去重

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
    
    private BigDecimal aliPayDiscountMoney;

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

    private String customerAddressId;

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

    private String telephone;

    private BigDecimal payValue;

    private int paymentModeId;

    private int orderCount;

    private BigDecimal orderTotal;

    //等位红包
    private BigDecimal waitMoney;

    private String waitId;

    private Integer isShare;

    private  String shareCustomer;

    //该订单是否使用优惠券
    private Boolean ifUseCoupon;

    //该订单的优惠券信息
    private Coupon couponInfo;

    private  List<Order> childList; //子订单

    //新增微信支付单号
    public OrderPaymentItem orderPaymentItem;

    //菜品总数量（包含加菜）
    private Integer totalCount;

    //加菜次数
    private int childCount;

    //订单原始金额（退菜 编辑菜 前）
    private BigDecimal baseMoney;

    private BigDecimal baseOrderMoney;

    //支付状态（0 未支付 1支付中 2已支付）
    private Integer isPay;

    //子订单的菜品项 ，key为子订单id
    private Map<String,List<OrderItem>> childItems;

    private BigDecimal refundMoney;

    //订单原始人数（退菜前）
    private Integer baseCustomerCount;

    //是否需要扫码 0不需要 1需要
    private Integer needScan;

    private Integer payType;

    //是否是退菜订单 0 不是 1是
    private Integer isRefund;

    //是否确认 0-未确认 1-已确认
    private Integer isConfirm;

    //订单及他所有加菜订单的原价总和
    private BigDecimal allOrderOriginalAmount;

    //加菜位置  pos为pos端点菜   wechat为wehcat端点菜
    private String createOrderByAddress;

    //是否退菜光 包括自订单   默认否
    private Boolean isRefundOrder;

    private Integer isGetShareCoupon;

    //找零
    private BigDecimal giveChange;

    private Integer isPosPay;

    //0-未打印 1-打印异常 2-异常修正 3打印正常
    private Integer printFailFlag;

    //0-未打印 1-打印异常 2-异常修正 3打印正常
    private Integer printKitchenFlag;

    private Integer failPrintCount;

    /**
     * 订单支付项
     */
    private List<OrderPaymentItem> orderPaymentItems;

    private BigDecimal servicePrice;

    private BigDecimal mealFeePrice;

    private Integer mealAllNumber;

    /**
     * 原始餐盒总数（退菜前）
     */
    private Integer baseMealAllCount;

    /**
     * 退菜原因
     */
    private RefundRemark refundRemark;

    /**
     * 退菜原因补充
     */
    private String remarkSupply;

    private Integer type;

    private BigDecimal posDiscount;

    private BigDecimal eraseMoney;

    private BigDecimal noDiscountMoney;

    private Integer refundType;

    private Integer isConsumptionRebate;    //是否参与消费返利

    private Integer orderBefore;

    public Integer getOrderBefore() {
        return orderBefore;
    }

    public void setOrderBefore(Integer orderBefore) {
        this.orderBefore = orderBefore;
    }

    public Integer getIsConsumptionRebate() {
        return isConsumptionRebate;
    }

    public void setIsConsumptionRebate(Integer isConsumptionRebate) {
        this.isConsumptionRebate = isConsumptionRebate;
    }

    public Integer getRefundType() {
        return refundType;
    }

    public void setRefundType(Integer refundType) {
        this.refundType = refundType;
    }

    private String groupId;

    public Integer getTag() {
        return tag;
    }

    public void setTag(Integer tag) {
        this.tag = tag;
    }

    public Integer getFailPrintCount() {
        return failPrintCount;
    }

    public void setFailPrintCount(Integer failPrintCount) {
        this.failPrintCount = failPrintCount;
    }

    public Integer getPrintKitchenFlag() {
        return printKitchenFlag;
    }

    public void setPrintKitchenFlag(Integer printKitchenFlag) {
        this.printKitchenFlag = printKitchenFlag;
    }

    public Integer getPrintFailFlag() {
        return printFailFlag;
    }

    public void setPrintFailFlag(Integer printFailFlag) {
        this.printFailFlag = printFailFlag;
    }

    public Integer getIsPosPay() {
        return isPosPay;
    }

    public void setIsPosPay(Integer isPosPay) {
        this.isPosPay = isPosPay;
    }

    public BigDecimal getGiveChange() {
        return giveChange;
    }

    public void setGiveChange(BigDecimal giveChange) {
        this.giveChange = giveChange;
    }

    public Integer getIsGetShareCoupon() {
        return isGetShareCoupon;
    }

    public void setIsGetShareCoupon(Integer isGetShareCoupon) {
        this.isGetShareCoupon = isGetShareCoupon;
    }

    public Boolean getIsRefundOrder() {
        return isRefundOrder;
    }

    public void setIsRefundOrder(Boolean isRefundOrder) {
        this.isRefundOrder = isRefundOrder;
    }

    public String getCreateOrderByAddress() {
        return createOrderByAddress;
    }

    public void setCreateOrderByAddress(String createOrderByAddress) {
        this.createOrderByAddress = createOrderByAddress;
    }

    public BigDecimal getAllOrderOriginalAmount() {
        return allOrderOriginalAmount;
    }

    public void setAllOrderOriginalAmount(BigDecimal allOrderOriginalAmount) {
        this.allOrderOriginalAmount = allOrderOriginalAmount;
    }

    final public Integer getIsConfirm() {
        return isConfirm;
    }

    final public void setIsConfirm(Integer isConfirm) {
        this.isConfirm = isConfirm;
    }

    final public Integer getIsRefund() {
        return isRefund;
    }

    final public void setIsRefund(Integer isRefund) {
        this.isRefund = isRefund;
    }

    public Integer getPayType() {
        return payType;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }

    public List<Order> getChildList() {
        return childList;
    }

    public void setChildList(List<Order> childList) {
        this.childList = childList;
    }

    public Integer getIsPay() {
        return isPay;
    }

    public void setIsPay(Integer isPay) {
        this.isPay = isPay;
    }

    public Integer getNeedScan() {
        return needScan;
    }

    public void setNeedScan(Integer needScan) {
        this.needScan = needScan;
    }
    
    public Integer getBaseMealAllCount() {
		return baseMealAllCount;
	}

	public void setBaseMealAllCount(Integer baseMealAllCount) {
		this.baseMealAllCount = baseMealAllCount;
	}

	public Integer getBaseCustomerCount() {
        return baseCustomerCount;
    }

    public void setBaseCustomerCount(Integer baseCustomerCount) {
        this.baseCustomerCount = baseCustomerCount;
    }

    public BigDecimal getBaseMoney() {
        return baseMoney;
    }

    public void setBaseMoney(BigDecimal baseMoney) {
        this.baseMoney = baseMoney;
    }

    public BigDecimal getRefundMoney() {
        return refundMoney;
    }

    public void setRefundMoney(BigDecimal refundMoney) {
        this.refundMoney = refundMoney;
    }

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

    public Map<String, List<OrderItem>> getChildItems() {
        return childItems;
    }

    public void setChildItems(Map<String, List<OrderItem>> childItems) {
        this.childItems = childItems;
    }

    public int getChildCount() {
        return childCount;
    }

    public void setChildCount(int childCount) {
        this.childCount = childCount;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public OrderPaymentItem getOrderPaymentItem() {
        return orderPaymentItem;
    }

    public void setOrderPaymentItem(OrderPaymentItem orderPaymentItem) {
        this.orderPaymentItem = orderPaymentItem;
    }

    public Integer getIsShare() {
        return isShare;
    }

    public void setIsShare(Integer isShare) {
        this.isShare = isShare;
    }

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

    final public String getCustomerAddressId() {
        return customerAddressId;
    }

    final public void setCustomerAddressId(String customerAddressId) {
        this.customerAddressId = customerAddressId;
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

	public BigDecimal getAliPayDiscountMoney() {
		return aliPayDiscountMoney;
	}

	public void setAliPayDiscountMoney(BigDecimal aliPayDiscountMoney) {
		this.aliPayDiscountMoney = aliPayDiscountMoney;
	}

    public Boolean getIfUseCoupon() {
        return ifUseCoupon;
    }

    public void setIfUseCoupon(Boolean ifUseCoupon) {
        this.ifUseCoupon = ifUseCoupon;
    }

    public Coupon getCouponInfo() {
        return couponInfo;
    }

    public void setCouponInfo(Coupon couponInfo) {
        this.couponInfo = couponInfo;
    }

    public String getShareCustomer() {
        return shareCustomer;
    }

    public void setShareCustomer(String shareCustomer) {
        this.shareCustomer = shareCustomer;
    }

    public RefundRemark getRefundRemark() {
        return refundRemark;
    }

    public void setRefundRemark(RefundRemark refundRemark) {
        this.refundRemark = refundRemark;
    }

    public String getRemarkSupply() {
        return remarkSupply;
    }

    public void setRemarkSupply(String remarkSupply) {
        this.remarkSupply = remarkSupply;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public BigDecimal getPosDiscount() {
        return posDiscount;
    }

    public void setPosDiscount(BigDecimal posDiscount) {
        this.posDiscount = posDiscount;
    }

    public BigDecimal getEraseMoney() {
        return eraseMoney;
    }

    public void setEraseMoney(BigDecimal eraseMoney) {
        this.eraseMoney = eraseMoney;
    }

    public BigDecimal getNoDiscountMoney() {
        return noDiscountMoney;
    }

    public void setNoDiscountMoney(BigDecimal noDiscountMoney) {
        this.noDiscountMoney = noDiscountMoney;
    }

    public BigDecimal getBaseOrderMoney() {
        return baseOrderMoney;
    }

    public void setBaseOrderMoney(BigDecimal baseOrderMoney) {
        this.baseOrderMoney = baseOrderMoney;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("tag", tag)
                .append("id", id)
                .append("tableNumber", tableNumber)
                .append("customerCount", customerCount)
                .append("accountingTime", accountingTime)
                .append("orderState", orderState)
                .append("productionStatus", productionStatus)
                .append("originalAmount", originalAmount)
                .append("reductionAmount", reductionAmount)
                .append("paymentAmount", paymentAmount)
                .append("orderMoney", orderMoney)
                .append("aliPayDiscountMoney", aliPayDiscountMoney)
                .append("articleCount", articleCount)
                .append("serialNumber", serialNumber)
                .append("confirmTime", confirmTime)
                .append("printTimes", printTimes)
                .append("allowCancel", allowCancel)
                .append("allowAppraise", allowAppraise)
                .append("closed", closed)
                .append("remark", remark)
                .append("createTime", createTime)
                .append("operatorId", operatorId)
                .append("customerId", customerId)
                .append("distributionDate", distributionDate)
                .append("distributionTimeId", distributionTimeId)
                .append("deliveryPointId", deliveryPointId)
                .append("shopDetailId", shopDetailId)
                .append("distributionModeId", distributionModeId)
                .append("orderItems", orderItems)
                .append("verCode", verCode)
                .append("useAccount", useAccount)
                .append("useCoupon", useCoupon)
                .append("pushOrderTime", pushOrderTime)
                .append("printOrderTime", printOrderTime)
                .append("callNumberTime", callNumberTime)
                .append("orderMode", orderMode)
                .append("brandId", brandId)
                .append("amountWithChildren", amountWithChildren)
                .append("parentOrderId", parentOrderId)
                .append("allowContinueOrder", allowContinueOrder)
                .append("countWithChild", countWithChild)
                .append("lastOrderTime", lastOrderTime)
                .append("customer", customer)
                .append("appraise", appraise)
                .append("personCount", personCount)
                .append("payMode", payMode)
                .append("employeeId", employeeId)
                .append("articleNames", articleNames)
                .append("shopName", shopName)
                .append("timeOut", timeOut)
                .append("telephone", telephone)
                .append("payValue", payValue)
                .append("paymentModeId", paymentModeId)
                .append("orderCount", orderCount)
                .append("orderTotal", orderTotal)
                .append("waitMoney", waitMoney)
                .append("waitId", waitId)
                .append("isShare", isShare)
                .append("shareCustomer", shareCustomer)
                .append("ifUseCoupon", ifUseCoupon)
                .append("couponInfo", couponInfo)
                .append("childList", childList)
                .append("orderPaymentItem", orderPaymentItem)
                .append("totalCount", totalCount)
                .append("childCount", childCount)
                .append("baseMoney", baseMoney)
                .append("isPay", isPay)
                .append("childItems", childItems)
                .append("refundMoney", refundMoney)
                .append("baseCustomerCount", baseCustomerCount)
                .append("needScan", needScan)
                .append("payType", payType)
                .append("isRefund", isRefund)
                .append("isConfirm", isConfirm)
                .append("allOrderOriginalAmount", allOrderOriginalAmount)
                .append("createOrderByAddress", createOrderByAddress)
                .append("isRefundOrder", isRefundOrder)
                .append("baseMealAllCount", baseMealAllCount)
                .append("mealFeePrice", mealFeePrice)
                .append("mealAllNumber", mealAllNumber)
                .append("orderPaymentItems", orderPaymentItems)
                .append("servicePrice", servicePrice)
                .toString();
    }

    private BigDecimal discountMoney;

    public BigDecimal getDiscountMoney() {
        return discountMoney;
    }

    public void setDiscountMoney(BigDecimal discountMoney) {
        this.discountMoney = discountMoney;
    }
}