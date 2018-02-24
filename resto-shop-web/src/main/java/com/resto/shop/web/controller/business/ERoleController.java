package com.resto.shop.web.controller.business;

import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;

import com.resto.shop.web.model.ERole;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.shop.web.controller.GenericController;
import com.resto.brand.core.entity.Result;
import com.resto.shop.web.service.ERoleService;

@Controller
@RequestMapping("erole")
public class ERoleController extends GenericController{

	@Resource
	ERoleService eroleService;
	
	@RequestMapping("/list")
    public void list(){
    }

	@RequestMapping("/list_all")
	@ResponseBody
	public List<ERole> listData(){
		return eroleService.selectList();
	}
	
	@RequestMapping("list_one")
	@ResponseBody
	public Result list_one(Long id){
		ERole erole = eroleService.selectById(id);
		return getSuccessResult(erole);
	}
	
	@RequestMapping("create")
	@ResponseBody
	public Result create(@Valid ERole erole){
		eroleService.insert(erole);
		return Result.getSuccess();
	}
	
	@RequestMapping("modify")
	@ResponseBody
	public Result modify(@Valid ERole erole){
		eroleService.update(erole);
		return Result.getSuccess();
	}
	
	@RequestMapping("delete")
	@ResponseBody
	public Result delete(Long id){
		eroleService.delete(id);
		return Result.getSuccess();
	}
}
