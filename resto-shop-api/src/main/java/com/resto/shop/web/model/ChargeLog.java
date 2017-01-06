package com.resto.shop.web.model;

import java.math.BigDecimal;
import java.util.Date;

public class ChargeLog {

	private String id;

    private BigDecimal chargeMoney;
    
    private String operationPhone;
    
    private String customerPhone;
    
    private String shopDetailId;
    
    private String shopName;
    
    private Date createTime;


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public BigDecimal getChargeMoney() {
		return chargeMoney;
	}

	public void setChargeMoney(BigDecimal chargeMoney) {
		this.chargeMoney = chargeMoney;
	}

	public String getOperationPhone() {
		return operationPhone;
	}

	public void setOperationPhone(String operationPhone) {
		this.operationPhone = operationPhone;
	}

	public String getCustomerPhone() {
		return customerPhone;
	}

	public void setCustomerPhone(String customerPhone) {
		this.customerPhone = customerPhone;
	}

	public String getShopDetailId() {
		return shopDetailId;
	}

	public void setShopDetailId(String shopDetailId) {
		this.shopDetailId = shopDetailId;
	}

	public String getShopName() {
		return shopName;
	}

	public void setShopName(String shopName) {
		this.shopName = shopName;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public ChargeLog() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ChargeLog(String id, BigDecimal chargeMoney, String operationPhone, String customerPhone,
			String shopDetailId, String shopName, Date createTime) {
		super();
		this.id = id;
		this.chargeMoney = chargeMoney;
		this.operationPhone = operationPhone;
		this.customerPhone = customerPhone;
		this.shopDetailId = shopDetailId;
		this.shopName = shopName;
		this.createTime = createTime;
	}
    
    
}
