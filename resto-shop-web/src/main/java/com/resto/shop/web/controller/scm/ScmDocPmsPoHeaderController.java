 package com.resto.shop.web.controller.scm;

import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.shop.web.controller.GenericController;
import com.resto.brand.core.entity.Result;
import com.resto.scm.web.model.DocPmsPoHeader;
import com.resto.scm.web.service.DocPmsPoHeaderService;

@Controller
@RequestMapping("scmDocPmsPoHeader")
public class ScmDocPmsPoHeaderController extends GenericController{

	@Resource
	DocPmsPoHeaderService docPmsPoHeaderService;
	
	@RequestMapping("/list")
    public void list(){
    }

	@RequestMapping("/list_all")
	@ResponseBody
	public List<DocPmsPoHeader> listData(){
		return docPmsPoHeaderService.selectList();
	}
	
	@RequestMapping("list_one")
	@ResponseBody
	public Result list_one(Long id){
		DocPmsPoHeader docPmsPoHeader = docPmsPoHeaderService.selectById(id);
		return getSuccessResult(docPmsPoHeader);
	}
	
	@RequestMapping("create")
	@ResponseBody
	public Result create(@Valid DocPmsPoHeader docPmsPoHeader){
		docPmsPoHeaderService.insert(docPmsPoHeader);
		return Result.getSuccess();
	}
	
	@RequestMapping("modify")
	@ResponseBody
	public Result modify(@Valid DocPmsPoHeader docPmsPoHeader){
		docPmsPoHeaderService.update(docPmsPoHeader);
		return Result.getSuccess();
	}
	
	@RequestMapping("delete")
	@ResponseBody
	public Result delete(Long id){
		docPmsPoHeaderService.deleteById(id);
		return Result.getSuccess();
	}
}
