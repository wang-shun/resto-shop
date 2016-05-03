 package com.resto.shop.web.controller.business;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.shop.web.controller.GenericController;
import com.resto.brand.core.entity.Result;
import com.resto.brand.core.util.ApplicationUtils;
import com.resto.shop.web.model.Article;
import com.resto.shop.web.model.ArticlePrice;
import com.resto.shop.web.service.ArticleService;

@Controller
@RequestMapping("article")
public class ArticleController extends GenericController{

	@Resource
	ArticleService articleService;
	
	@RequestMapping("/list")
    public void list(){
    }

	@RequestMapping("/list_all")
	@ResponseBody
	public List<Article> listData(){
		return articleService.selectList(getCurrentShopId());
	}
	
	@RequestMapping("list_one")
	@ResponseBody
	public Result list_one(String id){
		Article article = articleService.selectById(id);
		return getSuccessResult(article);
	}
	
	@RequestMapping("list_one_full")
	@ResponseBody
	public Result list_one_full(String id){
		Article article = articleService.selectFullById(id);
		return getSuccessResult(article);
	}
	
	@RequestMapping("save")
	@ResponseBody
	public Result create(@Valid @RequestBody Article article,Integer [] hasUnitIds,String[]unit_ids,String[]unitNames,Double []unitPrices,Double[]unitFansPrices,String[] unitPeferences){
		String articleId = article.getId();
		boolean isCreate = false;
		if(StringUtils.isEmpty(articleId)){
			articleId = ApplicationUtils.randomUUID();
			article.setId(articleId);
			article.setCreateUserId(getCurrentUserId());
			isCreate=true;
		}else{
			article.setUpdateTime(new Date());			
		}
		article.setShopDetailId(getCurrentShopId());
		article.setUpdateUserId(getCurrentUserId());
		
		if(isCreate){
			articleService.save(article);
		}else{
			articleService.update(article);
		}
		return Result.getSuccess();
	}
	
	@RequestMapping("delete")
	@ResponseBody
	public Result delete(String id){
		articleService.delete(id);
		return Result.getSuccess();
	}
}
