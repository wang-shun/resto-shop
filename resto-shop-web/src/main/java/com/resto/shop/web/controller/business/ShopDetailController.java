package com.resto.shop.web.controller.business;

import java.util.List;

import javax.annotation.Resource;

import com.resto.brand.web.model.Brand;
import com.resto.brand.web.service.BrandService;
import com.resto.shop.web.util.LogTemplateUtils;
import com.resto.shop.web.util.RedisUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.brand.core.entity.Result;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.shop.web.controller.GenericController;

@Controller
@RequestMapping("shopDetail")
public class ShopDetailController extends GenericController{

	@Resource
	ShopDetailService shopDetailService;

	@Resource
	BrandService brandService;
	
	@RequestMapping("/list")
        public void list(){
	    
	}

	
	@RequestMapping("list_one")
	@ResponseBody
	public Result list_one(){
		ShopDetail  shopDetail = shopDetailService.selectById(getCurrentShopId());
		return getSuccessResult(shopDetail);
	}
	
	@RequestMapping("modify")
	@ResponseBody
	public Result modify(ShopDetail shopDetail){
	    shopDetail.setId(getCurrentShopId());
	    log.info(shopDetail.getIsOpenTablewareFee() + "22222");
	    if(shopDetail.getIsOpenSauceFee() == null){
	    	shopDetail.setIsOpenSauceFee(0);
		}
		if(shopDetail.getIsOpenTablewareFee() == null){
	    	shopDetail.setIsOpenTablewareFee(0);
		}
		if(shopDetail.getIsOpenTowelFee() == null){
			shopDetail.setIsOpenTowelFee(0);
		}
	    shopDetailService.update(shopDetail);
	    if(RedisUtil.get(shopDetail.getId()+"info") != null){
			RedisUtil.remove(shopDetail.getId()+"info");
		}
		Brand brand = brandService.selectByPrimaryKey(getCurrentBrandId());
		shopDetail = shopDetailService.selectByPrimaryKey(getCurrentShopId());
		LogTemplateUtils.shopDeatilEdit(brand.getBrandName(), shopDetail.getName(), getCurrentBrandUser().getUsername());

	    return Result.getSuccess();
	}
	
	@RequestMapping("list_all")
	@ResponseBody
	public Result listAll(){
	    List<ShopDetail> lists = shopDetailService.selectByBrandId(getCurrentBrandId());
	    return getSuccessResult(lists);
	}


	@RequestMapping("list_without_self")
	@ResponseBody
	public Result listWithoutSelf(){
		List<ShopDetail> lists = shopDetailService.selectByBrandId(getCurrentBrandId());
		for(int i = 0;i < lists.size();i++){
			if(lists.get(i).getId().equals(getCurrentShopId())){
				lists.remove(i);
			}
		}
		return getSuccessResult(lists);
	}
	
}
