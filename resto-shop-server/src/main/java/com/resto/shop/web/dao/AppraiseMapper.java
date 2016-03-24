package com.resto.shop.web.dao;

import com.resto.shop.web.model.Appraise;
import com.resto.brand.core.generic.GenericDao;

public interface AppraiseMapper  extends GenericDao<Appraise,String> {
    int deleteByPrimaryKey(String id);

    int insert(Appraise record);

    int insertSelective(Appraise record);

    Appraise selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(Appraise record);

    int updateByPrimaryKey(Appraise record);
}
