package com.resto.shop.web.consumer;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

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
	public void startConsumer(){
		Properties pro= MQSetting.getPropertiesWithAccessSecret();
		String topic =null;
		String cid = null;
		if(System.getenv("debug")==null){
			topic = MQSetting.TOPIC_RESTO_SHOP;
			cid = MQSetting.CID_SHOP;
		}else{
			topic = MQSetting.TEST_TOPIC_ORDERMSG;
			cid = MQSetting.TEST_CID_SHOP;
		}
		pro.setProperty(PropertyKeyConst.ConsumerId, cid);
		Consumer consumer =   ONSFactory.createConsumer(pro);
		consumer.subscribe(topic, MQSetting.TAG_ALL, new MessageListener() {
			@Override
			public Action consume(Message message, ConsumeContext context) {
				try {
					return executeMessage(message);
				} catch (UnsupportedEncodingException e) {
					log.error("字符编码转换错误:"+message.toString());
					log.error(e.getMessage());
				}
				return Action.ReconsumeLater;
			}
		});
	}
	
	@Resource
	OrderService orderService;
	
	public Action executeMessage(Message message) throws UnsupportedEncodingException {
		String tag = message.getTag();
		if(tag.equals(MQSetting.TAG_CANCEL_ORDER)){
			String msg = new String(message.getBody(),MQSetting.DEFAULT_CHAT_SET);
			JSONObject object =JSONObject.parseObject(msg);
			String brandId = object.getString("brandId");
			DataSourceContextHolder.setDataSourceName(brandId);
			
		}
		
		
		
		return null;
	}
}
