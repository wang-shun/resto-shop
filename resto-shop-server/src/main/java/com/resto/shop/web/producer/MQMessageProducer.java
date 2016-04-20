package com.resto.shop.web.producer;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.Producer;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.ons.api.SendResult;
import com.resto.brand.core.util.MQSetting;
import com.resto.shop.web.model.Order;


public class MQMessageProducer {
	final static Logger log = LoggerFactory.getLogger(MQMessageProducer.class);
	
	private static final Producer producer;
	static{
		Properties pro = MQSetting.getPropertiesWithAccessSecret();
		pro.setProperty(PropertyKeyConst.ProducerId,MQSetting.PID_SHOP);
		producer = ONSFactory.createProducer(pro);
		producer.start();
	}
	
	public static void sendAutoCloseMsg(final String orderId, final String brandId,final long delay) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				JSONObject obj = new JSONObject();
				obj.put("orderId", orderId);
				obj.put("brandId", brandId);
				Message message = new Message(MQSetting.TOPIC_RESTO_SHOP,MQSetting.TAG_CANCEL_ORDER, obj.toJSONString().getBytes());				
				message.setStartDeliverTime(System.currentTimeMillis()+delay);
				SendResult result =  producer.send(message);
				log.info("发送自动取消订单消息 :"+message+" 成功:"+result);
			}
		}).start();
	}

	/**
	 * 生产 订单状态改变 消息
	 * @param order
	 * @param brandId
	 */
	public static void sendOrderProessMessage(final Order order, final String brandId) {
		new Thread(new Runnable() {
			public void run() {
				JSONObject obj = new JSONObject();
				obj.put("verCode", order.getVerCode());
				obj.put("orderId", order.getId());
				obj.put("shopDetailId", order.getShopDetailId());
				obj.put("brandId", brandId);
				obj.put("productionState", order.getProductionStatus());
				Message msg = new Message(MQSetting.TOPIC_RESTO_SHOP,MQSetting.TAG_ORDER_PRODUCTION_STATE_CHANGE,obj.toJSONString().getBytes());
				SendResult result = producer.send(msg);
				log.info("发送订单状态改变消息:"+msg+" 成功:"+result);
			}
		}).start();
	}


}
