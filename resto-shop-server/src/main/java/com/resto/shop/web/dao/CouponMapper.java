package com.resto.shop.web.dao;

import com.resto.shop.web.model.Coupon;
import com.resto.brand.core.generic.GenericDao;

public interface CouponMapper  extends GenericDao<Coupon,String> {
    int deleteByPrimaryKey(String id);

    int insert(Coupon record);

    int insertSelective(Coupon record);

    Coupon selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(Coupon record);

    int updateByPrimaryKey(Coupon record);
}
