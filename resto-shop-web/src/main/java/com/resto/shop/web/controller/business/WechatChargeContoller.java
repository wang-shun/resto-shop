 package com.resto.shop.web.controller.business;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.brand.web.model.Brand;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.ChargePayment;
import com.resto.shop.web.service.ChargePaymentService;

@Controller
@RequestMapping("wechatCharge")
public class WechatChargeContoller extends GenericController{

	@Resource
	ChargePaymentService chargepaymentService;
	
	@Resource
	BrandService brandService;
	
	@Resource
	ShopDetailService shopDetailService;
	
	
	@RequestMapping("/list")
    public void list(){
    }

//	@RequestMapping("/list_all")
//	@ResponseBody
//	public List<ChargePayment> listDataByTime(@RequestParam("begin")String begin,@RequestParam("end")String end){
//		List<ChargePayment> list = chargepaymentService.selectPayListByTime(begin,end);
//		
//		return null;
//	}
	
	@RequestMapping("/list_all")
	@ResponseBody
	public List<ChargePayment> listDataByTime(){
		List<ChargePayment> list = chargepaymentService.selectPayList();
		//查询品牌的名称
		Brand brand = brandService.selectById(getCurrentBrandId());
		for (ChargePayment chargePayment : list) {
			chargePayment.setBrandName(brand.getBrandName());
			//根据订单中存的店铺的id查询店铺的名字
			ShopDetail shopDetail = shopDetailService.selectById(chargePayment.getShopDetailId());
			chargePayment.setShopDetailName(shopDetail.getName());
		}
		return list;
	}
	
}
