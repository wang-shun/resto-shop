package com.resto.shop.web.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.resto.brand.core.util.*;
import com.resto.brand.web.dto.ArticleTopDto;
import com.resto.brand.web.dto.BackCustomerDto;
import com.resto.brand.web.model.*;
import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.brand.web.service.WechatConfigService;
import com.resto.brand.web.service.WetherService;
import com.resto.shop.web.constant.*;
import com.resto.shop.web.model.*;
import com.resto.shop.web.service.*;
import com.resto.shop.web.util.JdbcSmsUtils;
import com.resto.shop.web.util.LogTemplateUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

import static com.resto.brand.core.util.HttpClient.doPostAnsc;
import static com.resto.brand.core.util.OrderCountUtils.formatDouble;
import static com.resto.brand.core.util.OrderCountUtils.getOrderMoney;

public class CloseShopServieImpl implements CloseShopService{


	@Resource
	private OrderService orderService;

	@Resource
	private CustomerService customerService;

	@Resource
	private WechatConfigService wechatConfigService;


	@Resource
	private BrandService brandService;

	@Resource
	private ShopDetailService shopDetailService;

	@Resource
	private OrderItemService orderItemService;

	@Resource
	private WetherService wetherService;

	@Resource
	private OffLineOrderService offLineOrderService;


	@Resource
	private AppraiseService appraiseService;

	@Resource
	private ChargeOrderService chargeOrderService;

	@Resource
	private ArticleTopService articleTopService;

	@Resource
	private DayDataMessageService dayDataMessageService;

	@Resource
	private DayAppraiseMessageService dayAppraiseMessageService;

	Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public void cleanShopOrder(ShopDetail shopDetail, OffLineOrder offLineOrder, Brand brand) {

		//1.结店退款
		refundShopDetailOrder(shopDetail);
		//2查询天气
		Wether wether = wetherService.selectDateAndShopId(shopDetail.getId(), DateUtil.formatDate(new Date(),"yyyy-MM-dd"));
		if(wether==null){//说明没有调用定时任务 ---
			wether = new Wether();
			wether.setDayWeather("---");
			wether.setDayTemperature(-1);
			wether.setWeekady(-1);
		}
		WechatConfig wechatConfig = wechatConfigService.selectByBrandId(brand.getId());
		//短信第一版用来发日结短信
		Map<String, String> dayMapByFirstEdtion = querryDateDataByFirstEdtion(shopDetail, offLineOrder);
		//3发短信推送/微信推送
		pushMessageByFirstEdtion(dayMapByFirstEdtion, shopDetail, wechatConfig, brand.getBrandName());
		//3判断是否需要发送旬短信
		int temp = DateUtil.getEarlyMidLate();
		switch (temp){
			case  1:
				//第一版旬结短信
				Map<String, String> xunMapByFirstEdtion = querryXunDataByFirstEditon(shopDetail);
				pushMessageByFirstEdtion(xunMapByFirstEdtion, shopDetail, wechatConfig, brand.getBrandName());
				break;

			case 2:
				Map<String, String> xunMapByFirstEdtion2 = querryXunDataByFirstEditon(shopDetail);
				pushMessageByFirstEdtion(xunMapByFirstEdtion2, shopDetail, wechatConfig, brand.getBrandName());
				break;

			case 3:
				Map<String, String> xunMapByFirstEdtion3 = querryXunDataByFirstEditon(shopDetail);
				pushMessageByFirstEdtion(xunMapByFirstEdtion3, shopDetail, wechatConfig, brand.getBrandName());

				Map<String, String> monthMapByFirstEdtion = querryMonthDataByFirstEditon(shopDetail, offLineOrder);
				pushMessageByFirstEdtion(monthMapByFirstEdtion, shopDetail, wechatConfig, brand.getBrandName());
				break;

		}

		//第二版短信内容由于模板原因无法发送短信 因此保留第一版短信 第二版数据存到大数据库数据库中
		insertDateData(shopDetail,offLineOrder,wether,brand);

	}

	private void refundShopDetailOrder(ShopDetail shopDetail) {
		String[] orderStates = new String[]{OrderState.SUBMIT + "", OrderState.PAYMENT + ""};//未付款和未全部付款和已付款
		String[] productionStates = new String[]{ProductionStatus.NOT_ORDER + ""};//已付款未下单
		List<Order> orderList = orderService.selectByOrderSatesAndProductionStates(shopDetail.getId(), orderStates, productionStates);
		for (Order order : orderList) {
			if (!order.getClosed()) {//判断订单是否已被关闭，只对未被关闭的订单做退单处理
				sendWxRefundMsg(order);
			}
		}
		// 查询已付款且有支付项但是生产状态没有改变的订单
		List<Order> orderstates = orderService.selectHasPayNoChangeStatus(shopDetail.getId(), DateUtil.getDateBegin(new Date()), DateUtil.getDateEnd(new Date()));
		if (!orderstates.isEmpty()) {
			for (Order o : orderstates) {
				if (o.getOrderMode() == ShopMode.CALL_NUMBER) {
					o.setProductionStatus(ProductionStatus.HAS_CALL);
				} else {
					o.setProductionStatus(ProductionStatus.PRINTED);
				}
				orderService.update(o);
			}
		}

	}

	public void sendWxRefundMsg(Order order) {
		if (orderService.checkRefundLimit(order)) {
			orderService.autoRefundOrder(order.getId());
			log.info("款项自动退还到相应账户:" + order.getId());
			Customer customer = customerService.selectById(order.getCustomerId());
			WechatConfig config = wechatConfigService.selectByBrandId(order.getBrandId());
			Brand brand = brandService.selectById(customer.getBrandId());
			ShopDetail shopDetail = shopDetailService.selectById(order.getShopDetailId());
			StringBuilder sb = null;
			if (order.getOrderState().equals(OrderState.SUBMIT)) {//未支付和未完成支付
				sb = new StringBuilder("亲,今日未完成支付的订单已被商家取消,欢迎下次再来本店消费\n");
			} else {//已支付未消费
				sb = new StringBuilder("亲,今日未消费订单已自动退款,欢迎下次再来本店消费\n");
			}
			sb.append("订单编号:" + order.getSerialNumber() + "\n");
			if (order.getOrderMode() != null) {
				switch (order.getOrderMode()) {
					case ShopMode.TABLE_MODE:
						sb.append("桌号:" + (order.getTableNumber() != null ? order.getTableNumber() : "无") + "\n");
						break;
					default:
						sb.append("取餐码：" + (order.getVerCode() != null ? order.getVerCode() : "无") + "\n");
						break;
				}
			}
			if (order.getShopName() == null || "".equals(order.getShopName())) {
				order.setShopName(shopDetailService.selectById(order.getShopDetailId()).getName());
			}
			sb.append("就餐店铺：" + order.getShopName() + "\n");
			sb.append("订单时间：" + DateFormatUtils.format(order.getCreateTime(), "yyyy-MM-dd HH:mm") + "\n");
			sb.append("订单明细：\n");
			List<OrderItem> orderItem = orderItemService.listByOrderId(order.getId());
			for (OrderItem item : orderItem) {
				sb.append("  " + item.getArticleName() + "x" + item.getCount() + "\n");
			}
			sb.append("订单金额：" + order.getOrderMoney() + "\n");
			WeChatUtils.sendCustomerMsgASync(sb.toString(), customer.getWechatId(), config.getAppid(), config.getAppsecret());
//            UserActionUtils.writeToFtp(LogType.ORDER_LOG, brand.getBrandName(), shopDetail.getName(), order.getId(),
//                    "订单发送推送：" + sb.toString());
			Map map = new HashMap(4);
			map.put("brandName", brand.getBrandName());
			map.put("fileName", customer.getId());
			map.put("type", "UserAction");
			map.put("content", "系统向用户:" + customer.getNickname() + "推送微信消息:" + sb.toString() + ",请求服务器地址为:" + MQSetting.getLocalIP());
			doPostAnsc(LogUtils.url, map);
		} else {
			log.info("款项自动退还到相应账户失败，订单状态不是已付款或商品状态不是已付款未下单");
		}
	}

	/**
	 * 第一版短信 日结数据封装
	 * @param shopDetail
	 * @param offLineOrder
	 * @return
	 */
	private Map<String,String> querryDateDataByFirstEdtion(ShopDetail shopDetail, OffLineOrder offLineOrder) {
		// 查询该店铺是否结过店
		OffLineOrder offLineOrder1 = offLineOrderService.selectByTimeSourceAndShopId(OfflineOrderSource.OFFLINE_POS, shopDetail.getId(), DateUtil.getDateBegin(new Date()), DateUtil.getDateEnd(new Date()));
		if (null != offLineOrder1) {
			offLineOrder1.setState(0);
			offLineOrderService.update(offLineOrder1);
		}
		offLineOrder.setId(ApplicationUtils.randomUUID());
		offLineOrder.setState(1);
		offLineOrder.setResource(OfflineOrderSource.OFFLINE_POS);
		offLineOrderService.insert(offLineOrder);

		//----1.定义时间---
		Date todayBegin = DateUtil.getDateBegin(new Date());
		Date todayEnd = DateUtil.getDateEnd(new Date());
		//本月的开始时间 本月结束时间
		String beginMonth = DateUtil.getMonthBegin();
		Date begin = DateUtil.getDateBegin(DateUtil.fomatDate(beginMonth));
		Date end = todayEnd;
		//三.定义线下订单
		//本日线下订单总数(堂吃)
		int todayEnterCount = 0;
		//本日线下订单总额(堂吃)
		BigDecimal todayEnterTotal = BigDecimal.ZERO;
		//本月线下订单总数
		int monthEnterCount = 0;
		//本月线下订单总额
		BigDecimal monthEnterTotal = BigDecimal.ZERO;

		//4.外卖订单
		//本日外卖订单数
		int todayDeliverOrders = 0;
		//本日外卖订单总额
		BigDecimal todayOrderBooks = BigDecimal.ZERO;
		//本月外卖订单数
		int monthDeliverOrder = 0;
		//本月外卖订单总额
		BigDecimal monthOrderBooks = BigDecimal.ZERO;
		//查询pos端店铺录入信息(线下订单+外卖订单都是pos端录入的)
		List<OffLineOrder> offLineOrderList = offLineOrderService.selectlistByTimeSourceAndShopId(shopDetail.getId(), begin, end, OfflineOrderSource.OFFLINE_POS);
		if (!offLineOrderList.isEmpty()) {
			for (OffLineOrder of : offLineOrderList) {
				List<Integer> getTime = DateUtil.getDayByToday(of.getCreateTime());
				if (getTime.contains(2)) {//本日中
					todayEnterCount += of.getEnterCount();
					todayEnterTotal = todayEnterTotal.add(of.getEnterTotal());
					todayDeliverOrders += of.getDeliveryOrders();
					todayOrderBooks = todayOrderBooks.add(of.getOrderBooks());
				}
				if (getTime.contains(10)) {
					monthEnterCount += of.getEnterCount();
					monthEnterTotal = monthEnterTotal.add(of.getEnterTotal());
					monthDeliverOrder += of.getDeliveryOrders();
					monthOrderBooks = monthOrderBooks.add(of.getOrderBooks());
				}
			}
		}
		//查询当日新增用户的订单
		List<Order> newCustomerOrders = orderService.selectNewCustomerOrderByShopIdAndTime(shopDetail.getId(), todayBegin, todayEnd);
		//新增用户的订单总数
		int newCustomerOrderNum = 0;
		//新增用户的订单总额
		BigDecimal newCustomerOrderTotal = BigDecimal.ZERO;
		//新增分享用户的的订单总数
		int newShareCustomerOrderNum = 0;
		//新增分享用户的订单总额
		BigDecimal newShareCustomerOrderTotal = BigDecimal.ZERO;
		//新增自然用户的订单总数
		int newNormalCustomerOrderNum = 0;
		//新增自然用户的订单总额
		BigDecimal newNormalCustomerOrderTotal = BigDecimal.ZERO;
		if (!newCustomerOrders.isEmpty()) {
			for (Order o : newCustomerOrders) {
				newCustomerOrderNum++;
				newCustomerOrderTotal = newCustomerOrderTotal.add(getOrderMoney( o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
				if (o.getCustomer() != null && !StringUtils.isEmpty(o.getCustomer().getShareCustomer())) { //是分享用户
					newShareCustomerOrderNum++;
					newShareCustomerOrderTotal = newShareCustomerOrderTotal.add(getOrderMoney( o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
				} else {
					newNormalCustomerOrderNum++; //是新增用户
					newNormalCustomerOrderTotal = newNormalCustomerOrderTotal.add(getOrderMoney(o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
				}
			}
		}
		//查询回头用户的
		List<BackCustomerDto> backCustomerDtos = orderService.selectBackCustomerByShopIdAndTime(shopDetail.getId(), todayBegin, todayEnd);
		//回头用户
		Set<String> backCustomerId = new HashSet<>();
		//二次回头用户
		Set<String> backTwoCustomerId = new HashSet<>();
		//多次回头用户
		Set<String> backTwoMoreCustomerId = new HashSet<>();
		if (!backCustomerDtos.isEmpty()) {
			for (BackCustomerDto b : backCustomerDtos) {
				backCustomerId.add(b.getCustomerId());
				if (b.getNum() == 1) { //只要以前出现过一次那么就是二次回头用户 而非 ==2
					backTwoCustomerId.add(b.getCustomerId());
				} else if (b.getNum() > 1) {
					backTwoMoreCustomerId.add(b.getCustomerId());
				}
			}
		}
		//查询当日已消费的订单
		//回头用户的订单总数
		int backCustomerOrderNum = 0;
		//二次回头用户的订单总数
		int backTwoCustomerOrderNum = 0;
		//多次回头用户的订单总数
		int backTwoMoreCustomerOderNum = 0;
		//回头用户的订单总额
		BigDecimal backCustomerOrderTotal = BigDecimal.ZERO;
		//二次回头用户的订单总额
		BigDecimal backTwoCustomerOrderTotal = BigDecimal.ZERO;
		//多次回头用户的订单总额
		BigDecimal backTwoMoreCustomerOrderTotal = BigDecimal.ZERO;
		List<Order> orders = orderService.selectCompleteByShopIdAndTime(shopDetail.getId(), todayBegin, todayEnd);
		if (!orders.isEmpty()) {
			for (Order o : orders) {
					if (backCustomerId.contains(o.getCustomerId())) {
						backCustomerOrderNum++;
						backCustomerOrderTotal = backCustomerOrderTotal.add(getOrderMoney( o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
					}
					if (backTwoCustomerId.contains(o.getCustomerId())) {
						backTwoCustomerOrderNum++;
						backTwoCustomerOrderTotal = backTwoCustomerOrderTotal.add(getOrderMoney(o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
					}
					if (backTwoMoreCustomerId.contains(o.getCustomerId())) {
						backTwoMoreCustomerOrderTotal = backTwoMoreCustomerOrderTotal.add(getOrderMoney(o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
						backTwoMoreCustomerOderNum++;
					}
			}
		}
		//2定义resto订单
		//本日resto订单总数 新增+回头
		int todayRestoCount = backCustomerOrderNum + newCustomerOrderNum;
		//本日resto订单总额
		BigDecimal todayRestoTotal = BigDecimal.ZERO;
		//本月resto订单总数
		Set<String> monthRestoCount = new HashSet<>();
		//本月resto订单总额
		BigDecimal monthRestoTotal = BigDecimal.ZERO;
		//定义折扣合计
		BigDecimal discountTotal = BigDecimal.ZERO;
		//红包
		BigDecimal redPackTotal = BigDecimal.ZERO;
		//优惠券
		BigDecimal couponTotal = BigDecimal.ZERO;
		//充值赠送
		BigDecimal chargeReturn = BigDecimal.ZERO;
		//折扣比率
		String discountRatio = "";
		//本日用户消费比率
		String todayCustomerRatio = "";
		//回头用户消费比率
		String todayBackCustomerRatio = "";
		//新增用户比率
		String todayNewCustomerRatio = "";

		List<Order> monthOrders = orderService.selectCompleteByShopIdAndTime(shopDetail.getId(),begin, end);
		if (!monthOrders.isEmpty()) {
			for (Order o : monthOrders) {
				//封装   1.resto订单总额     3.resto订单总数  4订单中的实收总额  5新增用户的订单总额  6自然到店的用户总额  7分享到店的用户总额
				//8回头用户的订单总额  9二次回头用户的订单总额  10多次回头用户的订单总额 11折扣合计 12红包 13优惠券 14 充值赠送 15折扣比率
				//本日 begin-----------------------
				// if (DateUtil.getDayByToday(o.getCreateTime()).contains(2)) {
				/**
				 * 报表数据中的订单数  如果子订单和父订单算是一个订单
				 * 小程序+每日短信里的子订单和父订单算是两个订单
				 *
				 */

				if (o.getCreateTime().compareTo(todayBegin) > 0 && o.getCreateTime().compareTo(todayEnd) < 0) {//今日内订单
					//1.resto订单总额
					todayRestoTotal = todayRestoTotal.add(getOrderMoney( o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));

					//11折扣合计 12红包 13优惠券 14 充值赠送 15折扣比率
					if (!o.getOrderPaymentItems().isEmpty()) {
						//订单支付项
						for (OrderPaymentItem oi : o.getOrderPaymentItems()) {
							if (oi.getPaymentModeId() == PayMode.ACCOUNT_PAY) {
								redPackTotal = redPackTotal.add(oi.getPayValue());
							} else if (oi.getPaymentModeId() == PayMode.COUPON_PAY) {
								couponTotal = couponTotal.add(oi.getPayValue());
							} else if (oi.getPaymentModeId() == PayMode.REWARD_PAY) {
								chargeReturn = chargeReturn.add(oi.getPayValue());
							}
						}
					}
					discountTotal = redPackTotal.add(couponTotal).add(chargeReturn);
					if (todayRestoTotal.add(discountTotal).compareTo(BigDecimal.ZERO) > 0) {
						discountRatio = discountTotal.divide(todayRestoTotal.add(discountTotal), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).toString();
					}
				}
				//本日end----------
				//本月开始------
				//订单总额
				monthRestoTotal = monthRestoTotal.add(getOrderMoney(o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
				//本月结束
			}
		}

		//本日用户消费比率 R+线下+外卖
		//到店总笔数 线上+线下
		double dmax = todayEnterCount + todayRestoCount;
		if (dmax != 0) {
			//本日用户消费比率
			todayCustomerRatio = formatDouble((todayRestoCount / dmax) * 100);
			//本日新增用户利率
			todayNewCustomerRatio = formatDouble((newCustomerOrderNum / dmax) * 100);
			//本日回头用户的消费比率
			todayBackCustomerRatio = formatDouble((backCustomerOrderNum / dmax) * 100);
		}

		//五星
		int fiveStar = 0;
		//四星
		int fourStar = 0;
		//3星-1星
		int oneToThreeStar = 0;
		//3定义满意度
		//本日满意度
		String todaySatisfaction = "";
		//本旬满意度
		String theTenDaySatisfaction = "";
		//本月满意度
		String monthSatisfaction = "";

		int dayAppraiseNum = 0;//当日评价的总单数
		int xunAppraiseNum = 0;//本旬评价的总单数
		int monthAppraiseSum = 0;//本月评价的单数

		double dayAppraiseSum = 0;//当日所有评价的总分数
		double xunAppraiseSum = 0;//上旬所有评价的总分数
		double monthAppraiseNum = 0;//本月所有评价的总分数

		/**
		 * 评价 和 满意度 错误的原因 用户可能今天 下单 但是隔天
		 * 去评价 而现在 是查当天下单当天评价
		 *
		 *
		 */

		//单独查询评价和分数

		List<Appraise> appraises = appraiseService.selectByTimeAndShopId(shopDetail.getId(), begin, end);

		if (!appraises.isEmpty()) {
			for (Appraise a : appraises) {
				//本日 begin-----------------------
				if (DateUtil.getDayByToday(a.getCreateTime()).contains(2)) {
					dayAppraiseNum++;
					dayAppraiseSum += a.getLevel() * 20;
					if (a.getLevel() == 5) {
						fiveStar++;
					} else if (a.getLevel() == 4) {
						fourStar++;
					} else {
						oneToThreeStar++;
					}
				}
				//本旬开始
				if (DateUtil.getDayByToday(a.getCreateTime()).contains(12)) {
					//2.满意度
					xunAppraiseNum++;
					xunAppraiseSum += a.getLevel() * 20;
				}
				//本旬结束

				//本月开始------
				//.满意度

				monthAppraiseNum++;
				monthAppraiseSum += a.getLevel() * 20;

				//本月结束
			}
			//循环完之后操作--
			if (dayAppraiseNum != 0) {
				todaySatisfaction = formatDouble(dayAppraiseSum / dayAppraiseNum);
			}
			if (xunAppraiseNum != 0) {
				theTenDaySatisfaction = formatDouble(xunAppraiseSum / xunAppraiseNum);
			}

			if (monthAppraiseNum != 0) {
				monthSatisfaction = formatDouble(monthAppraiseSum / monthAppraiseNum);
			}

			//评论结束------------------------
		}

		//发送本日信息 本月信息 上旬信息
		//本日信息
		StringBuilder todayContent = new StringBuilder();

		todayContent.append("{")
				.append("shopName:").append("'").append(shopDetail.getName()).append("'").append(",")
				.append("datetime:").append("'").append(DateUtil.formatDate(new Date(), "yyyy-MM-dd")).append("'").append(",")
				//到店总笔数(r+线下)-----
				.append("arriveCount:").append("'").append(todayEnterCount + todayRestoCount).append("'").append(",")
				//到店消费总额 我们的总额+线下的总额，不包含外卖金额
				.append("arriveTotalAmount:").append("'").append(todayEnterTotal.add(todayRestoTotal)).append("'").append(",")
				//用户消费笔数  R+订单总数
				.append("customerPayCount:").append("'").append(todayRestoCount).append("'").append(",")
				//用户消费金额: (r+订单总额)
				.append("customerPayAmount:").append("'").append(todayRestoTotal).append("'").append(",")
				//用户消费比率  今日 R+订单总数/（R+订单总数+线下堂吃订单数+外卖订单数））
				.append("userPayPercent:").append("'").append(todayCustomerRatio).append("%").append("'").append(",")
				//回头消费比率 R+多次消费用户数/R+消费用户数）
				.append("userBackPercent:").append("'").append(todayBackCustomerRatio).append("%").append("'").append(",")
				//新增用户比率 （今日 R+新增用户数/R+消费用户数）
				.append("newCustomerPercent:").append("'").append(todayNewCustomerRatio).append("%").append("'").append(",")
				//新用户消费
				.append("newCustomerPay:").append("'").append(newCustomerOrderNum).append("笔/").append(newCustomerOrderTotal).append("'").append(",")
				// 其中自然用户
				.append("natureCustomerPay:").append("'").append(newNormalCustomerOrderNum).append("笔/").append(newNormalCustomerOrderTotal).append("'").append(",")
				//其中分享用户
				.append("shareCustomerPay:").append("'").append(newShareCustomerOrderNum).append("笔/").append(newShareCustomerOrderTotal).append("'").append(",")
				//回头用户消费
				.append("customerBackPay:").append("'").append(backCustomerOrderNum).append("笔/").append(backCustomerOrderTotal).append("'").append(",")
				//二次回头用户
				.append("secondBackPay:").append("'").append(backTwoCustomerOrderNum).append("笔/").append(backTwoCustomerOrderTotal).append("'").append(",")
				//多次回头
				.append("moreBackPay:").append("'").append(backTwoMoreCustomerOderNum).append("笔/").append(backTwoMoreCustomerOrderTotal).append("'").append(",")
				//折扣合计:648.05（使用红包总额+使用优惠券总额+使用充值赠送总额）
				.append("discountTotal:").append("'").append(discountTotal).append("'").append(",")
				//红包
				.append("redPayTotal:").append("'").append(redPackTotal).append("'").append(",")
				//优惠券
				.append("couponTotal:").append("'").append(couponTotal).append("'").append(",")
				//充值赠送
				.append("chargeTotal:").append("'").append(chargeReturn).append("'").append(",")
				//折扣比率2.7%（折扣合计/(堂吃消费总额＋折扣合计)
				.append("discountPercent:").append("'").append(discountRatio).append("%").append("'").append(",")
				//五星评论
				.append("goodCount:").append("'").append(fiveStar).append("'").append(",")
				//本日改进意见
				.append("badCount:").append("'").append(fourStar).append("'").append(",")
				//本日差评投诉
				.append("terribleCount:").append("'").append(oneToThreeStar).append("'").append(",")
				//本日满意度
				.append("satisfied1:").append("'").append(todaySatisfaction).append("'").append(",")
				//本旬满意度
				.append("satisfied2:").append("'").append(theTenDaySatisfaction).append("'").append(",")
				//本月满意度
				.append("satisfied3:").append("'").append(monthSatisfaction).append("'").append(",")
				//今日外卖金额
				.append("outFoodTotal:").append("'").append(todayOrderBooks).append("'").append(",")
				//总营业额
				.append("totalOrderMoney:").append("'").append(todayEnterTotal.add(todayRestoTotal).add(todayOrderBooks)).append("'").append(",")
				//本月总额
				.append("monthTotalMoney:").append("'").append(monthOrderBooks.add(monthEnterTotal).add(monthRestoTotal)).append("'")
				.append("}");

		//封装微信推送文本
		StringBuilder sb = new StringBuilder();
		sb
				.append("店铺名称:").append(shopDetail.getName()).append("\n")
				.append("时间:").append(DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss")).append("\n")
				.append("到店总笔数:").append(todayEnterCount + todayRestoCount).append("\n")
				.append("到店消费总额:").append(todayEnterTotal.add(todayRestoTotal)).append("\n")
				.append("---------------------").append("\n")
				.append("用户消费比数:").append(todayRestoCount).append("\n")
				.append("用户消费金额").append(todayRestoTotal).append("\n")
				.append("---------------------").append("\n")
				.append("用户消费比率:").append(todayCustomerRatio).append("%").append("\n")
				.append("回头消费比率:").append(todayBackCustomerRatio).append("%").append("\n")
				.append("新增用户比率:").append(todayNewCustomerRatio).append("%").append("\n")
				.append("---------------------").append("\n")
				.append("新用户消费:").append(newCustomerOrderNum).append("笔/").append(newCustomerOrderTotal).append("\n")
				.append("其中自然用户:").append(newNormalCustomerOrderNum).append("笔/").append(newNormalCustomerOrderTotal).append("\n")
				.append("其中分享用户:").append(newShareCustomerOrderNum).append("笔/").append(newShareCustomerOrderTotal).append("\n")
				.append("回头用户消费:").append(backCustomerOrderNum).append("笔/").append(backCustomerOrderTotal).append("\n")
				.append("二次回头用户:").append(backTwoCustomerOrderNum).append("笔/").append(backTwoCustomerOrderTotal).append("\n")
				.append("多次回头用户:").append(backTwoMoreCustomerOderNum).append("笔/").append(backTwoMoreCustomerOrderTotal).append("\n")
				.append("---------------------").append("\n")
				.append("折扣合计:").append(discountTotal).append("\n")
				.append("红包:").append(redPackTotal).append("\n")
				.append("优惠券:").append(couponTotal).append("\n")
				.append("充值赠送:").append(chargeReturn).append("\n")
				.append("折扣比率").append(discountRatio).append("\n")
				.append("---------------------").append("\n")
				.append("本日五星评论:").append(fiveStar).append("\n")
				.append("本日更改意见:").append(fourStar).append("\n")
				.append("本日差评投诉:").append(oneToThreeStar).append("\n")
				.append("本日满意度:").append(todaySatisfaction).append("\n")
				.append("本旬满意度:").append(theTenDaySatisfaction).append("\n")
				.append("本月满意度:").append(monthSatisfaction).append("\n")
				.append("---------------------").append("\n")
				.append("今日外卖金额:").append(todayOrderBooks).append("\n")
				.append("今日总营业额:").append(todayEnterTotal.add(todayRestoTotal).add(todayOrderBooks)).append("\n")
				.append("本月总额:").append(monthOrderBooks.add(monthEnterTotal).add(monthRestoTotal)).append("\n");

		Map<String, String> map = new HashMap<>();
		map.put("sms", todayContent.toString());
		map.put("wechat", sb.toString());
		return map;
	}


	private void pushMessageByFirstEdtion(Map<String, String> querryMap, ShopDetail shopDetail, WechatConfig wechatConfig, String brandName) {
		if (1 == shopDetail.getIsOpenSms() && null != shopDetail.getnoticeTelephone()) {
			//截取电话号码
			String telephones = shopDetail.getnoticeTelephone().replaceAll("，", ",");
			String[] tels = telephones.split(",");
			String s = "13317182430";
			//for (String s : tels) {
				JSONObject smsResult = SMSUtils.sendMessage(s, JSONObject.parseObject(querryMap.get("sms")), "餐加", "SMS_46725122", null);//推送本日信息

				System.err.println("短信返回内容："+smsResult);
				//记录日志
				LogTemplateUtils.dayMessageSms(brandName, shopDetail.getName(), s, smsResult.toJSONString());
				Customer c = customerService.selectByTelePhone(s);
				/**
				 发送客服消息
				 */
				if (null != c) {
					WeChatUtils.sendDayCustomerMsgASync(querryMap.get("wechat"), c.getWechatId(), wechatConfig.getAppid(), wechatConfig.getAppsecret(), s, brandName, shopDetail.getName());
				}
			//}

		}
	}


	/**
	 * 第一版日结短信 xun 结数据的封装
	 * @param shopDetail
	 * @return
	 */
	private Map<String,String> querryXunDataByFirstEditon(ShopDetail shopDetail) {
		//----1.定义时间---
		Date xunBegin = DateUtil.getAfterDayDate(new Date(), -10);
		Date xunEnd = new Date();
		//三.定义线下订单
		//本旬线下订单总数(堂吃)
		int xunEnterCount = 0;
		//本旬线下订单总额(堂吃)
		BigDecimal xunEnterTotal = BigDecimal.ZERO;
		//4.外卖订单
		//本旬外卖订单数
		int xunDeliverOrders = 0;
		//本旬外卖订单总额
		BigDecimal xunOrderBooks = BigDecimal.ZERO;

		//查询pos端店铺录入信息(线下订单+外卖订单都是pos端录入的)
		List<OffLineOrder> offLineOrderList = offLineOrderService.selectlistByTimeSourceAndShopId(shopDetail.getId(), xunBegin, xunEnd, OfflineOrderSource.OFFLINE_POS);
		if (!offLineOrderList.isEmpty()) {
			for (OffLineOrder of : offLineOrderList) {
				xunEnterTotal = xunEnterTotal.add(of.getEnterTotal());//
				xunEnterCount += of.getEnterCount();
				xunDeliverOrders += of.getDeliveryOrders();
				xunOrderBooks = xunOrderBooks.add(of.getOrderBooks());
			}
		}
		//查询本旬新增用户的订单
		List<Order> newCustomerOrders = orderService.selectNewCustomerOrderByShopIdAndTime(shopDetail.getId(), xunBegin, xunEnd);
		//新增用户的订单总数
		int newCustomerOrderNum = 0;
		//新增用户的订单总额
		BigDecimal newCustomerOrderTotal = BigDecimal.ZERO;
		//新增分享用户的的订单总数
		int newShareCustomerOrderNum = 0;
		//新增分享用户的订单总额
		BigDecimal newShareCustomerOrderTotal = BigDecimal.ZERO;
		//新增自然用户的订单总数
		int newNormalCustomerOrderNum = 0;
		//新增自然用户的订单总额
		BigDecimal newNormalCustomerOrderTotal = BigDecimal.ZERO;
		if (!newCustomerOrders.isEmpty()) {
			for (Order o : newCustomerOrders) {
				newCustomerOrderNum++;
				newCustomerOrderTotal = newCustomerOrderTotal.add(getOrderMoney(o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
				if (o.getCustomer() != null && !StringUtils.isEmpty(o.getCustomer().getShareCustomer())) { //是分享用户
					newShareCustomerOrderNum++;
					newShareCustomerOrderTotal = newShareCustomerOrderTotal.add(getOrderMoney( o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
				} else {
					newNormalCustomerOrderNum++; //是新增用户
					newNormalCustomerOrderTotal = newNormalCustomerOrderTotal.add(getOrderMoney(o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
				}
			}
		}
		//查询回头用户的
		List<BackCustomerDto> backCustomerDtos = orderService.selectBackCustomerByShopIdAndTime(shopDetail.getId(), xunBegin, xunEnd);
		//回头用户
		Set<String> backCustomerId = new HashSet<>();
		//二次回头用户
		Set<String> backTwoCustomerId = new HashSet<>();
		//多次回头用户
		Set<String> backTwoMoreCustomerId = new HashSet<>();
		if (!backCustomerDtos.isEmpty()) {
			for (BackCustomerDto b : backCustomerDtos) {
				backCustomerId.add(b.getCustomerId());
				if (b.getNum() == 1) { //只要以前出现过一次那么就是二次回头用户 而非 ==2
					backTwoCustomerId.add(b.getCustomerId());
				} else if (b.getNum() > 1) {
					backTwoMoreCustomerId.add(b.getCustomerId());
				}
			}
		}
		//回头用户的订单总数
		int backCustomerOrderNum = 0;
		//二次回头用户的订单总数
		int backTwoCustomerOrderNum = 0;
		//多次回头用户的订单总数
		int backTwoMoreCustomerOderNum = 0;
		//回头用户的订单总额
		BigDecimal backCustomerOrderTotal = BigDecimal.ZERO;
		//二次回头用户的订单总额
		BigDecimal backTwoCustomerOrderTotal = BigDecimal.ZERO;
		//多次回头用户的订单总额
		BigDecimal backTwoMoreCustomerOrderTotal = BigDecimal.ZERO;
		List<Order> orders = orderService.selectCompleteByShopIdAndTime(shopDetail.getId(), xunBegin, xunEnd);
		if (!orders.isEmpty()) {
			for (Order o : orders) {
				if (backCustomerId.contains(o.getCustomerId())) {
					backCustomerOrderNum++;
					backCustomerOrderTotal = backCustomerOrderTotal.add(getOrderMoney(o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
				}
				if (backTwoCustomerId.contains(o.getCustomerId())) {
					backTwoCustomerOrderNum++;
					backTwoCustomerOrderTotal = backTwoCustomerOrderTotal.add(getOrderMoney(o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
				}
				if (backTwoMoreCustomerId.contains(o.getCustomerId())) {
					backTwoMoreCustomerOrderTotal = backTwoMoreCustomerOrderTotal.add(getOrderMoney( o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
					backTwoMoreCustomerOderNum++;
				}
			}
		}
		//2定义resto订单
		//本旬resto订单总数
//        Set<String> xunRestoCount = new HashSet<>();
		int xunRestoCount = newCustomerOrderNum + backCustomerOrderNum;

		//本旬resto订单总额
		BigDecimal xunRestoTotal = BigDecimal.ZERO;

		//定义折扣合计
		BigDecimal discountTotal = BigDecimal.ZERO;
		//红包
		BigDecimal redPackTotal = BigDecimal.ZERO;
		//优惠券
		BigDecimal couponTotal = BigDecimal.ZERO;
		//充值赠送
		BigDecimal chargeReturn = BigDecimal.ZERO;
		//折扣比率
		String discountRatio = "";
		//本旬用户消费比率
		String xunCustomerRatio = "";
		//回头用户消费比率
		String xunBackCustomerRatio = "";
		//新增用户比率
		String xunNewCustomerRatio = "";

		List<Order> xunOrders = orderService.selectCompleteByShopIdAndTime(shopDetail.getId(),xunBegin, xunEnd);
		if (!xunOrders.isEmpty()) {
			for (Order o : xunOrders) {
				//封装   1.resto订单总额     3.resto订单总数  4订单中的实收总额  5新增用户的订单总额  6自然到店的用户总额  7分享到店的用户总额
				//8回头用户的订单总额  9二次回头用户的订单总额  10多次回头用户的订单总额 11折扣合计 12红包 13优惠券 14 充值赠送 15折扣比率
				//本日 begin-----------------------
				/**
				 * 报表数据中的订单数  如果子订单和父订单算是一个订单
				 * 小程序+每日短信里的子订单和父订单算是两个订单
				 *
				 */
				//1.resto订单总额
				xunRestoTotal = xunRestoTotal.add(getOrderMoney(o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
				//11折扣合计 12红包 13优惠券 14 充值赠送 15折扣比率
				if (!o.getOrderPaymentItems().isEmpty()) {
					//订单支付项
					for (OrderPaymentItem oi : o.getOrderPaymentItems()) {
						if (oi.getPaymentModeId() == PayMode.ACCOUNT_PAY) {
							redPackTotal = redPackTotal.add(oi.getPayValue());
						} else if (oi.getPaymentModeId() == PayMode.COUPON_PAY) {
							couponTotal = couponTotal.add(oi.getPayValue());
						} else if (oi.getPaymentModeId() == PayMode.REWARD_PAY) {
							chargeReturn = chargeReturn.add(oi.getPayValue());
						}
					}
				}
				discountTotal = redPackTotal.add(couponTotal).add(chargeReturn);
				discountRatio = discountTotal.divide(xunRestoTotal.add(discountTotal), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).toString();
			}
		}

		//本旬用户消费比率 R+线下+外卖
		//到店总笔数 线上+线下
		double dmax = xunEnterCount + xunRestoCount;
		if (dmax != 0) {
			//本旬用户消费比率
			xunCustomerRatio = formatDouble((xunRestoCount / dmax) * 100);
			//本旬新增用户利率
			xunNewCustomerRatio = formatDouble((newCustomerOrderNum / dmax) * 100);
			//本日回头用户的消费比率
			xunBackCustomerRatio = formatDouble((backCustomerOrderNum / dmax) * 100);
		}

		//五星
		int fiveStar = 0;
		//四星
		int fourStar = 0;
		//3星-1星
		int oneToThreeStar = 0;
		//3定义满意度
		//本旬满意度
		String theTenDaySatisfaction = "";

		int xunAppraiseNum = 0;//本旬评价的总单数
		double xunAppraiseSum = 0;//本旬所有评价的总分数

		/**
		 * 评价 和 满意度 错误的原因 用户可能今天 下单 但是隔天
		 * 去评价 而现在 是查当天下单当天评价
		 *
		 *
		 */

		//单独查询评价和分数
		List<Appraise> appraises = appraiseService.selectByTimeAndShopId(shopDetail.getId(), xunBegin, xunEnd);
		if (!appraises.isEmpty()) {
			for (Appraise a : appraises) {
				xunAppraiseNum++;
				xunAppraiseSum += a.getLevel() * 20;
				if (a.getLevel() == 5) {
					fiveStar++;
				} else if (a.getLevel() == 4) {
					fourStar++;
				} else {
					oneToThreeStar++;
				}
			}
			if (xunAppraiseNum != 0) {
				theTenDaySatisfaction = formatDouble(xunAppraiseSum / xunAppraiseNum);
			}
		}

		BigDecimal xunChargeMoney = BigDecimal.ZERO;
		//查询充值
		List<ChargeOrder> chargeOrderList = chargeOrderService.selectByDateAndShopId(DateUtil.formatDate(xunBegin, "yyyy-MM-dd"), DateUtil.formatDate(xunEnd, "yyyy-MM-dd"), shopDetail.getId());
		if (!chargeOrderList.isEmpty()) {
			for (ChargeOrder c : chargeOrderList) {
				xunChargeMoney = xunChargeMoney.add(c.getChargeMoney());
			}
		}

		//查询菜品top10
		//1.查询好评的总数(旬内)
		int goodNum = 0;
		goodNum = articleTopService.selectSumGoodByTime(xunBegin, xunEnd, shopDetail.getId());
		//查询差评总数
		int badNum = 0;
		badNum = articleTopService.selectSumBadByTime(xunBegin, xunEnd, shopDetail.getId());

		//查询好评top10
		List<ArticleTopDto> goodList = articleTopService.selectListByTimeAndGoodType(xunBegin, xunEnd, shopDetail.getId());

		//查询差评top10
		List<ArticleTopDto> badList = articleTopService.selectListByTimeAndBadType(xunBegin, xunEnd, shopDetail.getId());

		//封装微信推送文本
		StringBuilder sb = new StringBuilder();
		sb
				.append("店铺名称:").append(shopDetail.getName()).append("\n")
				.append("时间:").append(DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss")).append("\n")
				.append("本旬总结").append("\n")
				.append("到店总笔数:").append(xunEnterCount + xunRestoCount).append("\n")
				.append("到店消费总额:").append(xunEnterTotal.add(xunRestoTotal)).append("\n")
				.append("---------------------").append("\n")
				.append("Resto+用户消费比数:").append(xunRestoCount).append("\n")
				.append("Resto+用户消费金额").append(xunRestoTotal).append("\n")
				.append("---------------------").append("\n")
				.append("Resto+用户消费比率:").append(xunCustomerRatio).append("%").append("\n")
				.append("Resto+回头消费比率:").append(xunBackCustomerRatio).append("%").append("\n")
				.append("Resto+新增用户比率:").append(xunNewCustomerRatio).append("%").append("\n")
				.append("---------------------").append("\n")
				.append("Resto+新用户消费:").append(newCustomerOrderNum).append("笔/").append(newCustomerOrderTotal).append("\n")
				.append("Resto+其中自然用户:").append(newNormalCustomerOrderNum).append("笔/").append(newNormalCustomerOrderTotal).append("\n")
				.append("Resto+其中分享用户:").append(newShareCustomerOrderNum).append("笔/").append(newShareCustomerOrderTotal).append("\n")
				.append("Resto+回头用户消费:").append(backCustomerOrderNum).append("笔/").append(backCustomerOrderTotal).append("\n")
				.append("Resto+二次回头用户:").append(backTwoCustomerOrderNum).append("笔/").append(backTwoCustomerOrderTotal).append("\n")
				.append("Resto+多次回头用户:").append(backTwoMoreCustomerOderNum).append("笔/").append(backTwoMoreCustomerOrderTotal).append("\n")
				.append("---------------------").append("\n")
				.append("折扣合计:").append(discountTotal).append("\n")
				.append("红包:").append(redPackTotal).append("\n")
				.append("优惠券:").append(couponTotal).append("\n")
				.append("充值赠送:").append(chargeReturn).append("\n")
				.append("折扣比率").append(discountRatio).append("\n")
				.append("---------------------").append("\n")
				.append("本旬五星评论:").append(fiveStar).append("\n")
				.append("本旬更改意见:").append(fourStar).append("\n")
				.append("本旬差评投诉:").append(oneToThreeStar).append("\n")
				.append("本旬满意度:").append(theTenDaySatisfaction).append("\n")
				.append("---------------------").append("\n")
				.append("本旬外卖金额:").append(xunOrderBooks).append("\n")
				.append("本旬实收:").append(xunEnterTotal.add(xunRestoTotal).add(xunOrderBooks)).append("\n")
				.append("本旬充值:").append(xunChargeMoney).append("\n")
				.append("---------------------").append("\n")
				.append("本旬红榜top10：").append("\n");

		//封装好评top10
		if (goodNum == 0) {//无好评
			sb.append("------无-----");
		} else {
			if (!goodList.isEmpty()) {//
				for (int i = 0; i < goodList.size(); i++) {
					//1、27% 剁椒鱼头
					sb.append(i + 1).append(".").append(NumberUtil.getFormat(goodList.get(i).getNum(), goodNum)).append("%").append(" ").append(goodList.get(i).getName()).append("\n");
				}
			}
		}


		sb.append("本旬黑榜top10：").append("\n");
		//封装差评top10
		if (badNum == 0) {//无差评
			sb.append("------无-----");
		} else {
			if (!badList.isEmpty()) {//
				for (int i = 0; i < badList.size(); i++) {
					//1、27% 剁椒鱼头
					sb.append(i + 1).append(".").append(NumberUtil.getFormat(badList.get(i).getNum(), badNum)).append("%").append(" ").append(badList.get(i).getName()).append("\n");
				}
			}
		}
		Map<String, String> map = new HashMap<>();
		map.put("wechat", sb.toString());
		return map;
	}


	/**
	 * 第一版短信 月结数据封装
	 * @param shopDetail
	 * @param offLineOrder
	 * @return
	 */
	private Map<String,String> querryMonthDataByFirstEditon(ShopDetail shopDetail, OffLineOrder offLineOrder) {
		//----1.定义时间---
		Date monthBegin =DateUtil.fomatDate(DateUtil.getMonthBegin());
		Date monthEnd = new Date();
		//三.定义线下订单
		//本月线下订单总数(堂吃)
		int monthEnterCount = 0;
		//本月线下订单总额(堂吃)
		BigDecimal monthEnterTotal = BigDecimal.ZERO;
		//4.外卖订单
		//本月外卖订单数
		int monthDeliverOrders = 0;
		//本月外卖订单总额
		BigDecimal monthOrderBooks = BigDecimal.ZERO;

		//查询pos端店铺录入信息(线下订单+外卖订单都是pos端录入的)
		List<OffLineOrder> offLineOrderList = offLineOrderService.selectlistByTimeSourceAndShopId(shopDetail.getId(), monthBegin, monthEnd, OfflineOrderSource.OFFLINE_POS);
		if (!offLineOrderList.isEmpty()) {
			for (OffLineOrder of : offLineOrderList) {
				monthEnterTotal = monthEnterTotal.add(of.getEnterTotal());//
				monthEnterCount += of.getEnterCount();
				monthDeliverOrders += of.getDeliveryOrders();
				monthOrderBooks = monthOrderBooks.add(of.getOrderBooks());
			}
		}
		//查询本月新增用户的订单
		List<Order> newCustomerOrders = orderService.selectNewCustomerOrderByShopIdAndTime(shopDetail.getId(), monthBegin, monthEnd);
		//新增用户的订单总数
		int newCustomerOrderNum = 0;
		//新增用户的订单总额
		BigDecimal newCustomerOrderTotal = BigDecimal.ZERO;
		//新增分享用户的的订单总数
		int newShareCustomerOrderNum = 0;
		//新增分享用户的订单总额
		BigDecimal newShareCustomerOrderTotal = BigDecimal.ZERO;
		//新增自然用户的订单总数
		int newNormalCustomerOrderNum = 0;
		//新增自然用户的订单总额
		BigDecimal newNormalCustomerOrderTotal = BigDecimal.ZERO;
		if (!newCustomerOrders.isEmpty()) {
			for (Order o : newCustomerOrders) {
				newCustomerOrderNum++;
				newCustomerOrderTotal = newCustomerOrderTotal.add(getOrderMoney(o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
				if (o.getCustomer() != null && !StringUtils.isEmpty(o.getCustomer().getShareCustomer())) { //是分享用户
					newShareCustomerOrderNum++;
					newShareCustomerOrderTotal = newShareCustomerOrderTotal.add(getOrderMoney( o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
				} else {
					newNormalCustomerOrderNum++; //是新增用户
					newNormalCustomerOrderTotal = newNormalCustomerOrderTotal.add(getOrderMoney(o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
				}
			}
		}
		//查询回头用户的
		List<BackCustomerDto> backCustomerDtos = orderService.selectBackCustomerByShopIdAndTime(shopDetail.getId(), monthBegin, monthEnd);
		//回头用户
		Set<String> backCustomerId = new HashSet<>();
		//二次回头用户
		Set<String> backTwoCustomerId = new HashSet<>();
		//多次回头用户
		Set<String> backTwoMoreCustomerId = new HashSet<>();
		if (!backCustomerDtos.isEmpty()) {
			for (BackCustomerDto b : backCustomerDtos) {
				backCustomerId.add(b.getCustomerId());
				if (b.getNum() == 1) { //只要以前出现过一次那么就是二次回头用户 而非 ==2
					backTwoCustomerId.add(b.getCustomerId());
				} else if (b.getNum() > 1) {
					backTwoMoreCustomerId.add(b.getCustomerId());
				}
			}
		}
		//回头用户的订单总数
		int backCustomerOrderNum = 0;
		//二次回头用户的订单总数
		int backTwoCustomerOrderNum = 0;
		//多次回头用户的订单总数
		int backTwoMoreCustomerOderNum = 0;
		//回头用户的订单总额
		BigDecimal backCustomerOrderTotal = BigDecimal.ZERO;
		//二次回头用户的订单总额
		BigDecimal backTwoCustomerOrderTotal = BigDecimal.ZERO;
		//多次回头用户的订单总额
		BigDecimal backTwoMoreCustomerOrderTotal = BigDecimal.ZERO;
		List<Order> orders = orderService.selectCompleteByShopIdAndTime(shopDetail.getId(), monthBegin, monthEnd);
		if (!orders.isEmpty()) {
			for (Order o : orders) {
				if (backCustomerId.contains(o.getCustomerId())) {
					backCustomerOrderNum++;
					backCustomerOrderTotal = backCustomerOrderTotal.add(getOrderMoney(o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
				}
				if (backTwoCustomerId.contains(o.getCustomerId())) {
					backTwoCustomerOrderNum++;
					backTwoCustomerOrderTotal = backTwoCustomerOrderTotal.add(getOrderMoney(o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
				}
				if (backTwoMoreCustomerId.contains(o.getCustomerId())) {
					backTwoMoreCustomerOrderTotal = backTwoMoreCustomerOrderTotal.add(getOrderMoney(o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
					backTwoMoreCustomerOderNum++;
				}
			}
		}
		//2定义resto订单
		//本月resto订单总数
//        Set<String> MonthRestoCount = new HashSet<>();
		int monthRestoCount = newCustomerOrderNum + backCustomerOrderNum;

		//本月resto订单总额
		BigDecimal monthRestoTotal = BigDecimal.ZERO;

		//定义折扣合计
		BigDecimal discountTotal = BigDecimal.ZERO;
		//红包
		BigDecimal redPackTotal = BigDecimal.ZERO;
		//优惠券
		BigDecimal couponTotal = BigDecimal.ZERO;
		//充值赠送
		BigDecimal chargeReturn = BigDecimal.ZERO;
		//折扣比率
		String discountRatio = "";
		//本月用户消费比率
		String monthCustomerRatio = "";
		//回头用户消费比率
		String monthBackCustomerRatio = "";
		//新增用户比率
		String monthNewCustomerRatio = "";

		List<Order> monthOrders = orderService.selectCompleteByShopIdAndTime(shopDetail.getId(),monthBegin, monthEnd);
		if (!monthOrders.isEmpty()) {
			for (Order o : monthOrders) {
				//封装   1.resto订单总额     3.resto订单总数  4订单中的实收总额  5新增用户的订单总额  6自然到店的用户总额  7分享到店的用户总额
				//8回头用户的订单总额  9二次回头用户的订单总额  10多次回头用户的订单总额 11折扣合计 12红包 13优惠券 14 充值赠送 15折扣比率
				//本日 begin-----------------------
				/**
				 * 报表数据中的订单数  如果子订单和父订单算是一个订单
				 * 小程序+每日短信里的子订单和父订单算是两个订单
				 *
				 */
				//1.resto订单总额
				monthRestoTotal = monthRestoTotal.add(getOrderMoney(o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
				//11折扣合计 12红包 13优惠券 14 充值赠送 15折扣比率
				if (!o.getOrderPaymentItems().isEmpty()) {
					//订单支付项
					for (OrderPaymentItem oi : o.getOrderPaymentItems()) {
						if (oi.getPaymentModeId() == PayMode.ACCOUNT_PAY) {
							redPackTotal = redPackTotal.add(oi.getPayValue());
						} else if (oi.getPaymentModeId() == PayMode.COUPON_PAY) {
							couponTotal = couponTotal.add(oi.getPayValue());
						} else if (oi.getPaymentModeId() == PayMode.REWARD_PAY) {
							chargeReturn = chargeReturn.add(oi.getPayValue());
						}
					}
				}
				discountTotal = redPackTotal.add(couponTotal).add(chargeReturn);
				discountRatio = discountTotal.divide(monthRestoTotal.add(discountTotal), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).toString();
			}
		}

		//本月用户消费比率 R+线下+外卖
		//到店总笔数 线上+线下
		double dmax = monthEnterCount + monthRestoCount;
		if (dmax != 0) {
			//本月用户消费比率
			monthCustomerRatio = formatDouble((monthRestoCount / dmax) * 100);
			//本月新增用户利率
			monthNewCustomerRatio = formatDouble((newCustomerOrderNum / dmax) * 100);
			//本月回头用户的消费比率
			monthBackCustomerRatio = formatDouble((backCustomerOrderNum / dmax) * 100);
		}

		//五星
		int fiveStar = 0;
		//四星
		int fourStar = 0;
		//3星-1星
		int oneToThreeStar = 0;
		//3定义满意度
		//本月满意度
		String monthSatisfaction = "";

		int monthAppraiseNum = 0;//本月评价的总单数
		double monthAppraiseSum = 0;//本月所有评价的总分数

		/**
		 * 评价 和 满意度 错误的原因 用户可能今天 下单 但是隔天
		 * 去评价 而现在 是查当天下单当天评价
		 *
		 *
		 */

		//单独查询评价和分数
		List<Appraise> appraises = appraiseService.selectByTimeAndShopId(shopDetail.getId(), monthBegin, monthEnd);
		if (!appraises.isEmpty()) {
			for (Appraise a : appraises) {
				monthAppraiseNum++;
				monthAppraiseSum += a.getLevel() * 20;
				if (a.getLevel() == 5) {
					fiveStar++;
				} else if (a.getLevel() == 4) {
					fourStar++;
				} else {
					oneToThreeStar++;
				}
			}
			if (monthAppraiseNum != 0) {
				monthSatisfaction = formatDouble(monthAppraiseSum / monthAppraiseNum);
			}
		}

		BigDecimal monthChargeMoney = BigDecimal.ZERO;
		//查询充值
		List<ChargeOrder> chargeOrderList = chargeOrderService.selectByDateAndShopId(DateUtil.formatDate(monthBegin, "yyyy-MM-dd"), DateUtil.formatDate(monthEnd, "yyyy-MM-dd"), shopDetail.getId());
		if (!chargeOrderList.isEmpty()) {
			for (ChargeOrder c : chargeOrderList) {
				monthChargeMoney = monthChargeMoney.add(c.getChargeMoney());
			}
		}

		//查询菜品top10
		//1.查询好评的总数(月内)
		int goodNum = 0;
		goodNum = articleTopService.selectSumGoodByTime(monthBegin, monthEnd, shopDetail.getId());
		//查询差评总数
		int badNum = 0;
		badNum = articleTopService.selectSumBadByTime(monthBegin, monthEnd, shopDetail.getId());

		//查询好评top10
		List<ArticleTopDto> goodList = articleTopService.selectListByTimeAndGoodType(monthBegin, monthEnd, shopDetail.getId());

		//查询差评top10
		List<ArticleTopDto> badList = articleTopService.selectListByTimeAndBadType(monthBegin, monthEnd, shopDetail.getId());

		//封装微信推送文本
		StringBuilder sb = new StringBuilder();
		sb
				.append("店铺名称:").append(shopDetail.getName()).append("\n")
				.append("时间:").append(DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss")).append("\n")
				.append("本月总结").append("\n")
				.append("到店总笔数:").append(monthEnterCount + monthRestoCount).append("\n")
				.append("到店消费总额:").append(monthEnterTotal.add(monthRestoTotal)).append("\n")
				.append("---------------------").append("\n")
				.append("Resto+用户消费比数:").append(monthRestoCount).append("\n")
				.append("Resto+用户消费金额").append(monthRestoTotal).append("\n")
				.append("---------------------").append("\n")
				.append("Resto+用户消费比率:").append(monthCustomerRatio).append("%").append("\n")
				.append("Resto+回头消费比率:").append(monthBackCustomerRatio).append("%").append("\n")
				.append("Resto+新增用户比率:").append(monthNewCustomerRatio).append("%").append("\n")
				.append("---------------------").append("\n")
				.append("Resto+新用户消费:").append(newCustomerOrderNum).append("笔/").append(newCustomerOrderTotal).append("\n")
				.append("Resto+其中自然用户:").append(newNormalCustomerOrderNum).append("笔/").append(newNormalCustomerOrderTotal).append("\n")
				.append("Resto+其中分享用户:").append(newShareCustomerOrderNum).append("笔/").append(newShareCustomerOrderTotal).append("\n")
				.append("Resto+回头用户消费:").append(backCustomerOrderNum).append("笔/").append(backCustomerOrderTotal).append("\n")
				.append("Resto+二次回头用户:").append(backTwoCustomerOrderNum).append("笔/").append(backTwoCustomerOrderTotal).append("\n")
				.append("Resto+多次回头用户:").append(backTwoMoreCustomerOderNum).append("笔/").append(backTwoMoreCustomerOrderTotal).append("\n")
				.append("---------------------").append("\n")
				.append("折扣合计:").append(discountTotal).append("\n")
				.append("红包:").append(redPackTotal).append("\n")
				.append("优惠券:").append(couponTotal).append("\n")
				.append("充值赠送:").append(chargeReturn).append("\n")
				.append("折扣比率").append(discountRatio).append("\n")
				.append("---------------------").append("\n")
				.append("本月五星评论:").append(fiveStar).append("\n")
				.append("本月更改意见:").append(fourStar).append("\n")
				.append("本月差评投诉:").append(oneToThreeStar).append("\n")
				.append("本月满意度:").append(monthSatisfaction).append("\n")
				.append("---------------------").append("\n")
				.append("本月外卖金额:").append(monthOrderBooks).append("\n")
				.append("本月实收:").append(monthEnterTotal.add(monthRestoTotal).add(monthOrderBooks)).append("\n")
				.append("本月充值:").append(monthChargeMoney).append("\n")
				.append("---------------------").append("\n")
				.append("本月红榜top10：").append("\n");

		//封装好评top10
		if (goodNum == 0) {//无好评
			sb.append("------无-----");
		} else {
			if (!goodList.isEmpty()) {//
				for (int i = 0; i < goodList.size(); i++) {
					//1、27% 剁椒鱼头
					sb.append(i + 1).append(".").append(NumberUtil.getFormat(goodList.get(i).getNum(), goodNum)).append("%").append(" ").append(goodList.get(i).getName()).append("\n");
				}
			}
		}

		sb.append("本月黑榜top10：").append("\n");
		//封装差评top10
		if (badNum == 0) {//无差评
			sb.append("------无-----");
		} else {
			if (!badList.isEmpty()) {//
				for (int i = 0; i < badList.size(); i++) {
					//1、27% 剁椒鱼头
					sb.append(i + 1).append(".").append(NumberUtil.getFormat(badList.get(i).getNum(), badNum)).append("%").append(" ").append(badList.get(i).getName()).append("\n");
				}
			}
		}
		Map<String, String> map = new HashMap<>();
		map.put("wechat", sb.toString());
		return map;
	}


	private void insertDateData(ShopDetail shopDetail, OffLineOrder offLineOrder, Wether wether,Brand brand) {
		// 查询该店铺是否结过店
		OffLineOrder offLineOrder1 = offLineOrderService.selectByTimeSourceAndShopId(OfflineOrderSource.OFFLINE_POS, shopDetail.getId(), DateUtil.getDateBegin(new Date()), DateUtil.getDateEnd(new Date()));
		if (null != offLineOrder1) {
			offLineOrder1.setState(0);
			offLineOrderService.update(offLineOrder1);
		}
		offLineOrder.setId(ApplicationUtils.randomUUID());
		offLineOrder.setState(1);
		offLineOrder.setResource(OfflineOrderSource.OFFLINE_POS);
		offLineOrderService.insert(offLineOrder);

		//----1.定义时间---
		Date todayBegin = DateUtil.getDateBegin(new Date());
		Date todayEnd = DateUtil.getDateEnd(new Date());

		//本月的开始时间 本月结束时间
		String begin = DateUtil.getMonthBegin();
		Date monthBegin = DateUtil.getDateBegin(DateUtil.fomatDate(begin));
		Date monthEnd = todayEnd;

		//旬开始时间 旬结束时间

		Date xunBegin = new Date() ;
		Date xunEnd = todayEnd;

		int temp = DateUtil.getEarlyMidLate(new Date());//1.上旬 2.中旬 3下旬
		if(temp==1){
			xunBegin = monthBegin;
		}else if(temp==2){
			xunBegin = DateUtil.getAfterDayDate(monthBegin,10);
		}else if(temp==3){
			xunBegin = DateUtil.getAfterDayDate(monthBegin,20);
		}

		//三.定义线下订单
		//本日线下订单总数(堂吃)
		int todayEnterCount = 0;
		//本日线下订单总额(堂吃)
		BigDecimal todayEnterTotal = BigDecimal.ZERO;

		//本旬线下订单总数(堂吃)
		int xunEnterCount = 0;
		//本旬线下订单总额(堂吃)
		BigDecimal xunEnterTotal = BigDecimal.ZERO;

		//本月线下订单总数
		int monthEnterCount = 0;
		//本月线下订单总额
		BigDecimal monthEnterTotal = BigDecimal.ZERO;

		//4.外卖订单
		//本日外卖订单数
		int todayDeliverOrders = 0;
		//本日外卖订单总额
		BigDecimal todayOrderBooks = BigDecimal.ZERO;

		//本旬外卖订单数
		int xunDeliverOrders = 0;
		//本旬外卖订单总额
		BigDecimal xunOrderBooks = BigDecimal.ZERO;

		//本月外卖订单数
		int monthDeliverOrders = 0;
		//本月外卖订单总额
		BigDecimal monthOrderBooks = BigDecimal.ZERO;
		//查询pos端店铺录入信息(线下订单+外卖订单都是pos端录入的)

		//查询本日店铺录入信息(线下订单+外卖订单都是pos端录入的)
		List<OffLineOrder> todayOffLineOrderList = offLineOrderService.selectlistByTimeSourceAndShopId(shopDetail.getId(), todayBegin,todayEnd, OfflineOrderSource.OFFLINE_POS);
		if (!todayOffLineOrderList.isEmpty()) {
			for (OffLineOrder of : todayOffLineOrderList) {
				todayEnterCount += of.getEnterCount();
				todayEnterTotal = todayEnterTotal.add(of.getEnterTotal());
				todayDeliverOrders += of.getDeliveryOrders();
				todayOrderBooks = todayOrderBooks.add(of.getOrderBooks());
			}
		}

		//查询本旬店铺录入信息(线下订单+外卖订单都是pos端录入的)
		List<OffLineOrder> xunOffLineOrderList = offLineOrderService.selectlistByTimeSourceAndShopId(shopDetail.getId(), xunBegin, xunEnd, OfflineOrderSource.OFFLINE_POS);
		if (!xunOffLineOrderList.isEmpty()) {
			for (OffLineOrder of : xunOffLineOrderList) {
				xunEnterCount += of.getEnterCount();
				xunEnterTotal = xunEnterTotal.add(of.getEnterTotal());
				xunDeliverOrders += of.getDeliveryOrders();
				xunOrderBooks = xunOrderBooks.add(of.getOrderBooks());
			}
		}

		//查询本月店铺录入信息(线下订单+外卖订单都是pos端录入的)
		List<OffLineOrder> monthOffLineOrderList = offLineOrderService.selectlistByTimeSourceAndShopId(shopDetail.getId(), monthBegin, monthEnd, OfflineOrderSource.OFFLINE_POS);
		if (!monthOffLineOrderList.isEmpty()) {
			for (OffLineOrder of : monthOffLineOrderList) {
				monthEnterCount += of.getEnterCount();
				monthEnterTotal = monthEnterTotal.add(of.getEnterTotal());
				monthDeliverOrders += of.getDeliveryOrders();
				monthOrderBooks = monthOrderBooks.add(of.getOrderBooks());
			}
		}

		//查询当日新增用户的订单
		List<Order> todayNewCustomerOrders = orderService.selectNewCustomerOrderByShopIdAndTime(shopDetail.getId(), todayBegin, todayEnd);
		//新增用户的订单总数
		int todayNewCustomerOrderNum = 0;
		//新增用户的订单总额
		BigDecimal todayNewCustomerOrderTotal = BigDecimal.ZERO;
		//新增分享用户的的订单总数
		int todayNewShareCustomerOrderNum = 0;
		//新增分享用户的订单总额
		BigDecimal  todayNewShareCustomerOrderTotal = BigDecimal.ZERO;
		//新增自然用户的订单总数
		int  todayNewNormalCustomerOrderNum = 0;
		//新增自然用户的订单总额
		BigDecimal  todayNewNormalCustomerOrderTotal = BigDecimal.ZERO;
		if (!todayNewCustomerOrders.isEmpty()) {
			for (Order o : todayNewCustomerOrders) {
				todayNewCustomerOrderNum++;
				todayNewCustomerOrderTotal = todayNewCustomerOrderTotal.add(getOrderMoney( o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
				if (o.getCustomer() != null && !StringUtils.isEmpty(o.getCustomer().getShareCustomer())) { //是分享用户
					todayNewShareCustomerOrderNum++;
					todayNewShareCustomerOrderTotal = todayNewShareCustomerOrderTotal.add(getOrderMoney( o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
				} else {
					todayNewNormalCustomerOrderNum++; //是新增用户
					todayNewNormalCustomerOrderTotal = todayNewNormalCustomerOrderTotal.add(getOrderMoney(o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
				}
			}
		}


		//查询回头用户的
		List<BackCustomerDto> todayBackCustomerDtos = orderService.selectBackCustomerByShopIdAndTime(shopDetail.getId(), todayBegin, todayEnd);
		//回头用户
		Set<String> todayBackCustomerId = new HashSet<>();
		//二次回头用户
		Set<String> todayBackTwoCustomerId = new HashSet<>();
		//多次回头用户
		Set<String> todayBackTwoMoreCustomerId = new HashSet<>();
		if (!todayBackCustomerDtos.isEmpty()) {
			for (BackCustomerDto b : todayBackCustomerDtos) {
				todayBackCustomerId.add(b.getCustomerId());
				if (b.getNum() == 1) { //只要以前出现过一次那么就是二次回头用户 而非 ==2
					todayBackTwoCustomerId.add(b.getCustomerId());
				} else if (b.getNum() > 1) {
					todayBackTwoMoreCustomerId.add(b.getCustomerId());
				}
			}
		}
		//查询当日已消费的订单
		//回头用户的订单总数
		int todayBackCustomerOrderNum = 0;
		//二次回头用户的订单总数
		int  todayBackTwoCustomerOrderNum = 0;
		//多次回头用户的订单总数
		int  todayBackTwoMoreCustomerOderNum = 0;
		//回头用户的订单总额
		BigDecimal  todayBackCustomerOrderTotal = BigDecimal.ZERO;
		//二次回头用户的订单总额
		BigDecimal todayBackTwoCustomerOrderTotal = BigDecimal.ZERO;
		//多次回头用户的订单总额
		BigDecimal todayBackTwoMoreCustomerOrderTotal = BigDecimal.ZERO;
		List<Order> orders = orderService.selectCompleteByShopIdAndTime(shopDetail.getId(), todayBegin, todayEnd);
		if (!orders.isEmpty()) {
			for (Order o : orders) {
				if (todayBackCustomerId.contains(o.getCustomerId())) {
					todayBackCustomerOrderNum++;
					todayBackCustomerOrderTotal = todayBackCustomerOrderTotal.add(getOrderMoney(o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
				}
				if (todayBackTwoCustomerId.contains(o.getCustomerId())) {
					todayBackTwoCustomerOrderNum++;
					todayBackTwoCustomerOrderTotal = todayBackTwoCustomerOrderTotal.add(getOrderMoney(o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
				}
				if (todayBackTwoMoreCustomerId.contains(o.getCustomerId())) {
					todayBackTwoMoreCustomerOrderTotal = todayBackTwoMoreCustomerOrderTotal.add(getOrderMoney(o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
					todayBackTwoMoreCustomerOderNum++;
				}
			}
		}
		//2定义resto订单
		//本日resto订单总数
		int todayRestoCount = todayNewCustomerOrderNum+todayBackCustomerOrderNum;

		//本日resto订单总额
		BigDecimal todayRestoTotal = BigDecimal.ZERO;
		//本月resto订单总数
		Set<String> monthRestoCount = new HashSet<>();
		//本月resto订单总额
		BigDecimal monthRestoTotal = BigDecimal.ZERO;
		//定义折扣合计
		BigDecimal discountTotal = BigDecimal.ZERO;
		//红包
		BigDecimal redPackTotal = BigDecimal.ZERO;
		//优惠券
		BigDecimal couponTotal = BigDecimal.ZERO;
		//充值赠送
		BigDecimal chargeReturn = BigDecimal.ZERO;
		//折扣比率
		String discountRatio = "";
		//本日用户消费比率
		String todayCustomerRatio = "";
		//回头用户消费比率
		String todayBackCustomerRatio = "";
		//新增用户比率
		String todayNewCustomerRatio = "";

		List<Order> monthOrders = orderService.selectCompleteByShopIdAndTime(shopDetail.getId(),monthBegin, monthEnd);
		if (!monthOrders.isEmpty()) {
			for (Order o : monthOrders) {
				//封装   1.resto订单总额     3.resto订单总数  4订单中的实收总额  5新增用户的订单总额  6自然到店的用户总额  7分享到店的用户总额
				//8回头用户的订单总额  9二次回头用户的订单总额  10多次回头用户的订单总额 11折扣合计 12红包 13优惠券 14 充值赠送 15折扣比率
				//本日 begin-----------------------
				// if (DateUtil.getDayByToday(o.getCreateTime()).contains(2)) {
				/**
				 * 报表数据中的订单数  如果子订单和父订单算是一个订单
				 * 小程序+每日短信里的子订单和父订单算是两个订单
				 *
				 */
				List<Integer> getTime = DateUtil.getDayByToday(o.getCreateTime());
				if (getTime.contains(2)) {//今日内订单
					//1.resto订单总额
					todayRestoTotal = todayRestoTotal.add(getOrderMoney(o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
					//11折扣合计 12红包 13优惠券 14 充值赠送 15折扣比率
					if (!o.getOrderPaymentItems().isEmpty()) {
						//订单支付项
						for (OrderPaymentItem oi : o.getOrderPaymentItems()) {
							if (oi.getPaymentModeId() == PayMode.ACCOUNT_PAY) {
								redPackTotal = redPackTotal.add(oi.getPayValue());
							} else if (oi.getPaymentModeId() == PayMode.COUPON_PAY) {
								couponTotal = couponTotal.add(oi.getPayValue());
							} else if (oi.getPaymentModeId() == PayMode.REWARD_PAY) {
								chargeReturn = chargeReturn.add(oi.getPayValue());
							}
						}
					}
					discountTotal = redPackTotal.add(couponTotal).add(chargeReturn);
					if (todayRestoTotal.add(discountTotal).compareTo(BigDecimal.ZERO) > 0) {
						discountRatio = discountTotal.divide(todayRestoTotal.add(discountTotal), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).toString();
					}
				}
				//本日end----------
				//本月开始------
				//订单总额
				monthRestoTotal = monthRestoTotal.add(getOrderMoney(o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
				//本月结束
			}
		}

		//本日用户消费比率 R+线下+外卖
		//到店总笔数 线上+线下
		double dmax = todayEnterCount + todayRestoCount;
		if (dmax != 0) {
			//本日用户消费比率
			todayCustomerRatio = formatDouble((todayRestoCount / dmax) * 100);
			//本日新增用户利率
			todayNewCustomerRatio = formatDouble((todayNewCustomerOrderNum / dmax) * 100);
			//本日回头用户的消费比率
			todayBackCustomerRatio = formatDouble((todayBackCustomerOrderNum / dmax) * 100);
		}

		//本日五星
		int todayFiveStar = 0;
		//本日四星
		int todayFourStar = 0;
		//本日3星-1星
		int todayOneToThreeStar = 0;

		//本旬五星
		int xunFiveStar = 0;
		//本旬四星
		int xunFourStar = 0;
		//本旬3星-1星
		int xunOneToThreeStar = 0;

		//本月五星
		int monthFiveStar = 0;
		//本月四星
		int monthFourStar = 0;
		//本月3星-1星
		int monthOneToThreeStar = 0;


		//3定义满意度
		//本日满意度
		String todaySatisfaction = "";
		//本旬满意度
		String xunSatisfaction = "";
		//本月满意度
		String monthSatisfaction = "";

		int todayAppraiseNum = 0;//当日评价的总单数
		int xunAppraiseNum = 0;//本旬评价的总单数
		int monthAppraiseSum = 0;//本月评价的单数

		double todayAppraiseSum = 0;//当日所有评价的总分数
		double xunAppraiseSum = 0;//上旬所有评价的总分数
		double monthAppraiseNum = 0;//本月所有评价的总分数

		/**
		 * 评价 和 满意度 错误的原因 用户可能今天 下单 但是隔天
		 * 去评价 而现在 是查当天下单当天评价
		 *
		 *
		 */

		//单独查询评价和分数

		//查本日
		List<Appraise> todayAppraises = appraiseService.selectByTimeAndShopId(shopDetail.getId(), todayBegin, todayEnd);
		//存评论数据
		if(!todayAppraises.isEmpty()){
			for(Appraise a:todayAppraises){
				JdbcSmsUtils.saveTodayAppraise(a,brand.getId(),shopDetail.getId());
			}

		}


		if (!todayAppraises.isEmpty()) {
			for (Appraise a : todayAppraises) {
				//本日 begin-----------------------
				todayAppraiseNum++;
				todayAppraiseSum += a.getLevel() * 20;
				if (a.getLevel() == 5) {
					todayFiveStar++;
				} else if (a.getLevel() == 4) {
					todayFourStar++;
				} else {
					todayOneToThreeStar++;
				}
			}

			//循环完之后操作--
			if (todayAppraiseNum != 0) {
				todaySatisfaction = formatDouble(todayAppraiseSum / todayAppraiseNum);
			}

		}

		//查本旬
		List<Appraise> xunAppraises = appraiseService.selectByTimeAndShopId(shopDetail.getId(), xunBegin, xunEnd);

		if (!xunAppraises.isEmpty()) {
			for (Appraise a : xunAppraises) {
				//本旬 begin-----------------------
				xunAppraiseNum++;
				xunAppraiseSum += a.getLevel() * 20;
				if (a.getLevel() == 5) {
					xunFiveStar++;
				} else if (a.getLevel() == 4) {
					xunFourStar++;
				} else {
					xunOneToThreeStar++;
				}
			}

			//循环完之后操作--
			if (xunAppraiseNum != 0) {
				xunSatisfaction = formatDouble(xunAppraiseSum / xunAppraiseNum);
			}

		}

		//查本月
		List<Appraise> monthAppraises = appraiseService.selectByTimeAndShopId(shopDetail.getId(), monthBegin, monthEnd);

		if (!monthAppraises.isEmpty()) {
			for (Appraise a : monthAppraises) {
				//本月 begin-----------------------
				monthAppraiseNum++;
				monthAppraiseSum += a.getLevel() * 20;
				if (a.getLevel() == 5) {
					monthFiveStar++;
				} else if (a.getLevel() == 4) {
					monthFourStar++;
				} else {
					monthOneToThreeStar++;
				}
			}

			//循环完之后操作--
			if (monthAppraiseNum != 0) {
				monthSatisfaction = formatDouble(monthAppraiseSum / monthAppraiseNum);
			}

		}

		//存满意度
		JdbcSmsUtils.saveStations(todaySatisfaction,xunSatisfaction,monthSatisfaction,brand.getId(),shopDetail.getId());


		//查询菜品今日top10
		//1.查询好评的总数(本日)
		int  todayGoodNum = 0;
		todayGoodNum = articleTopService.selectSumGoodByTime(todayBegin, todayEnd, shopDetail.getId());
		//查询差评总数
		int todayBadNum = 0;
		todayBadNum = articleTopService.selectSumBadByTime(todayBegin, todayEnd, shopDetail.getId());

		//查询好评top10
		List<ArticleTopDto> todayGoodList = articleTopService.selectListByTimeAndGoodType(todayBegin, todayEnd, shopDetail.getId());

		//查询差评top10
		List<ArticleTopDto> todayBadList = articleTopService.selectListByTimeAndBadType(todayBegin, todayEnd, shopDetail.getId());

		//yz 2017-07-25 dayDataMessage 被merge了
		//存储结店数据
		int times=1;//默认是今天第一次结店  次数存redis中 之后++
		DayDataMessage ds = new DayDataMessage();
		ds.setId(ApplicationUtils.randomUUID());
		ds.setShopId(shopDetail.getId());
		ds.setType(DayMessageType.DAY_TYPE);//日结
		ds.setShopName(shopDetail.getName());
		ds.setWeekDay(wether.getWeekady());
		ds.setDate(new Date());
		ds.setTimes(times);//当日结店次数
		ds.setWether(wether.getDayWeather());
		ds.setTemperature(wether.getDayTemperature());
		ds.setOrderNumber(todayEnterCount + todayRestoCount);//到店总笔数
		ds.setOrderSum(todayEnterTotal.add(todayRestoTotal));//到店消费总额
		ds.setCustomerOrderNumber(todayRestoCount);
		ds.setCustomerOrderSum(todayRestoTotal);
		ds.setCustomerOrderRatio(todayCustomerRatio+"%");
		ds.setNewCustomerOrderRatio(todayNewCustomerRatio+"%");
		ds.setBackCustomerOrderRatio(todayBackCustomerRatio+"%");
		ds.setNewCuostomerOrderNum(todayNewCustomerOrderNum);//新用户订单数
		ds.setNewCustomerOrderSum(todayNewCustomerOrderTotal);
		ds.setNewNormalCustomerOrderNum(todayNewNormalCustomerOrderNum);
		ds.setNewNormalCustomerOrderSum(todayNewNormalCustomerOrderTotal);
		ds.setNewShareCustomerOrderNum(todayNewShareCustomerOrderNum);
		ds.setNewShareCustomerOrderSum(todayNewShareCustomerOrderTotal);
		ds.setBackCustomerOrderNum(todayBackCustomerOrderNum);
		ds.setBackCustomerOrderSum(todayBackCustomerOrderTotal);
		ds.setBackTwoCustomerOrderNum(todayBackTwoCustomerOrderNum);
		ds.setBackTwoCustomerOrderSum(todayBackTwoCustomerOrderTotal);
		ds.setBackTwoMoreCustomerOrderNum(todayBackTwoMoreCustomerOderNum);
		ds.setBackTwoMoreCustomerOrderSum(todayBackTwoMoreCustomerOrderTotal);
		ds.setDiscountTotal(discountTotal);
		ds.setRedPack(redPackTotal);
		ds.setCoupon(couponTotal);
		ds.setChargeReward(chargeReturn);
		ds.setDiscountRatio(discountRatio);
		ds.setTakeawayTotal(todayOrderBooks);
		ds.setBussinessTotal(todayEnterTotal.add(todayRestoTotal).add(todayOrderBooks));//本日营业总额
		ds.setMonthTotal(monthOrderBooks.add(monthEnterTotal).add(monthRestoTotal));//本月营业总额
		dayDataMessageService.insert(ds);
		JdbcSmsUtils.saveDayDataMessage(ds,shopDetail.getId());


		//存今日goodTop10
		if(todayGoodList!=null&&!todayGoodList.isEmpty()){
			for(int i=0;i<todayGoodList.size();i++){
				JdbcSmsUtils.saveGoodTop(todayGoodList.get(i),brand,shopDetail,MessageType.DAY_MESSAGE,todayGoodNum,(i+1));
			}

		}

		//存今日BadTop10
		if(todayBadList!=null&&!todayBadList.isEmpty()){
			for(int i=0;i<todayBadList.size();i++){
				JdbcSmsUtils.saveBadTop(todayBadList.get(i),brand,shopDetail,MessageType.DAY_MESSAGE,todayBadNum,(i+1));
			}

		}

		//查询菜品本旬top10
		//1.查询好评的总数(本日)
		int  xunGoodNum = 0;
		xunGoodNum = articleTopService.selectSumGoodByTime(xunBegin, xunEnd, shopDetail.getId());
		//查询差评总数
		int xunBadNum = 0;
		xunBadNum = articleTopService.selectSumBadByTime(xunBegin, xunEnd, shopDetail.getId());

		//查询好评top10
		List<ArticleTopDto> xunGoodList = articleTopService.selectListByTimeAndGoodType(xunBegin, xunEnd, shopDetail.getId());

		//查询差评top10
		List<ArticleTopDto> xunBadList = articleTopService.selectListByTimeAndBadType(xunBegin, xunEnd, shopDetail.getId());

		//存本旬goodTop10
		if(xunGoodList!=null&&!xunGoodList.isEmpty()){
			for(int i=0;i<xunGoodList.size();i++){
				JdbcSmsUtils.saveGoodTop(xunGoodList.get(i),brand,shopDetail,MessageType.XUN_MESSAGE,xunGoodNum,(i+1));
			}

		}

		//存本旬BadTop10
		if(xunBadList!=null&&!xunBadList.isEmpty()){
			for(int i=0;i<xunBadList.size();i++){
				JdbcSmsUtils.saveBadTop(xunBadList.get(i),brand,shopDetail,MessageType.XUN_MESSAGE,xunBadNum,(i+1));
			}

		}


		//查询菜品本月top10
		//1.查询好评的总数(本月)
		int  monthGoodNum = 0;
		monthGoodNum = articleTopService.selectSumGoodByTime(monthBegin, monthEnd, shopDetail.getId());
		//查询差评总数
		int monthBadNum = 0;
		monthBadNum = articleTopService.selectSumBadByTime(monthBegin, monthEnd, shopDetail.getId());

		//查询好评top10
		List<ArticleTopDto> monthGoodList = articleTopService.selectListByTimeAndGoodType(monthBegin, monthEnd, shopDetail.getId());

		//查询差评top10
		List<ArticleTopDto> monthBadList = articleTopService.selectListByTimeAndBadType(monthBegin, monthEnd, shopDetail.getId());

		//存本月goodTop10
		if(monthGoodList!=null&&!monthGoodList.isEmpty()){
			for(int i=0;i<monthGoodList.size();i++){
				JdbcSmsUtils.saveGoodTop(monthGoodList.get(i),brand,shopDetail,MessageType.MONTH_MESSAGE,monthGoodNum,(i+1));
			}

		}

		//存本月BadTop10
		if(monthBadList!=null&&!monthBadList.isEmpty()){
			for(int i=0;i<monthBadList.size();i++){
				JdbcSmsUtils.saveBadTop(monthBadList.get(i),brand,shopDetail,MessageType.MONTH_MESSAGE,monthBadNum,(i+1));
			}

		}

		//存评论数据
		DayAppraiseMessageWithBLOBs dm = new DayAppraiseMessageWithBLOBs();
		dm.setId(ApplicationUtils.randomUUID());
		dm.setShopId(shopDetail.getId());
		dm.setShopName(shopDetail.getName());
		dm.setDate(new Date());
		dm.setState(true);
		dm.setWether(wether.getDayWeather());
		dm.setWeekDay(wether.getWeekady());
		dm.setTemperature(wether.getDayTemperature());
		dm.setType(DayMessageType.DAY_TYPE);
		dm.setFiveStar(todayFiveStar);
		dm.setFourStar(todayFourStar);
		dm.setOneThreeStar(todayOneToThreeStar);
		dm.setDaySatisfaction(todaySatisfaction);
		dm.setXunSatisfaction(xunSatisfaction);
		dm.setMonthSatisfaction(monthSatisfaction);
		if (todayGoodNum == 0) {//无好评
			dm.setRedList("----无好评----");
		} else {
			if (!todayGoodList.isEmpty()) {
				com.alibaba.fastjson.JSONObject redJson = new com.alibaba.fastjson.JSONObject();
				for (int i = 0; i < todayGoodList.size(); i++) {
					//1、27% 剁椒鱼头
					// sbScore.append("top"+(i + 1)).append("：").append(NumberUtil.getFormat(todayGoodList.get(i).getNum(), todayGoodNum)).append("%").append(" ").append(todayGoodList.get(i).getName())
					StringBuilder sb = new StringBuilder();
					sb.append(NumberUtil.getFormat(todayGoodList.get(i).getNum(), todayGoodNum)).append("%").append(" ").append(todayGoodList.get(i).getName());
					redJson.put("top"+(i+1),sb.toString());
				}
				dm.setRedList(com.alibaba.fastjson.JSONObject.toJSONString(redJson));
			}
		}
		if (todayBadNum == 0) {//无差评
			dm.setBadList("------无差评-----");
		} else {
			if (!todayBadList.isEmpty()) {
				com.alibaba.fastjson.JSONObject bakcJson = new com.alibaba.fastjson.JSONObject();
				for (int i = 0; i < todayBadList.size(); i++) {
					//1、27% 剁椒鱼头
					//sbScore.append("top"+(i + 1)).append("：").append(NumberUtil.getFormat(todayBadList.get(i).getNum(), todayBadNum)).append("%").append(" ").append(todayBadList.get(i).getName()).append("\n");
					StringBuilder sb = new StringBuilder();
					sb.append(NumberUtil.getFormat(todayBadList.get(i).getNum(), todayBadNum)).append("%").append(" ").append(todayBadList.get(i).getName());
					bakcJson.put("top"+(i+1),sb.toString());
				}
				dm.setBadList(com.alibaba.fastjson.JSONObject.toJSONString(bakcJson));
			}
		}
		dayAppraiseMessageService.insert(dm);


	}





}
