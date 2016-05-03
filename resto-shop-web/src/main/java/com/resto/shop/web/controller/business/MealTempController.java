 package com.resto.shop.web.controller.business;

import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.shop.web.controller.GenericController;
import com.resto.brand.core.entity.Result;
import com.resto.shop.web.model.MealTemp;
import com.resto.shop.web.service.MealTempService;

@Controller
@RequestMapping("mealtemp")
public class MealTempController extends GenericController{

	@Resource
	MealTempService mealtempService;
	
	@RequestMapping("/list")
    public void list(){
    }

	@RequestMapping("/list_all")
	@ResponseBody
	public List<MealTemp> listData(){
		return mealtempService.selectList();
	}
	
	@RequestMapping("list_one")
	@ResponseBody
	public Result list_one(Integer id){
		MealTemp mealtemp = mealtempService.selectById(id);
		return getSuccessResult(mealtemp);
	}
	
	@RequestMapping("create")
	@ResponseBody
	public Result create(@Valid MealTemp brand){
		mealtempService.insert(brand);
		return Result.getSuccess();
	}
	
	@RequestMapping("modify")
	@ResponseBody
	public Result modify(@Valid MealTemp brand){
		mealtempService.update(brand);
		return Result.getSuccess();
	}
	
	@RequestMapping("delete")
	@ResponseBody
	public Result delete(Integer id){
		mealtempService.delete(id);
		return Result.getSuccess();
	}
}
