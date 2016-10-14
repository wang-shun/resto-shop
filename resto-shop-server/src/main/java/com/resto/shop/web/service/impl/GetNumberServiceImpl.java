package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.GetNumberMapper;
import com.resto.shop.web.model.GetNumber;
import com.resto.shop.web.service.GetNumberService;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by carl on 2016/10/14.
 */
@RpcService
public class GetNumberServiceImpl extends GenericServiceImpl<GetNumber, String> implements GetNumberService {

    @Resource
    private GetNumberMapper getNumberMapper;

    @Override
    public GenericDao<GetNumber, String> getDao() {
        return getNumberMapper;
    }

    @Override
    public List<GetNumber> selectByTableTypeShopId(String tableType, String shopId) {
        return getNumberMapper.selectByTableTypeShopId(tableType, shopId);
    }
}
