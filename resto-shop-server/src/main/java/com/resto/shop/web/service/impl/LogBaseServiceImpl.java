package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.LogBaseMapper;
import com.resto.shop.web.model.LogBase;
import com.resto.shop.web.service.LogBaseService;

import javax.annotation.Resource;

/**
 * Created by carl on 2016/11/14.
 */
@RpcService
public class LogBaseServiceImpl extends GenericServiceImpl<LogBase, String> implements LogBaseService {

    @Resource
    private LogBaseMapper logBaseMapper;

    @Override
    public GenericDao<LogBase, String> getDao() {
        return logBaseMapper;
    }
}
