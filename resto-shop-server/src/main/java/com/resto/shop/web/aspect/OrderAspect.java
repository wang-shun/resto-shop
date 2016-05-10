package com.resto.shop.web.aspect;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.resto.brand.core.util.DateUtil;
import com.resto.brand.core.util.WeChatUtils;
import com.resto.brand.web.model.BrandSetting;
import com.resto.brand.web.model.ShopMode;
import com.resto.brand.web.model.WechatConfig;
import com.resto.brand.web.service.BrandSettingService;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.brand.web.service.WechatConfigService;
import com.resto.shop.web.constant.OrderState;
import com.resto.shop.web.constant.ProductionStatus;
import com.resto.shop.web.container.OrderProductionStateContainer;
import com.resto.shop.web.model.Customer;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.model.OrderItem;
import com.resto.shop.web.producer.MQMessageProducer;
import com.resto.shop.web.service.CustomerService;
import com.resto.shop.web.service.OrderItemService;
import com.resto.shop.web.service.ShopCartService;

@Component
@Aspect
public class OrderAspect {
	
	Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	ShopCartService shopCartService;
	@Resource
	CustomerService customerService;
	@Resource
	WechatConfigService wechatConfigService;
	@Resource
	BrandSettingService brandSettingService;
	@Resource
	OrderProductionStateContainer orderProductionStateContainer;
	@Resource
	OrderItemService orderItemService;
	@Resource
	ShopDetailService shopDetailService;
	
	@Pointcut("execution(* com.resto.shop.web.service.OrderService.createOrder(..))")
	public void createOrder(){};
	
	@AfterReturning(value="createOrder()",returning="order")
	public void createOrderAround(Order order) throws Throwable{
		shopCartService.clearShopCart(order.getCustomerId(),order.getDistributionModeId(),order.getShopDetailId());
		if(order.getOrderState().equals(OrderState.SUBMIT)){
			long delay = 1000*60*15;//15分钟后自动取消订单
			MQMessageProducer.sendAutoCloseMsg(order.getId(),order.getBrandId(),delay);
		}else if(order.getOrderState().equals((OrderState.PAYMENT))){
			sendPaySuccessMsg(order);
		}
	}
	
	private void sendPaySuccessMsg(Order order) {
		Customer customer = customerService.selectById(order.getCustomerId());
		WechatConfig config= wechatConfigService.selectByBrandId(customer.getBrandId());
		StringBuffer msg = new StringBuffer("取餐码："+order.getVerCode()+"\n");
		if( order.getShopName()==null||"".equals(order.getShopName())){
			order.setShopName(shopDetailService.selectById(order.getShopDetailId()).getName());
		}
		msg.append("取餐店铺："+order.getShopName()+"\n");
		msg.append("订单时间："+DateFormatUtils.format(order.getCreateTime(), "yyyy-MM-dd HH:mm")+"\n");
		msg.append("订单明细：\n");
		List<OrderItem> orderItem  = orderItemService.listByOrderId(order.getId());
		for(OrderItem item : orderItem){
			msg.append("  "+item.getArticleName()+"x"+item.getCount()+"\n");
		}
		msg.append("订单金额："+order.getOrderMoney()+"\n");
		try {
			String result = WeChatUtils.sendCustomerMsg(msg.toString(),customer.getWechatId(),config.getAppid(),config.getAppsecret());
			log.info("订单支付完成后，发送客服消息:"+order.getId()+" -- "+result);
		} catch (Exception e) {
			log.error("发送客服消息失败:"+e.getMessage());
		}
	}

	@Pointcut("execution(* com.resto.shop.web.service.OrderService.orderWxPaySuccess(..))")
	public void orderWxPaySuccess(){};
	
	@AfterReturning(value="orderWxPaySuccess()",returning="order")
	public void orderPayAfter(Order order){
		if(order!=null&&order.getOrderState().equals(OrderState.PAYMENT)){
			sendPaySuccessMsg(order);
		}
	}
	
	@Pointcut("execution(* com.resto.shop.web.service.OrderService.pushOrder(..))")
	public void pushOrder(){};
	@Pointcut("execution(* com.resto.shop.web.service.OrderService.callNumber(..))")
	public void callNumber(){};
	@Pointcut("execution(* com.resto.shop.web.service.OrderService.printSuccess(..))")
	public void printSuccess(){};
	
	
	@AfterReturning(value="pushOrder()||callNumber()||printSuccess()",returning="order")
	public void pushOrderAfter(Order order){
		if(order!=null){
			if(ProductionStatus.HAS_ORDER==order.getProductionStatus()){
				log.info("客户下单,发送成功下单通知");
				MQMessageProducer.sendPlaceOrderMessage(order);
				log.info("客户下单，添加自动拒绝5分钟未打印的订单");
				MQMessageProducer.sendNotPrintedMessage(order,1000*60*5); //延迟五分钟，检测订单是否已经打印
			}else if(ProductionStatus.PRINTED==order.getProductionStatus()){
				MQMessageProducer.sendNotAllowContinueMessage(order,1000*60*120); //延迟两小时，禁止继续加菜
				if(order.getOrderMode()!=null){
					switch (order.getOrderMode()) {
					case ShopMode.CALL_NUMBER:
						log.info("叫号模式,发送取餐码信息:"+order.getId());
						sendVerCodeMsg(order);
						break;
					default:
						break;
					}
				}
				log.info("发送打印信息");
				MQMessageProducer.sendPlaceOrderMessage(order);
				
				log.info("打印成功后，发送自动确认订单通知！");
				BrandSetting setting = brandSettingService.selectByBrandId(order.getBrandId());
				MQMessageProducer.sendAutoConfirmOrder(order,setting.getAutoConfirmTime()*1000);
			}else if(ProductionStatus.HAS_CALL==order.getProductionStatus()){
				log.info("发送叫号信息");
				MQMessageProducer.sendPlaceOrderMessage(order);
				
			}
		}
	}

	@Pointcut("execution(* com.resto.shop.web.service.OrderService.confirmOrder(..))")
	public void confirmOrder(){};
	
	@AfterReturning(value="confirmOrder()",returning="order")
	public void confirmOrderAfter(Order order){
		if(order.getAllowAppraise()){
			Customer customer = customerService.selectById(order.getCustomerId());
			WechatConfig config = wechatConfigService.selectByBrandId(customer.getBrandId());
			BrandSetting setting = brandSettingService.selectByBrandId(customer.getBrandId());
			StringBuffer msg = new StringBuffer();
			msg.append("您有一个红包未领取\n");
			msg.append("<a href='"+setting.getWechatWelcomeUrl()+"?subpage=my&&dialog=redpackage&&orderId="+order.getId()+"'>点击领取</a>");
			
			String result = WeChatUtils.sendCustomerMsg(msg.toString(), customer.getWechatId(), config.getAppid(), config.getAppsecret());
			log.info("发送评论通知成功:"+msg+result);
		}
	}
	
	@Pointcut("execution(* com.resto.shop.web.service.OrderService.cancelOrderPos(..))")
	public void cancelOrderPos(){};
	
	@AfterReturning(value="cancelOrderPos()",returning="order")
	public void cancelOrderPosAfter(Order order){
		if(order!=null){
			Customer customer = customerService.selectById(order.getCustomerId());
			WechatConfig config = wechatConfigService.selectByBrandId(customer.getBrandId());
			StringBuffer msg = new StringBuffer();
			msg.append("您好，您 "+DateUtil.formatDate(order.getCreateTime(), "yyyy-MM-dd HH:ss")+" 的订单"+"已被商家取消");
			String result = WeChatUtils.sendCustomerMsg(msg.toString(), customer.getWechatId(), config.getAppid(), config.getAppsecret());
			log.info("发送订单取消通知成功:"+msg+result);
			MQMessageProducer.sendNoticeOrderMessage(order);
		}
	}
	
	private void sendVerCodeMsg(Order order) {
		Customer customer = customerService.selectById(order.getCustomerId());
		WechatConfig config= wechatConfigService.selectByBrandId(customer.getBrandId());
		StringBuffer msg = new StringBuffer();
		msg.append("交易码:"+order.getVerCode()+"\n");
		msg.append("请留意餐厅叫号信息");
		String result = WeChatUtils.sendCustomerMsg(msg.toString(), customer.getWechatId(), config.getAppid(), config.getAppsecret());
		log.info("发送取餐信息成功:"+result);
	}
	
	
}
