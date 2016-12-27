package com.resto.shop.web.service.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.AccountLogMapper;
import com.resto.shop.web.model.AccountLog;
import com.resto.shop.web.service.AccountLogService;

import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class AccountLogServiceImpl extends GenericServiceImpl<AccountLog, String> implements AccountLogService {

    @Resource
    private AccountLogMapper accountlogMapper;

    @Override
    public GenericDao<AccountLog, String> getDao() {
        return accountlogMapper;
    }

	@Override
	public List<AccountLog> selectLogsByAccountId(String accountId) {
		return accountlogMapper.selectLogsByAccountId(accountId);
	}
	
	@Override
	public List<AccountLog> selectAccountLog(Map<String, String> map) {
		return accountlogMapper.selectAccountLog(map);
	}

    @Override
    public int selectByCustomerIdNumber(String id) {
        List<AccountLog> list = accountlogMapper.selectByCustomerIdNumber(id);
        return list.size();
    }

}
