package com.resto.shop.web.service.impl;

import java.util.List;

import javax.annotation.Resource;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.FreeDayMapper;
import com.resto.shop.web.model.DistributionTime;
import com.resto.shop.web.model.FreeDay;
import com.resto.shop.web.service.FreedayService;

import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class FreedayServiceImpl extends GenericServiceImpl<FreeDay, String> implements FreedayService {

    @Resource
    private FreeDayMapper freedayMapper;

    @Override
    public GenericDao<FreeDay, String> getDao() {
        return freedayMapper;
    }

    @Override
    public List<FreeDay> list(FreeDay day) {
        return freedayMapper.selectList(day);
    } 

}
