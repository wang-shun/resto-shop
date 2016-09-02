package com.resto.shop.web.aspect;

import javax.annotation.Resource;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.druid.util.StringUtils;
import com.resto.shop.web.model.Customer;
import com.resto.shop.web.producer.MQMessageProducer;
import com.resto.shop.web.service.CustomerService;
import com.resto.shop.web.service.NewCustomCouponService;
import com.resto.shop.web.service.SmsLogService;

@Component
@Aspect
public class BindPhoneAspect {
	Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	CustomerService customerService;
	@Resource
	NewCustomCouponService newCustomerCouponService;
	
	@Resource
	SmsLogService smsLogService;
	
	@Pointcut("execution(* com.resto.shop.web.service.CustomerService.bindPhone(..))")
	public void bindPhone(){};
	
	@Around("bindPhone()")
	public Object bindPhoneAround(ProceedingJoinPoint pj) throws Throwable{
		String customerId = (String) pj.getArgs()[1];
		Integer couponType = (Integer) pj.getArgs()[2];
		Customer cus = customerService.selectById(customerId);
		boolean isFirstBind = !cus.getIsBindPhone();
		Object obj = pj.proceed();
		if(isFirstBind){
			newCustomerCouponService.giftCoupon(cus,couponType);
			//如果有分享者，那么给分享者发消息
			if(!StringUtils.isEmpty(cus.getShareCustomer())){
				MQMessageProducer.sendNoticeShareMessage(cus);
			}
			log.info("首次绑定手机，执行指定动作");
			
		}else{
			log.info("不是首次绑定，无任何动作");
		}
		return obj;
	}

}
