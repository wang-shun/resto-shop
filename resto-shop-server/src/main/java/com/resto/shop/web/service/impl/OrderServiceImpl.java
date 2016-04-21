package com.resto.shop.web.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.json.JSONObject;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.ApplicationUtils;
import com.resto.brand.core.util.WeChatPayUtils;
import com.resto.brand.web.model.WechatConfig;
import com.resto.brand.web.service.WechatConfigService;
import com.resto.shop.web.constant.DistributionType;
import com.resto.shop.web.constant.OrderItemType;
import com.resto.shop.web.constant.OrderState;
import com.resto.shop.web.constant.PayMode;
import com.resto.shop.web.constant.ProductionStatus;
import com.resto.shop.web.constant.TicketType;
import com.resto.shop.web.container.OrderProductionStateContainer;
import com.resto.shop.web.dao.OrderMapper;
import com.resto.shop.web.datasource.DataSourceContextHolder;
import com.resto.shop.web.exception.AppException;
import com.resto.shop.web.model.Account;
import com.resto.shop.web.model.Article;
import com.resto.shop.web.model.ArticlePrice;
import com.resto.shop.web.model.Coupon;
import com.resto.shop.web.model.Customer;
import com.resto.shop.web.model.Kitchen;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.model.OrderItem;
import com.resto.shop.web.model.OrderPaymentItem;
import com.resto.shop.web.model.Printer;
import com.resto.shop.web.service.AccountService;
import com.resto.shop.web.service.ArticlePriceService;
import com.resto.shop.web.service.ArticleService;
import com.resto.shop.web.service.CouponService;
import com.resto.shop.web.service.CustomerService;
import com.resto.shop.web.service.KitchenService;
import com.resto.shop.web.service.OrderItemService;
import com.resto.shop.web.service.OrderPaymentItemService;
import com.resto.shop.web.service.OrderService;
import com.resto.shop.web.service.PrinterService;
import com.resto.shop.web.service.ShopCartService;
import com.resto.shop.web.util.DateUtil;

import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class OrderServiceImpl extends GenericServiceImpl<Order, String> implements OrderService {

    @Resource
    private OrderMapper orderMapper;
    
    @Resource
    private CustomerService customerService;
    
    @Resource
    private ArticleService articleService;
    
    @Resource
    private ArticlePriceService articlePriceService;
    
    @Resource
    private CouponService couponService;
    
    @Resource
    OrderPaymentItemService orderPaymentItemService;
    
    @Resource
    private AccountService accountService;
    
    @Resource
    OrderItemService orderItemService;
    
    @Resource
    ShopCartService shopCartService;
    
    @Resource
    WechatConfigService wechatConfigService;
    
    @Resource
    OrderProductionStateContainer orderProductionStateContainer;
    
    @Resource	
    KitchenService kitchenService;
    
    @Resource
    PrinterService printerService;
    
    @Override
    public GenericDao<Order, String> getDao() {
        return orderMapper;
    }

	@Override
	public List<Order> listOrder(Integer start, Integer datalength, String shopId, String customerId,
			String ORDER_STATE) {
		String[] states = null;
		if (ORDER_STATE != null) {
			states = ORDER_STATE.split(",");
		}
		return orderMapper.orderList(start, datalength, shopId, customerId, states);
	}

	@Override
	public Order selectOrderStatesById(String orderId) {
		return orderMapper.selectOrderStatesById(orderId);
	}

	@Override
	public Order createOrder(Order order) throws AppException {
		String orderId = ApplicationUtils.randomUUID();
		order.setId(orderId);
		Customer customer = customerService.selectById(order.getCustomerId());
		if (customer == null) {
			throw new AppException(AppException.CUSTOMER_NOT_EXISTS);
		} else if (customer.getTelephone() == null) {
			throw new AppException(AppException.NOT_BIND_PHONE);
		} else if (order.getOrderItems().isEmpty()) {
			throw new AppException(AppException.ORDER_ITEMS_EMPTY);
		}
		List<Article> articles = articleService.selectList(order.getShopDetailId());
		List<ArticlePrice> articlePrices = articlePriceService.selectList(order.getShopDetailId());
		Map<String, Article> articleMap = ApplicationUtils.convertCollectionToMap(String.class, articles);
		Map<String, ArticlePrice> articlePriceMap = ApplicationUtils.convertCollectionToMap(String.class,
				articlePrices);

		order.setVerCode(customer.getTelephone().substring(7));
		order.setId(orderId);
		order.setCreateTime(new Date());
		BigDecimal totalMoney = BigDecimal.ZERO;
		int articleCount = 0;
		for (OrderItem item : order.getOrderItems()) {
			Article a = null;
			BigDecimal org_price = null;
			BigDecimal price = null;
			BigDecimal fans_price = null;
			switch (item.getType()) {
			case OrderItemType.ARTICLE:
				// 查出 item对应的 商品信息，并将item的原价，单价，总价，商品名称，商品详情 设置为对应的
				a = articleMap.get(item.getArticleId());
				org_price = a.getPrice();
				price = a.getPrice();
				fans_price = a.getFansPrice();
				break;
			case OrderItemType.UNITPRICE:
				ArticlePrice p = articlePriceMap.get(item.getArticleId());
				a = articleMap.get(p.getArticleId());
				org_price = p.getPrice();
				price = p.getPrice();
				fans_price = p.getFansPrice();
				break;
			default:
				throw new AppException(AppException.UNSUPPORT_ITEM_TYPE, "不支持的餐品类型:" + item.getType());
			}
			item.setArticleDesignation(a.getDescription());
			item.setArticleName(a.getName());
			item.setOriginalPrice(org_price);
			item.setStatus(1);
			item.setSort(0);
			if (fans_price != null) {
				item.setUnitPrice(fans_price);
			} else {
				item.setUnitPrice(price);
			}
			BigDecimal finalMoney = item.getUnitPrice().multiply(new BigDecimal(item.getCount())).setScale(2,
					BigDecimal.ROUND_HALF_UP);
			articleCount += item.getCount();
			item.setFinalPrice(finalMoney);
			item.setOrderId(orderId);
			item.setId(ApplicationUtils.randomUUID());
			totalMoney = totalMoney.add(finalMoney).setScale(2, BigDecimal.ROUND_HALF_UP);
		}
		orderItemService.insertItems(order.getOrderItems());
		BigDecimal payMoney = totalMoney;

		// 使用优惠卷
		if (order.getUseCoupon() != null) {
			Coupon coupon = couponService.useCoupon(totalMoney, order);
			OrderPaymentItem item = new OrderPaymentItem();
			item.setId(ApplicationUtils.randomUUID());
			item.setOrderId(orderId);
			item.setPaymentModeId(PayMode.COUPON_PAY);
			item.setPayTime(order.getCreateTime());
			item.setPayValue(coupon.getValue());
			item.setRemark("优惠卷支付:" + item.getPayValue());
			item.setResultData(coupon.getId());
			orderPaymentItemService.insert(item);
			payMoney = payMoney.subtract(item.getPayValue()).setScale(2, BigDecimal.ROUND_HALF_UP);
		}

		// 使用余额
		if (payMoney.doubleValue() > 0 && order.isUseAccount()) {
			Account account = accountService.selectById(customer.getAccountId());
			BigDecimal payValue = accountService.useAccount(payMoney, account);
			if (payValue.doubleValue() > 0) {
				OrderPaymentItem item = new OrderPaymentItem();
				item.setId(ApplicationUtils.randomUUID());
				item.setOrderId(orderId);
				item.setPaymentModeId(PayMode.ACCOUNT_PAY);
				item.setPayTime(order.getCreateTime());
				item.setPayValue(payValue);
				item.setRemark("余额支付:" + item.getPayValue());
				item.setResultData(account.getId());
				orderPaymentItemService.insert(item);
				payMoney = payMoney.subtract(item.getPayValue()).setScale(2, BigDecimal.ROUND_HALF_UP);
			}
		}

		order.setAccountingTime(order.getCreateTime()); // 财务结算时间
		order.setAllowCancel(true); // 订单是否允许取消
		order.setArticleCount(articleCount); // 订单餐品总数
		order.setClosed(false); // 订单是否关闭 否
		order.setSerialNumber(DateFormatUtils.format(new Date(), "yyyyMMddHHmmssSSSS")); // 流水号
		order.setOriginalAmount(totalMoney);// 原价
		order.setReductionAmount(BigDecimal.ZERO);// 折扣金额
		order.setOrderMoney(totalMoney); // 订单实际金额
		order.setPaymentAmount(payMoney); // 订单剩余需要维修支付的金额
		order.setPrintTimes(0);
		order.setOrderState(OrderState.SUBMIT);
		order.setProductionStatus(ProductionStatus.NOT_ORDER);
		insert(order);

		if (order.getPaymentAmount().doubleValue() == 0) {
			payOrderSuccess(order);
		}
		return order;
	}

	public Order payOrderSuccess(Order order) {
		order.setOrderState(OrderState.PAYMENT);
		update(order);
		return order;
	}

	@Override
	public Order findCustomerNewOrder(String customerId, String shopId, String orderId) {
		Date beginDate = DateUtil.getDateBegin(new Date());
		Integer[] orderState = new Integer[] { OrderState.SUBMIT, OrderState.PAYMENT, OrderState.CONFIRM };
		Order order = orderMapper.findCustomerNewOrder(beginDate, customerId, shopId, orderState, orderId);
		if (!StringUtils.isBlank(orderId) && order != null) {
			List<OrderItem> itemList = orderItemService.listByOrderId(order.getId());
			order.setOrderItems(itemList);
		}
		return order;
	}

	@Override
	public boolean cancelOrder(String orderId) {
		Order order = selectById(orderId);
		if (order.getOrderState().equals(OrderState.SUBMIT) || order.getOrderState().equals(OrderState.PAYMENT)) {
			order.setAllowCancel(false);
			order.setClosed(true);
			order.setOrderState(OrderState.CANCEL);
			update(order);
			refundOrder(order);
			log.info("取消订单成功:" + order.getId());
			return true;
		} else {
			log.warn("取消订单失败，订单状态订单状态或者订单可取消字段为false");
			return false;
		}
	}

	private void refundOrder(Order order) {
		List<OrderPaymentItem> payItemsList = orderPaymentItemService.selectByOrderId(order.getId());
		for (OrderPaymentItem item : payItemsList) {
			String newPayItemId = ApplicationUtils.randomUUID();
			switch (item.getPaymentModeId()) {
			case PayMode.COUPON_PAY:
				couponService.refundCoupon(item.getResultData());
				break;
			case PayMode.ACCOUNT_PAY:
				accountService.addAccount(item.getPayValue(), item.getResultData(), "取消订单返还");
				break;
			case PayMode.WEIXIN_PAY:
				WechatConfig config = wechatConfigService.selectByBrandId(DataSourceContextHolder.getDataSourceName());
				JSONObject obj = new JSONObject(item.getResultData());
				Map<String, String> result = WeChatPayUtils.refund(newPayItemId, obj.getString("transaction_id"),
						obj.getInt("total_fee"), obj.getInt("refund_fee"), config.getAppid(), config.getMchid(),
						config.getMchkey(), config.getPayCertPath());
				item.setResultData(new JSONObject(result).toString());
				break;
			}
			item.setId(newPayItemId);
			item.setPayValue(item.getPayValue().multiply(new BigDecimal(-1)));
			orderPaymentItemService.insert(item);
		}
	}

	@Override
	public Order orderWxPaySuccess(OrderPaymentItem item) {
		Order order = selectById(item.getOrderId());
		if (order.getOrderState().equals(OrderState.SUBMIT)) {
			orderPaymentItemService.insert(item);
			return payOrderSuccess(order);
		}
		return null;
	}

	@Override
	public Order pushOrder(String orderId) {
		Order order = selectById(orderId);
		if (OrderState.PAYMENT == order.getOrderState() && ProductionStatus.NOT_ORDER == order.getProductionStatus()) {
			order.setProductionStatus(ProductionStatus.HAS_ORDER);
			order.setPushOrderTime(new Date());
			update(order);
			return order;
		}
		return null;
	}

	@Override
	public Order callNumber(String orderId) {
		Order order = selectById(orderId);
		if(order.getCallNumberTime()==null){
			order.setProductionStatus(ProductionStatus.HAS_CALL);
			order.setCallNumberTime(new Date());
			update(order);
		}
		return order;
	}

	@Override
	public List<Map<String, Object>> getPrintData(String orderId) {

		return null;
	}

	@Override
	public Order printSuccess(String orderId) {
		Order order = selectById(orderId);
		if(order.getPrintOrderTime()==null){
			order.setProductionStatus(ProductionStatus.PRINTED);
			order.setPrintOrderTime(new Date());
			update(order);
		}
		return order;
	}


	@Override
	public List<Order> selectTodayOrder(String shopId, int[] proStatus) {
		Date date = DateUtil.getDateBegin(new Date());
		List<Order> orderList = orderMapper.selectShopOrderByDateAndProductionStates(shopId,date,proStatus);
		return orderList;
	}

	@Override
	public List<Order> selectReadyOrder(String currentShopId) {
		List<Order> order = orderProductionStateContainer.getReadyOrderList(currentShopId);
		return order;
	}
	
	@Override
	public List<Order> selectPushOrder(String currentShopId) {
		return orderProductionStateContainer.getPushOrderList(currentShopId);
	}

	@Override
	public List<Order> selectCallOrder(String currentBrandId) {
		return orderProductionStateContainer.getCallNowList(currentBrandId);
	}

	@Override
	public Map<String, Object> printReceipt(String orderId) {
		// 根据id查询订单
		Order order = selectById(orderId);
		//查询订单菜品
		List<Map<String,Object>> articleList = orderItemService.selectOrderArticleList();
		
		
		
		return null;
	}

	
	
	@Override
	public List<Map<String, Object>> printKitchen(Order order, List<Map<String, Object>> articleList) {
		Map<String,List<Map<String,Object>>> kitchenArticleMap = new HashMap<String, List<Map<String,Object>>>();
		Map<String,Kitchen> kitchenMap = new HashMap<String, Kitchen>();
		
		//得到 需要打印的菜品信息和厨房信息
		for(Map<String,Object> articleMap : articleList){
			//得到 当前菜品 对应的厨房信息
			List<Kitchen> kitchenList = kitchenService.selectInfoByArticleId(articleMap.get("articleId").toString());
			for(Kitchen kitchen : kitchenList){
				String kitchenId = kitchen.getId().toString();
				kitchenMap.put(kitchenId, kitchen);
				//判断 厨房集合中 是否已经包含当前厨房信息
				if(kitchenArticleMap.containsKey(kitchenId)){
					//如果有  则直接 把需要制作的 菜品信息 放入
					kitchenArticleMap.get(kitchenId).add(articleMap);
				}else{
					//如果没有 则新建
					kitchenArticleMap.put(kitchenId, new ArrayList<Map<String,Object>>());
					kitchenArticleMap.get(kitchenId).add(articleMap);
				}
			}
		}
		
		String tableNumber = order.getTableNumber();
		
		List<Map<String,Object>> printTask = new ArrayList<Map<String,Object>>();
		String modeText = DistributionType.getModeText(order.getDistributionModeId());
		String serialNumber = order.getSerialNumber();//原来写的是流水号。。。
		
		//厨房 循环
		for(String kitchenId : kitchenArticleMap.keySet()){
			//生成厨房小票
			for(Map<String,Object> articleMap : kitchenArticleMap.get(kitchenId)){
				//保存 菜品的名称和数量
				List<Map<String,Object>> items = new ArrayList<Map<String,Object>>();
				Map<String,Object> item = new HashMap<String,Object>();
				item.put("ARTICLE_NAME", articleMap.get("name"));
				item.put("ARTICLE_COUNT",articleMap.get("count"));
				items.add(item);
				//得到厨房 信息
				Kitchen kitchen = kitchenMap.get(kitchenId);
				//得到打印机信息
				Printer printer = printerService.selectById(kitchen.getPrinterId());
				
				Map<String,Object> data = new HashMap<String,Object>();
				data.put("KITCHEN_NAME",kitchen.getName());
				data.put("DISTRIBUTION_MODE",modeText);
				data.put("TABLE_NUMBER", tableNumber);
				data.put("ORDER_ID", serialNumber);
				data.put("DATETIME",DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
				data.put("ITEMS", items);
				
				Map<String,Object> print = new HashMap<String,Object>();
				String print_id = ApplicationUtils.randomUUID();
				print.put("PRINT_TASK_ID", print_id);
				print.put("STATUS",0);
				print.put("ORDER_ID", serialNumber);
				print.put("KITCHEN_NAME", kitchen.getName());
				print.put("DATA", data);
				print.put("TABLE_NO", tableNumber);
				print.put("IP", printer.getIp());
				print.put("PORT", printer.getPort());
				print.put("ADD_TIME", new Date());
				print.put("TICKET_TYPE", TicketType.KITCHEN);
				printTask.add(print);
			}
		}
		
		//如果是外带，添加一张外带小票
		if(order.getDistributionModeId().equals(DistributionType.TAKE_IT_SELF)){
			List<Map<String,Object>> items = new ArrayList<Map<String,Object>>();
			for(Map<String,Object> articleMap:articleList){
				Map<String,Object> item = new HashMap<String,Object>();
				item.put("ARTICLE_NAME", articleMap.get("NAME"));
				item.put("ARTICLE_COUNT", articleMap.get("COUNT"));
				items.add(item);
			}
			Map<String,Object> data = new HashMap<String,Object>();
			data.put("ARTICLE_COUNT",order.getArticleCount());
			data.put("DISTRIBUTION_MODE","外带");
			data.put("TABLE_NUMBER",tableNumber);
			data.put("ORDER_ID", serialNumber);
			data.put("DATETIME",DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm"));
			data.put("ITEMS", items);
			
			Printer printer = printerService.selectById(1);//查找外包的打印机
			Map<String,Object> print = new HashMap<String,Object>();
			print.put("ORDER_ID", serialNumber);
			print.put("KITCHEN_NAME", "打包处");
			print.put("DATA", data);
			print.put("TABLE_NO", tableNumber);
			print.put("IP", printer.getIp());
			print.put("PORT", printer.getPort());
			print.put("ADD_TIME", new Date());
			print.put("TICKET_TYPE", TicketType.PACKAGE);
			printTask.add(print);
		}
				
		return printTask;
	}

	@Override
	public List<Map<String, Object>> testKitchen(String orderId) {
		// 根据id查询订单
		Order order = selectById(orderId);
		//查询订单菜品
		List<Map<String,Object>> articleList = orderItemService.selectOrderArticleList();
		//测试厨房打印
		List<Map<String, Object>> test = printKitchen(order,articleList);
		return test;
	}


}
