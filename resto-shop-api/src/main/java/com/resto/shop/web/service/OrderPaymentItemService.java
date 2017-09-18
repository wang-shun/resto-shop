package com.resto.shop.web.service;

import java.util.List;
import java.util.Map;

import com.resto.brand.core.generic.GenericService;
import com.resto.brand.web.dto.IncomeReportDto;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.model.OrderPaymentItem;

public interface OrderPaymentItemService extends GenericService<OrderPaymentItem, String> {
    List<OrderPaymentItem> selectByOrderId(String orderId);



    List<OrderPaymentItem> selectByOrderIdList(String orderId);

    List<OrderPaymentItem> selectpaymentByPaymentMode(String ShopId, String beginDate, String endDate);

    List<IncomeReportDto> selectIncomeList(String brandId,String beginDate,String endDate);

    List<OrderPaymentItem> selectListByShopId(String shopId);
    List<IncomeReportDto> selectIncomeListByShopId(String shopId,String beginDate,String endDate);

    List<OrderPaymentItem> selectListByResultData(String beginDate, String endDate);

    List<Order> selectOrderMoneyByBrandIdGroupByOrderId(String beginDate, String endDate);

    /**
     * 2016-10-29
     * 用于查询品牌收入报表
     * @return
     */
    List<Map<String, Object>> selectShopIncomeList(Map<String, Object> map);

    /**
     * 2016-10-29
     * 用于pos端查询店铺的收入
     * @param beginDate
     * @param endDate
     * @param shopId
     * @return
     */
    List<OrderPaymentItem> selectShopIncomeListByShopId(String beginDate, String endDate, String shopId);


    List<OrderPaymentItem> selectListByResultDataByNoFile(String beginDate, String endDate);

    /**
     * 查询退款证书丢失的订单项
     * 2016-10-29
     * @param orderId
     * @return
     */
    OrderPaymentItem selectByOrderIdAndResultData(String orderId);
    
    List<OrderPaymentItem> selectOrderPayMentItem(Map<String, String> map);
    
    public List<OrderPaymentItem> selectPaymentCountByOrderId(String orderId);
    
    public OrderPaymentItem selectPayMentSumByrefundOrder(String orderId);

    /**
     * 查询订单使用闪惠支付的时候订单项信息
     * @param orderId
     * @return
     */
    OrderPaymentItem selectByShanhuiPayOrder(String orderId);

    /**
     * 修改订单使用闪惠支付的时候订单项信息
     * @param orderId
     * @param param
     */
    void updateByShanhuiPayOrder(String orderId, String param);

    OrderPaymentItem insertByBeforePay(OrderPaymentItem orderPaymentItem);


    OrderPaymentItem selectWeChatPayResultData(String shopId);

    List<OrderPaymentItem> selectRefundPayMent(String orderId);

    int deleteByOrderId(String orderId);
}
