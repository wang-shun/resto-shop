package com.resto.shop.web.service.impl;


import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.MemberActivityThingMapper;
import com.resto.shop.web.model.MemberActivityThing;
import com.resto.shop.web.service.MemberActivityThingService;

import javax.annotation.Resource;

@RpcService
public class MemberActivityThingServerImpl extends GenericServiceImpl<MemberActivityThing, Integer> implements MemberActivityThingService {

    @Resource
    private MemberActivityThingMapper memberActivityThingMapper;

    @Override
    public GenericDao<MemberActivityThing, Integer> getDao() {
        return memberActivityThingMapper;
    }

    @Override
    public MemberActivityThing selectByTelephone(String telephone) {
        return memberActivityThingMapper.selectByTelephone(telephone);
    }
}
