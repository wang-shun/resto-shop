package com.resto.shop.web.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.Coupon;

public interface CouponMapper  extends GenericDao<Coupon,String> {
    int deleteByPrimaryKey(String id);

    int insert(Coupon record);

    int insertSelective(Coupon record);

    Coupon selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(Coupon record);

    int updateByPrimaryKey(Coupon record);

    List<Coupon> listCoupon(Coupon coupon);
    
    /**
     * 根据状态查询用户的品牌优惠券
     * @param status
     * @param IS_EXPIRE
     * @param NOT_EXPIRE
     * @param customerId
     * @return
     */
    List<Coupon> listCouponByStatusAndBrandId(@Param("status") String status,@Param("IS_EXPIRE") String IS_EXPIRE,@Param("NOT_EXPIRE") String NOT_EXPIRE,@Param("customerId") String customerId,@Param("brandId")String brandId);


    /**
     * 查询该店铺的专属优惠券
     * @param status
     * @param IS_EXPIRE
     * @param NOT_EXPIRE
     * @param customerId
     * @return
     */
    List<Coupon> listCouponByStatusAndShopId(@Param("status") String status,@Param("IS_EXPIRE") String IS_EXPIRE,@Param("NOT_EXPIRE") String NOT_EXPIRE,@Param("customerId") String customerId,@Param("shopId")String shopId);

    /**
     * 查询品牌优惠券
     * @param coupon
     * @return
     */
    List<Coupon> listCouponByBrandId(Coupon coupon);

    /**
     * 查询店铺优惠券
     * @param coupon
     * @return
     */
    List<Coupon> listCouponByShopId(Coupon coupon);

}
