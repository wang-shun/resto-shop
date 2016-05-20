package com.resto.shop.web.aspect;

import javax.annotation.Resource;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.resto.brand.web.model.SmsAcount;
import com.resto.brand.web.service.SmsAcountService;
import cn.restoplus.rpc.client.RpcProxy;

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
	RpcProxy rpcProxy;
	private SmsAcountService smsAcountService;
	
	@Autowired
	public SmsAspect(RpcProxy rpcProxy) {
		smsAcountService = rpcProxy.create(SmsAcountService.class);
	}
	
	
//	@Resource
//	RpcProxy rpcProxy;
//	PermissionService permissionService;
//	
//	@Autowired
//	public MenuCacheAspect(RpcProxy rpcProxy) {
//		permissionService = rpcProxy.create(PermissionService.class);
//	}
	
	
    
	@Pointcut("execution(* com.resto.shop.web.service.impl.SmsLogServiceImpl.sendMsg(..))")
	public void sendMsg(){};
	
	
	//发短信前看是否有剩余条数
	@Before(value="sendMsg()")
	public void sendMessageBefore(JoinPoint pj) throws Throwable{
		System.out.println("======");
		Object[] args = pj.getArgs();
		String brandId = args[4].toString();
		log.info(".............................+");
		//查询这个商家的剩余短信的条数
		SmsAcount smsCount = smsAcountService.selectByBrandId(brandId);
		if(smsCount.getRemainderNum()<=0){
			//通知商家短信余额不足需要充值
			
			log.info("短信账户余额不足");
			
			return;
			//不能执行发短信的方法
		}
		return;
	}
	
	@Pointcut("execution(* com.resto.shop.web.service.impl.SmsLogServiceImpl.selectListByShopId(..))")
	public void selectList(){};
	
	@Before(value="selectList()")
	public void selectBefore(){
		
		System.out.println("执行了该方法。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。");
	}
	
	
//	@AfterReturning(value="sendMsg()")
//	public void sendCodeAfter(String phone,String data,String sign, String codeSmsTemp,String brandId) throws Throwable{
//	        //判断当前剩余短信的条数是否需要提醒商家
//		SmsAcount smsAccount = smsAcountService.selectByBrandId(brandId);
//		
//		if(NoticeType.first==smsAccount.getRemainderNum()||NoticeType.second==smsAccount.getRemainderNum()||NoticeType.last==smsAccount.getRemainderNum()){
//		    //通知商家短信不足
//		    
//		}
//		
//	}
	
//	@Before(value="sendMsg()")
//	public String sendMsgBefore(String sign,String serviceName,String code,String phone){
//		
//		System.out.println("执行了前置方法");
//		
//		return "s";
//	}
	
}
