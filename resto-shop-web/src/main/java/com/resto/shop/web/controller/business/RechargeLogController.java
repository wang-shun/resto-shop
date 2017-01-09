package com.resto.shop.web.controller.business;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.brand.core.entity.Result;
import com.resto.brand.web.dto.RechargeLogDto;
import com.resto.brand.web.model.ShopDetail;
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
	public Result selectBrandOrShopRecharge(String beginDate,String endDate){
		return this.getResult(beginDate, endDate);
	}

	private Result getResult(String beginDate, String endDate) {
		return getSuccessResult(this.RechargeList(beginDate, endDate));
	}
	
	public Map<String, Object> RechargeList(String beginDate,String endDate){
		//初始化品牌充值记录
		RechargeLogDto brandInit = new RechargeLogDto(getBrandName(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
		RechargeLogDto rechargeLogDto=chargepaymentService.selectRechargeLog(beginDate, endDate,getCurrentBrandId());
		if(rechargeLogDto!=null){
			brandInit.setBrandName(getBrandName());
			brandInit.setRechargeCount(rechargeLogDto.getRechargeCount());
			brandInit.setRechargeCsNum(rechargeLogDto.getRechargeCsNum());
			brandInit.setRechargeGaCsNum(rechargeLogDto.getRechargeGaCsNum());
			brandInit.setRechargeGaNum(rechargeLogDto.getRechargeGaNum());
			brandInit.setRechargeGaSpNum(rechargeLogDto.getRechargeGaSpNum());
			brandInit.setRechargeNum(rechargeLogDto.getRechargeNum());
			brandInit.setRechargePos(rechargeLogDto.getRechargePos());
			brandInit.setRechargeSpNum(rechargeLogDto.getRechargeSpNum());
			brandInit.setRechargeWeChat(rechargeLogDto.getRechargeWeChat());
		}
		
		List<RechargeLogDto> shopRrchargeLogs=new ArrayList<>();
		
		List<ShopDetail> shoplist = getCurrentShopDetails();
        if(!shoplist.isEmpty()){
            shoplist = shopDetailService.selectByBrandId(getCurrentBrandId());
        }
        for (ShopDetail shopDetail : shoplist) {
        	//初始化店铺充值记录
        	RechargeLogDto shopInit=new RechargeLogDto(shopDetail.getId(),shopDetail.getName(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        	System.out.println(shopInit.getShopCsNum());
        	RechargeLogDto shopRechargeLogDto=chargepaymentService.selectShopRechargeLog(beginDate, endDate, shopDetail.getId());
        	shopInit.setShopId(shopDetail.getId());
        	shopInit.setShopName(shopDetail.getName());
        	shopInit.setShopCount(shopRechargeLogDto.getShopCount());
        	shopInit.setShopCsNum(shopRechargeLogDto.getShopCsNum());
        	shopInit.setShopGaNum(shopRechargeLogDto.getShopGaNum());
        	shopInit.setShopGaCsNum(shopRechargeLogDto.getShopGaCsNum());
        	shopInit.setShopNum(shopRechargeLogDto.getShopNum());
        	shopInit.setRechargePos(shopRechargeLogDto.getRechargePos());
        	shopInit.setShopWeChat(shopRechargeLogDto.getShopWeChat());
        	shopRrchargeLogs.add(shopInit);
		}
        
		Map<String, Object> map=new HashMap<>();
		map.put("brandInit", brandInit);
		map.put("shopRrchargeLogs", shopRrchargeLogs);
		
		return map;
	}

	
	
	


}