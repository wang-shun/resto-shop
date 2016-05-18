 package com.resto.shop.web.controller.business;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

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
    public ModelAndView list(){
		ModelAndView mv = new ModelAndView();
		List<ShopDetail> shopDetails = shopDetailService.selectByBrandId(getCurrentBrandId());
		mv.setViewName("smsloginfo/list");;
		mv.addObject("shopDetails", shopDetails);
		return mv;
    }
	
	@ResponseBody
	@RequestMapping("/shopName")
	public List<ShopDetail> queryList(){
		return shopDetailService.selectByBrandId(getCurrentBrandId());	
	}
	
	@ResponseBody
	@RequestMapping("/list_all")
	public List<SmsLog> list_all(){
		return smsLogService.selecByBrandId(getCurrentBrandId());
	}
	
	@ResponseBody
	@RequestMapping("/listByShop")
	public List<SmsLog> listByWhere(@RequestParam("begin")String begin,@RequestParam("end")String end,@RequestParam("shopIds")String shopIds){
		return smsLogService.selectListWhere(begin,end,shopIds) ;
	}
	
}
