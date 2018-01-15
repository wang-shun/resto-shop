package com.resto.shop.web.report;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.MealTemp;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MealTempMapper  extends GenericDao<MealTemp,Integer> {
    int deleteByPrimaryKey(Integer id);

    int insert(MealTemp record);

    int insertSelective(MealTemp record);

    MealTemp selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(MealTemp record);

    int updateByPrimaryKey(MealTemp record);

	MealTemp selectFull(Integer id);

	List<MealTemp> selectByBrandId(String brandId);

	List<MealTemp> selectList(@Param("brandId") String brandId);
}
