package com.resto.shop.web.controller.business;

import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.Kitchen;
import com.resto.shop.web.model.VirtualProducts;
import com.resto.shop.web.model.VirtualProductsAndKitchen;
import com.resto.shop.web.service.KitchenService;
import com.resto.shop.web.service.VirtualProductsService;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

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

    @RequestMapping("/list_all")
    public Map<String,Object> getVirtuals(){
        List<VirtualProducts> productses=virtualProductsService.selectAll(getCurrentShopId());
        List<Kitchen> kitchens=null;
        for (VirtualProducts virtualProducts:productses) {
           List<VirtualProductsAndKitchen> productsAndKitchens=virtualProductsService.getVirtualProductsAndKitchenById(virtualProducts.getId());
            for (VirtualProductsAndKitchen virtualProductsAndKitchen:productsAndKitchens) {
                Kitchen kitchen=kitchenService.selectById(virtualProductsAndKitchen.getKitchenId());
                kitchens.add(kitchen);
            }
            virtualProducts.setKitchen(kitchens);
        }
        Map<String,Object> map=new HashedMap();
        map.put("productses",productses);

        return map;
    }




}
