 package com.resto.shop.web.controller.business;

 import com.resto.brand.core.entity.Result;
 import com.resto.brand.core.util.DateUtil;
 import com.resto.brand.web.model.BrandAccount;
 import com.resto.brand.web.model.BrandAccountLog;
 import com.resto.brand.web.service.BrandAccountLogService;
 import com.resto.brand.web.service.BrandAccountService;
 import com.resto.shop.web.controller.GenericController;
 import com.resto.shop.web.dto.BrandAccountManager;
 import com.resto.shop.web.service.CustomerService;
 import com.resto.shop.web.service.SmsLogService;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;

 import javax.annotation.Resource;
 import java.math.BigDecimal;
 import java.util.Date;
 import java.util.List;

 @Controller
 @RequestMapping("brandaccount")
 public class BrandAccountController extends GenericController {

	 @Resource
	 BrandAccountService brandAccountService;

	 @Resource
	 CustomerService customerService;

	 @Resource
	 SmsLogService smsLogService;

	 @Resource
	 BrandAccountLogService brandAccountLogService;


	 @RequestMapping("/list")
	 public void list(){
	 }


	 @RequestMapping("initData")
	 @ResponseBody
	 public Result initData(){
		 BrandAccountManager b = new BrandAccountManager();
		 b.setBrandId(getCurrentBrandId());
		 b.setShopId(getCurrentShopId());
		 BrandAccount brandAccount = brandAccountService.selectByBrandId(getCurrentBrandId());
		 b.setAccountBalance(brandAccount.getAccountBalance());//账户余额
		 b.setBrandAccountId(brandAccount.getId());

		 String beginDate = DateUtil.formatDate(new Date(),"yyyy-MM-dd");
		 String endDate = DateUtil.formatDate(new Date(),"yyyy-MM-dd");

		 List<BrandAccountLog> brandAccountLogList = brandAccountLogService.selectListByBrandIdAndTime(beginDate,endDate,getCurrentBrandId());
		 int registerCustomerNum = 0; //注册用户个数
		 BigDecimal registerCustomerMoney = BigDecimal.ZERO;//注册用户支出

		 if(!brandAccountLogList.isEmpty()){
		 	for(BrandAccountLog blog: brandAccountLogList){
				System.out.println("");

			}
		 }





	 	return null;
	 }


 }
