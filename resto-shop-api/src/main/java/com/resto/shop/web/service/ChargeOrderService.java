package com.resto.shop.web.service;

import java.math.BigDecimal;
import java.util.List;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.ChargeOrder;
import com.resto.shop.web.model.ChargePayment;
import com.resto.shop.web.model.Order;

public interface ChargeOrderService extends GenericService<ChargeOrder, String> {

	
	/**
	 * 创建微信充值订单
	 * @param settingId
	 * @param customerId
	 * @return
	 */
	ChargeOrder createChargeOrder(String settingId,String customerId,String shopId,String brandId);


	void chargeorderWxPaySuccess(ChargePayment cp);


	BigDecimal selectTotalBalance(String customerId);


	/**
	 * 
	 * @param remainPay
	 * @param customerId
	 * @param order 
	 * @return  array[0]充值支付金额  array[1]赠送支付金额
	 */
	void useChargePay(BigDecimal remainPay, String customerId, Order order);


	void refundCharge(BigDecimal payValue, String id);


	void refundReward(BigDecimal payValue, String id);

    /**
     * 查询店铺某个时间段的充值记录
     * @param beginDate
     * @param endDate
     * @param shopDetailId
     * @return
     */
    List<ChargeOrder> selectByDateAndShopId(String beginDate, String endDate, String shopDetailId);

    /**
     * 查询品牌某个时间的充值记录
     * @param beginDate
     * @param endDate
     * @param id
     * @return
     */
    List<ChargeOrder> selectByDateAndBrandId(String beginDate, String endDate, String id);
}
