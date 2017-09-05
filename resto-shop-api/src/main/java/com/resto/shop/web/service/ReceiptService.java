package com.resto.shop.web.service;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.dto.ReceiptOrder;
import com.resto.shop.web.model.Receipt;

import java.util.List;

/**
 * Created by xielc on 2017/9/5.
 */
public interface ReceiptService extends GenericService<Receipt, String> {

    int insertSelective(Receipt record);

    //根据状态查询发票订单
    List<ReceiptOrder> selectReceiptOrderList(String customerId, String state);
}
