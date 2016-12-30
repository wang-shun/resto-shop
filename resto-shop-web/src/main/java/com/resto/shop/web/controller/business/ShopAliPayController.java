package com.resto.shop.web.controller.business;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.brand.core.entity.Result;
import com.resto.brand.web.model.BrandSetting;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.service.BrandSettingService;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.shop.web.controller.GenericController;

@Controller
@RequestMapping("/aliPay")
public class ShopAliPayController extends GenericController{

	@Resource
    ShopDetailService shopDetailService;
	
	@Resource
	BrandSettingService brandSettingService;
	
	@RequestMapping("/list")
	public void list(){
	}

	@RequestMapping("/list_one")
    @ResponseBody
    public Result list_one(){
        ShopDetail shopDetail = shopDetailService.selectById(getCurrentShopId());
        BrandSetting brandSetting = brandSettingService.selectByBrandId(getCurrentBrandId());
        List<Object> list = new ArrayList<Object>();
        list.add(shopDetail);
        list.add(brandSetting);
        return getSuccessResult(list);
    }

    @RequestMapping("/modify")
    @ResponseBody
    public Result modify(ShopDetail shopDetail){
        shopDetail.setId(getCurrentShopId());
        shopDetailService.update(shopDetail);
        return Result.getSuccess();
    }
	
}
