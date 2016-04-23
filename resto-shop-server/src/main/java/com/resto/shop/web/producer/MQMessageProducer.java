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

	public static void sendAutoConfirmOrder(final Order order, final long delayTime) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				JSONObject obj = new JSONObject();
				obj.put("brandId", order.getBrandId());
				obj.put("id", order.getId());
				Message message = new Message(MQSetting.TOPIC_RESTO_SHOP,MQSetting.TAG_AUTO_CONFIRM_ORDER,obj.toJSONString().getBytes());
				long delay = System.currentTimeMillis()+delayTime;
				message.setStartDeliverTime(delay);
				SendResult result = producer.send(message);
				log.info("发送自动确认订单消息:"+delayTime+"ms 后执行"+order.getId()+"@"+order.getBrandId()+"@"+ result);
			}
		}).start();
	}

	public static void sendNotPrintedMessage(final Order order, final long delayTime) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				JSONObject obj = new JSONObject();
				obj.put("brandId", order.getBrandId());
				obj.put("id", order.getId());
				Message message = new Message(MQSetting.TOPIC_RESTO_SHOP,MQSetting.TAG_NOT_PRINT_ORDER,obj.toJSONString().getBytes());
				message.setStartDeliverTime(System.currentTimeMillis()+delayTime);
				SendResult result = producer.send(message);
				log.info("发送超时未打印订单消息:"+result);
				
			}
		});
	}

}
