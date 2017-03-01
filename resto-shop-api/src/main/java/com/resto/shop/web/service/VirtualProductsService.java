package com.resto.shop.web.service;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.VirtualProducts;
import com.resto.shop.web.model.VirtualProductsAndKitchen;

import java.util.List;

/**
 * Created by yangwei on 2017/2/22.
 */
public interface VirtualProductsService extends GenericService<VirtualProducts, String> {
    VirtualProducts getVirtualProductsById(int id);

    List<VirtualProductsAndKitchen> getVirtualProductsAndKitchenById(int virtualId);

    List<VirtualProducts> getAllProducuts(String shopId);

    void insertVirtualProducts(VirtualProducts virtualProducts);

    void insertVirtualProductsKitchen(VirtualProductsAndKitchen virtualProductsAndKitchen);

    void insertVirtualProductsAndKitchen(VirtualProductsAndKitchen virtualProductsAndKitchen);

    void deleteById(Integer id);

    void deleteVirtualById(Integer virtualId);

    void updateVirtual(VirtualProducts virtualProducts);

    Integer selectMaxId();
}
