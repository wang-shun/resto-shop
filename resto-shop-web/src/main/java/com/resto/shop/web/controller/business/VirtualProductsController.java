package com.resto.shop.web.controller.business;

import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.Kitchen;
import com.resto.shop.web.model.VirtualProducts;
import com.resto.shop.web.model.VirtualProductsAndKitchen;
import com.resto.shop.web.service.*;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
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

    @RequestMapping("list_all")
    public Map<String,Object> getVirtuals(){
        List<VirtualProducts> virtualProducts=virtualProductsService.getAllProducuts(getCurrentShopId());
//        System.out.print(result.size()+"------------------------size");
//        List<Kitchen> kitchens=null;
//        for (VirtualProducts virtualProducts:result) {
//           List<VirtualProductsAndKitchen> VirtualProductsAndKitchens=virtualProductsService.getVirtualProductsAndKitchenById(virtualProducts.getId());
//            for (VirtualProductsAndKitchen virtualProductsAndKitchen:VirtualProductsAndKitchens) {
//                Kitchen kitchen=kitchenService.selectById(virtualProductsAndKitchen.getKitchenId());
//                kitchens.add(kitchen);
//                System.out.print(kitchen.getName()+"-----------------------name");
////                if(virtualProducts.getShopDetailId().equals(kitchen.getShopDetailId())){
////                    kitchens.add(kitchen);
////                    System.out.print("我就是逃不出去了");
////                    break;
////                }
//                System.out.print(kitchens.size()+"------------------------size");
//                break;
//            }
//            virtualProducts.setKitchens(kitchens);
//            result.add(virtualProducts);
//            break;
//        }
        System.out.print("我已经跳出循环了");

        Map<String,Object> map=new HashedMap();
        map.put("virtualProducts",virtualProducts);
        return map;
    }

    @RequestMapping("/create")
    public void insertVirtualProducts(@Valid VirtualProducts virtualProducts,@Valid VirtualProductsAndKitchen virtualProductsAndKitchen){
        virtualProducts.setShopDetailId(getCurrentShopId());
        virtualProductsService.insert(virtualProducts);
        virtualProductsService.insertVirtualProductsKitchen(virtualProductsAndKitchen);
    }

    @RequestMapping("modify")
    public void updateVirtualProducts(@Valid VirtualProducts virtualProducts){
        virtualProducts.setShopDetailId(getCurrentShopId());
        virtualProductsService.update(virtualProducts);
    }

    @RequestMapping("delete")
    public void deleteVirtualProducts(Integer id){
    }

}
