 package com.resto.shop.web.controller.business;

import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.shop.web.controller.GenericController;
import com.resto.brand.core.entity.Result;
import com.resto.shop.web.model.BonusSetting;
import com.resto.shop.web.service.BonusSettingService;

@Controller
@RequestMapping("bonussetting")
public class BonusSettingController extends GenericController{

	@Resource
	BonusSettingService bonussettingService;
	
	@RequestMapping("/list")
    public void list(){
    }

	@RequestMapping("/list_all")
	@ResponseBody
	public List<BonusSetting> listData(){
		return bonussettingService.selectList();
	}
	
	@RequestMapping("list_one")
	@ResponseBody
	public Result list_one(String id){
		BonusSetting bonussetting = bonussettingService.selectById(id);
		return getSuccessResult(bonussetting);
	}
	
	@RequestMapping("create")
	@ResponseBody
	public Result create(@Valid BonusSetting bonussetting){
		bonussettingService.insert(bonussetting);
		return Result.getSuccess();
	}
	
	@RequestMapping("modify")
	@ResponseBody
	public Result modify(@Valid BonusSetting bonussetting){
		bonussettingService.update(bonussetting);
		return Result.getSuccess();
	}
	
	@RequestMapping("delete")
	@ResponseBody
	public Result delete(String id){
		bonussettingService.delete(id);
		return Result.getSuccess();
	}
}
