package com.resto.shop.web.service.impl;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.BonusSettingMapper;
import com.resto.shop.web.model.BonusSetting;
import com.resto.shop.web.service.BonusSettingService;
import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class BonusSettingServiceImpl extends GenericServiceImpl<BonusSetting, String> implements BonusSettingService {

    @Resource
    private BonusSettingMapper bonussettingMapper;

    @Override
    public GenericDao<BonusSetting, String> getDao() {
        return bonussettingMapper;
    }


    @Override
    public BonusSetting selectByChargeSettingId(String chargeSettingId) {
        return bonussettingMapper.selectByChargeSettingId(chargeSettingId);
    }
}
