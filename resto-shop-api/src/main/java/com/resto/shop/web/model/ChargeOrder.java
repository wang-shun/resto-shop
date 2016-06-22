package com.resto.shop.web.model;

import java.math.BigDecimal;
import java.util.Date;

public class ChargeOrder {
    private String id;

    private BigDecimal chargeMoney;

    private BigDecimal rewardMoney;

    private Byte orderState;

    private Date createTime;

    private Date finishTime;

    private String customerId;

    private String shopDetailId;
    
    private String brandId;
    
    private BigDecimal chargeBalance;

    private BigDecimal rewardBalance;

    private BigDecimal totalBalance;
    
	public ChargeOrder(String id, BigDecimal chargeMoney, BigDecimal rewardMoney, Byte orderState, Date createTime,
			String customerId, String shopDetailId, String brandId) {
		super();
		this.id = id;
		this.chargeMoney = chargeMoney;
		this.rewardMoney = rewardMoney;
		this.orderState = orderState;
		this.createTime = createTime;
		this.customerId = customerId;
		this.shopDetailId = shopDetailId;
		this.brandId = brandId;
	}



	public ChargeOrder() {
		super();
	}



	public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public BigDecimal getChargeMoney() {
        return chargeMoney;
    }

    public void setChargeMoney(BigDecimal chargeMoney) {
        this.chargeMoney = chargeMoney;
    }

    public BigDecimal getRewardMoney() {
        return rewardMoney;
    }

    public void setRewardMoney(BigDecimal rewardMoney) {
        this.rewardMoney = rewardMoney;
    }

    public Byte getOrderState() {
        return orderState;
    }

    public void setOrderState(Byte orderState) {
        this.orderState = orderState;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId == null ? null : customerId.trim();
    }

    public String getShopDetailId() {
        return shopDetailId;
    }

    public void setShopDetailId(String shopDetailId) {
        this.shopDetailId = shopDetailId == null ? null : shopDetailId.trim();
    }

	public String getBrandId() {
		return brandId;
	}

	public void setBrandId(String brandId) {
		this.brandId = brandId;
	}



	public BigDecimal getChargeBalance() {
		return chargeBalance;
	}



	public BigDecimal getRewardBalance() {
		return rewardBalance;
	}



	public BigDecimal getTotalBalance() {
		return totalBalance;
	}



	public void setChargeBalance(BigDecimal chargeBalance) {
		this.chargeBalance = chargeBalance;
	}



	public void setRewardBalance(BigDecimal rewardBalance) {
		this.rewardBalance = rewardBalance;
	}



	public void setTotalBalance(BigDecimal totalBalance) {
		this.totalBalance = totalBalance;
	}
}