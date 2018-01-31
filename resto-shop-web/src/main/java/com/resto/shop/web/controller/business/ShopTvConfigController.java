package com.resto.shop.web.controller.business;

import com.resto.brand.core.entity.Result;
import com.resto.brand.web.model.ShopTvConfig;
import com.resto.brand.web.service.ShopTvConfigService;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.service.PosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * Created by carl on 2017/7/17.
 */
@Controller
@RequestMapping("shopTvConfig")
public class ShopTvConfigController extends GenericController {

    @Resource
    ShopTvConfigService shopTvConfigService;

    @Autowired
    private PosService posService;

    @RequestMapping("/list")
    public void list(){
    }

    @RequestMapping("list_one")
    @ResponseBody
    public Result list_one(){
        ShopTvConfig shopTvConfig = shopTvConfigService.selectByShopId(getCurrentShopId());
        return getSuccessResult(shopTvConfig);
    }

    @RequestMapping("modify")
    @ResponseBody
    public Result modify(ShopTvConfig shopTvConfig){
        if(shopTvConfig.getShopDetailId() == null || "".equals(shopTvConfig.getShopDetailId())){
            shopTvConfig.setShopDetailId(getCurrentShopId());
            shopTvConfig.setBrandId(getCurrentBrandId());
            shopTvConfigService.insert(shopTvConfig);
        }else{
            shopTvConfigService.update(shopTvConfig);
        }
        posService.shopMsgChange(getCurrentShopId());
        return Result.getSuccess();
    }
}
