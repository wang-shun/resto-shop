package com.resto.shop.web.dao;

import com.resto.shop.web.model.ArticlePrice;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.resto.brand.core.generic.GenericDao;

public interface ArticlePriceMapper  extends GenericDao<ArticlePrice,String> {
    int deleteByPrimaryKey(String id);

    int insert(ArticlePrice record);

    int insertSelective(ArticlePrice record);

    ArticlePrice selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ArticlePrice record);

    int updateByPrimaryKey(ArticlePrice record);

	void deleteArticlePrices(String articleId);

	List<ArticlePrice> selectByArticleId(String articleId);

	List<ArticlePrice> selectList(@Param("shopId") String shopDetailId);
}
