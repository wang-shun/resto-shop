package com.resto.shop.web.service.impl;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.MealTempAttrMapper;
import com.resto.shop.web.model.MealTempAttr;
import com.resto.shop.web.service.MealTempAttrService;
import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class MealTempAttrServiceImpl extends GenericServiceImpl<MealTempAttr, Integer> implements MealTempAttrService {

    @Resource
    private MealTempAttrMapper mealtempattrMapper;

    @Override
    public GenericDao<MealTempAttr, Integer> getDao() {
        return mealtempattrMapper;
    } 

}
