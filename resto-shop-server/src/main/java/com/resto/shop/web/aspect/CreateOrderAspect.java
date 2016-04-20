package com.resto.shop.web.aspect;

import javax.annotation.Resource;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import com.resto.shop.web.constant.OrderState;
import com.resto.shop.web.datasource.DataSourceContextHolder;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.producer.MQMessageProducer;
import com.resto.shop.web.service.ShopCartService;

@Component
@Aspect
public class CreateOrderAspect {
	
	@Resource
	ShopCartService shopCartService;
	
	@Pointcut("execution(* com.resto.shop.web.service.OrderService.createOrder(..))")
	public void createOrder(){};
	
	@AfterReturning(value="createOrder()",returning="order")
	public void createOrderAround(Order order) throws Throwable{
		shopCartService.clearShopCart(order.getCustomerId(),order.getDistributionModeId(),order.getShopDetailId());
		if(order.getOrderState().equals(OrderState.SUBMIT)){
			long delay = 1000*60*5;//
			MQMessageProducer.sendAutoCloseMsg(order.getId(),DataSourceContextHolder.getDataSourceName(),delay);
		}
	}
}
