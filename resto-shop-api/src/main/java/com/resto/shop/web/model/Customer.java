package com.resto.shop.web.model;

import java.util.Date;

public class Customer {
    private String id;

    private String wechatId;

    private String nickname;

    private String telephone;

    private String headPhoto;

    private Integer defaultDeliveryPoint;

    private Integer isBindPhone;

    private Date regiestTime;

    private Date firstOrderTime;

    private Date lastLoginTime;

    private String accountId;

    private String brandId;

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

    public Integer getIsBindPhone() {
        return isBindPhone;
    }

    public void setIsBindPhone(Integer isBindPhone) {
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
}