package com.resto.shop.web.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.Customer;

public interface CustomerMapper extends GenericDao<Customer, String>{
    int deleteByPrimaryKey(String id);

    int insert(Customer record);

    int insertSelective(Customer record);

    Customer selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(Customer record);

    int updateByPrimaryKey(Customer record);

	Customer selectByOpenId(String openid);

	void bindAccount(String accountId, String customerId);
	
	/**
	 * 根据ID才查询用户昵称和手机号码
	 * @param customerId
	 * @return
	 */
	Customer selectNickNameAndTelephone(@Param("customerId")String customerId);

	List<Customer> selectListByBrandId(String brandId);

	Customer selectByPhone(String phone);

	void changeLastOrderShop(String shopDetailId, String customerId);
	
	void updateNewNoticeTime(String customerId);

	void updateFirstOrderTime(String id);

	Integer checkRegistered(String id);
}