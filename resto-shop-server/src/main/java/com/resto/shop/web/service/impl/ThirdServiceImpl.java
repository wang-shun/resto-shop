package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.util.HungerUtil;
import com.resto.brand.web.model.BrandSetting;
import com.resto.shop.web.constant.CodeType;
import com.resto.shop.web.constant.PushAction;
import com.resto.shop.web.constant.PushType;
import com.resto.shop.web.service.OrderService;
import com.resto.shop.web.service.ThirdService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * Created by KONATA on 2016/10/28.
 * 饿了吗接口实现
 */
@RpcService
public class ThirdServiceImpl implements ThirdService {


    @Autowired
    private OrderService orderService;


    @Override
    public Boolean orderAccept(Map map,BrandSetting brandSetting) {
        String pushType = map.get("pushType").toString();
        try {
            switch (pushType){
                case PushType.HUNGER:
                    //饿了吗推送接口
                    return hungerPush(map,brandSetting);
                default:
                    return false;
            }
        }catch (Exception e){
            return false;
        }
    }

    private Boolean hungerPush(Map map,BrandSetting brandSetting) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        String pushAction;
        if(StringUtils.isEmpty(map.get("push_action"))){
            return false;
        }else{
            pushAction = map.get("push_action").toString();
        }
        if(pushAction.equals(PushAction.NEW_ORDER)){ //新订   单
            String orderIds = map.get("eleme_order_ids").toString();
            String [] ids = orderIds.split(","); //得到饿了吗的新增订单列表
            for(String id : ids){
                JSONObject json = new JSONObject(HungerUtil.HungerConnection("/order/"+id+"/",brandSetting.getConsumerKey(),brandSetting.getConsumerSecret()));
                if(json.get("code").equals(CodeType.SUCCESS)){
                    JSONObject order = json.getJSONObject("data");
                }

            }
        }else if (pushAction.equals(PushAction.ORDER_STATUS_UPDATGE)){ //订单状态更新

        }else if (pushAction.equals(PushAction.REFUND_ORDER)){ //退单
            String orderId = map.get("eleme_order_id").toString();


        }else if(pushAction.equals(PushAction.DELIVERY)){ //配送状态
            //目前对于配送状态推送不做处理
            return true;
        }
    }

}
