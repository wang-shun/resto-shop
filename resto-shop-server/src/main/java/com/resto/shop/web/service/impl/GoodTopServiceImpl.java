package com.resto.shop.web.service.impl;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.GoodTopMapper;
import com.resto.shop.web.model.GoodTop;
import com.resto.shop.web.service.GoodTopService;
import cn.restoplus.rpc.server.RpcService;

import java.util.Date;

/**
 *
 */
@RpcService
public class GoodTopServiceImpl extends GenericServiceImpl<GoodTop, Long> implements GoodTopService {

    @Resource
    private GoodTopMapper goodtopMapper;

    @Override
    public GenericDao<GoodTop, Long> getDao() {
        return goodtopMapper;
    }

    @Override
    public int deleteByTodayAndShopId(String brandId, String shopId, int dayMessage, Date date) {
       return goodtopMapper.deleteByTodayAndShopId(brandId,shopId,dayMessage,date);




    }
}
