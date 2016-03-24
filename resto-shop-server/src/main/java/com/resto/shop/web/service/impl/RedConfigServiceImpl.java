package com.resto.shop.web.service.impl;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.RedConfigMapper;
import com.resto.shop.web.model.RedConfig;
import com.resto.shop.web.service.RedConfigService;
import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class RedConfigServiceImpl extends GenericServiceImpl<RedConfig, Long> implements RedConfigService {

    @Resource
    private RedConfigMapper redconfigMapper;

    @Override
    public GenericDao<RedConfig, Long> getDao() {
        return redconfigMapper;
    } 

}
