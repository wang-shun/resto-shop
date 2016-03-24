 package com.resto.shop.web.controller.business;

import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.shop.web.controller.GenericController;
import com.resto.brand.core.entity.Result;
import com.resto.shop.web.model.Appraise;
import com.resto.shop.web.service.AppraiseService;

@Controller
@RequestMapping("appraise")
public class AppraiseController extends GenericController{

	@Resource
	AppraiseService appraiseService;
	
	@RequestMapping("/list")
    public void list(){
    }

	@RequestMapping("/list_all")
	@ResponseBody
	public List<Appraise> listData(){
		return appraiseService.selectList();
	}
	
	@RequestMapping("list_one")
	@ResponseBody
	public Result list_one(String id){
		Appraise appraise = appraiseService.selectById(id);
		return getSuccessResult(appraise);
	}
	
	@RequestMapping("create")
	@ResponseBody
	public Result create(@Valid Appraise brand){
		appraiseService.insert(brand);
		return Result.getSuccess();
	}
	
	@RequestMapping("modify")
	@ResponseBody
	public Result modify(@Valid Appraise brand){
		appraiseService.update(brand);
		return Result.getSuccess();
	}
	
	@RequestMapping("delete")
	@ResponseBody
	public Result delete(String id){
		appraiseService.delete(id);
		return Result.getSuccess();
	}
}
