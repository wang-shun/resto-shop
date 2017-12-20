 package com.resto.shop.web.controller.business;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.resto.shop.web.constant.PrinterRange;
import com.resto.shop.web.service.PosService;
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

	@Resource
	PosService posService;
	
	@RequestMapping("/list")
    public void list(){
    }

	@RequestMapping("/list_all")
	@ResponseBody
	public List<Printer> listData(){
		return printerService.selectListByShopId(getCurrentShopId());
	}

	@RequestMapping("/qiantai")
	@ResponseBody
	public List<Printer> qiantai(){
		return printerService.selectQiantai(getCurrentShopId(), PrinterRange.QUYU);
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
		posService.shopMsgChange(getCurrentShopId());
		return Result.getSuccess();
	}
	
	@RequestMapping("modify")
	@ResponseBody
	public Result modify(@Valid Printer printer){
		if (printer.getBillOfAccount() == null){
			printer.setBillOfAccount(0);
		}
		if (printer.getBillOfConsumption() == null){
			printer.setBillOfConsumption(0);
		}
		printerService.update(printer);
		posService.shopMsgChange(getCurrentShopId());
		return Result.getSuccess();
	}
	
	@RequestMapping("delete")
	@ResponseBody
	public Result delete(Integer id){
		printerService.delete(id);
		posService.shopMsgChange(getCurrentShopId());
		return Result.getSuccess();
	}
}
