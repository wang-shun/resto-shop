package com.resto.shop.web.service.impl;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.BadTopMapper;
import com.resto.shop.web.model.BadTop;
import com.resto.shop.web.service.BadTopService;
import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class BadTopServiceImpl extends GenericServiceImpl<BadTop, Long> implements BadTopService {

    @Resource
    private BadTopMapper badtopMapper;

    @Override
    public GenericDao<BadTop, Long> getDao() {
        return badtopMapper;
    } 

}
