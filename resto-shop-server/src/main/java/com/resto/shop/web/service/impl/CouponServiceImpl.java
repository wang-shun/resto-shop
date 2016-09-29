package com.resto.shop.web.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.ApplicationUtils;
import com.resto.brand.core.util.DateUtil;
import com.resto.shop.web.constant.PayMode;
import com.resto.shop.web.dao.CouponMapper;
import com.resto.shop.web.exception.AppException;
import com.resto.shop.web.model.Coupon;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.model.OrderPaymentItem;
import com.resto.shop.web.service.CouponService;

import cn.restoplus.rpc.server.RpcService;
import com.resto.shop.web.service.OrderPaymentItemService;

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

	@Resource
	OrderPaymentItemService orderPaymentItemService;

    @Override
    public void insertCoupon(Coupon coupon) {
        coupon.setId(ApplicationUtils.randomUUID());
        coupon.setUsingTime(null);
        coupon.setIsUsed(false);
        couponMapper.insertSelective(coupon);
    }

	@Override
	public Coupon useCoupon(BigDecimal totalMoney, Order order) throws AppException {
		Coupon coupon = selectById(order.getUseCoupon());
		//判断优惠卷是否已使用
		if(coupon.getIsUsed()){
			throw new AppException(AppException.COUPON_IS_USED);
		}
		//判断优惠卷有效期
		Date beginDate = DateUtil.getDateBegin(coupon.getBeginDate());
		Date endDate = DateUtil.getDateEnd(coupon.getEndDate());
		Date now = new Date();
		if(beginDate.getTime()>now.getTime()||endDate.getTime()<now.getTime()){
			throw new AppException(AppException.COUPON_IS_EXPIRE);
		}
		//判断优惠卷使用时间段
		int beginMin = DateUtil.getMinOfDay(coupon.getBeginTime());
		int endMin = DateUtil.getMinOfDay(coupon.getEndTime());
		int nowMin = DateUtil.getMinOfDay(now);
		if(beginMin>nowMin||endMin<nowMin){
			throw new AppException(AppException.COUPON_TIME_ERR);
		}
		
		//判断优惠卷使用类型是否0 或者是否等于订单类型
		if(coupon.getDistributionModeId()!=0&&coupon.getDistributionModeId()!=order.getDistributionModeId()){
			if(coupon.getDistributionModeId()!=1&&order.getDistributionModeId()!=3){
				throw new AppException(AppException.COUPON_MODE_ERR);
			}
		}
		//判断优惠卷订单金额是否大于优惠卷可用金额
		if(totalMoney.compareTo(totalMoney)<0){
			throw new AppException(AppException.COUPON_MIN_AMOUNT_ERR);
		}
		//判断是否使用了余额 并且 当前优惠卷可否使用余额
		if(order.isUseAccount()&&!coupon.getUseWithAccount()){
			throw new AppException(AppException.COUPON_NOT_USEACCOUNT);
		}
		
		coupon.setIsUsed(true);
		coupon.setUsingTime(new Date());
		this.update(coupon);
		return coupon;
	}

	@Override
	public void refundCoupon(String id) {
		Coupon coupon = selectById(id);
		coupon.setIsUsed(false);
		coupon.setRemark("退还优惠卷");
		update(coupon);
	}

	@Override
	public List<Coupon> listCouponByStatus(String status, String customerId) {
		String IS_EXPIRE = null;
		String NOT_EXPIRE = null;
		if("2".equals(status)){
			IS_EXPIRE = "IS_EXPIRE";
			status = null;
		}
		if("0".equals(status)){
			NOT_EXPIRE = "NOT_EXPIRE";
		}
		return couponMapper.listCouponByStatus(status, IS_EXPIRE, NOT_EXPIRE, customerId);
	}

	@Override
	public void useCouponById(String orderId, String id) {
		Coupon coupon = selectById(id);
		coupon.setIsUsed(true);
		coupon.setRemark("后付款消费优惠券");
		update(coupon);

		OrderPaymentItem item = new OrderPaymentItem();
		item.setId(ApplicationUtils.randomUUID());
		item.setOrderId(orderId);
		item.setPaymentModeId(PayMode.COUPON_PAY);
		item.setPayTime(new Date());
		item.setPayValue(coupon.getValue());
		item.setRemark("优惠卷支付:" + item.getPayValue());
		item.setResultData(coupon.getId());
		orderPaymentItemService.insert(item);
	}
}
