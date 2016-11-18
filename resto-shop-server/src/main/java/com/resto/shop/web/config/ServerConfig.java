package com.resto.shop.web.config;

import javax.annotation.Resource;

import com.resto.brand.web.service.*;
import com.resto.shop.web.service.LogBaseService;
import com.resto.shop.web.service.ThirdService;
import com.resto.shop.web.service.EmployeeService;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;

import com.resto.shop.web.datasource.DynamicDataSource;

import cn.restoplus.rpc.client.RpcProxy;

@Configurable
@ImportResource({"classpath:applicationContext.xml"})
public class ServerConfig {
	@Bean
	public WechatConfigService wechatConfigService(){
		return proxy.create(WechatConfigService.class);
	}


	@Bean
	public BrandUserService brandUserService(){
		return proxy.create(BrandUserService.class);
	}
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
    public BrandSettingService brandSettingService(){
        return proxy.create(BrandSettingService.class);
    }
    @Bean
    public ShopDetailService shopDetailService(){
        return proxy.create(ShopDetailService.class);
    }

    @Bean
    public SmsAcountService smsAcountService(){
        return proxy.create(SmsAcountService.class);
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
	public UserService userService(){ return proxy.create(UserService.class) ;}

	@Bean
	public  com.resto.brand.web.service.EmployeeService employeeService(){
		return  proxy.create(com.resto.brand.web.service.EmployeeService.class);
	}

	@Bean
    public  com.resto.brand.web.service.PermissionService    brandPermissionService(){return  proxy.create( com.resto.brand.web.service.PermissionService.class);}

    @Bean
    public DynamicDataSource dataSource(){
        return new DynamicDataSource();
    }

    @Bean
    public OrderExceptionService orderExceptionService(){
        return  proxy.create(OrderExceptionService.class);
    }

	@Bean
	public ThirdService hungerService(){ return proxy.create(ThirdService.class) ;}

	@Bean
	public PlatformService platformService(){ return proxy.create(PlatformService.class) ;}

    @Bean
    public LogBaseService logBaseService(){ return  proxy.create(LogBaseService.class); }

    @Bean
    public  PictureService pictureService(){
        return  proxy.create(PictureService.class);
    }
}
