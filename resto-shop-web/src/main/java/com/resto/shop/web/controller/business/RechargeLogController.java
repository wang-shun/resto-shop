package com.resto.shop.web.controller.business;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.brand.web.dto.RechargeLogDto;
import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.service.ChargePaymentService;
import com.resto.shop.web.service.CustomerService;


@Controller
@RequestMapping("recharge")

public class RechargeLogController extends GenericController{
	@Resource
	ChargePaymentService chargepaymentService;

	@Resource
	BrandService brandService;

	@Resource
	ShopDetailService shopDetailService;
	
	@Resource
	private CustomerService customerService;
	
	@RequestMapping("/list")
	public void list(){
	}
	
	@RequestMapping("/rechargeLog")
	@ResponseBody
	public Map<String, Object> RechargeList(String beginDate,String endDate){
		
		RechargeLogDto rechargeLogDto=chargepaymentService.selectRechargeLog(beginDate, endDate,getCurrentBrandId());
		rechargeLogDto.setBrandName(getBrandName());
		
		Map<String, Object> map=new HashMap<>();
		map.put("rechargeLogDto", rechargeLogDto);
		
		return map;
	}

	


}