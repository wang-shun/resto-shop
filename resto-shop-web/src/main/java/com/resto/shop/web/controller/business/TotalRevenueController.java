 package com.resto.shop.web.controller.business;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.brand.web.dto.BrandIncomeDto;
import com.resto.brand.web.dto.IncomeReportDto;
import com.resto.brand.web.dto.ShopIncomeDto;
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
	//封装品牌和店铺收入需要的数据
	
	@RequestMapping("reportIncome")
	@ResponseBody
	public Map<String,Object> selectIncomeReportList(@RequestParam("beginDate")String beginDate,@RequestParam("endDate")String endDate){
		//查询品牌和店铺的收入情况
		List<IncomeReportDto> incomeReportList = orderpaymentitemService.selectIncomeList(getCurrentBrandId(),beginDate,endDate);
		//封装店铺所需要的数据结构
		List<ShopDetail> listShop = shopDetailService.selectByBrandId(getCurrentBrandId());
		List<ShopIncomeDto> shopIncomeList = new ArrayList<>();
		Map<String,ShopIncomeDto> hm = new HashMap<>();
		for (int i = 0; i < listShop.size(); i++) {//实际有多少个店铺显示多少个数据
			ShopIncomeDto sin = new ShopIncomeDto();
			sin.setShopDetailId(listShop.get(i).getId());
			sin.setShopName(listShop.get(i).getName());
			//设置每个店铺初始营业额为零
			BigDecimal temp = BigDecimal.ZERO;
			sin.setWechatIncome(temp);
			sin.setAccountIncome(temp);
			sin.setCouponIncome(temp);
			sin.setTotalIncome(temp, temp, temp);
			String s = ""+i;
			hm.put(s, sin);
			if(!incomeReportList.isEmpty()){
				for(IncomeReportDto in : incomeReportList){
			        if(hm.get(s).getShopDetailId().equals(in.getShopDetailId())){
			            switch (in.getPayMentModeId()) {
						case 1:
							hm.get(s).setWechatIncome(in.getPayValue());
							break;
						case 2:
							hm.get(s).setAccountIncome(in.getPayValue());
							break;
						case 3:
							hm.get(s).setCouponIncome(in.getPayValue());
							break;
						default:
							break;
						}
			            hm.get(s).setTotalIncome(hm.get(s).getWechatIncome(),hm.get(s).getAccountIncome(),hm.get(s).getCouponIncome());
			        }
				}
			}
			shopIncomeList.add(hm.get(s));
		}
		//封装brand所需要的数据结构
		
		Brand brand = brandService.selectById(getCurrentBrandId());
		List<BrandIncomeDto> brandIncomeList = new ArrayList<>();
		BrandIncomeDto in = new BrandIncomeDto();
		//初始化品牌的信息
		BigDecimal wechatIncome = BigDecimal.ZERO;
		BigDecimal accountIncome = BigDecimal.ZERO;
		BigDecimal couponIncome = BigDecimal.ZERO;
		
		if(!incomeReportList.isEmpty()){
			for(IncomeReportDto income : incomeReportList){
				if(income.getPaymentModeId()==1){
					wechatIncome=wechatIncome.add(income.getPayValue()).setScale(2);
				}else if(income.getPayMentModeId()==2){
					accountIncome=accountIncome.add(income.getPayValue()).setScale(2);
				}else if(income.getPayMentModeId()==3){
					couponIncome=couponIncome.add(income.getPayValue()).setScale(2);
				}
			}
		}
		in.setBrandName(brand.getBrandName());
		in.setWechatIncome(wechatIncome);
		in.setAccountIncome(accountIncome);
		in.setCouponIncome(couponIncome);
		in.setTotalIncome(in.getWechatIncome(), in.getAccountIncome(), in.getCouponIncome());
		brandIncomeList.add(in);
		
		Map<String,Object> map = new HashMap<>();
		map.put("shopIncome", shopIncomeList);
		map.put("brandIncome", brandIncomeList);
		return map;
	}

}
	
	
	
	

