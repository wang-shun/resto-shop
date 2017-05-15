package com.resto.shop.web.service.impl;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.BonusLogMapper;
import com.resto.shop.web.model.BonusLog;
import com.resto.shop.web.service.BonusLogService;
import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class BonusLogServiceImpl extends GenericServiceImpl<BonusLog, String> implements BonusLogService {

    @Resource
    private BonusLogMapper bonuslogMapper;

    @Override
    public GenericDao<BonusLog, String> getDao() {
        return bonuslogMapper;
    } 

}
