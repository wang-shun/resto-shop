package com.resto.shop.web.consumer;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class OrderConsumer {
	Logger log = LoggerFactory.getLogger(getClass());
	Consumer consumer =  null;
	
	
	@PreDestroy
	public void stopConsumer(){
		consumer.shutdown();
	}
	
	@PostConstruct
	public void startConsumer(){
		Properties pro= MQSetting.getPropertiesWithAccessSecret();
		pro.setProperty(PropertyKeyConst.ConsumerId, MQSetting.CID_SHOP);
		consumer = ONSFactory.createConsumer(pro);
		consumer.subscribe(MQSetting.TOPIC_RESTO_SHOP, MQSetting.TAG_ALL, new MessageListener() {
			@Override
			public Action consume(Message message, ConsumeContext context) {
				log.info("接收到队列消息:"+message);
				try {
					return executeMessage(message);
				} catch (UnsupportedEncodingException e) {
					log.error("字符编码转换错误:"+message.toString());
					log.error(e.getMessage());
				}
				return Action.ReconsumeLater;
			}
		});
		consumer.start();
		log.info("启动消费者接受消息！");
	}
	
	@Resource
	OrderService orderService;
	
	public Action executeMessage(Message message) throws UnsupportedEncodingException {
		String tag = message.getTag();
		if(tag.equals(MQSetting.TAG_CANCEL_ORDER)){ //取消订单消息
			String msg = new String(message.getBody(),MQSetting.DEFAULT_CHAT_SET);
			JSONObject object =JSONObject.parseObject(msg);
			String brandId = object.getString("brandId");
			DataSourceContextHolder.setDataSourceName(brandId);
			orderService.cancelOrder(object.getString("orderId"));
		}
		return Action.CommitMessage;
	}
}
