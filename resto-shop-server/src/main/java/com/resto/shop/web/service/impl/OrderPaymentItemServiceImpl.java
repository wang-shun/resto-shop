package com.resto.shop.web.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.OrderPaymentItemMapper;
import com.resto.shop.web.model.OrderPaymentItem;
import com.resto.shop.web.service.OrderPaymentItemService;
import com.resto.shop.web.util.DateUtil;

import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class OrderPaymentItemServiceImpl extends GenericServiceImpl<OrderPaymentItem, String> implements OrderPaymentItemService {

    @Resource
    private OrderPaymentItemMapper orderpaymentitemMapper;

    @Override
    public GenericDao<OrderPaymentItem, String> getDao() {
        return orderpaymentitemMapper;
    }

	@Override
	public List<OrderPaymentItem> selectByOrderId(String orderId) {
		return orderpaymentitemMapper.selectByOrderId(orderId);
	}


	@Override
	public List<OrderPaymentItem> selectpaymentByPaymentMode(String beginDate, String endDate, String shopId) {
		if(beginDate==null&&endDate==null){
			beginDate=DateUtil.getDateBegin(new Date()).toString();
			endDate=DateUtil.getDateEnd(new Date()).toString();
		}
		
		return orderpaymentitemMapper.selectpaymentByPaymentMode(beginDate,endDate,shopId);
	}



}
