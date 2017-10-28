 package com.resto.shop.web.controller.business;


import javax.annotation.Resource;
import javax.validation.Valid;

import com.resto.brand.core.util.MemcachedUtils;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.shop.web.constant.Common;
import com.resto.shop.web.util.LogTemplateUtils;
import com.resto.shop.web.util.RedisUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.shop.web.controller.GenericController;
import com.resto.brand.core.entity.Result;
import com.resto.brand.web.model.Brand;
import com.resto.brand.web.model.BrandSetting;
import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.BrandSettingService;

@Controller
@RequestMapping("brandSetting")
public class BrandSettingController extends GenericController{

	@Resource
	BrandSettingService brandSettingService;
	
	@Resource
	BrandService brandService;

	@Resource
	ShopDetailService shopDetailService;
	
	@RequestMapping("/list")
    public void list(){
    }

	@RequestMapping("list_one")
	@ResponseBody
	public Result list_one(){
		Brand brand = brandService.selectById(getCurrentBrandId());
		String brandSettingId = brand.getBrandSettingId();
		BrandSetting brandSetting = brandSettingService.selectById(brandSettingId);
		return getSuccessResult(brandSetting);
	}


	@RequestMapping("modify")
	@ResponseBody
	public Result modify(@Valid BrandSetting brandSetting){
		brandSettingService.update(brandSetting);
		/*if(RedisUtil.get(getCurrentBrandId()+"setting") != null){
			RedisUtil.remove(getCurrentBrandId()+"setting");
		}*/
		Brand brand = brandService.selectByPrimaryKey(getCurrentBrandId());
		RedisUtil.clean(brand.getId(), brand.getBrandSign());
		ShopDetail shopDetail = shopDetailService.selectByPrimaryKey(getCurrentShopId());
		LogTemplateUtils.brandSettingEdit(brand.getBrandName(), shopDetail.getName(), getCurrentBrandUser().getUsername());

		return Result.getSuccess();
	}
	
}
