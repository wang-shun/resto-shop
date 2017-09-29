 package com.resto.shop.web.controller.scm;

 import com.resto.brand.core.entity.Result;
 import com.resto.scm.web.dto.MdSupplierAndContactDo;
 import com.resto.scm.web.dto.MdSupplierDo;
 import com.resto.scm.web.model.MdSupplier;
 import com.resto.scm.web.service.SupplierService;
 import com.resto.shop.web.config.SessionKey;
 import com.resto.shop.web.controller.GenericController;
 import org.apache.shiro.session.Session;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;

 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 import javax.validation.Valid;
 import java.util.List;

@Controller
@RequestMapping("scmSupplier")
public class SupplierController extends GenericController{

	@Resource
	SupplierService supplierService;

	@RequestMapping("/list")
    public void list(){
    }

	@RequestMapping("/list_all")
	@ResponseBody
	public Result listData(HttpServletRequest request){
		return  getSuccessResult(supplierService.queryJoin4Page(getCurrentShopId()));
	}
	
	@RequestMapping("list_one")
	@ResponseBody
	public Result list_one(Long id){
		MdSupplier supplier = supplierService.queryById(id);
		return getSuccessResult(supplier);
	}

	@RequestMapping("create")
	@ResponseBody
	public Result create(@Valid @RequestBody  MdSupplierDo supplier){
		supplier.setShopDetailId(getCurrentShopId());
		supplier.setCreaterId(getCurrentUserId());
		supplier.setCreaterName(getCurrentBrandUser().getName());
		supplierService.addMdSupplier(supplier);
		return Result.getSuccess();
	}

	@RequestMapping("modify")
	@ResponseBody
	public Result modify(@Valid @RequestBody MdSupplierDo supplier){
		supplier.setShopDetailId(getCurrentShopId());
		supplier.setUpdaterId(getCurrentUserId());
		supplier.setUpdaterName(getCurrentBrandUser().getName());
		supplierService.updateMdSupplier(supplier);
		return Result.getSuccess();
	}
	
	@RequestMapping("delete")
	@ResponseBody
	public Result delete(Long id){
		supplierService.delete(id,getCurrentShopId());
		return Result.getSuccess();
	}
}
