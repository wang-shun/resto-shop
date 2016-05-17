package com.resto.shop.web.controller.business;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.brand.core.entity.Result;
import com.resto.brand.web.model.SmsChargeOrder;
import com.resto.brand.web.service.SmsChargeOrderService;
import com.resto.shop.web.controller.GenericController;

@Controller
@RequestMapping("smschargeorder")
public class SmsChargeOrderController extends GenericController {
	
	@Resource
	private SmsChargeOrderService smsChargeOrderService;

	@RequestMapping("/list")
	public void smscharge(){
	}
	
	@RequestMapping("/list_all")
	@ResponseBody
	public Result list_all(){
		List<SmsChargeOrder> list = smsChargeOrderService.selectByBrandId(getCurrentBrandId());
		return getSuccessResult(list);
	}
	
	
	public Result smsCharge(){
		
		return getSuccessResult();
	}
}
