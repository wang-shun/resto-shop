 package com.resto.shop.web.controller.business;

 import com.alibaba.fastjson.JSONObject;
 import com.resto.brand.core.alipay.util.AlipaySubmit;
 import com.resto.brand.core.entity.Result;
 import com.resto.brand.core.enums.ChargePayType;
 import com.resto.brand.core.payUtil.PayConfigUtil;
 import com.resto.brand.web.model.AccountChargeOrder;
 import com.resto.brand.web.model.ShopDetail;
 import com.resto.brand.web.service.AccountChargeOrderService;
 import com.resto.brand.web.service.ShopDetailService;
 import com.resto.shop.web.controller.GenericController;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;

 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.validation.Valid;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;

 @Controller
 @RequestMapping("accountchargeorder")
 public class AccountChargeOrderController extends GenericController {

     @Resource
     AccountChargeOrderService accountchargeorderService;

     @Resource
     ShopDetailService shopDetailService;

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
     public Result list_one(String id){
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
     public Result delete(String id){
         accountchargeorderService.delete(id);
         return Result.getSuccess();
     }

     @RequestMapping("charge")
     @ResponseBody
     public void accountCharge(String chargeMoney, String payType, HttpServletRequest request, HttpServletResponse response){
         StringBuilder sb = new StringBuilder();
         sb.append(getCurrentShopId());
         sb.append(",");
         ShopDetail shopDetail = shopDetailService.selectByPrimaryKey(getCurrentShopId());
         sb.append(shopDetail.getName());
         sb.append(",");
         sb.append(getCurrentBrandId());
         sb.append(",");
         sb.append(getBrandName());

         String returnHtml = PayConfigUtil.RETURNHTML;
         AccountChargeOrder accountChargeOrder =accountchargeorderService.saveChargeOrder(getCurrentBrandId(),chargeMoney);//创建充值订单
         if((ChargePayType.ALI_PAY+"").equals(payType)){
             String out_trade_no = accountChargeOrder.getId();
             String show_url = "";///商品展示页面
             String notify_url = getBaseUrl()+PayConfigUtil.ACCOUNT_ALIPAY_NOTIFY_URL;
             String return_url = getBaseUrl()+PayConfigUtil.ACCOUNT_ALIPAY_RETURN_URL;
             String subject = PayConfigUtil.ACCOUNT_SUBJECT;
             Map<String, String> formParame = AlipaySubmit.createFormParame(out_trade_no, subject, chargeMoney, show_url, notify_url, return_url,sb.toString());
             returnHtml = AlipaySubmit.buildRequest(formParame, "post", "确认");
         }
        PayConfigUtil.outprint(returnHtml, response);
     }




 }
