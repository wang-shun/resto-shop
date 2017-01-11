package com.resto.shop.web.controller.business;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("recharge")
public class RechargeLogController {

	@RequestMapping("/list")
	public String shopChargeRecorde(){

       return "/recharge/shopchargerecord";
	}




}
