package com.resto.shop.web.service;

import java.util.List;

import com.resto.brand.core.generic.GenericService;
import com.resto.brand.web.dto.IncomeReportDto;
import com.resto.brand.web.dto.ShopIncomeDto;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.model.OrderPaymentItem;

public interface OrderPaymentItemService extends GenericService<OrderPaymentItem, String> {

    List<OrderPaymentItem> selectByOrderId(String orderId);

    List<OrderPaymentItem> selectpaymentByPaymentMode(String ShopId, String beginDate, String endDate);

    List<IncomeReportDto> selectIncomeList(String brandId,String beginDate,String endDate);

    List<OrderPaymentItem> selectListByShopId(String shopId);
    List<IncomeReportDto> selectIncomeListByShopId(String shopId,String beginDate,String endDate);

    List<OrderPaymentItem> selectListByResultData(String beginDate, String endDate);

    List<Order> selectOrderMoneyByBrandIdGroupByOrderId(String beginDate, String endDate);

    /**
     * 2016-10-29
     * 用于查询品牌收入报表
     * @param beginDate
     * @param endDate
     * @param currentBrandId
     * @return
     */
    List<OrderPaymentItem> selectShopIncomeList(String beginDate, String endDate, String currentBrandId);

    List<OrderPaymentItem> selectListByResultDataByNoFile(String beginDate, String endDate);
}
