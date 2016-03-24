 package com.resto.shop.web.controller.business;

import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.shop.web.controller.GenericController;
import com.resto.brand.core.entity.Result;
import com.resto.shop.web.model.NewCustomCoupon;
import com.resto.shop.web.service.NewCustomCouponService;

@Controller
@RequestMapping("newcustomcoupon")
public class NewCustomCouponController extends GenericController{

	@Resource
	NewCustomCouponService newcustomcouponService;
	
	@RequestMapping("/list")
    public void list(){
    }

	@RequestMapping("/list_all")
	@ResponseBody
	public List<NewCustomCoupon> listData(){
		return newcustomcouponService.selectList();
	}
	
	@RequestMapping("list_one")
	@ResponseBody
	public Result list_one(Long id){
		NewCustomCoupon newcustomcoupon = newcustomcouponService.selectById(id);
		return getSuccessResult(newcustomcoupon);
	}
	
	@RequestMapping("create")
	@ResponseBody
	public Result create(@Valid NewCustomCoupon brand){
		newcustomcouponService.insert(brand);
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
}
