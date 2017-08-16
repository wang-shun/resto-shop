package com.resto.shop.web.controller.business;

import com.alibaba.fastjson.JSONObject;
import com.resto.brand.core.entity.Result;
import com.resto.brand.core.util.MemcachedUtils;
import com.resto.brand.web.model.Brand;
import com.resto.brand.web.model.BrandSetting;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.BrandSettingService;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.shop.web.constant.Common;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.util.LogTemplateUtils;
import com.resto.shop.web.util.RedisUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServlet;
import javax.validation.Valid;
import java.util.List;

/**
 * Created by carl on 2016/12/5.
 */
@Controller
@RequestMapping("shopInfo")
public class ShopInfoController extends GenericController{
    @Resource
    ShopDetailService shopDetailService;
    @Resource
    BrandService brandService;
    @Resource
    BrandSettingService brandSettingService;

    @RequestMapping("/list")
    public void list(){

    }
   /***
    * name:yjunay
    * 服务设置
    *
     **/

    @RequestMapping("list_one")
    @ResponseBody
    public Result list_one(){
        JSONObject object = new JSONObject();
        ShopDetail shopDetail = shopDetailService.selectById(getCurrentShopId());
        object.put("shop",shopDetail);
        BrandSetting brandSetting = brandSettingService.selectByBrandId(getCurrentBrandId());
        object.put("brand",brandSetting);
        return getSuccessResult(object);
    }


    /**
     * name:yjuany
     * @param shopDetail
     * @return
     */
    @RequestMapping("modify")
    @ResponseBody
    public Result modify(ShopDetail shopDetail){
        shopDetail.setId(getCurrentShopId());
        if(shopDetail.getIsUserIdentity()!=null && shopDetail.getIsUserIdentity()!=0){
            switch (shopDetail.getConsumeConfineUnit()){
                case 1 :
                    shopDetail.setConsumeConfineTime(shopDetail.getConsumeConfineTime());
                    break;
                case 2 ://月
                    shopDetail.setConsumeConfineTime(shopDetail.getConsumeConfineTime()*30);
                    break;
                case 3 :
                    shopDetail.setConsumeConfineTime(Integer.MAX_VALUE);
                    break;
            }
        }
        if(shopDetail.getPrintReceipt() == null){
            shopDetail.setPrintReceipt(0);
        }
        if(shopDetail.getPrintKitchen() == null){
            shopDetail.setPrintKitchen(0);
        }
        if (shopDetail.getModifyOrderPrintReceipt() == null){
            shopDetail.setModifyOrderPrintReceipt(0);
        }
        if (shopDetail.getModifyOrderPrintKitchen() == null){
            shopDetail.setModifyOrderPrintKitchen(0);
        }
        if (shopDetail.getBadAppraisePrintKitchen() == null){
            shopDetail.setBadAppraisePrintKitchen(false);
        }
        if (shopDetail.getBadAppraisePrintReceipt() == null){
            shopDetail.setBadAppraisePrintReceipt(false);
        }
        if(shopDetail.getIsOpenSms()==0){//表示是关闭日短信通知
            shopDetail.setnoticeTelephone("");
        }else  if(shopDetail.getIsOpenSms()==1){
            shopDetail.setnoticeTelephone(shopDetail.getnoticeTelephone().replace("，",","));
        }
//        shopDetailService.updateWithDatong(shopDetail,getCurrentBrandId(),getBrandName());
        ShopDetail shopDetail1 =(ShopDetail) RedisUtil.get(getCurrentShopId()+"info");
        if(shopDetail != null){
            RedisUtil.remove(getCurrentShopId()+"info");
        }
        Brand brand = brandService.selectByPrimaryKey(getCurrentBrandId());
        shopDetail = shopDetailService.selectByPrimaryKey(getCurrentShopId());
        LogTemplateUtils.shopDeatilEdit(brand.getBrandName(), shopDetail.getName(), getCurrentBrandUser().getUsername());
        return Result.getSuccess();
    }




    @RequestMapping("list_all")
    @ResponseBody
    public Result listAll(){
        List<ShopDetail> lists = shopDetailService.selectByBrandId(getCurrentBrandId());
        return getSuccessResult(lists);
    }

}
