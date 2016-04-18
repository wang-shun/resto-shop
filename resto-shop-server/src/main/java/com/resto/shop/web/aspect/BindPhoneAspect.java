package com.resto.shop.web.aspect;

import javax.annotation.Resource;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.resto.shop.web.model.Customer;
import com.resto.shop.web.service.CustomerService;
import com.resto.shop.web.service.NewCustomCouponService;

@Component
@Aspect
public class BindPhoneAspect {
	Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	CustomerService customerService;
	@Resource
	NewCustomCouponService newCustomerCouponService;
	
	@Pointcut("execution(* com.resto.shop.web.service.CustomerService.bindPhone(..))")
	public void bindPhone(){};
	
	@Around("bindPhone()")
	public Object bindPhoneAround(ProceedingJoinPoint pj) throws Throwable{
		String customerId = (String) pj.getArgs()[1];
		Customer cus = customerService.selectById(customerId);
		boolean isFirstBind = !cus.getIsBindPhone();
		Object obj = pj.proceed();
		if(isFirstBind){
			newCustomerCouponService.giftCoupon(cus);
			log.info("首次绑定手机，执行指定动作");
		}else{
			log.info("不是首次绑定，无任何动作");
		}
		return obj;
	}
	
	
}
