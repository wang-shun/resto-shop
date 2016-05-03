package com.resto.shop.web.dao;

import com.resto.shop.web.model.MealTemp;
import com.resto.brand.core.generic.GenericDao;

public interface MealTempMapper  extends GenericDao<MealTemp,Integer> {
    int deleteByPrimaryKey(Integer id);

    int insert(MealTemp record);

    int insertSelective(MealTemp record);

    MealTemp selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(MealTemp record);

    int updateByPrimaryKey(MealTemp record);
}
