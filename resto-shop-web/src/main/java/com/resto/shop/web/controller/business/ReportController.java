 package com.resto.shop.web.controller.business;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.brand.core.entity.Result;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.OrderPaymentItem;
import com.resto.shop.web.service.OrderItemService;
import com.resto.shop.web.service.OrderPaymentItemService;

@Controller
@RequestMapping("report")
public class ReportController extends GenericController{
	
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
		//初始化值，用来在前端显示
		List<OrderPaymentItem> list = new LinkedList<>();
		list.add(new OrderPaymentItem(BigDecimal.ZERO, 1,"微信支付"));
		list.add(new OrderPaymentItem(BigDecimal.ZERO, 6,"充值账户支付"));
		list.add(new OrderPaymentItem(BigDecimal.ZERO, 2,"红包支付"));
		list.add(new OrderPaymentItem(BigDecimal.ZERO, 3,"优惠券支付"));
		list.add(new OrderPaymentItem(BigDecimal.ZERO, 7,"充值赠送支付"));
		List<OrderPaymentItem> olist = orderPaymentItemService.selectpaymentByPaymentMode(getCurrentShopId(),beginDate,endDate);
		//收入条目
		for (OrderPaymentItem od : list) {
			for (OrderPaymentItem orderPaymentItem : olist) {
				if(od.getPaymentModeId().equals(orderPaymentItem.getPaymentModeId())){
					od.setPayValue(orderPaymentItem.getPayValue());
				}
			}
		}
		return getSuccessResult(list);
	}
	
	@RequestMapping("/orderArticleItems")
	@ResponseBody
	public Result reportList(String beginDate,String endDate){
		//菜品销售记录
		return getSuccessResult(orderItemService.selectSaleArticleByDate(getCurrentShopId() ,beginDate, endDate));
	}
	
	
	
	
}
