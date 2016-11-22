package com.resto.shop.web.aspect;

import com.resto.brand.core.util.WeChatUtils;
import com.resto.brand.web.model.BrandSetting;
import com.resto.brand.web.model.WechatConfig;
import com.resto.brand.web.service.BrandSettingService;
import com.resto.brand.web.service.WechatConfigService;
import com.resto.shop.web.model.*;
import com.resto.shop.web.service.AppraiseService;
import com.resto.shop.web.service.CustomerService;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * Created by carl on 2016/11/22.
 */
@Component
@Aspect
public class AppraiseAspect {
    Logger log = LoggerFactory.getLogger(getClass());

    @Resource
    private CustomerService customerService;
    @Resource
    private AppraiseService appraiseService;
    @Resource
    private WechatConfigService wechatConfigService;

    @Pointcut("execution(* com.resto.shop.web.service.AppraisePraiseService.updateCancelPraise(..))")
    public void updateCancelPraise(){};

    @AfterReturning(value = "updateCancelPraise()", returning = "appraisePraise")
    public void updateCancelPraiseAfter(AppraisePraise appraisePraise) {
        if(appraisePraise.getIsDel() == 0){
            Appraise appraise = appraiseService.selectById(appraisePraise.getAppraiseId());
            Customer aCustomer = customerService.selectById(appraise.getCustomerId());
            Customer customer = customerService.selectById(appraisePraise.getCustomerId());
            WechatConfig config = wechatConfigService.selectByBrandId(customer.getBrandId());
            StringBuffer msg = new StringBuffer();
            msg.append(customer.getNickname() + "给你点赞了\n");
            WeChatUtils.sendCustomerMsg(msg.toString(), aCustomer.getWechatId(), config.getAppid(), config.getAppsecret());
        }
    }

    @Pointcut("execution(* com.resto.shop.web.service.AppraiseCommentService.insertComment(..))")
    public void insertComment(){};

    @AfterReturning(value = "insertComment()", returning = "appraiseComment")
    public void insertCommentAfter(AppraiseComment appraiseComment) {
            Appraise appraise = appraiseService.selectById(appraiseComment.getAppraiseId());
            Customer appCustomer = customerService.selectById(appraise.getCustomerId());
            Customer customer = customerService.selectById(appraiseComment.getCustomerId());
            WechatConfig config = wechatConfigService.selectByBrandId(customer.getBrandId());
            StringBuffer msg = new StringBuffer();
            msg.append(customer.getNickname() + "给你评论了\n");
            WeChatUtils.sendCustomerMsg(msg.toString(), appCustomer.getWechatId(), config.getAppid(), config.getAppsecret());
    }
}
