package com.resto.shop.web.aspect;

import com.resto.brand.core.util.WeChatUtils;
import com.resto.brand.web.model.BrandSetting;
import com.resto.brand.web.model.WechatConfig;
import com.resto.brand.web.service.BrandSettingService;
import com.resto.brand.web.service.WechatConfigService;
import com.resto.shop.web.constant.WaitModerState;
import com.resto.shop.web.model.Customer;
import com.resto.shop.web.model.GetNumber;
import com.resto.shop.web.service.CustomerService;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by carl on 2016/10/16.
 */
@Component
@Aspect
public class GetNumberAspect {

    Logger log = LoggerFactory.getLogger(getClass());

    @Resource
    private BrandSettingService brandSettingService;

    @Resource
    private CustomerService customerService;

    @Resource
    private WechatConfigService wechatConfigService;

    @Pointcut("execution(* com.resto.shop.web.service.GetNumberService.updateGetNumber(..))")
    public void updateGetNumber(){};

    @AfterReturning(value = "updateGetNumber()", returning = "getNumber")
    public void updateGetNumberAfter(GetNumber getNumber) {
//        Customer customer = null;
//        WechatConfig config = null;
//        BrandSetting setting = null;
        if(getNumber.getCustomerId() != null){
            Customer customer = customerService.selectById(getNumber.getCustomerId());
            WechatConfig config = wechatConfigService.selectByBrandId(customer.getBrandId());
            BrandSetting setting = brandSettingService.selectByBrandId(customer.getBrandId());
            if(getNumber.getState() == WaitModerState.WAIT_MODEL_NUMBER_ZERO){
                StringBuffer msg = new StringBuffer();
                msg.append(customer.getNickname() + "，请至餐厅就餐，您一共生成了" + getNumber.getFinalMoney() + "的等位红包。\n");
                WeChatUtils.sendCustomerWaitNumberMsg(msg.toString(), customer.getWechatId(), config.getAppid(), config.getAppsecret());
            } else if(getNumber.getState() == WaitModerState.WAIT_MODEL_NUMBER_ONE) {
                StringBuffer msg = new StringBuffer();
                msg.append("亲，您一共获取"+getNumber.getFinalMoney()+"元等位红包，红包金额在本次消费中将会直接使用哦。\n");
                msg.append("<a href='" + setting.getWechatWelcomeUrl() + "?subpage=tangshi&shopId=" + getNumber.getShopDetailId() + " '>立即点餐</a>");
                WeChatUtils.sendCustomerWaitNumberMsg(msg.toString(), customer.getWechatId(), config.getAppid(), config.getAppsecret());
            } else if(getNumber.getState() == WaitModerState.WAIT_MODEL_NUMBER_TWO) {
                StringBuffer msg = new StringBuffer();
                msg.append(customer.getNickname() + "已过号，谢谢您的支持与谅解，" + getNumber.getFinalMoney() + "元等位红包已失效，期待您的下次光临。\n");
                WeChatUtils.sendCustomerWaitNumberMsg(msg.toString(), customer.getWechatId(), config.getAppid(), config.getAppsecret());
            }
        }
    }
}
