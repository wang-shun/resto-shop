 package com.resto.shop.web.controller.business;

import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.shop.web.controller.GenericController;
import com.resto.brand.core.entity.Result;
import com.resto.shop.web.model.BonusLog;
import com.resto.shop.web.service.BonusLogService;

@Controller
@RequestMapping("bonuslog")
public class BonusLogController extends GenericController{

	@Resource
	BonusLogService bonuslogService;
	
	@RequestMapping("/list")
    public void list(){
    }

	@RequestMapping("/list_all")
	@ResponseBody
	public List<BonusLog> listData(){
		return bonuslogService.selectList();
	}
	
	@RequestMapping("list_one")
	@ResponseBody
	public Result list_one(String id){
		BonusLog bonuslog = bonuslogService.selectById(id);
		return getSuccessResult(bonuslog);
	}
	
	@RequestMapping("create")
	@ResponseBody
	public Result create(@Valid BonusLog bonuslog){
		bonuslogService.insert(bonuslog);
		return Result.getSuccess();
	}
	
	@RequestMapping("modify")
	@ResponseBody
	public Result modify(@Valid BonusLog bonuslog){
		bonuslogService.update(bonuslog);
		return Result.getSuccess();
	}
	
	@RequestMapping("delete")
	@ResponseBody
	public Result delete(String id){
		bonuslogService.delete(id);
		return Result.getSuccess();
	}
}
