package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.ArticleRecommendMapper;
import com.resto.shop.web.model.ArticleRecommend;
import com.resto.shop.web.service.ArticleRecommendService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by KONATA on 2016/9/8.
 */
@RpcService
public class ArticleRecommendServiceImpl extends GenericServiceImpl<ArticleRecommend, String>
        implements ArticleRecommendService {


    @Autowired
    private ArticleRecommendMapper articleRecommendMapper;



    @Override
    public List<ArticleRecommend> getRecommendList(String shopId) {
        List<ArticleRecommend> articleRecommends = articleRecommendMapper.getRecommendList(shopId);
        return  articleRecommends;
    }

    @Override
    public GenericDao<ArticleRecommend, String> getDao() {
        return articleRecommendMapper;
    }
}
