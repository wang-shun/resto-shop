package com.resto.shop.web.aspect;

import com.resto.brand.core.util.LogUtils;
import com.resto.brand.core.util.MQSetting;
import com.resto.brand.core.util.WeChatUtils;
import com.resto.brand.web.model.BrandSetting;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.model.WechatConfig;
import com.resto.brand.web.service.BrandSettingService;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.brand.web.service.WechatConfigService;
import com.resto.shop.web.constant.WaitModerState;
import com.resto.shop.web.model.Customer;
import com.resto.shop.web.model.GetNumber;
import com.resto.shop.web.service.CustomerService;
import com.resto.shop.web.service.GetNumberService;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.resto.brand.core.util.HttpClient.doPostAnsc;

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

    @Resource
    private ShopDetailService shopDetailService;

    @Resource
    private GetNumberService getNumberService;

    @Pointcut("execution(* com.resto.shop.web.service.GetNumberService.updateGetNumber(..))")
    public void updateGetNumber(){};

    @AfterReturning(value = "updateGetNumber()", returning = "getNumber")
    public void updateGetNumberAfter(GetNumber getNumber) {
//        Customer customer = null;
//        WechatConfig config = null;
//        BrandSetting setting = null;
        if(getNumber.getCustomerId() != null){
            ShopDetail shop = shopDetailService.selectByPrimaryKey(getNumber.getShopDetailId());
            Customer customer = customerService.selectById(getNumber.getCustomerId());
            WechatConfig config = wechatConfigService.selectByBrandId(customer.getBrandId());
            BrandSetting setting = brandSettingService.selectByBrandId(customer.getBrandId());
            if(getNumber.getState() == WaitModerState.WAIT_MODEL_NUMBER_ZERO){
            	log.info("发送叫号提示");
                StringBuffer msg = new StringBuffer();
//                msg.append(customer.getNickname() + "，请至餐厅就餐，您一共获得" + getNumber.getFinalMoney().setScale(2,   BigDecimal.ROUND_HALF_UP) + "元的等位红包。\n");
                msg.append(shop.getWaitJiaohao());
                WeChatUtils.sendCustomerMsg(msg.toString(), customer.getWechatId(), config.getAppid(), config.getAppsecret());
                Map map = new HashMap(4);
                map.put("brandName", setting.getBrandName());
                map.put("fileName", customer.getId());
                map.put("type", "UserAction");
                map.put("content", "系统向用户:"+customer.getNickname()+"推送微信消息:"+msg.toString()+",请求服务器地址为:" + MQSetting.getLocalIP());
                doPostAnsc(LogUtils.url, map);
            } else if(getNumber.getState() == WaitModerState.WAIT_MODEL_NUMBER_ONE) {
            	log.info("发送就餐提示");
                StringBuffer msg = new StringBuffer();
//                msg.append("亲，您一共获得"+getNumber.getFinalMoney().setScale(2,   BigDecimal.ROUND_HALF_UP)+"元等位红包，红包金额在本次消费中将直接使用哦。\n");
//                msg.append("<a href='" + setting.getWechatWelcomeUrl() + "?subpage=tangshi&shopId=" + getNumber.getShopDetailId() + " '>立即点餐</a>");
                msg.append(shop.getWaitJiucan());
                WeChatUtils.sendCustomerMsg(msg.toString(), customer.getWechatId(), config.getAppid(), config.getAppsecret());
                Map map = new HashMap(4);
                map.put("brandName", setting.getBrandName());
                map.put("fileName", customer.getId());
                map.put("type", "UserAction");
                map.put("content", "系统向用户:"+customer.getNickname()+"推送微信消息:"+msg.toString()+",请求服务器地址为:" + MQSetting.getLocalIP());
                doPostAnsc(LogUtils.url, map);
            } else if(getNumber.getState() == WaitModerState.WAIT_MODEL_NUMBER_TWO) {
            	log.info("发送过号提示");
                StringBuffer msg = new StringBuffer();
//                msg.append(customer.getNickname() + "已过号，谢谢您的支持与谅解，" + getNumber.getFinalMoney().setScale(2,   BigDecimal.ROUND_HALF_UP) + "元等位红包已失效，期待您的下次光临。\n");
                msg.append(shop.getWaitGuohao());
                WeChatUtils.sendCustomerMsg(msg.toString(), customer.getWechatId(), config.getAppid(), config.getAppsecret());
                Map map = new HashMap(4);
                map.put("brandName", setting.getBrandName());
                map.put("fileName", customer.getId());
                map.put("type", "UserAction");
                map.put("content", "系统向用户:"+customer.getNickname()+"推送微信消息:"+msg.toString()+",请求服务器地址为:" + MQSetting.getLocalIP());
                doPostAnsc(LogUtils.url, map);
            }

            if(shop.getWaitRemindSwitch() == 1 && shop.getWaitRemindNumber() > 0 &&
                    (getNumber.getState() == WaitModerState.WAIT_MODEL_NUMBER_ONE || getNumber.getState() == WaitModerState.WAIT_MODEL_NUMBER_TWO)){
                List<GetNumber> getNumberList = getNumberService.selectBeforeNumberByCodeId(getNumber.getShopDetailId(), getNumber.getCodeId(), getNumber.getCreateTime());
                if((getNumberList.size() + 1) >= shop.getWaitRemindNumber()){
                    GetNumber gn = getNumberList.get(shop.getWaitRemindNumber() - 1);
                    Customer c = customerService.selectById(gn.getCustomerId());
                    StringBuffer msg = new StringBuffer();
                    msg.append(shop.getWaitRemindText());
                    WeChatUtils.sendCustomerMsg(msg.toString(), c.getWechatId(), config.getAppid(), config.getAppsecret());
                }
            }
        }
    }
}
