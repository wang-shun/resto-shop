 package com.resto.shop.web.controller.business;


import java.util.List;
import java.util.Map;

import javax.annotation.Resource;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


import com.resto.brand.core.entity.Result;
import com.resto.brand.web.service.ShopDetailService;

import com.resto.shop.web.controller.GenericController;

@Controller
@RequestMapping("order")
public class OrderController extends GenericController{

	@Resource
	OrderService orderService;
	
	@Resource
	ShopDetailService shopDetailService;
	
	@RequestMapping("list_push")
	@ResponseBody
	public Result list_push(){
		List<Order> order = orderService.selectPushOrder(getCurrentShopId());
		return getSuccessResult(order);
	}
	
	@RequestMapping("list_ready")
	@ResponseBody
	public Result list_ready(){
		List<Order> order= orderService.selectReadyOrder(getCurrentShopId());
		return getSuccessResult(order);
	}
	
	@RequestMapping("list_call")
	@ResponseBody
	public Result list_call(){
		List<Order> order = orderService.selectCallOrder(getCurrentBrandId());
		return getSuccessResult(order);
	}
	
	@RequestMapping("callNumber")
	@ResponseBody
	public Result callOrder(String orderId){
		orderService.callNumber(orderId);
		return getSuccessResult();
	}
	
	@RequestMapping("/printReceipt")
	@ResponseBody
	public Map<String,Object> printReceipt(String orderId){
		String shopDetailId = getCurrentShopId();
		return orderService.printReceipt(orderId,shopDetailId);
	}

}
