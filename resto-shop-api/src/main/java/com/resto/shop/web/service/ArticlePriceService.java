package com.resto.shop.web.service;

import java.util.List;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.ArticlePrice;
import org.apache.ibatis.annotations.Param;

public interface ArticlePriceService extends GenericService<ArticlePrice, String> {


	void saveArticlePrices(String articleId, List<ArticlePrice> articlePrises);

	List<ArticlePrice> selectByArticleId(String articleId);

	List<ArticlePrice> selectList(String shopDetailId);

	ArticlePrice selectByArticle(String articleId,int unitId);

	Integer clearPriceStock(@Param("articleId") String articleId, @Param("emptyRemark") String emptyRemark);

	Integer clearPriceTotal(@Param("articleId") String articleId, @Param("emptyRemark") String emptyRemark);
    
}
