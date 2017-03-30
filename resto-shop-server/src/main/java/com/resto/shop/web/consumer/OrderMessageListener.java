package com.resto.shop.web.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.resto.brand.core.util.*;
import com.resto.brand.web.dto.LogType;
import com.resto.brand.web.model.*;
import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.BrandSettingService;
import com.resto.brand.web.service.ShareSettingService;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.brand.web.service.WechatConfigService;
import com.resto.shop.web.constant.Common;
import com.resto.shop.web.constant.LogBaseState;
import com.resto.shop.web.constant.OrderState;
import com.resto.shop.web.constant.ProductionStatus;
import com.resto.shop.web.datasource.DataSourceContextHolder;
import com.resto.shop.web.model.*;
import com.resto.shop.web.service.*;
import com.resto.shop.web.util.LogTemplateUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.resto.brand.core.util.HttpClient.doPost;

@Component
public class OrderMessageListener implements MessageListener {
    Logger log = LoggerFactory.getLogger(getClass());

    @Resource
    OrderService orderService;

    @Resource
    WechatConfigService wechatConfigService;
    @Resource
    BrandSettingService brandSettingService;
    
    @Resource
    BrandService brandService;
    
    @Resource
    CustomerService customerService;

    @Resource
    CouponService couponService;

    @Resource
    ShareSettingService shareSettingService;

    @Resource
    OrderItemService orderItemService;
    @Resource
    ShopDetailService shopDetailService;

    @Resource
    NewCustomCouponService newcustomcouponService;
    @Resource
    LogBaseService logBaseService;
    @Value("#{propertyConfigurer['orderMsg']}")
    public static String orderMsg;
    @Override
    public Action consume(Message message, ConsumeContext context) {
        Logger log = LoggerFactory.getLogger(getClass());

        log.info("接收到队列消息:" + message.getTag() + "@" + message);
        try {
            return executeMessage(message);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            log.error("字符编码转换错误:" + e.getMessage());
        }
        return Action.CommitMessage;
    }

    public Action executeMessage(Message message) throws UnsupportedEncodingException {
        String tag = message.getTag();
        if (tag.equals(MQSetting.TAG_CANCEL_ORDER)) { //取消订单消息
            return executeCancelOrder(message);
        } else if (tag.equals(MQSetting.TAG_AUTO_CONFIRM_ORDER)) {
            return executeAutoConfirmOrder(message);
        } else if (tag.equals(MQSetting.TAG_NOT_PRINT_ORDER)) {
            return executeChangeProductionState(message);
        } else if (tag.equals(MQSetting.TAG_NOT_ALLOW_CONTINUE)) {
            return executeNotAllowContinue(message);
        } else if (tag.equals(MQSetting.TAG_SHOW_ORDER)) {
            return executeShowComment(message);
        } else if (tag.equals(MQSetting.TAG_AUTO_REFUND_ORDER)) {
            return executeAutoRefundOrder(message);
        } else if (tag.equals(MQSetting.TAG_NOTICE_SHARE_CUSTOMER)) {
            return executeNoticeShareCustomer(message);
        } else if (tag.equals(MQSetting.SEND_CALL_MESSAGE)){
            return executeSendCallMessage(message);
        }else if (tag.equals(MQSetting.TAG_REMIND_MSG)){
        	return executeRemindMsg(message);
        }else if (tag.equals(MQSetting.TAG_AUTO_SEND_REMMEND)){
        	return executeRecommendMsg(message);
        }else if(tag.equals(MQSetting.TAG_BOSS_ORDER)){
            return executeBossOrderMsg(message);
        }
        return Action.CommitMessage;
    }


    private Action executeNoticeShareCustomer(Message message) throws UnsupportedEncodingException {
        String msg = new String(message.getBody(), MQSetting.DEFAULT_CHAT_SET);
        Customer customer = JSONObject.parseObject(msg, Customer.class);
        DataSourceContextHolder.setDataSourceName(customer.getBrandId());
        noticeShareCustomer(customer);
        return Action.CommitMessage;
    }
    
    private Action executeRemindMsg(Message message) throws UnsupportedEncodingException {
    	//就餐提醒的消息队列
        String msg = new String(message.getBody(), MQSetting.DEFAULT_CHAT_SET);
        Order order = JSONObject.parseObject(msg, Order.class);
        DataSourceContextHolder.setDataSourceName(order.getBrandId());
        Customer customer = customerService.selectById(order.getCustomerId());
        WechatConfig config = wechatConfigService.selectByBrandId(order.getBrandId());
        ShopDetail shop = shopDetailService.selectById(order.getShopDetailId());
        WeChatUtils.sendCustomerMsgASync(shop.getPushContext(), customer.getWechatId(), config.getAppid(), config.getAppsecret());
        return Action.CommitMessage;
    }
    
    
    //优惠券过期提前推送消息队列
    private Action executeRecommendMsg(Message message) throws UnsupportedEncodingException {
    	String msg = new String(message.getBody(), MQSetting.DEFAULT_CHAT_SET);
    	JSONObject obj = JSONObject.parseObject(msg);
    	Customer customer = customerService.selectById(obj.getString("id"));
    	WechatConfig config = wechatConfigService.selectByBrandId(customer.getBrandId());
        BrandSetting setting = brandSettingService.selectByBrandId(customer.getBrandId());
        String pr = obj.getString("pr");
        String shopName = obj.getString("shopName");
        String name = obj.getString("name");
        String pushDay = obj.getInteger("pushDay")+"";
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
    	StringBuffer str=new StringBuffer();
        String jumpurl = setting.getWechatWelcomeUrl()+"?subpage=tangshi";
        str.append("优惠券到期提醒"+"\n");
        str.append("<a href='"+jumpurl+"'>"+shopName+"温馨提醒您：您价值"+pr+"元的\""+name+"\""+pushDay+"天后即将到期，快来尝尝我们的新菜吧~</a>");
        WeChatUtils.sendCustomerMsg(str.toString(), customer.getWechatId(), config.getAppid(), config.getAppsecret());//提交推送
        Map map = new HashMap(4);
        map.put("brandName", setting.getBrandName());
        map.put("fileName", customer.getId());
        map.put("type", "UserAction");
        map.put("content", "系统向用户:"+customer.getNickname()+"推送微信消息:"+msg.toString()+",请求服务器地址为:" + MQSetting.getLocalIP());
        doPost(LogUtils.url, map);
        map.put("content","用户:"+customer.getNickname()+"优惠券过期发短信提醒"+"请求地址:"+MQSetting.getLocalIP());
        if(setting.getIsSendCouponMsg() == Common.YES){
            sendNote(shopName,pr,name,pushDay,customer.getId(),map);
        }

        return Action.CommitMessage;
    }
    
  //发送短信
    private void sendNote(String shop,String price,String name,String pushDay,String customerId,Map<String,String>logMap){
        Customer customer=customerService.selectById(customerId);
    	Map param = new HashMap();
        param.put("shop", shop);
		param.put("price", price);
		param.put("name", name);
		param.put("day", pushDay);
        SMSUtils.sendMessage(customer.getTelephone(), new JSONObject(param).toString(), "餐加", "SMS_43790004",logMap);
    }

    //
    private Action executeBossOrderMsg(Message message) throws UnsupportedEncodingException {
        String msg = new String(message.getBody(), MQSetting.DEFAULT_CHAT_SET);
        Order order = JSON.parseObject(msg, Order.class);
        DataSourceContextHolder.setDataSourceName(order.getBrandId());
        log.info("执行自动确认逻辑" + order.getId());
        orderService.confirmBossOrder(order);
        return Action.CommitMessage;
    }

    private void noticeShareCustomer(Customer customer) {
        Customer shareCustomer = customerService.selectById(customer.getShareCustomer());
        ShareSetting shareSetting = shareSettingService.selectValidSettingByBrandId(customer.getBrandId());
        if (shareCustomer != null && shareSetting != null) {
            BigDecimal sum = new BigDecimal(0);
            List<Coupon> couponList = new ArrayList<>();
            //品牌专属优惠券
            List<Coupon> couponList1 = couponService.listCouponByStatus("0", customer.getId(),customer.getBrandId(),null);
            couponList.addAll(couponList1);
            List<ShopDetail> listShop = shopDetailService.selectByBrandId(customer.getBrandId());
            for(ShopDetail s : listShop){
                //店铺专属优惠券
                List<Coupon> couponList2 = couponService.listCouponByStatus("0", customer.getId(),null,s.getId());
                couponList.addAll(couponList2);
            }
            for (Coupon coupon : couponList) {
                sum = sum.add(coupon.getValue());
            }
            StringBuffer msg = new StringBuffer("亲，感谢您的分享，您的好友");
            msg.append(customer.getNickname()).append("已领取").append(sum).append("元红包，")
                    .append(customer.getNickname()).append("如到店消费您将获得").append(shareSetting.getMinMoney())
                    .append("-").append(shareSetting.getMaxMoney()).append("元红包返利");
            WechatConfig config = wechatConfigService.selectByBrandId(customer.getBrandId());
            log.info("异步发送分享注册微信通知ID:" + customer.getShareCustomer() + " 内容:" + msg);
            WeChatUtils.sendCustomerMsgASync(msg.toString(), shareCustomer.getWechatId(), config.getAppid(), config.getAppsecret());
        }

    }

    private Action executeAutoRefundOrder(Message message) throws UnsupportedEncodingException {
        String msg = new String(message.getBody(), MQSetting.DEFAULT_CHAT_SET);
        JSONObject obj = JSONObject.parseObject(msg);
        String brandId = obj.getString("brandId");
        DataSourceContextHolder.setDataSourceName(brandId);
        Order order = orderService.selectById(obj.getString("orderId"));
        ShopDetail shopDetail = shopDetailService.selectById(order.getShopDetailId());
        Brand brand = brandService.selectById(order.getBrandId());
        String customerId = obj.getString("customerId");
        if (orderService.checkRefundLimit(order)) {
            orderService.autoRefundOrder(obj.getString("orderId"));
            log.info("款项自动退还到相应账户:" + obj.getString("orderId"));
            Customer customer = customerService.selectById(customerId);
            WechatConfig config = wechatConfigService.selectByBrandId(brandId);
            StringBuilder sb = new StringBuilder("亲,昨日未消费订单已退款,欢迎下次再来本店消费\n");
            sb.append("订单编号:"+order.getSerialNumber()+"\n");
            if(order.getOrderMode()!=null){
                switch (order.getOrderMode()) {
                    case ShopMode.TABLE_MODE:
                        sb.append("桌号:"+(order.getTableNumber()!=null?order.getTableNumber():"无")+"\n");
                        break;
                    default:
                        sb.append("取餐码："+(order.getVerCode()!=null?order.getVerCode():"无")+"\n");
                        break;
                }
            }
            if( order.getShopName()==null||"".equals(order.getShopName())){
                order.setShopName(shopDetailService.selectById(order.getShopDetailId()).getName());
            }
            sb.append("就餐店铺："+order.getShopName()+"\n");
            sb.append("订单时间："+ DateFormatUtils.format(order.getCreateTime(), "yyyy-MM-dd HH:mm")+"\n");
            sb.append("订单明细：\n");
            List<OrderItem> orderItem  = orderItemService.listByOrderId(order.getId());
            for(OrderItem item : orderItem){
                sb.append("  "+item.getArticleName()+"x"+item.getCount()+"\n");
            }
            sb.append("订单金额："+order.getOrderMoney()+"\n");
            WeChatUtils.sendCustomerMsgASync(sb.toString(), customer.getWechatId(), config.getAppid(), config.getAppsecret());
//            UserActionUtils.writeToFtp(LogType.ORDER_LOG, brand.getBrandName(), shopDetail.getName(), order.getId(),
//                    "订单发送推送：" + msg.toString());
            Map map = new HashMap(4);
            map.put("brandName", brand.getBrandName());
            map.put("fileName", customer.getId());
            map.put("type", "UserAction");
            map.put("content", "系统向用户:"+customer.getNickname()+"推送微信消息:"+msg.toString()+",请求服务器地址为:" + MQSetting.getLocalIP());
            doPost(LogUtils.url, map);
        } else {
            log.info("款项自动退还到相应账户失败，订单状态不是已付款或商品状态不是已付款未下单");
        }

        return Action.CommitMessage;



    }

    private Action executeSendCallMessage(Message message) throws UnsupportedEncodingException {
        String msg = new String(message.getBody(), MQSetting.DEFAULT_CHAT_SET);
        JSONObject obj = JSONObject.parseObject(msg);
        String brandId = obj.getString("brandId");
        DataSourceContextHolder.setDataSourceName(brandId);
        String customerId = obj.getString("customerId");
        Customer customer = customerService.selectById(customerId);
        WechatConfig config = wechatConfigService.selectByBrandId(brandId);
        WeChatUtils.sendCustomerMsgASync("您的餐品已经准备好了，请尽快到吧台取餐！", customer.getWechatId(), config.getAppid(), config.getAppsecret());

        return Action.CommitMessage;
    }


    private Action executeNotAllowContinue(Message message) throws UnsupportedEncodingException {
        String msg = new String(message.getBody(), MQSetting.DEFAULT_CHAT_SET);
        Order order = JSON.parseObject(msg, Order.class);
        Brand brand = brandService.selectById(order.getBrandId());
        ShopDetail shopDetail = shopDetailService.selectById(order.getShopDetailId());
        DataSourceContextHolder.setDataSourceName(order.getBrandId());
        orderService.updateAllowContinue(order.getId(), false);
        UserActionUtils.writeToFtp(LogType.ORDER_LOG, brand.getBrandName(), shopDetail.getName(), order.getId(),
                "订单加菜时间已过期，不允许继续加菜！");
        return Action.CommitMessage;
    }

    private Action executeShowComment(Message message) throws UnsupportedEncodingException {
        String msg = new String(message.getBody(), MQSetting.DEFAULT_CHAT_SET);
        Appraise appraise = JSON.parseObject(msg, Appraise.class);
        DataSourceContextHolder.setDataSourceName(appraise.getBrandId());
        log.info("开始发送分享通知:");
        sendShareMsg(appraise);

        return Action.CommitMessage;
    }

    private void sendShareMsg(Appraise appraise) {
        StringBuffer msg = new StringBuffer("感谢您的评论，将");
        BrandSetting setting = brandSettingService.selectByBrandId(appraise.getBrandId());
        WechatConfig config = wechatConfigService.selectByBrandId(appraise.getBrandId());
        Customer customer = customerService.selectById(appraise.getCustomerId());
        ShareSetting shareSetting = shareSettingService.selectByBrandId(customer.getBrandId());
        log.info("分享人:" + customer.getNickname());
        List<NewCustomCoupon> coupons = newcustomcouponService.selectListByCouponType(customer.getBrandId(), 1, appraise.getShopDetailId());
        BigDecimal money = new BigDecimal("0.00");
        for(NewCustomCoupon coupon : coupons){
            money = money.add(coupon.getCouponValue().multiply(new BigDecimal(coupon.getCouponNumber())));
        }
        if(money.doubleValue() == 0.00 && shareSetting == null){
            msg.append("红包发送给朋友/分享朋友圈，朋友到店消费后，您将获得红包返利\n");
        }else if(money.doubleValue() == 0.00){
            msg.append(money+"元红包发送给朋友/分享朋友圈，朋友到店消费后，您将获得红包返利\n");
        }else if(shareSetting == null){
            msg.append("红包发送给朋友/分享朋友圈，朋友到店消费后，您将获得"+shareSetting.getMinMoney()+"元-"+shareSetting.getMaxMoney()+"元红包返利\n");
        }else{
            msg.append(money +"元红包发送给朋友/分享朋友圈，朋友到店消费后，您将获得"+shareSetting.getMinMoney()+"元-"+shareSetting.getMaxMoney()+"元红包返利\n");
        }
        msg.append("<a href='" + setting.getWechatWelcomeUrl() + "?shopId=" + customer.getLastOrderShop() + "&subpage=home&dialog=share&appraiseId=" + appraise.getId() + "'>立即分享红包</a>");
        log.info("异步发送分享好评微信通知ID:" + appraise.getId() + " 内容:" + msg);
        log.info("ddddd-"+customer.getWechatId()+"dddd-"+config.getAppid()+"dddd-"+config.getAppsecret());
        ShopDetail shopDetail = shopDetailService.selectById(appraise.getShopDetailId());
        WeChatUtils.sendCustomerMsgASync(msg.toString(), customer.getWechatId(), config.getAppid(), config.getAppsecret());
        //logBaseService.insertLogBaseInfoState(shopDetail, customer, appraise.getId(), LogBaseState.SHARE);
        log.info("分享完毕:" );
    }

    private Action executeChangeProductionState(Message message) throws UnsupportedEncodingException {
        String msg = new String(message.getBody(), MQSetting.DEFAULT_CHAT_SET);
        Order order = JSON.parseObject(msg, Order.class);
        DataSourceContextHolder.setDataSourceName(order.getBrandId());
        orderService.changePushOrder(order);
        return Action.CommitMessage;
    }

    private Action executeAutoConfirmOrder(Message message) throws UnsupportedEncodingException {
        String msg = new String(message.getBody(), MQSetting.DEFAULT_CHAT_SET);
        Order order = JSON.parseObject(msg, Order.class);
        DataSourceContextHolder.setDataSourceName(order.getBrandId());
        log.info("执行自动确认逻辑" + order.getId());
        orderService.confirmOrder(order);
        return Action.CommitMessage;
    }

    private Action executeCancelOrder(Message message) throws UnsupportedEncodingException {
        String msg = new String(message.getBody(), MQSetting.DEFAULT_CHAT_SET);
        JSONObject obj = JSONObject.parseObject(msg);
        String brandId = obj.getString("brandId");
        Boolean auto = obj.getBoolean("auto");
        DataSourceContextHolder.setDataSourceName(brandId);
        Order order = orderService.selectById(obj.getString("orderId"));
        ShopDetail shopDetail = shopDetailService.selectById(order.getShopDetailId());
        Brand brand = brandService.selectById(order.getBrandId());
        if (order.getOrderState() == OrderState.SUBMIT) {
            log.info("自动取消订单:" + obj.getString("orderId"));
            orderService.cancelOrder(obj.getString("orderId"));
            LogTemplateUtils.getAutoCancleOrderByOrderType(brand.getBrandName(),order.getId(),auto);
            log.info("款项自动退还到相应账户:" + obj.getString("orderId"));
            Customer customer = customerService.selectById(order.getCustomerId());
            WechatConfig config = wechatConfigService.selectByBrandId(brandId);
            StringBuilder sb = new StringBuilder("亲,今日未完成支付的订单已被系统自动取消,欢迎下次再来本店消费\n");
            sb.append("订单编号:"+order.getSerialNumber()+"\n");
            if(order.getOrderMode()!=null){
                switch (order.getOrderMode()) {
                    case ShopMode.TABLE_MODE:
                        sb.append("桌号:"+(order.getTableNumber()!=null?order.getTableNumber():"无")+"\n");
                        break;
                    default:
                        sb.append("取餐码："+(order.getVerCode()!=null?order.getVerCode():"无")+"\n");
                        break;
                }
            }
            if( order.getShopName()==null||"".equals(order.getShopName())){
                order.setShopName(shopDetailService.selectById(order.getShopDetailId()).getName());
            }
            sb.append("就餐店铺："+order.getShopName()+"\n");
            sb.append("订单时间："+ DateFormatUtils.format(order.getCreateTime(), "yyyy-MM-dd HH:mm")+"\n");
            sb.append("订单明细：\n");
            List<OrderItem> orderItem  = orderItemService.listByOrderId(order.getId());
            for(OrderItem item : orderItem){
                sb.append("  "+item.getArticleName()+"x"+item.getCount()+"\n");
            }
            sb.append("订单金额："+order.getOrderMoney()+"\n");
            WeChatUtils.sendCustomerMsgASync(sb.toString(), customer.getWechatId(), config.getAppid(), config.getAppsecret());
//            UserActionUtils.writeToFtp(LogType.ORDER_LOG, brand.getBrandName(), shopDetail.getName(), order.getId(),
//                    "订单发送推送：" + msg.toString());
            Map map = new HashMap(4);
            map.put("brandName", brand.getBrandName());
            map.put("fileName", customer.getId());
            map.put("type", "UserAction");
            map.put("content", "系统向用户:"+customer.getNickname()+"推送微信消息:"+msg.toString()+",请求服务器地址为:" + MQSetting.getLocalIP());
            doPost(LogUtils.url, map);
        } else {
            log.info("自动取消订单失败，订单状态不是已提交");
        }
        return Action.CommitMessage;
    }


}
