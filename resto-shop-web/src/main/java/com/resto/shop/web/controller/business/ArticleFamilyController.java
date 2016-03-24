 package com.resto.shop.web.controller.business;

import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.shop.web.controller.GenericController;
import com.resto.brand.core.entity.Result;
import com.resto.shop.web.model.ArticleFamily;
import com.resto.shop.web.service.ArticleFamilyService;

@Controller
@RequestMapping("articlefamily")
public class ArticleFamilyController extends GenericController{

	@Resource
	ArticleFamilyService articlefamilyService;
	
	@RequestMapping("/list")
    public void list(){
    }

	@RequestMapping("/list_all")
	@ResponseBody
	public List<ArticleFamily> listData(){
		return articlefamilyService.selectList();
	}
	
	@RequestMapping("list_one")
	@ResponseBody
	public Result list_one(String id){
		ArticleFamily articlefamily = articlefamilyService.selectById(id);
		return getSuccessResult(articlefamily);
	}
	
	@RequestMapping("create")
	@ResponseBody
	public Result create(@Valid ArticleFamily brand){
		articlefamilyService.insert(brand);
		return Result.getSuccess();
	}
	
	@RequestMapping("modify")
	@ResponseBody
	public Result modify(@Valid ArticleFamily brand){
		articlefamilyService.update(brand);
		return Result.getSuccess();
	}
	
	@RequestMapping("delete")
	@ResponseBody
	public Result delete(String id){
		articlefamilyService.delete(id);
		return Result.getSuccess();
	}
}
