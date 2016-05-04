package com.resto.shop.web.container;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.resto.brand.core.util.ObjectTranscoder;
import com.resto.shop.web.constant.ProductionStatus;
import com.resto.shop.web.model.Customer;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.service.CustomerService;
import com.resto.shop.web.service.OrderService;

import redis.clients.jedis.Jedis;


/**
 * 订单生产状态容器
 * @author Diamond
 * @date 2016年4月21日
 */
@Component
public class OrderProductionStateContainer {
	private static final String PUSH_ORDER = "PUSH_ORDER_MAP";
	private static final String READY_ORDER = "READY_ORDER_MAP";
	private static final String CALL_NOW = "CALL_NOW_MAP";
	private static final String SHOP_INFO = "INTI_SHOP";
	
	private static final String IP = "localhost";
//	private static final int PORT = 6379;
	private static final Jedis redis = new Jedis(IP);
	
	
	@Resource
	OrderService orderService;
	@Resource
	CustomerService customerService;
	
	
	public List<Order> getPushOrderList(String shopId,Long lastTime){
		initShop(shopId);
		if(lastTime == null){
			lastTime = Long.valueOf(0);
		}
		List<Order> orderList = new ArrayList<>();
		Set<byte[]> orderSet = redis.zrangeByScore(buildPushKey(shopId), lastTime+1, System.currentTimeMillis());
		Iterator<byte[]> iterator = orderSet.iterator();
		while (iterator.hasNext()) {
			Order order = (Order)ObjectTranscoder.deserialize(iterator.next());
			orderList.add(order);
		}
		return orderList;
	}
	
	public List<Order> getReadyOrderList(String shopId,Long lastTime){
		if(lastTime == null){
			lastTime = Long.valueOf(0);
		}
		List<Order> orderList = new ArrayList<>();
		Set<byte[]> orderSet = redis.zrangeByScore(buildReadyKey(shopId), lastTime+1, System.currentTimeMillis());
		Iterator<byte[]> iterator = orderSet.iterator();
		while (iterator.hasNext()) {  
			Order order = (Order)ObjectTranscoder.deserialize(iterator.next());
			orderList.add(order);
		}
		return orderList;
	}
	
	public List<Order> getCallNowList(String shopId, Long lastTime){
 		if(lastTime == null){
			lastTime = Long.valueOf(0);
		}
		List<Order> orderList = new ArrayList<>();
		Set<byte[]> orderSet = redis.zrangeByScore(buildCallKey(shopId), lastTime+1, System.currentTimeMillis());
		Iterator<byte[]> iterator = orderSet.iterator();
		while (iterator.hasNext()) {  
			Order order = (Order)ObjectTranscoder.deserialize(iterator.next());
			orderList.add(order);
		}
		return orderList;
	}
	
	public void addOrder(Order order){
		if(order.getProductionStatus()==ProductionStatus.HAS_ORDER){  //-- 已下单
			redis.zadd(buildPushKey(order.getShopDetailId()), order.getPushOrderTime().getTime(), ObjectTranscoder.serialize(order));
		}else if(order.getProductionStatus()==ProductionStatus.HAS_CALL){//-- 叫号
			if(order.getCustomer()==null){
				Customer cus = customerService.selectById(order.getCustomerId());
				order.setCustomer(cus);
			}
			//添加 到 叫号队列中 
			redis.zadd(buildCallKey(order.getShopDetailId()), order.getCallNumberTime().getTime(), ObjectTranscoder.serialize(order));
			//移除 准备列队 中的此订单
			redis.zrem(buildReadyKey(order.getShopDetailId()), ObjectTranscoder.serialize(order));
		}else if(order.getProductionStatus()==ProductionStatus.PRINTED){//-- 打印
			if(order.getCustomer()==null){
				Customer cus = customerService.selectById(order.getCustomerId());
				order.setCustomer(cus);
			}
			//添加到 准备队列 中
			redis.zadd(buildReadyKey(order.getShopDetailId()), order.getPrintOrderTime().getTime(), ObjectTranscoder.serialize(order));
			//移除 已下单列队 中的此订单
			redis.zrem(buildPushKey(order.getShopDetailId()), ObjectTranscoder.serialize(order));
		}
	}


	/**
	 * 初始化店铺订单信息
	 * @param shopId
	 */
	private void initShop(String shopId) {
		//判断是否存在
		if(!redis.hexists(SHOP_INFO, shopId)){
			redis.hset(SHOP_INFO, shopId, "true");
			List<Order> order = orderService.selectTodayOrder(shopId,new int[]{ProductionStatus.HAS_ORDER,ProductionStatus.PRINTED});
			for (Order o: order) {
				addOrder(o);
			}
		}
	}
	

	/**
	 * 移除  已下单的订单信息
	 * @param order
	 */
	public void removePushOrder(Order order) {
		redis.zrem(buildPushKey(order.getShopDetailId()), ObjectTranscoder.serialize(order));
	}

	/**
	 * 删除 指定店铺的 队列 数据
	 * @param currentShopId
	 */
	public static void clearMap(String currentShopId) {
		redis.del(buildCallKey(currentShopId));
		redis.del(buildPushKey(currentShopId));
		redis.del(buildReadyKey(currentShopId));
		redis.hdel(SHOP_INFO, currentShopId);
	}
	
	/**
	 * 根据 店铺ID 生成 已下单ID 
	 * @param shopId
	 * @return
	 */
	public static byte[] buildPushKey(String shopId){
		return (PUSH_ORDER+"@"+shopId).getBytes();
	}
	
	/**
	 * 根据 店铺ID 生成 准备中 ID
	 * @param shopId
	 * @return
	 */
	public static byte[] buildReadyKey(String shopId){
		return (READY_ORDER+"@"+shopId).getBytes();
	}
	
	/**
	 * 根据 店铺ID 生成 已叫号 ID
	 * @param shopId
	 * @return
	 */
	public static byte[] buildCallKey(String shopId){
		return (CALL_NOW+"@"+shopId).getBytes();
	}
	
}
