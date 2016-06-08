 package com.resto.shop.web.controller.business;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.brand.core.entity.Result;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.service.OrderItemService;
import com.resto.shop.web.service.OrderPaymentItemService;

@Controller
@RequestMapping("totalRevenue")
public class TotalRevenueController extends GenericController{
	
	@Resource
	OrderPaymentItemService orderPaymentItemService;
	
	@Resource
	OrderItemService orderItemService;
	
	
	@RequestMapping("/list")
    public void list(){
    }
	
	
	@RequestMapping("/orderPaymentItems")
	@ResponseBody
	public Result orderPaymentItems(String beginDate,String endDate){
		//收入条目
		return getSuccessResult(orderPaymentItemService.selectpaymentByPaymentMode(getCurrentShopId(),beginDate,endDate));
	}
	
	@RequestMapping("/orderArticleItems")
	@ResponseBody
	public Result reportList(String beginDate,String endDate){
		//菜品销售记录
		return getSuccessResult(orderItemService.selectSaleArticleByDate(getCurrentShopId() ,beginDate, endDate));
	}
	
	
}
