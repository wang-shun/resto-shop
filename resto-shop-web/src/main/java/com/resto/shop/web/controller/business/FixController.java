package com.resto.shop.web.controller.business;
import com.resto.brand.core.entity.Result;
import com.resto.brand.web.model.Brand;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.model.WechatConfig;
import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.brand.web.service.WechatConfigService;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.annotation.Resource;

@Controller
@RequestMapping("fix")
public class FixController extends GenericController{

	@Resource
	private OrderService orderService;

	@Resource
    private ShopDetailService shopDetailService;

	@Resource
    private BrandService brandService;

	@Resource
    private WechatConfigService wechatConfigService;

	/*
	每日推送功能校准
	 */
	@RequestMapping("dayMessage")
    @ResponseBody
	public  Result dayMessage(String shopId,String telePhone){
	    //定义店铺
	    ShopDetail s  = shopDetailService.selectByPrimaryKey(shopId);
	    //查品牌
        WechatConfig wechatConfig = wechatConfigService.selectByBrandId(getCurrentBrandId());

        orderService.cleanShopOrderFix(s,wechatConfig,telePhone);
        return  getSuccessResult();
    }
}
