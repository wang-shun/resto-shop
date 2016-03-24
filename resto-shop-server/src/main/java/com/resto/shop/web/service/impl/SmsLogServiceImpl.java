package com.resto.shop.web.service.impl;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.SmsLogMapper;
import com.resto.shop.web.model.SmsLog;
import com.resto.shop.web.service.SmsLogService;
import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class SmsLogServiceImpl extends GenericServiceImpl<SmsLog, Long> implements SmsLogService {

    @Resource
    private SmsLogMapper smslogMapper;

    @Override
    public GenericDao<SmsLog, Long> getDao() {
        return smslogMapper;
    } 

}
