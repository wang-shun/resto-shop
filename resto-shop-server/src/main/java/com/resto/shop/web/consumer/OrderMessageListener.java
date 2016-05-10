package com.resto.shop.web.consumer;

import java.io.UnsupportedEncodingException;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.resto.brand.core.util.MQSetting;
import com.resto.shop.web.constant.OrderState;
import com.resto.shop.web.datasource.DataSourceContextHolder;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.service.OrderService;

@Component
public class OrderMessageListener implements MessageListener{
	Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	OrderService orderService;

	@Override
	public Action consume(Message message, ConsumeContext context) {
		Logger log = LoggerFactory.getLogger(getClass());
		
		log.info("接收到队列消息:"+message.getTag()+"@"+message);
		try {
			return executeMessage(message);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			log.error("字符编码转换错误:"+e.getMessage());
		}
		return Action.CommitMessage;
	}
	
	public Action executeMessage(Message message) throws UnsupportedEncodingException{
		String tag = message.getTag();
		if(tag.equals(MQSetting.TAG_CANCEL_ORDER)){ //取消订单消息
			return executeCancelOrder(message);
		}else if(tag.equals(MQSetting.TAG_AUTO_CONFIRM_ORDER)){
			return executeAutoConfirmOrder(message);
		}else if(tag.equals(MQSetting.TAG_NOT_PRINT_ORDER)){
			return executeChangeProductionState(message);
		}else if(tag.equals(MQSetting.TAG_NOT_ALLOW_CONTINUE)){
			return executeNotAllowContinue(message);
		}
		return Action.CommitMessage;
	}

	private Action executeNotAllowContinue(Message message) throws UnsupportedEncodingException {
		String 	msg = new String(message.getBody(),MQSetting.DEFAULT_CHAT_SET);
		Order order = JSON.parseObject(msg, Order.class);
		DataSourceContextHolder.setDataSourceName(order.getBrandId());
		orderService.updateAllowContinue(order.getId(),false);
		return Action.CommitMessage;
	}

	private Action executeChangeProductionState(Message message) throws UnsupportedEncodingException {
		String 	msg = new String(message.getBody(),MQSetting.DEFAULT_CHAT_SET);
		Order order = JSON.parseObject(msg, Order.class);
		DataSourceContextHolder.setDataSourceName(order.getBrandId());
		orderService.changePushOrder(order);
		return Action.CommitMessage;
	}

	private Action executeAutoConfirmOrder(Message message) throws UnsupportedEncodingException {
		String 	msg = new String(message.getBody(),MQSetting.DEFAULT_CHAT_SET);
		Order order = JSON.parseObject(msg, Order.class);
		DataSourceContextHolder.setDataSourceName(order.getBrandId());
		log.info("执行自动确认逻辑"+order.getId());
		orderService.confirmOrder(order);
		return Action.CommitMessage;
	}

	private Action executeCancelOrder(Message message) throws UnsupportedEncodingException {
		String 	msg = new String(message.getBody(),MQSetting.DEFAULT_CHAT_SET);
		JSONObject obj =JSONObject.parseObject(msg);
		String brandId = obj.getString("brandId");
		DataSourceContextHolder.setDataSourceName(brandId);
		Order order = orderService.selectById(obj.getString("orderId"));
		if(order.getOrderState()==OrderState.SUBMIT){
			log.info("自动取消订单:"+obj.getString("orderId"));
			orderService.cancelOrder(obj.getString("orderId"));
		}else{
			log.info("自动取消订单失败，订单状态不是已提交");
		}
		return Action.CommitMessage;
	}
}
