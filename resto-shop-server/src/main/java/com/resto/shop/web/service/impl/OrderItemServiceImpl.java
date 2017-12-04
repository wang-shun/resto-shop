package com.resto.shop.web.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.ApplicationUtils;
import com.resto.brand.core.util.DateUtil;
import com.resto.shop.web.constant.OrderItemType;
import com.resto.shop.web.dao.ArticleFamilyMapper;
import com.resto.shop.web.dao.OrderItemMapper;
import com.resto.shop.web.dao.OrderMapper;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.model.OrderItem;
import com.resto.shop.web.service.OrderItemService;

import cn.restoplus.rpc.server.RpcService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 */
@RpcService
public class OrderItemServiceImpl extends GenericServiceImpl<OrderItem, String> implements OrderItemService {

    @Resource
    private OrderItemMapper orderitemMapper;


    @Autowired
    private OrderMapper orderMapper;

    @Override
    public GenericDao<OrderItem, String> getDao() {
        return orderitemMapper;
    }

    @Override
    public List<OrderItem> listByOrderId(Map<String, String> param) {
        List<OrderItem> orderItems = orderitemMapper.listByOrderId(param);

        List<OrderItem> other = orderitemMapper.listTotalByOrderId(param.get("orderId"));

        orderItems.addAll(other);

        return getOrderItemsWithChild(orderItems);
    }


    @Override
    public List<OrderItem> listByParentId(String orderId) {
        return orderitemMapper.listByParentId(orderId);
    }

    List<OrderItem> getOrderItemsWithChild(List<OrderItem> orderItems) {
        log.debug("这里查看套餐子项: ");
        Map<String, OrderItem> idItems = ApplicationUtils.convertCollectionToMap(String.class, orderItems);
        for (OrderItem item : orderItems) {
            if (item.getType() == OrderItemType.MEALS_CHILDREN) {
                OrderItem parent = idItems.get(item.getParentId());
                if (parent.getChildren() == null) {
                    parent.setChildren(new ArrayList<OrderItem>());
                }
                parent.getChildren().add(item);
                idItems.remove(item.getId());
            }
        }
        List<OrderItem> items = new ArrayList<>();
        for (OrderItem orderItem : idItems.values()) {
            items.add(orderItem);
            if (orderItem.getChildren() != null && !orderItem.getChildren().isEmpty()) {
//				for (OrderItem childItem:orderItem.getChildren()) {
                List<OrderItem> item = orderitemMapper.getListBySort(orderItem.getId(),orderItem.getArticleId());
                for(OrderItem obj : item){
                    obj.setArticleName("|_" + obj.getArticleName());
                    items.add(obj);
                }
//                childItem.setArticleName("|__" + childItem.getArticleName());
//                items.add(childItem);
//				}
            }
        }
        return items;
    }

    @Override
    public void insertItems(List<OrderItem> orderItems) {
        //合并相同新规格的餐品
        for(int i = 0; i < orderItems.size(); i++){
            if(orderItems.get(i).getType() == OrderItemType.UNIT_NEW){
                for(int j = 0; j < orderItems.size(); j++){
                    if(orderItems.get(i).getArticleName().equals(orderItems.get(j).getArticleName()) && !orderItems.get(i).getId().equals(orderItems.get(j).getId())){
                        orderItems.get(i).setCount(orderItems.get(i).getCount() + orderItems.get(j).getCount());
                        orderItems.get(i).setFinalPrice(orderItems.get(i).getFinalPrice().add(orderItems.get(j).getFinalPrice()));
                        orderItems.remove(orderItems.get(j));
                    }
                }
            }
        }
        orderitemMapper.insertBatch(orderItems);
        List<OrderItem> allChildren = new ArrayList<>();
        for (OrderItem orderItem : orderItems) {
            if (orderItem.getChildren() != null && !orderItem.getChildren().isEmpty()) {
                allChildren.addAll(orderItem.getChildren());
            }
        }
        if (!allChildren.isEmpty()) {
            orderitemMapper.insertBatch(allChildren);
        }
    }

    @Override
    public List<OrderItem> selectSaleArticleByDate(String shopId, String beginDate, String endDate, String sort) {
        Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);
        if ("0".equals(sort)) {
            sort = "f.peference ,a.sort";
        } else if ("desc".equals(sort)) {
            sort = "brand_report.brandSellNum desc";
        } else if ("asc".equals(sort)) {
            sort = "brand_report.brandSellNum asc";
        }
        return orderitemMapper.selectSaleArticleByDate(begin, end, shopId, sort);
    }

    @Override
    public List<OrderItem> listByOrderIds(List<String> childIds) {
        if (childIds == null || childIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<OrderItem> orderItems = orderitemMapper.listByOrderIds(childIds);

        return getOrderItemsWithChild(orderItems);
    }

	public List<Map<String, Object>> selectOrderItems(String beginDate, String endDate) {
		Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);
		return orderitemMapper.selectOrderItems(begin, end);
	}

	@Override
	public List<OrderItem> selectOrderItemByOrderIds(Map<String, Object> map) {
		return orderitemMapper.selectOrderItemByOrderIds(map);
	}

	@Override
	public List<OrderItem> selectOrderItemByOrderId(Map<String, Object> map) {
		return orderitemMapper.selectOrderItemByOrderId(map);
	}
	
	@Override
	public List<OrderItem> selectRefundOrderItem(Map<String, Object> map) {
		return orderitemMapper.selectRefundOrderItem(map);
	}

    @Override
    public List<OrderItem> getListByParentId(String parentId) {
        return orderitemMapper.getListByParentId(parentId);
    }

    @Override
    public List<OrderItem> getListByRecommendId(String recommendId,String orderId) {
        return orderitemMapper.getListByRecommendId(recommendId,orderId);
    }

    @Override
    public List<OrderItem> selectRefundArticleItem(String orderId) {
        return orderitemMapper.selectRefundArticleItem(orderId);
    }

    @Override
    public List<OrderItem> selectByArticleIds(String[] articleIds) {
        return orderitemMapper.selectByArticleIds(articleIds);
    }

    @Override
    public void posSyncDeleteByOrderId(String orderId) {
        orderitemMapper.posSyncDeleteByOrderId(orderId);
    }

    @Override
    public List<OrderItem> getOrderBefore(String tableNumber, String shopId, String customerId) {
        return orderitemMapper.getOrderBefore(tableNumber, shopId, customerId);
    }

    @Override
    public List<OrderItem> posSyncListByOrderId(String orderId) {
        return orderitemMapper.posSyncListByOrderId(orderId);
    }
}
