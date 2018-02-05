package com.resto.shop.web.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.DateUtil;
import com.resto.brand.web.dto.RechargeLogDto;
import com.resto.shop.web.dao.ChargePaymentMapper;
import com.resto.shop.web.model.ChargePayment;
import com.resto.shop.web.service.ChargePaymentService;

/**
 *
 */
@RpcService
public class ChargePaymentServiceImpl extends GenericServiceImpl<ChargePayment, String> implements ChargePaymentService {

    @Resource
    private ChargePaymentMapper chargepaymentMapper;

    @Override
    public GenericDao<ChargePayment, String> getDao() {
        return chargepaymentMapper;
    }

    @Override
    public List<ChargePayment> selectPayList(String beginDate, String endDate) {
        //获取开始时间
        Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);
        return chargepaymentMapper.selectPayList(begin,end);
    }

	@Override
	public RechargeLogDto selectRechargeLog(String beginDate, String endDate, String brandId) {
		// TODO Auto-generated method stub
		Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);
        return chargepaymentMapper.selectRechargeLog(begin,end,brandId);
	}

	@Override
	public RechargeLogDto selectShopRechargeLog(String beginDate, String endDate, String shopId) {
		// TODO Auto-generated method stub
		Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);
        return chargepaymentMapper.selectShopRechargeLog(begin,end,shopId);
	}




    @Override
    public ChargePayment selectPayData(String shopId) {
        return chargepaymentMapper.selectPayData(shopId);
    }
}
