 package com.resto.shop.web.controller.business;

 import com.resto.brand.core.alipay.util.AlipaySubmit;
 import com.resto.brand.core.entity.Result;
 import com.resto.brand.core.enums.PayType;
 import com.resto.brand.web.model.AccountChargeOrder;
 import com.resto.brand.web.model.SmsChargeOrder;
 import com.resto.brand.web.service.AccountChargeOrderService;
 import com.resto.shop.web.controller.GenericController;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;

 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.validation.Valid;
 import java.math.BigDecimal;
 import java.util.List;
 import java.util.Map;

 @Controller
 @RequestMapping("accountchargeorder")
 public class AccountChargeOrderController extends GenericController {

     @Resource
     AccountChargeOrderService accountchargeorderService;

     @RequestMapping("/list")
     public void list(){
     }

     @RequestMapping("/list_all")
     @ResponseBody
     public List<AccountChargeOrder> listData(){
         return accountchargeorderService.selectList();
     }

     @RequestMapping("list_one")
     @ResponseBody
     public Result list_one(Long id){
         AccountChargeOrder accountchargeorder = accountchargeorderService.selectById(id);
         return getSuccessResult(accountchargeorder);
     }

     @RequestMapping("create")
     @ResponseBody
     public Result create(@Valid AccountChargeOrder brand){
         accountchargeorderService.insert(brand);
         return Result.getSuccess();
     }

     @RequestMapping("modify")
     @ResponseBody
     public Result modify(@Valid AccountChargeOrder brand){
         accountchargeorderService.update(brand);
         return Result.getSuccess();
     }

     @RequestMapping("delete")
     @ResponseBody
     public Result delete(Long id){
         accountchargeorderService.delete(id);
         return Result.getSuccess();
     }

     @RequestMapping("charge")
     @ResponseBody
     public void accountCharge(BigDecimal chargeMoney, Integer payType, HttpServletRequest request, HttpServletResponse response){
         String returnHtml = "<h1>参数错误！</h1>";

         AccountChargeOrder accountChargeOrder =accountchargeorderService.saveChargeOrder(getCurrentBrandId(), chargeMoney,payType);//创建充值订单
         String out_trade_no = accountChargeOrder.getId();
         String show_url = "";///商品展示页面
         String notify_url = getBaseUrl()+"paynotify/alipay_notify";
         String return_url = getBaseUrl()+"paynotify/alipay_return";
         String subject = "【餐加】短信充值";
         Map<String, String> formParame = AlipaySubmit.createFormParame(out_trade_no, subject, chargeMoney.toString(), show_url, notify_url, return_url, null);
         returnHtml = AlipaySubmit.buildRequest(formParame, "post", "确认");

     }



 }
