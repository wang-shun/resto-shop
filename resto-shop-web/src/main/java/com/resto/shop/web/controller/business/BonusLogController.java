 package com.resto.shop.web.controller.business;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.resto.brand.core.util.WeChatPayUtils;
import com.resto.brand.web.model.*;
import com.resto.brand.web.service.*;
import com.resto.shop.web.constant.Common;
import com.resto.shop.web.model.*;
import com.resto.shop.web.service.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.shop.web.controller.GenericController;
import com.resto.brand.core.entity.Result;

 @Controller
@RequestMapping("bonusLog")
public class BonusLogController extends GenericController{

	@Resource
	BonusLogService bonusLogService;

    @Resource
    CustomerService customerService;

    @Resource
    ShopDetailService shopDetailService;

    @Resource
    BonusSettingService bonusSettingService;

    @Resource
    NewEmployeeService newEmployeeService;

    @Resource
    WxServerConfigService wxServerConfigService;

    @Resource
    WechatConfigService wechatConfigService;

    @Resource
    ChargePaymentService chargePaymentService;

    @Resource
    OrderPaymentItemService orderPaymentItemService;

     @Resource
     BrandSettingService brandSettingService;

     @RequestMapping("/list")
     public String list(){
         BrandSetting brandSetting = brandSettingService.selectByBrandId(getCurrentBrandId());
         if (brandSetting.getOpenBonus().equals(Common.YES)){
             return "bonusLog/list";
         }else {
             return "notopen";
         }
     }

	@RequestMapping("/list_all")
	@ResponseBody
	public Result listData(){
	    try {
            List<Map<String, Object>> bonusLogs = bonusLogService.selectAllBonusLog(null);
            List<NewEmployee> newEmployees = newEmployeeService.selectByBrandId(getCurrentBrandId());
            List<ShopDetail> shopDetails = getCurrentShopDetails();
            for (Map bonusLog : bonusLogs){
                for (ShopDetail shopDetail : shopDetails){
                    if (bonusLog.get("shopId").toString().equals(shopDetail.getId())){
                        bonusLog.put("shopName", shopDetail.getName());
                        break;
                    }
                }
                if (bonusLog.get("employeeId") == null){
                    bonusLog.put("employeeId", "");
                }else {
                    for (NewEmployee employee : newEmployees){
                        if (employee.getId().equalsIgnoreCase(bonusLog.get("employeeId").toString())){
                            bonusLog.put("employeeName", employee.getName());
                            break;
                        }
                    }
                }
                if (bonusLog.get("shopownerId") == null){
                    bonusLog.put("shopownerId", "");
                }else {
                    for (NewEmployee employee : newEmployees){
                        if (employee.getId().equalsIgnoreCase(bonusLog.get("shopownerId").toString())){
                            bonusLog.put("shopownerName", employee.getName());
                            break;
                        }
                    }
                }
            }
            return getSuccessResult(bonusLogs);
        }catch (Exception e){
            e.printStackTrace();
            log.error("查看所有分红记录出错");
            return new Result(false);
        }
	}

	@RequestMapping("/getShopEmployees")
    @ResponseBody
	public Result getShopEmployees(String shopId){
	    try{
            Map<String, Object> map = new HashMap<>();
            List<NewEmployee> newEmployees = newEmployeeService.selectByShopId(shopId);
            List<NewEmployee> employees = new ArrayList<>();
            List<NewEmployee> shopowners = new ArrayList<>();
            for (NewEmployee newEmployee : newEmployees){
                if (newEmployee.getRoleType() == 1){
                    employees.add(newEmployee);
                }else{
                    shopowners.add(newEmployee);
                }
            }
            map.put("employees",employees);
            map.put("shopowners",shopowners);
	        return getSuccessResult(map);
        }catch (Exception e){
            e.printStackTrace();
            log.error("查看店铺下的员工和店长出错");
            return new Result(false);
        }
    }
	
	@RequestMapping("/modify")
	@ResponseBody
	public Result modify(String id, String shopownerId, String employeeId){
	    boolean isOneCustomer = false;
        boolean isEmployee = true;
        boolean isOneGrant = true;
        boolean isTwoEmpliyee = true;
        BonusLog bonusLog = bonusLogService.selectById(id);
        bonusLog.setShopownerId(shopownerId);
        bonusLog.setEmployeeId(employeeId);
	    try{
	        if (bonusLog.getState() == 2){
	            return new Result("奖励已发放，请勿重新操作", true);
            }
            BonusSetting bonusSetting = bonusSettingService.selectById(bonusLog.getBonusSettingId());
            WechatConfig wechatConfig = wechatConfigService.selectByBrandId(bonusSetting.getBrandId());
            ShopDetail shopDetail = shopDetailService.selectById(bonusSetting.getShopDetailId());
            List<String> employeeIds = new ArrayList<>();
            Integer reissueAmount = 0;
            if (bonusLog.getState() == 0 || bonusLog.getState() == 1) {
                if (bonusLog.getShopownerBonusAmount() > 0) {
                    isEmployee = false;
                    employeeIds.add(shopownerId);
                }
                if (bonusLog.getEmployeeBonusAmount() > 0) {
                    employeeIds.add(employeeId);
                }
            }else{
                if (bonusLog.getShopownerIssuingState() == 1 && bonusLog.getEmployeeIssuingState() == 1){
                    employeeIds.add(shopownerId);
                    employeeIds.add(employeeId);
                }else if (bonusLog.getShopownerIssuingState() == 1){
                    isEmployee = false;
                    employeeIds.add(shopownerId);
                    reissueAmount = bonusLog.getShopownerBonusAmount();
                }else if (bonusLog.getEmployeeIssuingState() == 1){
                    employeeIds.add(employeeId);
                    reissueAmount = bonusLog.getEmployeeBonusAmount();
                }
            }
            if (!employeeIds.isEmpty()) {
                List<NewEmployee> employees = newEmployeeService.selectByIds(employeeIds);
                if (employees.isEmpty()) {
                    throw new RuntimeException("未找到发放对象");
                }
                List<String> telePhones = new ArrayList<>();
                for (NewEmployee employee : employees) {
                    telePhones.add(employee.getTelephone());
                }
                List<Customer> customers = customerService.selectByTelePhones(telePhones);
                if (customers.size() == 1) {
                    isOneCustomer = true;
                    if (employeeIds.size() != 2) {
                        isTwoEmpliyee = false;
                    }
                    grantRewards(customers.get(0), reissueAmount != 0 ? reissueAmount : bonusLog.getBonusAmount(), bonusLog.getWishing(), wechatConfig, shopDetail);
                } else {
                    int i = 0;
                    for (Customer customer : customers) {
                        for (NewEmployee employee : employees) {
                            if (customer.getTelephone().equalsIgnoreCase(employee.getTelephone())) {
                                if (i != 0) {
                                    isOneGrant = false;
                                }
                                if (employee.getId().equalsIgnoreCase(shopownerId)) {
                                    isEmployee = false;
                                    grantRewards(customer, bonusLog.getShopownerBonusAmount(), bonusLog.getWishing(), wechatConfig, shopDetail);
                                } else {
                                    isEmployee = true;
                                    grantRewards(customer, bonusLog.getEmployeeBonusAmount(), bonusLog.getWishing(), wechatConfig, shopDetail);
                                }
                                break;
                            }
                        }
                        i++;
                    }
                }
            }
            bonusLog.setShopownerIssuingState(0);
            bonusLog.setEmployeeIssuingState(0);
            bonusLog.setState(2);
            bonusLogService.update(bonusLog);
            return getSuccessResult();
        }catch (Exception ex){
            try {
                if (isOneCustomer) {
                    if (isTwoEmpliyee) {
                        bonusLog.setShopownerIssuingState(1);
                        bonusLog.setEmployeeIssuingState(1);
                    }else{
                        if (isEmployee) {
                            bonusLog.setEmployeeIssuingState(1);
                        } else {
                            bonusLog.setShopownerIssuingState(1);
                        }
                    }
                } else {
                    if (!isOneGrant) {
                        if (isEmployee) {
                            bonusLog.setEmployeeIssuingState(1);
                        } else {
                            bonusLog.setShopownerIssuingState(1);
                        }
                    } else {
                        bonusLog.setShopownerIssuingState(1);
                        bonusLog.setEmployeeIssuingState(1);
                    }
                }
                bonusLog.setState(3);
                bonusLogService.update(bonusLog);
            }catch (Exception e){
                e.printStackTrace();
                log.error("红包发放异常，修改发放记录出错");
            }
            ex.printStackTrace();
            log.error("发放奖励出错");
            return new Result(ex.getMessage(),false);
        }
	}

	public void grantRewards(Customer customer, Integer bonusAmount, String wishing, WechatConfig wechatConfig, ShopDetail shopDetail) throws Exception{
        boolean isUseChargePay = true;
        String resultData = "";
	    ChargePayment chargePayment = chargePaymentService.selectPayData(shopDetail.getId());
        OrderPaymentItem paymentItem = orderPaymentItemService.selectWeChatPayResultData(shopDetail.getId());;
        if (chargePayment != null){
            resultData = chargePayment.getPayData();
        } else if (paymentItem != null){
            resultData = paymentItem.getResultData();
            isUseChargePay = false;
        }
        if (StringUtils.isBlank(resultData)){
            throw new RuntimeException("无微信支付订单作为载体发放现金红包");
        }
        JSONObject resultObject = JSON.parseObject(resultData);
        Object mch_billno = resultObject.get("transaction_id");
        JSONObject object = new JSONObject();
        object.put("mch_billno", mch_billno);
        object.put("re_openid", customer.getWechatId());
        object.put("send_name", getBrandName());
        object.put("wishing", wishing);
//        object.put("total_amount", bonusAmount * 100);
        object.put("total_amount", 100);
        if (shopDetail.getWxServerId() == null){
            object.put("mch_id", wechatConfig.getMchid());
            object.put("wxappid", wechatConfig.getAppid());
            object.put("mch_key", wechatConfig.getMchkey());
            object.put("cert_path", wechatConfig.getPayCertPath());
        }else{
            WxServerConfig serverConfig = wxServerConfigService.selectById(shopDetail.getWxServerId());
            object.put("mch_id", serverConfig.getMchid());
            object.put("wxappid", serverConfig.getAppid());
            object.put("mch_key", serverConfig.getMchkey());
            object.put("cert_path", serverConfig.getPayCertPath());
            object.put("consume_mch_id", shopDetail.getMchid());
            object.put("msgappid", shopDetail.getAppid());
            object.put("sub_mch_id", shopDetail.getMchid());
        }
        Map<String, String> result = WeChatPayUtils.sendredpack(object);
        if (result.containsKey("ERROR")){
            throw new RuntimeException(result.get("err_code_des"));
        }
        if (isUseChargePay) {
            chargePayment.setIsUseBonus(1);
            chargePaymentService.update(chargePayment);
        }else{
            paymentItem.setIsUseBonus(1);
            orderPaymentItemService.update(paymentItem);
        }
    }
}
