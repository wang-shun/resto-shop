package com.resto.shop.web.service.impl;

import java.util.List;

import javax.annotation.Resource;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.NewCustomCouponMapper;
import com.resto.shop.web.model.NewCustomCoupon;
import com.resto.shop.web.service.NewCustomCouponService;
import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class NewCustomCouponServiceImpl extends GenericServiceImpl<NewCustomCoupon, Long> implements NewCustomCouponService {

    @Resource
    private NewCustomCouponMapper newcustomcouponMapper;

    @Override
    public GenericDao<NewCustomCoupon, Long> getDao() {
        return newcustomcouponMapper;
    }

    @Override
    public int insertNewCustomCoupon(NewCustomCoupon brand) {
        
        return newcustomcouponMapper.insertSelective(brand);
    }

    @Override
    public List<NewCustomCoupon> selectListByShopId(String currentBrandId) {
        return newcustomcouponMapper.selectListByShopId(currentBrandId);
    }


}
