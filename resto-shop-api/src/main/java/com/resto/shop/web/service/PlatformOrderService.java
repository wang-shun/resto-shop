package com.resto.shop.web.service;

import com.resto.brand.core.generic.GenericService;
import com.resto.brand.web.dto.MeiTuanOrderDto;
import com.resto.shop.web.model.PlatformOrder;

public interface PlatformOrderService extends GenericService<PlatformOrder, String> {
    /**
     *  根据第三方平台的订单ID查询订单详情
     * @param platformOrderId
     * @return
     */
    PlatformOrder selectByPlatformOrderId(String platformOrderId,int type);

    /**
     * 美团外卖新订单
     * @param orderDto
     * @return
     */
    void meituanNewOrder(MeiTuanOrderDto orderDto);
}
