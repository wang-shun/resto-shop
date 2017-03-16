package com.resto.shop.web.aspect;

import javax.annotation.Resource;

import com.resto.brand.core.util.LogUtils;
import com.resto.brand.core.util.MQSetting;
import com.resto.brand.core.util.WeChatUtils;
import com.resto.brand.web.model.Brand;
import com.resto.brand.web.model.ShareSetting;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.model.WechatConfig;
import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.ShareSettingService;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.brand.web.service.WechatConfigService;
import com.resto.shop.web.consumer.OrderMessageListener;
import com.resto.shop.web.model.Coupon;
import com.resto.shop.web.service.CouponService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.druid.util.StringUtils;
import com.resto.shop.web.model.Customer;
import com.resto.shop.web.producer.MQMessageProducer;
import com.resto.shop.web.service.CustomerService;
import com.resto.shop.web.service.NewCustomCouponService;
import com.resto.shop.web.service.SmsLogService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.resto.brand.core.util.HttpClient.doPost;

@Component
@Aspect
public class BindPhoneAspect {
	Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	CustomerService customerService;
	@Resource
	NewCustomCouponService newCustomerCouponService;
	@Resource
	CouponService couponService;
	@Resource
	ShareSettingService shareSettingService;
	@Resource
	SmsLogService smsLogService;
	@Resource
	ShopDetailService shopDetailService;
    @Resource
    BrandService brandService;
	@Resource
	WechatConfigService wechatConfigService;

	@Pointcut("execution(* com.resto.shop.web.service.CustomerService.bindPhone(..))")
	public void bindPhone(){};

	@Around("bindPhone()")
	public Object bindPhoneAround(ProceedingJoinPoint pj) throws Throwable{
		String customerId = (String) pj.getArgs()[1];
		Integer couponType = (Integer) pj.getArgs()[2];
        String shopId = (String) pj.getArgs()[3];
		String shareCustomer = (String) pj.getArgs()[4];
		if(customerId.equals(shareCustomer)){
			shareCustomer = null;
		}
		Customer cus = customerService.selectById(customerId);
		boolean isFirstBind = !cus.getIsBindPhone();
		Object obj = pj.proceed();
		if(isFirstBind){
			newCustomerCouponService.giftCoupon(cus,couponType,shopId);
			//如果有分享者，那么给分享者发消息
//			if(!StringUtils.isEmpty(cus.getShareCustomer())){
			if(!StringUtils.isEmpty(shareCustomer)){
//				MQMessageProducer.sendNoticeShareMessage(cus);
				Customer sc = customerService.selectById(shareCustomer);
				ShareSetting shareSetting = shareSettingService.selectValidSettingByBrandId(cus.getBrandId());
				BigDecimal sum = new BigDecimal(0);
				List<Coupon> couponList = new ArrayList<>();
				//品牌专属优惠券
				List<Coupon> couponList1 = couponService.listCouponByStatus("0", cus.getId(),cus.getBrandId(),null);
				couponList.addAll(couponList1);
				List<ShopDetail> listShop = shopDetailService.selectByBrandId(cus.getBrandId());
				for(ShopDetail s : listShop){
					//店铺专属优惠券
					List<Coupon> couponList2 = couponService.listCouponByStatus("0", cus.getId(),null,s.getId());
					couponList.addAll(couponList2);
				}
				for (Coupon coupon : couponList) {
					sum = sum.add(coupon.getValue());
				}
				StringBuffer msg = new StringBuffer("亲，感谢您的分享，您的好友");
				if(shareSetting == null){
					msg.append(cus.getNickname()).append("已领取").append(sum).append("元红包，")
							.append(cus.getNickname()).append("如到店消费您将获得红包返利");
				}else{
					msg.append(cus.getNickname()).append("已领取").append(sum).append("元红包，")
							.append(cus.getNickname()).append("如到店消费您将获得").append(shareSetting.getMinMoney())
							.append("-").append(shareSetting.getMaxMoney()).append("元红包返利");
				}
				WechatConfig config = wechatConfigService.selectByBrandId(cus.getBrandId());
				log.info("异步发送分享注册微信通知ID:" + shareCustomer + " 内容:" + msg);
				WeChatUtils.sendCustomerMsg(msg.toString(), sc.getWechatId(), config.getAppid(), config.getAppsecret());
                Brand brand = brandService.selectById(sc.getBrandId());
                Map map = new HashMap(4);
                map.put("brandName", brand.getBrandName());
                map.put("fileName", sc.getId());
                map.put("type", "UserAction");
                map.put("content", "系统向用户:"+sc.getNickname()+"推送微信消息:"+msg.toString()+",请求服务器地址为:" + MQSetting.getLocalIP());
                doPost(LogUtils.url, map);
			}
			log.info("首次绑定手机，执行指定动作");

		}else{
			log.info("不是首次绑定，无任何动作");
		}
		return obj;
	}

//	@AfterReturning(value = "bindPhone()", returning = "customer")
//	public void bindPhoneAround(JoinPoint jp, Customer customer) throws Throwable{
//		boolean isFirstBind = !customer.getIsBindPhone();
//		Integer couponType = (Integer) jp.getArgs()[2];
//		String shopId = (String) jp.getArgs()[3];
//		if(isFirstBind){
//			newCustomerCouponService.giftCoupon(customer,couponType,shopId);
//			//如果有分享者，那么给分享者发消息
//			if(!StringUtils.isEmpty(customer.getShareCustomer())){
//				MQMessageProducer.sendNoticeShareMessage(customer);
//			}
//			log.info("首次绑定手机，执行指定动作");
//
//		}else{
//			log.info("不是首次绑定，无任何动作");
//		}
//	}

}
