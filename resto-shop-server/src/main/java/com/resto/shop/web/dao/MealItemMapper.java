package com.resto.shop.web.dao;

import com.resto.shop.web.model.MealItem;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.resto.brand.core.generic.GenericDao;

public interface MealItemMapper  extends GenericDao<MealItem,Integer> {
    int deleteByPrimaryKey(Integer id);

    int insert(MealItem record);

    int insertSelective(MealItem record);

    MealItem selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(MealItem record);

    int updateByPrimaryKey(MealItem record);

	void deleteByMealAttrIds(@Param("mealAttrIds")List<Integer> mealAttrIds);

	void insertBatch(@Param("mealItems") List<MealItem> mealAttrs);

	List<MealItem> selectByAttrIds(@Param("mealAttrIds") List<Integer> mealAttrIds);
}
