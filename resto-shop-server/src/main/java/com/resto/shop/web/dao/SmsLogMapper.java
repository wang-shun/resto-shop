package com.resto.shop.web.dao;

import com.resto.shop.web.model.SmsLog;
import com.resto.brand.core.generic.GenericDao;

public interface SmsLogMapper  extends GenericDao<SmsLog,Long> {
    int deleteByPrimaryKey(Long id);

    int insert(SmsLog record);

    int insertSelective(SmsLog record);

    SmsLog selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SmsLog record);

    int updateByPrimaryKey(SmsLog record);
}
