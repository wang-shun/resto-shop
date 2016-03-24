package com.resto.shop.web.service.impl;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.ArticleFamilyMapper;
import com.resto.shop.web.model.ArticleFamily;
import com.resto.shop.web.service.ArticleFamilyService;
import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class ArticleFamilyServiceImpl extends GenericServiceImpl<ArticleFamily, String> implements ArticleFamilyService {

    @Resource
    private ArticleFamilyMapper articlefamilyMapper;

    @Override
    public GenericDao<ArticleFamily, String> getDao() {
        return articlefamilyMapper;
    } 

}
