package com.resto.shop.web.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.resto.brand.core.entity.Result;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.ApplicationUtils;
import com.resto.brand.core.util.WeChatUtils;
import com.resto.brand.web.model.Brand;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.service.BrandService;
import com.resto.shop.web.constant.AccountLogType;
import com.resto.shop.web.constant.PayMode;
import com.resto.shop.web.dao.AccountMapper;
import com.resto.shop.web.dao.ChargeOrderMapper;
import com.resto.shop.web.model.Account;
import com.resto.shop.web.model.AccountLog;
import com.resto.shop.web.model.ChargeOrder;
import com.resto.shop.web.model.ChargeSetting;
import com.resto.shop.web.model.Customer;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.model.OrderPaymentItem;
import com.resto.shop.web.service.AccountLogService;
import com.resto.shop.web.service.AccountService;
import com.resto.shop.web.service.ChargeLogService;
import com.resto.shop.web.service.ChargeOrderService;
import com.resto.shop.web.service.ChargeSettingService;
import com.resto.shop.web.service.CustomerService;
import com.resto.shop.web.service.OrderPaymentItemService;

import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class AccountServiceImpl extends GenericServiceImpl<Account, String> implements AccountService {

    @Resource
    private AccountMapper accountMapper;

    @Resource
    OrderPaymentItemService orderPaymentItemService;
    
    @Resource
    CustomerService customerService;
    
    @Resource
    AccountLogService accountLogService;
    
    @Resource
    ChargeOrderService chargeOrderService;
    
    @Resource
    ChargeOrderMapper chargeOrderMapper;
        
    @Resource
    ChargeLogService chargeLogService;
    
    @Resource
    ChargeSettingService chargeSettingService;
    
    @Resource
    BrandService brandService;
    
    @Override
    public GenericDao<Account, String> getDao() {
        return accountMapper;
    }

	@Override
	public BigDecimal useAccount(BigDecimal payMoney, Account account,Integer source) {
		if(account.getRemain().equals(BigDecimal.ZERO)||payMoney.equals(BigDecimal.ZERO)){
			return BigDecimal.ZERO;
		}
		//如果 需要支付的金额大于余额，则扣除所有余额
		BigDecimal useAccountValue = BigDecimal.ZERO;
		if(payMoney.compareTo(account.getRemain())>=0){
			useAccountValue=account.getRemain();
		}else{  //如果 需要支付的金额 小于 余额
			useAccountValue = payMoney;
		}
		account.setRemain(account.getRemain().subtract(useAccountValue));
		String remark= "使用余额:"+useAccountValue+"元";
		addLog(useAccountValue, account, remark, AccountLogType.PAY,source);
		update(account);
		return useAccountValue;
	}

	@Override
	public void addAccount(BigDecimal value, String accountId, String remark,Integer source) {
		Account account = selectById(accountId);
		account.setRemain(account.getRemain().add(value));
		if(value.doubleValue() > 0){
			addLog(value, account, remark, AccountLogType.INCOME,source);
		}else{
			addLog(new BigDecimal(-1).multiply(value), account, remark, AccountLogType.PAY,source);
		}

		update(account);
	}

	private void addLog(BigDecimal money,Account account,String remark,int type,int source){
		AccountLog acclog = new AccountLog();
		acclog.setCreateTime(new Date());
		acclog.setId(ApplicationUtils.randomUUID());
		acclog.setMoney(money);
		acclog.setRemain(account.getRemain());
		acclog.setPaymentType(type);
		acclog.setRemark(remark);
		acclog.setAccountId(account.getId());
		acclog.setSource(source);
		accountLogService.insert(acclog);
	}

	@Override
	public Account selectAccountAndLogByCustomerId(String customerId) {
		Account account = accountMapper.selectAccountByCustomerId(customerId);
		if(account!=null){
			List<AccountLog> accountLogs = accountLogService.selectLogsByAccountId(account.getId());
			account.setAccountLogs(accountLogs);
		}
		return account;
	}

	@Override
	public Account createCustomerAccount(Customer cus) {
		Account acc =new Account();
		acc.setId(ApplicationUtils.randomUUID());
		acc.setRemain(BigDecimal.ZERO);
		insert(acc);
		cus.setAccountId(acc.getId());
		customerService.update(cus);
		return acc;
	}

	@Override
	public BigDecimal payOrder(Order order,BigDecimal payMoney, Customer customer) {
		Account account = selectById(customer.getAccountId());  //找到用户帐户
		BigDecimal balance = chargeOrderService.selectTotalBalance(customer.getId()); //获取所有剩余充值金额
		if(balance==null){
			balance = BigDecimal.ZERO;
		}
		//计算剩余红包金额
		BigDecimal redPackageMoney = account.getRemain().subtract(balance);
		BigDecimal realPay = useAccount(payMoney,account,AccountLog.SOURCE_PAYMENT);  //得出真实支付的值
		//算出 支付比例
		BigDecimal redPay = BigDecimal.ZERO;
		if(realPay.compareTo(BigDecimal.ZERO)>0){ //如果支付金额大于0
			if(redPackageMoney.compareTo(realPay)>=0){ //如果红包金额足够支付所有金额，则只添加红包金额支付项
				redPay = realPay;
			}else{ //如果红包金额不足够支付所有金额，则剩余金额从充值订单里面扣除
				redPay = redPackageMoney;
				BigDecimal remainPay = realPay.subtract(redPay).setScale(2, BigDecimal.ROUND_HALF_UP);  //除去红包后，需要支付的金额
				chargeOrderService.useChargePay(remainPay,customer.getId(),order);
			}
		}
		if(redPay.compareTo(BigDecimal.ZERO)>0){
			OrderPaymentItem item = new OrderPaymentItem();
			item.setId(ApplicationUtils.randomUUID());
			item.setOrderId(order.getId());
			item.setPaymentModeId(PayMode.ACCOUNT_PAY);
			item.setPayTime(new Date());
			item.setPayValue(redPay);
			item.setRemark("余额(红包)支付:" + item.getPayValue());
			item.setResultData(account.getId());
			orderPaymentItemService.insert(item); 
		}
		return realPay;
	}

	@Override
	public BigDecimal houFuPayOrder(Order order,BigDecimal payMoney, Customer customer) {
		Account account = selectById(customer.getAccountId());  //找到用户帐户
		BigDecimal balance = chargeOrderService.selectTotalBalance(customer.getId()); //获取所有剩余充值金额
		if(balance==null){
			balance = BigDecimal.ZERO;
		}
		//计算剩余红包金额
		BigDecimal redPackageMoney = account.getRemain().subtract(balance);
		BigDecimal realPay = useAccount(payMoney,account,AccountLog.SOURCE_PAYMENT);  //得出真实支付的值
		//算出 支付比例
		BigDecimal redPay = BigDecimal.ZERO;
		if(realPay.compareTo(BigDecimal.ZERO)>0){ //如果支付金额大于0
			if(redPackageMoney.compareTo(realPay)>=0){ //如果红包金额足够支付所有金额，则只添加红包金额支付项
				redPay = realPay;
			}else{ //如果红包金额不足够支付所有金额，则剩余金额从充值订单里面扣除
				redPay = redPackageMoney;
				BigDecimal remainPay = realPay.subtract(redPay).setScale(2, BigDecimal.ROUND_HALF_UP);  //除去红包后，需要支付的金额
				chargeOrderService.useChargePay(remainPay,customer.getId(),order);
			}
		}
		if(redPay.compareTo(BigDecimal.ZERO)>0){
			OrderPaymentItem item = new OrderPaymentItem();
			item.setId(ApplicationUtils.randomUUID());
			item.setOrderId(order.getId());
			item.setPaymentModeId(PayMode.ACCOUNT_PAY);
			item.setPayTime(new Date());
			item.setPayValue(redPay);
			item.setRemark("余额(红包)支付:" + item.getPayValue());
			item.setResultData(account.getId());
			orderPaymentItemService.insert(item);
		}
		return realPay;
	}
	
	@Override
	public void updateCustomerAccount(String operationPhone,String customerPhone,ChargeSetting chargeSetting,String customerId,String accountId,Brand brand,ShopDetail shopDetail) {
		try{
	    	ChargeOrder chargeOrder = new ChargeOrder();
	    	chargeOrder.setId(ApplicationUtils.randomUUID());
	    	chargeOrder.setChargeMoney(chargeSetting.getChargeMoney());
	    	chargeOrder.setRewardMoney(chargeSetting.getRewardMoney());
	    	chargeOrder.setOrderState((byte)1);
	    	chargeOrder.setCreateTime(new Date());
	    	chargeOrder.setFinishTime(new Date());
	    	chargeOrder.setCustomerId(customerId);
	    	chargeOrder.setBrandId(brand.getId());
			chargeOrder.setType(0);
	    	chargeOrder.setShopDetailId(shopDetail.getId());
	    	chargeOrder.setChargeBalance(chargeSetting.getChargeMoney());
	    	chargeOrder.setNumberDayNow(chargeSetting.getNumberDay() - 1);
	    	BigDecimal amount = chargeSetting.getRewardMoney().divide(new BigDecimal(chargeSetting.getNumberDay()),2,BigDecimal.ROUND_FLOOR);
	    	chargeOrder.setArrivalAmount(amount);
	    	chargeOrder.setRewardBalance(amount);
	    	chargeOrder.setTotalBalance(chargeOrder.getChargeBalance().add(amount));
	    	BigDecimal endAmount = chargeSetting.getRewardMoney().subtract(amount.multiply(new BigDecimal(chargeSetting.getNumberDay() - 1)));
			chargeOrder.setEndAmount(endAmount);
	    	chargeOrderMapper.insert(chargeOrder);
	    	chargeLogService.insertChargeLogService(operationPhone, customerPhone, chargeOrder.getChargeBalance(), shopDetail,chargeOrder.getId());
	    	addAccount(chargeOrder.getChargeBalance(), accountId, "自助充值",AccountLog.SOURCE_CHARGE);
	    	addAccount(chargeOrder.getRewardBalance(), accountId, "充值赠送",AccountLog.SOURCE_CHARGE_REWARD);
	    	//微信推送
			wxPush(chargeOrder);
    	}catch (Exception e) {
    		log.error("插入ChargeOrder或AccountLog失败!");
    		throw e;
		}
	}
	
	public void wxPush(ChargeOrder chargeOrder){
		log.info("----------品牌Id为:"+chargeOrder.getBrandId()+"");
		log.info("----------用户Id为:"+chargeOrder.getCustomerId()+"");
		Brand brand = brandService.selectById(chargeOrder.getBrandId());
		Customer customer = customerService.selectById(chargeOrder.getCustomerId());
		//如果不是立即到账 优先推送一条提醒
		if(chargeOrder.getNumberDayNow() > 0){
			String msgFrist = "充值成功！充值赠送红包会在" + (chargeOrder.getNumberDayNow() + 1) + "天内分批返还给您，请注意查收～";
			WeChatUtils.sendCustomerMsg(msgFrist.toString(), customer.getWechatId(), brand.getWechatConfig().getAppid(), brand.getWechatConfig().getAppsecret());
		}
		StringBuffer msg = new StringBuffer();
		msg.append("今日充值余额已到账，快去看看吧~");
		String jumpurl = "http://" + brand.getBrandSign() + ".restoplus.cn/wechat/index?dialog=myYue&subpage=my";
		msg.append("<a href='" + jumpurl+ "'>查看账户</a>");
		WeChatUtils.sendCustomerMsg(msg.toString(), customer.getWechatId(), brand.getWechatConfig().getAppid(), brand.getWechatConfig().getAppsecret());
	}
	
}
