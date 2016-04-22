package com.resto.shop.web.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.Article;

public interface ArticleMapper extends GenericDao<Article, String>{
    int deleteByPrimaryKey(String id);

    int insert(Article record);

    int insertSelective(Article record);

    Article selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(Article record);

    int updateByPrimaryKey(Article record);

	List<Article> selectList(@Param(value = "shopId") String currentShopId);

	List<Article> selectListByShopIdAndDistributionId(String currentShopId, Integer distributionModeId);

	List<Article> selectBySupportTimeId(@Param("times") List<Integer> supportTimes,@Param("shopId") String currentShopId);
	
	/**
	 * 根据是否谷清 查询菜品信息
	 * @param isEmpty
	 * @return
	 */
	List<Article> selectListByIsEmpty(@Param("isEmpty") Integer isEmpty,@Param("shopId")String shopId);
	
	/**
	 * 根据菜品 Id 设置谷清
	 * @param articleId
	 */
	void setEmpty(@Param("isEmpty") Integer isEmpty,@Param("articleId") String articleId);
}