 package com.resto.shop.web.controller.business;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.brand.core.entity.Result;
import com.resto.brand.core.util.ApplicationUtils;
import com.resto.brand.web.model.DistributionMode;
import com.resto.brand.web.service.DistributionModeService;
import com.resto.shop.web.config.SessionKey;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.ArticleFamily;
import com.resto.shop.web.service.ArticleFamilyService;

@Controller
@RequestMapping("articlefamily")
public class ArticleFamilyController extends GenericController{

	@Resource
	ArticleFamilyService articlefamilyService;
	@Resource
	DistributionModeService distributionModeService;
	
	@RequestMapping("/list")
    public void list(){
    }

	@RequestMapping("/list_all")
	@ResponseBody
	public List<ArticleFamily> listData(){
		return articlefamilyService.selectList(getCurrentShopId());
	}
	
	@RequestMapping("list_one")
	@ResponseBody
	public Result list_one(String id){
		ArticleFamily articlefamily = articlefamilyService.selectById(id);
		return getSuccessResult(articlefamily);
	}
	
	@RequestMapping("create")
	@ResponseBody
	public Result create(@Valid ArticleFamily articleFamily,HttpServletRequest request){
		articleFamily.setShopDetailId(request.getSession().getAttribute(SessionKey.CURRENT_SHOP_ID).toString());
		articleFamily.setId(ApplicationUtils.randomUUID());
		articlefamilyService.insert(articleFamily);
		return Result.getSuccess();
	}
	
	@RequestMapping("modify")
	@ResponseBody
	public Result modify(@Valid ArticleFamily articleFamily){
		articlefamilyService.update(articleFamily);
		return Result.getSuccess();
	}
	
	@RequestMapping("delete")
	@ResponseBody
	public Result delete(String id){
		articlefamilyService.delete(id);
		return Result.getSuccess();
	}
	
	@RequestMapping("querydistributionMode")
	@ResponseBody
	public Result querydistributionMode(){
		List<DistributionMode> distributions =  distributionModeService.selectList();
		return getSuccessResult(distributions);
	}
}
