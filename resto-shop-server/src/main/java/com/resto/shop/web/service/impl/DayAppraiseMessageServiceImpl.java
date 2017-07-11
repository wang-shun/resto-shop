package com.resto.shop.web.service.impl;

import javax.annotation.Resource;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.DayAppraiseMessageMapper;
import com.resto.shop.web.model.DayAppraiseMessage;
import com.resto.shop.web.service.DayAppraiseMessageService;
import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class DayAppraiseMessageServiceImpl extends GenericServiceImpl<DayAppraiseMessage, String> implements DayAppraiseMessageService {

    @Resource
    private DayAppraiseMessageMapper dayappraisemessageMapper;

    @Override
    public GenericDao<DayAppraiseMessage, String> getDao() {
        return dayappraisemessageMapper;
    } 

}
