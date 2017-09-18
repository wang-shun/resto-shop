 package com.resto.shop.web.controller.scm;

 import com.resto.brand.core.entity.Result;
 import com.resto.scm.web.dto.MdSupplierAndContactDo;
 import com.resto.scm.web.dto.MdSupplierDo;
 import com.resto.scm.web.model.MdSupplier;
 import com.resto.scm.web.service.SupplierService;
 import com.resto.shop.web.controller.GenericController;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;

 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 import javax.validation.Valid;
 import java.util.List;

@Controller
@RequestMapping("upplier")
public class SupplierController extends GenericController{

	@Resource
	SupplierService supplierService;

	@RequestMapping("/list")
    public void list(){
    }

	@RequestMapping("/list_all")
	@ResponseBody
	public List<MdSupplierAndContactDo> listData(HttpServletRequest request){
		return supplierService.queryJoin4Page(getCurrentShopId());
	}
	
	@RequestMapping("list_one")
	@ResponseBody
	public Result list_one(Long id){
		MdSupplier supplier = supplierService.queryById(id);
		return getSuccessResult(supplier);
	}

	@RequestMapping("create")
	@ResponseBody
	public Result create(@Valid MdSupplierDo supplier){
		supplierService.addMdSupplier(supplier);
		return Result.getSuccess();
	}

	@RequestMapping("modify")
	@ResponseBody
	public Result modify(@Valid MdSupplierDo supplier){
		supplierService.updateMdSupplier(supplier);
		return Result.getSuccess();
	}
	
	@RequestMapping("delete")
	@ResponseBody
	public Result delete(Long id){
		supplierService.delete(id);
		return Result.getSuccess();
	}
}
