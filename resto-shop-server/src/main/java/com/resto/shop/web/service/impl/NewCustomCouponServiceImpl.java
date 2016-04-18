package com.resto.shop.web.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.NewCustomCouponMapper;
import com.resto.shop.web.model.Coupon;
import com.resto.shop.web.model.Customer;
import com.resto.shop.web.model.NewCustomCoupon;
import com.resto.shop.web.service.CouponService;
import com.resto.shop.web.service.NewCustomCouponService;

import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class NewCustomCouponServiceImpl extends GenericServiceImpl<NewCustomCoupon, Long> implements NewCustomCouponService {

    @Resource
    private NewCustomCouponMapper newcustomcouponMapper;
    
    @Resource
    private CouponService couponService;

    @Override
    public GenericDao<NewCustomCoupon, Long> getDao() {
        return newcustomcouponMapper;
    }

    @Override
    public int insertNewCustomCoupon(NewCustomCoupon brand) {
        
        return newcustomcouponMapper.insertSelective(brand);
    }
    
    @Override
    public List<NewCustomCoupon> selectListByBrandId(String currentBrandId) {
        
        return newcustomcouponMapper.selectListByBrandId(currentBrandId);
    }

	@Override
	public void giftCoupon(Customer cus) {
		//根据 品牌id 查询该品牌的优惠卷配置
	    List<NewCustomCoupon> couponConfigs = newcustomcouponMapper.selectListByBrandId(cus.getBrandId());
		//根据优惠卷配置，添加对应数量的优惠卷
		
	    Date beginDate  = new Date();
//            for (PageData cfg : couponConfigs) {
//                            PageData coupon = new PageData();
//                            coupon.putAll(pd);
//                            coupon.putAll(cfg);
//                            coupon.put("NAME", cfg.getString("COUPON_NAME"));
//                            coupon.put("VALUE", cfg.get("COUPON_VALUE"));
//                            coupon.put("BEGIN_DATE", beginDate);
//                            coupon.put("END_DATE", DateUtil.getAfterDayDate(beginDate,cfg.getInteger("COUPON_VALIDITY")));
//                            coupon.put("SOURCE", CouponSource.NEW_CUSTOMER_COUPON);
//                            coupon.put("MIN_AMOUNT", cfg.get("COUPON_MIN_AMOUNT"));
//                            for(int i=0;i<cfg.getInteger("COUPON_NUMBER");i++){
//                                    couponService.save(coupon);
//                            }
//            }
	    
	    for(NewCustomCoupon cfg: couponConfigs){
	        Coupon coupon = new Coupon();
	        coupon.setName(cfg.getCouponName());
	        coupon.setValue(cfg.getCouponValue());
	        coupon.setBeginDate(beginDate);
	        coupon.setEndDate(beginDate);
	        coupon.setMinAmount(cfg.getCouponMinMoney());
	        
	        for(int i=0;i<cfg.getCouponNumber();i++){
	            couponService.insert(coupon);
	        }
	        
	    }
	    
	}

   

}
