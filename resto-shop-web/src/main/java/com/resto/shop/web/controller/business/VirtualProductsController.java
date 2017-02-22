package com.resto.shop.web.controller.business;

import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.service.UnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * Created by yangwei on 2017/2/22.
 * 虚拟餐品Controller
 */
@RequestMapping("virtual")
@Controller
public class VirtualProductsController extends GenericController {
    @Autowired
    private UnitService unitService;

    @RequestMapping("/list")
    public ModelAndView index() {
        return new ModelAndView("virtual/list");
    }

    @RequestMapping("/list_all")
    public void getVirtual(){

    }

}
