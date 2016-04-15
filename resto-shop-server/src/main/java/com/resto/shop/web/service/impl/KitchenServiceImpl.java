package com.resto.shop.web.service.impl;

import java.util.List;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.KitchenMapper;
import com.resto.shop.web.model.Kitchen;
import com.resto.shop.web.service.KitchenService;
import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class KitchenServiceImpl extends GenericServiceImpl<Kitchen, Integer> implements KitchenService {

    @Resource
    private KitchenMapper kitchenMapper;

    @Override
    public GenericDao<Kitchen, Integer> getDao() {
        return kitchenMapper;
    }

	@Override
	public List<Kitchen> selectListByShopId(String shopId) {
		return null;
	} 

}
