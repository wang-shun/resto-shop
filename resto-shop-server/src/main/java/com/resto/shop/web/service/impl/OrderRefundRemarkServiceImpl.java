package com.resto.shop.web.service.impl;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.OrderRefundRemarkMapper;
import com.resto.shop.web.model.OrderRefundRemark;
import com.resto.shop.web.service.OrderRefundRemarkService;
import cn.restoplus.rpc.server.RpcService;

import java.util.List;

/**
 *
 */
@RpcService
public class OrderRefundRemarkServiceImpl extends GenericServiceImpl<OrderRefundRemark, Long> implements OrderRefundRemarkService {

    @Resource
    private OrderRefundRemarkMapper orderrefundremarkMapper;

    @Override
    public GenericDao<OrderRefundRemark, Long> getDao() {
        return orderrefundremarkMapper;
    }

    @Override
    public void posSyncDeleteByOrderId(String orderId) {
        orderrefundremarkMapper.posSyncDeleteByOrderId(orderId);
    }

    @Override
    public List<OrderRefundRemark> posSyncListByOrderId(String orderId) {
        return orderrefundremarkMapper.posSyncListByOrderId(orderId);
    }
}
