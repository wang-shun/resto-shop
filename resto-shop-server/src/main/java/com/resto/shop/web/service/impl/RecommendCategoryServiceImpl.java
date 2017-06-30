package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.RecommendCategoryMapper;
import com.resto.shop.web.model.RecommendCategory;
import com.resto.shop.web.service.RecommendCategoryService;

import javax.annotation.Resource;

/**
 * Created by xielc on 2017/6/29.
 */
@RpcService
public class RecommendCategoryServiceImpl extends GenericServiceImpl<RecommendCategory, String> implements RecommendCategoryService {

    @Resource
    private RecommendCategoryMapper recommendCategoryMapper;

    @Override
    public GenericDao<RecommendCategory, String> getDao() {
        return null;
    }
}
