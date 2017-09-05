package com.resto.shop.web.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.resto.brand.core.generic.GenericService;
import com.resto.brand.web.dto.MemberUserDto;
import com.resto.brand.web.model.ShareSetting;
import com.resto.shop.web.exception.AppException;
import com.resto.shop.web.model.Customer;
import com.resto.shop.web.model.Order;

public interface CustomerService extends GenericService<Customer, String> {

	Customer login(String openid);

	Customer getCustomerLimitOne();

	Customer register(Customer customer);

	Customer registerCard(Customer customer);

    void updateCustomer(Customer customer);

	Customer bindPhone(String phone, String currentCustomerId,Integer couponType,String shopId,String shareCustomer) throws AppException;
    
	/**
	 * 根据ID才查询用户昵称和手机号码
	 * @param customerId
	 * @return
	 */
	Customer selectNickNameAndTelephone(String customerId);

	List<Customer> selectListByBrandId(String currentBrandId);

	List<String> selectTelephoneList();


	void changeLastOrderShop(String shopDetailId, String customerId);
	
	/**
	 * 解绑手机号码
	 * @param currentCustomerId
	 */
	void unbindphone(String currentCustomerId);

	void updateNewNoticeTime(String id);

	void updateFirstOrderTime(String id);

	BigDecimal rewareShareCustomer(ShareSetting shareSetting, Order order, Customer shareCustomer, Customer customer);

	BigDecimal rewareShareCustomerAgain(ShareSetting shareSetting, Order order, Customer shareCustomer, Customer customer);

	Boolean checkRegistered(String id);

	Customer selectByOpenIdInfo(String openId);

    /**
     * 查询时间段内的注册用户
     * @param beginDate
     * @param endDate
     * @return
     */
    List<Customer> selectListByBrandIdHasRegister(String beginDate, String endDate,String brandId);
    
    
    /**
     * 得到某个时间段店铺的会员信息和订单情况
     * @param beginDate
     * @param endDate
     * @return
     */
    List<MemberUserDto> callListMemberUser(String beginDate,String endDate);
    /**
     * 查询某个时间段的店铺会员信息
     * @param beginDate
     * @param endDate
     * @param brandId
     * @return
     */
	Map<String,Object> selectListMember(String beginDate, String endDate,String brandId);
    
    Customer selectCustomerAccount(String telephone);
    
    //得到品牌用户信息
    String selectBrandUser();

	Integer selectByShareCustomer(String customerId);

    List<Customer> selectBirthUser();

    /**
     * 通过电话号码查询用户信息
     * @param s
     * @return
     */
    Customer selectByTelePhone(String s);

	Customer selectBySerialNumber(String number);


    List<Customer> selectByTelePhones(List<String> telePhones);

    List<Customer> getCommentCustomer(String startTime,Integer time,Integer type);

	List<Customer> selectShareCustomerList(String customerId, Integer currentPage, Integer showCount);

	List<Customer> selectBySelectMap(Map<String, Object> selectMap);

	 int updateCustomerWechatId(Customer customer);
}
