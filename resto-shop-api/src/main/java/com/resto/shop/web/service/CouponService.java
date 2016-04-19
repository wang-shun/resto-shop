package com.resto.shop.web.service;

import java.util.List;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.Coupon;

public interface CouponService extends GenericService<Coupon, String> {

    List<Coupon> listCoupon(Coupon coupon);

    void insertCoupon(Coupon coupon);
    
}
