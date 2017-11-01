package com.resto.shop.web.service;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.Notice;
import com.resto.shop.web.model.OrderBefore;

import java.util.List;

/**
 * Created by KONATA on 2017/11/1.
 */
public interface OrderBeforeService extends GenericService<OrderBefore, Long> {

    OrderBefore getOrderNoPay(String tableNumber, String shopId,String customerId);


}
