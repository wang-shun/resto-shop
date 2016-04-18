package com.resto.shop.web.dao;

import com.resto.shop.web.model.ArticleFamily;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.resto.brand.core.generic.GenericDao;

public interface ArticleFamilyMapper  extends GenericDao<ArticleFamily,String> {
    int deleteByPrimaryKey(String id);

    int insert(ArticleFamily record);

    int insertSelective(ArticleFamily record);

    ArticleFamily selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ArticleFamily record);

    int updateByPrimaryKey(ArticleFamily record);

	List<ArticleFamily> selectList(@Param("shopId") String shopId);
	
	List<ArticleFamily> selectListByDistributionModeId(@Param("currentShopId") String currentShopId,@Param("distributionModeId") Integer distributionModeId);
}
