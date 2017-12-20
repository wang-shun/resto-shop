package com.resto.shop.web.service;

import java.util.List;
import java.util.Map;

import com.resto.brand.core.generic.GenericService;
import com.resto.brand.web.dto.ArticleSellDto;
import com.resto.shop.web.model.Article;
import com.resto.shop.web.model.MealAttr;

public interface MealAttrService extends GenericService<MealAttr, Integer> {

	void insertBatch(List<MealAttr> mealAttrs, String article_id);

	List<MealAttr> selectList(String article_id);

	void deleteByIds(List<Integer> ids);


	List<MealAttr> selectFullByArticleId(String articleId,String show,Map<String, Article> articleMap);


	List<ArticleSellDto> queryArticleMealAttr(Map<String, Object> selectMap);
}
