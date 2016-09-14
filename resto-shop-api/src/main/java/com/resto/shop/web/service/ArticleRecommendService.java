package com.resto.shop.web.service;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.ArticleRecommend;
import com.resto.shop.web.model.ArticleRecommendPrice;

import java.util.List;

/**
 * Created by KONATA on 2016/9/8.
 */
public interface ArticleRecommendService extends GenericService<ArticleRecommend, String> {


    List<ArticleRecommend> getRecommendList(String shopId);

    ArticleRecommend getRecommendById(String id);

    void insertRecommendArticle(String recommendId, List<ArticleRecommendPrice> articleRecommendPrices);

    void updateRecommendArticle(String recommendId,List<ArticleRecommendPrice> articleRecommendPrices);

    ArticleRecommend getRecommentByArticleId(String articleId,String shopId);
}
