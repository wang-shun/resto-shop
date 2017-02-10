package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.RedPacketMapper;
import com.resto.shop.web.model.RedPacket;
import com.resto.shop.web.service.RedPacketService;

import javax.annotation.Resource;

/**
 *
 */
@RpcService
public class RedPacketServiceImpl extends GenericServiceImpl<RedPacket, String> implements RedPacketService {

    @Resource
    private RedPacketMapper redPacketMapper;

    @Override
    public GenericDao<RedPacket, String> getDao() {
        return redPacketMapper;
    }

}
