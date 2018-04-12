package com.resto.shop.web.aspect;

import com.resto.brand.core.util.WeChatUtils;
import com.resto.brand.web.model.WechatConfig;
import com.resto.brand.web.service.BrandSettingService;
import com.resto.brand.web.service.WechatConfigService;
import com.resto.shop.web.model.Customer;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.model.OrderItem;
import com.resto.shop.web.service.CustomerService;
import com.resto.shop.web.service.OrderItemService;
import com.resto.shop.web.service.OrderService;
import com.resto.shop.web.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.resto.shop.web.producer.MQMessageProducer.sendShopChangeMessage;

/**
 * Created by KONATA on 2017/8/10.
 */
@Component
@Aspect
public class PosAspect {

    Logger log = LoggerFactory.getLogger(getClass());

    @Pointcut("execution(* com.resto.shop.web.service.PosService.shopMsgChange(..))")
    public void shopMsgChange() {
    }

    @Pointcut("execution(* com.resto.shop.web.service.PosService.updateData(..))")
    public void updateData() {
    }


    @AfterReturning(value = "shopMsgChange()", returning = "shopId")
    public void shopMsgChange(String shopId) throws Throwable {
        sendShopChangeMessage(shopId);
    }

    @Resource
    OrderService orderService;

    @Resource
    OrderItemService orderItemService;

    @Resource
    CustomerService customerService;

    @Resource
    WechatConfigService wechatConfigService;


    @AfterReturning(value = "updateData()", returning = "resultInfo")
    public void updateData(JoinPoint point, String resultInfo){
        log.info("修改数据完成，进入切面返回信息：" + resultInfo);
        //得到调用此方法时传递的参数
        String paramData = (String) point.getArgs()[0];
        log.info("调用方法是传递的参数：" + paramData);
        JSONObject param = new JSONObject(paramData);
        //得到此次修改数据的业务类型
        String serverType = param.getString("serverType");
        JSONObject returnObject = new JSONObject(resultInfo);
        List<String> orderIds = (List<String>) returnObject.get("orderIds");
        Order order = new Order();
        //判断业务类型进行联动操作
        switch (serverType){
            case "weightPackage":
                log.info("开始修改重量包后的后续操作");
                //修改重量包
                List<String> orderItemIds = (List<String>) returnObject.get("orderItemIds");
                //得到当前订单
                order = orderService.selectById(orderIds.get(0));
                //查询到当前用户
                Customer customer = customerService.selectById(order.getCustomerId());
                if (customer != null){
                    String sendMessage = "报告老板，商家已确认您的订单，香喷喷的美食马上就来~\n" +
                            "菜品明细：\n";
                    for (String orderItemId : orderItemIds){
                        OrderItem orderItem = orderItemService.selectById(orderItemId);
                        sendMessage = sendMessage + orderItem.getArticleName() + "  ×1";
                    }
                    WechatConfig wechatConfig = wechatConfigService.selectByBrandId(order.getBrandId());
                    log.info("发送微信消息：" + sendMessage);
                    WeChatUtils.sendCustomerMsg(sendMessage, customer.getWechatId(), wechatConfig.getAppid(), wechatConfig.getAppsecret());
                }
                break;
            case "orderPay":
                log.info("开始支付订单后的后续操作");
                for (String orderId : orderIds){
                    order = orderService.selectById(orderId);
                    if (StringUtils.isBlank(order.getParentOrderId())){
                        break;
                    }
                }
                //释放桌位
                RedisUtil.set(order.getShopDetailId()+order.getTableNumber()+"status",true);
                //确认订单
                orderService.confirmBossOrder(order);
                break;
            default:
                log.info("本次调用无后续联动操作");
                break;
        }
    }
}
