 package com.resto.shop.web.controller.business;

 import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.brand.core.entity.Result;
import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.BrandSettingService;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.Article;
import com.resto.shop.web.model.ArticlePrice;
import com.resto.shop.web.service.ArticlePriceService;
import com.resto.shop.web.service.ArticleService;

@Controller
@RequestMapping("article")
public class ArticleController extends GenericController{

	@Resource
	ArticleService articleService;

	@Resource
	ArticlePriceService articlePriceService;

	@Resource
	BrandSettingService brandSettingService;

	@Resource
	BrandService brandService;
	
	@RequestMapping("/list")
    public void list(){
    }

	@RequestMapping("/list_all")
	@ResponseBody
	public List<Article> listData(){
		List<Article> articles =  articleService.selectList(getCurrentShopId());
		return articles;
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
		Article article = articleService.selectFullById(id, "");
		return getSuccessResult(article);
	}


	@RequestMapping("save")
	@ResponseBody
	public Result create(@Valid @RequestBody Article article){
		article.setShopDetailId(getCurrentShopId());
		article.setUpdateUserId(getCurrentUserId());
		article.setUpdateTime(new Date());	
		if(StringUtils.isEmpty(article.getId())){
			article.setCreateUserId(getCurrentUserId());
			articleService.save(article);
		}else{
			List<ArticlePrice> list = articlePriceService.selectByArticleId(article.getId());
			if(article.getIsEmpty() == true){
				if(article.getArticleType() == 1 && list.size() ==0){
					article.setCurrentWorkingStock(0);
					articleService.update(article);
				} else if(article.getArticleType() == 1 && list.size() !=0){
					article.setCurrentWorkingStock(0);
					articleService.update(article);
					for(ArticlePrice ap :list){
						ap.setCurrentWorkingStock(0);
						articlePriceService.update(ap);
					}
				}
			}else {
				if(article.getArticleType() == 1 && list.size() ==0) {
					if (IsFreeday(new Date())) {
						article.setCurrentWorkingStock(article.getStockWorkingDay());
						articleService.update(article);
					} else {
						article.setCurrentWorkingStock(article.getStockWeekend());
						articleService.update(article);
					}
				} else if (article.getArticleType() == 1 && list.size() !=0){
					if (IsFreeday(new Date())) {
						for(ArticlePrice ap : list){
							ap.setCurrentWorkingStock(ap.getStockWorkingDay());
							articlePriceService.update(ap);
						}
					} else {
						for(ArticlePrice ap : list){
							ap.setCurrentWorkingStock(ap.getStockWeekend());
							articlePriceService.update(ap);
						}
					}
				}
			}
		}
        articleService.initStock();
		return Result.getSuccess();
	}
	
	@RequestMapping("delete")
	@ResponseBody
	public Result delete(String id){
		articleService.delete(id);
		return Result.getSuccess();
	}
	
	public boolean IsFreeday(Date time){
		Calendar cal = Calendar.getInstance();
		cal.setTime(time);
		int dayForWeek = 0;
		if(cal.get(Calendar.DAY_OF_WEEK) == 1){
			dayForWeek = 7;
		}else{
			dayForWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
		}
		if(dayForWeek > 5){
			return false;
		}
		return true;
	}
}
