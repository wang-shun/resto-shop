package com.resto.shop.web.controller.business;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.resto.brand.core.entity.Result;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.AccountLog;
import com.resto.shop.web.model.Coupon;
import com.resto.shop.web.model.OrderPaymentItem;
import com.resto.shop.web.service.AccountLogService;
import com.resto.shop.web.service.CouponService;
import com.resto.shop.web.service.OrderPaymentItemService;

@Controller
@RequestMapping("/brandMarketing")
public class BrandMarketingController extends GenericController{

	@Resource
	private AccountLogService accountLogService;
	
	@Resource
	private OrderPaymentItemService orderPaymentItemService;
	
	@Resource
	private CouponService couponService;
	
	@RequestMapping("/list")
	public void list(){}
	
	@RequestMapping("/selectAll")
	@ResponseBody
	public Result list_all(){
		Result result = new Result();
		try{
			List<AccountLog> accountLogs = accountLogService.selectAccountLog();
			List<OrderPaymentItem> orderPaymentItems = orderPaymentItemService.selectOrderPayMentItem();
			List<Coupon> coupons = couponService.selectCoupon();
			JSONObject object = new JSONObject();
			object.put("brandName", getBrandName());
			BigDecimal redMoneyAll = new BigDecimal(0);
			for(AccountLog accountLog : accountLogs){
				if(accountLog.getSource().equals(1)){
					object.put("plRedMoney", accountLog.getMoney());
					redMoneyAll = redMoneyAll.add(accountLog.getMoney());
				}else if(accountLog.getSource().equals(3)){
					object.put("czRedMoney", accountLog.getMoney());
					redMoneyAll = redMoneyAll.add(accountLog.getMoney());
				}else if(accountLog.getSource().equals(4)){
					object.put("fxRedMoney", accountLog.getMoney());
					redMoneyAll = redMoneyAll.add(accountLog.getMoney());
				}
			}
			for(OrderPaymentItem paymentItem : orderPaymentItems){
				if(paymentItem.getPaymentModeId().equals(8)){
					object.put("dwRedMoney", paymentItem.getPayValue());
					redMoneyAll = redMoneyAll.add(paymentItem.getPayValue());
				}else if(paymentItem.getPaymentModeId().equals(11)){
					object.put("tcRedMoney", paymentItem.getPayValue());
					redMoneyAll = redMoneyAll.add(paymentItem.getPayValue());
				}
			}
			object.put("redMoneyAll", redMoneyAll);
			BigDecimal couponAllMoney = new BigDecimal(0);
			for(Coupon coupon : coupons){
				if(coupon.getCouponType().equals(0)){
					object.put("zcCouponMoney", coupon.getValue());
					couponAllMoney = couponAllMoney.add(coupon.getValue());
				}else if(coupon.getCouponType().equals(1)){
					object.put("yqCouponMoney", coupon.getValue());
					couponAllMoney = couponAllMoney.add(coupon.getValue());
				}
			}
			object.put("couponAllMoney", couponAllMoney);
			JSONArray array = new JSONArray();
			array.add(object);
			return getSuccessResult(array);
		}catch (Exception ex) {
			log.error(ex.getMessage());
			log.debug("查询出错!");
			result.setSuccess(false);
			result.setMessage("查询出错!");
		}
		return result;
	}
}
