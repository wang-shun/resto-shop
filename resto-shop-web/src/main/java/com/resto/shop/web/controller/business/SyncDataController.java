package com.resto.shop.web.controller.business;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.brand.core.entity.Result;
import com.resto.brand.web.dto.ArticleSellDto;
import com.resto.brand.web.dto.BrandIncomeDto;
import com.resto.brand.web.dto.IncomeReportDto;
import com.resto.brand.web.dto.OrderArticleDto;
import com.resto.brand.web.dto.OrderDetailDto;
import com.resto.brand.web.dto.ShopIncomeDto;
import com.resto.brand.web.model.Brand;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.shop.web.constant.PayMode;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.model.OrderItem;
import com.resto.shop.web.model.OrderPaymentItem;
import com.resto.shop.web.service.OrderItemService;
import com.resto.shop.web.service.OrderPaymentItemService;
import com.resto.shop.web.service.OrderService;

/**
 * 用于同步第三方数据库的Controller
 * 
 * @author lmx
 */
@Controller
@RequestMapping("syncData")
public class SyncDataController extends GenericController {

	@Resource
	private OrderService orderService;

	@Resource
	private BrandService brandService;

	@Resource
	OrderPaymentItemService orderpaymentitemService;

	@Resource
	private ShopDetailService shopDetailService;

	@Resource
	private OrderItemService orderItemService;

	// 品牌总收入，旗下所有店铺收入总和
	@RequestMapping("syncBrandIncome")
	@ResponseBody
	public Result syncBrandIncome(String beginDate, String endDate) {
		// 查询品牌和店铺的收入情况
		List<IncomeReportDto> incomeReportList = orderpaymentitemService.selectIncomeList(getCurrentBrandId(),
				beginDate, endDate);
		// 封装brand所需要的数据结构
		Brand brand = brandService.selectById(getCurrentBrandId());
		List<Map<String, Object>> brandIncomeList = new ArrayList<>();
		Map<String, Object> brandIncomeMap = new HashMap<>();
// 		BrandIncomeDto in = new BrandIncomeDto();
		// 初始化品牌的信息
		BigDecimal wechatIncome = BigDecimal.ZERO;
		BigDecimal redIncome = BigDecimal.ZERO;
		BigDecimal couponIncome = BigDecimal.ZERO;
		BigDecimal chargeAccountIncome = BigDecimal.ZERO;
		BigDecimal chargeGifAccountIncome = BigDecimal.ZERO;
		if (!incomeReportList.isEmpty()) {
			for (IncomeReportDto income : incomeReportList) {
				if (income.getPaymentModeId() == PayMode.WEIXIN_PAY) {
					wechatIncome = wechatIncome.add(income.getPayValue()).setScale(2);
				} else if (income.getPayMentModeId() == PayMode.ACCOUNT_PAY) {
					redIncome = redIncome.add(income.getPayValue()).setScale(2);
				} else if (income.getPayMentModeId() == PayMode.COUPON_PAY) {
					couponIncome = couponIncome.add(income.getPayValue()).setScale(2);
				} else if (income.getPaymentModeId() == PayMode.CHARGE_PAY) {
					chargeAccountIncome = chargeAccountIncome.add(income.getPayValue()).setScale(2);
				} else if (income.getPayMentModeId() == PayMode.REWARD_PAY) {
					chargeGifAccountIncome = chargeGifAccountIncome.add(income.getPayValue()).setScale(2);
				}
			}
		}
		brandIncomeMap.put("brand_id", brand.getId());
		brandIncomeMap.put("brand_name", brand.getBrandName());
		brandIncomeMap.put("wechat_pay", getBigDecimal(wechatIncome));
		brandIncomeMap.put("red_packet_pay", getBigDecimal(redIncome));
		brandIncomeMap.put("coupon_pay", getBigDecimal(couponIncome));
		brandIncomeMap.put("charge_pay", getBigDecimal(chargeAccountIncome));
		brandIncomeMap.put("charge_reward_pay", getBigDecimal(chargeGifAccountIncome));
		BigDecimal total_income = getBigDecimal(wechatIncome).add(getBigDecimal(redIncome)).add(getBigDecimal(couponIncome))
									.add(getBigDecimal(chargeAccountIncome)).add(getBigDecimal(chargeGifAccountIncome));
		brandIncomeMap.put("total_income", total_income);
		brandIncomeList.add(brandIncomeMap);
		return getSuccessResult(brandIncomeList);
	}

	// 店铺总收入
	@RequestMapping("syncShopIncome")
	@ResponseBody
	public Result syncShopIncome(String beginDate, String endDate) {
		// 查询品牌和店铺的收入情况
		List<IncomeReportDto> incomeReportList = orderpaymentitemService.selectIncomeList(getCurrentBrandId(),
				beginDate, endDate);
		// 封装店铺所需要的数据结构
		List<ShopDetail> listShop = shopDetailService.selectByBrandId(getCurrentBrandId());
		List<Map<String,Object>> shopIncomeList = new ArrayList<>();
		for (int i = 0; i < listShop.size(); i++) {// 实际有多少个店铺显示多少个数据
			Map<String,Object> shopIncomeMap = new HashMap<>();
			String shopId = listShop.get(i).getId();
			shopIncomeMap.put("shop_id", shopId);
			shopIncomeMap.put("shop_name", listShop.get(i).getName());
			//设置默认值
			shopIncomeMap.put("wechat_pay", BigDecimal.ZERO);
			shopIncomeMap.put("red_packet_pay",  BigDecimal.ZERO);
			shopIncomeMap.put("coupon_pay",  BigDecimal.ZERO);
			shopIncomeMap.put("charge_pay",  BigDecimal.ZERO);
			shopIncomeMap.put("charge_reward_pay",  BigDecimal.ZERO);
			BigDecimal total_income = BigDecimal.ZERO;//设置总收入为 零
			if (!incomeReportList.isEmpty()) {
				for (IncomeReportDto in : incomeReportList) {
					if (shopId.equals(in.getShopDetailId())) {
						switch (in.getPayMentModeId()) {
							case PayMode.WEIXIN_PAY:
								shopIncomeMap.put("wechat_pay", getBigDecimal(in.getPayValue()));
								total_income = total_income.add(getBigDecimal(in.getPayValue()));
								break;
							case PayMode.ACCOUNT_PAY:
								shopIncomeMap.put("red_packet_pay",  getBigDecimal(in.getPayValue()));
								total_income = total_income.add(getBigDecimal(in.getPayValue()));
								break;
							case PayMode.COUPON_PAY:
								shopIncomeMap.put("coupon_pay",  getBigDecimal(in.getPayValue()));
								total_income = total_income.add(getBigDecimal(in.getPayValue()));
								break;
							case PayMode.CHARGE_PAY:
								shopIncomeMap.put("charge_pay",  getBigDecimal(in.getPayValue()));
								total_income = total_income.add(getBigDecimal(in.getPayValue()));
								break;
							case PayMode.REWARD_PAY:
								shopIncomeMap.put("charge_reward_pay",  getBigDecimal(in.getPayValue()));
								total_income = total_income.add(getBigDecimal(in.getPayValue()));
								break;
						}
					}
				}
			}
			shopIncomeMap.put("total_income", total_income);
			shopIncomeList.add(shopIncomeMap);
		}
		return getSuccessResult(shopIncomeList);
	}

	// 品牌菜品销售总量
	@RequestMapping("syncBrandOrderArticle")
	@ResponseBody
	public Result syncBrandOrderArticle(String beginDate, String endDate) {
		List<OrderArticleDto> oA = orderService.selectOrderArticle(getCurrentBrandId(), beginDate, endDate);
		List<ShopDetail> list = shopDetailService.selectByBrandId(getCurrentBrandId());
		for (OrderArticleDto ot : oA) {
			for (ShopDetail s : list) {
				if (s.getId().equals(ot.getShopId())) {
					ot.setShopName(s.getName());
				}
			}
		}
		return getSuccessResult(oA);
	}

	// 店铺菜品销售总量
	@RequestMapping("syncShopOrderArticle")
	@ResponseBody
	public Result syncOrderItems(String beginDate, String endDate, String shopId) {
		List<ArticleSellDto> list = orderService.selectShopArticleSellByDate(beginDate, endDate, shopId, "0asc");
		return getSuccessResult(list);
	}

	// 订单详情信息
	@RequestMapping("syncOrderDetail")
	@ResponseBody
	public Result syncOrderDetail(String beginDate, String endDate, String shopId) {
		// 查询店铺名称
		ShopDetail shop = shopDetailService.selectById(shopId);
		List<OrderDetailDto> listDto = new ArrayList<>();
		List<Order> list = orderService.selectListByTime(beginDate, endDate, shopId);
		for (Order o : list) {
			OrderDetailDto ot = new OrderDetailDto(o.getId(), o.getShopDetailId(), shop.getName(), o.getCreateTime(),
					"", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
					BigDecimal.ZERO, "", "", "");
			if (o.getCustomer() != null) {
				// 手机号
				if (o.getCustomer().getTelephone() != null && o.getCustomer().getTelephone() != "") {
					ot.setTelephone(o.getCustomer().getTelephone());
				}
			}
			// 订单状态
			if (o.getOrderState() != null) {
				switch (o.getOrderState()) {
				case 1:
					ot.setOrderState("未支付");
					break;
				case 2:
					if (o.getProductionStatus() == 0) {
						ot.setOrderState("已付款");
					} else if (o.getProductionStatus() == 2) {
						ot.setOrderState("等待叫号");
					} else if (o.getProductionStatus() == 5) {
						ot.setOrderState("异常订单");
					}
					break;
				case 9:
					ot.setOrderState("已取消");
					break;
				case 10:
					if (o.getProductionStatus() == 5) {
						ot.setOrderState("异常订单");
					} else {
						ot.setOrderState("已消费");
					}
					break;
				case 11:
					ot.setOrderState("已评价");
					break;
				case 12:
					ot.setOrderState("已分享");
					break;

				default:
					break;
				}
			}
			// 订单评价
			// 判断是否为空，不是所有订单都评价
			if (null != o.getAppraise()) {
				switch (o.getAppraise().getLevel()) {
				case 1:
					ot.setLevel("一星");
					break;
				case 2:
					ot.setLevel("二星");
					break;
				case 3:
					ot.setLevel("三星");
					break;
				case 4:
					ot.setLevel("四星");
					break;
				case 5:
					ot.setLevel("五星");
					break;

				default:
					break;
				}
			}

			// 订单支付
			if (o.getOrderPaymentItems() != null) {
				if (!o.getOrderPaymentItems().isEmpty()) {
					for (OrderPaymentItem oi : o.getOrderPaymentItems()) {
						if (null != oi.getPaymentModeId()) {
							switch (oi.getPaymentModeId()) {
							case 1:
								ot.setWeChatPay(oi.getPayValue());
								break;
							case 2:
								ot.setAccountPay(oi.getPayValue());
								break;
							case 3:
								ot.setCouponPay(oi.getPayValue());
								break;
							case 6:
								ot.setChargePay(oi.getPayValue());
								break;
							case 7:
								ot.setRewardPay(oi.getPayValue());
								break;
							default:
								break;
							}
						}
					}
				}
			}
			// 设置营销撬动率 (订单金额-实际支付金额)/实际支付的金额

			BigDecimal real = ot.getChargePay().add(ot.getWeChatPay());
			BigDecimal temp = o.getOrderMoney().subtract(real);
			String incomPrize = "";
			if (temp.compareTo(BigDecimal.ZERO) != 0) {
				incomPrize = real.divide(temp, 2, BigDecimal.ROUND_HALF_UP) + "";
			}
			ot.setIncomePrize(incomPrize);
			// 订单金额
			ot.setOrderMoney(o.getOrderMoney());
			listDto.add(ot);
		}
		return getSuccessResult(listDto);
	}

	// 订单详情信息
	@RequestMapping("syncOrderItems")
	@ResponseBody
	public Result syncOrderItems(String beginDate, String endDate) {
		List<Map<String, Object>> list_orderItem = orderItemService.selectOrderItems(beginDate, endDate);
		return getSuccessResult(list_orderItem);
	}


	//设置 BigDecimal 默认值为 0
	public BigDecimal getBigDecimal(BigDecimal data){
		return data != null ? data : BigDecimal.ZERO;
	}
}
