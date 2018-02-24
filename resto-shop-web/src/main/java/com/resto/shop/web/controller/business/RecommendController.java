package com.resto.shop.web.controller.business;

import com.resto.brand.core.entity.Result;
import com.resto.brand.core.util.ApplicationUtils;
import com.resto.brand.web.model.BrandSetting;
import com.resto.brand.web.service.BrandSettingService;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.ArticleRecommend;
import com.resto.shop.web.service.ArticleRecommendService;
import com.resto.shop.web.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.List;

/**
 * Created by KONATA on 2016/9/8.
 */
@RequestMapping("recommend")
@Controller
public class RecommendController extends GenericController {

    @Autowired
    private ArticleRecommendService articleRecommendService;

    @Autowired
    private BrandSettingService brandSettingService;

    @Autowired
    private ArticleService articleService;

    @RequestMapping("/articleList")
    public ModelAndView index(){
        BrandSetting setting = brandSettingService.selectByBrandId(getCurrentBrandId());
        if(setting != null && setting.getRecommendArticle().equals(new Integer(1))){ //开启推荐餐包功能
            return new ModelAndView("recommend/list");
        }else{
            return new ModelAndView("recommend/none");
        }

    }


    @RequestMapping("/list_all")
    @ResponseBody
    public List<ArticleRecommend> getList(){
        List<ArticleRecommend> result =  articleRecommendService.getRecommendList(getCurrentShopId());
        return result;
    }

    @RequestMapping("/delete")
    @ResponseBody
    public Result delete(String id){
        articleRecommendService.delete(id);
        //将餐品设置成 无推荐餐品
        articleService.deleteRecommendId(id);
        return Result.getSuccess();
    }

    @RequestMapping("/create")
    @ResponseBody
    public Result create(@Valid @RequestBody ArticleRecommend articleRecommend){
        //创建主表
        String id = ApplicationUtils.randomUUID();
        articleRecommend.setId(id);
        articleRecommend.setShopId(getCurrentShopId());
        articleRecommendService.insert(articleRecommend);
        articleRecommendService.insertRecommendArticle(id,articleRecommend.getArticles());
        return new Result(true);
    }


    @RequestMapping("/getRecommendById")
    @ResponseBody
    public ArticleRecommend getRecommendById(String id){
        return articleRecommendService.getRecommendById(id);
    }

    @RequestMapping("/modify")
    @ResponseBody
    public Result modify(@Valid @RequestBody ArticleRecommend articleRecommend){
        articleRecommendService.update(articleRecommend);
        articleRecommendService.updateRecommendArticle(articleRecommend.getId(),articleRecommend.getArticles());
        return Result.getSuccess();
    }

}
