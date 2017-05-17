 package com.resto.shop.web.controller.business;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.validation.Valid;

import com.resto.brand.web.model.ShopDetail;
import com.resto.shop.web.model.NewEmployee;
import com.resto.shop.web.service.NewEmployeeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.shop.web.controller.GenericController;
import com.resto.brand.core.entity.Result;
import com.resto.shop.web.model.BonusLog;
import com.resto.shop.web.service.BonusLogService;

@Controller
@RequestMapping("bonusLog")
public class BonusLogController extends GenericController{

	@Resource
	BonusLogService bonusLogService;

    @Resource
    NewEmployeeService newEmployeeService;
	
	@RequestMapping("/list")
    public void list(){
    }

	@RequestMapping("/list_all")
	@ResponseBody
	public Result listData(){
	    try {
            Map<String, Object> map = new HashMap<>();
            List<Map<String, Object>> bonusLogs = bonusLogService.selectAllBonusLog();
            List<ShopDetail> shopDetails = getCurrentShopDetails();
            for (Map bonusLog : bonusLogs){
                for (ShopDetail shopDetail : shopDetails){
                    if (bonusLog.get("shopId").toString().equals(shopDetail.getId())){
                        bonusLog.put("shopName", shopDetail.getName());
                        break;
                    }
                }
                if (bonusLog.get("emNames") != null){
                    if (StringUtils.isNotBlank(bonusLog.get("emNames").toString())) {
                        String[] emNames = bonusLog.get("emNames").toString().split(",");
                        for (String emName : emNames) {
                            String[] name = emName.split(":");
                            if (Integer.valueOf(name[0]) == 1) {
                                bonusLog.put("employeeName", name[1]);
                            } else {
                                bonusLog.put("shopownerName", name[1]);
                            }
                        }
                    }
                }
            }
            List<NewEmployee> newEmployees = newEmployeeService.selectList();
            List<NewEmployee> employees = new ArrayList<>();
            List<NewEmployee> shopowners = new ArrayList<>();
            for (NewEmployee newEmployee : newEmployees){
                if (newEmployee.getRoleType() == 1){
                    employees.add(newEmployee);
                }else{
                    shopowners.add(newEmployee);
                }
            }
            map.put("employees",employees);
            map.put("shopowners",shopowners);
            map.put("bonusLogs",bonusLogs);
            return getSuccessResult(map);
        }catch (Exception e){
            e.printStackTrace();
            log.error("查看所有分红记录出错！");
            return new Result(false);
        }
	}
	
	@RequestMapping("list_one")
	@ResponseBody
	public Result list_one(String id){
		BonusLog bonuslog = bonusLogService.selectById(id);
		return getSuccessResult(bonuslog);
	}
	
	@RequestMapping("modify")
	@ResponseBody
	public Result modify(@Valid BonusLog bonuslog){
        bonusLogService.update(bonuslog);
		return Result.getSuccess();
	}
}
