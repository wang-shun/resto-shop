package com.resto.shop.web.aspect;

import javax.annotation.Resource;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.ons.api.Message;
import com.resto.brand.core.util.MQSetting;
import com.resto.shop.web.producer.MQMessageProducer;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.resto.brand.core.util.WeChatUtils;
import com.resto.brand.web.model.BrandSetting;
import com.resto.brand.web.model.ShareSetting;
import com.resto.brand.web.model.WechatConfig;
import com.resto.brand.web.service.BrandSettingService;
import com.resto.brand.web.service.ShareSettingService;
import com.resto.brand.web.service.WechatConfigService;
import com.resto.shop.web.datasource.DataSourceContextHolder;
import com.resto.shop.web.model.Appraise;
import com.resto.shop.web.model.Customer;
import com.resto.shop.web.service.AppraiseService;
import com.resto.shop.web.service.CustomerService;

/**
 * 分享功能切面
 * @author Diamond
 * @date 2016年6月3日
 */
@Component
@Aspect
public class ShareAspect {

	@Resource
	ShareSettingService shareSettingService;
	
	@Resource
	WechatConfigService wechatConfigService;
	@Resource
	BrandSettingService brandSettingService;
	
	@Resource
	CustomerService customerService;
	
	@Resource
	AppraiseService appraiseService;
	
	Logger log = LoggerFactory.getLogger(getClass());
	
	@Pointcut("execution(* com.resto.shop.web.service.AppraiseService.saveAppraise(..))")
	public void saveAppraise(){};
	
	@AfterReturning(value="saveAppraise()",returning="appraise")
	public void saveAppraiseSuccess(Appraise appraise) throws InterruptedException {
		log.info("保存评论成功,触发分享判定:"+appraise.getId());
		if(appraise!=null){
			ShareSetting setting = shareSettingService.selectValidSettingByBrandId(DataSourceContextHolder.getDataSourceName());
			if(setting!=null){
				boolean isCanShare = isCanShare(setting,appraise);			
				log.info("拥有分享好评设置,ID:"+setting.getId());
				if(isCanShare){
					//发送分享通知!
					Long delayTime = setting.getDelayTime() == null ? 120000 : setting.getDelayTime() * 1000L;
					appraise.setBrandId(setting.getBrandId());
					MQMessageProducer.sendShareMsg(appraise,delayTime);
				}
			}
		}
		
	}



	private boolean isCanShare(ShareSetting setting, Appraise appraise) {
		if(setting.getMinLevel()<=appraise.getLevel()&&setting.getMinLength()<=appraise.getContent().length()){
			return true;
		}
		return false;
	}
}
