package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.UnitMapper;
import com.resto.shop.web.model.SupportTime;
import com.resto.shop.web.model.Unit;
import com.resto.shop.web.service.UnitService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by KONATA on 2016/9/11.
 */
@RpcService
public class UnitServiceImpl extends GenericServiceImpl<Unit, String> implements UnitService{

    @Autowired
    private UnitMapper unitMapper;

    @Override
    public GenericDao<Unit, String> getDao() {
        return unitMapper;
    }

    @Override
    public List<Unit> getUnits(String shopId) {
        return unitMapper.getUnits(shopId);
    }
}
