package com.resto.shop.web.dao;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.VirtualProducts;
import com.resto.shop.web.model.VirtualProductsAndKitchen;

import java.util.List;

/**
 * Created by yangwei on 2017/2/22.
 */
public interface VirtualProductsMapper extends GenericDao<VirtualProducts, String> {
    VirtualProducts getVirtualProductsById(int id);

    List<VirtualProductsAndKitchen> getVirtualProductsAndKitchenById(int virtualId);

    List<VirtualProducts> selectAll(String shopId);
}
