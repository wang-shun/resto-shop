 package com.resto.shop.web.controller.business;

import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;

import com.resto.brand.web.model.Role;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.shop.web.controller.GenericController;
import com.resto.brand.core.entity.Result;
import com.resto.shop.web.model.Employee;
import com.resto.shop.web.service.EmployeeService;

@Controller
@RequestMapping("employee")
public class EmployeeController extends GenericController{

	@Resource
	EmployeeService employeeService;
	
	@RequestMapping("/list")
    public void list(){
    }


	@RequestMapping("/list_all")
	@ResponseBody
	public List<Employee> listData(){
		return employeeService.selectList();
	}
	
	@RequestMapping("list_one")
	@ResponseBody
	public Result list_one(Long id){
		Employee employee = employeeService.selectById(id);
		return getSuccessResult(employee);
	}
	
	@RequestMapping("addData")
	@ResponseBody
	public Result create(@Valid Employee employee){
		employeeService.insertOne(employee,getCurrentBrandUser());
		return Result.getSuccess();
	}
	
	@RequestMapping("modify")
	@ResponseBody
	public Result modify(@Valid Employee employee){
		employeeService.update(employee);
		return Result.getSuccess();
	}
	
	@RequestMapping("delete")
	@ResponseBody
	public Result delete(Long id){
		employeeService.delete(id);
		return Result.getSuccess();
	}


	@RequestMapping("add")
	public String add(){
		return "employee/save";
	}


}
