 package com.resto.shop.web.controller.business;




import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.brand.web.dto.OrderPayDto;
import com.resto.brand.web.model.Brand;
import com.resto.brand.web.service.BrandService;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.service.OrderService;

@Controller
@RequestMapping("orderReport")
public class OrderController extends GenericController{
	
	@Resource
	private OrderService orderService;
	
	@Resource
	private BrandService brandService;
	
	@RequestMapping("/list")
    public void list(){
    }
	
	//查询已消费订单的订单份数和订单金额
	@ResponseBody
	@RequestMapping("orderPaymentItems")
	public List<OrderPayDto> orderPaymentItems(String beginDate,String endDate){
		
		//茶品牌名字
		Brand brand = brandService.selectById(getCurrentBrandId());
		OrderPayDto op= orderService.selectBytimeAndState(beginDate,endDate,getCurrentBrandId());
		op.setBrandName(brand.getBrandName());
		List<OrderPayDto> list = new ArrayList<>();
		list.add(op);
		return list;
	}
}
