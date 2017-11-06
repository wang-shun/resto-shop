package com.resto.shop.web.controller.scm;

import com.resto.brand.core.entity.Result;
import com.resto.brand.web.model.BrandSetting;
import com.resto.brand.web.service.BrandSettingService;
import com.resto.scm.web.model.MdRulArticleBomHead;
import com.resto.scm.web.service.MdBillService;
import com.resto.shop.web.constant.Common;
import com.resto.shop.web.controller.GenericController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * Created by bruce on 2017-11-06 10:11
 */
@Controller
@RequestMapping("scmBill")
public class BillController extends GenericController {

    @Resource
    private BrandSettingService brandSettingService;
    @Resource
    private MdBillService mdBillService;

    @RequestMapping("/list")
    public String list(){
        BrandSetting brandSetting = brandSettingService.selectByBrandId(getCurrentBrandId());
        if (brandSetting.getIsOpenScm().equals(Common.YES)){
            return "scmBill/list";
        }else {
            return "notopen";
        }
    }

    @RequestMapping("/list_all")
    @ResponseBody
    public Result listData(){
        return  getSuccessResult(mdBillService.findBillList(getCurrentShopId()));
    }

}
