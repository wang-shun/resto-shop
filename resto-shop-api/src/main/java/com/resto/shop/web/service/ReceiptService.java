package com.resto.shop.web.service;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.dto.ReceiptOrder;
import com.resto.shop.web.dto.ReceiptPos;
import com.resto.shop.web.dto.ReceiptPosOrder;
import com.resto.shop.web.model.Receipt;

import java.util.List;
import java.util.Map;

/**
 * Created by xielc on 2017/9/5.
 */
public interface ReceiptService extends GenericService<Receipt, String> {

    Receipt insertSelective(Receipt record);

    int updateByPrimaryKeySelective(Receipt record);

    int updateState(Receipt record);

    //根据状态查询发票订单
    List<ReceiptOrder> selectReceiptOrderList(String customerId, String state);
    //根据店铺id，查询该店铺下的发票
    ReceiptPosOrder getReceiptOrderList(String receiptId);
    //根据发票id，查询发票详情
    List<ReceiptPos> getReceiptList(String shopId,String state);
    //发票打印
    List<Map<String, Object>> printReceiptOrder(String ShopId, String receiptId);
    //pos发票打印
    List<Map<String, Object>> printReceiptPosOrder(String orderNumber,String ShopId);
    //根据订单号查询发票订单
    ReceiptPos getPosReceiptList(String orderNumber);
}
