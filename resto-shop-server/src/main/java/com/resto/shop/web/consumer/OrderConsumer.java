package com.resto.shop.web.consumer;

import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.aliyun.openservices.ons.api.Consumer;
import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.resto.brand.core.util.MQSetting;

@Component
public class OrderConsumer{
	Logger log = LoggerFactory.getLogger(getClass());
	Consumer consumer =  null;
	
	@Resource
	OrderMessageListener orderMessageListener;
	
	@PostConstruct
	public void startConsumer(){
		Properties pro= MQSetting.getPropertiesWithAccessSecret();
		pro.setProperty(PropertyKeyConst.ConsumerId, MQSetting.CID_SHOP);
		log.info("正在启动消费者");
		consumer = ONSFactory.createConsumer(pro);
		consumer.subscribe(MQSetting.TOPIC_RESTO_SHOP, MQSetting.TAG_ALL, orderMessageListener);
		consumer.start();
		log.info("消费者启动成功！TOPIC:"+MQSetting.TOPIC_RESTO_SHOP+"  CID:"+MQSetting.CID_SHOP);
	}

	@PreDestroy
	public void stopConsumer(){
		consumer.shutdown();
		log.info("消费者关闭！");
	}
}
