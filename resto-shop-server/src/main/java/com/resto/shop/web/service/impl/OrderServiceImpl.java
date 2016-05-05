package com.resto.shop.web.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.json.JSONObject;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.ApplicationUtils;
import com.resto.brand.core.util.DateUtil;
import com.resto.brand.core.util.WeChatPayUtils;
import com.resto.brand.web.model.BrandSetting;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.model.WechatConfig;
import com.resto.brand.web.service.BrandSettingService;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.brand.web.service.WechatConfigService;
import com.resto.shop.web.constant.DistributionType;
import com.resto.shop.web.constant.OrderItemType;
import com.resto.shop.web.constant.OrderState;
import com.resto.shop.web.constant.PayMode;
import com.resto.shop.web.constant.PrinterType;
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
    BrandSettingService brandSettingService;
    
    @Resource
    ShopDetailService shopDetailService;

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
				item.setArticleName(a.getName());
				org_price = a.getPrice();
				price = a.getPrice();
				fans_price = a.getFansPrice();
				break;
			case OrderItemType.UNITPRICE:
				ArticlePrice p = articlePriceMap.get(item.getArticleId());
				a = articleMap.get(p.getArticleId());
				item.setArticleName(a.getName()+p.getName());
				org_price = p.getPrice();
				price = p.getPrice();
				fans_price = p.getFansPrice();
				break;
			default:
				throw new AppException(AppException.UNSUPPORT_ITEM_TYPE, "不支持的餐品类型:" + item.getType());
			}
			item.setArticleDesignation(a.getDescription());
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
		
		if(payMoney.doubleValue()<0){
			payMoney = BigDecimal.ZERO;
		}
		order.setAccountingTime(order.getCreateTime()); // 财务结算时间
		order.setAllowCancel(true); // 订单是否允许取消
		order.setAllowAppraise(false);
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
		ShopDetail detail = shopDetailService.selectById(order.getShopDetailId());
		order.setOrderMode(detail.getShopMode());
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

	private void updateParentAmount(String orderId) {
		Double money = orderMapper.selectParentAmount(orderId);
		orderMapper.updateParentAmount(orderId,money);
	}

	@Override
	public Order findCustomerNewOrder(String customerId, String shopId, String orderId) {
		Date beginDate = DateUtil.getDateBegin(new Date());
		Integer[] orderState = new Integer[] { OrderState.SUBMIT, OrderState.PAYMENT, OrderState.CONFIRM };
		Order order = orderMapper.findCustomerNewOrder(beginDate, customerId, shopId, orderState, orderId);
		if (order != null) {
			List<OrderItem> itemList = orderItemService.listByOrderId(order.getId());
			order.setOrderItems(itemList);
		}
		return order;
	}

	@Override
	public boolean cancelOrder(String orderId) {
		Order order = selectById(orderId);
		if (order.getAllowCancel()&&order.getProductionStatus()!=ProductionStatus.PRINTED&&(order.getOrderState().equals(OrderState.SUBMIT)||order.getOrderState()==OrderState.PAYMENT)) {
			order.setAllowCancel(false);
			order.setClosed(true);
			order.setOrderState(OrderState.CANCEL);
			update(order);
			refundOrder(order);
			orderProductionStateContainer.removePushOrder(order);
			log.info("取消订单成功:" + order.getId());
			return true;
		} else {
			log.warn("取消订单失败，订单状态订单状态或者订单可取消字段为false"+order.getId());
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
						obj.getInt("total_fee"), obj.getInt("total_fee"), config.getAppid(), config.getMchid(),
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
		orderPaymentItemService.insert(item);
		return payOrderSuccess(order);
	}

	@Override
	public Order pushOrder(String orderId) throws AppException {
		Order order = selectById(orderId);
		if (validOrderCanPush(order)) {
			order.setProductionStatus(ProductionStatus.HAS_ORDER);
			order.setPushOrderTime(new Date());
			update(order);
			return order;
		}
		return null;
	}

	private boolean validOrderCanPush(Order order) throws AppException {
		if(order.getOrderState()!=OrderState.PAYMENT||ProductionStatus.NOT_ORDER!=order.getProductionStatus()){
			throw new AppException(AppException.ORDER_STATE_ERR);
		}
		switch(order.getOrderMode()){
		case 1:
			if(order.getTableNumber()==null){
				throw new AppException(AppException.ORDER_MODE_CHECK,"桌号不得为空");
			}
			break;
		}
		return true;
	}

	@Override
	public Order callNumber(String orderId) {
		Order order = selectById(orderId);
		if (order.getCallNumberTime() == null) {
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
		if (order.getPrintOrderTime() == null) {
			if(order.getParentOrderId()==null){
				order.setAllowContinueOrder(true);
			}else{
				order.setAllowContinueOrder(false);
				updateParentAmount(order.getParentOrderId());
			}
			order.setProductionStatus(ProductionStatus.PRINTED);
			order.setPrintOrderTime(new Date());
			if(order.getOrderMode()==3){
				order.setAllowCancel(false);
			}
			update(order);
		}
		return order;
	}

	@Override
	public List<Order> selectTodayOrder(String shopId, int[] proStatus) {
		Date date = DateUtil.getDateBegin(new Date());
		List<Order> orderList = orderMapper.selectShopOrderByDateAndProductionStates(shopId, date, proStatus);
		return orderList;
	}

	@Override
	public List<Order> selectReadyOrder(String currentShopId,Long lastTime) {
		List<Order> order = orderProductionStateContainer.getReadyOrderList(currentShopId,lastTime);
		return order;
	}

	@Override
	public List<Order> selectPushOrder(String currentShopId,Long lastTime) {
		return orderProductionStateContainer.getPushOrderList(currentShopId,lastTime);
	}

	@Override
	public List<Order> selectCallOrder(String currentBrandId,Long lastTime) {
		return orderProductionStateContainer.getCallNowList(currentBrandId,lastTime);
	}

	
	@Override
	public List<Map<String, Object>> printKitchen(Order order, List<OrderItem> articleList) {
		//每个厨房 所需制作的   菜品信息
		Map<String,List<OrderItem>> kitchenArticleMap = new HashMap<String, List<OrderItem>>();
		//厨房信息
		Map<String,Kitchen> kitchenMap = new HashMap<String, Kitchen>();
		//遍历 订单集合 
		for(OrderItem item : articleList){
			//得到当前菜品 所关联的厨房信息
			String articleId = item.getArticleId();
			if(item.getType()==OrderItemType.UNITPRICE){
				if(articleId.length()>32){
					articleId = item.getArticleId().substring(0,32);
				}
			}
			List<Kitchen> kitchenList = kitchenService.selectInfoByArticleId(articleId);
			for(Kitchen kitchen : kitchenList){
				String kitchenId = kitchen.getId().toString();
				kitchenMap.put(kitchenId, kitchen);//保存厨房信息
				//判断 厨房集合中 是否已经包含当前厨房信息
				if(!kitchenArticleMap.containsKey(kitchenId)){
					//如果没有 则新建
					kitchenArticleMap.put(kitchenId, new ArrayList<OrderItem>());
				}
				kitchenArticleMap.get(kitchenId).add(item);
			}
		}
		
		//桌号
		String tableNumber = order.getTableNumber() != null ? order.getTableNumber() : "" ;
		//打印线程集合
		List<Map<String,Object>> printTask = new ArrayList<Map<String,Object>>();
		
		String modeText = DistributionType.getModeText(order.getDistributionModeId());//就餐模式
		String serialNumber = order.getSerialNumber();//序列号
		
		//编列 厨房菜品 集合
		for(String kitchenId : kitchenArticleMap.keySet()){
			Kitchen kitchen = kitchenMap.get(kitchenId);//得到厨房 信息
			Printer printer = printerService.selectById(kitchen.getPrinterId());//得到打印机信息
			//生成厨房小票
			for(OrderItem article : kitchenArticleMap.get(kitchenId)){
				//保存 菜品的名称和数量
				List<Map<String,Object>> items = new ArrayList<Map<String,Object>>();
				Map<String,Object> item = new HashMap<String,Object>();
				item.put("ARTICLE_NAME", article.getArticleName());
				item.put("ARTICLE_COUNT",article.getCount());
				items.add(item);
				//保存基本信息
				Map<String,Object> data = new HashMap<String,Object>();
				data.put("KITCHEN_NAME",kitchen.getName());
				data.put("DISTRIBUTION_MODE",modeText);
				data.put("TABLE_NUMBER", tableNumber);
				data.put("ORDER_ID", serialNumber);
				data.put("DATE",DateUtil.formatDate(new Date(), "MM-dd HH:mm"));
				data.put("ITEMS", items);
				//保存打印配置信息
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
				//添加到 打印集合
				printTask.add(print);
			}
		}
		
		//如果是外带，添加一张外带小票
		if(order.getDistributionModeId().equals(DistributionType.TAKE_IT_SELF)){
			List<Map<String,Object>> items = new ArrayList<Map<String,Object>>();
			for(OrderItem article: articleList){
				Map<String,Object> item = new HashMap<String,Object>();
				item.put("ARTICLE_NAME", article.getArticleName());
				item.put("ARTICLE_COUNT", article.getCount());
				items.add(item);
			}
			Map<String,Object> data = new HashMap<String,Object>();
			data.put("ARTICLE_COUNT",order.getArticleCount());
			data.put("DISTRIBUTION_MODE","外带");
			data.put("TABLE_NUMBER",tableNumber);
			data.put("ORDER_ID", serialNumber);
			data.put("DATE",DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm"));
			data.put("ITEMS", items);
			
			Printer printer = printerService.selectByShopAndType(order.getShopDetailId(), PrinterType.PACKAGE); //查找外带的打印机
			if(printer!=null){
				Map<String,Object> print = new HashMap<String,Object>();
				print.put("ORDER_ID", serialNumber);
				print.put("KITCHEN_NAME", printer.getName());
				print.put("DATA", data);
				print.put("TABLE_NO", tableNumber);
				print.put("IP", printer.getIp());
				print.put("PORT", printer.getPort());
				print.put("ADD_TIME", new Date());
				print.put("TICKET_TYPE", TicketType.PACKAGE);
				printTask.add(print);
			}
		}
		
		return printTask;
	}

  
	@Override
	public Map<String, Object> printReceipt(String orderId) {
		// 根据id查询订单
		Order order = selectById(orderId);
		//查询店铺
		ShopDetail shopDetail = shopDetailService.selectById(order.getShopDetailId());
		// 查询订单菜品
		List<OrderItem> articleList = orderItemService.selectOrderArticleList(orderId);
		return printTicket(order, articleList, shopDetail);
	}

 

	public Map<String, Object> printTicket(Order order, List<OrderItem> articleList , ShopDetail shopDetail) {
		List<Map<String, Object>> items = new ArrayList<>();
		for (OrderItem article : articleList) {
			Map<String, Object> item = new HashMap<>();
			item.put("ARTICLE_NAME", article.getArticleName());
			item.put("ARTICLE_COUNT", article.getCount());
			item.put("SUBTOTAL", article.getFinalPrice());
			items.add(item);
		}

		Map<String, Object> data = new HashMap<>();
		String modeText = DistributionType.getModeText(order.getDistributionModeId());
		data.put("DISTRIBUTION_MODE", modeText);
		data.put("ARTICLE_COUNT", order.getArticleCount());
		data.put("RESTAURANT_NAME", shopDetail.getName());

		data.put("RESTAURANT_ADDRESS", shopDetail.getAddress());
		data.put("RESTAURANT_TEL", shopDetail.getPhone());
		data.put("TABLE_NUMBER", order.getTableNumber());
		data.put("ORDER_ID", order.getSerialNumber()+"-"+order.getVerCode());
		data.put("DATE", DateUtil.formatDate(new Date(), "MM-dd HH:mm"));
		data.put("ITEMS", items);
		data.put("ORIGINAL_AMOUNT", order.getOriginalAmount());
		data.put("REDUCTION_AMOUNT", order.getReductionAmount());
		data.put("PAYMENT_AMOUNT", order.getPaymentAmount());

		// 根据shopDetailId查询出打印机类型为2的打印机(前台打印机)
		Printer printer = printerService.selectByShopAndType(shopDetail.getId(), PrinterType.RECEPTION);
		if (printer == null) {
			return null;
		}
		Map<String, Object> print = new HashMap<>();
		String print_id = ApplicationUtils.randomUUID();
		print.put("PRINT_TASK_ID", print_id);
		print.put("STATUS", 0);
		print.put("ORDER_ID", order.getSerialNumber());

		print.put("KITCHEN_NAME", printer.getName());
		print.put("DATA", data);
		print.put("TABLE_NO", order.getTableNumber());
		print.put("IP", printer.getIp());
		print.put("PORT", printer.getPort());
		print.put("ADD_TIME", new Date());
		print.put("TICKET_TYPE", TicketType.RECEIPT);
		return print;
	}

	@Override
	public Order confirmOrder(Order order) {
		order = selectById(order.getId());
		if(order.getConfirmTime()==null){
			order.setOrderState(OrderState.CONFIRM);
			order.setConfirmTime(new Date());
			order.setAllowCancel(false);
			BrandSetting setting = brandSettingService.selectByBrandId(order.getBrandId());
			if(setting.getAppraiseMinMoney().compareTo(order.getOrderMoney())<=0){ //如果订单金额大于 评论金额 则允许评论
				order.setAllowAppraise(true);
			}else{
				order.setAllowAppraise(false);
			}
			update(order);
			return order;
		}
		return null;
	}

	@Override
	public Order getOrderInfo(String orderId) {
		Order order = orderMapper.selectByPrimaryKey(orderId);
		List<OrderItem> orderItems = orderItemService.listByOrderId(orderId);
		order.setOrderItems(orderItems);
		Customer cus = customerService.selectById(order.getCustomerId());
		order.setCustomer(cus);
		return order;
	}

	@Override
	public List<Order> selectHistoryOrderList(String currentShopId, Date date) {
		Date begin = DateUtil.getDateBegin(date);
		Date end  = DateUtil.getDateEnd(date);
	 return  orderMapper.selectHistoryOrderList(currentShopId,begin,end);
		
	}

	@Override
	public Order cancelOrderPos(String orderId) throws AppException {
		Order order = selectById(orderId);
		if(order.getClosed()){
			throw new AppException(AppException.ORDER_IS_CLOSED);
		}else{
			order.setClosed(true);
			order.setOrderState(OrderState.CANCEL);
			update(order);
			refundOrder(order);
			log.info("取消订单成功:" + order.getId());
			orderProductionStateContainer.removePushOrder(order);
		}
		return order;
	}

	@Override
	public void changePushOrder(Order order) {
		order = selectById(order.getId());
		if(order.getProductionStatus()==ProductionStatus.HAS_ORDER){ //如果还是已下单状态，则将订单状态改为未下单
			orderMapper.clearPushOrder(order.getId(),ProductionStatus.NOT_ORDER);
			orderProductionStateContainer.removePushOrder(order);
		}
	}

	@Override
	public List<Map<String, Object>> printOrderAll(String orderId) {
		log.info("打印订单全部:"+orderId);
		Order order = selectById(orderId);
		ShopDetail shop= shopDetailService.selectById(order.getShopDetailId());
		List<OrderItem> items = orderItemService.selectOrderArticleList(orderId);
		List<Map<String,Object>> printTask = new ArrayList<>();
		Map<String,Object> ticket = printTicket(order, items,shop);
		List<Map<String,Object>> kitchenTicket = printKitchen(order, items);
		if(ticket!=null){
			printTask.add(ticket);
		}
		if(!kitchenTicket.isEmpty()){
			printTask.addAll(kitchenTicket);
		}
		return printTask;
	}

	@Override
	public void setTableNumber(String orderId, String tableNumber) {
		orderMapper.setOrderNumber(orderId,tableNumber);
	}

	@Override
	public List<Order> selectOrderByVercode(String vercode,String shopId) {
		List<Order> orderList = orderMapper.selectOrderByVercode(vercode,shopId);
		return orderList;
	}

	@Override
	public List<Order> selectOrderByTableNumber(String tableNumber, String shopId) {
		List<Order> orderList = orderMapper.selectOrderByTableNumber(tableNumber, shopId);
		return orderList;
	}

	@Override
	public void updateDistributionMode(Integer modeId, String orderId){
		Order order = selectById(orderId);
		order.setDistributionModeId(modeId);
		 orderMapper.updateByPrimaryKeySelective(order);
	}

	@Override
	public void clearNumber(String currentShopId) {
		orderProductionStateContainer.clearMap(currentShopId);
		
	}

	@Override
	public List<Order> listOrderByStatus(String currentShopId, Date begin, Date end, int[] productionStatus,
			int[] orderState) {
		return orderMapper.listOrderByStatus(currentShopId,begin,end,productionStatus,orderState);
	}
}
