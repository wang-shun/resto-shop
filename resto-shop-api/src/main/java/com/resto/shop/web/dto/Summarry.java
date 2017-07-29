package com.resto.shop.web.dto;

/**
 * 店铺 获取汇总数据
 * 本次给 鲁肉范 --- 田林路店用
 */
public class Summarry {


	private Integer customerOrder;//用户消费笔数

	private Integer newCustomerOrder;//新用户消费笔数

	private Integer shareCustomerOrder;//分享用户消费笔数

	private Integer backCustomerOrder;//回头用户消费笔数

	private String statisfaction;//折扣比率

	private Integer fiveStar ;//五星评论数

	private Integer fourStar ;//改进评论数

	private Integer oneToThree;//差评数

	private String shopName;//店铺名字


	public Integer getCustomerOrder() {
		return customerOrder;
	}

	public void setCustomerOrder(Integer customerOrder) {
		this.customerOrder = customerOrder;
	}

	public Integer getNewCustomerOrder() {
		return newCustomerOrder;
	}

	public void setNewCustomerOrder(Integer newCustomerOrder) {
		this.newCustomerOrder = newCustomerOrder;
	}

	public Integer getShareCustomerOrder() {
		return shareCustomerOrder;
	}

	public void setShareCustomerOrder(Integer shareCustomerOrder) {
		this.shareCustomerOrder = shareCustomerOrder;
	}

	public String getStatisfaction() {
		return statisfaction;
	}

	public void setStatisfaction(String statisfaction) {
		this.statisfaction = statisfaction;
	}

	public Integer getFiveStar() {
		return fiveStar;
	}

	public void setFiveStar(Integer fiveStar) {
		this.fiveStar = fiveStar;
	}

	public Integer getFourStar() {
		return fourStar;
	}

	public void setFourStar(Integer fourStar) {
		this.fourStar = fourStar;
	}

	public Integer getOneToThree() {
		return oneToThree;
	}

	public void setOneToThree(Integer oneToThree) {
		this.oneToThree = oneToThree;
	}

	public String getShopName() {
		return shopName;
	}

	public void setShopName(String shopName) {
		this.shopName = shopName;
	}

	public Integer getBackCustomerOrder() {
		return backCustomerOrder;
	}

	public void setBackCustomerOrder(Integer backCustomerOrder) {
		this.backCustomerOrder = backCustomerOrder;
	}
}
