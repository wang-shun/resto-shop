 package com.resto.shop.web.controller.scm;

 import com.resto.brand.core.entity.Result;
 import com.resto.common.dao.domain.PageResult;
 import com.resto.scm.web.model.MdCategory;
 import com.resto.scm.web.model.MdUnit;
 import com.resto.scm.web.service.CategoryService;
 import com.resto.scm.web.service.ScmUnitService;
 import com.resto.shop.web.controller.GenericController;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;

 import javax.validation.Valid;
 import java.util.List;

 @Controller
 @RequestMapping("scmCategory")
 public class ScmCategoryController extends GenericController {

	 @Autowired
     CategoryService categoryService;

	 @RequestMapping("/list")
	 public void list(){
	 }

	 @RequestMapping("/list_all")
	 @ResponseBody
	 public Result listData(MdCategory mdCategory){
		 PageResult<MdCategory> list = categoryService.query4Page(mdCategory);

		return getSuccessResult(list);
	 }

	 @RequestMapping("/list_categoryHierarchy")
	 @ResponseBody
	 public Result list_categoryHierarchy(Integer categoryHierarchy){
		 List<MdCategory> list = categoryService.queryByCategoryHierarchy(categoryHierarchy);
		 return getSuccessResult(list);
	 }



	 @RequestMapping("list_one")
	 @ResponseBody
	 public Result list_one(Long id){
		 MdCategory mdCategory = categoryService.queryById(id);
		 return getSuccessResult(mdCategory);
	 }

	 @RequestMapping("create")
	 @ResponseBody
	 public Result create(@Valid MdCategory mdCategory){
		 int i = categoryService.addCategory(mdCategory);
		 if(i>0){
			 return Result.getSuccess();
		 }
		 return new Result("保存失败", 5000,false);
	 }

	 @RequestMapping("modify")
	 @ResponseBody
	 public Result modify(@Valid MdCategory mdCategory){
		 Integer row = categoryService.updateMdCategory(mdCategory);
		 return Result.getSuccess();
	 }

	 @RequestMapping("delete")
	 @ResponseBody
	 public Result delete(Long id){
		 Integer row = categoryService.deleteById(id);
		 return Result.getSuccess();
	 }
 }
