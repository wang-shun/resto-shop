package com.resto.shop.web.service;

import com.resto.brand.core.generic.GenericService;
import com.resto.brand.web.model.WechatConfig;
import com.resto.shop.web.model.Customer;
import com.resto.shop.web.model.GetNumber;

import java.util.Date;
import java.util.List;

/**
 * Created by carl on 2016/10/14.
 */
public interface GetNumberService extends GenericService<GetNumber, String> {

    List<GetNumber> selectByTableTypeShopId(String tableType, String shopId);

    Integer selectCount(String tableType, Date date);

    GetNumber updateGetNumber(GetNumber getNumber, Integer state);

    GetNumber getWaitInfoByCustomerId(String customerId,String shopId);

}
