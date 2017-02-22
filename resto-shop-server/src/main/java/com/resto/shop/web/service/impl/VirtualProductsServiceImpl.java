package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.VirtualProductsMapper;
import com.resto.shop.web.model.Kitchen;
import com.resto.shop.web.model.VirtualProducts;
import com.resto.shop.web.model.VirtualProductsAndKitchen;
import com.resto.shop.web.service.VirtualProductsService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by yangwei on 2017/2/22.
 */
@RpcService
public class VirtualProductsServiceImpl extends GenericServiceImpl<VirtualProducts, String> implements VirtualProductsService{
    @Autowired
    private VirtualProductsMapper virtualProductsMapper;


    @Override
    public GenericDao<VirtualProducts, String> getDao() {
        return virtualProductsMapper;
    }

    @Override
    public VirtualProducts getVirtualProductsById(int id) {
        return virtualProductsMapper.getVirtualProductsById(id);
    }

    @Override
    public VirtualProductsAndKitchen getVirtualProductsAndKitchenById(int virtualId) {
        return virtualProductsMapper.getVirtualProductsAndKitchenById(virtualId);
    }

    @Override
    public Kitchen getKitchenById(int kitChenId) {
        return virtualProductsMapper.getKichenById(kitChenId);
    }
}
