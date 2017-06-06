 package com.resto.shop.web.controller.business;

import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.shop.web.controller.GenericController;
import com.resto.brand.core.entity.Result;
import com.resto.shop.web.model.DayDataMessage;
import com.resto.shop.web.service.DayDataMessageService;

@Controller
@RequestMapping("daydatamessage")
public class DayDataMessageController extends GenericController{

	@Resource
	DayDataMessageService daydatamessageService;
	
	@RequestMapping("/list")
    public void list(){
    }

	@RequestMapping("/list_all")
	@ResponseBody
	public List<DayDataMessage> listData(){
		return daydatamessageService.selectList();
	}
	
	@RequestMapping("list_one")
	@ResponseBody
	public Result list_one(String id){
		DayDataMessage daydatamessage = daydatamessageService.selectById(id);
		return getSuccessResult(daydatamessage);
	}
	
	@RequestMapping("create")
	@ResponseBody
	public Result create(@Valid DayDataMessage daydatamessage){
		daydatamessageService.insert(daydatamessage);
		return Result.getSuccess();
	}
	
	@RequestMapping("modify")
	@ResponseBody
	public Result modify(@Valid DayDataMessage daydatamessage){
		daydatamessageService.update(daydatamessage);
		return Result.getSuccess();
	}
	
	@RequestMapping("delete")
	@ResponseBody
	public Result delete(String id){
		daydatamessageService.delete(id);
		return Result.getSuccess();
	}
}
