package com.resto.shop.web.service.impl;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.ArticleAttrMapper;
import com.resto.shop.web.model.ArticleAttr;
import com.resto.shop.web.service.ArticleAttrService;
import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class ArticleAttrServiceImpl extends GenericServiceImpl<ArticleAttr, Integer> implements ArticleAttrService {

    @Resource
    private ArticleAttrMapper articleattrMapper;

    @Override
    public GenericDao<ArticleAttr, Integer> getDao() {
        return articleattrMapper;
    } 

}
