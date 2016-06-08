 package com.resto.shop.web.controller.business;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.brand.core.entity.Result;
import com.resto.brand.web.dto.SaleReportDto;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.service.OrderItemService;
import com.resto.shop.web.service.OrderService;

/**
 * 菜品销售报表
 * @author lmx
 */
@Controller
@RequestMapping("articleSell")
public class ArticleSellController extends GenericController{
	
	@Resource
	OrderService orderService;
	
	@Resource
	OrderItemService orderItemService;
	
	
	@RequestMapping("/list")
    public void list(){
    }
	
	
	@RequestMapping("show/{type}")
	public String showModal(@PathVariable("type")String type){
		return "articleSell/"+type;
	}
	
	@RequestMapping("list_all")
	@ResponseBody
	public Result list_all(String beginDate,String endDate){
		List<SaleReportDto> list = orderService.selectArticleSumCountByData(beginDate, endDate);
		return getSuccessResult(list);
	}
	
}
