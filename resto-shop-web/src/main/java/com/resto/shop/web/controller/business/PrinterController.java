 package com.resto.shop.web.controller.business;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.shop.web.config.SessionKey;
import com.resto.shop.web.controller.GenericController;
import com.resto.brand.core.entity.Result;
import com.resto.shop.web.model.Printer;
import com.resto.shop.web.service.PrinterService;

@Controller
@RequestMapping("printer")
public class PrinterController extends GenericController{

	@Resource
	PrinterService printerService;
	
	@RequestMapping("/list")
    public void list(){
    }

	@RequestMapping("/list_all")
	@ResponseBody
	public List<Printer> listData(){
		return printerService.selectList();
	}
	
	@RequestMapping("list_one")
	@ResponseBody
	public Result list_one(Integer id){
		Printer printer = printerService.selectById(id);
		return getSuccessResult(printer);
	}
	
	@RequestMapping("create")
	@ResponseBody
	public Result create(@Valid Printer printer,HttpServletRequest request){
		printer.setShopDetailId(request.getSession().getAttribute(SessionKey.CURRENT_SHOP_ID).toString());
		printerService.insert(printer);
		return Result.getSuccess();
	}
	
	@RequestMapping("modify")
	@ResponseBody
	public Result modify(@Valid Printer printer){
		printerService.update(printer);
		return Result.getSuccess();
	}
	
	@RequestMapping("delete")
	@ResponseBody
	public Result delete(Integer id){
		printerService.delete(id);
		return Result.getSuccess();
	}
}
