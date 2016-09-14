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
import com.resto.shop.web.model.RolePermission;
import com.resto.shop.web.service.RolePermissionService;

@Controller
@RequestMapping("rolepermission")
public class RolePermissionController extends GenericController{

	@Resource
	RolePermissionService rolepermissionService;
	
	@RequestMapping("/list")
    public void list(){
    }

	@RequestMapping("/list_all")
	@ResponseBody
	public Result listData(){
		return rolepermissionService.selectRolePermissionList();
	}
	
	@RequestMapping("list_one")
	@ResponseBody
	public Result list_one(Long id){
		RolePermission rolepermission = rolepermissionService.selectById(id);
		return getSuccessResult(rolepermission);
	}
	
	@RequestMapping("create")
	@ResponseBody
	public Result create(@Valid RolePermission rolepermission){
		rolepermissionService.insert(rolepermission);
		return Result.getSuccess();
	}
	
	@RequestMapping("modify")
	@ResponseBody
	public Result modify(@Valid RolePermission rolepermission){
		rolepermissionService.update(rolepermission);
		return Result.getSuccess();
	}
	
	@RequestMapping("delete")
	@ResponseBody
	public Result delete(Long id){
		rolepermissionService.delete(id);
		return Result.getSuccess();
	}
}
