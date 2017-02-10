package com.resto.shop.web.service;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.model.RedPacket;

import java.math.BigDecimal;

public interface RedPacketService extends GenericService<RedPacket, String> {


    /**
     * 使用红包支付
     * @param redPay
     * @param customerId
     * @param order
     */
    void useRedPacketPay(BigDecimal redPay, String customerId, Order order);
}
