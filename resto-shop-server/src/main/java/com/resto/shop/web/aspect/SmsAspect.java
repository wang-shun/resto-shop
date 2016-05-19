package com.resto.shop.web.aspect;

import javax.annotation.Resource;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.resto.brand.web.model.SmsAccount;
import com.resto.brand.web.service.SmsAccountService;
import com.resto.shop.web.constant.SmsLogType;

import com.resto.brand.core.enums.NoticeType;

/**
 * 短信切面
 * @author Administrator
 *
 */
@Component
@Aspect
public class SmsAspect {
    
	Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	private SmsAccountService smsAcountService;
	
	@Pointcut("execution(* com.resto.brand.core.util.sendMessage(..))")
	public void sendCode(){};
	
	//发短信前看是否有剩余条数
	@Before(value="sendMessage()")
	public void sendMessageBefore(String phone,String data,String sign, String codeSmsTemp,String brandId){
		//查询这个商家的剩余短信的条数
		SmsAccount smsCount = smsAcountService.selectByBrandId(brandId);
		if(smsCount.getRemainderNum()<=0){
			//通知商家短信余额不足需要充值
			System.out.println(",,");
			//不能执行发短信的方法
		    return;
		}
	}
	
	@AfterReturning(value="sendMessage()")
	public void sendCodeAfter(String phone,String data,String sign, String codeSmsTemp,String brandId) throws Throwable{
		//减少一条剩余短信的条数 和增加一条短信使用的条数
	        smsAcountService.updateByBrandId(brandId);
	        
	        //判断当前剩余短信的条数是否需要提醒商家
		SmsAccount smsAccount = smsAcountService.selectByBrandId(brandId);
		
		if(NoticeType.first==smsAccount.getRemainderNum()||NoticeType.second==smsAccount.getRemainderNum()||NoticeType.last==smsAccount.getRemainderNum()){
		    //通知商家短信不足
		    
		}
		
		
	}

}
