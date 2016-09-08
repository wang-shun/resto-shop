package com.resto.shop.web.controller.business;

import com.resto.brand.core.entity.JSONResult;
import com.resto.brand.core.entity.Result;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.Article;
import com.resto.shop.web.model.ArticleRecommend;
import com.resto.shop.web.service.ArticleRecommendService;
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

    @RequestMapping("/articleList")
    public ModelAndView index(){
        return new ModelAndView("recommend/list");
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
        return Result.getSuccess();
    }

    @RequestMapping("save")
    @ResponseBody
    public Result create(@Valid @RequestBody ArticleRecommend articleRecommend){
        return new JSONResult<>();
    }
}
