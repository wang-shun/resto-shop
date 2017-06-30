package com.resto.shop.web.controller.business;

import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.RecommendCategory;
import com.resto.shop.web.service.RecommendCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * Created by xielc on 2017/6/29.
 */
@RequestMapping("recommendCategory")
@Controller
public class RecommendedCategoryController extends GenericController {

    @Autowired
    private RecommendCategoryService recommendCategoryService;

    @RequestMapping("/list")
    public ModelAndView index(){
       /* BrandSetting setting = brandSettingService.selectByBrandId(getCurrentBrandId());
        if(setting != null && setting.getRecommendArticle().equals(new Integer(1))){ //开启推荐餐包功能
            return new ModelAndView("recommend/list");
        }else{
            return new ModelAndView("recommend/none");
        }*/
        return new ModelAndView("recommendCategory/list");
    }

    @RequestMapping("/list_all")
    @ResponseBody
    public List<RecommendCategory> getList(){
        //List<RecommendCategory> result =  recommendCategoryService.getRecommendList(getCurrentShopId());
        List<RecommendCategory> result = recommendCategoryService.selectList();
        return result;
    }

}
