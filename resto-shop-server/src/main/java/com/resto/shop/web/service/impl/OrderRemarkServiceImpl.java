package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.OrderRemarkMapper;
import com.resto.shop.web.model.OrderRemark;
import com.resto.shop.web.service.OrderRemarkService;

import javax.annotation.Resource;
import java.util.List;

@RpcService
public class OrderRemarkServiceImpl extends GenericServiceImpl<OrderRemark, String> implements OrderRemarkService{

	@Resource
	private OrderRemarkMapper orderRemarkMapper;
	
	@Override
	public GenericDao<OrderRemark, String> getDao() {
		return orderRemarkMapper;
	}

    @Override
    public List<OrderRemark> selectOrderRemarks(String shopId) {
        return orderRemarkMapper.selectOrderRemarks(shopId);
    }

    @Override
    public String selectOrderRemarkName(String[] orderRemarkIds) {
        return orderRemarkMapper.selectOrderRemarkName(orderRemarkIds);
    }

    @Override
    public List<OrderRemark> selectOrderRemarkAll(String shopId) {
        return orderRemarkMapper.selectOrderRemarkAll(shopId);
    }
}
