package com.resto.shop.web.service;

import java.util.List;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.MealItem;

public interface MealItemService extends GenericService<MealItem, Integer> {

	void deleteByMealAttrIds(List<Integer> mealAttrIds);

	void insertBatch(List<MealItem> mealItems);

	List<MealItem> selectByAttrIds(List<Integer> ids);

	List<MealItem> selectByIds(Integer[] mealItemIds);
    
}
