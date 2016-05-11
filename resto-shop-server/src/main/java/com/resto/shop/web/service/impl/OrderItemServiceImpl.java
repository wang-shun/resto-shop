package com.resto.shop.web.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.ApplicationUtils;
import com.resto.brand.core.util.DateUtil;
import com.resto.shop.web.constant.OrderItemType;
import com.resto.shop.web.dao.OrderItemMapper;
import com.resto.shop.web.model.OrderItem;
import com.resto.shop.web.service.OrderItemService;

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
		List<OrderItem> orderItems = orderitemMapper.listByOrderId(orderId);
		return getOrderItemsWithChild(orderItems);
	}
	
	List<OrderItem> getOrderItemsWithChild(List<OrderItem> orderItems){
		Map<String, OrderItem> idItems = ApplicationUtils.convertCollectionToMap(String.class, orderItems);
		for(OrderItem item : orderItems){
			if(item.getType()==OrderItemType.MEALS_CHILDREN){
				OrderItem parent = idItems.get(item.getParentId());
				if(parent.getChildren()==null){
					parent.setChildren(new ArrayList<OrderItem>());
				}
				parent.getChildren().add(item);
				idItems.remove(item.getId());
			}
		}
		List<OrderItem> items= new ArrayList<>();
		for (OrderItem orderItem : idItems.values()) {
			items.add(orderItem);
			if(orderItem.getChildren()!=null&&!orderItem.getChildren().isEmpty()){
				for (OrderItem childItem:orderItem.getChildren()) {
					childItem.setArticleName("|__"+childItem.getArticleName());
					items.add(childItem);
				}
			}
		}
		return items;
	}

	@Override
	public void insertItems(List<OrderItem> orderItems) {
		orderitemMapper.insertBatch(orderItems);
		List<OrderItem> allChildren = new ArrayList<>();
		for (OrderItem orderItem : orderItems) {
			if(orderItem.getChildren()!=null&&!orderItem.getChildren().isEmpty()){
				allChildren.addAll(orderItem.getChildren());
			}
		}
		if(!allChildren.isEmpty()){
			orderitemMapper.insertBatch(allChildren);
		}
	}

	@Override
	public List<OrderItem> selectSaleArticleByDate( String shopId,String beginDate, String endDate) {
		Date begin = DateUtil.getformatBeginDate(beginDate);
		Date end = DateUtil.getformatEndDate(endDate);
		return orderitemMapper.selectSaleArticleByDate(begin, end, shopId);
	}

	@Override
	public List<OrderItem> listByOrderIds(List<String> childIds) {
		if(childIds==null||childIds.isEmpty()){
			return new ArrayList<>();
		}
		List<OrderItem> orderItems = orderitemMapper.listByOrderIds(childIds);
		
		return getOrderItemsWithChild(orderItems);
	} 

}
