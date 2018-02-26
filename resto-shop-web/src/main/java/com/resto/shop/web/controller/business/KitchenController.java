package com.resto.shop.web.controller.business;

import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.brand.core.entity.Result;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.Kitchen;
import com.resto.shop.web.service.KitchenService;

@Controller
@RequestMapping("kitchen")
public class KitchenController extends GenericController{

	@Resource
	KitchenService kitchenService;
	
	@RequestMapping("/list")
    public void list(){
    }

	@RequestMapping("/list_all")
	@ResponseBody
	public List<Kitchen> listData(){
		return kitchenService.selectListByShopId(getCurrentShopId());
	}
	
	@RequestMapping("list_one")
	@ResponseBody
	public Result list_one(Integer id){
		Kitchen kitchen = kitchenService.selectById(id);
		return getSuccessResult(kitchen);
	}
	
	@RequestMapping("create")
	@ResponseBody
	public Result create(@Valid Kitchen kitchen){
		kitchen.setShopDetailId(getCurrentShopId());
		kitchenService.insertSelective(kitchen);
		return Result.getSuccess();
	}
	
	@RequestMapping("modify")
	@ResponseBody
	public Result modify(@Valid Kitchen kitchen){
		kitchenService.update(kitchen);
		return Result.getSuccess();
	}
	
	@RequestMapping("delete")
	@ResponseBody
	public Result delete(Integer id){
		kitchenService.delete(id);
		return Result.getSuccess();
	}
}
