package com.resto.shop.web.service.impl;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.MealTempMapper;
import com.resto.shop.web.model.MealTemp;
import com.resto.shop.web.service.MealTempService;
import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class MealTempServiceImpl extends GenericServiceImpl<MealTemp, Integer> implements MealTempService {

    @Resource
    private MealTempMapper mealtempMapper;

    @Override
    public GenericDao<MealTemp, Integer> getDao() {
        return mealtempMapper;
    } 

}
