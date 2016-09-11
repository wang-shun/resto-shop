package com.resto.shop.web.controller.business;

import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.ArticleRecommend;
import com.resto.shop.web.model.Unit;
import com.resto.shop.web.service.UnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * Created by KONATA on 2016/9/11.
 */
@RequestMapping("unit")
@Controller
public class UnitController extends GenericController {

    @Autowired
    private UnitService unitService;

    @RequestMapping("/unitlist")
    public ModelAndView index() {
        return new ModelAndView("unit/list");
    }



    @RequestMapping("/list_all")
    @ResponseBody
    public List<Unit> getList(){
        List<Unit> result =  unitService.getUnits(getCurrentShopId());
        return result;
    }
}
