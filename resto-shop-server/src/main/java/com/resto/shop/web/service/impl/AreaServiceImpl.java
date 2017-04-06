package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.AreaMapper;
import com.resto.shop.web.model.Area;
import com.resto.shop.web.service.AreaService;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by KONATA on 2017/4/5.
 */
@RpcService
public class AreaServiceImpl extends GenericServiceImpl<Area, Long> implements AreaService {


    @Resource
    private AreaMapper areaMapper;

    @Override
    public GenericDao<Area, Long> getDao() {
        return areaMapper;
    }


    @Override
    public List<Area> getAreaList(String shopId) {
        return areaMapper.getAreaList(shopId);
    }
}
