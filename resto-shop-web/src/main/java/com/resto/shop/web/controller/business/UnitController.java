package com.resto.shop.web.controller.business;

import com.resto.brand.core.entity.Result;
import com.resto.brand.core.util.ApplicationUtils;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.Unit;
import com.resto.shop.web.service.PosService;
import com.resto.shop.web.service.UnitService;
import com.resto.shop.web.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.List;

/**
 * Created by KONATA on 2016/9/11.
 */
@RequestMapping("unit")
@Controller
public class UnitController extends GenericController {

    @Autowired
    private UnitService unitService;

    @Autowired
    PosService posService;

    @RequestMapping("/unitlist")
    public ModelAndView index() {
        return new ModelAndView("unit/list");
    }


    @RequestMapping("/list_all")
    @ResponseBody
    public List<Unit> getList() {
        List<Unit> result = unitService.getUnits(getCurrentShopId());
        return result;
    }


    @RequestMapping("/list_all_id")
    @ResponseBody
    public List<Unit> getListById(String articleId) {
        List<Unit> result = unitService.getUnitsByArticleId(getCurrentShopId(), articleId);
        return result;
    }


    @RequestMapping("/create")
    @ResponseBody
    public Result create(@Valid @RequestBody Unit unit) {
        //创建主表
        String id = ApplicationUtils.randomUUID();
        unit.setId(id);
        unit.setShopId(getCurrentShopId());
        unitService.insert(unit);
        //创建属性
        unitService.insertDetail(unit);
        if(RedisUtil.get(getCurrentShopId()+"unit") != null){
            RedisUtil.remove(getCurrentShopId()+"unit");
        }
        posService.shopMsgChange(getCurrentShopId());
        return new Result(true);
    }

    @RequestMapping("/modify")
    @ResponseBody
    public Result modify(@Valid @RequestBody Unit unit) {
        unitService.update(unit);
        unitService.initUnit(unit);
        //创建属性
        Unit u = unitService.insertDetail(unit);

        //同步更新 使用该规格包的菜品信息
        unitService.modifyUnit(u);
        if(RedisUtil.get(getCurrentShopId()+"unit") != null){
            RedisUtil.remove(getCurrentShopId()+"unit");
        }
        posService.shopMsgChange(getCurrentShopId());
        return new Result(true);
    }

    @RequestMapping("/delete")
    @ResponseBody
    public Result delete(String id) {
        unitService.delete(id);
        unitService.deleteUnit(id);
        if(RedisUtil.get(getCurrentShopId()+"unit") != null){
            RedisUtil.remove(getCurrentShopId()+"unit");
        }
        posService.shopMsgChange(getCurrentShopId());
        return Result.getSuccess();
    }

    @RequestMapping("/getUnitById")
    @ResponseBody
    public Unit getUnitById(String id) {
        return unitService.getUnitById(id);
    }


}
