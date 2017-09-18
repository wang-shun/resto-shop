// package com.resto.shop.web.controller.scm;
//
//import java.util.List;
//
//import javax.annotation.Resource;
//import javax.validation.Valid;
//
//import com.resto.scm.web.dto.MaterialStockDo;
//import com.resto.scm.web.service.MaterialStockService;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.ResponseBody;
//
//import com.resto.shop.web.controller.GenericController;
//import com.resto.brand.core.entity.Result;
//
//
//@Controller
//@RequestMapping("materialstock")
//public class MaterialStockController extends GenericController{
//
//	@Resource
//	MaterialStockService materialstockService;
//
//	@RequestMapping("/list")
//    public void list(){
//    }
//
//	@RequestMapping("/list_all")
//	@ResponseBody
//	public Result listData(){
//
//		return getSuccessResult(null);
//
//	}
//
//	@RequestMapping("list_one")
//	@ResponseBody
//	public Result list_one(Long id){
//		MaterialStock materialstock = materialstockService.
//		return getSuccessResult(null);
//	}
//
//	@RequestMapping("create")
//	@ResponseBody
//	public Result create(@Valid MaterialStock materialstock){
//		materialstockService.insert(materialstock);
//		return Result.getSuccess();
//	}
//
//	@RequestMapping("modify")
//	@ResponseBody
//	public Result modify(@Valid MaterialStock materialstock){
//		materialstockService.update(materialstock);
//		return Result.getSuccess();
//	}
//
//	@RequestMapping("delete")
//	@ResponseBody
//	public Result delete(Long id){
//		materialstockService.delete(id);
//		return Result.getSuccess();
//	}
//}
