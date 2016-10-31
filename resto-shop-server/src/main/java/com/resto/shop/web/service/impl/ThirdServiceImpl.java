package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.util.ApplicationUtils;
import com.resto.brand.core.util.HungerUtil;
import com.resto.brand.web.model.BrandSetting;
import com.resto.shop.web.constant.*;
import com.resto.shop.web.dao.HungerOrderMapper;
import com.resto.shop.web.model.*;
import com.resto.shop.web.service.OrderPaymentItemService;
import com.resto.shop.web.service.OrderService;
import com.resto.shop.web.service.ThirdService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by KONATA on 2016/10/28.
 * 饿了吗接口实现
 */
@RpcService
public class ThirdServiceImpl implements ThirdService {


    @Resource
    OrderPaymentItemService orderPaymentItemService;

    @Autowired
    private HungerOrderMapper hungerOrderMapper;


    @Override
    public Boolean orderAccept(Map map, BrandSetting brandSetting) {
        String pushType = map.get("pushType").toString();

        Boolean result = false;
        try {
            switch (pushType) {
                case PushType.HUNGER:
                    //饿了吗推送接口
                    result =  hungerPush(map, brandSetting);
                        break;
                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
        return result;
    }

    private Boolean hungerPush(Map map, BrandSetting brandSetting) throws Exception {
        String pushAction;
        if (StringUtils.isEmpty(map.get("push_action"))) {
            return false;
        } else {
            pushAction = map.get("push_action").toString();
        }
        if (pushAction.equals(PushAction.NEW_ORDER)) { //新订   单
            addHungerOrder(map, brandSetting);
        } else if (pushAction.equals(PushAction.ORDER_STATUS_UPDATGE)) { //订单状态更新
            updateHungerOrder(map.get("eleme_order_id").toString(),Integer.valueOf(map.get("new_status").toString()));
        } else if (pushAction.equals(PushAction.REFUND_ORDER)) { //退单
            updateHungerOrder(map.get("eleme_order_id").toString(),Integer.valueOf(map.get("refund_status").toString()));
            //退单的时候 加入 记录
            if(map.get("refund_status").toString().equals(HungerStatus.REFUND_SUCCESS)){ //退款成功
                HungerOrder hungerOrder = getHungerOrderById(map.get("eleme_order_id").toString(),brandSetting);
                OrderPaymentItem item = new OrderPaymentItem();
                item.setId(ApplicationUtils.randomUUID());
                item.setOrderId(map.get("eleme_order_id").toString());
                item.setPaymentModeId(PayMode.HUNGER_MONEY);
                item.setPayTime(new Date());
                item.setPayValue(hungerOrder.getOriginalPrice());
                item.setRemark("饿了吗退款:" + hungerOrder.getOriginalPrice().multiply(new BigDecimal(-1)));
                item.setResultData("原价:"+hungerOrder.getOriginalPrice()+"，订单总价(减去优惠后的价格)："+hungerOrder.getTotalPrice());
                orderPaymentItemService.insert(item);
            }
        } else if (pushAction.equals(PushAction.DELIVERY)) { //配送状态
            //目前对于配送状态推送不做处理
        }
        return true;
    }


    private void updateHungerOrder(String orderId,Integer orderState){
        hungerOrderMapper.updateHungerOrder(orderId, orderState);
    }

    private HungerOrder getHungerOrderById(String orderId,BrandSetting brandSetting) throws Exception {
        JSONObject json = new JSONObject(HungerUtil.HungerConnection(new HashMap<String, String>(),
                "/order/" + orderId + "/", brandSetting.getConsumerKey(), brandSetting.getConsumerSecret()));
        if (json.optString("code").equals(CodeType.SUCCESS)) {
            JSONObject order = json.getJSONObject("data");
            HungerOrder hungerOrder =  new HungerOrder(order);
            return hungerOrder;
        }else{
            return null;
        }

    }

    private void addHungerOrder(Map map, BrandSetting brandSetting) throws Exception {
        String orderIds = map.get("eleme_order_ids").toString();
        String[] ids = orderIds.split(","); //得到饿了吗的新增订单列表
        for (String id : ids) {
            JSONObject json = new JSONObject(HungerUtil.HungerConnection(new HashMap<String, String>(),
                    "/order/" + id + "/", brandSetting.getConsumerKey(), brandSetting.getConsumerSecret()));
            if (json.optString("code").equals(CodeType.SUCCESS)) {
                JSONObject order = json.getJSONObject("data");
                HungerOrder hungerOrder =  new HungerOrder(order);
                hungerOrderMapper.insertHungerOrder(hungerOrder);

                OrderPaymentItem item = new OrderPaymentItem();
                item.setId(ApplicationUtils.randomUUID());
                item.setOrderId(id);
                item.setPaymentModeId(PayMode.HUNGER_MONEY);
                item.setPayTime(new Date());
                item.setPayValue(hungerOrder.getOriginalPrice());
                item.setRemark("饿了吗付款:" + hungerOrder.getOriginalPrice());
                item.setResultData("原价:"+hungerOrder.getOriginalPrice()+"，订单总价(减去优惠后的价格)："+hungerOrder.getTotalPrice());
                orderPaymentItemService.insert(item);

                JSONObject detail = order.optJSONObject("detail");
                if(detail != null){
                    JSONArray array =  detail.optJSONArray("extra");
                    if(array != null){
                        for(int i = 0;i< array.length();i++){
                            HungerOrderExtra extra = new HungerOrderExtra(array.getJSONObject(i),order.optString("order_id"));
                            hungerOrderMapper.insertHungerExtra(extra);
                        }
                    }

                    JSONArray group =  detail.optJSONArray("group");
                    if(group != null){
                        for(int i = 0;i< group.length();i++){
                            HungerOrderGroup orderGroup = new HungerOrderGroup();
                            orderGroup.setOrderId(order.optString("order_id"));
                            hungerOrderMapper.insertHungerGroup(orderGroup);
                            JSONArray details = group.getJSONArray(i);
                            if(details != null){
                                for(int k = 0;k < details.length();k++){
                                    JSONObject orderDetailJson = details.getJSONObject(k);
                                    HungerOrderDetail orderDetail = new HungerOrderDetail(orderDetailJson,orderGroup.getId(),order.optString("order_id"));
                                    hungerOrderMapper.insertHungerOrderDetail(orderDetail);
                                    JSONArray garnish =  orderDetailJson.optJSONArray("garnish");
                                    if(garnish != null){
                                        for(int o = 0 ;o < garnish.length();o ++){
                                            HungerOrderGarnish orderGarnish = new HungerOrderGarnish(garnish.getJSONObject(o),orderDetailJson.optString("id"),
                                                    order.optString("order_id"),orderGroup.getId());
                                            hungerOrderMapper.insertHungerOrderGarnish(orderGarnish);
                                        }

                                    }

                                }
                            }
                        }
                    }
                }
            }

        }
    }

}
