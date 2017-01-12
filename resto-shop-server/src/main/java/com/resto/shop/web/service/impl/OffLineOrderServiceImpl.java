package com.resto.shop.web.service.impl;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.OffLineOrderMapper;
import com.resto.shop.web.model.OffLineOrder;
import com.resto.shop.web.service.OffLineOrderService;
import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class OffLineOrderServiceImpl extends GenericServiceImpl<OffLineOrder, String> implements OffLineOrderService {

    @Resource
    private OffLineOrderMapper offlineorderMapper;

    @Override
    public GenericDao<OffLineOrder, String> getDao() {
        return offlineorderMapper;
    } 

}
