package com.resto.shop.web.controller.business;

import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.service.WeightPackageDetailService;
import com.resto.shop.web.service.WeightPackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by carl on 2018/03/19.
 */
@RequestMapping("WeightPackage")
@Controller
public class WeightPackageController extends GenericController {

    @Autowired
    private WeightPackageService weightPackageService;

    @Autowired
    private WeightPackageDetailService weightPackageDetailService;

    @RequestMapping("/list")
    public void list(){
    }

}
