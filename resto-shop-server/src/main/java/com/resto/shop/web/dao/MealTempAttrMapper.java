package com.resto.shop.web.dao;

import com.resto.shop.web.model.MealTempAttr;
import com.resto.brand.core.generic.GenericDao;

public interface MealTempAttrMapper  extends GenericDao<MealTempAttr,Integer> {
    int deleteByPrimaryKey(Integer id);

    int insert(MealTempAttr record);

    int insertSelective(MealTempAttr record);

    MealTempAttr selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(MealTempAttr record);

    int updateByPrimaryKey(MealTempAttr record);
}
