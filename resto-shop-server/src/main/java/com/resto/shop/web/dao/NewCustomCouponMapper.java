package com.resto.shop.web.dao;

import java.util.List;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.NewCustomCoupon;

public interface NewCustomCouponMapper  extends GenericDao<NewCustomCoupon,Long> {
    int deleteByPrimaryKey(Long id);

    int insert(NewCustomCoupon record);

    int insertSelective(NewCustomCoupon record);

    NewCustomCoupon selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(NewCustomCoupon record);

    int updateByPrimaryKey(NewCustomCoupon record);

    List<NewCustomCoupon> selectListByBrandId(String brandId);

	List<NewCustomCoupon> selectListByBrandIdAndIsActive(String brandId);
}
