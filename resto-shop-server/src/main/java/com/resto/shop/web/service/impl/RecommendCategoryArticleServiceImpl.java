package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.RecommendCategoryArticleMapper;
import com.resto.shop.web.model.RecommendCategory;
import com.resto.shop.web.model.RecommendCategoryArticle;
import com.resto.shop.web.service.RecommendCategoryArticleService;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by xielc on 2017/6/29.
 */
@RpcService
public class RecommendCategoryArticleServiceImpl extends GenericServiceImpl<RecommendCategoryArticle, String> implements RecommendCategoryArticleService {

    @Resource
    private RecommendCategoryArticleMapper recommendCategoryArticleMapper;

    @Override
    public GenericDao<RecommendCategoryArticle, String> getDao() {
        return null;
    }

    /**
     * 根据菜品ID查询信息
     */
    @Override
    public RecommendCategoryArticle selectByArticleId(String articleId) {
        RecommendCategoryArticle recommendCategorys = recommendCategoryArticleMapper.selectByArticleId(articleId);
        return recommendCategorys;
    }
}
