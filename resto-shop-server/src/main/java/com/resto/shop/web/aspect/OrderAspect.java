package com.resto.shop.web.aspect;

import javax.annotation.Resource;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.resto.brand.core.util.WeChatUtils;
import com.resto.brand.web.model.WechatConfig;
import com.resto.brand.web.service.WechatConfigService;
import com.resto.shop.web.constant.OrderState;
import com.resto.shop.web.constant.ProductionStatus;
import com.resto.shop.web.container.OrderProductionStateContainer;
import com.resto.shop.web.datasource.DataSourceContextHolder;
import com.resto.shop.web.model.Customer;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.producer.MQMessageProducer;
import com.resto.shop.web.service.CustomerService;
import com.resto.shop.web.service.ShopCartService;

@Component
@Aspect
public class OrderAspect {
	
	Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	ShopCartService shopCartService;
	@Resource
	CustomerService customerService;
	@Resource
	WechatConfigService wechatConfigService;
	
	@Resource
	OrderProductionStateContainer orderProductionStateContainer;
	
	@Pointcut("execution(* com.resto.shop.web.service.OrderService.createOrder(..))")
	public void createOrder(){};
	
	@AfterReturning(value="createOrder()",returning="order")
	public void createOrderAround(Order order) throws Throwable{
		shopCartService.clearShopCart(order.getCustomerId(),order.getDistributionModeId(),order.getShopDetailId());
		if(order.getOrderState().equals(OrderState.SUBMIT)){
			long delay = 1000*60*5;//
			MQMessageProducer.sendAutoCloseMsg(order.getId(),DataSourceContextHolder.getDataSourceName(),delay);
		}else if(order.getOrderState().equals((OrderState.PAYMENT))){
			sendPaySuccessMsg(order);
		}
	}
	
	private void sendPaySuccessMsg(Order order) {
		Customer customer = customerService.selectById(order.getCustomerId());
		WechatConfig config= wechatConfigService.selectByBrandId(customer.getBrandId());
		StringBuffer msg = new StringBuffer("test");
		//TODO 订单完成后 msg 的填充
		try {
			String result = WeChatUtils.sendCustomerMsg(msg.toString(),customer.getWechatId(),config.getAppid(),config.getAppsecret());
			log.info("订单支付完成后，发送客服消息:"+result);
		} catch (Exception e) {
			log.error("发送客服消息失败:"+e.getMessage());
		}
	}

	@Pointcut("execution(* com.resto.shop.web.service.OrderService.orderWxPaySuccess(..))")
	public void orderWxPaySuccess(){};
	
	@AfterReturning(value="orderWxPaySuccess()",returning="order")
	public void orderPayAfter(Order order){
		if(order!=null&&order.getOrderState().equals(OrderState.PAYMENT)){
			sendPaySuccessMsg(order);
		}
	}
	
	@Pointcut("execution(* com.resto.shop.web.service.OrderService.pushOrder(..))")
	public void pushOrder(){};
	@Pointcut("execution(* com.resto.shop.web.service.OrderService.callNumber(..))")
	public void callNumber(){};
	@Pointcut("execution(* com.resto.shop.web.service.OrderService.printSuccess(..))")
	public void printSuccess(){};
	
	
	@AfterReturning(value="pushOrder()||callNumber()||printSuccess()",returning="order")
	public void pushOrderAfter(Order order){
		if(order!=null){
			orderProductionStateContainer.addOrder(order);
			if(ProductionStatus.HAS_ORDER==order.getProductionStatus()){
				sendVerCodeMsg(order);
			}
		}
	}

	
	
	
	
	private void sendVerCodeMsg(Order order) {
		Customer customer = customerService.selectById(order.getCustomerId());
		WechatConfig config= wechatConfigService.selectByBrandId(customer.getBrandId());
		StringBuffer msg = new StringBuffer();
		msg.append("取餐码:"+order.getVerCode()+"\n");
		msg.append("请留意餐厅叫号信息");
		String result = WeChatUtils.sendCustomerMsg(msg.toString(), customer.getWechatId(), config.getAppid(), config.getAppsecret());
		log.info("发送取餐信息成功:"+result);
	}
	
	
}
