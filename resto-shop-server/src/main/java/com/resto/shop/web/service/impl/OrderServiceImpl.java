package com.resto.shop.web.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.ApplicationUtils;
import com.resto.shop.web.constant.OrderItemType;
import com.resto.shop.web.constant.OrderState;
import com.resto.shop.web.constant.PayMode;
import com.resto.shop.web.dao.OrderMapper;
import com.resto.shop.web.exception.AppException;
import com.resto.shop.web.model.Account;
import com.resto.shop.web.model.Article;
import com.resto.shop.web.model.ArticlePrice;
import com.resto.shop.web.model.Coupon;
import com.resto.shop.web.model.Customer;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.model.OrderItem;
import com.resto.shop.web.model.OrderPaymentItem;
import com.resto.shop.web.service.AccountService;
import com.resto.shop.web.service.ArticlePriceService;
import com.resto.shop.web.service.ArticleService;
import com.resto.shop.web.service.CouponService;
import com.resto.shop.web.service.CustomerService;
import com.resto.shop.web.service.OrderItemService;
import com.resto.shop.web.service.OrderPaymentItemService;
import com.resto.shop.web.service.OrderService;
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
    
    @Override
    public GenericDao<Order, String> getDao() {
        return orderMapper;
    }

	@Override
	public List<Order> listOrder(Integer start, Integer datalength, String shopId, String customerId,String ORDER_STATE) {
		String[] states = null;
		if(	ORDER_STATE != null){
			states = ORDER_STATE.split(",");
		}
		return orderMapper.orderList(start, datalength, shopId, customerId,states);
	}

	@Override
	public Order selectOrderStatesById(String orderId) {
		return orderMapper.selectOrderStatesById(orderId);
	}

	@Override
	public Order createOrder(Order order, String useCoupon, Boolean useAccount) throws AppException {
		String orderId = ApplicationUtils.randomUUID();
		order.setId(orderId);
		Customer customer = customerService.selectById(order.getCustomerId());
		if(customer==null){
			throw new AppException(AppException.CUSTOMER_NOT_EXISTS);
		}else if(customer.getTelephone()==null){
			throw new AppException(AppException.NOT_BIND_PHONE);
		}else if(order.getOrderItems().isEmpty()){
			throw new AppException(AppException.ORDER_ITEMS_EMPTY);
		}
		List<Article> articles =  articleService.selectList(order.getShopDetailId());
		List<ArticlePrice> articlePrices = articlePriceService.selectList(order.getShopDetailId());
		Map<String,Article> articleMap = ApplicationUtils.convertCollectionToMap(String.class,articles);
		Map<String,ArticlePrice> articlePriceMap = ApplicationUtils.convertCollectionToMap(String.class, articlePrices);
		
		order.setVerCode(customer.getTelephone().substring(7));
		order.setId(orderId);
		order.setCreateTime(new Date());
		BigDecimal totalMoney = BigDecimal.ZERO;
		for(OrderItem item :order.getOrderItems()){
			Article a=  null;
			BigDecimal org_price = null;
			BigDecimal price = null;
			BigDecimal fans_price = null;
			switch (item.getType()) {
			case OrderItemType.ARTICLE:
				//查出 item对应的 商品信息，并将item的原价，单价，总价，商品名称，商品详情 设置为对应的
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
			}
			item.setArticleDesignation(a.getDescription());
			item.setArticleName(a.getName());
			item.setOriginalPrice(org_price);
			if(fans_price==null){
				item.setUnitPrice(fans_price);
			}else{
				item.setUnitPrice(price);
			}
			BigDecimal finalMoney = item.getUnitPrice().multiply(new BigDecimal(item.getCount())).setScale(2, BigDecimal.ROUND_HALF_UP);
			item.setFinalPrice(finalMoney);
			item.setOrderId(orderId);
			totalMoney = totalMoney.add(finalMoney).setScale(2, BigDecimal.ROUND_HALF_UP);
		}
		orderItemService.insertItems(order.getOrderItems());
		BigDecimal payMoney = totalMoney;
		
		//使用优惠卷
		if(useCoupon!=null){
			Coupon coupon = couponService.useCoupon(useCoupon,totalMoney, order,useAccount);
			OrderPaymentItem item = new OrderPaymentItem();
			item.setId(ApplicationUtils.randomUUID());
			item.setOrderId(orderId);
			item.setPaymentModeId(PayMode.COUPON_PAY);
			item.setPayTime(order.getCreateTime());
			item.setPayValue(coupon.getValue());
			item.setRemark("优惠卷支付:"+item.getPayValue());
			orderPaymentItemService.insert(item);
			payMoney = payMoney.subtract(item.getPayValue()).setScale(2, BigDecimal.ROUND_HALF_UP);
		}
		
		//使用余额
		if(payMoney.doubleValue()>0&&useAccount){
			Account account = accountService.selectById(customer.getAccountId());
			BigDecimal payValue = accountService.useAccount(payMoney, account);
			if(payValue.doubleValue()>0){
				OrderPaymentItem item = new OrderPaymentItem();
				item.setId(ApplicationUtils.randomUUID());
				item.setOrderId(orderId);
				item.setPaymentModeId(PayMode.ACCOUNT_PAY);
				item.setPayTime(order.getCreateTime());
				item.setPayValue(payValue);
				item.setRemark("余额支付:"+item.getPayValue());
				orderPaymentItemService.insert(item);
				payMoney = payMoney.subtract(item.getPayValue()).setScale(2, BigDecimal.ROUND_HALF_UP);
			}
		}
		

		order.setAccountingTime(order.getCreateTime());
		
		
		return order;
	}

	@Override
	public List<Order> findCustomerNewOrder() {
		Date TODAY = DateUtil.getDateBegin(new Date());
		Integer[] STATES = new Integer[]{OrderState.SUBMIT,OrderState.PAYMENT,OrderState.CONFIRM};
//		OrderMapper
//		//PageData order = (PageData) dao.findForObject("OrderMapper.findCustomerNewOrder", custom);
//		if(order!=null){
//			List<PageData> items = orderItemService.selectNameAndNumber(order);
//			order.put("ITEMS", items);
//		}
		return null;
	}

}
