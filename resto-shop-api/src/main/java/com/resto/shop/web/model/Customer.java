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

    private Integer defaultDeliveryPoint;

    private Boolean isBindPhone;

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
}