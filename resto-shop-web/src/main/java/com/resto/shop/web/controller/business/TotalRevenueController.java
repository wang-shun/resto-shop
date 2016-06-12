 package com.resto.shop.web.controller.business;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
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
	
	//获取数据封装在map中
	private static final Map<String,List<IncomeReportDto>> incomeMap=new ConcurrentHashMap<>(); //存放查询出来的数据
	
	
	private  List<IncomeReportDto> getIncomeList(){
		
		return orderpaymentitemService.selectIncomeList(getCurrentBrandId());
	}
	
	
	@RequestMapping("/list")
        public void list(){
        }
	
	/**
	 * 封装店铺报表数据
	 * @return
	 */
	@RequestMapping("shopIncome")
	@ResponseBody
	public List<ShopIncomeDto> selectShopReportList(){
		List<ShopDetail> listShop = shopDetailService.selectByBrandId(getCurrentBrandId());
		List<ShopIncomeDto> shopIncomeList = new ArrayList<>();
		Map<String,ShopIncomeDto> hm = new HashMap<>();
		for (int i = 0; i < listShop.size(); i++) {
			ShopIncomeDto sin = new ShopIncomeDto();
			sin.setShopDetailId(listShop.get(i).getId());
			sin.setShopName(listShop.get(i).getName());
			String s = ""+i;
			hm.put(s, sin);
			if(incomeMap.isEmpty()){
				incomeMap.put("income", this.getIncomeList());
			}
			for(IncomeReportDto in : incomeMap.get("income")){
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
		            hm.get(s).setTotalIncome(hm.get(s).getWechatIncome(), hm.get(s).getAccountIncome(), hm.get(s).getCouponIncome());
		        }
		}
			shopIncomeList.add(hm.get(s));
		
		}
		return shopIncomeList;
	}
	/**
	 * 封装品牌报表数据
	 * @return
	 */
	@RequestMapping("brandIncome")
	@ResponseBody
	public List<BrandIncomeDto> selectBrandReportList(){
		Brand brand = brandService.selectById(getCurrentBrandId());
		//品牌报表数据
		List<BrandIncomeDto> brandIncomeList = new ArrayList<>();
		BrandIncomeDto in = new BrandIncomeDto();
		//初始化品牌的信息
		BigDecimal wechatIncome = BigDecimal.ZERO;
		BigDecimal accountIncome = BigDecimal.ZERO;
		BigDecimal couponIncome = BigDecimal.ZERO;
		if(incomeMap.isEmpty()){
			incomeMap.put("income", this.getIncomeList());
		}
		for(IncomeReportDto income : incomeMap.get("income")){
			if(income.getPaymentModeId()==1){
				wechatIncome=wechatIncome.add(income.getPayValue()).setScale(2);
			}else if(income.getPayMentModeId()==2){
				accountIncome=accountIncome.add(income.getPayValue()).setScale(2);
			}else if(income.getPayMentModeId()==3){
				couponIncome=couponIncome.add(income.getPayValue()).setScale(2);
			}
			
		}
		in.setBrandName(brand.getBrandName());
		in.setWechatIncome(wechatIncome);
		in.setAccountIncome(accountIncome);
		in.setCouponIncome(couponIncome);
		in.setTotalIncome(in.getWechatIncome(), in.getAccountIncome(), in.getCouponIncome());
		brandIncomeList.add(in);
		return brandIncomeList;
	}

}
	
	
	
	

