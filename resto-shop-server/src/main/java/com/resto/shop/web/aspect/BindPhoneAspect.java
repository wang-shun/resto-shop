package com.resto.shop.web.aspect;

import javax.annotation.Resource;

import com.resto.brand.core.entity.Result;
import com.resto.brand.core.enums.BehaviorType;
import com.resto.brand.core.enums.DetailType;
import com.resto.brand.core.util.*;
import com.resto.brand.web.model.*;
import com.resto.brand.web.service.*;
import com.resto.shop.web.constant.AccountLogType;
import com.resto.shop.web.constant.RedType;
import com.resto.shop.web.model.*;
import com.resto.shop.web.producer.MQMessageProducer;
import com.resto.shop.web.service.*;
import com.resto.shop.web.util.BrandAccountSendUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.druid.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;

import static com.resto.brand.core.util.HttpClient.doPostAnsc;

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


	@Resource
	BrandSettingService brandSettingService;

	@Resource
	BrandAccountService brandAccountService;

	@Resource
	BrandAccountLogService brandAccountLogService;

	@Resource
	AccountSettingService accountSettingService;

	@Resource
	AccountNoticeService accountNoticeService;

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
		Brand brand = brandService.selectById(cus.getBrandId());
		log.info("当前用户注册的状态" + !isFirstBind);
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
                Map map = new HashMap(4);
                map.put("brandName", brand.getBrandName());
                map.put("fileName", sc.getId());
                map.put("type", "UserAction");
                map.put("content", "系统向用户:"+sc.getNickname()+"推送微信消息:"+msg.toString()+",请求服务器地址为:" + MQSetting.getLocalIP());
                doPostAnsc(LogUtils.url, map);
			}
			//yz 2017/07/28 计费系统 注册收费
			BrandSetting brandSetting = brandSettingService.selectByBrandId(cus.getBrandId());
			if(brandSetting.getOpenBrandAccount()==1){//开启了品牌账户信息
				BrandAccount brandAccount = brandAccountService.selectByBrandId(brand.getId());
				//获取品牌账户设置
				AccountSetting accountSetting = accountSettingService.selectByBrandSettingId(brandSetting.getId());
				BigDecimal money = BigDecimal.ZERO;
				if(accountSetting.getOpenNewCustomerRegister()==1){
					money = accountSetting.getNewCustomerValue();
				}

				//品牌剩余的money 不计算剩余 在sql中控制
				//BigDecimal remain = brandAccount.getAccountBalance().subtract(money);
				//更新日志
				BrandAccountLog blog = new BrandAccountLog();
				blog.setCreateTime(new Date());
				blog.setGroupName(brand.getBrandName());
				blog.setBehavior(BehaviorType.REGISTER);
				blog.setFoundChange(money.negate());
				//blog.setRemain(remain);
				blog.setDetail(DetailType.NEW_CUSTOMER_REGISTER);
				blog.setAccountId(brandAccount.getId());
				blog.setShopId(shopId);
				blog.setBrandId(brand.getId());
				blog.setSerialNumber(DateUtil.getRandomSerialNumber());
				//记录 品牌账户更新日志 + 更新账户
//				Integer brandAccountId = brandAccount.getId();
//				brandAccount = new BrandAccount();
//				brandAccount.setId(brandAccountId);
				//brandAccount.setAccountBalance(remain);
				brandAccount.setUpdateTime(new Date());
				brandAccountLogService.updateBrandAccountAndLog(blog,brandAccount.getId(),money);
				List<AccountNotice> noticeList = accountNoticeService.selectByAccountId(brandAccount.getId());
			    Result result =  BrandAccountSendUtil.sendSms(brandAccount,noticeList,brand.getBrandName(),accountSetting);
			    if(result.isSuccess()){
					Long id = accountSetting.getId();
					AccountSetting as = new AccountSetting();
					as.setId(id);
					as.setType(1);
					accountSettingService.update(as);//设置为不可以发短信
					log.info(brand.getBrandName()+"品牌账户余额欠费生产者开始生产欠费消息");
					MQMessageProducer.sendBrandAccountSms(brand.getId(),MQSetting.DELAY_TIME);
				}
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
