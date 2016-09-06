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


//	{"permissions":[{"id":100,"text":"菜品管理","children":[{"id":1005,"text":"菜品管理","children":[{"id":10051,"text":"article:add","children":null},{"id":10052,"text":"article:delete","children":null},{"id":10053,"text":"article:edit","children":null}]},{"id":1006,"text":"菜品类型管理","children":[{"id":10061,"text":"articlefamily:add","children":null},{"id":10062,"text":"articlefamily:delete","children":null},{"id":10063,"text":"articlefamily:edit","children":null}]},{"id":1007,"text":"菜品规格管理","children":[{"id":10071,"text":"articleattr:add","children":null},{"id":10072,"text":"articleattr:delete","children":null},{"id":10073,"text":"articleattr:edit","children":null}]},{"id":1027,"text":"菜品供应时间","children":[{"id":10271,"text":"supporttime:add","children":null},{"id":10272,"text":"supporttime:delete","children":null},{"id":10273,"text":"supporttime:edit","children":null}]},{"id":4001,"text":"套餐模板管理","children":[{"id":40011,"text":"mealtemp:add","children":null},{"id":40012,"text":"mealtemp:delete","children":null},{"id":40013,"text":"mealtemp:edit","children":null}]},{"id":300055,"text":"菜品库存管理（未开放）","children":null}]},{"id":102,"text":"店铺设置","children":[{"id":1015,"text":"假期管理","children":[{"id":10151,"text":"freeday:add","children":null},{"id":10152,"text":"freeday:delete","children":null},{"id":10153,"text":"freeday:edit","children":null}]},{"id":1016,"text":"厨房管理","children":[{"id":10161,"text":"kitchen:add","children":null},{"id":10162,"text":"kitchen:delete","children":null},{"id":10163,"text":"kitchen:edit","children":null}]},{"id":1023,"text":"打印机管理","children":[{"id":10231,"text":"printer:add","children":null},{"id":10232,"text":"printer:delete","children":null},{"id":10233,"text":"printer:edit","children":null}]}]},{"id":103,"text":"微信首页管理","children":[{"id":1003,"text":"首页店铺介绍","children":[{"id":10031,"text":"advert:add","children":null},{"id":10032,"text":"advert:delete","children":null},{"id":10033,"text":"advert:edit","children":null}]},{"id":1018,"text":"首页通知","children":[{"id":10181,"text":"notice:add","children":null},{"id":10182,"text":"notice:delete","children":null},{"id":10183,"text":"notice:edit","children":null}]},{"id":1022,"text":"首页图片","children":[{"id":10221,"text":"pictureslider:add","children":null},{"id":10222,"text":"pictureslider:delete","children":null},{"id":10223,"text":"pictureslider:edit","children":null}]},{"id":20016,"text":"店铺详情","children":null},{"id":2001,"text":"评论展示","children":[{"id":20011,"text":"showphoto:add","children":null},{"id":20012,"text":"showphoto:delete","children":null},{"id":20013,"text":"showphoto:edit","children":null}]}]},{"id":300023,"text":"短信管理","children":[{"id":1026,"text":"短信日志","children":[{"id":10261,"text":"smslog:add","children":null},{"id":10262,"text":"smslog:delete","children":null},{"id":10263,"text":"smslog:edit","children":null}]},{"id":300046,"text":"短信记录","children":null},{"id":300024,"text":"店铺短信记录","children":null},{"id":300025,"text":"订单管理","children":null},{"id":300033,"text":"发票管理","children":null}]},{"id":300044,"text":"品牌设置","children":[{"id":300014,"text":"品牌参数设置","children":null},{"id":300017,"text":"品牌用户设置","children":[{"id":300018,"text":"branduser:add","children":null}]},{"id":300045,"text":"附加功能设置","children":null},{"id":300066,"text":"员工管理(待开发)","children":[{"id":300068,"text":"employee:add","children":null},{"id":300069,"text":"employee:modify","children":null},{"id":300070,"text":"employee:delete","children":null},{"id":300076,"text":"employee:assign","children":null}]},{"id":300064,"text":"生成二维码","children":null},{"id":300071,"text":"员工角色管理(待开发)","children":[{"id":300072,"text":"erole:modify","children":null},{"id":300073,"text":"erole:delete","children":null},{"id":300074,"text":"erole:assign","children":null},{"id":300075,"text":"erole:add","children":null}]}]},{"id":300049,"text":"充值记录","children":[{"id":300050,"text":"微信充值记录","children":null}]},{"id":300051,"text":"结算报表","children":[{"id":300058,"text":"订单列表","children":null},{"id":300059,"text":"营业总额","children":null},{"id":300060,"text":"菜品销售报表","children":null},{"id":300061,"text":"评论报表","children":null},{"id":300062,"text":"市场营销报表(未开放)","children":null}]},{"id":10279,"text":"微信活动管理","children":[{"id":1024,"text":"红包管理","children":[{"id":10241,"text":"redconfig:add","children":null},{"id":10242,"text":"redconfig:delete","children":null},{"id":10243,"text":"redconfig:edit","children":null}]},{"id":1010,"text":"充值设置","children":[{"id":10101,"text":"chargesetting:add","children":null},{"id":10102,"text":"chargesetting:delete","children":null},{"id":10103,"text":"chargesetting:edit","children":null}]},{"id":1017,"text":"优惠劵设置","children":[{"id":10171,"text":"newcustomcoupon:add","children":null},{"id":10172,"text":"newcustomcoupon:delete","children":null},{"id":10173,"text":"newcustomcoupon:edit","children":null}]}]}],"hasPermissions":[10232,10062,300075,10271,300074,10151,40012,1017,10173,10031,10233,10063,300076,10272,1003,10152,40013,1018,10181,10032,10241,300068,10071,10273,10153,1022,10182,10183,10033,10242,300069,10072,10161,1023,10221,10051,10243,300070,10073,20011,10162,300018,1024,10222,10052,10261,10101,20012,1010,10163,1026,10223,10053,10262,300072,10102,20013,1015,10171,10231,10061,10263,300073,10103,40011,1016,10172,300046,1005,300014,300050,300017,300058,300045,102,1006,300059,300025,300066,1007,300060,300033,300064,300071,1027,2001,300061,4001,300049,300044,10279]}

	 @RequestMapping("assign")
	 public ModelAndView assignPermissions(Long employeeId){
		 ModelAndView mv = new ModelAndView("employee/assign");
		 mv.addObject("employeeId", employeeId);
		 return mv;
	 }


//	 @RequestMapping("assignData")
//	 @ResponseBody
//	 public AssignJsTreeDto assignData(Long roleId) throws ReflectiveOperationException{
//		 Role role = roleService.selectById(roleId);
//		 Long userGroupId = role.getUserGroupId();
//		 List<Permission> hasPermission = permissionService.selectPermissionsByRoleIdWithOutParent(roleId);
//		 List<Permission> parentPermission = permissionService.selectAllParents(userGroupId);
//		 List<Permission> allPermission = permissionService.selectList(userGroupId);
//		 AssignJsTreeDto dto = AssignJsTreeDto.createPermissionTree(hasPermission,parentPermission,allPermission);
//		 return dto;
//	 }


	 @RequestMapping("assignData")
	 @ResponseBody
	 public Result assignData(Long employeeId) throws ReflectiveOperationException{

		  String s = "\n" +
				  "{\"eRoles\":[\n" +
				  "\t{\"id\":1,\"text\":\"测试店铺电视叫号\",\"children\":[{\"id\":10051,\"text\":\"店长\",\"children\":null},{\"id\":10052,\"text\":\"服务员\",\"children\":null},{\"id\":10053,\"text\":\"经理\",\"children\":null}]},\n" +
				  "\t{\"id\":2,\"text\":\"测试店铺坐下点餐\",\"children\":[{\"id\":10054,\"text\":\"店长\",\"children\":null},{\"id\":10055,\"text\":\"服务员\",\"children\":null},{\"id\":10056,\"text\":\"经理\",\"children\":null}]},\n" +
				  "\t{\"id\":3,\"text\":\"测试店铺扫码\",\"children\":[{\"id\":10057,\"text\":\"店长\",\"children\":null},{\"id\":10058,\"text\":\"服务员\",\"children\":null},{\"id\":10059,\"text\":\"经理\",\"children\":null}]}\t\n" +
				  "],\n" +
				  "\n" +
				  "\"hasERoles\":[10051,10052]\n" +
				  "}";

		 JSONObject js  = JSON.parseObject(s);

		 return  getSuccessResult(js);

	 }


	 @RequestMapping("assign_form")
	 @ResponseBody
	 public Result assignForm(Long employeeId,Long[] pids) throws ReflectiveOperationException{
		// roleService.assignRolePermissions(roleId,pids);
		 return new Result(true);
	 }

}
