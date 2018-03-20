package com.resto.shop.web.controller.business;

import com.resto.brand.core.entity.Result;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.WeightPackage;
import com.resto.shop.web.service.WeightPackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;

/**
 * Created by carl on 2018/03/19.
 */
@RequestMapping("weightPackage")
@Controller
public class WeightPackageController extends GenericController {

    @Autowired
    private WeightPackageService weightPackageService;

    @RequestMapping("/list")
    public void list(){
    }

    @RequestMapping("/list_all")
    @ResponseBody
    public List<WeightPackage> getList() {
        List<WeightPackage> result = weightPackageService.getAllWeightPackages(getCurrentShopId());
        return result;
    }

    @RequestMapping("/create")
    @ResponseBody
    public Result create(@Valid @RequestBody WeightPackage weightPackage) {
        //创建主表
        weightPackage.setShopId(getCurrentShopId());
        weightPackage.setCreateTime(new Date());
        weightPackageService.insert(weightPackage);
        //创建属性
        weightPackageService.insertDetail(weightPackage);
        return new Result(true);
    }

    @RequestMapping("/modify")
    @ResponseBody
    public Result modify(@Valid @RequestBody WeightPackage weightPackage) {
        weightPackageService.update(weightPackage);
        weightPackageService.initWeightPackageDetail(weightPackage);
        //创建属性
        WeightPackage u = weightPackageService.insertDetail(weightPackage);

        return new Result(true);
    }
}
