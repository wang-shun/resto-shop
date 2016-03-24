package com.resto.shop.web.service.impl;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.DeliveryPointMapper;
import com.resto.shop.web.model.DeliveryPoint;
import com.resto.shop.web.service.DeliveryPointService;
import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class DeliveryPointServiceImpl extends GenericServiceImpl<DeliveryPoint, Integer> implements DeliveryPointService {

    @Resource
    private DeliveryPointMapper deliverypointMapper;

    @Override
    public GenericDao<DeliveryPoint, Integer> getDao() {
        return deliverypointMapper;
    } 

}
