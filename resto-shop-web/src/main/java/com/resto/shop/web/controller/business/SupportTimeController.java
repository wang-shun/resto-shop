 package com.resto.shop.web.controller.business;

import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.shop.web.controller.GenericController;
import com.resto.brand.core.entity.Result;
import com.resto.shop.web.model.SupportTime;
import com.resto.shop.web.service.SupportTimeService;

@Controller
@RequestMapping("supporttime")
public class SupportTimeController extends GenericController{

	@Resource
	SupportTimeService supporttimeService;
	
	@RequestMapping("/list")
    public void list(){
    }

	@RequestMapping("/list_all")
	@ResponseBody
	public List<SupportTime> listData(){
		return supporttimeService.selectList();
	}
	
	@RequestMapping("list_one")
	@ResponseBody
	public Result list_one(Integer id){
		SupportTime supporttime = supporttimeService.selectById(id);
		return getSuccessResult(supporttime);
	}
	
	@RequestMapping("create")
	@ResponseBody
	public Result create(@Valid SupportTime brand){
		supporttimeService.insert(brand);
		return Result.getSuccess();
	}
	
	@RequestMapping("modify")
	@ResponseBody
	public Result modify(@Valid SupportTime brand){
		supporttimeService.update(brand);
		return Result.getSuccess();
	}
	
	@RequestMapping("delete")
	@ResponseBody
	public Result delete(Integer id){
		supporttimeService.delete(id);
		return Result.getSuccess();
	}
}
