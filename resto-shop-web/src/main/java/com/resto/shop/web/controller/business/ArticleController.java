 package com.resto.shop.web.controller.business;

 import com.resto.brand.core.entity.Result;
 import com.resto.brand.web.service.BrandService;
 import com.resto.brand.web.service.BrandSettingService;
 import com.resto.shop.web.controller.GenericController;
 import com.resto.shop.web.model.Article;
 import com.resto.shop.web.service.ArticleService;
 import org.apache.commons.lang3.StringUtils;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;

 import javax.annotation.Resource;
 import javax.validation.Valid;
 import java.util.Date;
 import java.util.List;

@Controller
@RequestMapping("article")
public class ArticleController extends GenericController{

	@Resource
	ArticleService articleService;


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
		Article article = articleService.selectFullById(id);
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
