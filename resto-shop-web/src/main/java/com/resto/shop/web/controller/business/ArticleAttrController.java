 package com.resto.shop.web.controller.business;

import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resto.brand.core.util.MemcachedUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.brand.core.entity.Result;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.ArticleAttr;
import com.resto.shop.web.service.ArticleAttrService;

@Controller
@RequestMapping("articleattr")
public class ArticleAttrController extends GenericController{

	@Resource
	ArticleAttrService articleattrService;

    private static final ObjectMapper MAPPER = new ObjectMapper();
	
	@RequestMapping("/list")
    public void list(){
    }

	@RequestMapping("/list_all")
	@ResponseBody
	public List<ArticleAttr> listData(){
		return articleattrService.selectListByShopId(getCurrentShopId());
	}
	
	@RequestMapping("list_one")
	@ResponseBody
	public Result list_one(Integer id){
		ArticleAttr articleattr = articleattrService.selectById(id);
		return getSuccessResult(articleattr);
	}
	
	@RequestMapping("create")
	@ResponseBody
	public Result create(@Valid ArticleAttr articleAttr){
		articleAttr.setShopDetailId(getCurrentShopId());
		articleattrService.create(articleAttr);
		MemcachedUtils.delete(getCurrentShopId()+"articleAttr");
		return Result.getSuccess();
	}
	
	@RequestMapping("modify")
	@ResponseBody
	public Result modify(@Valid ArticleAttr brand){
		articleattrService.updateInfo(brand);
		MemcachedUtils.delete(getCurrentShopId()+"articleAttr");
		return Result.getSuccess();
	}
	
	@RequestMapping("delete")
	@ResponseBody
	public Result delete(Integer id){
		articleattrService.deleteInfo(id);
		MemcachedUtils.delete(getCurrentShopId()+"articleAttr");
		return Result.getSuccess();
	}
}
