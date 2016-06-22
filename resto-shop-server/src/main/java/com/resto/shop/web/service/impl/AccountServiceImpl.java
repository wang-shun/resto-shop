package com.resto.shop.web.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.ApplicationUtils;
import com.resto.shop.web.constant.AccountLogType;
import com.resto.shop.web.constant.PayMode;
import com.resto.shop.web.dao.AccountMapper;
import com.resto.shop.web.model.Account;
import com.resto.shop.web.model.AccountLog;
import com.resto.shop.web.model.Customer;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.model.OrderPaymentItem;
import com.resto.shop.web.service.AccountLogService;
import com.resto.shop.web.service.AccountService;
import com.resto.shop.web.service.ChargeOrderService;
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
		addLog(value, account, remark, AccountLogType.INCOME,source);
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
				BigDecimal remainPay = realPay.subtract(redPackageMoney).setScale(2, BigDecimal.ROUND_HALF_UP);  //除去红包后，需要支付的金额
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

}
