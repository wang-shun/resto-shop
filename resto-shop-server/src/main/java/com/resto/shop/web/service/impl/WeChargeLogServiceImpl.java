package com.resto.shop.web.service.impl;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.WeChargeLogMapper;
import com.resto.shop.web.model.WeChargeLog;
import com.resto.shop.web.service.WeChargeLogService;
import cn.restoplus.rpc.server.RpcService;

import java.util.List;

/**
 *
 */
@RpcService
public class WeChargeLogServiceImpl extends GenericServiceImpl<WeChargeLog, Long> implements WeChargeLogService {

    @Resource
    private WeChargeLogMapper wechargelogMapper;

    @Override
    public GenericDao<WeChargeLog, Long> getDao() {
        return wechargelogMapper;
    }

    @Override
    public List<WeChargeLog> selectByShopIdAndTime(String shopId, String begin) {
        return wechargelogMapper.selectByShopIdAndTime(shopId,begin);
    }

    @Override
    public void deleteByIds(List<Long> ids) {
         wechargelogMapper.deleteByIds(ids);
    }


}
