package com.resto.shop.web.dao;

import com.resto.shop.web.model.Account;
import com.resto.brand.core.generic.GenericDao;

public interface AccountMapper  extends GenericDao<Account,String> {
    int deleteByPrimaryKey(String id);

    int insert(Account record);

    int insertSelective(Account record);

    Account selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(Account record);

    int updateByPrimaryKey(Account record);
}
