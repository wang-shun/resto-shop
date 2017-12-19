package com.resto.shop.web.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.ApplicationUtils;
import com.resto.brand.core.util.DateUtil;
import com.resto.brand.web.dto.MemberUserDto;
import com.resto.brand.web.model.ShareSetting;
import com.resto.shop.web.constant.AccountLogType;
import com.resto.shop.web.constant.RedType;
import com.resto.shop.web.dao.CustomerMapper;
import com.resto.shop.web.exception.AppException;
import com.resto.shop.web.model.*;
import com.resto.shop.web.service.*;

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
    @Resource
    private RedPacketService redPacketService;

	@Resource
	ThirdCustomerService thirdCustomerService;

	@Resource
	AccountLogService accountLogService;

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
			cus.setAccount(account == null ? new BigDecimal(0) : account.getRemain());
		}
		return cus;
	}
	
	@Override
	public Customer selectById(String id) {
		Customer cus = customerMapper.selectByPrimaryKey(id);
		if(cus==null){
			return null;
		}
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
		Customer cus = customerMapper.selectByOpenId(customer.getWechatId());
		if(cus != null){
			return cus;
		}
		customer.setId(customerId);
		Account account = new Account();
		account.setId(ApplicationUtils.randomUUID());
		account.setRemain(BigDecimal.ZERO);
		accountService.insert(account);
		customer.setAccountId(account.getId());
		customer.setIsBindPhone(false);
		customer.setLastLoginTime(new Date());
		customer.setRegiestTime(new Date());
        customer.setCreateTime(new Date());
		customer.setAccount(account.getRemain());
		insert(customer);
		return customer;
	}

	@Override
	public Customer registerCard(Customer customer) {
		String customerId = ApplicationUtils.randomUUID();
		Customer cus = customerMapper.selectByOpenId(customer.getWechatId());
		if(cus != null){
			return cus;
		}
		customer.setId(customerId);
		Account account = new Account();
		account.setId(ApplicationUtils.randomUUID());
		account.setRemain(BigDecimal.ZERO);
		accountService.insert(account);
		customer.setAccountId(account.getId());
		customer.setLastLoginTime(new Date());
		customer.setRegiestTime(new Date());
		customer.setCreateTime(new Date());
		customer.setAccount(account.getRemain());
		insert(customer);
		return customer;
	}

	@Override
	public void updateCustomer(Customer customer) {
		update(customer);
	}

	@Override
	public Customer bindPhone(String phone, String currentCustomerId,Integer couponType,String shopId,String shareCustomer,String shareOrderId) throws AppException {
		Customer customer = customerMapper.selectByPhone(phone);
		if(customer!=null){
			throw new AppException(AppException.PHONE_IS_BIND);
		}
		customer = new Customer();
		customer.setIsBindPhone(true);
		customer.setTelephone(phone);
		customer.setId(currentCustomerId);
		customer.setBindPhoneTime(new Date());
		customer.setBindPhoneShop(shopId);
		if(!currentCustomerId.equals(shareCustomer)){
			customer.setShareCustomer(shareCustomer);
			customer.setShareLink("clearShareLink");
		}
//		customer.setRegisterShopId(shopId);
//		update(customer);
		customerMapper.registerCustomer(customer);

		//判断该用户是否在第三方储值有余额
		ThirdCustomer thirdCustomer = thirdCustomerService.selectByTelephone(customer.getTelephone());
		if(thirdCustomer != null){
			customer = customerMapper.selectByPrimaryKey(customer.getId());
			//插入tb_red_packet
			RedPacket redPacket = new RedPacket();
			redPacket.setId(ApplicationUtils.randomUUID());
			redPacket.setRedMoney(thirdCustomer.getMoney());
			redPacket.setCreateTime(new Date());
			redPacket.setCustomerId(customer.getId());
			redPacket.setBrandId(customer.getBrandId());
			redPacket.setShopDetailId(customer.getBindPhoneShop());
			redPacket.setRedRemainderMoney(thirdCustomer.getMoney());
			redPacket.setRedType(RedType.THIRD_MONEY);
			redPacket.setOrderId(null);
			redPacketService.insert(redPacket);
			//修改余额
			Account account = accountService.selectById(customer.getAccountId());
			account.setRemain(account.getRemain().add(thirdCustomer.getMoney()));
			accountService.update(account);
			//修改tb_third_customer表
			thirdCustomer.setType(0);
			thirdCustomer.setMoney(new BigDecimal(0));
			thirdCustomerService.update(thirdCustomer);
			//添加余额日志表
			AccountLog acclog = new AccountLog();
			acclog.setCreateTime(new Date());
			acclog.setId(ApplicationUtils.randomUUID());
			acclog.setMoney(thirdCustomer.getMoney());
			acclog.setRemain(account.getRemain());
			acclog.setPaymentType(AccountLogType.INCOME);
			acclog.setRemark("第三方储值余额");
			acclog.setAccountId(account.getId());
			acclog.setSource(AccountLog.THIRD_MONEY);
			acclog.setShopDetailId(customer.getBindPhoneShop());
			accountLogService.insert(acclog);
		}
		return customer;
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
	public List<String> selectTelephoneList() {
		return customerMapper.selectCustomerList();
	}

	@Override
	public void changeLastOrderShop(String shopDetailId, String customerId) {
		customerMapper.changeLastOrderShop(shopDetailId,customerId);
	}

	@Override
	public void unbindphone(String currentCustomerId) {
		Customer customer = customerMapper.selectByPrimaryKey(currentCustomerId);
		customer.setTelephone(null);
		customerMapper.updateByPrimaryKeySelective(customer);
	} 
	
	@Override
	public void updateNewNoticeTime(String id){
		customerMapper.updateNewNoticeTime(id);
	}

	@Override
	public void updateFirstOrderTime(String id) {
		customerMapper.updateFirstOrderTime(id);
	}

	@Override
	public BigDecimal rewareShareCustomer(ShareSetting shareSetting, Order order, Customer shareCustomer, Customer customer) {
		BigDecimal rebate = shareSetting.getRebate();
		BigDecimal money = order.getOrderMoney();
		BigDecimal rewardMoney = money.multiply(rebate).divide(new BigDecimal(100)).setScale(BigDecimal.ROUND_HALF_DOWN, 2);
		if(rewardMoney.compareTo(shareSetting.getMinMoney())<0){
			rewardMoney = shareSetting.getMinMoney();
		}else if(rewardMoney.compareTo(shareSetting.getMaxMoney())>0){
			rewardMoney = shareSetting.getMaxMoney();
		}
		accountService.addAccount(rewardMoney, shareCustomer.getAccountId(), "分享奖励", AccountLog.SOURCE_SHARE_REWARD,customer.getBindPhoneShop());
        RedPacket redPacket = new RedPacket();
        redPacket.setId(ApplicationUtils.randomUUID());
        redPacket.setRedMoney(rewardMoney);
        redPacket.setCreateTime(new Date());
        redPacket.setCustomerId(shareCustomer.getId());
        redPacket.setBrandId(customer.getBrandId());
        redPacket.setShopDetailId(customer.getBindPhoneShop());
        redPacket.setRedRemainderMoney(rewardMoney);
        redPacket.setRedType(RedType.SHARE_RED);
		redPacket.setOrderId(order.getId());
        redPacketService.insert(redPacket);
		log.info("分享奖励用户:"+rewardMoney+" 元"+"  分享者:"+shareCustomer.getId());
		return rewardMoney;
	}

	@Override
	public BigDecimal rewareShareCustomerAgain(ShareSetting shareSetting, Order order, Customer shareCustomer, Customer customer) {
		BigDecimal rebate = shareSetting.getAfterRebate();
		BigDecimal money = order.getOrderMoney();
		BigDecimal rewardMoney = money.multiply(rebate).divide(new BigDecimal(100)).setScale(BigDecimal.ROUND_HALF_DOWN, 2);
		if(rewardMoney.compareTo(shareSetting.getAfterMinMoney())<0){
			rewardMoney = shareSetting.getAfterMinMoney();
		}else if(rewardMoney.compareTo(shareSetting.getAfterMaxMoney())>0){
			rewardMoney = shareSetting.getAfterMaxMoney();
		}
		accountService.addAccount(rewardMoney, shareCustomer.getAccountId(), "分享奖励", AccountLog.SOURCE_SHARE_REWARD,customer.getBindPhoneShop());
		RedPacket redPacket = new RedPacket();
		redPacket.setId(ApplicationUtils.randomUUID());
		redPacket.setRedMoney(rewardMoney);
		redPacket.setCreateTime(new Date());
		redPacket.setCustomerId(shareCustomer.getId());
		redPacket.setBrandId(customer.getBrandId());
		redPacket.setShopDetailId(customer.getBindPhoneShop());
		redPacket.setRedRemainderMoney(rewardMoney);
		redPacket.setRedType(RedType.SHARE_RED);
		redPacket.setOrderId(order.getId());
		redPacketService.insert(redPacket);
		log.info("分享奖励用户:"+rewardMoney+" 元"+"  分享者:"+shareCustomer.getId());
		return rewardMoney;
	}

	@Override
	public Boolean checkRegistered(String id) {
		return customerMapper.checkRegistered(id) > 0;
	}

	@Override
	public Customer selectByOpenIdInfo(String openId) {
		return customerMapper.selectByOpenId(openId);
	}

    @Override
    public List<Customer> selectListByBrandIdHasRegister(String beginDate, String endDate,String brandId) {
        Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);
        return  customerMapper.selectListByBrandIdHasRegister(begin,end,brandId);
    }
    
    @Override
    public Customer selectCustomerAccount(String telephone) {
    	return customerMapper.selectCustomerAccount(telephone);
    }

	@Override
	public Map<String, Object> selectListMember(String beginDate, String endDate, String brandId) {
		Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);
		return customerMapper.selectListMember(begin,end,brandId);
	}

	@Override
	public List<MemberUserDto> callListMemberUser(String beginDate,String endDate) {
		Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);
		return customerMapper.selectListMemberUser(begin,end);
	}
	
	@Override
	public String selectBrandUser() {
		return customerMapper.selectBrandUser();
	}

	@Override
	public Integer selectByShareCustomer(String customerId) {
		return customerMapper.selectByShareCustomer(customerId);
	}

    @Override
    public List<Customer> selectBirthUser() {
        return customerMapper.selectBirthUser();
    }

    @Override
    public Customer selectByTelePhone(String telePhone) {
        return customerMapper.selectByTelePhone(telePhone);
    }

	@Override
	public Customer selectBySerialNumber(String number) {
		return customerMapper.selectBySerialNumber(number);
	}


	@Override
	public Customer getCustomerLimitOne() {
		return customerMapper.getCustomerLimitOne();
	}

    @Override
    public List<Customer> selectByTelePhones(List<String> telePhones) {
        return customerMapper.selectByTelePhones(telePhones);
    }

	@Override
	public List<Customer> getCommentCustomer(String startTime, Integer time,Integer type) {
		return customerMapper.getCommentCustomer(startTime,time,type);
	}

	@Override
	public List<Customer> selectShareCustomerList(String customerId, Integer currentPage, Integer showCount) {
		return customerMapper.selectShareCustomerList(customerId, currentPage, showCount);
	}

	@Override
	public List<Customer> selectBySelectMap(Map<String, Object> selectMap) {
		return customerMapper.selectBySelectMap(selectMap);
	}

	@Override
	public int updateCustomerWechatId(Customer customer) {
		return customerMapper.updateCustomerWechatId(customer);
	}

	@Override
	public Customer selectByAccountId(String accountId) {
		return customerMapper.selectByAccountId(accountId);
	}
}
