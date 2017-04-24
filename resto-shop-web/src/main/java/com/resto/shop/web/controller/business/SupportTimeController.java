 package com.resto.shop.web.controller.business;

import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.shop.web.controller.GenericController;
import com.resto.brand.core.entity.Result;
import com.resto.shop.web.model.SupportTime;
import com.resto.shop.web.service.SupportTimeService;

@Controller
@RequestMapping("supporttime")
public class SupportTimeController extends GenericController{

	@Resource
	SupportTimeService supporttimeService;
	
	@RequestMapping("/list")
           public void list(){
        }

	@RequestMapping("/list_all")
	@ResponseBody
	public List<SupportTime> listData(){
	        String shopDetailId=getCurrentShopId();
		return supporttimeService.selectList(shopDetailId);
	}
	
	@RequestMapping("list_one")
	@ResponseBody
	public Result list_one(Integer id){
		SupportTime supporttime = supporttimeService.selectById(id);
		return getSuccessResult(supporttime);
	}
	
	@RequestMapping("create")
	@ResponseBody
	public Result create(@Valid SupportTime brand){
	    brand.setShopDetailId(getCurrentShopId());
		int count=0;
		for(int i=0;i<brand.getBeginTime().length();i++){
			if(brand.getBeginTime().charAt(i)==':'){
				count++;
			}
		}
		if(count == 1){
			brand.setBeginTime(brand.getBeginTime()+":00");
		}
		int number=0;
		for(int j=0;j<brand.getEndTime().length();j++){
			if(brand.getEndTime().charAt(j)==':'){
				number++;
			}
		}
		if(number == 1){
			brand.setEndTime(brand.getEndTime()+":59");
		}
		supporttimeService.insert(brand);
		return Result.getSuccess();
	}
	
	@RequestMapping("modify")
	@ResponseBody
	public Result modify(@Valid SupportTime brand){
		int count=0;
		for(int i=0;i<brand.getBeginTime().length();i++){
			if(brand.getBeginTime().charAt(i)==':'){
				count++;
			}
		}
		if(count == 1){
			brand.setBeginTime(brand.getBeginTime()+":00");
		}
		int number=0;
		for(int j=0;j<brand.getEndTime().length();j++){
			if(brand.getEndTime().charAt(j)==':'){
				number++;
			}
		}
		if(number == 1){
			brand.setEndTime(brand.getEndTime()+":59");
		}
		supporttimeService.update(brand);
		return Result.getSuccess();
	}
	
	@RequestMapping("delete")
	@ResponseBody
	public Result delete(Integer id){
		supporttimeService.delete(id);
		return Result.getSuccess();
	}
}
