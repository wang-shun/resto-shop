package com.resto.shop.web.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.resto.brand.core.util.MQSetting;
import com.resto.brand.core.util.WeChatUtils;
import com.resto.brand.web.model.*;
import com.resto.brand.web.service.BrandSettingService;
import com.resto.brand.web.service.ShareSettingService;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.brand.web.service.WechatConfigService;
import com.resto.shop.web.constant.OrderState;
import com.resto.shop.web.constant.ProductionStatus;
import com.resto.shop.web.datasource.DataSourceContextHolder;
import com.resto.shop.web.model.*;
import com.resto.shop.web.service.CouponService;
import com.resto.shop.web.service.CustomerService;
import com.resto.shop.web.service.OrderItemService;
import com.resto.shop.web.service.OrderService;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
    CustomerService customerService;

    @Resource
    CouponService couponService;

    @Resource
    ShareSettingService shareSettingService;

    @Resource
    OrderItemService orderItemService;
    @Resource
    ShopDetailService shopDetailService;

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
        DataSourceContextHolder.setDataSourceName(order.getBrandId());
        orderService.updateAllowContinue(order.getId(), false);
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
        StringBuffer msg = new StringBuffer("感谢您的评价 ，分享好友\n");
        BrandSetting setting = brandSettingService.selectByBrandId(appraise.getBrandId());
        WechatConfig config = wechatConfigService.selectByBrandId(appraise.getBrandId());
        Customer customer = customerService.selectById(appraise.getCustomerId());
        log.info("分享人:" + customer.getNickname());
        msg.append("<a href='" + setting.getWechatWelcomeUrl() + "?shopId=" + appraise.getShopDetailId() + "&subpage=home&dialog=share&appraiseId=" + appraise.getId() + "'>再次领取红包</a>");
        log.info("异步发送分享好评微信通知ID:" + appraise.getId() + " 内容:" + msg);
        WeChatUtils.sendCustomerMsgASync(msg.toString(), customer.getWechatId(), config.getAppid(), config.getAppsecret());
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
        DataSourceContextHolder.setDataSourceName(brandId);
        Order order = orderService.selectById(obj.getString("orderId"));
        if (order.getOrderState() == OrderState.SUBMIT) {
            log.info("自动取消订单:" + obj.getString("orderId"));
            orderService.cancelOrder(obj.getString("orderId"));
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
        } else {
            log.info("自动取消订单失败，订单状态不是已提交");
        }
        return Action.CommitMessage;
    }


}
