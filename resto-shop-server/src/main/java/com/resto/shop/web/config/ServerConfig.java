package com.resto.shop.web.config;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;

import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.BrandSettingService;
import com.resto.brand.web.service.DatabaseConfigService;
import com.resto.brand.web.service.RewardSettingService;
import com.resto.brand.web.service.ShareSettingService;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.brand.web.service.WechatConfigService;
import com.resto.shop.web.datasource.DynamicDataSource;

import cn.restoplus.rpc.client.RpcProxy;

@Configurable
@ImportResource({"classpath:applicationContext.xml"})
public class ServerConfig {
	@Resource
	RpcProxy proxy;
	
	@Bean
	public DatabaseConfigService databaseConfigService(){
		return proxy.create(DatabaseConfigService.class);
	}
	@Bean
	public BrandService brandService(){
		return proxy.create(BrandService.class);
	}
	
	@Bean
	public WechatConfigService wechatConfigService(){
		return proxy.create(WechatConfigService.class);
	}
	
	@Bean
	public BrandSettingService brandSettingService(){
		return proxy.create(BrandSettingService.class);
	}
	@Bean
	public ShopDetailService shopDetailService(){
		return proxy.create(ShopDetailService.class);
	}
	
	@Bean
	public ShareSettingService ShareSettingService(){
		return proxy.create(ShareSettingService.class);
	}
	@Bean
	public RewardSettingService rewardSettingService(){
		return proxy.create(RewardSettingService.class);
	}
	@Bean
	public DynamicDataSource dataSource(){
		return new DynamicDataSource();
	}


}
