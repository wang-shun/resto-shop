package com.resto.shop.web.service;

import java.util.List;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.Article;

public interface ArticleService extends GenericService<Article, String> {

	List<Article> selectList(String currentShopId);

	Article save(Article article);

	Article selectFullById(String id);

	/**
	 * 通过店铺Id以及配送模式查询
	 * @param currentShopId
	 * @param distributionModeId
	 * @return
	 */
	List<Article> selectListFull(String currentShopId, Integer distributionModeId);
	
	/**
	 * 根据 是否 谷清 查询菜品信息
	 * @param isEmpty
	 * @return
	 */
	List<Article> selectListByIsEmpty(Integer isEmpty,String shopId);
	
	/**
	 * 根据 菜品Id 设置谷清
	 * @param articleId
	 */
	void setEmpty(Integer isEmpty,String articleId);
    
}
