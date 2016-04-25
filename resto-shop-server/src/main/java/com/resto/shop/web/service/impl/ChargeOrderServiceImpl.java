package com.resto.shop.web.service.impl;

import java.util.Date;

import javax.annotation.Resource;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.ApplicationUtils;
import com.resto.shop.web.dao.ChargeOrderMapper;
import com.resto.shop.web.dao.ChargeSettingMapper;
import com.resto.shop.web.model.ChargeOrder;
import com.resto.shop.web.model.ChargeSetting;
import com.resto.shop.web.service.ChargeOrderService;

import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class ChargeOrderServiceImpl extends GenericServiceImpl<ChargeOrder, String> implements ChargeOrderService {

    @Resource
    private ChargeOrderMapper chargeorderMapper;
    
    @Resource
    private ChargeSettingMapper chargeSettingMapper;

    
    @Override
    public GenericDao<ChargeOrder, String> getDao() {
        return chargeorderMapper;
    }
    
	@Override
	public ChargeOrder createChargeOrder(String settingId, String customerId, String shopId, String brandId) {
		ChargeSetting chargeSetting = chargeSettingMapper.selectByPrimaryKey(settingId);
		byte orderState = 0;
		ChargeOrder chargeOrder = new ChargeOrder(ApplicationUtils.randomUUID(),chargeSetting.getChargeMoney(),
				chargeSetting.getRewardMoney(),orderState,new Date(),customerId,shopId,brandId);
		chargeorderMapper.insert(chargeOrder);
		return chargeOrder;
	} 

}
