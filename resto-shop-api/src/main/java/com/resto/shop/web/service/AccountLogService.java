package com.resto.shop.web.service;

import java.util.List;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.AccountLog;

public interface AccountLogService extends GenericService<AccountLog, String> {
	/**
	 * 根据 账户ID 查询 账户交易明细
	 * @param accountId
	 * @return
	 */
    List<AccountLog> selectLogsByAccountId(String accountId);
    
    List<AccountLog> selectAccountLog();
}
