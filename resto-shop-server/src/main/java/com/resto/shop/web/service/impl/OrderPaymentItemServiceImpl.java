package com.resto.shop.web.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.DateUtil;
import com.resto.brand.web.dto.IncomeReportDto;
import com.resto.brand.web.dto.ShopIncomeDto;
import com.resto.shop.web.constant.PayMode;
import com.resto.shop.web.dao.ChargeOrderMapper;
import com.resto.shop.web.dao.OrderPaymentItemMapper;
import com.resto.shop.web.model.OrderPaymentItem;
import com.resto.shop.web.service.OrderPaymentItemService;

import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class OrderPaymentItemServiceImpl extends GenericServiceImpl<OrderPaymentItem, String> implements OrderPaymentItemService {

    @Resource
    private OrderPaymentItemMapper orderpaymentitemMapper;
    
    @Resource
    private ChargeOrderMapper chargeOrderMapper;

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
		Date begin = DateUtil.getformatBeginDate(beginDate);
		Date end = DateUtil.getformatEndDate(endDate);
		//查询订单支付记录
		List<OrderPaymentItem> list = orderpaymentitemMapper.selectpaymentByPaymentMode(shopId,begin,end);
		for(OrderPaymentItem item : list){
			item.setPaymentModeVal(PayMode.getPayModeName(item.getPaymentModeId()));
		}
		return orderpaymentitemMapper.selectpaymentByPaymentMode(shopId,begin,end);
	}

	@Override
	public List<IncomeReportDto> selectIncomeList(String brandId, String beginDate, String endDate) {
		Date begin = DateUtil.getformatBeginDate(beginDate);
		Date end =  DateUtil.getformatEndDate(endDate);
		return orderpaymentitemMapper.selectIncomeList(brandId,begin,end);
	}

	@Override
	public List<OrderPaymentItem> selectListByShopId(String shopId) {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    public List<IncomeReportDto> selectIncomeListByShopId(String shopId, String beginDate, String endDate) {
        Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end =  DateUtil.getformatEndDate(endDate);
        return orderpaymentitemMapper.selectIncomeListByShopId(shopId,begin,end);
    }


}
