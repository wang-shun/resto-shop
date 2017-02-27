package com.resto.shop.web.controller.business;

import com.resto.brand.core.entity.Result;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.Kitchen;
import com.resto.shop.web.model.VirtualProducts;
import com.resto.shop.web.model.VirtualProductsAndKitchen;
import com.resto.shop.web.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by yangwei on 2017/2/22.
 * 虚拟餐品Controller
 */
@RequestMapping("virtual")
@Controller
public class VirtualProductsController extends GenericController {
    @Autowired
    private VirtualProductsService virtualProductsService;


    @Autowired
    private KitchenService kitchenService;

    @RequestMapping("/list")
    public ModelAndView index() {
        return new ModelAndView("virtual/list");
    }

    @RequestMapping("/listAll")
    @ResponseBody
    public List<VirtualProducts> getVirtuals(){
        List<VirtualProducts> result=virtualProductsService.getAllProducuts(getCurrentShopId());
        List<Kitchen> kitchens = new ArrayList<>();
        for (VirtualProducts virtualProducts:result) {
           List<VirtualProductsAndKitchen> VirtualProductsAndKitchens=virtualProductsService.getVirtualProductsAndKitchenById(virtualProducts.getId());
            if(VirtualProductsAndKitchens.size()>0){
                for (VirtualProductsAndKitchen virtualProductsAndKitchen:VirtualProductsAndKitchens) {
                    Kitchen kitchen=kitchenService.selectById(virtualProductsAndKitchen.getKitchenId());
                    if(kitchen != null){
                        kitchens.add(kitchen);
                    }
                }
            }
            virtualProducts.setKitchens(kitchens);
        }
        return result;
    }

    @RequestMapping("create")
    @ResponseBody
    public Result create(@Valid VirtualProducts virtualProducts){
        virtualProducts.setShopDetailId(getCurrentShopId());
        virtualProducts.setCreateTime(new Date());
        virtualProductsService.insert(virtualProducts);
        System.out.println("==============我已经保存好了");
        VirtualProductsAndKitchen virtualProductsAndKitchen=new VirtualProductsAndKitchen();
        virtualProductsAndKitchen.setKitchenId(virtualProducts.getId());
        for (Kitchen k:virtualProducts.getKitchens()) {
            virtualProductsAndKitchen.setVirtualId(k.getId());
        }
        virtualProductsService.insertVirtualProductsKitchen(virtualProductsAndKitchen);
        System.out.println("---------保存完成");
        return Result.getSuccess();
    }

    @RequestMapping("modify")
    @ResponseBody
    public Result modify(@Valid VirtualProducts virtualProducts){
        virtualProducts.setShopDetailId(getCurrentShopId());
        virtualProducts.setCreateTime(new Date());
        virtualProductsService.updateVirtual(virtualProducts);
        //修改关系表时，先删除当前条，然后在添加
        virtualProductsService.deleteVirtualById(virtualProducts.getId());
        VirtualProductsAndKitchen virtualProductsAndKitchen=new VirtualProductsAndKitchen();
        virtualProductsAndKitchen.setKitchenId(virtualProducts.getId());
        for (Kitchen k:virtualProducts.getKitchens()) {
            virtualProductsAndKitchen.setVirtualId(k.getId());
        }
        virtualProductsService.insertVirtualProductsAndKitchen(virtualProductsAndKitchen);

        return Result.getSuccess();
    }

    @RequestMapping("delete")
    @ResponseBody
    public Result delete(Integer id){
        virtualProductsService.deleteById(id);
        virtualProductsService.deleteVirtualById(id);
        return Result.getSuccess();
    }

    @RequestMapping("/getVirtualById")
    @ResponseBody
    public VirtualProducts getVirtualById(Integer id) {
        VirtualProducts virtualProducts=virtualProductsService.getVirtualProductsById(id);
        List<Kitchen> kitchens = new ArrayList<>();
        List<VirtualProductsAndKitchen> VirtualProductsAndKitchens=virtualProductsService.getVirtualProductsAndKitchenById(virtualProducts.getId());
        if(VirtualProductsAndKitchens.size()>0){
            for (VirtualProductsAndKitchen virtualProductsAndKitchen:VirtualProductsAndKitchens) {
                Kitchen kitchen=kitchenService.selectById(virtualProductsAndKitchen.getKitchenId());
                if(kitchen != null){
                    kitchens.add(kitchen);
                }
            }
        }
        virtualProducts.setKitchens(kitchens);
        return virtualProducts;
    }

}
