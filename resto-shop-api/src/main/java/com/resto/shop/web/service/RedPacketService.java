package com.resto.shop.web.service;

import com.resto.brand.core.generic.GenericService;
import com.resto.brand.web.dto.RedPacketDto;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.model.RedPacket;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface RedPacketService extends GenericService<RedPacket, String> {


    /**
     * 使用红包支付
     * @param redPay
     * @param customerId
     * @param order
     */
    void useRedPacketPay(BigDecimal redPay, String customerId, Order order);

    List<RedPacketDto> selectRedPacketLog(Map<String, Object> selectMap);

    Map<String, Object> selectUseRedOrder(Map<String, Object> selectMap);

    void refundRedPacket(BigDecimal payValue, String Id);
}
