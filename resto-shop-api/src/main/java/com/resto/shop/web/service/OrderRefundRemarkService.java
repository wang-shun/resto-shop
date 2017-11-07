package com.resto.shop.web.service;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.OrderRefundRemark;

public interface OrderRefundRemarkService extends GenericService<OrderRefundRemark, Long> {
    /**
     * 根据 订单ID 删除
     * Pos 2.0 同步数据使用
     * @param orderId
     */
    void posSyncDeleteByOrderId(String orderId);
}
