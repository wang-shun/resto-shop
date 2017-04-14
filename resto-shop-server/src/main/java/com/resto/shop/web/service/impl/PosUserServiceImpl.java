package com.resto.shop.web.service.impl;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.PosUserMapper;
import com.resto.shop.web.model.PosUser;
import com.resto.shop.web.service.PosUserService;
import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class PosUserServiceImpl extends GenericServiceImpl<PosUser, Long> implements PosUserService {

    @Resource
    private PosUserMapper posuserMapper;

    @Override
    public GenericDao<PosUser, Long> getDao() {
        return posuserMapper;
    } 

}
