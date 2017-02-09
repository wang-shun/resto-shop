package com.resto.shop.web.service;

import java.math.BigDecimal;
import java.util.List;

import com.resto.brand.core.generic.GenericService;
import com.resto.brand.web.model.ShopDetail;
import com.resto.shop.web.model.Customer;
import com.resto.shop.web.model.NewCustomCoupon;

public interface NewCustomCouponService extends GenericService<NewCustomCoupon, Long> {

    int insertNewCustomCoupon(NewCustomCoupon newCustomCoupon);

    List<NewCustomCoupon> selectListByBrandId(String currentBrandId,String shopId);

	void giftCoupon(Customer cus,Integer couponType,String shopId);


    List<NewCustomCoupon> selectListByCouponType(String currentBrandId,Integer couponType,String shopId);

    List<NewCustomCoupon>  selectListByCouponTypeAndShopId(String shopId,Integer couponType);

    /**
     * 查询所有属于该店铺的
     * 优惠券设置
     * @param currentShopId
     * @return
     */
    List<NewCustomCoupon> selectListShopId(String currentShopId);
    
    
    void timedPush(long BeginDate,long EndDate,String customerId,String name,BigDecimal price,ShopDetail shopDetail);
}
