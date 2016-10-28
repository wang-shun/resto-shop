package com.resto.shop.web.service;

import java.math.BigDecimal;
import java.util.List;

import com.resto.brand.core.generic.GenericService;
import com.resto.brand.web.model.ShareSetting;
import com.resto.shop.web.exception.AppException;
import com.resto.shop.web.model.Customer;
import com.resto.shop.web.model.Order;

public interface CustomerService extends GenericService<Customer, String> {

	Customer login(String openid);

	Customer register(Customer customer);
    void updateCustomer(Customer customer);

	void bindPhone(String phone, String currentCustomerId,Integer couponType,String shopId) throws AppException;
    
	/**
	 * 根据ID才查询用户昵称和手机号码
	 * @param customerId
	 * @return
	 */
	Customer selectNickNameAndTelephone(String customerId);

	List<Customer> selectListByBrandId(String currentBrandId);

	void changeLastOrderShop(String shopDetailId, String customerId);
	
	/**
	 * 解绑手机号码
	 * @param currentCustomerId
	 */
	void unbindphone(String currentCustomerId);

	void updateNewNoticeTime(String id);

	void updateFirstOrderTime(String id);

	BigDecimal rewareShareCustomer(ShareSetting shareSetting, Order order, Customer shareCustomer, Customer customer);

	Boolean checkRegistered(String id);
}
