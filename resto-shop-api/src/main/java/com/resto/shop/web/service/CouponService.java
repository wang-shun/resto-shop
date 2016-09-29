package com.resto.shop.web.service;

import java.math.BigDecimal;
import java.util.List;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.exception.AppException;
import com.resto.shop.web.model.Coupon;
import com.resto.shop.web.model.Order;

public interface CouponService extends GenericService<Coupon, String> {

    List<Coupon> listCoupon(Coupon coupon);

    void insertCoupon(Coupon coupon);

	Coupon useCoupon(BigDecimal totalMoney, Order order) throws AppException;

	void refundCoupon(String id);
    
	/**
	 * 根据 状态 查询 优惠劵列表
	 * @param status
	 * @return
	 */
	List<Coupon> listCouponByStatus(String status,String customerId);

	void useCouponById(String orderId,String id);
}
