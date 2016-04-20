package com.resto.shop.web.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.ApplicationUtils;
import com.resto.shop.web.constant.AccountLogType;
import com.resto.shop.web.dao.AccountMapper;
import com.resto.shop.web.model.Account;
import com.resto.shop.web.model.AccountLog;
import com.resto.shop.web.service.AccountLogService;
import com.resto.shop.web.service.AccountService;

import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class AccountServiceImpl extends GenericServiceImpl<Account, String> implements AccountService {

    @Resource
    private AccountMapper accountMapper;

    @Resource
    AccountLogService accountLogService;
    
    @Override
    public GenericDao<Account, String> getDao() {
        return accountMapper;
    }

	@Override
	public BigDecimal useAccount(BigDecimal payMoney, Account account) {
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
		AccountLog acclog = new AccountLog();
		acclog.setCreateTime(new Date());
		acclog.setId(ApplicationUtils.randomUUID());
		acclog.setMoney(useAccountValue);
		acclog.setRemain(account.getRemain());
		acclog.setPaymentType(AccountLogType.PAY);
		acclog.setRemark("使用余额:"+acclog.getMoney()+"元");
		acclog.setAccountId(account.getId());
		accountLogService.insert(acclog);
		update(account);
		return useAccountValue;
	}

	@Override
	public Account selectAccountAndLogByCustomerId(String customerId) {
		System.out.println(customerId);
		Account account = accountMapper.selectAccountByCustomerId(customerId);
		if(account!=null){
			List<AccountLog> accountLogs = accountLogService.selectLogsByAccountId(account.getId());
			account.setAccountLogs(accountLogs);
		}
		return account;
	} 

}
