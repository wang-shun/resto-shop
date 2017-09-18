package com.resto.shop.web.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.resto.brand.core.generic.GenericService;
import com.resto.brand.web.dto.RechargeLogDto;
import com.resto.brand.web.dto.RedPacketDto;
import com.resto.shop.web.model.ChargeOrder;
import com.resto.shop.web.model.ChargePayment;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.model.RedPacket;


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
	void useChargePay(BigDecimal remainPay, String customerId, Order order,String brandName);



	void refundCharge(BigDecimal payValue, String id,String shopDetailId);

	void refundMoney(BigDecimal charge,BigDecimal reward, String id,String shopDetailId);


	void refundReward(BigDecimal payValue, String id,String shopDetailId);

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
	/**
	 * 店铺详细
	 *
	 */

	List<ChargeOrder> shopChargeCodes(String shopDetailId, Date beginDate, Date endDate);


	/**
	 * 下载报表
	 */

	public Map<String,Object>  shopChargeCodesSetDto(String shopDetailId, String beginDate, String endDate,String shopname);


    public List<RedPacketDto> selectChargeRedPacket(Map<String, Object> selectMap);


    List<Map<String, Object>> selectByShopToDay(Map<String, Object> selectMap);

    List<ChargeOrder> selectListByDateAndShopId(String zuoriDay, String zuoriDay1, String id);

    /**
     * 判断该人是否是首单
     * @param customerId
     * @param brandId
     * @return
     */
    List<ChargeOrder> selectByCustomerIdAndBrandId(String customerId,String brandId);

    List<ChargeOrder> selectMonthDto(Map<String, Object> selectMap);

	RechargeLogDto selectRechargeLog(String beginDate, String endDate, String brandId);


	RechargeLogDto selectShopRechargeLog(String beginDate,String endDate,String shopId);

	List<Map<String, Object>> getChargeSumInfo(Map<String, Object> selectMap);

	List<String> selectCustomerChargeOrder(List<String> customerIds);
}
