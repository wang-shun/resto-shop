package com.resto.shop.web.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.constant.PayMode;
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
	public List<OrderPaymentItem> selectpaymentByPaymentMode(String shopId, String beginDate, String endDate) {
		Date begin = null;
		Date end = null;
		
		System.out.println(DateUtil.getDateBegin(new Date()) );
		if(beginDate==null || ("").equals(beginDate.trim())){
			begin=DateUtil.getDateBegin(new Date());
		}else{
			System.out.println("222");
		}
		begin = DateUtil.getDateBegin(new Date());
		if(endDate==null || ("").equals(endDate.trim())){
			end=DateUtil.getDateEnd(new Date());
		}
		List<OrderPaymentItem> list = orderpaymentitemMapper.selectpaymentByPaymentMode(shopId,begin,end);
		for(OrderPaymentItem item : list){
			item.setPaymentModeVal(PayMode.getPayModeName(item.getPaymentModeId()));
		}
		return orderpaymentitemMapper.selectpaymentByPaymentMode(shopId,begin,end);
	}



}
