package com.resto.shop.web.config;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;

import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.BrandUserService;
import com.resto.brand.web.service.DatabaseConfigService;
import com.resto.brand.web.service.DistributionModeService;
import com.resto.brand.web.service.PermissionService;
import com.resto.brand.web.service.RoleService;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.brand.web.service.ShopModeService;
import com.resto.brand.web.service.UserGroupService;
import com.resto.brand.web.service.UserService;
import com.resto.brand.web.service.WechatConfigService;
import com.resto.brand.web.service.WechatTempIdService;
import com.resto.brand.web.service.WechatTempTypeService;

import cn.restoplus.rpc.client.RpcProxy;

@Configurable
@ImportResource("classpath:applicationContext*.xml")
public class SpringContextConfig {
	
	@Resource
	RpcProxy proxy;
	
	@Bean
	public PermissionService permissionService(){
		return proxy.create(PermissionService.class);
	}
	
	@Bean 
	public RoleService roleService(){
		return proxy.create(RoleService.class);
	}
	
	@Bean
	public UserService userService(){
		return proxy.create(UserService.class);
	}
	
	@Bean
	public BrandService brandService(){
		return proxy.create(BrandService.class);
	}
	
	@Bean
	public BrandUserService brandUserService(){
		return proxy.create(BrandUserService.class);
	}
	
	@Bean
	public DatabaseConfigService databaseConfigService(){
		return proxy.create(DatabaseConfigService.class);
	}
	
	@Bean
	public DistributionModeService distributionModeService(){
		return proxy.create(DistributionModeService.class);
	}
	
	@Bean
	public ShopDetailService shopDetailService(){
		return proxy.create(ShopDetailService.class);
	}
	
	@Bean
	public ShopModeService shopModeService(){
		return proxy.create(ShopModeService.class);
	}
	
	@Bean
	public WechatConfigService wechatConfigService(){
		return proxy.create(WechatConfigService.class);
	}
	
	@Bean
	public WechatTempIdService wechatTempIdService(){
		return proxy.create(WechatTempIdService.class);
	}
	
	@Bean
	public WechatTempTypeService wechatTempTypeService(){
		return proxy.create(WechatTempTypeService.class);
	}
	
	@Bean
	public UserGroupService userGroupService(){
		return proxy.create(UserGroupService.class);
	}
	
}
