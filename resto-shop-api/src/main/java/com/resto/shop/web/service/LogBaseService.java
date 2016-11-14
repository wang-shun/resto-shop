package com.resto.shop.web.service;

import com.resto.brand.core.generic.GenericService;
import com.resto.brand.web.model.ShopDetail;
import com.resto.shop.web.model.Customer;
import com.resto.shop.web.model.LogBase;
import com.resto.shop.web.model.Order;

/**
 * Created by carl on 2016/11/14.
 */
public interface LogBaseService extends GenericService<LogBase, String> {

    void insertLogBaseInfoState(ShopDetail shopDetail, Customer customer, Order order, Integer type);
}
