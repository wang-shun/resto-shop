package com.resto.shop.web.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class Customer implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String id;

    private String wechatId;

    private String nickname;

    private String telephone;

    private String headPhoto;
    
    private Date birthday;

    private Integer defaultDeliveryPoint;

    private Boolean isBindPhone;

    private Date createTime;

    private Date regiestTime;

    private Date firstOrderTime;

    private Date lastLoginTime;

    private String accountId;

    private String brandId;

    private Integer sex;

    private String province;

    private String city;

    private String country;
    
    private BigDecimal account;
    
    private String lastOrderShop;

    private String lastTableNumber;

    private Date newNoticeTime;
    
    private String shareCustomer;

    private Integer isNowRegister;
    
    /**
     * 附加属性用来接收账户的余额
     */
    private BigDecimal remain;

    private Integer isShare;

    private String registerShopId;

    /**
     * 个人信息详细
     */
    private CustomerDetail customerDetail;
    private String customerDetailId;

    private Date bindPhoneTime;

    private String bindPhoneShop;

    private String realTimeCouponIds;

    private String birthdayCouponIds;

    private String shareCouponIds;

    //是否关注
    private Integer subscribe;

    //会员序号
    private Long serialNumber;

    private String shareLink;

    private String orderId;

    private String shopDetailId;

    //表示该用户是否有订单
    private Boolean useOrder;

    private String cardId;

    private String code;

    public Boolean getUseOrder() {
        return useOrder;
    }

    public void setUseOrder(Boolean useOrder) {
        this.useOrder = useOrder;
    }

    //表示该用户是否有充值订单
    private Boolean chargeOrder;

    public Boolean getChargeOrder() {
        return chargeOrder;
    }

    public void setChargeOrder(Boolean chargeOrder) {
        this.chargeOrder = chargeOrder;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getShopDetailId() {
        return shopDetailId;
    }

    public void setShopDetailId(String shopDetailId) {
        this.shopDetailId = shopDetailId;
    }

    public String getShareLink() {
        return shareLink;
    }

    public void setShareLink(String shareLink) {
        this.shareLink = shareLink;
    }

    public Long getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(Long serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getShareCouponIds() {
        return shareCouponIds;
    }

    public void setShareCouponIds(String shareCouponIds) {
        this.shareCouponIds = shareCouponIds;
    }

    public Integer getSubscribe() {
        return subscribe;
    }

    public void setSubscribe(Integer subscribe) {
        this.subscribe = subscribe;
    }

    public String getBirthdayCouponIds() {
        return birthdayCouponIds;
    }

    public void setBirthdayCouponIds(String birthdayCouponIds) {
        this.birthdayCouponIds = birthdayCouponIds;
    }

    public String getRealTimeCouponIds() {
        return realTimeCouponIds;
    }

    public void setRealTimeCouponIds(String realTimeCouponIds) {
        this.realTimeCouponIds = realTimeCouponIds;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getIsShare() {
        return isShare;
    }

    public void setIsShare(Integer isShare) {
        this.isShare = isShare;
    }

    public BigDecimal getRemain() {
		return remain;
	}

	public void setRemain(BigDecimal remain) {
		this.remain = remain;
	}

	public void setIsNowRegister(Integer isNowRegister) {
        this.isNowRegister = isNowRegister;
    }

    public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public Integer getIsNowRegister() {
        return isNowRegister;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getWechatId() {
        return wechatId;
    }

    public void setWechatId(String wechatId) {
        this.wechatId = wechatId == null ? null : wechatId.trim();
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname == null ? null : nickname.trim();
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone == null ? null : telephone.trim();
    }

    public String getHeadPhoto() {
        return headPhoto;
    }

    public void setHeadPhoto(String headPhoto) {
        this.headPhoto = headPhoto == null ? null : headPhoto.trim();
    }

    public Integer getDefaultDeliveryPoint() {
        return defaultDeliveryPoint;
    }

    public void setDefaultDeliveryPoint(Integer defaultDeliveryPoint) {
        this.defaultDeliveryPoint = defaultDeliveryPoint;
    }

    public Boolean getIsBindPhone() {
        return isBindPhone;
    }

    public void setIsBindPhone(Boolean isBindPhone) {
        this.isBindPhone = isBindPhone;
    }

    public Date getRegiestTime() {
        return regiestTime;
    }

    public void setRegiestTime(Date regiestTime) {
        this.regiestTime = regiestTime;
    }

    public Date getFirstOrderTime() {
        return firstOrderTime;
    }

    public void setFirstOrderTime(Date firstOrderTime) {
        this.firstOrderTime = firstOrderTime;
    }

    public Date getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Date lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId == null ? null : accountId.trim();
    }

    public String getBrandId() {
        return brandId;
    }

    public void setBrandId(String brandId) {
        this.brandId = brandId == null ? null : brandId.trim();
    }

	public Integer getSex() {
		return sex;
	}

	public String getProvince() {
		return province;
	}

	public String getCity() {
		return city;
	}

	public String getCountry() {
		return country;
	}

	public BigDecimal getAccount() {
		return account;
	}

	public void setSex(Integer sex) {
		this.sex = sex;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public void setAccount(BigDecimal account) {
		this.account = account;
	}

	public String getLastOrderShop() {
		return lastOrderShop;
	}

	public void setLastOrderShop(String lastOrderShop) {
		this.lastOrderShop = lastOrderShop;
	}

	public Date getNewNoticeTime() {
		return newNoticeTime;
	}

	public void setNewNoticeTime(Date newNoticeTime) {
		this.newNoticeTime = newNoticeTime;
	}

	public String getShareCustomer() {
		return shareCustomer;
	}

	public void setShareCustomer(String shareCustomer) {
		this.shareCustomer = shareCustomer;
	}

    public CustomerDetail getCustomerDetail() {
        return customerDetail;
    }

    public void setCustomerDetail(CustomerDetail customerDetail) {
        this.customerDetail = customerDetail;
    }

    public String getCustomerDetailId() {
        return customerDetailId;
    }

    public void setCustomerDetailId(String customerDetailId) {
        this.customerDetailId = customerDetailId;
    }

    public String getRegisterShopId() {
        return registerShopId;
    }

    public void setRegisterShopId(String registerShopId) {
        this.registerShopId = registerShopId;
    }

    public Date getBindPhoneTime() {
        return bindPhoneTime;
    }

    public void setBindPhoneTime(Date bindPhoneTime) {
        this.bindPhoneTime = bindPhoneTime;
    }

    public String getBindPhoneShop() {
        return bindPhoneShop;
    }

    public void setBindPhoneShop(String bindPhoneShop) {
        this.bindPhoneShop = bindPhoneShop;
    }

    public String getLastTableNumber() {
        return lastTableNumber;
    }

    public void setLastTableNumber(String lastTableNumber) {
        this.lastTableNumber = lastTableNumber;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Customer{");
        sb.append("id='").append(id).append('\'');
        sb.append(", wechatId='").append(wechatId).append('\'');
        sb.append(", nickname='").append(nickname).append('\'');
        sb.append(", telephone='").append(telephone).append('\'');
        sb.append(", headPhoto='").append(headPhoto).append('\'');
        sb.append(", defaultDeliveryPoint=").append(defaultDeliveryPoint);
        sb.append(", isBindPhone=").append(isBindPhone);
        sb.append(", regiestTime=").append(regiestTime);
        sb.append(", firstOrderTime=").append(firstOrderTime);
        sb.append(", lastLoginTime=").append(lastLoginTime);
        sb.append(", accountId='").append(accountId).append('\'');
        sb.append(", brandId='").append(brandId).append('\'');
        sb.append(", sex=").append(sex);
        sb.append(", province='").append(province).append('\'');
        sb.append(", city='").append(city).append('\'');
        sb.append(", country='").append(country).append('\'');
        sb.append(", account=").append(account);
        sb.append(", lastOrderShop='").append(lastOrderShop).append('\'');
        sb.append(", newNoticeTime=").append(newNoticeTime);
        sb.append(", shareCustomer='").append(shareCustomer).append('\'');
        sb.append('}');
        return sb.toString();
    }
}