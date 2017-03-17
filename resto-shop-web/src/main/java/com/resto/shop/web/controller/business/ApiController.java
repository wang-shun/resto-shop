package com.resto.shop.web.controller.business;

import com.resto.brand.core.entity.Result;
import com.resto.brand.core.util.ThirdPatyUtils;
import com.resto.brand.web.model.BrandSetting;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.service.BrandSettingService;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.shop.web.config.SessionKey;
import com.resto.shop.web.constant.ItemType;
import com.resto.shop.web.constant.PayMode;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.model.OrderItem;
import com.resto.shop.web.model.OrderPaymentItem;
import com.resto.shop.web.service.*;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用于同步第三方数据库的Controller(以及用来查询不正常的订单存到品牌)
 *
 * @author lmx
 */
@Controller
@RequestMapping("api")
public class ApiController extends GenericController {

    @Resource
    private OrderService orderService;

    @Resource
    private BrandSettingService brandSettingService;

    @Resource
    OrderPaymentItemService orderpaymentitemService;

    @Resource
    private ShopDetailService shopDetailService;

    @Resource
    private OrderItemService orderItemService;


    //对接嫩绿茶

    /**
     * 对接的方式:
     * 1.设定一个秘钥 appid='2323dsfadfewrasa3434" 这个是在品牌设置中开启或者关闭
     * 2.这个appid只有发送方和接收方知道
     * 3.调用时，发送方，组合各个参数用密钥 key按照一定的规则(各种排序，MD5，ip等)生成一个access_key。
     * 一起post提交到API接口
     *4. 接收方拿到post过来的参数以及这个access_key。
     * 也和发送一样，用密钥key 对各个参数进行一样的规则(各种排序，MD5，ip等)也生成一个access_key2。
     * 5. 对比access_key 和access_key2 。一样。则允许操作，不一样，报错返回或者加入黑名单。
     * 6在店铺端设置 thirdAppid 作为参数传过来 有值 说明需要的店铺数据 没有则返回品牌数据
     * @return
     */

    @RequestMapping(value = "getThirdData",method = RequestMethod.POST)
    @ResponseBody
    public  Result getThirdData(String signature,String timestamp,String nonce,String appId,String thirdAppid,String beginDate,String endDate,HttpServletRequest request){
        //签名条件可以参照微信
        Result result = new Result();
        result.setSuccess(true);
        /**
         * 1.将appId timestamp nonce 三个参数进行字典排序
         * 2,.将3个参数字符串拼接成一个字符串进行sha1加密
         */
        if(!ThirdPatyUtils.checkSignature(signature,timestamp,nonce)){
            result.setSuccess(false);
            result.setMessage("请求非法");
            return  result;
        }else {
            //定位数据库
            BrandSetting brandSetting = brandSettingService.selectByAppid(appId);
            if(null==brandSetting||"0".equals(brandSetting.getOpenThirdInterface().toString())){
                result.setSuccess(false);
                result.setMessage("参数非法");
                return  result;
            }
            //判断是需要店铺数据还是品牌数据
            List<Order> orderList = new ArrayList<>();

            if(StringUtils.isEmpty(thirdAppid)){//说明需要的是品牌数据
                request.getSession().setAttribute(SessionKey.CURRENT_BRAND_ID,brandSetting.getBrandId());
                orderList = orderService.selectBaseToThirdList(brandSetting.getBrandId(),beginDate,endDate);
            }else {
                //说明需要的是店铺端的数据
                //判断是否是
                ShopDetail shopDetail = shopDetailService.selectByThirdAppId(thirdAppid);
                if(null==shopDetail){
                    result.setSuccess(false);
                    result.setMessage("参数非法");
                    return  result;
                }else {
                    orderList = orderService.selectBaseToThirdListByShopId(shopDetail.getId(),beginDate,endDate);
                }
            }
            //把需要的信息封装起来
            List <Map<String,Object>> ThirdData = new ArrayList<>();
            if(!orderList.isEmpty()){
                for(Order o:orderList){
                    Map<String,Object> map = new HashMap<>();
                    map.put("posId",o.getShopDetailId());
                    map.put("addTime",o.getCreateTime());//订单创建时间
                    map.put("posDate",o.getPushOrderTime());//订单推送时间
                    map.put("tableNumber",o.getTableNumber());//座号
                    map.put("serialNumber",o.getSerialNumber());//
                    //订单支付项
                    map.put("payDetail","");//订单支付项详细
                    if(!o.getOrderPaymentItems().isEmpty()){
                        Map<Integer,BigDecimal> payMap = new HashedMap();
                        for(OrderPaymentItem oi:o.getOrderPaymentItems()){
                            addKey(oi.getPaymentModeId(),oi.getPayValue(),payMap);
                        }
                        //合并完支付项后 完整输出
                        map.put("payDetail",getPayDetail(payMap));
                    }

                    List<Map<String,String>> itemList = new ArrayList<>();
                    //订单菜品项
                    if(!o.getOrderItems().isEmpty()){
                        for(OrderItem orderItem: o.getOrderItems()){
                            Map<String,String> itemMap = new HashedMap();
                                itemMap.put("menuType", ItemType.getItemTypeName(orderItem.getType()));//订单菜品类型
                                 itemMap.put("menuCode",orderItem.getType().toString());//菜品id
                                 itemMap.put("menuName",orderItem.getType().toString());//菜品name
                                 itemMap.put("quanity",orderItem.getType().toString());//个数
                                // itemMap.put("parentType",orderItem.getParentId());//针对套餐和子品
                            itemList.add(itemMap);
                        }
                    }
                    map.put("itemList",itemList);
                    ThirdData.add(map);
                }
            }
            return getSuccessResult(ThirdData);

            //查询地方报表需要的数据
        }
    }

    /**
     * 合并订单项中相同的payModeId
     * @param key
     * @param value
     */
    private void addKey(Integer key, BigDecimal value,Map<Integer,BigDecimal> payMap) {
        if(payMap.get(key)!=null){//说明map中已有改值
            payMap.put(key,payMap.get(key).add(value));
        }else {
            payMap.put(key,value);
        }
    }


    private   String getPayDetail (Map<Integer,BigDecimal> payMap){
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<Integer,BigDecimal> map:payMap.entrySet()){
            switch (map.getKey()){
                case PayMode.WEIXIN_PAY:
                    sb.append("微信支付金额为"+map.getValue());
                    break;
                case PayMode.ACCOUNT_PAY:
                    sb.append("红包(余额)支付金额为:"+map.getValue());
                    break;
                case PayMode.COUPON_PAY:
                    sb.append("优惠券支付金额为:"+map.getValue());
                    break;
                case PayMode.MONEY_PAY:
                    sb.append("其它方式支付金额为:"+map.getValue());
                    break;
                case PayMode.BANK_CART_PAY:
                    sb.append("银行卡支付金额为:"+map.getValue());
                    break;
                case PayMode.CHARGE_PAY:
                    sb.append("充值支付金额为:"+map.getValue());
                    break;
                case PayMode.REWARD_PAY:
                    sb.append("充值返还支付金额为:"+map.getValue());
                    break;
                case PayMode.WAIT_MONEY:
                    sb.append("等位红包的支付金额为:"+map.getValue());
                    break;
                case PayMode.HUNGER_MONEY:
                    sb.append("饿了么支付金额为:"+map.getValue());
                    break;
                case PayMode.ALI_PAY:
                    sb.append("支付宝支付金额为:"+map.getValue());
                    break;
                case PayMode.ARTICLE_BACK_PAY:
                    sb.append("退菜红包支付金额为:"+map.getValue());
                    break;
                case PayMode.CRASH_PAY:
                    sb.append("现金支付金额为:"+map.getValue());
                    break;
                case PayMode.APPRAISE_RED_PAY:
                    sb.append("评论红包支付金额为:"+map.getValue());
                    break;
                case  PayMode.SHARE_RED_PAY:
                    sb.append("分享返利红包金额为"+map.getValue());
                default:
                        break;
            }
        }
        return  sb.toString();
    }



}
