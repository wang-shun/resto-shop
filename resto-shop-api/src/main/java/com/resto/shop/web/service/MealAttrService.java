package com.resto.shop.web.service;

import java.util.List;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.MealAttr;

public interface MealAttrService extends GenericService<MealAttr, Integer> {

	void insertBatch(List<MealAttr> mealAttrs, String article_id);

	List<MealAttr> selectList(String article_id);

	void deleteByIds(List<Integer> ids);


	List<MealAttr> selectFullByArticleId(String articleId,String show);
    
}
