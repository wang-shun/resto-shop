 package com.resto.shop.web.controller.business;

 import com.resto.brand.core.entity.Result;
 import com.resto.brand.web.model.ShopDetail;
 import com.resto.brand.web.service.ShopDetailService;
 import com.resto.shop.web.constant.ERoleDto;
 import com.resto.shop.web.controller.GenericController;
 import com.resto.shop.web.model.ERole;
 import com.resto.shop.web.model.Employee;
 import com.resto.shop.web.service.ERoleService;
 import com.resto.shop.web.service.EmployeeService;
 import org.apache.commons.collections.map.HashedMap;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;

 import javax.annotation.Resource;
 import javax.validation.Valid;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;

 @Controller
@RequestMapping("employee")
public class EmployeeController extends GenericController{

	@Resource
	EmployeeService employeeService;

	 @Resource
	 ShopDetailService shopDetailService;

	 @Resource
	 ERoleService eRoleService;
	
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
	 public Result assignForm(Long employeeId) throws ReflectiveOperationException{
		// roleService.assignRolePermissions(roleId,pids);
		 return new Result(true);
	 }


	 @RequestMapping(" listAllShopsAndRoles")
	 @ResponseBody
	 public Result listAllShopsAndRoles() {
		//查询所有的店铺
		 List<ShopDetail> shops = shopDetailService.selectByBrandId(getCurrentBrandId());
		//查询出所有的定义的角色
		 List<ERole> eRoles = eRoleService.selectList();
		 //定义一个map封装店铺和 角色的数据
		List<ERoleDto> elist = new ArrayList<>();
		 for (ShopDetail shop : shops) {
			 ERoleDto eDto = new ERoleDto();
			 eDto.setShopId(shop.getId());
			 eDto.setShopName(shop.getName());
			 eDto.seteRolelist(eRoles);
			 elist.add(eDto);
		 }
		 return getSuccessResult(elist);
	 }

}
