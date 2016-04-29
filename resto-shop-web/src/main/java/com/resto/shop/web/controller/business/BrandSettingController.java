 package com.resto.shop.web.controller.business;


import javax.annotation.Resource;
import javax.validation.Valid;

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
		return Result.getSuccess();
	}
	
}
