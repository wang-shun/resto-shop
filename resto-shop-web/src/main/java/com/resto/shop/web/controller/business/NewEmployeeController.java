 package com.resto.shop.web.controller.business;

import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.shop.web.controller.GenericController;
import com.resto.brand.core.entity.Result;
import com.resto.shop.web.model.NewEmployee;
import com.resto.shop.web.service.NewEmployeeService;

@Controller
@RequestMapping("newemployee")
public class NewEmployeeController extends GenericController{

	@Resource
	NewEmployeeService newemployeeService;
	
	@RequestMapping("/list")
    public void list(){
    }

	@RequestMapping("/list_all")
	@ResponseBody
	public List<NewEmployee> listData(){
		return newemployeeService.selectList();
	}
	
	@RequestMapping("list_one")
	@ResponseBody
	public Result list_one(String id){
		NewEmployee newemployee = newemployeeService.selectById(id);
		return getSuccessResult(newemployee);
	}
	
	@RequestMapping("create")
	@ResponseBody
	public Result create(@Valid NewEmployee newemployee){
		newemployeeService.insert(newemployee);
		return Result.getSuccess();
	}
	
	@RequestMapping("modify")
	@ResponseBody
	public Result modify(@Valid NewEmployee newemployee){
		newemployeeService.update(newemployee);
		return Result.getSuccess();
	}
	
	@RequestMapping("delete")
	@ResponseBody
	public Result delete(String id){
		newemployeeService.delete(id);
		return Result.getSuccess();
	}
}
