package com.resto.shop.web.service.impl;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.OrderMapper;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.service.OrderService;
import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class OrderServiceImpl extends GenericServiceImpl<Order, String> implements OrderService {

    @Resource
    private OrderMapper orderMapper;

    @Override
    public GenericDao<Order, String> getDao() {
        return orderMapper;
    } 

}
