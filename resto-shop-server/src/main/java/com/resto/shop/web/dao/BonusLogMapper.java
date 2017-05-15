package com.resto.shop.web.dao;

import com.resto.shop.web.model.BonusLog;
import com.resto.brand.core.generic.GenericDao;

public interface BonusLogMapper  extends GenericDao<BonusLog,String> {
    int deleteByPrimaryKey(String id);

    int insert(BonusLog record);

    int insertSelective(BonusLog record);

    BonusLog selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(BonusLog record);

    int updateByPrimaryKey(BonusLog record);
}
