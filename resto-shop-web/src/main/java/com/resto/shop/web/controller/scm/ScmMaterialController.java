 package com.resto.shop.web.controller.scm;

 import com.resto.brand.core.entity.Result;
 import com.resto.scm.web.dto.MaterialDo;
 import com.resto.scm.web.model.MdMaterial;
 import com.resto.scm.web.service.MaterialService;
 import com.resto.shop.web.controller.GenericController;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;

 import javax.validation.Valid;
 import java.util.List;

 @Controller
 @RequestMapping("scmMaterial")
 public class ScmMaterialController extends GenericController {

	 @Autowired
	 MaterialService materialService;

	 @RequestMapping("/list")
	 public void list(){
	 }

	 @RequestMapping("/list_all")
	 @ResponseBody
	 public Result listData(MdMaterial mdMaterial){
		 List<MaterialDo> list = materialService.queryJoin4Page();
		return getSuccessResult(list);
	 }

	 @RequestMapping("list_one")
	 @ResponseBody
	 public Result list_one(Long id){
		 MdMaterial mdMaterial = materialService.queryById(id);
		 return getSuccessResult(mdMaterial);
	 }

	 @RequestMapping("create")
	 @ResponseBody
	 public Result create(@Valid MdMaterial mdMaterial){
		 mdMaterial.setShopDetailId(getCurrentShopId());
		 int i = materialService.addMaterial(mdMaterial);
		 if(i>0){
			 return Result.getSuccess();
		 }
		 return new Result("保存失败", 5000,false);
	 }

	 @RequestMapping("modify")
	 @ResponseBody
	 public Result modify(@Valid MdMaterial mdMaterial){
		 Integer row = materialService.updateMaterial(mdMaterial);
		 return Result.getSuccess();
	 }

	 @RequestMapping("delete")
	 @ResponseBody
	 public Result delete(Long id){
		 Integer row = materialService.deleteById(id);
		 return Result.getSuccess();
	 }
 }
