package com.resto.shop.web.service.impl;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.ChargeOrderMapper;
import com.resto.shop.web.model.ChargeOrder;
import com.resto.shop.web.service.ChargeOrderService;
import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class ChargeOrderServiceImpl extends GenericServiceImpl<ChargeOrder, String> implements ChargeOrderService {

    @Resource
    private ChargeOrderMapper chargeorderMapper;

    @Override
    public GenericDao<ChargeOrder, String> getDao() {
        return chargeorderMapper;
    } 

}
