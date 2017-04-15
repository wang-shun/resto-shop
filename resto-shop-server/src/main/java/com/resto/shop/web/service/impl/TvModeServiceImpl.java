package com.resto.shop.web.service.impl;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.TvModeMapper;
import com.resto.shop.web.model.TvMode;
import com.resto.shop.web.service.TvModeService;
import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class TvModeServiceImpl extends GenericServiceImpl<TvMode, Integer> implements TvModeService {

    @Resource
    private TvModeMapper tvmodeMapper;

    @Override
    public GenericDao<TvMode, Integer> getDao() {
        return tvmodeMapper;
    } 

}
