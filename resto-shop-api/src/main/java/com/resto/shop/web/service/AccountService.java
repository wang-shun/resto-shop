package com.resto.shop.web.service;

import java.math.BigDecimal;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.Account;

public interface AccountService extends GenericService<Account, String> {

	/**
	 * @param maxUseAccount 最高使用余额的金额
	 * @param account
	 * @return
	 */
	BigDecimal useAccount(BigDecimal maxUseAccount, Account account);
    
	/**
	 * 根据用户查询 余额 和 交易明细
	 * @param customerId
	 * @return
	 */
	Account selectAccountAndLogByCustomerId(String customerId);
}
