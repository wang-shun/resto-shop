 package com.resto.shop.web.controller.business;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.brand.web.dto.BrandIncome;
import com.resto.brand.web.dto.IncomeReportDto;
import com.resto.brand.web.dto.ShopIncome;
import com.resto.brand.web.model.Brand;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.service.OrderPaymentItemService;

@Controller
@RequestMapping("totalRevenue")
public class TotalRevenueController extends GenericController{
	
	@Resource
	BrandService brandService;
	
	@Resource
	ShopDetailService shopDetailService;
	
	@Resource
	OrderPaymentItemService orderpaymentitemService;
	
	@RequestMapping("/list")
        public void list(){
        }
	
	/**
	 * 用来处理报表的数据
	 * @return
	 */
	@RequestMapping("reportIncome")
	@ResponseBody
	public List<IncomeReportDto> selectIncomeReportList(){
		Brand brand = brandService.selectById(getCurrentBrandId());
		List<ShopDetail> lists = shopDetailService.selectByBrandId(getCurrentBrandId());
		List<IncomeReportDto> incomelist = orderpaymentitemService.selectIncomeReportList(getCurrentBrandId());
		for(ShopDetail shop:lists){
		    for(IncomeReportDto in : incomelist){
		        if(shop.getId().equals(in.getShopDetailId())){
		            in.setShopName(shop.getName());
		            in.setBrandName(brand.getBrandName());
		        }
		    }
		} 
		return incomelist;
	}
	
	

}
	
	
	
	

