package com.resto.shop.web.dao;

import com.resto.shop.web.model.AccountLog;
import com.resto.brand.core.generic.GenericDao;

public interface AccountLogMapper  extends GenericDao<AccountLog,String> {
    int deleteByPrimaryKey(String id);

    int insert(AccountLog record);

    int insertSelective(AccountLog record);

    AccountLog selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(AccountLog record);

    int updateByPrimaryKey(AccountLog record);
}
