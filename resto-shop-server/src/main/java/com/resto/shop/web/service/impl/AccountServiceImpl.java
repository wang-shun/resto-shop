package com.resto.shop.web.service.impl;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.AccountMapper;
import com.resto.shop.web.model.Account;
import com.resto.shop.web.service.AccountService;
import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class AccountServiceImpl extends GenericServiceImpl<Account, String> implements AccountService {

    @Resource
    private AccountMapper accountMapper;

    @Override
    public GenericDao<Account, String> getDao() {
        return accountMapper;
    } 

}
