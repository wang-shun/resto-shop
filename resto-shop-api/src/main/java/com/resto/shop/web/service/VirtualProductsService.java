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

    List<VirtualProducts> selectAll(String shopId);
}
