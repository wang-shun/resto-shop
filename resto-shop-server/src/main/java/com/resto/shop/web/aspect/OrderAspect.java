package com.resto.shop.web.aspect;

import com.resto.brand.core.entity.JSONResult;
import com.resto.brand.core.util.*;
import com.resto.brand.web.model.*;
import com.resto.brand.web.service.*;
import com.resto.shop.web.constant.*;
import com.resto.shop.web.container.OrderProductionStateContainer;
import com.resto.shop.web.exception.AppException;
import com.resto.shop.web.model.*;
import com.resto.shop.web.producer.MQMessageProducer;
import com.resto.shop.web.service.*;
import com.resto.shop.web.util.LogTemplateUtils;
import com.resto.shop.web.util.RedisUtil;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.resto.brand.core.util.HttpClient.doPostAnsc;

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
    RedConfigService redConfigService;
    @Resource
    OrderService orderService;
    @Resource
    OrderPaymentItemService orderPaymentItemService;
    @Resource
    LogBaseService logBaseService;
    @Resource
    NewCustomCouponService newcustomcouponService;
    @Resource
    BrandService brandService;

    @Pointcut("execution(* com.resto.shop.web.service.OrderService.createOrder(..))")
    public void createOrder() {
    }


    @Pointcut("execution(* com.resto.shop.web.service.OrderService.createOrderByEmployee(..))")
    public void createOrderByEmployee() {
    }

    @AfterReturning(value = "createOrderByEmployee()", returning = "jsonResult")
    public void createOrderByEmployeeAround(JSONResult jsonResult) throws Throwable {
        if (jsonResult.isSuccess() == true) {
            Order order = (Order) jsonResult.getData();
            shopCartService.clearShopCartGeekPos(String.valueOf(order.getEmployeeId()), order.getShopDetailId());
            //出单时减少库存
            Boolean updateStockSuccess = false;
            updateStockSuccess = orderService.updateStock(orderService.getOrderInfo(order.getId()));
            if (!updateStockSuccess) {
                log.info("库存变更失败:" + order.getId());
            }
        }
    }

    @AfterReturning(value = "createOrder()", returning = "jsonResult")
    public void createOrderAround(JSONResult jsonResult) throws Throwable {
        String time = DateUtil.formatDate(new Date(), "yyyy-MM-dd hh:mm:ss");
        if (jsonResult.isSuccess() == true) {
            Order order = (Order) jsonResult.getData();
            log.info("(createOrderAround)创建订单时候订单状态为：orderstate：" + order.getOrderState() + "production：" + order.getProductionStatus() + "订单id：" + order.getId() + "当前时间为：" + time);
            if (order.getCustomerId().equals("0")) {
                //pos端点餐
                MQMessageProducer.sendPlaceOrderMessage(order);
                String shopId = order.getShopDetailId();
                Integer orderCount = (Integer) RedisUtil.get(shopId + "shopOrderCount");
                BigDecimal orderTotal = (BigDecimal) RedisUtil.get(shopId + "shopOrderTotal");
                if(orderCount == null){
                    orderCount = 0;
                }
                if(orderTotal == null){
                    orderTotal = BigDecimal.valueOf(0);
                }
                orderCount++;
                orderTotal = orderTotal.add(order.getOrderMoney());
                RedisUtil.set(shopId + "shopOrderCount", orderCount);
                RedisUtil.set(shopId + "shopOrderTotal", orderTotal);
                MQMessageProducer.sendPrintSuccess(shopId);
                return;
            }


            ShopDetail shopDetail = shopDetailService.selectByPrimaryKey(order.getShopDetailId());
            log.info("tttttttttttttt----------");
            if(shopDetail.getPosVersion() == PosVersion.VERSION_2_0){
                MQMessageProducer.sendCreateOrderMessage(order);
            }

            if (order.getPayMode() != PayMode.WEIXIN_PAY) {
                shopCartService.clearShopCart(order.getCustomerId(), order.getShopDetailId());
            }
//            现金银联闪惠支付应该在pos上确认订单已收款后在进行出单
            if (order.getPayMode() == OrderPayMode.YL_PAY || order.getPayMode() == OrderPayMode.XJ_PAY || order.getPayMode() == OrderPayMode.SHH_PAY || order.getPayMode() == OrderPayMode.JF_PAY) {
                MQMessageProducer.sendPlaceOrderNoPayMessage(order);
            }

            if (order.getOrderMode() == ShopMode.BOSS_ORDER && order.getPayType() == PayType.NOPAY) {
//                shopCartService.clearShopCart(order.getCustomerId(), order.getShopDetailId());
                MQMessageProducer.sendPlaceOrderMessage(order);
            }
            if (order.getDistributionModeId() == 2 && order.getOrderState() == OrderState.PAYMENT) {
                BrandSetting setting = brandSettingService.selectByBrandId(order.getBrandId());
                MQMessageProducer.sendPlatformOrderMessage(order.getId(), PlatformType.R_VERSION, order.getBrandId(), order.getShopDetailId());
                MQMessageProducer.sendAutoConfirmOrder(order, setting.getAutoConfirmTime() * 1000);
            }

            //自动取消订单，大boss模式下  先付2小时未付款 自动取消订单
//            if (order.getOrderState().equals(OrderState.SUBMIT) && order.getOrderMode() == ShopMode.BOSS_ORDER && order.getPayType() == PayType.PAY) {//未支付和未完全支付的订单，不包括后付款模式
//                long delay = 1000*60*60*2;//两个小时后自动取消订单
//                MQMessageProducer.sendAutoCloseMsg(order.getId(),order.getBrandId(),delay);
//            }

            //出单时减少库存
            Boolean updateStockSuccess = false;
            updateStockSuccess = orderService.updateStock(orderService.getOrderInfo(order.getId()));
            if (!updateStockSuccess) {
                log.info("库存变更失败:" + order.getId());
            }

        }
    }

    private void sendPaySuccessMsg(Order order) {
        Customer customer = customerService.selectById(order.getCustomerId());
        if (customer == null) {
            return;
        }
        WechatConfig config = wechatConfigService.selectByBrandId(customer.getBrandId());
        StringBuffer msg = new StringBuffer();
        msg.append("订单编号:\n" + order.getSerialNumber() + "\n");
        if (order.getOrderMode() != null) {
            switch (order.getOrderMode()) {
                case ShopMode.TABLE_MODE:
                    msg.append("桌号:" + order.getTableNumber() + "\n");
                    break;
                case ShopMode.BOSS_ORDER:
                    msg.append("桌号:" + order.getTableNumber() + "\n");
                    break;
                default:
                    msg.append("消费码：" + order.getVerCode() + "\n");
                    break;
            }
        }
        Brand brand = brandService.selectById(order.getBrandId());
        ShopDetail shopDetail = shopDetailService.selectById(order.getShopDetailId());
        if (order.getShopName() == null || "".equals(order.getShopName())) {
            order.setShopName(shopDetail.getName());
        }
        msg.append("店铺名：" + order.getShopName() + "\n");
        msg.append("订单时间：" + DateFormatUtils.format(order.getCreateTime(), "yyyy-MM-dd HH:mm") + "\n");

        BrandSetting setting = brandSettingService.selectByBrandId(order.getBrandId());
        if (setting.getIsUseServicePrice() == 1 && shopDetail.getIsUseServicePrice() == 1 && order.getServicePrice().compareTo(BigDecimal.ZERO) != 0 && order.getDistributionModeId() == 1) {
            msg.append(shopDetail.getServiceName() + "：" + order.getServicePrice() + "\n");
        }
        if (setting.getIsMealFee() == 1 && order.getMealFeePrice().compareTo(BigDecimal.ZERO) != 0 && order.getDistributionModeId() == 3 && shopDetail.getIsMealFee() == 1) {
            msg.append(shopDetail.getMealFeeName() + "：" + order.getMealFeePrice() + "\n");
        }
        msg.append("订单明细：\n");
        List<OrderItem> orderItem = orderItemService.listByOrderId(order.getId());
        for (OrderItem item : orderItem) {
            msg.append("  " + item.getArticleName() + "x" + item.getCount() + "\n");
        }
        msg.append("订单金额：" + order.getOrderMoney() + "\n");
        if (order.getOrderMode() == ShopMode.BOSS_ORDER) {
            String url = "";
            if (order.getParentOrderId() == null) {
                url = setting.getWechatWelcomeUrl() + "?orderBossId=" + order.getId() + "&dialog=closeRedPacket&shopId=" + order.getShopDetailId();
            } else {
                Order o = orderService.selectById(order.getParentOrderId());
                url = setting.getWechatWelcomeUrl() + "?orderBossId=" + o.getId() + "&dialog=closeRedPacket&shopId=" + order.getShopDetailId();
            }
            msg.append("<a href='" + url + "'>点击这里进行\"加菜\"或\"买单\"</a> \n");
        }
        try {
            String result = WeChatUtils.sendCustomerMsg(msg.toString(), customer.getWechatId(), config.getAppid(), config.getAppsecret());
//            log.info("订单支付完成后，发送客服消息:" + order.getId() + " -- " + result);
//            UserActionUtils.writeToFtp(LogType.ORDER_LOG, brand.getBrandName(), shopDetail.getName(), order.getId(),
//                    "订单发送推送：" + msg.toString());
            Map map = new HashMap(4);
            map.put("brandName", brand.getBrandName());
            map.put("fileName", customer.getId());
            map.put("type", "UserAction");
            map.put("content", "系统向用户:" + customer.getNickname() + "推送微信消息:" + msg.toString() + ",请求服务器地址为:" + MQSetting.getLocalIP());
            doPostAnsc(LogUtils.url, map);
        } catch (Exception e) {
            log.error("发送客服消息失败:" + e.getMessage());
        }

//        Customer customer = customerService.selectById(order.getCustomerId());
//        WechatConfig config = wechatConfigService.selectByBrandId(order.getBrandId());
//        ShopDetail shop = shopDetailService.selectById(order.getShopDetailId());
//
//        String res = WeChatUtils.getTemplate("OPENTM408705883", config.getAppid(), config.getAppsecret());
//        JSONObject access = new JSONObject(res);
//        String templateId = access.optString("template_id");
//
//        String jumpUrl = "http://www.baidu.com";
//        Map<String, Map<String, Object>> content = new HashMap<String, Map<String, Object>>();
//        Map<String, Object> first = new HashMap<String, Object>();
//
//        if(order.getParentOrderId() == null){
//            first.put("value", "下单成功！\n您于"+DateUtil.formatDate(order.getCreateTime(),"yyyy-MM-dd HH:mm:ss")+"的订单已下厨，请稍候~");
//        }else{
//            first.put("value", "加菜成功！\n您于"+DateUtil.formatDate(order.getCreateTime(),"yyyy-MM-dd HH:mm:ss")+"的订单已下厨，请稍候~");
//        }
//        first.put("color", "#00DB00");
//        Map<String, Object> keyword1 = new HashMap<String, Object>();
//        keyword1.put("value", order.getSerialNumber());
//        keyword1.put("color", "#000000");
//        Map<String, Object> keyword2 = new HashMap<String, Object>();
//        keyword2.put("value", shop.getName());
//        keyword2.put("color", "#000000");
//        Map<String, Object> keyword3 = new HashMap<String, Object>();
//        if(order.getOrderMode() == 2){
//            keyword3.put("value", order.getVerCode());
//        }else{
//            keyword3.put("value", order.getTableNumber());
//        }
//        keyword3.put("color", "#000000");
//        Map<String, Object> keyword4 = new HashMap<String, Object>();
//        keyword4.put("value", "￥" + order.getOrderState());
//        keyword4.put("color", "#000000");
//        Map<String, Object> keyword5 = new HashMap<String, Object>();
//        List<OrderItem> orderItem = orderItemService.listByOrderId(order.getId());
//        StringBuffer msg = new StringBuffer();
//        for (int i=0; i< orderItem.size(); i++) {
//            OrderItem item = orderItem.get(i);
//            if(i == 0){
//                msg.append(" " + item.getArticleName() + "x" + item.getCount() + "\n");
//            }else{
//                msg.append("\t\t\t" + item.getArticleName() + "x" + item.getCount() + "\n");
//            }
//        }
//        keyword5.put("value", msg.toString());
//        keyword5.put("color", "#000000");
//        Map<String, Object> remark = new HashMap<String, Object>();
//        remark.put("value", "点击结果进行\"加菜\"或\"买单\"");
//        remark.put("color", "#173177");
//        content.put("first", first);
//        content.put("keyword1", keyword1);
//        content.put("keyword2", keyword2);
//        content.put("keyword3", keyword3);
//        content.put("keyword4", keyword4);
//        content.put("keyword5", keyword5);
//        content.put("remark", remark);
//        String result = WeChatUtils.sendTemplate(customer.getWechatId(), templateId, jumpUrl, content, config.getAppid(), config.getAppsecret());
//
//        String data = WeChatUtils.delTemplate(templateId, config.getAppid(), config.getAppsecret());
    }

    @Pointcut("execution(* com.resto.shop.web.service.OrderService.orderWxPaySuccess(..))")
    public void orderWxPaySuccess() {
    }

    ;

    @Pointcut("execution(* com.resto.shop.web.service.OrderService.orderAliPaySuccess(..))")
    public void orderAliPaySuccess() {
    }

    ;

    @Pointcut("execution(* com.resto.shop.web.service.OrderService.afterPay(..))")
    public void afterPay() {

    }

    ;


    @Pointcut("execution(* com.resto.shop.web.service.OrderService.confirmOrderPos(..))")
    public void confirmOrderPos() {

    }

    ;


    @AfterReturning(value = "confirmOrderPos()", returning = "order")
    public void confirmOrderPos(Order order) throws AppException {
        BrandSetting setting = brandSettingService.selectByBrandId(order.getBrandId());
        MQMessageProducer.sendNotAllowContinueMessage(order, 1000 * setting.getCloseContinueTime()); //延迟两小时，禁止继续加菜
        Order o = orderService.getOrderAccount(order.getShopDetailId());
        RedisUtil.set(order.getShopDetailId() + "shopOrderCount", o.getOrderCount());
        RedisUtil.set(order.getShopDetailId() + "shopOrderTotal", o.getOrderTotal());
        MQMessageProducer.sendPrintSuccess(order.getShopDetailId());
        //在pos端确认的情况下无需再去修改订单了
//        if (order.getPayMode() == OrderPayMode.XJ_PAY || order.getPayMode() == OrderPayMode.YL_PAY || order.getPayMode() == OrderPayMode.SHH_PAY || order.getPayMode() == OrderPayMode.JF_PAY) {
//            orderService.pushOrder(order.getId());
//        }
    }

    @AfterReturning(value = "afterPay()", returning = "order")
    public void afterPay(Order order) {
        if (order.getPayMode() != OrderPayMode.ALI_PAY) { //已支付
            MQMessageProducer.sendPlaceOrderMessage(order);
        }
        if (order.getOrderState() == OrderState.PAYMENT) {
            String shopId = order.getShopDetailId();
            Integer orderCount = (Integer) RedisUtil.get(shopId + "shopOrderCount");
            BigDecimal orderTotal = (BigDecimal) RedisUtil.get(shopId + "shopOrderTotal");
            if(orderCount == null){
                orderCount = 0;
            }
            if(orderTotal == null){
                orderTotal = BigDecimal.valueOf(0);
            }
            if (order.getParentOrderId() == null) {
                orderCount++;
            }
            orderTotal = orderTotal.add(order.getOrderMoney());
            RedisUtil.set(shopId + "shopOrderCount", orderCount);
            RedisUtil.set(shopId + "shopOrderTotal", orderTotal);
            MQMessageProducer.sendPrintSuccess(shopId);
        }
        if(order.getPayMode() != OrderPayMode.WX_PAY || order.getPayMode() != OrderPayMode.ALI_PAY){
            MQMessageProducer.sendOrderPay(order);
        }
    }

    @Pointcut("execution(* com.resto.shop.web.service.OrderService.posPayOrder(..))")
    public void posPayOrder() {

    }

    ;

    @AfterReturning(value = "posPayOrder()", returning = "order")
    public void posPayOrder(Order order) {
//        WeChatUtils.sendCustomerMsgASync("测试测试！", "oBHT9sqENykH4eDytavxr7nlmeKs", "wx36bd5b9b7d264a8c", "807530431fe6e19e3f2c4a7d1a149465");
        MQMessageProducer.sendPlaceOrderNoPayMessage(order);
    }

    @AfterReturning(value = "orderWxPaySuccess()", returning = "order")
    public void orderPayAfter(Order order) {
//        if (order != null && order.getOrderState().equals(OrderState.PAYMENT) &&
//                (ShopMode.TABLE_MODE != order.getOrderMode() || ShopMode.BOSS_ORDER != order.getOrderMode())) {//坐下点餐模式不发送该消息
//            sendPaySuccessMsg(order);
//        }

        //R+外卖走消息队列  (订单不为空 支付模式不为空  支付为微信或者支付宝支付  已支付  已下单 外卖模式)
        if (order != null && order.getPayMode() != null && (order.getPayMode() == OrderPayMode.WX_PAY || order.getPayMode() == OrderPayMode.ALI_PAY) &&
                order.getOrderState().equals(OrderState.PAYMENT) && !order.getProductionStatus().equals(ProductionStatus.PRINTED) && order.getDistributionModeId() == 2) {
            BrandSetting setting = brandSettingService.selectByBrandId(order.getBrandId());
            MQMessageProducer.sendPlatformOrderMessage(order.getId(), PlatformType.R_VERSION, order.getBrandId(), order.getShopDetailId());
            MQMessageProducer.sendAutoConfirmOrder(order, setting.getAutoConfirmTime() * 1000);
            return;
        }

        //订单不为空 支付模式不为空  支付为支付宝支付  已支付  已下单
        if (order != null && order.getPayMode() != null && order.getPayMode() == OrderPayMode.ALI_PAY &&
                order.getOrderState().equals(OrderState.PAYMENT)
                && order.getProductionStatus().equals(ProductionStatus.HAS_ORDER)) {
            if (order.getPayType() == PayType.NOPAY) {
                order.setPrintTimes(1);//打印次数
                orderService.update(order);
            }
            /*if(order.getDistributionModeId()==2&&order.getOrderState()==OrderState.PAYMENT){
                MQMessageProducer.sendPlatformOrderMessage(order.getId(), 4, order.getBrandId(), order.getShopDetailId());
            }else{*/
            MQMessageProducer.sendPlaceOrderMessage(order);
            //}
        }

        ShopDetail shopDetail = shopDetailService.selectByPrimaryKey(order.getShopDetailId());
        //订单不为空  已支付   桌号不为空或者外带   坐下点餐模式或者大Boss模式
        if (order != null && order.getOrderState() == OrderState.PAYMENT
                && (order.getTableNumber() != null || (order.getDistributionModeId() == DistributionType.TAKE_IT_SELF && shopDetail.getContinueOrderScan() == Common.NO))
                && (order.getOrderMode() == ShopMode.TABLE_MODE || order.getOrderMode() == ShopMode.BOSS_ORDER)) {
            if (order.getPayType() == PayType.NOPAY) {
                order.setPrintTimes(1);
                orderService.update(order);
            }
            MQMessageProducer.sendPlaceOrderMessage(order);
        }

        if (order.getOrderMode() != ShopMode.HOUFU_ORDER) {
            shopCartService.clearShopCart(order.getCustomerId(), order.getShopDetailId());
        }

        //订单不为空  已支付  电视叫号模式
        if (order != null && order.getOrderState() == OrderState.PAYMENT
                && order.getOrderMode() == ShopMode.CALL_NUMBER) {
                MQMessageProducer.sendPlaceOrderMessage(order);
        }

        //稍后支付  大Boss模式  支付宝或微信支付
        if (order.getPayType() == PayType.NOPAY && order.getOrderMode() == ShopMode.BOSS_ORDER && (order.getPayMode() == OrderPayMode.WX_PAY || order.getPayMode() == OrderPayMode.ALI_PAY)) {
            /*if(order.getDistributionModeId()==2&&order.getOrderState()==OrderState.PAYMENT){
                MQMessageProducer.sendPlatformOrderMessage(order.getId(), 4, order.getBrandId(), order.getShopDetailId());
            }else{*/
            MQMessageProducer.sendAutoConfirmOrder(order, 5 * 1000);
            //}
            Order o = orderService.getOrderAccount(order.getShopDetailId());
            RedisUtil.set(order.getShopDetailId() + "shopOrderCount", o.getOrderCount());
            RedisUtil.set(order.getShopDetailId() + "shopOrderTotal", o.getOrderTotal());
            MQMessageProducer.sendPrintSuccess(order.getShopDetailId());
//            orderService.confirmOrder(order);
        }
        if(shopDetail.getPosVersion() == PosVersion.VERSION_2_0){
            MQMessageProducer.sendOrderPay(order);
        }
    }

    public static void main(String[] args) {
        Order order = new Order();
        order.setId("00b8a27437cf460c93910bdc2489d061");
        order.setBrandId("31946c940e194311b117e3fff5327215");
        order.setShopDetailId("31164cebcc4b422685e8d9a32db12ab8");
        MQMessageProducer.sendPlaceOrderMessage(order);
    }

    @Pointcut("execution(* com.resto.shop.web.service.OrderService.pushOrder(..))")
    public void pushOrder() {
    }


    @Pointcut("execution(* com.resto.shop.web.service.OrderService.callNumber(..))")
    public void callNumber() {
    }


    @Pointcut("execution(* com.resto.shop.web.service.OrderService.printSuccess(..))")
    public void printSuccess() {
    }


    @Pointcut("execution(* com.resto.shop.web.service.AccountService.houFuPayOrder(..))")
    public void houFuPayOrder() {
    }


    @Pointcut("execution(* com.resto.shop.web.service.OrderService.payOrderModeFive(..))")
    public void payOrderModeFive() {
    }


    @Pointcut("execution(* com.resto.shop.web.service.OrderService.payOrderWXModeFive(..))")
    public void payOrderWXModeFive() {
    }


    @Pointcut("execution(* com.resto.shop.web.service.OrderService.payPrice(..))")
    public void payPrice() {
    }


    @AfterReturning(value = "callNumber()", returning = "order")
    public void createCallMessage(Order order) throws Throwable {
        Customer customer = customerService.selectById(order.getCustomerId());
        WechatConfig config = wechatConfigService.selectByBrandId(order.getBrandId());
        Brand brand = brandService.selectById(order.getBrandId());
        ShopDetail shopDetail = shopDetailService.selectById(order.getShopDetailId());
        ;//根据订单找到对应的店铺
        if (customer != null) {
            WeChatUtils.sendCustomerMsgASync("您的餐品已经准备好了，请尽快到吧台取餐！", customer.getWechatId(), config.getAppid(), config.getAppsecret());
//        UserActionUtils.writeToFtp(LogType.ORDER_LOG, brand.getBrandName(), shopDetail.getName(), order.getId(),
//                "订单发送推送：您的餐品已经准备好了，请尽快到吧台取餐！");
            Map map = new HashMap(4);
            map.put("brandName", brand.getBrandName());
            map.put("fileName", customer.getId());
            map.put("type", "UserAction");
            map.put("content", "系统向用户:" + customer.getNickname() + "推送微信消息:您的餐品已经准备好了，请尽快到吧台取餐！,请求服务器地址为:" + MQSetting.getLocalIP());
            doPostAnsc(LogUtils.url, map);
        }

//        WeChatUtils.sendCustomerWaitNumberMsg("您的餐品已经准备好了，请尽快到吧台取餐！", customer.getWechatId(), config.getAppid(), config.getAppsecret());
//		MQMessageProducer.sendCallMessage(order.getBrandId(),order.getId(),order.getCustomerId());

        LogTemplateUtils.getCallNumber(brand.getBrandName(), order.getId());
        if (shopDetail.getIsPush() == Common.YES) { //开启就餐提醒
            MQMessageProducer.sendRemindMsg(order, shopDetail.getPushTime() * 1000);
        }

    }

    @AfterReturning(value = "pushOrder()||callNumber()||printSuccess()||payOrderModeFive()||payPrice()|| createOrderByEmployee()||payOrderWXModeFive()", argNames = "joinPoint,order", returning = "order")
    public void pushOrderAfter(JoinPoint joinPoint, Order order) throws Throwable {
        log.info("切面pushOrderAfter" + joinPoint.getSignature().getName());
        if (order != null) {
            if (ProductionStatus.HAS_ORDER == order.getProductionStatus()) {
                if (order.getPayMode() != null && order.getPayMode() == OrderPayMode.ALI_PAY && order.getOrderState().equals(OrderState.SUBMIT)) {
                    return;
                }
//                BrandSetting setting = brandSettingService.selectByBrandId(order.getBrandId());
                log.info("客户下单,发送成功下单通知" + order.getId());

                if (order.getEmployeeId() == null) {
                    if (order.getPrintOrderTime() == null) {
                        if (order.getPayMode() == OrderPayMode.YL_PAY || order.getPayMode() == OrderPayMode.XJ_PAY || order.getPayMode() == OrderPayMode.SHH_PAY || order.getPayMode() == OrderPayMode.JF_PAY) {
                            MQMessageProducer.sendPlaceOrderNoPayMessage(order);
                        } else {
                            MQMessageProducer.sendPlaceOrderMessage(order);
                        }
                    }
                } else {
                    if (order.getOrderState().equals(OrderState.PAYMENT)) {
                        if (order.getPayMode() == OrderPayMode.YL_PAY || order.getPayMode() == OrderPayMode.XJ_PAY || order.getPayMode() == OrderPayMode.SHH_PAY || order.getPayMode() == OrderPayMode.JF_PAY) {
                            MQMessageProducer.sendPlaceOrderNoPayMessage(order);
                        } else {
                            MQMessageProducer.sendPlaceOrderMessage(order);
                        }
                    }
                }

//				log.info("客户下单，添加自动拒绝5分钟未打印的订单");
//				MQMessageProducer.sendNotPrintedMessage(order,1000*60*5); //延迟五分钟，检测订单是否已经打印
//                if ((order.getOrderMode() == ShopMode.TABLE_MODE || order.getOrderMode() == ShopMode.BOSS_ORDER) && order.getEmployeeId() == null) {  //坐下点餐在立即下单的时候，发送支付成功消息通知
//                    log.info("坐下点餐在立即下单的时候，发送支付成功消息通知:" + order.getId());
//                    sendPaySuccessMsg(order);
//                }
                log.info("检查打印异常");
//                int times = setting.getReconnectTimes();
//                int seconds = setting.getReconnectSecond();
//                for (int i = 0; i < times; i++) {
//                    MQMessageProducer.checkPlaceOrderMessage(order, (i + 1) * seconds * 1000L, seconds * times * 1000L);
//                }
            } else if (ProductionStatus.PRINTED == order.getProductionStatus()) {
                BrandSetting setting = brandSettingService.selectByBrandId(order.getBrandId());
                log.info("发送禁止加菜:" + setting.getCloseContinueTime() + "s 后发送");
                if (order.getOrderMode() == ShopMode.BOSS_ORDER && order.getPayType() == PayType.PAY) {
//                    MQMessageProducer.sendNotAllowContinueMessage(order, 1000 * setting.getCloseContinueTime()); //延迟禁止继续加菜
//                    if(order.getOrderState() == OrderState.SUBMIT){
//                        MQMessageProducer.sendAutoConfirmOrder(order, setting.getAutoConfirmTime() * 1000*2);
//                    }else{
//                        MQMessageProducer.sendAutoConfirmOrder(order, setting.getAutoConfirmTime() * 1000);
//                    }
                    if (setting.getAutoConfirmTime() <= setting.getCloseContinueTime()) {    //加菜时间跟领取红包时间对比
                        if (order.getOrderState() == OrderState.SUBMIT) {   //是否买单
                            MQMessageProducer.sendNotAllowContinueMessage(order, 1000 * setting.getCloseContinueTime()); //延迟禁止继续加菜
                        } else if (order.getOrderState() == OrderState.PAYMENT) {
                            MQMessageProducer.sendBossOrder(order, setting.getCloseContinueTime() * 1000 - 10000);
                        }
                    } else {
                        MQMessageProducer.sendNotAllowContinueMessage(order, 1000 * setting.getCloseContinueTime()); //延迟禁止继续加菜
                        MQMessageProducer.sendAutoConfirmOrder(order, setting.getAutoConfirmTime() * 1000);
                    }
                } else if (order.getOrderMode() == ShopMode.BOSS_ORDER && order.getPayType() == PayType.NOPAY) {
                    MQMessageProducer.sendNotAllowContinueMessage(order, 1000 * setting.getCloseContinueTime()); //延迟禁止继续加菜
                } else if (order.getOrderMode() != ShopMode.HOUFU_ORDER) {
                    MQMessageProducer.sendNotAllowContinueMessage(order, 1000 * setting.getCloseContinueTime()); //延迟禁止继续加菜
                    MQMessageProducer.sendPlaceOrderMessage(order);
                    MQMessageProducer.sendAutoConfirmOrder(order, setting.getAutoConfirmTime() * 1000);
                } else {
                    if (order.getOrderState() == OrderState.PAYMENT) {
                        MQMessageProducer.sendAutoConfirmOrder(order, setting.getAutoConfirmTime() * 1000);
                        MQMessageProducer.sendModelFivePaySuccess(order);
                        if (order.getPrintTimes() == 0) {
                            order.setPrintTimes(order.getPrintTimes() + 1);
                            orderService.update(order);
                            MQMessageProducer.sendPlaceOrderMessageAgain(order, 6000);
                        }
                    }
                }
                if (order.getPrintTimes() == 0) {
                    sendPaySuccessMsg(order);
                }

                if (order.getOrderMode() != null) {
                    switch (order.getOrderMode()) {
                        case ShopMode.CALL_NUMBER:
                            log.info("叫号模式,发送取餐码信息:" + order.getId());
                            sendVerCodeMsg(order);
                            break;
                        default:
                            break;
                    }
                }
                log.info("发送打印信息");
                log.info("打印成功后，发送自动确认订单通知！" + setting.getAutoConfirmTime() + "s 后发送" + ",orderId:" + order.getId());
            } else if (ProductionStatus.HAS_CALL == order.getProductionStatus()) {
                log.info("发送叫号信息");
                MQMessageProducer.sendPlaceOrderMessage(order);
            }
        }
    }

    @Pointcut("execution(* com.resto.shop.web.service.OrderService.confirmOrder(..))")
    public void confirmOrder() {
    }

    ;

    @Pointcut("execution(* com.resto.shop.web.service.OrderService.confirmWaiMaiOrder(..))")
    public void confirmWaiMaiOrder() {
    }

    ;

    @Pointcut("execution(* com.resto.shop.web.service.OrderService.confirmBossOrder(..))")
    public void confirmBossOrder() {
    }

    ;

    @AfterReturning(value = "printSuccess()", returning = "order")
    public void pushContent(Order order) {
        if (order.getPayType() == PayType.PAY && (order.getPayMode() == OrderPayMode.YUE_PAY || order.getPayMode() == OrderPayMode.WX_PAY
                || order.getPayMode() == OrderPayMode.ALI_PAY)) {
            String shopId = order.getShopDetailId();
            if (!MemcachedUtils.add(order.getId(), 1)) {
                Integer orderCount = (Integer) RedisUtil.get(shopId + "shopOrderCount");
                BigDecimal orderTotal = (BigDecimal) RedisUtil.get(shopId + "shopOrderTotal");
                if(orderCount == null){
                    orderCount = 0;
                }
                if(orderTotal == null){
                    orderTotal = BigDecimal.valueOf(0);
                }
                if (order.getParentOrderId() == null) {
                    orderCount++;
                }
                orderTotal = orderTotal.add(order.getOrderMoney());
                RedisUtil.set(shopId + "shopOrderCount", orderCount);
                RedisUtil.set(shopId + "shopOrderTotal", orderTotal);
                MQMessageProducer.sendPrintSuccess(shopId);
            }

        }

        if (order != null
                && (order.getOrderMode() == ShopMode.HOUFU_ORDER)
                && order.getOrderState() == OrderState.SUBMIT
                && order.getProductionStatus() == ProductionStatus.PRINTED) {
            Customer customer = customerService.selectById(order.getCustomerId());
            WechatConfig config = wechatConfigService.selectByBrandId(customer.getBrandId());
            ShopDetail shopDetail = shopDetailService.selectById(order.getShopDetailId());
            Brand brand = brandService.selectById(order.getBrandId());
            StringBuffer msg = new StringBuffer();
            if (order.getParentOrderId() == null) {
                msg.append("下单成功!" + "\n");
            } else {
                msg.append("加菜成功!" + "\n");
            }
            msg.append("订单编号:" + order.getSerialNumber() + "\n");
            msg.append("桌号：" + order.getTableNumber() + "\n");
            msg.append("店铺名：" + shopDetail.getName() + "\n");
            msg.append("订单时间：" + DateFormatUtils.format(order.getCreateTime(), "yyyy-MM-dd HH:mm") + "\n");
            BrandSetting setting = brandSettingService.selectByBrandId(order.getBrandId());
            if (setting.getIsUseServicePrice() == 1 && shopDetail.getIsUseServicePrice() == 1 && order.getDistributionModeId() == 1) {
                msg.append(shopDetail.getServiceName() + "：" + order.getServicePrice() + "\n");
            }
            if (setting.getIsMealFee() == 1 && order.getDistributionModeId() == 3 && shopDetail.getIsMealFee() == 1) {
                msg.append(shopDetail.getMealFeeName() + "：" + order.getMealFeePrice() + "\n");
            }
            BigDecimal sum = order.getOrderMoney();
            List<Order> orders = orderService.selectByParentId(order.getId(), order.getPayType()); //得到子订单
            for (Order child : orders) { //遍历子订单
                sum = sum.add(child.getOrderMoney());
            }
            msg.append("订单明细：\n");
            List<OrderItem> orderItem = orderItemService.listByOrderId(order.getId());
            for (OrderItem item : orderItem) {
                msg.append("  " + item.getArticleName() + "x" + item.getCount() + "\n");
            }
            msg.append("订单金额：" + sum + "\n");
            if (order.getOrderMode() == ShopMode.BOSS_ORDER && order.getPayMode() != 3 && order.getPayMode() != 4) {
                String url = "";
                if (order.getParentOrderId() == null) {
                    url = setting.getWechatWelcomeUrl() + "?orderBossId=" + order.getId() + "&dialog=closeRedPacket&shopId=" + order.getShopDetailId();
                } else {
                    Order o = orderService.selectById(order.getParentOrderId());
                    url = setting.getWechatWelcomeUrl() + "?orderBossId=" + o.getId() + "&dialog=closeRedPacket&shopId=" + order.getShopDetailId();
                }
                msg.append("<a href='" + url + "'>点击这里进行\"加菜\"或\"买单\"</a> \n");
            }
            String result = WeChatUtils.sendCustomerMsg(msg.toString(), customer.getWechatId(), config.getAppid(), config.getAppsecret());
//            UserActionUtils.writeToFtp(LogType.ORDER_LOG, brand.getBrandName(), shopDetail.getName(), order.getId(),
//                    "订单发送推送：" + msg.toString());
            Map map = new HashMap(4);
            map.put("brandName", brand.getBrandName());
            map.put("fileName", customer.getId());
            map.put("type", "UserAction");
            map.put("content", "系统向用户:" + customer.getNickname() + "推送微信消息:" + msg.toString() + ",请求服务器地址为:" + MQSetting.getLocalIP());
            doPostAnsc(LogUtils.url, map);
        }
    }

    //推送分享领红包，跳转到我的二维码界面
    public void scanaQRcode(WechatConfig config, Customer customer, BrandSetting setting, Order order) {
        StringBuffer str = new StringBuffer();
        Brand brand = brandService.selectById(order.getBrandId());
        ShareSetting shareSetting = shareSettingService.selectByBrandId(customer.getBrandId());
        ShopDetail shopDetail = shopDetailService.selectById(order.getShopDetailId());
        List<NewCustomCoupon> coupons = newcustomcouponService.selectListByCouponType(customer.getBrandId(), 1, order.getShopDetailId());
        if (coupons.size() > 0) {
            BigDecimal money = new BigDecimal("0.00");
            for (NewCustomCoupon coupon : coupons) {
                money = money.add(coupon.getCouponValue().multiply(new BigDecimal(coupon.getCouponNumber())));
            }
            str.append("邀请朋友扫一扫，");
            if (money.doubleValue() == 0.00 && shareSetting == null) {
                str.append("送他/她红包，朋友到店消费后，您将获得红包返利\n");
            } else if (money.doubleValue() == 0.00) {
                str.append("送他/她" + money + "元红包，朋友到店消费后，您将获得红包返利\n");
            } else if (shareSetting == null) {
                str.append("送他/她红包，朋友到店消费后，您将获得" + shareSetting.getMinMoney() + "元-" + shareSetting.getMaxMoney() + "元红包返利\n");
            } else {
                str.append("送他/她" + money + "元红包，朋友到店消费后，您将获得" + shareSetting.getMinMoney() + "元-" + shareSetting.getMaxMoney() + "元红包返利\n");
            }
            String jumpurl = setting.getWechatWelcomeUrl() + "?dialog=scanAqrCode&subpage=my&shopId=" + order.getShopDetailId();
            str.append("<a href='" + jumpurl + "'>打开邀请二维码</a>");
            String result = WeChatUtils.sendCustomerMsg(str.toString(), customer.getWechatId(), config.getAppid(), config.getAppsecret());
//        UserActionUtils.writeToFtp(LogType.ORDER_LOG, brand.getBrandName(), shopDetail.getName(), order.getId(),
//                "订单发送推送：" + str.toString());
            Map map = new HashMap(4);
            map.put("brandName", brand.getBrandName());
            map.put("fileName", customer.getId());
            map.put("type", "UserAction");
            map.put("content", "系统向用户:" + customer.getNickname() + "推送微信消息:" + str.toString() + ",请求服务器地址为:" + MQSetting.getLocalIP());
            doPostAnsc(LogUtils.url, map);
        }
    }

//    @AfterReturning(value = "payOrderModeFive()||payPrice()", returning = "order")
//    public void payContent(Order order) {
//        if (order != null && order.getOrderMode() == ShopMode.HOUFU_ORDER && order.getOrderState() == OrderState.PAYMENT
//                && order.getProductionStatus() == ProductionStatus.PRINTED) {
//            Customer customer = customerService.selectById(order.getCustomerId());
//            WechatConfig config = wechatConfigService.selectByBrandId(customer.getBrandId());
//            List<OrderPaymentItem> paymentItems = orderPaymentItemService.selectByOrderId(order.getId());
//            String money = "(";
//            for (OrderPaymentItem orderPaymentItem : paymentItems) {
//                money += PayMode.getPayModeName(orderPaymentItem.getPaymentModeId()) + "： " + orderPaymentItem.getPayValue() + " ";
//
//            }
//            StringBuffer msg = new StringBuffer();
//            BigDecimal sum = order.getOrderMoney();
//            List<Order> orders = orderService.selectByParentId(order.getId()); //得到子订单
//            for (Order child : orders) { //遍历子订单
//                sum = sum.add(child.getOrderMoney());
//            }
//
//            BrandSetting setting = brandSettingService.selectByBrandId(order.getBrandId());
//            if(setting.getIsUseServicePrice() == 1){
//                sum = sum.add(order.getServicePrice());
//            }
//
//            msg.append("您的订单").append(order.getSerialNumber()).append("已于").append(DateFormatUtils.format(paymentItems.get(0).getPayTime(), "yyyy-MM-dd HH:mm"));
//            msg.append("支付成功。订单金额：").append(sum).append(money).append(") ");
//            String result = WeChatUtils.sendCustomerMsg(msg.toString(), customer.getWechatId(), config.getAppid(), config.getAppsecret());
//        }
//
//    }

    @Pointcut("execution(* com.resto.shop.web.service.OrderService.afterPayShareBenefits(..))")
    public void afterPayShareBenefits() {
    }

    @AfterReturning(value = "confirmOrder()||confirmBossOrder()||confirmWaiMaiOrder()||afterPayShareBenefits()", returning = "order")
    public void confirmOrderAfter(Order order) {
        if (order != null) {
            log.info("确认订单成功后回调:" + order.getId());
            Customer customer = customerService.selectById(order.getCustomerId());
            if (customer == null) {
                return;
            }
            if (order.getOrderState() != OrderState.CONFIRM) {
                return;
            }
            WechatConfig config = wechatConfigService.selectByBrandId(customer.getBrandId());
            BrandSetting setting = brandSettingService.selectByBrandId(customer.getBrandId());
            Brand brand = brandService.selectById(customer.getBrandId());
//		RedConfig redConfig = redConfigService.selectListByShopId(order.getShopDetailId());
            if (order.getAllowAppraise()) {
                StringBuffer msg = new StringBuffer();
                msg.append("您有一个红包未领取，红包是" + brand.getBrandName() + "送您的一片心意xoxo");
                msg.append("<a href='" + setting.getWechatWelcomeUrl() + "?subpage=my&dialog=redpackage&orderId=" + order.getId() + "&shopId=" + order.getShopDetailId() + "'>点击领取</a>");

                String result = WeChatUtils.sendCustomerMsg(msg.toString(), customer.getWechatId(), config.getAppid(), config.getAppsecret());
//            UserActionUtils.writeToFtp(LogType.ORDER_LOG, brand.getBrandName(), shopDetail.getName(), order.getId(),
//                    "订单发送推送：" + msg.toString());
                Map map = new HashMap(4);
                map.put("brandName", brand.getBrandName());
                map.put("fileName", customer.getId());
                map.put("type", "UserAction");
                map.put("content", "系统向用户:" + customer.getNickname() + "推送微信消息:" + msg.toString() + ",请求服务器地址为:" + MQSetting.getLocalIP());
                doPostAnsc(LogUtils.url, map);
//            log.info("发送评论通知成功:" + msg + result);
                scanaQRcode(config, customer, setting, order);
            }
            try {
                if (customer.getFirstOrderTime() == null) { //分享判定
                    customerService.updateFirstOrderTime(customer.getId());
                    if (customer.getShareCustomer() != null) {
                        Customer shareCustomer = customerService.selectById(customer.getShareCustomer());
                        if (shareCustomer != null) {
                            ShareSetting shareSetting = shareSettingService.selectValidSettingByBrandId(customer.getBrandId());
                            if (shareSetting != null) {
                                log.info("是被分享用户，并且分享设置已启用:" + customer.getId() + " oid:" + order.getId() + " setting:" + shareSetting.getId());
                                BigDecimal rewardMoney = customerService.rewareShareCustomer(shareSetting, order, shareCustomer, customer);
                                log.info("准备发送返利通知");
                                sendRewardShareMsg(shareCustomer, customer, config, setting, rewardMoney, order);
                            } else {
                                log.info("准备发送返利通知  but品牌没有设置返利  so返利0元");
                                sendRewardShareMsg(shareCustomer, customer, config, setting, BigDecimal.ZERO, order);
                            }
                        }
                    }
                }else{
                    if (customer.getShareCustomer() != null){
                        Customer shareCustomer = customerService.selectById(customer.getShareCustomer());
                        if (shareCustomer != null) {
                            ShareSetting shareSetting = shareSettingService.selectValidSettingByBrandId(customer.getBrandId());
                            if (shareSetting != null && shareSetting.getOpenMultipleRebates() == 1) {
                                BigDecimal rewardMoney = customerService.rewareShareCustomerAgain(shareSetting, order, shareCustomer, customer);
                                log.info("准备发送返利通知");
                                sendRewardShareMsg(shareCustomer, customer, config, setting, rewardMoney, order);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("分享功能出错:" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void sendRewardShareMsg(Customer shareCustomer, Customer customer, WechatConfig config,
                                    BrandSetting setting, BigDecimal rewardMoney, Order order) {
        StringBuffer msg = new StringBuffer();
        Brand brand = brandService.selectById(order.getBrandId());
        ShopDetail shopDetail = shopDetailService.selectById(order.getShopDetailId());
        if (rewardMoney.compareTo(BigDecimal.ZERO) != 0) {
            rewardMoney = rewardMoney.setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        msg.append("您邀请的好友" + customer.getNickname() + "已到店消费，您已获得" + rewardMoney + "元红包返利\n<a href='" + setting.getWechatWelcomeUrl() + "?subpage=my&dialog=myYue'>点击查看余额！</a>");
//        msg.append("<a href='" + setting.getWechatWelcomeUrl() + "?subpage=my&dialog=myYue'>")
//                .append("您邀请的好友").append(customer.getNickname()).append("已到店消费，您已获得")
//                .append(rewardMoney).append("元红包返利").append("</a>");
        String result = WeChatUtils.sendCustomerMsg(msg.toString(), shareCustomer.getWechatId(), config.getAppid(), config.getAppsecret());
        //logBaseService.insertLogBaseInfoState(shopDetailService.selectById(order.getShopDetailId()),customer,shareCustomer.getId(),LogBaseState.FIRST_SHARE_PAY);
//        log.info("发送返利通知成功:" + shareCustomer.getId() + " MSG: " + msg + result);
//                "订单发送推送：" + msg.toString());//        UserActionUtils.writeToFtp(LogType.ORDER_LOG, brand.getBrandName(), shopDetail.getName(), order.getId(),

		Map map = new HashMap(4);
        map.put("brandName", brand.getBrandName());
        map.put("fileName", shareCustomer.getId());
        map.put("type", "UserAction");
        map.put("content", "系统向用户:" + shareCustomer.getNickname() + "推送微信消息:" + msg.toString() + ",请求服务器地址为:" + MQSetting.getLocalIP());
        doPostAnsc(LogUtils.url, map);
    }

    @Pointcut("execution(* com.resto.shop.web.service.OrderService.cancelOrderPos(..))")
    public void cancelOrderPos() {
    }

    ;

    @AfterReturning(value = "cancelOrderPos()", returning = "order")
    public void cancelOrderPosAfter(Order order) throws Throwable {
        if (order != null) {
            Customer customer = customerService.selectById(order.getCustomerId());
            WechatConfig config = wechatConfigService.selectByBrandId(customer.getBrandId());
            ShopDetail shopDetail = shopDetailService.selectById(order.getShopDetailId());
            Brand brand = brandService.selectById(order.getBrandId());
            StringBuffer msg = new StringBuffer();
            msg.append("您好，您 " + DateUtil.formatDate(order.getCreateTime(), "yyyy-MM-dd HH:mm") + " 的订单" + "已被商家取消\n");
            msg.append("订单编号:\n" + order.getSerialNumber() + "\n");
            if (order.getOrderMode() != null) {
                switch (order.getOrderMode()) {
                    case ShopMode.TABLE_MODE:
                        msg.append("桌号:" + order.getTableNumber() + "\n");
                        break;
                    case ShopMode.BOSS_ORDER:
                        msg.append("桌号:" + order.getTableNumber() + "\n");
                        break;
                    default:
                        msg.append("消费码：" + order.getVerCode() + "\n");
                        break;
                }
            }
            if (order.getShopName() == null || "".equals(order.getShopName())) {
                order.setShopName(shopDetail.getName());
            }
            msg.append("店铺名：" + order.getShopName() + "\n");
            msg.append("订单时间：" + DateFormatUtils.format(order.getCreateTime(), "yyyy-MM-dd HH:mm") + "\n");
            BrandSetting setting = brandSettingService.selectByBrandId(order.getBrandId());

            if (setting.getIsUseServicePrice() == 1 && shopDetail.getIsUseServicePrice() == 1 && order.getDistributionModeId() == 1) {
                msg.append(shopDetail.getServiceName() + "：" + order.getServicePrice() + "\n");
            }
            if (setting.getIsMealFee() == 1 && order.getDistributionModeId() == 3 && shopDetail.getIsMealFee() == 1) {
                msg.append(shopDetail.getMealFeeName() + "：" + order.getMealFeePrice() + "\n");
            }
            msg.append("订单明细：\n");
            List<OrderItem> orderItem = orderItemService.listByOrderId(order.getId());
            for (OrderItem item : orderItem) {
                if (item.getCount() > 0) {
                    msg.append("  " + item.getArticleName() + "x" + item.getCount() + "\n");
                }
            }
            msg.append("订单金额：" + order.getOrderMoney() + "\n");

            String result = WeChatUtils.sendCustomerMsg(msg.toString(), customer.getWechatId(), config.getAppid(), config.getAppsecret());
//            log.info("发送订单取消通知成功:" + msg + result);
//            UserActionUtils.writeToFtp(LogType.ORDER_LOG, brand.getBrandName(), shopDetail.getName(), order.getId(),
//                    "订单发送推送：" + msg.toString());
            Map map = new HashMap(4);
            map.put("brandName", brand.getBrandName());
            map.put("fileName", customer.getId());
            map.put("type", "UserAction");
            map.put("content", "系统向用户:" + customer.getNickname() + "推送微信消息:" + msg.toString() + ",请求服务器地址为:" + MQSetting.getLocalIP());
            doPostAnsc(LogUtils.url, map);
            MQMessageProducer.sendNoticeOrderMessage(order);

            if (order.getParentOrderId() != null) {  //子订单
                orderService.updateOrderChild(order.getId());
            }
//			//拒绝订单后还原库存
//			Boolean addStockSuccess  = false;
//			addStockSuccess	= orderService.addStock(orderService.getOrderInfo(order.getId()));
//			if(!addStockSuccess){
//				log.info("库存还原失败:"+order.getId());
//			}

            Order o = orderService.getOrderAccount(order.getShopDetailId());
            RedisUtil.set(order.getShopDetailId() + "shopOrderCount", o.getOrderCount());
            RedisUtil.set(order.getShopDetailId() + "shopOrderTotal", o.getOrderTotal());
            MQMessageProducer.sendPrintSuccess(order.getShopDetailId());

        }
    }

    private void sendVerCodeMsg(Order order) {
        Customer customer = customerService.selectById(order.getCustomerId());
        if (customer != null) {
            WechatConfig config = wechatConfigService.selectByBrandId(customer.getBrandId());
            ShopDetail shopDetail = shopDetailService.selectById(order.getShopDetailId());
            Brand brand = brandService.selectById(order.getBrandId());
            StringBuffer msg = new StringBuffer();
            msg.append("交易码:" + order.getVerCode() + "\n");
            msg.append("请留意叫号信息");
            String result = WeChatUtils.sendCustomerMsg(msg.toString(), customer.getWechatId(), config.getAppid(), config.getAppsecret());
//        UserActionUtils.writeToFtp(LogType.ORDER_LOG, brand.getBrandName(), shopDetail.getName(), order.getId(),
//                "订单发送推送：" + msg.toString());
            Map map = new HashMap(4);
            map.put("brandName", brand.getBrandName());
            map.put("fileName", customer.getId());
            map.put("type", "UserAction");
            map.put("content", "系统向用户:" + customer.getNickname() + "推送微信消息:" + msg.toString() + ",请求服务器地址为:" + MQSetting.getLocalIP());
            doPostAnsc(LogUtils.url, map);
//        log.info("发送取餐信息成功:" + result);
        }
    }

    @Pointcut("execution(* com.resto.shop.web.service.OrderPaymentItemService.insertByBeforePay(..))")
    public void insertByBeforePay() {

    }

    ;

    @AfterReturning(value = "insertByBeforePay()", returning = "orderPaymentItem")
    public void insertByBeforePay(OrderPaymentItem orderPaymentItem) {
        Order order = orderService.selectById(orderPaymentItem.getOrderId());
        MQMessageProducer.sendPlaceOrderNoPayMessage(order);
    }

    @Pointcut("execution(* com.resto.shop.web.service.OrderService.colseOrder(..))")
    public void colseOrder() {

    }

    ;

    @AfterReturning(value = "colseOrder()", returning = "order")
    public void colseOrder(Order order) {
        Brand brand = brandService.selectById(order.getBrandId());
        Customer customer = customerService.selectById(order.getCustomerId());
        WechatConfig config = wechatConfigService.selectByBrandId(brand.getId());
        StringBuilder sb = new StringBuilder("亲,今日未完成支付的订单已被系统自动取消,欢迎下次再来本店消费\n");
        sb.append("订单编号:" + order.getSerialNumber() + "\n");
        if (order.getOrderMode() != null) {
            switch (order.getOrderMode()) {
                case ShopMode.TABLE_MODE:
                    sb.append("桌号:" + (order.getTableNumber() != null ? order.getTableNumber() : "无") + "\n");
                    break;
                default:
                    sb.append("消费码：" + (order.getVerCode() != null ? order.getVerCode() : "无") + "\n");
                    break;
            }
        }
        if (order.getShopName() == null || "".equals(order.getShopName())) {
            order.setShopName(shopDetailService.selectById(order.getShopDetailId()).getName());
        }
        sb.append("店铺名：" + order.getShopName() + "\n");
        sb.append("订单时间：" + DateFormatUtils.format(order.getCreateTime(), "yyyy-MM-dd HH:mm") + "\n");
        sb.append("订单明细：\n");
        List<OrderItem> orderItem = orderItemService.listByOrderId(order.getId());
        for (OrderItem item : orderItem) {
            sb.append("  " + item.getArticleName() + "x" + item.getCount() + "\n");
        }
        sb.append("订单金额：" + order.getOrderMoney() + "\n");
        WeChatUtils.sendCustomerMsgASync(sb.toString(), customer.getWechatId(), config.getAppid(), config.getAppsecret());
    }

    /**
     * 切到评论方法
     */
    @Pointcut("execution(* com.resto.shop.web.service.AppraiseService.saveAppraise(..))")
    public void saveAppraise() {
    }


    /**
     * 订单评论完成后执行， 如是差评则发送消息队列执行打单操作
     *
     * @param appraise
     */
    @AfterReturning(value = "saveAppraise()", returning = "appraise")
    public void saveAppraise(Appraise appraise) {
        if (appraise != null) {
            log.info("订单评论完成");
            ShopDetail shopDetail = shopDetailService.selectById(appraise.getShopDetailId());
            //判断是否开启差评打单
            if (shopDetail.getOpenBadAppraisePrintOrder()) {
                //如满足差评条件则打印订单
                if (appraise.getLevel() <= 4) {
                    log.info("订单评论满足差评推送消息队列");
                    //发送队列消息
                    MQMessageProducer.sendBadAppraisePrintOrderMessage(appraise.getOrderId(), appraise.getShopDetailId());
                }
            }
        }
    }
}
