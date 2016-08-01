package com.resto.shop.web.aspect;

import com.resto.brand.core.util.DateUtil;
import com.resto.brand.core.util.WeChatUtils;
import com.resto.brand.web.model.BrandSetting;
import com.resto.brand.web.model.ShareSetting;
import com.resto.brand.web.model.ShopMode;
import com.resto.brand.web.model.WechatConfig;
import com.resto.brand.web.service.BrandSettingService;
import com.resto.brand.web.service.ShareSettingService;
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
import com.resto.shop.web.service.OrderService;
import com.resto.shop.web.service.ShopCartService;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

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
	@Resource
	ShareSettingService shareSettingService;

	@Resource
	OrderService orderService;


	@Pointcut("execution(* com.resto.shop.web.service.OrderService.createOrder(..))")
	public void createOrder(){};


	@AfterReturning(value="createOrder()",returning="order")
	public void createOrderAround(Order order) throws Throwable{
		shopCartService.clearShopCart(order.getCustomerId(),order.getShopDetailId());
		//订单在每天0点未被消费系统自动取消订单（款项自动退还到相应账户）
		log.info("当天24小时开启自动退款:"+order.getId());
		MQMessageProducer.sendAutoRefundMsg(order.getBrandId(),order.getId());
		if(order.getOrderState().equals(OrderState.SUBMIT)){
//			long delay = 1000*60*15;//15分钟后自动取消订单
//			MQMessageProducer.sendAutoCloseMsg(order.getId(),order.getBrandId(),delay);
		}else if(order.getOrderState().equals((OrderState.PAYMENT))&&order.getOrderMode()!=ShopMode.TABLE_MODE){ //坐下点餐模式不发送
			sendPaySuccessMsg(order);
		}
	}

	private void sendPaySuccessMsg(Order order) {
		Customer customer = customerService.selectById(order.getCustomerId());
		WechatConfig config= wechatConfigService.selectByBrandId(customer.getBrandId());
		StringBuffer msg = new StringBuffer();
		msg.append("订单编号:"+order.getSerialNumber()+"\n");
		if(order.getOrderMode()!=null){
			switch (order.getOrderMode()) {
			case ShopMode.TABLE_MODE:
				msg.append("桌号:"+order.getTableNumber()+"\n");
				break;
			default:
				msg.append("取餐码："+order.getVerCode()+"\n");
				break;
			}
		}
		if( order.getShopName()==null||"".equals(order.getShopName())){
			order.setShopName(shopDetailService.selectById(order.getShopDetailId()).getName());
		}
		msg.append("就餐店铺："+order.getShopName()+"\n");
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
		if(order!=null&&order.getOrderState().equals(OrderState.PAYMENT)&&ShopMode.TABLE_MODE!=order.getOrderMode()){//坐下点餐模式不发送该消息
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
	public void pushOrderAfter (Order order) throws Throwable{
		if(order!=null){
			if(ProductionStatus.HAS_ORDER==order.getProductionStatus()){
				BrandSetting setting = brandSettingService.selectByBrandId(order.getBrandId());
				log.info("客户下单,发送成功下单通知"+order.getId());
				MQMessageProducer.sendPlaceOrderMessage(order);
//				log.info("客户下单，添加自动拒绝5分钟未打印的订单");
//				MQMessageProducer.sendNotPrintedMessage(order,1000*60*5); //延迟五分钟，检测订单是否已经打印
				if(order.getOrderMode()==ShopMode.TABLE_MODE){  //坐下点餐在立即下单的时候，发送支付成功消息通知
					log.info("坐下点餐在立即下单的时候，发送支付成功消息通知:"+order.getId());
					sendPaySuccessMsg(order);
				}
				log.info("检查打印异常");
				int times = setting.getReconnectTimes();
				int seconds = setting.getReconnectSecond();
				for(int i = 0;i< times;i++){
					MQMessageProducer.checkPlaceOrderMessage(order,(i+1) * seconds * 1000L,seconds*times*1000L);
				}
			}else if(ProductionStatus.PRINTED==order.getProductionStatus()){
				BrandSetting setting = brandSettingService.selectByBrandId(order.getBrandId());
				log.info("发送禁止加菜:"+setting.getCloseContinueTime()+"s 后发送");
				MQMessageProducer.sendNotAllowContinueMessage(order,1000*setting.getCloseContinueTime()); //延迟两小时，禁止继续加菜
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



				log.info("打印成功后，发送自动确认订单通知！"+setting.getAutoConfirmTime()+"s 后发送");
				MQMessageProducer.sendAutoConfirmOrder(order,setting.getAutoConfirmTime()*1000);

//				//出单时减少库存
//				Boolean updateStockSuccess  = false;
//				updateStockSuccess	= orderService.updateStock(orderService.getOrderInfo(order.getId()));
//				if(!updateStockSuccess){
//					log.info("库存变更失败:"+order.getId());
//				}

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
		log.info("确认订单成功后回调:"+order.getId());
		Customer customer = customerService.selectById(order.getCustomerId());
		WechatConfig config = wechatConfigService.selectByBrandId(customer.getBrandId());
		BrandSetting setting = brandSettingService.selectByBrandId(customer.getBrandId());
		if(order.getAllowAppraise()){
			StringBuffer msg = new StringBuffer();
			msg.append("您有一个红包未领取\n");
			msg.append("<a href='"+setting.getWechatWelcomeUrl()+"?subpage=my&dialog=redpackage&orderId="+order.getId()+"'>点击领取</a>");

			String result = WeChatUtils.sendCustomerMsg(msg.toString(), customer.getWechatId(), config.getAppid(), config.getAppsecret());
			log.info("发送评论通知成功:"+msg+result);
		}
		try {
			if(customer.getFirstOrderTime()==null){ //分享判定
				customerService.updateFirstOrderTime(customer.getId());
				if(customer.getShareCustomer()!=null){
					Customer shareCustomer= customerService.selectById(customer.getShareCustomer());
					if(shareCustomer!=null){
						ShareSetting shareSetting = shareSettingService.selectValidSettingByBrandId(customer.getBrandId());
						if(shareSetting!=null){
							log.info("是被分享用户，并且分享设置已启用:"+customer.getId()+" oid:"+order.getId()+" setting:"+shareSetting.getId());
							BigDecimal rewardMoney = customerService.rewareShareCustomer(shareSetting,order,shareCustomer,customer);
							log.info("准备发送返利通知");
							sendRewardShareMsg(shareCustomer,customer,config,setting,rewardMoney);
						}
					}
				}
			}
		} catch (Exception e) {
			log.error("分享功能出错:"+e.getMessage());
			e.printStackTrace();
		}

	}

	private void sendRewardShareMsg(Customer shareCustomer,Customer customer, WechatConfig config,
									BrandSetting setting, BigDecimal rewardMoney) {
		StringBuffer msg = new StringBuffer();
		rewardMoney = rewardMoney.setScale(2, BigDecimal.ROUND_HALF_UP);
		msg.append("<a href='"+setting.getWechatWelcomeUrl()+"?subpage=my&dialog=account'>")
		.append("你邀请的好友").append(customer.getNickname()).append("已到店消费，你已获得")
				.append(rewardMoney).append("元红包返利").append("</a>");
		String result = WeChatUtils.sendCustomerMsg(msg.toString(), shareCustomer.getWechatId(), config.getAppid(), config.getAppsecret());
		log.info("发送返利通知成功:"+shareCustomer.getId()+" MSG: "+msg+result);
	}

	@Pointcut("execution(* com.resto.shop.web.service.OrderService.cancelOrderPos(..))")
	public void cancelOrderPos(){};

	@AfterReturning(value="cancelOrderPos()",returning="order")
	public void cancelOrderPosAfter(Order order) throws Throwable{
		if(order!=null){
			Customer customer = customerService.selectById(order.getCustomerId());
			WechatConfig config = wechatConfigService.selectByBrandId(customer.getBrandId());
			StringBuffer msg = new StringBuffer();
			msg.append("您好，您 "+DateUtil.formatDate(order.getCreateTime(), "yyyy-MM-dd HH:ss")+" 的订单"+"已被商家取消");
			String result = WeChatUtils.sendCustomerMsg(msg.toString(), customer.getWechatId(), config.getAppid(), config.getAppsecret());
			log.info("发送订单取消通知成功:"+msg+result);
			MQMessageProducer.sendNoticeOrderMessage(order);

			//拒绝订单后还原库存
			Boolean addStockSuccess  = false;
			addStockSuccess	= orderService.addStock(orderService.getOrderInfo(order.getId()));
			if(!addStockSuccess){
				log.info("库存还原失败:"+order.getId());
			}

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
