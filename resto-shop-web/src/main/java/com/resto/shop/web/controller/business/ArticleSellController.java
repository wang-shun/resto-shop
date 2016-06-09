 package com.resto.shop.web.controller.business;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.brand.core.entity.Result;
import com.resto.brand.web.dto.ArticleSellDto;
import com.resto.brand.web.dto.SaleReportDto;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.service.OrderItemService;
import com.resto.shop.web.service.OrderService;

/**
 * 菜品销售报表
 * @author lmx
 */
@Controller
@RequestMapping("/articleSell")
public class ArticleSellController extends GenericController{
	
	@Resource
	OrderService orderService;
	
	@Resource
	OrderItemService orderItemService;
	
	@Resource
	ShopDetailService shopDetailService;
	
	
	@RequestMapping("/list")
    public void list(){
    }
	
	
	@RequestMapping("/show/{type}")
	public String showModal(@PathVariable("type")String type,String beginDate,String endDate,String shopId,HttpServletRequest request){
		request.setAttribute("beginDate", beginDate);
		request.setAttribute("endDate", endDate);
		if(shopId!=null){
			request.setAttribute("shopId", shopId);
		}
		return "articleSell/"+type;
	}
	
	@RequestMapping("/list_all")
	@ResponseBody
	public SaleReportDto list_all(String beginDate,String endDate){
		SaleReportDto saleReportDto = orderService.selectArticleSumCountByData(beginDate, endDate,getCurrentBrandId());
		return saleReportDto;
	}
	
	
	@RequestMapping("/shop_data")
	@ResponseBody
	public Result shop_data(String beginDate,String endDate,String shopId){
		List<ArticleSellDto> list = orderService.selectShopArticleSellByDate(beginDate, endDate, shopId);
		return getSuccessResult(list);
	}
	
	@RequestMapping("/brand_data")
	@ResponseBody
	public Result brand_data(String beginDate,String endDate){
		List<ArticleSellDto> list = orderService.selectBrandArticleSellByDate(beginDate, endDate);
		return getSuccessResult(list);
	}
}
