package com.resto.shop.web.service;

import java.util.List;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.MealAttr;

public interface MealAttrService extends GenericService<MealAttr, Integer> {

	void insertBatch(List<MealAttr> mealAttrs, String article_id);

	List<MealAttr> selectList(String article_id);

	void deleteByIds(List<Integer> ids);


	List<MealAttr> selectFullByArticleId(String articleId,String show);

	/**
	 * 根据 店铺ID 查询店铺下的所有  MealAttr  数据
	 * Pos2.0 数据拉取接口			By___lmx
	 * @param shopId
	 * @return
	 */
	List<MealAttr> selectMealAttrByShopId(String shopId);
}
