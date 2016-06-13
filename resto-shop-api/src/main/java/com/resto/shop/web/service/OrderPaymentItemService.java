package com.resto.shop.web.service;

import java.util.List;

import com.resto.brand.core.generic.GenericService;
import com.resto.brand.web.dto.IncomeReportDto;
import com.resto.shop.web.model.OrderPaymentItem;

public interface OrderPaymentItemService extends GenericService<OrderPaymentItem, String> {

	List<OrderPaymentItem> selectByOrderId(String orderId);

	List<OrderPaymentItem> selectpaymentByPaymentMode(String ShopId, String beginDate, String endDate);

	List<IncomeReportDto> selectIncomeList(String brandId,String beginDate,String endDate);

}
