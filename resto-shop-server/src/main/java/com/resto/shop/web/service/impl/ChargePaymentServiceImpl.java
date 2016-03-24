package com.resto.shop.web.service.impl;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.ChargePaymentMapper;
import com.resto.shop.web.model.ChargePayment;
import com.resto.shop.web.service.ChargePaymentService;
import cn.restoplus.rpc.server.RpcService;

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

}
