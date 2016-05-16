package com.resto.shop.web.model;

import java.math.BigDecimal;
import java.util.Date;

public class SmsCharge {
	
	private String id;
	private String brandId;
	private Integer status;
	private Date createTime;
	private Date pushOrderTime;//确认时间
	private BigDecimal chargeMoney;//充值金额
	private String ticketId;//发票id
	private Integer num;//购买条数
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getBrandId() {
		return brandId;
	}
	public void setBrandId(String brandId) {
		this.brandId = brandId;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
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
	public BigDecimal getChargeMoney() {
		return chargeMoney;
	}
	public void setChargeMoney(BigDecimal chargeMoney) {
		this.chargeMoney = chargeMoney;
	}
	public String getTicketId() {
		return ticketId;
	}
	public void setTicketId(String ticketId) {
		this.ticketId = ticketId;
	}
	public Integer getNum() {
		return num;
	}
	public void setNum(Integer num) {
		this.num = num;
	}
	
	
	
}
