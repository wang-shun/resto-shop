package com.resto.shop.web.service.impl;

import java.util.List;
import java.util.Map;

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

	@Override
	public List<Order> listOrder(Integer start, Integer datalength, String shopId, String customerId) {
		return orderMapper.orderList(start, datalength, shopId, customerId);
	}

	@Override
	public Map<String, Integer> selectOrderStatesById(String orderId) {
		return orderMapper.selectOrderStatesById(orderId);
	}

}
