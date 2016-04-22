package com.resto.shop.web.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.OrderItemMapper;
import com.resto.shop.web.model.OrderItem;
import com.resto.shop.web.service.OrderItemService;
import com.resto.shop.web.util.DateUtil;

import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class OrderItemServiceImpl extends GenericServiceImpl<OrderItem, String> implements OrderItemService {

    @Resource
    private OrderItemMapper orderitemMapper;

    @Override
    public GenericDao<OrderItem, String> getDao() {
        return orderitemMapper;
    }

	@Override
	public List<OrderItem> listByOrderId(String orderId) {
		return orderitemMapper.listByOrderId(orderId);
	}

	@Override
	public void insertItems(List<OrderItem> orderItems) {
		
		orderitemMapper.insertBatch(orderItems);
	}

	@Override
	public List<OrderItem> selectOrderArticleList(String orderId) {
		return orderitemMapper.listByOrderId(orderId);
	}

	@Override
	public List<OrderItem> selectSaleArticleByDate(Date beginDate, Date endDate, String shopId) {
		beginDate = DateUtil.getDateBegin(beginDate);
		endDate = DateUtil.getDateEnd(endDate);
		return orderitemMapper.selectSaleArticleByDate(beginDate, endDate, shopId);
	} 

}
