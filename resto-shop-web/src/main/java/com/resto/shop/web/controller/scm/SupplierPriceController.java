 package com.resto.shop.web.controller.scm;

 import com.resto.brand.core.entity.Result;
 import com.resto.scm.web.dto.MdSupplierPriceHeadDo;
 import com.resto.scm.web.service.SupplierMaterialPriceService;
 import com.resto.shop.web.controller.GenericController;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;

 import javax.annotation.Resource;
 import javax.validation.Valid;
 import java.util.List;

@Controller
@RequestMapping("scmSupplerPrice")
public class SupplierPriceController extends GenericController{

	@Resource
	SupplierMaterialPriceService supplierpriceService;
	
	@RequestMapping("/list")
    public void list(){
    }

	@RequestMapping("/list_all")
	@ResponseBody
	public Result listData(String contactId){
		List<MdSupplierPriceHeadDo> mdSupplierPriceHeadDos = supplierpriceService.queryJoin4Page(getCurrentShopId(), contactId);
		return getSuccessResult(mdSupplierPriceHeadDos);
	}
	
	@RequestMapping("list_one")
	@ResponseBody
	public Result list_one(Long id){
		//MdSupplierPriceHeadDo supplierprice = supplierpriceService.updateMdSupplierPriceStatus()
		return getSuccessResult(null);
	}
	
	@RequestMapping("create")
	@ResponseBody
	public Result create(@Valid MdSupplierPriceHeadDo supplierprice){
		supplierpriceService.addMdSupplierPrice(supplierprice);
		return Result.getSuccess();
	}
	
	@RequestMapping("modify")
	@ResponseBody
	public Result modify(@Valid MdSupplierPriceHeadDo supplierprice){
		supplierpriceService.updateMdSupplierPrice(supplierprice);
		return Result.getSuccess();
	}
	
	@RequestMapping("delete")
	@ResponseBody
	public Result delete(Long id){
		supplierpriceService.deleteById(id);
		return Result.getSuccess();
	}
}
