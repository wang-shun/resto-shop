package com.resto.shop.web.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.ApplicationUtils;
import com.resto.shop.web.constant.OrderItemType;
import com.resto.shop.web.dao.OrderMapper;
import com.resto.shop.web.exception.AppException;
import com.resto.shop.web.model.Customer;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.model.OrderItem;
import com.resto.shop.web.service.CustomerService;
import com.resto.shop.web.service.OrderService;
import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class OrderServiceImpl extends GenericServiceImpl<Order, String> implements OrderService {

    @Resource
    private OrderMapper orderMapper;
    
    @Resource
    private CustomerService customerService;
    
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

	@Override
	public Order createOrder(Order order, String useCoupon, Boolean useAccount) throws AppException {
		String orderId = ApplicationUtils.randomUUID();
		order.setId(orderId);
		Customer customer = customerService.selectById(order.getCustomerId());
		if(customer==null){
			throw new AppException(AppException.CUSTOMER_NOT_EXISTS);
		}else if(customer.getTelephone()==null){
			throw new AppException(AppException.NOT_BIND_PHONE);
		}else if(order.getOrderItems().isEmpty()){
			throw new AppException(AppException.ORDER_ITEMS_EMPTY);
		}
		
		order.setVerCode(customer.getTelephone().substring(7));
		order.setId(orderId);
		order.setCreateTime(new Date());
		order.setAccountingTime(order.getCreateTime());
		for(OrderItem item :order.getOrderItems()){
			switch (item.getType()) {
			case OrderItemType.ARTICLE:
				
				break;
			case OrderItemType.UNITPRICE:
				break;
			}
		}
		
		return order;
	}

}
