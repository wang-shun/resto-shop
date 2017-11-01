package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.OrderBeforeMapper;
import com.resto.shop.web.model.OffLineOrder;
import com.resto.shop.web.model.OrderBefore;
import com.resto.shop.web.service.OffLineOrderService;
import com.resto.shop.web.service.OrderBeforeService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by KONATA on 2017/11/1.
 */
@RpcService
public class OrderBeforeServiceImpl extends GenericServiceImpl<OrderBefore, Long> implements OrderBeforeService {

    @Autowired
    private OrderBeforeMapper orderBeforeMapper;

    @Override
    public GenericDao<OrderBefore, Long> getDao() {
        return orderBeforeMapper;
    }



    @Override
    public OrderBefore getOrderNoPay(String tableNumber, String shopId) {
        return orderBeforeMapper.getOrderNoPay(tableNumber, shopId);
    }
}
