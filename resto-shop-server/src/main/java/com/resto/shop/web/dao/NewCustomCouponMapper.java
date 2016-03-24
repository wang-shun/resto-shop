package com.resto.shop.web.dao;

import com.resto.shop.web.model.NewCustomCoupon;
import com.resto.brand.core.generic.GenericDao;

public interface NewCustomCouponMapper  extends GenericDao<NewCustomCoupon,Long> {
    int deleteByPrimaryKey(Long id);

    int insert(NewCustomCoupon record);

    int insertSelective(NewCustomCoupon record);

    NewCustomCoupon selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(NewCustomCoupon record);

    int updateByPrimaryKey(NewCustomCoupon record);
}
