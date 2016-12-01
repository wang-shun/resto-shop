package com.resto.shop.web.aspect;

import com.resto.brand.core.util.WeChatUtils;
import com.resto.brand.web.model.Brand;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.model.WechatConfig;
import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.brand.web.service.WechatConfigService;
import com.resto.shop.web.model.Appraise;
import com.resto.shop.web.model.AppraiseComment;
import com.resto.shop.web.model.AppraisePraise;
import com.resto.shop.web.model.Customer;
import com.resto.shop.web.service.AppraiseCommentService;
import com.resto.shop.web.service.AppraiseService;
import com.resto.shop.web.service.CustomerService;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

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
    @Resource
    private ShopDetailService shopDetailService;
    @Resource
    private BrandService brandService;
    @Resource
    private AppraiseCommentService appraiseCommentService;

    @Pointcut("execution(* com.resto.shop.web.service.AppraisePraiseService.updateCancelPraise(..))")
    public void updateCancelPraise(){};

    @AfterReturning(value = "updateCancelPraise()", returning = "appraisePraise")
    public void updateCancelPraiseAfter(AppraisePraise appraisePraise) {
        if(appraisePraise.getIsDel() == 0){
            Appraise appraise = appraiseService.selectById(appraisePraise.getAppraiseId());
            Customer aCustomer = customerService.selectById(appraise.getCustomerId());
            ShopDetail shopDetail = shopDetailService.selectById(appraise.getShopDetailId());
            Brand brand = brandService.selectById(aCustomer.getBrandId());
            Customer customer = customerService.selectById(appraisePraise.getCustomerId());
            WechatConfig config = wechatConfigService.selectByBrandId(customer.getBrandId());
            StringBuffer msg = new StringBuffer();
            String url = "http://" + brand.getBrandSign() + ".restoplus.cn/wechat/appraise?appraiseId=" + appraise.getId() + "&baseUrl=" + "http://" + brand.getBrandSign() + ".restoplus.cn";
            msg.append(customer.getNickname() + "为你在" + shopDetail.getName() + "的评论点了赞，快去<a href='" + url+ "'>回复TA</a>吧~\n");
            WeChatUtils.sendCustomerMsg(msg.toString(), aCustomer.getWechatId(), config.getAppid(), config.getAppsecret());
        }
    }

    @Pointcut("execution(* com.resto.shop.web.service.AppraiseCommentService.insertComment(..))")
    public void insertComment(){};

    @AfterReturning(value = "insertComment()", returning = "appraiseComment")
    public void insertCommentAfter(AppraiseComment appraiseComment) {
            Appraise appraise = appraiseService.selectById(appraiseComment.getAppraiseId());
            Customer appCustomer = customerService.selectById(appraise.getCustomerId());
            ShopDetail shopDetail = shopDetailService.selectById(appraise.getShopDetailId());
            Brand brand = brandService.selectById(appCustomer.getBrandId());
            Customer customer = customerService.selectById(appraiseComment.getCustomerId());
            WechatConfig config = wechatConfigService.selectByBrandId(customer.getBrandId());
            StringBuffer msg = new StringBuffer();
            String url = "http://" + brand.getBrandSign() + ".restoplus.cn/wechat/appraise?appraiseId=" + appraise.getId() + "&baseUrl=" + "http://" + brand.getBrandSign() + ".restoplus.cn";
            msg.append(customer.getNickname() + "回复了您在" + shopDetail.getName() + "的评论，快去<a href='" + url+ "'>回复TA</a>吧~\n");
            WeChatUtils.sendCustomerMsg(msg.toString(), appCustomer.getWechatId(), config.getAppid(), config.getAppsecret());
            //继续发送给你回复的人
            if(!StringUtils.isEmpty(appraiseComment.getPid())){
                AppraiseComment faComment = appraiseCommentService.selectById(appraiseComment.getPid());
                Customer faCustomer = customerService.selectById(faComment.getCustomerId());
                if(faCustomer.getWechatId() != appCustomer.getWechatId()){
                    WeChatUtils.sendCustomerMsg(msg.toString(), faCustomer.getWechatId(), config.getAppid(), config.getAppsecret());
                }
            }
    }
}
