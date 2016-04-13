package com.resto.shop.web.service.impl;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.ArticlePriceMapper;
import com.resto.shop.web.model.ArticlePrice;
import com.resto.shop.web.service.ArticlePriceService;
import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class ArticlePriceServiceImpl extends GenericServiceImpl<ArticlePrice, String> implements ArticlePriceService {

    @Resource
    private ArticlePriceMapper articlepriceMapper;

    @Override
    public GenericDao<ArticlePrice, String> getDao() {
        return articlepriceMapper;
    } 

}
