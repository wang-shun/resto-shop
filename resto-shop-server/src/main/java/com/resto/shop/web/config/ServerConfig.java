package com.resto.shop.web.config;

import javax.annotation.Resource;

import com.resto.brand.web.service.*;
import com.resto.brand.web.service.OrderRemarkService;
import com.resto.brand.web.service.TableQrcodeService;
import com.resto.shop.web.service.*;
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
    public TableQrcodeService tableQrcodeService(){
        return proxy.create(TableQrcodeService.class);
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
    public WxServerConfigService wxServerConfigService(){
        return proxy.create(WxServerConfigService.class);
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
    public WeBrandScoreService weBrandScoreService(){ return  proxy.create(WeBrandScoreService.class); }

    @Bean
    public OrderRemarkService boOrderRemarkService(){ return  proxy.create(OrderRemarkService.class); }

    @Bean
    public ElemeTokenService elemeTokenService(){ return  proxy.create(ElemeTokenService.class); }


    @Bean
    public  WetherService wetherService(){
        return proxy.create(WetherService.class);
    }

    @Bean
    public DayDataMessageService dayDataMessageService(){
        return proxy.create(DayDataMessageService.class);
    }

    @Bean
    public DayAppraiseMessageService dayAppraiseMessageService(){
        return proxy.create(DayAppraiseMessageService.class);
    }

    //品牌账户
	@Bean
	public BrandAccountService brandAccountService(){

    	return proxy.create(BrandAccountService.class);
	}

	//品牌账户日志(流水)
	@Bean
	public  BrandAccountLogService brandAccountLogService(){
		return proxy.create(BrandAccountLogService.class);
	}

	//品牌账户设置
	@Bean
	public AccountSettingService accountSettingService(){

		return proxy.create(AccountSettingService.class);
	}

	@Bean
	public AccountAddressInfoService accountAddressInfoService(){

		return proxy.create(AccountAddressInfoService.class);
	}

	@Bean
	public AccountChargeOrderService accountChargeOrderService(){
		return proxy.create(AccountChargeOrderService.class);
	}

	@Bean
	public AccountNoticeService accountNoticeService(){
		return proxy.create(AccountNoticeService.class);
	}

	@Bean
	public CloseShopService closeShopService(){
		return proxy.create(CloseShopService.class);
	}

    @Bean
    public TemplateService templateService(){
        return proxy.create(TemplateService.class);
    }


}
