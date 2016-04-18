 package com.resto.shop.web.controller.business;

import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.brand.core.entity.Result;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.service.OrderService;

@Controller
@RequestMapping("order")
public class OrderController extends GenericController{

	@Resource
	OrderService orderService;
	
	@RequestMapping("/list")
    public void list(){
    }

	@RequestMapping("/list_all")
	@ResponseBody
	public List<Order> listData(){
		return orderService.selectList();
	}
	
	@RequestMapping("list_one")
	@ResponseBody
	public Result list_one(String id){
		Order order = orderService.selectById(id);
		return getSuccessResult(order);
	}
	
	@RequestMapping("create")
	@ResponseBody
	public Result create(@Valid Order brand){
		orderService.insert(brand);
		return Result.getSuccess();
	}
	
	@RequestMapping("modify")
	@ResponseBody
	public Result modify(@Valid Order brand){
		orderService.update(brand);
		return Result.getSuccess();
	}
	
	@RequestMapping("delete")
	@ResponseBody
	public Result delete(String id){
		orderService.delete(id);
		return Result.getSuccess();
	}
}
