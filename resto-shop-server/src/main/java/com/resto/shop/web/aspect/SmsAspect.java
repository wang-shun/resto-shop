package com.resto.shop.web.aspect;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.resto.brand.web.model.SmsAcount;
import com.resto.brand.web.service.SmsAcountService;

/**
 * 短信切面
 * @author Administrator
 *
 */
@Component
@Aspect
public class SmsAspect {
	
	Logger log = LoggerFactory.getLogger(getClass());
	
	private SmsAcountService smsCountService;
	
	@Pointcut("execution(* com.resto.shop.web.service.SmsLogService.sendCode(..))")
	public void sendCode(){};
	
	
	//发短信前看是否有剩余条数
	@Before(value="sendCode")
	public void sendCodeBefore(String phone, String code, String brandId,String shopId){
		//查询这个商家的剩余短信的条数
		SmsAcount smsCount = smsCountService.selectByBrandId(brandId);
		if(smsCount.getRemainderNum()<=0){
			//通知商家短信余额不足需要充值
			//TODO
			
			
			//不能执行发短信的方法
			return ;
			
		}
		
	}
	
	
	
	@AfterReturning(value="sendCode()")
	public void sendCodeAfter(String phone, String code, String brandId,String shopId) throws Throwable{
		//减少一条剩余短信的条数 和增加一条短信使用的条数
		SmsAcount smsCount = smsCountService.selectByBrandId(brandId);
		
		
		smsCountService.updateByBrandId(brandId);
		
		
		
	}

}
