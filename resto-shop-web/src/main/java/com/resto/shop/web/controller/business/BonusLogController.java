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
	    boolean isOneCustomer = false;//标识是否是向一个人发放
        boolean isEmployee = true;//标识当前是否在为员工发放
        boolean isOneGrant = true;//标识是否是第一次进行发放
        boolean isTwoEmpliyee = true;//标识员工、店长是否是同一个人
        //将当前分红查询出老
        BonusLog bonusLog = bonusLogService.selectById(id);
        //设置要发放的店长
        bonusLog.setShopownerId(shopownerId);
        //要发放的员工
        bonusLog.setEmployeeId(employeeId);
	    try{
	        //判断是否已经发放过，如已发放过则返回
	        if (bonusLog.getState() == 2){
	            return new Result("奖励已发放，请勿重新操作", true);
            }
            //得到该笔分红所对应的分红设置
            BonusSetting bonusSetting = bonusSettingService.selectById(bonusLog.getBonusSettingId());
	        //得到该品牌的微信配置
            WechatConfig wechatConfig = wechatConfigService.selectByBrandId(bonusSetting.getBrandId());
            //得到该笔分红设置所对应的店铺
            ShopDetail shopDetail = shopDetailService.selectById(bonusSetting.getShopDetailId());
            //存储要发放对象
            List<String> employeeIds = new ArrayList<>();
            //店长和员工是同一个人的时候存储发放金额的和
            Integer reissueAmount = 0;
            //如果是未分红或审核中的情况下
            if (bonusLog.getState() == 0 || bonusLog.getState() == 1) {
                //判断店长是否有分红金额，有则将店长加到要发放的对象中
                if (bonusLog.getShopownerBonusAmount() > 0) {
                    isEmployee = false;
                    employeeIds.add(shopownerId);
                }
                //判断员工是否有分红金额，有则将员工加到发放的对象在
                if (bonusLog.getEmployeeBonusAmount() > 0) {
                    employeeIds.add(employeeId);
                }
            }else{
                //发放异常补发红包
                if (bonusLog.getShopownerIssuingState() == 1 && bonusLog.getEmployeeIssuingState() == 1){
                    //店长、员工都发放异常
                    employeeIds.add(shopownerId);
                    employeeIds.add(employeeId);
                }else if (bonusLog.getShopownerIssuingState() == 1){
                    //店长发放异常，加入到发放对象中
                    isEmployee = false;
                    employeeIds.add(shopownerId);
                    reissueAmount = bonusLog.getShopownerBonusAmount();
                }else if (bonusLog.getEmployeeIssuingState() == 1){
                    //员工发放异常，加入到发放对象中
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
                //找到发放对象所对应的微信用户
                List<Customer> customers = customerService.selectByTelePhones(telePhones);
                if (customers.size() == 1) {//如果微信用户是同一个人
                    isOneCustomer = true;//给标识赋值，表示为一个人
                    if (employeeIds.size() != 2) {
                        isTwoEmpliyee = false;//给标识赋值，标识为员工、店长是同一个人
                    }
                    //发放金额
                    Integer amountOfPayment = 0;
                    if (reissueAmount != 0){
                        //reissueAmount不为0说明有员工或店长发放失败重新补发
                        amountOfPayment = reissueAmount;
                    }else {
                        //直接向微信用户发放该笔分红的总金额
                        amountOfPayment = bonusLog.getBonusAmount();
                    }
                    //得到要发放的微信用户信息
                    Customer customer = customers.get(0);
                    grantRewards(customer, amountOfPayment, bonusLog.getWishing(), wechatConfig, shopDetail);
                } else {
                    int i = 0;//标识是在第一次发的
                    for (Customer customer : customers) {
                        for (NewEmployee employee : employees) {
                            if (customer.getTelephone().equalsIgnoreCase(employee.getTelephone())) {
                                if (i != 0) {
                                    isOneGrant = false;//给标识赋值，标识当前是第二次在发
                                }
                                if (employee.getId().equalsIgnoreCase(shopownerId)) {//店长
                                    isEmployee = false;//给标识赋值，表示当前正在为店长发
                                    grantRewards(customer, bonusLog.getShopownerBonusAmount(), bonusLog.getWishing(), wechatConfig, shopDetail);
                                } else {//员工
                                    isEmployee = true;//给标识赋值，表示当前正在为员工发
                                    grantRewards(customer, bonusLog.getEmployeeBonusAmount(), bonusLog.getWishing(), wechatConfig, shopDetail);
                                }
                                break;
                            }
                        }
                        i++;
                    }
                }
            }
            //发放成功修改分红状态
            bonusLog.setShopownerIssuingState(0);
            bonusLog.setEmployeeIssuingState(0);
            bonusLog.setState(2);
            bonusLogService.update(bonusLog);
            return getSuccessResult();
        }catch (Exception ex){
            try {
                if (isOneCustomer) {
                    //是向一个人发放
                    if (isTwoEmpliyee) {//店长、员工是同一个人的情况下
                        bonusLog.setShopownerIssuingState(1);
                        bonusLog.setEmployeeIssuingState(1);
                    }else{
                        if (isEmployee) {//正在为员工发放
                            bonusLog.setEmployeeIssuingState(1);
                        } else {//正在为店长发放
                            bonusLog.setShopownerIssuingState(1);
                        }
                    }
                } else {
                    //是向两个人发放
                    if (!isOneGrant) {
                        //是第二次进行发放
                        if (isEmployee) {//当时正在为员工发放
                            //将员工变成发放失败
                            bonusLog.setEmployeeIssuingState(1);
                            //店长变成发放成功
                            bonusLog.setShopownerIssuingState(0);
                        } else {//当时正在为店长进行发放
                            //将店长设成发放失败
                            bonusLog.setShopownerIssuingState(1);
                            //将员工设成发放成功
                            bonusLog.setEmployeeIssuingState(0);
                        }
                    } else {
                        //是第一次发放
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
	    //用来标识使用的是谁的微信订单 true：充值产生的微信支付订单  false：下单时产生的微信支付订单
        boolean isUseChargePay = true;
        //接收微信的订单信息
        String resultData = "";
        //查询充值时产生的微信支付订单
	    ChargePayment chargePayment = chargePaymentService.selectPayData(shopDetail.getId());
	    //查询下单时产生的微信支付订单
        OrderPaymentItem paymentItem = orderPaymentItemService.selectWeChatPayResultData(shopDetail.getId());;
        if (chargePayment != null){
            //如果有充值的微信订单就用充值的微信订单
            resultData = chargePayment.getPayData();
        } else if (paymentItem != null){
            //没有充值的微信订单的情况下使用下单是产生的微信订单
            resultData = paymentItem.getResultData();
            isUseChargePay = false;
        }
        if (StringUtils.isBlank(resultData)){
            throw new RuntimeException("无微信支付订单作为载体发放现金红包");
        }
        JSONObject resultObject = JSON.parseObject(resultData);
        //得到微信商户的订单号
        Object mch_billno = resultObject.get("transaction_id");
        //封装参数
        JSONObject object = new JSONObject();
        object.put("mch_billno", mch_billno);//商户订单号
        object.put("re_openid", customer.getWechatId());//发放目标的openId
        object.put("send_name", getBrandName());//商户名称
        object.put("wishing", wishing);//红包祝福语
//        object.put("total_amount", bonusAmount * 100);
        object.put("total_amount", 100);//发放金额(以分为单位)
        if (shopDetail.getWxServerId() == null){
            object.put("mch_id", wechatConfig.getMchid());//商户号
            object.put("wxappid", wechatConfig.getAppid());//公众号appId
            object.put("mch_key", wechatConfig.getMchkey());//商户支付密钥
            object.put("cert_path", wechatConfig.getPayCertPath());//API证书地址
//            object.put("cert_path", "F:/resto/75093c6a-eea2-443b-91a9-a5402bba3c4b.p12");
        }else{
            WxServerConfig serverConfig = wxServerConfigService.selectById(shopDetail.getWxServerId());
            object.put("mch_id", serverConfig.getMchid());//服务商商户号
            object.put("wxappid", serverConfig.getAppid());//服务商公众号appId
            object.put("mch_key", serverConfig.getMchkey());//服务商商户支付密钥
            object.put("cert_path", serverConfig.getPayCertPath());//服务商API证书
//            object.put("cert_path", "F:/resto/6b6f99ff-642c-43b1-86e7-349b0f3548c1.p12");
            object.put("consume_mch_id", shopDetail.getMchid());//扣钱方的商户号
            object.put("msgappid", shopDetail.getAppid());//服务商下特约商户的公众号appId
            object.put("sub_mch_id", shopDetail.getMchid());//服务商下特约商户的商户号
        }
        //将用于分红的微信订单变成已用于分红
        useChargePay(paymentItem, chargePayment, isUseChargePay);
        //执行发放红包操作
        Map<String, String> result = WeChatPayUtils.sendredpack(object);
        //判断是否发放失败，如失败则抛出异常
        if (result.containsKey("ERROR")){
            throw new RuntimeException(result.get("err_code_des"));
        }
    }

     /**
      * 将使用过的微信支付订单变成已用于分红
      * @param paymentItem
      * @param chargePayment
      * @param isUseChargePay
      */
    public void useChargePay(OrderPaymentItem paymentItem, ChargePayment chargePayment, Boolean isUseChargePay){
        if (isUseChargePay) {
            chargePayment.setIsUseBonus(1);
            chargePaymentService.update(chargePayment);
        }else{
            paymentItem.setIsUseBonus(1);
            orderPaymentItemService.update(paymentItem);
        }
    }
}
