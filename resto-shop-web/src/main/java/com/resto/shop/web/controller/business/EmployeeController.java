 package com.resto.shop.web.controller.business;

import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.resto.brand.core.entity.JSONResult;
import com.resto.brand.web.dto.AssignJsTreeDto;
import com.resto.brand.web.model.Permission;
import com.resto.brand.web.model.Role;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.shop.web.controller.GenericController;
import com.resto.brand.core.entity.Result;
import com.resto.shop.web.model.Employee;
import com.resto.shop.web.service.EmployeeService;
import org.springframework.web.servlet.ModelAndView;

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



	 @RequestMapping("employee_role")
	 public ModelAndView assignPermissions(Long employeeId){
		 ModelAndView mv = new ModelAndView("employee/employee_role");
		 mv.addObject("employeeId", employeeId);
		 return mv;
	 }


//	 @RequestMapping("assignData")
//	 @ResponseBody
//	 public Result assignData(Long employeeId) throws ReflectiveOperationException{
//
//		  String s = "\n" +
//				  "{\"eRoles\":[\n" +
//				  "\t{\"id\":1,\"text\":\"测试店铺电视叫号\",\"children\":[{\"id\":10051,\"text\":\"店长\",\"children\":null},{\"id\":10052,\"text\":\"服务员\",\"children\":null},{\"id\":10053,\"text\":\"经理\",\"children\":null}]},\n" +
//				  "\t{\"id\":2,\"text\":\"测试店铺坐下点餐\",\"children\":[{\"id\":10054,\"text\":\"店长\",\"children\":null},{\"id\":10055,\"text\":\"服务员\",\"children\":null},{\"id\":10056,\"text\":\"经理\",\"children\":null}]},\n" +
//				  "\t{\"id\":3,\"text\":\"测试店铺扫码\",\"children\":[{\"id\":10057,\"text\":\"店长\",\"children\":null},{\"id\":10058,\"text\":\"服务员\",\"children\":null},{\"id\":10059,\"text\":\"经理\",\"children\":null}]}\t\n" +
//				  "],\n" +
//				  "\n" +
//				  "\"hasERoles\":[10051,10052]\n" +
//				  "}";
//
//		 JSONObject js  = JSON.parseObject(s);
//
//		 return  getSuccessResult(js);
//
//	 }


	 @RequestMapping("assign_form")
	 @ResponseBody
	 public Result assignForm(Long employeeId,Long[] pids) throws ReflectiveOperationException{
		// roleService.assignRolePermissions(roleId,pids);
		 return new Result(true);
	 }

}
