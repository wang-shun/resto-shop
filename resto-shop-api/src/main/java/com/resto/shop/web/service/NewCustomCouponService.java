package com.resto.shop.web.service;

import java.util.List;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.Customer;
import com.resto.shop.web.model.NewCustomCoupon;

public interface NewCustomCouponService extends GenericService<NewCustomCoupon, Long> {

    int insertNewCustomCoupon(NewCustomCoupon brand);

    List<NewCustomCoupon> selectListByBrandId(String currentBrandId);

	void giftCoupon(Customer cus);

    
    
}
