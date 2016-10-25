package com.resto.shop.web.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.DateUtil;
import com.resto.shop.web.constant.CouponSource;
import com.resto.shop.web.constant.TimeCons;
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
	public void giftCoupon(Customer cus,Integer couponType,String shopId) {
		//根据 品牌id 查询店铺优惠券(包含品牌和该店铺自己的优惠券)
	    List<NewCustomCoupon> couponConfigs = newcustomcouponMapper.selectListByBrandIdAndIsActive(cus.getBrandId(),couponType);
	    //如果没有找到 对应类型的优惠券，则显示通用的优惠券。用于兼容老版本红包没有设置 优惠券类型问题
	    if(couponConfigs == null || couponConfigs.size()== 0 ){
	    	couponType = -1;
	    	couponConfigs = newcustomcouponMapper.selectListByBrandIdAndIsActive(cus.getBrandId(),couponType);
	    }
		//根据优惠卷配置，添加对应数量的优惠卷
	    Date beginDate  = new Date();
	    for(NewCustomCoupon cfg: couponConfigs){
	        Coupon coupon = new Coupon();
	        coupon.setName(cfg.getCouponName());
	        coupon.setValue(cfg.getCouponValue());
	        coupon.setMinAmount(cfg.getCouponMinMoney());
	        coupon.setCouponType(couponType);
	        coupon.setBeginTime(cfg.getBeginTime());
	        coupon.setEndTime(cfg.getEndTime());
	        coupon.setUseWithAccount(cfg.getUseWithAccount());
	        coupon.setDistributionModeId(cfg.getDistributionModeId());
	        coupon.setCouponSource(CouponSource.NEW_CUSTOMER_COUPON);
	        coupon.setCustomerId(cus.getId());

            //如果是品牌的专有优惠券
            if(cfg.getIsBrand()==1&&cfg.getBrandId()!=null){
                coupon.setBrandId(cfg.getBrandId());
            }

            //如果是店铺专有的优惠券设置 设置该优惠券的shopId表示只有这个店铺可以用
            if(cfg.getShopDetailId()!=null&&shopId.equals(cfg.getShopDetailId())){
                coupon.setShopDetailId(cfg.getShopDetailId());
            }
	        //优惠券时间选择的类型分配时间
	        if(cfg.getTimeConsType()==TimeCons.MODELA){
	        	coupon.setBeginDate(beginDate);
		        coupon.setEndDate(DateUtil.getAfterDayDate(beginDate,cfg.getCouponValiday()));
	        }else if(cfg.getTimeConsType()==TimeCons.MODELB){
	        	coupon.setBeginDate(cfg.getBeginDateTime());
	        	coupon.setEndDate(cfg.getEndDateTime());
	        }
	        
	        for(int i=0;i<cfg.getCouponNumber();i++){
	            couponService.insertCoupon(coupon);
	        }
	        
	    }
	    
	}


    @Override
	public List<NewCustomCoupon> selectListByCouponType(String brandId, Integer couponType,String shopId) {
        List<NewCustomCoupon> list = new ArrayList<>();
        //查询品牌设置的优惠券
        List<NewCustomCoupon> brandList = newcustomcouponMapper.selectListByCouponTypeAndBrandId(brandId,couponType);
        //查询店铺设置的优惠券
        List<NewCustomCoupon> shopList = newcustomcouponMapper.selectListByCouponTypeAndShopId(shopId,couponType);
		list.addAll(brandList);
        list.addAll(shopList);
		//如果没有找到 对应类型的优惠券，则显示通用的优惠券。用于兼容老版本红包没有设置 优惠券类型问题
		if(list==null || list.size()==0){
			list = newcustomcouponMapper.selectListByCouponType(brandId, -1);
		}
		return list;
	}

   

}
