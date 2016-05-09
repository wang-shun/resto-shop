package com.resto.shop.web.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.ApplicationUtils;
import com.resto.shop.web.dao.CustomerMapper;
import com.resto.shop.web.exception.AppException;
import com.resto.shop.web.model.Account;
import com.resto.shop.web.model.Customer;
import com.resto.shop.web.service.AccountService;
import com.resto.shop.web.service.CustomerService;
import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class CustomerServiceImpl extends GenericServiceImpl<Customer, String> implements CustomerService {

    @Resource
    private CustomerMapper customerMapper;
    @Resource
    private AccountService accountService;

    @Override
    public GenericDao<Customer, String> getDao() {
        return customerMapper;
    }

	@Override
	public Customer login(String openid) {
		Customer customer = selectByOpenId(openid);
		if(customer!=null){
			Customer change = new Customer();
			change.setId(customer.getId());
			change.setLastLoginTime(new Date());
			update(change);
		}
		return customer;
	}
	
	private Customer selectByOpenId(String openid) {
		Customer cus = customerMapper.selectByOpenId(openid);
		if(cus!=null){
			Account account = accountService.selectById(cus.getAccountId());
			cus.setAccount(account.getRemain());
		}
		return cus;
	}
	
	@Override
	public Customer selectById(String id) {
		Customer cus = customerMapper.selectByPrimaryKey(id);
		Account account = accountService.selectById(cus.getAccountId());
		if(account==null){
			account = accountService.createCustomerAccount(cus);
		}
		cus.setAccount(account.getRemain());
		return cus;
	}
	
	@Override
	public Customer register(Customer customer) {
		String customerId = ApplicationUtils.randomUUID();
		customer.setId(customerId);
		Account account = new Account();
		account.setId(ApplicationUtils.randomUUID());
		account.setRemain(BigDecimal.ZERO);
		accountService.insert(account);
		customer.setAccountId(account.getId());
		customer.setIsBindPhone(false);
		customer.setLastLoginTime(new Date());
		customer.setRegiestTime(new Date());
		customer.setAccount(account.getRemain());
		insert(customer);
		return customer;
	}

	@Override
	public void bindPhone(String phone, String currentCustomerId) throws AppException {
		Customer customer = customerMapper.selectByPhone(phone);
		if(customer!=null){
			throw new AppException(AppException.PHONE_IS_BIND);
		}
		customer = new Customer();
		customer.setIsBindPhone(true);
		customer.setTelephone(phone);
		customer.setId(currentCustomerId);
		update(customer);
	}

	@Override
	public Customer selectNickNameAndTelephone(String customerId) {
		return customerMapper.selectNickNameAndTelephone(customerId);
	}

	@Override
	public List<Customer> selectListByBrandId(String currentBrandId) {
		return customerMapper.selectListByBrandId(currentBrandId);
	}

	@Override
	public void changeLastOrderShop(String shopDetailId, String customerId) {
		customerMapper.changeLastOrderShop(shopDetailId,customerId);
	} 
	

}
