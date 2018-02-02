package com.resto.shop.web.config;

import cn.restoplus.rpc.client.RpcProxy;
import com.resto.brand.web.service.*;
import com.resto.brand.web.service.TableQrcodeService;
import com.resto.scm.web.service.*;
import com.resto.scm.web.service.ScmUnitService;
import com.resto.shop.web.service.*;
import com.resto.shop.web.service.EmployeeService;
import com.resto.shop.web.service.OrderRemarkService;
import com.resto.shop.web.service.PermissionService;
import com.resto.shop.web.service.PosUserService;
import com.resto.shop.web.service.ShowPhotoService;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;

import javax.annotation.Resource;
import java.io.File;

@Configurable
@ImportResource("classpath:applicationContext*.xml")
public class SpringContextConfig {

    @Resource
    RpcProxy proxy;

    @Bean
    public PermissionService permissionService() {
        return getProxy(PermissionService.class);
    }

    @Bean
    public RoleService roleService() {
        return getProxy(RoleService.class);
    }


    @Bean
    public AreaService areaService() {
        return getProxy(AreaService.class);
    }


    @Bean
    public UserService userService() {
        return getProxy(UserService.class);
    }

    @Bean
    public BrandService brandService() {
        return getProxy(BrandService.class);
    }

    @Bean
    public BrandUserService brandUserService() {
        return getProxy(BrandUserService.class);
    }

    @Bean
    public DatabaseConfigService databaseConfigService() {
        return getProxy(DatabaseConfigService.class);
    }

    @Bean
    public DistributionModeService distributionModeService() {
        return getProxy(DistributionModeService.class);
    }

    @Bean
    public ShopDetailService shopDetailService() {
        return getProxy(ShopDetailService.class);
    }

    @Bean
    public ShopModeService shopModeService() {
        return getProxy(ShopModeService.class);
    }

    @Bean
    public WechatConfigService wechatConfigService() {
        return getProxy(WechatConfigService.class);
    }

    @Bean
    public UserGroupService userGroupService() {
        return getProxy(UserGroupService.class);
    }

    @Bean
    public AccountLogService accountLogService() {
        return getProxy(AccountLogService.class);
    }

    /**
     * 引用店铺端 -- 下面有一个会引用品牌端
     * 2017-07-19
     * @return
     */
    @Bean
    public AccountService accountService() {
        return getProxy(AccountService.class);
    }

    @Bean
    public AdvertService advertService() {
        return getProxy(AdvertService.class);
    }

    @Bean
    public AppraiseService appraiseService() {
        return getProxy(AppraiseService.class);
    }

    @Bean
    public ArticleFamilyService articleFamilyService() {
        return getProxy(ArticleFamilyService.class);
    }

    @Bean
    public ArticleService articleService() {
        return getProxy(ArticleService.class);
    }

    @Bean
    public ArticleUnitService articleUnitService() {
        return getProxy(ArticleUnitService.class);
    }

    @Bean
    public ChargeOrderService chargeOrderService() {
        return getProxy(ChargeOrderService.class);
    }

    @Bean
    public ChargePaymentService chargePaymentService() {
        return getProxy(ChargePaymentService.class);
    }

    @Bean
    public ChargeSettingService chargeSettingService() {
        return getProxy(ChargeSettingService.class);
    }

    @Bean
    public ChargeLogService chargeLogService() {
        return getProxy(ChargeLogService.class);
    }

    @Bean
    public CouponService couponService() {
        return getProxy(CouponService.class);
    }

    @Bean
    public CustomerService customerService() {
        return getProxy(CustomerService.class);
    }

    @Bean
    public DeliveryPointService deliveryPointService() {
        return getProxy(DeliveryPointService.class);
    }

    @Bean
    public DistributionTimeService distributionTimeService() {
        return getProxy(DistributionTimeService.class);
    }

    @Bean
    public KitchenService kitchenService() {
        return getProxy(KitchenService.class);
    }

    @Bean
    public VirtualProductsService virtualProductsService() {
        return getProxy(VirtualProductsService.class);
    }

    @Bean
    public NewCustomCouponService newCustomCouponService() {
        return getProxy(NewCustomCouponService.class);
    }

    @Bean
    public NoticeService noticeService() {
        return getProxy(NoticeService.class);
    }

    @Bean
    public OrderItemService orderItemService() {
        return getProxy(OrderItemService.class);
    }

    @Bean
    public OrderPaymentItemService orderPaymentItemService() {
        return getProxy(OrderPaymentItemService.class);
    }

    @Bean
    public OrderService orderService() {
        return getProxy(OrderService.class);
    }

    @Bean
    public PictureSliderService pictureSliderService() {
        return getProxy(PictureSliderService.class);
    }

    @Bean
    public PrinterService printerService() {
        return getProxy(PrinterService.class);
    }

    @Bean
    public RedConfigService redConfigService() {
        return getProxy(RedConfigService.class);
    }

    @Bean
    public ShopCartService shopCartService() {
        return getProxy(ShopCartService.class);
    }

    @Bean
    public SmsLogService smsLogService() {
        return getProxy(SmsLogService.class);
    }

    @Bean
    public SupportTimeService supportTimeService() {
        return getProxy(SupportTimeService.class);
    }

    @Bean
    public ArticlePriceService articlePriceService() {
        return getProxy(ArticlePriceService.class);
    }


    @Bean
    public ArticleAttrService articleAttrService() {
        return getProxy(ArticleAttrService.class);
    }

    @Bean
    public FreedayService freedayService() {
        return getProxy(FreedayService.class);
    }

    @Bean
    public ShowPhotoService showPhotoService() {
        return getProxy(ShowPhotoService.class);
    }

    @Bean
    public com.resto.brand.web.service.ShowPhotoService showPhotoServiceBrands() {
        return getProxy(com.resto.brand.web.service.ShowPhotoService.class);
    }

    @Bean
    public BrandSettingService brandSettingService() {
        return getProxy(BrandSettingService.class);
    }

    @Bean
    public PlatformService platformService() {
        return getProxy(PlatformService.class);
    }

    @Bean
    public MealTempService mealTempService() {
        return getProxy(MealTempService.class);
    }

    @Bean
    public MealTempAttrService mealTempAttrService() {
        return getProxy(MealTempAttrService.class);
    }

    @Bean
    public MealAttrService mealAttrService() {
        return getProxy(MealAttrService.class);
    }

    @Bean
    public ModuleListService moduleListService() {
        return getProxy(ModuleListService.class);
    }

    @Bean
    public ShareSettingService shareSettingService() {
        return getProxy(ShareSettingService.class);
    }

    @Bean
    public ERoleService eRoleService() {
        return getProxy(ERoleService.class);
    }

    @Bean
    public RewardSettingService rewardSettingService() {
        return getProxy(RewardSettingService.class);
    }

    @Bean
    public SmsChargeOrderService smsChargeOrderService() {
        return proxy.create(SmsChargeOrderService.class);
    }

    @Bean
    public SmsAcountService smsAcountService() {
        return proxy.create(SmsAcountService.class);
    }

    @Bean
    public AddressInfoService addressInfoService() {
        return proxy.create(AddressInfoService.class);
    }

    @Bean
    public SmsTicketService smsTicketService() {
        return proxy.create(SmsTicketService.class);
    }

    @Bean
    public UnitService unitService() {
        return proxy.create(UnitService.class);
    }

    @Bean
    public ArticleRecommendService articleRecommendService() {
        return proxy.create(ArticleRecommendService.class);
    }


    @Bean
    public TableCodeService tableCodeService() {
        return proxy.create(TableCodeService.class);
    }

    @Bean
    public EmployeeService employeeService() {
        return getProxy(EmployeeService.class);
    }


    @Bean
    public LogBaseService logBaseService() {
        return proxy.create(LogBaseService.class);
    }


    @Bean
    public SysErrorService sysErrorService() {
        return proxy.create(SysErrorService.class);
    }

    @Bean
    public WeItemService weItemService() {
        return proxy.create(WeItemService.class);
    }

    @Bean
    public WeChargeLogService weChargeLogService() {
        return proxy.create(WeChargeLogService.class);
    }

    @Bean
    public ExperienceService experienceService() {
        return proxy.create(ExperienceService.class);
    }

    @Bean
    public com.resto.brand.web.service.EmployeeService employeeBrandService() {
        return proxy.create(com.resto.brand.web.service.EmployeeService.class);
    }

    @Bean
    public com.resto.brand.web.service.PermissionService brandPermissionService() {
        return proxy.create(com.resto.brand.web.service.PermissionService.class);
    }

    @Bean
    public RolePermissionService rolePermissionService() {
        return proxy.create(RolePermissionService.class);
    }

    @Bean
    public OrderExceptionService orderExceptionService() {
        return proxy.create(OrderExceptionService.class);
    }

    @Bean
    public TableQrcodeService tableQrcodeService() {
        return proxy.create(TableQrcodeService.class);
    }

    @Bean
    public OffLineOrderService offLineOrderService() {
        return proxy.create(OffLineOrderService.class);
    }

    @Bean
    public WeOrderDetailService weOrderDetailService() {
        return proxy.create(WeOrderDetailService.class);
    }

    @Bean
    public WeShopService weShopService() {
        return proxy.create(WeShopService.class);
    }

    @Bean
    public WeShopScoreService weShopScoreService() {
        return proxy.create(WeShopScoreService.class);
    }

    @Bean
    public GetNumberService getNumberService() {
        return proxy.create(GetNumberService.class);
    }

    @Bean
    public RedPacketService redPacketService() {
        return proxy.create(RedPacketService.class);
    }

    @Bean
    public PosUserService posUserService() {
        return proxy.create(PosUserService.class);
    }

    @Bean
    public OrderRemarkService orderRemarkService() {return  proxy.create(OrderRemarkService.class);}

    @Bean
    public WaitPictureService waitPictureService() {return  proxy.create(WaitPictureService.class);}

    //天气
    @Bean
    public  WetherService wetherService(){
        return proxy.create(WetherService.class);
    }


    @Bean
    public  DayDataMessageService dayDataMessageService(){
        return proxy.create(DayDataMessageService.class);
    }

    @Bean
    public  DayAppraiseMessageService dayAppraiseMessageService(){
        return  proxy.create(DayAppraiseMessageService.class);
    }

    @Bean
    public com.resto.brand.web.service.OrderRemarkService boOrderRemarkService() {return  proxy.create(com.resto.brand.web.service.OrderRemarkService.class);}

    @Bean
    public CustomerAddressService customerAddressService() {return  proxy.create(CustomerAddressService.class);}

    @Bean
    public RecommendCategoryArticleService recommendCategoryArticleService() {return  proxy.create(RecommendCategoryArticleService.class);}

    @Bean
    public RecommendCategoryService recommendCategoryService() {return  proxy.create(RecommendCategoryService.class);}

    @Bean
    public BonusSettingService bonusSettingService() {return  proxy.create(BonusSettingService.class);}

    @Bean
    public BonusLogService bonusLogService() {return  proxy.create(BonusLogService.class);}

    @Bean
    public NewEmployeeService newEmployeeService() {return  proxy.create(NewEmployeeService.class);}

    @Bean
    public WxServerConfigService wxServerConfigService(){return  proxy.create(WxServerConfigService.class);}

    @Bean
    public PlatformOrderService platformOrderService(){return  proxy.create(PlatformOrderService.class);}

    @Bean
    public ShopTvConfigService shopTvConfigService() {return  proxy.create(ShopTvConfigService.class);}


    //yz 2017-07-19-----------------
    @Bean
    public BrandAccountService brandAccountService(){
        return proxy.create(BrandAccountService.class);
    }

    @Bean
    public AccountChargeOrderService accountChargeOrderService(){
        return proxy.create(AccountChargeOrderService.class);
    }

	@Bean
	public  BrandAccountLogService brandAccountLogService(){
    	return proxy.create(BrandAccountLogService.class);
	}


	@Bean
	public  AccountTicketService accountTicketService(){

		return  proxy.create(AccountTicketService.class);
	}


	@Bean
	public AccountAddressInfoService accountAddressInfoService(){

		return proxy.create(AccountAddressInfoService.class);
	}

	@Bean
	public AccountSettingService accountSettingService(){
		return proxy.create(AccountSettingService.class);
	}


	@Bean
	public AccountNoticeService accountNoticeService(){
		return proxy.create(AccountNoticeService.class);
	}

    @Bean
    public MealItemService mealItemService(){
        return proxy.create(MealItemService.class);
    }

    @Bean
    public ProvinceService provinceService(){
        return proxy.create(ProvinceService.class);
    }

    @Bean
    public CityService cityService(){
        return proxy.create(CityService.class);
    }

    @Bean
    public DistrictService districtService(){
        return proxy.create(DistrictService.class);
    }

    @Bean
    public MemberActivityService memberActivityService() { return proxy.create(MemberActivityService.class); }

    //------------scm server config start-----------

    @Bean
    public ScmUnitService scmUnitService(){
	    return proxy.create(ScmUnitService.class);
    }
    @Bean
    public MaterialService materialService(){
        return proxy.create(MaterialService.class);
    }

    @Bean
    public CategoryService categoryService(){
        return proxy.create(CategoryService.class);
    }


    @Bean
    public ArticleBomHeadService articleBomHeadService(){
        return proxy.create(ArticleBomHeadService.class);
    }

    @Bean
    public StockCountCheckService stockCountCheckService(){
        return proxy.create(StockCountCheckService.class);
    }
    @Bean
    public StockInPlanService stockInPlanService(){
        return proxy.create(StockInPlanService.class);
    }
    @Bean
    public SupplierMaterialPriceService supplierMaterialPriceDetailService(){
        return proxy.create(SupplierMaterialPriceService.class);
    }

    @Bean
    public SupplierService supplierService(){
        return proxy.create(SupplierService.class);
    }

    @Bean
    public MaterialStockService materialStockService(){
        return proxy.create(MaterialStockService.class);
    }

    @Bean
    public MdBillService mdBillService(){
        return proxy.create(MdBillService.class);
    }

    @Bean
    public ArticleBomHeadHistoryService articleBomHeadHistoryService(){
        return proxy.create(ArticleBomHeadHistoryService.class);
    }

    @Bean
    public DocPmsPoHeaderService docPmsPoHeaderService(){
        return proxy.create(DocPmsPoHeaderService.class);
    }
    @Bean
    public DocReturnHeaderService docDocReturnHeaderService(){
        return proxy.create(DocReturnHeaderService.class);
    }


    //------------scm server config end-----------

    //-----------------------

    @Bean
    public GoodTopService goodTopService(){
	    return proxy.create(GoodTopService.class);
    }


    @Bean
    public PosService posService() {return  proxy.create(PosService.class);}

    public <T> T getProxy(Class<T> clazz) {
        return proxy.create(clazz);
    }

    public static void main(String[] args) {
        File file = new File("D:/work/RestoPlus/gitRepository/resto-shop/resto-shop-api/src/main/java/com/resto/shop/web/service");
        StringBuffer code = new StringBuffer();
        StringBuffer importPath = new StringBuffer();
        for (File f : file.listFiles()) {
            String classSimpleName = f.getName().substring(0, f.getName().length() - 5);
            code.append("\t@Bean\n");
            code.append("\tpublic " + classSimpleName + " " + (classSimpleName.charAt(0) + "").toLowerCase() + classSimpleName.substring(1) + "(){\n");
            code.append("\t\treturn getProxy(" + classSimpleName + ".class" + ");\n");
            code.append("\t}\n");
            importPath.append("import com.resto.shop.web.service." + classSimpleName + ";\n");
        }
        System.out.println(importPath);
        System.out.println(code);
    }
}
