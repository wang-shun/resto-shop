 package com.resto.shop.web.controller.business;

import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;

import com.resto.brand.core.util.DateUtil;
import com.resto.shop.web.constant.MessageType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.shop.web.controller.GenericController;
import com.resto.brand.core.entity.Result;
import com.resto.shop.web.model.DayDataMessage;
import com.resto.shop.web.service.DayDataMessageService;
import org.springframework.web.servlet.ModelAndView;

 @Controller
@RequestMapping("daydatamessage")
public class DayDataMessageController extends GenericController{

	@Resource
	DayDataMessageService daydatamessageService;

	@RequestMapping("/list")
    public ModelAndView list(){
	    //判断是否要显示旬 和 月
       int type= DateUtil.getEarlyMidLate();
       ModelAndView mv = new ModelAndView();
       mv.setViewName("daydatamessage/list");
       mv.addObject("type",type);
       return mv;
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

    @RequestMapping("getShopData")
    @ResponseBody
    public Result getShopData(String date,Integer type){
	    //根据状态(正常/删除) 时间 类型(1,2,3日/旬/月)
        List<DayDataMessage> dayDataMessageList = daydatamessageService.selectListByTime(MessageType.NORMAL,date,type);
        return getSuccessResult(dayDataMessageList);
    }


}
