package com.resto.shop.web.service.impl;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.AppraiseMapper;
import com.resto.shop.web.model.Appraise;
import com.resto.shop.web.service.AppraiseService;
import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class AppraiseServiceImpl extends GenericServiceImpl<Appraise, String> implements AppraiseService {

    @Resource
    private AppraiseMapper appraiseMapper;

    @Override
    public GenericDao<Appraise, String> getDao() {
        return appraiseMapper;
    } 

}
