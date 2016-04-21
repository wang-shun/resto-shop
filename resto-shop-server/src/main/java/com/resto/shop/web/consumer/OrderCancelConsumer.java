package com.resto.shop.web.consumer;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Consumer;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.resto.brand.core.util.MQSetting;
import com.resto.shop.web.datasource.DataSourceContextHolder;
import com.resto.shop.web.service.OrderService;

@Component
public class OrderCancelConsumer implements ApplicationContextAware{
	Logger log = LoggerFactory.getLogger(getClass());
	Consumer consumer =  null;
	
	@Resource
	OrderService orderService;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		Properties pro= MQSetting.getPropertiesWithAccessSecret();
		pro.setProperty(PropertyKeyConst.ConsumerId, MQSetting.CID_SHOP);
		consumer = ONSFactory.createConsumer(pro);
		consumer.subscribe(MQSetting.TOPIC_RESTO_SHOP, MQSetting.TAG_CANCEL_ORDER, new MessageListener() {
			@Override
			public Action consume(Message message, ConsumeContext context) {
				log.info("接收到队列消息:"+message);
				try {
					return executeMessage(message);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
					log.error("字符编码转换错误:"+e.getMessage());
				}
				return Action.CommitMessage;
			}
		});
		consumer.start();
		log.info("启动消费者接受消息！");
	}
	
	public Action executeMessage(Message message) throws UnsupportedEncodingException{
		String tag = message.getTag();
		if(tag.equals(MQSetting.TAG_CANCEL_ORDER)){ //取消订单消息
			return executeCancelOrder(message);
		}
		return Action.CommitMessage;
	}

	private Action executeCancelOrder(Message message) throws UnsupportedEncodingException {
		String 	msg = new String(message.getBody(),MQSetting.DEFAULT_CHAT_SET);
		JSONObject obj =JSONObject.parseObject(msg);
		String brandId = obj.getString("brandId");
		DataSourceContextHolder.setDataSourceName(brandId);
		orderService.cancelOrder(obj.getString("orderId"));
		log.info("自动取消订单:"+obj.getString("orderId"));
		return Action.CommitMessage;
	}

	@PreDestroy
	public void stopConsumer(){
		consumer.shutdown();
	}
}
