 package com.resto.shop.web.controller.business;

import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.shop.web.controller.GenericController;
import com.resto.brand.core.entity.Result;
import com.resto.shop.web.model.MealTempAttr;
import com.resto.shop.web.service.MealTempAttrService;

@Controller
@RequestMapping("mealtempattr")
public class MealTempAttrController extends GenericController{

	@Resource
	MealTempAttrService mealtempattrService;
	
	@RequestMapping("/list")
    public void list(){
    }

	@RequestMapping("/list_all")
	@ResponseBody
	public List<MealTempAttr> listData(){
		return mealtempattrService.selectList();
	}
	
	@RequestMapping("list_one")
	@ResponseBody
	public Result list_one(Integer id){
		MealTempAttr mealtempattr = mealtempattrService.selectById(id);
		return getSuccessResult(mealtempattr);
	}
	
	@RequestMapping("create")
	@ResponseBody
	public Result create(@Valid MealTempAttr brand){
		mealtempattrService.insert(brand);
		return Result.getSuccess();
	}
	
	@RequestMapping("modify")
	@ResponseBody
	public Result modify(@Valid MealTempAttr brand){
		mealtempattrService.update(brand);
		return Result.getSuccess();
	}
	
	@RequestMapping("delete")
	@ResponseBody
	public Result delete(Integer id){
		mealtempattrService.delete(id);
		return Result.getSuccess();
	}
}
