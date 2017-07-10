package com.resto.shop.web.service.impl;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.RecommendCategoryArticleMapper;
import com.resto.shop.web.model.RecommendCategoryArticle;
import com.resto.shop.web.service.RecommendCategoryArticleService;

import javax.annotation.Resource;

/**
 * Created by xielc on 2017/6/29.
 */
public class RecommendCategoryArticleServiceImpl extends GenericServiceImpl<RecommendCategoryArticle, String> implements RecommendCategoryArticleService {

    @Resource
    private RecommendCategoryArticleMapper recommendCategoryArticleMapper;

    @Override
    public GenericDao<RecommendCategoryArticle, String> getDao() {
        return null;
    }
}
