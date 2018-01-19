//package com.resto.shop.web.controller.scm;
//import com.resto.brand.core.entity.Result;
//import com.resto.brand.web.model.BrandSetting;
//import com.resto.brand.web.service.BrandSettingService;
//import com.resto.scm.web.dto.MaterialStockDo;
//import com.resto.scm.web.service.MaterialStockService;
//import com.resto.shop.web.constant.Common;
//import com.resto.shop.web.controller.GenericController;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.ResponseBody;
//import javax.annotation.Resource;
//import java.util.List;
//
///**
// * 原料库存
// */
//@Controller
//@RequestMapping("scmMaterialStock")
//public class MaterialStockController extends GenericController {
//
//    @Resource
//    private MaterialStockService materialstockService;
//
//    @Resource
//    BrandSettingService brandSettingService;
//
//
//    @RequestMapping("/list")
//    public String list(){
//        BrandSetting brandSetting = brandSettingService.selectByBrandId(getCurrentBrandId());
//        if (brandSetting.getIsOpenScm().equals(Common.YES)){
//            return "scmMaterialStock/list";
//        }else {
//            return "notopen";
//        }
//    }
//
//    @RequestMapping("/list_all")
//    @ResponseBody
//    public Result listData(String shopId) {
//        String shopDetailId =StringUtils.isEmpty(shopId)?this.getCurrentShopId():shopId;
//        List<MaterialStockDo> list = materialstockService.queryJoin4Page(shopDetailId);
//        return getSuccessResult(list);
//
//    }
//
//
//}
