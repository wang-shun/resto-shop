package com.resto.shop.web.producer;

import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import com.resto.shop.web.model.Appraise;
import com.resto.shop.web.model.Customer;
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
		JSONObject obj = new JSONObject();
		obj.put("orderId", orderId);
		obj.put("brandId", brandId);
		Message message = new Message(MQSetting.TOPIC_RESTO_SHOP,MQSetting.TAG_CANCEL_ORDER, obj.toJSONString().getBytes());				
		message.setStartDeliverTime(System.currentTimeMillis()+delay);
		sendMessageASync(message);
	}

	public static void sendAutoRefundMsg(final String brandId,final String orderId,final String customerId){
		JSONObject obj = new JSONObject();
		obj.put("brandId", brandId);
		obj.put("orderId", orderId);
		obj.put("customerId",customerId);
		Message message = new Message(MQSetting.TOPIC_RESTO_SHOP,MQSetting.TAG_AUTO_REFUND_ORDER, obj.toJSONString().getBytes());
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.set(Calendar.HOUR_OF_DAY,23);
		calendar.set(Calendar.MINUTE,59);
		message.setStartDeliverTime(calendar.getTime().getTime());
		sendMessageASync(message);
	}


	public static void sendAutoConfirmOrder(final Order order, final long delayTime) {
		JSONObject obj = new JSONObject();
		obj.put("brandId", order.getBrandId());
		obj.put("id", order.getId());
		Message message = new Message(MQSetting.TOPIC_RESTO_SHOP,MQSetting.TAG_AUTO_CONFIRM_ORDER,obj.toJSONString().getBytes());
		long delay = System.currentTimeMillis()+delayTime;
		message.setStartDeliverTime(delay);
		sendMessageASync(message);
	}

	public static void sendNotPrintedMessage(final Order order, final long delayTime) {
		JSONObject obj = new JSONObject();
		obj.put("brandId", order.getBrandId());
		obj.put("id", order.getId());
		obj.put("shopDetailId", order.getShopDetailId());
		obj.put("articleCount", order.getArticleCount());
		obj.put("productionStatus", order.getProductionStatus());
		obj.put("verCode", order.getVerCode());
		obj.put("parentOrderId", order.getParentOrderId());
		
		Message message = new Message(MQSetting.TOPIC_RESTO_SHOP,MQSetting.TAG_NOT_PRINT_ORDER,obj.toJSONString().getBytes());
		message.setStartDeliverTime(System.currentTimeMillis()+delayTime);
		sendMessageASync(message);
	}

	public static void sendShareMsg(final Appraise appraise,final long delayTime){
		JSONObject obj = new JSONObject();
		obj.put("brandId", appraise.getBrandId());
		obj.put("id", appraise.getId());
		obj.put("customerId", appraise.getCustomerId());
		Message message = new Message(MQSetting.TOPIC_RESTO_SHOP,MQSetting.TAG_SHOW_ORDER,obj.toJSONString().getBytes());
		message.setStartDeliverTime(System.currentTimeMillis()+delayTime);
		sendMessageASync(message);
	}


	public static void sendMessageASync(final Message message) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				SendResult result = producer.send(message);
				log.info("["+message.getTag()+"] "+"发送消息成功:"+result);
			}
		}).start();
	}

	
	public static void sendPlaceOrderMessage(Order order) {
 		JSONObject obj  = new JSONObject();
		obj.put("brandId", order.getBrandId());
		obj.put("id", order.getId());
		obj.put("tableNumber", order.getTableNumber());
		obj.put("shopDetailId", order.getShopDetailId());
		obj.put("articleCount", order.getArticleCount());
		obj.put("orderMode",order.getOrderMode());
		obj.put("productionStatus", order.getProductionStatus());
		obj.put("verCode", order.getVerCode());
		obj.put("parentOrderId", order.getParentOrderId());
		Message message = new Message(MQSetting.TOPIC_RESTO_SHOP,MQSetting.TAG_PLACE_ORDER,obj.toJSONString().getBytes());
		sendMessageASync(message);
	}

	public static void checkPlaceOrderMessage(Order order,Long delayTime,Long limitTime) {
		JSONObject obj = new JSONObject();
		obj.put("brandId", order.getBrandId());
		obj.put("id", order.getId());
		obj.put("timeOut",delayTime.equals(limitTime));
		Message message = new Message(MQSetting.TOPIC_RESTO_SHOP,MQSetting.TAG_CHECK_ORDER, obj.toJSONString().getBytes());
		message.setStartDeliverTime(System.currentTimeMillis()+delayTime);
		sendMessageASync(message);
	}


	public static void sendNoticeOrderMessage(Order order) {
		JSONObject obj  = new JSONObject();
		obj.put("brandId", order.getBrandId());
		obj.put("tableNumber", order.getTableNumber());
		obj.put("id", order.getId());
		obj.put("orderState", order.getOrderState());
		obj.put("shopDetailId", order.getShopDetailId());
		obj.put("articleCount", order.getArticleCount());
		obj.put("productionStatus", order.getProductionStatus());
		obj.put("verCode", order.getVerCode());
		obj.put("parentOrderId", order.getParentOrderId());
		Message message = new Message(MQSetting.TOPIC_RESTO_SHOP,MQSetting.TAG_NOTICE_ORDER,obj.toJSONString().getBytes());
		sendMessageASync(message);
		
	}


	public static void sendNoticeShareMessage(Customer customer){
		JSONObject obj  = new JSONObject();
		obj.put("id", customer.getId());
		obj.put("shareCustomer", customer.getShareCustomer());
		obj.put("brandId", customer.getBrandId());
		obj.put("nickname",customer.getNickname());
		Message message = new Message(MQSetting.TOPIC_RESTO_SHOP,MQSetting.TAG_NOTICE_SHARE_CUSTOMER,obj.toJSONString().getBytes());
		sendMessageASync(message);

	}

	public static void sendNotAllowContinueMessage(Order order, long delay) {
		JSONObject object=  new JSONObject();
		object.put("brandId", order.getBrandId());
		object.put("id", order.getId());
		Message message = new Message(MQSetting.TOPIC_RESTO_SHOP,MQSetting.TAG_NOT_ALLOW_CONTINUE,object.toJSONString().getBytes());
		message.setStartDeliverTime(System.currentTimeMillis()+delay);
		sendMessageASync(message);
	}

}
