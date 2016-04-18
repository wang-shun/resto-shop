 package com.resto.shop.web.controller.business;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.brand.core.entity.Result;
import com.resto.brand.web.model.DistributionMode;
import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.DistributionModeService;
import com.resto.shop.web.config.SessionKey;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.NewCustomCoupon;
import com.resto.shop.web.service.NewCustomCouponService;

@Controller
@RequestMapping("newcustomcoupon")
public class NewCustomCouponController extends GenericController{

	@Resource
	NewCustomCouponService newcustomcouponService;
	
	@Resource
	DistributionModeService distributionmodeService;
	
	@Resource
	BrandService brandService;
	
	@RequestMapping("/list")
	public void list(){
        }

	@RequestMapping("/list_all")
	@ResponseBody
	public List<NewCustomCoupon> listData(){
		return newcustomcouponService.selectListByBrandId(getCurrentBrandId());
		
	}
	
	@RequestMapping("list_one")
	@ResponseBody
	public Result list_one(Long id){
		NewCustomCoupon newcustomcoupon = newcustomcouponService.selectById(id);
		return getSuccessResult(newcustomcoupon);
	}
	
	@RequestMapping("create")
	@ResponseBody
	public Result create(@Valid NewCustomCoupon brand, HttpServletRequest request){
	        System.out.println(brand.getBeginTime());
	        String brandId = (String) request.getSession().getAttribute(SessionKey.CURRENT_BRAND_ID);
	        brand.setBrandId(brandId);
	        brand.setCreateTime(new Date());
		newcustomcouponService.insertNewCustomCoupon(brand);
		return Result.getSuccess();
	}
	
	@RequestMapping("modify")
	@ResponseBody
	public Result modify(@Valid NewCustomCoupon brand){
		newcustomcouponService.update(brand);
		return Result.getSuccess();
	}
	
	@RequestMapping("delete")
	@ResponseBody
	public Result delete(Long id){
		newcustomcouponService.delete(id);
		return Result.getSuccess();
	}
	
	@RequestMapping("distributionmode/list_all")
	@ResponseBody
	public List<DistributionMode> lists(){
            return distributionmodeService.selectList();
        }
	@RequestMapping("distributionMode/list_one")
	@ResponseBody
	public DistributionMode listOne(Integer id){
	   return distributionmodeService.selectById(id);
	}
	
}	
