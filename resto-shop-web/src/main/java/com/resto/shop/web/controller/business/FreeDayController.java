 package com.resto.shop.web.controller.business;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.FreeDay;
import com.resto.shop.web.service.FreedayService;

@Controller
@RequestMapping("freeday")
public class FreeDayController extends GenericController{

	@Resource
	FreedayService freedayService;
	
	@RequestMapping("/list")
        public void list(){
        }
	
	@RequestMapping("addFreeDay")
        @ResponseBody
        public String addFreeDay(FreeDay freeDay,HttpServletRequest request) throws Exception{
	   String date = request.getParameter("FREE_DAY");
	   SimpleDateFormat formatter = new SimpleDateFormat( "yyyy-MM-dd ");
	   Date date2=  formatter.parse(date);
	   freeDay.setFreeDay(date2);
	   System.out.println(date2);
	    return null;
        }
	
	@RequestMapping("freeDayList")
        @ResponseBody
        public List<FreeDay> freeDayList(HttpServletRequest request) throws Exception{
	    FreeDay day = new FreeDay();
	    String begin = request.getParameter("begin");
	   String end = request.getParameter("end");
	   SimpleDateFormat formatter = new SimpleDateFormat( "yyyy-MM-dd ");
	   Date begin1 = formatter.parse(begin);
	   Date end1 = formatter.parse(end);
	   day.setBegin(begin1);
	   day.setEnd(end1);
	   day.setShopDetailId(getCurrentShopId());
	   List<FreeDay> list = freedayService.list(day);
	    return list;
        }

        

	/*@RequestMapping("/list_all")
	@ResponseBody
	public List<Account> listData(){
		return freedayService.selectList();
	}
	
	@RequestMapping("list_one")
	@ResponseBody
	public Result list_one(String id){
		Account account = freedayService.selectById(id);
		return getSuccessResult(account);
	}
	
	@RequestMapping("create")
	@ResponseBody
	public Result create(@Valid Account brand){
		freedayService.insert(brand);
		return Result.getSuccess();
	}
	
	@RequestMapping("modify")
	@ResponseBody
	public Result modify(@Valid Account brand){
		freedayService.update(brand);
		return Result.getSuccess();
	}
	
	@RequestMapping("delete")
	@ResponseBody
	public Result delete(String id){
		freedayService.delete(id);
		return Result.getSuccess();
	}*/
}
