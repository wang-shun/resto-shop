 package com.resto.shop.web.controller.business;

import javax.annotation.Resource;
import javax.validation.Valid;

import com.alibaba.fastjson.JSONArray;
import com.resto.brand.web.model.ShopDetail;
import com.resto.shop.web.model.ChargeSetting;
import com.resto.shop.web.service.ChargeSettingService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.shop.web.controller.GenericController;
import com.resto.brand.core.entity.Result;
import com.resto.shop.web.model.BonusSetting;
import com.resto.shop.web.service.BonusSettingService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

 @Controller
@RequestMapping("bonusSetting")
public class BonusSettingController extends GenericController{

	@Resource
	BonusSettingService bonussettingService;

    @Resource
    ChargeSettingService chargeSettingService;

	@RequestMapping("/list")
    public void list(){
    }

	@RequestMapping("/list_all")
	@ResponseBody
	public Result listData(){
	    try{
            Map<String, Object> map = new HashMap<>();
            List<BonusSetting> bonusSettings = bonussettingService.selectList();
            List<ShopDetail> shopDetails = getCurrentShopDetails();
            List<ChargeSetting> chargeSettings = chargeSettingService.selectList();
            for (BonusSetting setting : bonusSettings){
                for (ShopDetail shopDetail : shopDetails){
                    if (setting.getShopDetailId().equalsIgnoreCase(shopDetail.getId())){
                        setting.setShopName(shopDetail.getName());
                        break;
                    }
                }
                for (ChargeSetting chargeSetting : chargeSettings){
                    if (setting.getChargeSettingId().equalsIgnoreCase(chargeSetting.getId())){
                        setting.setChargeName(chargeSetting.getLabelText());
                        chargeSettings.remove(chargeSetting);
                        break;
                    }
                }
            }
            map.put("bonusSettings",bonusSettings);
            map.put("chargeSettings",chargeSettings);
	        return getSuccessResult(map);
        }catch (Exception e){
            e.printStackTrace();
            log.error("查看所有分红设置出错！");
            return new Result(false);
        }
	}
	
	@RequestMapping("create")
	@ResponseBody
	public Result create(@Valid BonusSetting bonussetting){
	    try{
            bonussettingService.insert(bonussetting);
            return getSuccessResult();
        }catch (Exception e){
            e.printStackTrace();
            log.error("新建分红设置出错！");
            return new Result(false);
        }
	}
	
	@RequestMapping("modify")
	@ResponseBody
	public Result modify(@Valid BonusSetting bonussetting){
		bonussettingService.update(bonussetting);
		return Result.getSuccess();
	}
}
