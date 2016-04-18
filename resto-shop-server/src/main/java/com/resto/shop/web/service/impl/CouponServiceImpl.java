package com.resto.shop.web.service.impl;

import java.util.List;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.CouponMapper;
import com.resto.shop.web.model.Coupon;
import com.resto.shop.web.service.CouponService;
import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class CouponServiceImpl extends GenericServiceImpl<Coupon, String> implements CouponService {

    @Resource
    private CouponMapper couponMapper;

    @Override
    public GenericDao<Coupon, String> getDao() {
        return couponMapper;
    }

    @Override
    public List<Coupon> listCoupon(Coupon coupon) {
        return couponMapper.listCoupon(coupon);
    } 

}
