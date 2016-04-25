package com.resto.shop.web.service.impl;

<<<<<<< 3fdbfb1f18c64b5648254368dc10a6033cedf088

import java.math.BigDecimal;

import java.util.Date;

import javax.annotation.Resource;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;

import com.resto.brand.core.util.ApplicationUtils;

import com.resto.shop.web.constant.OrderState;

import com.resto.shop.web.dao.ChargeOrderMapper;
import com.resto.shop.web.dao.ChargeSettingMapper;
import com.resto.shop.web.model.ChargeOrder;

import com.resto.shop.web.model.ChargeSetting;
import com.re
import com.resto.shop.web.model.ChargePayment;
import com.resto.shop.web.model.Customer;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.service.AccountService;
import com.resto.shop.web.service.ChargeOrderService;
import com.resto.shop.web.service.ChargePaymentService;
import com.resto.shop.web.service.CustomerService;


import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class ChargeOrderServiceImpl extends GenericServiceImpl<ChargeOrder, String> implements ChargeOrderService {

  
    @Resource
    private ChargeSettingMapper chargeSettingMapper;

	@Resource
	private ChargeOrderMapper chargeorderMapper;
	@Resource
	private ChargePaymentService chargePaymentService;

	@Resource
	private AccountService accountService;
	@Resource
	CustomerService customerService;
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

	@Override
	public void chargeorderWxPaySuccess(ChargePayment cp) {
		ChargeOrder chargeOrder = selectById(cp.getTbChargeOrderId());
		if (chargeOrder != null && chargeOrder.getOrderState() == 0) {
			// 开始充值余额
			BigDecimal reward = chargeOrder.getRewardMoney();
			BigDecimal chargeMoney = chargeOrder.getChargeMoney();
			BigDecimal value = chargeMoney.add(reward).divide(new BigDecimal(100));
			Customer customer = customerService.selectById(chargeOrder.getCustomerId());
			accountService.addAccount(value, customer.getAccountId(), "自助充值");
			// 添加充值记录
			chargePaymentService.insert(cp);
			chargeOrder.setOrderState((byte) 1);
			chargeOrder.setFinishTime(new Date());
			update(chargeOrder);// 只能更新状态和结束时间
		}


	}
}