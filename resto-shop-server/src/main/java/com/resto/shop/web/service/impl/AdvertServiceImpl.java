package com.resto.shop.web.service.impl;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.AdvertMapper;
import com.resto.shop.web.model.Advert;
import com.resto.shop.web.service.AdvertService;
import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class AdvertServiceImpl extends GenericServiceImpl<Advert, Integer> implements AdvertService {

    @Resource
    private AdvertMapper advertMapper;

    @Override
    public GenericDao<Advert, Integer> getDao() {
        return advertMapper;
    } 

}
