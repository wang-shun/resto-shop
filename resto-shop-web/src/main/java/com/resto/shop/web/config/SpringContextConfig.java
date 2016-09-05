package com.resto.shop.web.config;

import java.io.File;

import javax.annotation.Resource;

import com.resto.brand.web.service.RoleService;
import com.resto.shop.web.model.ERole;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;

import com.resto.brand.web.service.*;
import com.resto.shop.web.service.*;
import cn.restoplus.rpc.client.RpcProxy;

@Configurable
@ImportResource("classpath:applicationContext*.xml")
public class SpringContextConfig {
	
	@Resource
	RpcProxy proxy;
	
	@Bean
	public PermissionService permissionService(){
		return getProxy(PermissionService.class);
	}

	
	@Bean 
	public RoleService roleService(){
		return getProxy(RoleService.class);
	}


	@Bean
	public UserService userService(){
		return getProxy(UserService.class);
	}
	
	@Bean
	public BrandService brandService(){
		return getProxy(BrandService.class);
	}
	
	@Bean
	public BrandUserService brandUserService(){
		return getProxy(BrandUserService.class);
	}
	
	@Bean
	public DatabaseConfigService databaseConfigService(){
		return getProxy(DatabaseConfigService.class);
	}
	
	@Bean
	public DistributionModeService distributionModeService(){
		return getProxy(DistributionModeService.class);
	}
	
	@Bean
	public ShopDetailService shopDetailService(){
		return getProxy(ShopDetailService.class);
	}
	
	@Bean
	public ShopModeService shopModeService(){
		return getProxy(ShopModeService.class);
	}
	
	@Bean
	public WechatConfigService wechatConfigService(){
		return getProxy(WechatConfigService.class);
	}
	
	@Bean
	public UserGroupService userGroupService(){
		return getProxy(UserGroupService.class);
	}
	
	@Bean
	public AccountLogService accountLogService(){
		return getProxy(AccountLogService.class);
	}
	@Bean
	public AccountService accountService(){
		return getProxy(AccountService.class);
	}
	@Bean
	public AdvertService advertService(){
		return getProxy(AdvertService.class);
	}
	@Bean
	public AppraiseService appraiseService(){
		return getProxy(AppraiseService.class);
	}
	@Bean
	public ArticleFamilyService articleFamilyService(){
		return getProxy(ArticleFamilyService.class);
	}
	@Bean
	public ArticleService articleService(){
		return getProxy(ArticleService.class);
	}
	@Bean
	public ArticleUnitService articleUnitService(){
		return getProxy(ArticleUnitService.class);
	}
	@Bean
	public ChargeOrderService chargeOrderService(){
		return getProxy(ChargeOrderService.class);
	}
	@Bean
	public ChargePaymentService chargePaymentService(){
		return getProxy(ChargePaymentService.class);
	}
	@Bean
	public ChargeSettingService chargeSettingService(){
		return getProxy(ChargeSettingService.class);
	}
	@Bean
	public CouponService couponService(){
		return getProxy(CouponService.class);
	}
	@Bean
	public CustomerService customerService(){
		return getProxy(CustomerService.class);
	}
	@Bean
	public DeliveryPointService deliveryPointService(){
		return getProxy(DeliveryPointService.class);
	}
	@Bean
	public DistributionTimeService distributionTimeService(){
		return getProxy(DistributionTimeService.class);
	}
	@Bean
	public KitchenService kitchenService(){
		return getProxy(KitchenService.class);
	}
	@Bean
	public NewCustomCouponService newCustomCouponService(){
		return getProxy(NewCustomCouponService.class);
	}
	@Bean
	public NoticeService noticeService(){
		return getProxy(NoticeService.class);
	}
	@Bean
	public OrderItemService orderItemService(){
		return getProxy(OrderItemService.class);
	}
	@Bean
	public OrderPaymentItemService orderPaymentItemService(){
		return getProxy(OrderPaymentItemService.class);
	}
	@Bean
	public OrderService orderService(){
		return getProxy(OrderService.class);
	}
	@Bean
	public PictureSliderService pictureSliderService(){
		return getProxy(PictureSliderService.class);
	}
	@Bean
	public PrinterService printerService(){
		return getProxy(PrinterService.class);
	}
	@Bean
	public RedConfigService redConfigService(){
		return getProxy(RedConfigService.class);
	}
	@Bean
	public ShopCartService shopCartService(){
		return getProxy(ShopCartService.class);
	}
	@Bean
	public SmsLogService smsLogService(){
		return getProxy(SmsLogService.class);
	}
	@Bean
	public SupportTimeService supportTimeService(){
		return getProxy(SupportTimeService.class);
	}
	
	@Bean
	public ArticlePriceService articlePriceService(){
		return getProxy(ArticlePriceService.class);
	}
	
	
	@Bean
	public ArticleAttrService articleAttrService(){
		return getProxy(ArticleAttrService.class);
	}
	
	@Bean
	public FreedayService freedayService(){
        return getProxy(FreedayService.class);
    }
	
	@Bean
	public ShowPhotoService showPhotoService(){
		return getProxy(ShowPhotoService.class);
	}
	
	@Bean
	public BrandSettingService brandSettingService(){
		return getProxy(BrandSettingService.class);
	}
	
	@Bean 
	public MealTempService mealTempService(){
		return getProxy(MealTempService.class);
	}
	
	@Bean 
	public MealTempAttrService mealTempAttrService(){
		return getProxy(MealTempAttrService.class);
	}
	@Bean
	public MealAttrService mealAttrService(){
		return getProxy(MealAttrService.class);
	}
	
	@Bean
	public ModuleListService moduleListService(){
		return getProxy(ModuleListService.class);
	}
	@Bean
	public ShareSettingService shareSettingService(){
		return getProxy(ShareSettingService.class);
	}

    @Bean
    public EmployeeService employeeService(){
        return getProxy(EmployeeService.class);
    }

	@Bean

	public ERoleService eRoleService(){return  getProxy(ERoleService.class); }

	@Bean
	public RewardSettingService rewardSettingService(){
		return getProxy(RewardSettingService.class);
	}
	public <T> T getProxy(Class<T> clazz){
		return proxy.create(clazz);
	}
	
	public static void main(String[] args) {
		File file = new File("D:/work/RestoPlus/gitRepository/resto-shop/resto-shop-api/src/main/java/com/resto/shop/web/service");
		StringBuffer code= new StringBuffer();
		StringBuffer importPath = new StringBuffer();
		for(File f:file.listFiles()){
			String classSimpleName = f.getName().substring(0,f.getName().length()-5);
			code.append("\t@Bean\n");
			code.append("\tpublic "+classSimpleName+" "+(classSimpleName.charAt(0)+"").toLowerCase()+classSimpleName.substring(1)+"(){\n");
			code.append("\t\treturn getProxy("+classSimpleName+".class"+");\n");
			code.append("\t}\n");
			importPath.append("import com.resto.shop.web.service."+classSimpleName+";\n");
		}
		System.out.println(importPath);
		System.out.println(code);
	}
}
