package com.resto.shop.web.dao;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.ArticleRecommend;

import java.util.List;

/**
 * Created by KONATA on 2016/9/8.
 */
public interface ArticleRecommendMapper extends GenericDao<ArticleRecommend, String> {

    List<ArticleRecommend> getRecommendList(String shopId);

    int deleteByPrimaryKey(String id);
}
