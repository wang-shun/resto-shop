 package com.resto.shop.web.controller.business;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.brand.core.entity.Result;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.service.OrderService;

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
	public Map<String,Object> printReceipt(String orderId){
		Map<String,Object> printTask = new HashMap<>();
		ShopDetail shopDetail2 = shopDetailService.selectById(getCurrentShopId());
		Map<String,Object> shopDetail = new HashMap<>();
		shopDetail.put("name", shopDetail2.getName());
		shopDetail.put("address", shopDetail2.getAddress());
		shopDetail.put("phone", shopDetail2.getPhone());
		shopDetail.put("id", shopDetail2.getId());
		printTask = orderService.printReceipt(orderId,shopDetail);
		return printTask;
	}
	
	
	@RequestMapping("testKitchen")
	@ResponseBody
	public Result testKitchen(String orderId){
		return getSuccessResult(orderService.kitchenTest(orderId));
	}
}
