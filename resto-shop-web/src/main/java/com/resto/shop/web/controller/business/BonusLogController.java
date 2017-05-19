 package com.resto.shop.web.controller.business;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.resto.brand.core.util.WeChatPayUtils;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.model.WechatConfig;
import com.resto.brand.web.model.WxServerConfig;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.brand.web.service.WechatConfigService;
import com.resto.brand.web.service.WxServerConfigService;
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
	
	@RequestMapping("/list")
    public void list(){
    }

	@RequestMapping("/list_all")
	@ResponseBody
	public Result listData(){
	    try {
            Map<String, Object> map = new HashMap<>();
            List<Map<String, Object>> bonusLogs = bonusLogService.selectAllBonusLog();
            List<ShopDetail> shopDetails = getCurrentShopDetails();
            for (Map bonusLog : bonusLogs){
                for (ShopDetail shopDetail : shopDetails){
                    if (bonusLog.get("shopId").toString().equals(shopDetail.getId())){
                        bonusLog.put("shopName", shopDetail.getName());
                        break;
                    }
                }
                if (bonusLog.get("emNames") != null){
                    if (StringUtils.isNotBlank(bonusLog.get("emNames").toString())) {
                        String[] emNames = bonusLog.get("emNames").toString().split(",");
                        for (String emName : emNames) {
                            String[] name = emName.split(":");
                            if (Integer.valueOf(name[0]) == 1) {
                                bonusLog.put("employeeName", name[1]);
                            } else {
                                bonusLog.put("shopownerName", name[1]);
                            }
                        }
                    }
                }
                if (bonusLog.get("employeeId") == null){
                    bonusLog.put("employeeId", "");
                }
                if (bonusLog.get("shopownerId") == null){
                    bonusLog.put("shopownerId", "");
                }
            }
            List<NewEmployee> newEmployees = newEmployeeService.selectList();
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
            map.put("bonusLogs",bonusLogs);
            return getSuccessResult(map);
        }catch (Exception e){
            e.printStackTrace();
            log.error("查看所有分红记录出错");
            return new Result(false);
        }
	}
	
	@RequestMapping("modify")
	@ResponseBody
	public Result modify(String id, String shopownerId, String employeeId){
	    try{
	        BonusLog bonusLog = bonusLogService.selectById(id);
            BonusSetting bonusSetting = bonusSettingService.selectById(bonusLog.getBonusSettingId());
            WechatConfig wechatConfig = wechatConfigService.selectByBrandId(bonusSetting.getBrandId());
            ShopDetail shopDetail = shopDetailService.selectById(bonusSetting.getShopDetailId());
            List<String> employeeIds = new ArrayList<>();
            if (bonusLog.getShopownerBonusAmount() > 0){
                employeeIds.add(shopownerId);
            }
            if (bonusLog.getEmployeeBonusAmount() > 0){
                employeeIds.add(employeeId);
            }
            List<NewEmployee> employees = newEmployeeService.selectByIds(employeeIds);
            if (employees.isEmpty()){
                throw new RuntimeException("未找到发放对象");
            }
            List<String> telePhones = new ArrayList<>();
            for (NewEmployee employee : employees){
                telePhones.add(employee.getTelephone());
            }
            List<Customer> customers = customerService.selectByTelePhones(telePhones);
            if (customers.size() == 1) {
                grantRewards(customers.get(0), bonusLog.getBonusAmount(), wechatConfig, shopDetail);
            }else{
                for (Customer customer : customers){
                    for (NewEmployee employee : employees){
                        if (customer.getTelephone().equalsIgnoreCase(employee.getTelephone())){
                            if (employee.getId().equalsIgnoreCase(shopownerId)){
                                grantRewards(customer, bonusLog.getShopownerBonusAmount(), wechatConfig, shopDetail);
                            }else{
                                grantRewards(customer, bonusLog.getEmployeeBonusAmount(), wechatConfig, shopDetail);
                            }
                            break;
                        }
                    }
                }
            }
            bonusLog.setShopownerId(shopownerId);
            bonusLog.setEmployeeId(employeeId);
            bonusLog.setState(2);
            bonusLogService.update(bonusLog);
            return getSuccessResult();
        }catch (Exception e){
            e.printStackTrace();
            log.error("发放奖励出错");
            return new Result(e.getMessage(),false);
        }
	}

	public void grantRewards(Customer customer, Integer bonusAmount, WechatConfig wechatConfig, ShopDetail shopDetail) throws Exception{
        ChargePayment chargePayment = chargePaymentService.selectPayData(shopDetail.getId());
        String resultData = chargePayment.getPayData();
        boolean isUseChargePay = true;
        OrderPaymentItem paymentItem = null;
        if (StringUtils.isBlank(resultData)){
            paymentItem = orderPaymentItemService.selectWeChatPayResultData(shopDetail.getId());
            resultData = paymentItem.getResultData();
            isUseChargePay = false;
        }
        if (StringUtils.isBlank(resultData)){
            throw new RuntimeException("无微信支付订单作为载体发放现金红包");
        }
        JSONObject resultObject = JSON.parseObject(resultData);
        JSONObject object = new JSONObject();
        object.put("mch_billno",resultObject.get("transaction_id"));
        object.put("re_openid",customer.getWechatId());
        object.put("send_name","上海餐加");
        object.put("wishing","恭喜你获得充值分红");
        object.put("total_amount",100);
        if (shopDetail.getWxServerId() == null){
            object.put("mch_id",wechatConfig.getMchid());
            object.put("wxappid",wechatConfig.getAppid());
            object.put("mch_key",wechatConfig.getMchkey());
            object.put("cert_path","F:/resto/75093c6a-eea2-443b-91a9-a5402bba3c4b.p12");
        }else{
            WxServerConfig serverConfig = wxServerConfigService.selectById(shopDetail.getWxServerId());
            object.put("mch_id",serverConfig.getMchid());
            object.put("wxappid",serverConfig.getAppid());
            object.put("mch_key",serverConfig.getMchkey());
            object.put("cert_path","F:/resto/6b6f99ff-642c-43b1-86e7-349b0f3548c1.p12");
            object.put("consume_mch_id",shopDetail.getMchid());
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
