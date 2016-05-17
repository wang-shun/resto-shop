 package com.resto.shop.web.controller.business;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.brand.core.entity.Result;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.SmsLog;
import com.resto.shop.web.service.OrderItemService;
import com.resto.shop.web.service.OrderPaymentItemService;
import com.resto.shop.web.service.SmsLogService;

@Controller
@RequestMapping("smsloginfo")
public class SmsLogoInfoController extends GenericController{
	
	@Resource
	OrderPaymentItemService orderPaymentItemService;
	
	@Resource
	OrderItemService orderItemService;
	
	@Resource
	ShopDetailService shopDetailService;
	
	@Resource
	SmsLogService smsLogService;
	
	
	@RequestMapping("/list")
    public void list(){
    }
	
	@ResponseBody
	@RequestMapping("/shopName")
	public List<ShopDetail> queryList(){
		return shopDetailService.selectByBrandId(getCurrentBrandId());	
	}
	
	@ResponseBody
	@RequestMapping("/list_all")
	public List<SmsLog> list_all(){
		
		return smsLogService.selectList();
	}
	
	@ResponseBody
	@RequestMapping("/listByShop")
	public List<SmsLog> listByWhere(List<String> shopIds,Date begin,Date end){
		return smsLogService.selectListWhere(shopIds,begin,end) ;
		
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
