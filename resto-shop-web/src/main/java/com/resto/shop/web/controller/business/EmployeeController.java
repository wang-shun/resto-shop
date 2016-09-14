 package com.resto.shop.web.controller.business;

 import com.google.zxing.WriterException;
 import com.resto.brand.core.entity.Result;
 import com.resto.brand.core.util.QRCodeUtil;
 import com.resto.brand.web.model.ShopDetail;
 import com.resto.brand.web.service.ShopDetailService;
 import com.resto.shop.web.constant.ERoleDto;
 import com.resto.shop.web.controller.GenericController;
 import com.resto.shop.web.model.ERole;
 import com.resto.shop.web.model.Employee;
 import com.resto.shop.web.model.EmployeeRole;
 import com.resto.shop.web.service.ERoleService;
 import com.resto.shop.web.service.EmployeeService;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;

 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletResponse;
 import javax.validation.Valid;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;

 @Controller
 @RequestMapping("employee")
 class EmployeeController extends GenericController{

     @Resource
     private EmployeeService employeeService;

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

	@RequestMapping("listOne")
	@ResponseBody
	public  Result listOne(Long employeeId){
			Employee employee = employeeService.selectOneById(employeeId);
			return getSuccessResult(employee);
	}

	 @RequestMapping("listIds")
	 @ResponseBody
	 public  Result listIds(Long employeeId){
		 Employee employee = employeeService.selectOneById(employeeId);
		 Set<String> ids = new HashSet<>();
		 if(employee!=null){
				if(!employee.getEmployeeRoleList().isEmpty()){
						for(EmployeeRole er : employee.getEmployeeRoleList()){
								String id = er.getShopId()+"_"+er.geteRole().getId();
								String shopId = er.getShopId();
								ids.add(id);
								ids.add(shopId);
						}
				}
		 }
		 return getSuccessResult(ids);
	 }


	
	@RequestMapping("addData")
	@ResponseBody
	public Result create(@Valid Employee employee){
		return employeeService.insertOne(employee,getCurrentBrandUser(),getCurrentBrandId());
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
	    //做假删除

		return employeeService.updateEmployee(id);
	}


	@RequestMapping("add")
	public String add(){
		return "employee/save";
	}



	 @RequestMapping("employee_role")
	 public ModelAndView assignPermissions(Long employeeId){

	     //查询出该员工所有店铺的所有角色
        Employee employee =  employeeService.selectOneById(employeeId);

		 //查询出所有的店铺角色
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

		 ModelAndView mv = new ModelAndView("employee/employee_role");
		 mv.addObject("employee", employee);
         mv.addObject("employeeId",employeeId);
		 mv.addObject("elist",elist);

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
	 public Result assignForm(String employeeId,String id) {
         //31164cebcc4b422685e8d9a32db12ab8_1002,31164cebcc4b422685e8d9a32db12ab8_1003

        employeeService.updateSelected(Long.parseLong(employeeId),id,getCurrentBrandUser());
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


	 @RequestMapping("QR")
     @ResponseBody
     public  Result createQR(String employeeId, HttpServletResponse httpResponse) throws WriterException {
         OutputStream out = null;
         try {
             out = httpResponse.getOutputStream();
             QRCodeUtil.createQRCode(String.valueOf(employeeId),"png",out);

         } catch (IOException e) {
             e.printStackTrace();
         }finally {
             if(out==null){
                 try {
                     out.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }

         }
         return  getSuccessResult();

     }


     @RequestMapping("checkeTelephone")
     @ResponseBody
     public  Result checkeTelephone(String telephone){

        return employeeService.checkeTelephone(telephone);

     }


}
