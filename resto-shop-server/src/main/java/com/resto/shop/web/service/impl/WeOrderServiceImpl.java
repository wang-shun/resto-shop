package com.resto.shop.web.service.impl;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.WeOrderMapper;
import com.resto.shop.web.model.WeOrder;
import com.resto.shop.web.service.WeOrderService;
import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class WeOrderServiceImpl extends GenericServiceImpl<WeOrder, Integer> implements WeOrderService {

    @Resource
    private WeOrderMapper weorderMapper;

    @Override
    public GenericDao<WeOrder, Integer> getDao() {
        return weorderMapper;
    } 

}
