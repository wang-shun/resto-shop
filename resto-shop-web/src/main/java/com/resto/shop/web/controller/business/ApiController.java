package com.resto.shop.web.controller.business;

import com.resto.brand.core.entity.Result;
import com.resto.brand.core.util.DateUtil;
import com.resto.brand.core.util.OrderCountUtils;
import com.resto.brand.core.util.ThirdPatyUtils;
import com.resto.brand.web.model.Brand;
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

import static com.resto.brand.core.util.OrderCountUtils.addKey;
import static com.resto.brand.core.util.OrderCountUtils.getPayDetail;

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
     * 4. 接收方拿到post过来的参数以及这个access_key。
     * 也和发送一样，用密钥key 对各个参数进行一样的规则(各种排序，MD5，ip等)也生成一个access_key2。
     * 5. 对比access_key 和access_key2 。一样。则允许操作，不一样，报错返回或者加入黑名单。
     * 6在店铺端设置 thirdAppid 作为参数传过来 有值 说明需要的店铺数据 没有则返回品牌数据
     *
     * @return
     */

    @RequestMapping(value = "getThirdData", method = RequestMethod.POST)
    @ResponseBody
    public Result getThirdData(String signature, String timestamp, String nonce, String appid, String thirdAppid, String beginDate, String endDate, HttpServletRequest request) {
        //默认返回false
        Result result = new Result();
        result.setSuccess(false);

        //查询所有已经配置第三方接口的品牌
        List<BrandSetting> brandSettingList = brandSettingService.selectListByState();
        List<String> appidList = new ArrayList<>();
        if (!brandSettingList.isEmpty()) {
            for (BrandSetting brandSetting : brandSettingList) {
                appidList.add(brandSetting.getAppid());
            }
        }
        /**
         * 1.将appId timestamp nonce 三个参数进行字典排序
         * 2,.将3个参数字符串拼接成一个字符串进行sha1加密
         */
        if (ThirdPatyUtils.checkSignature(signature, timestamp, nonce, appidList)) {
            //定位数据库
            BrandSetting brandSetting = brandSettingService.selectByAppid(appid);
            if (null == brandSetting || "0".equals(brandSetting.getOpenThirdInterface().toString())) {
                result.setSuccess(false);
                result.setMessage("参数非法");
                return result;
            }
            //判断是需要店铺数据还是品牌数据
            List<Order> orderList = new ArrayList<>();
            if (StringUtils.isEmpty(thirdAppid)) {//说明需要的是品牌数据
                //定位数据库
                request.getSession().setAttribute(SessionKey.CURRENT_BRAND_ID, brandSetting.getBrandId());
                orderList = orderService.selectBaseToThirdList(brandSetting.getBrandId(), beginDate, endDate);
            } else {
                //说明需要的是店铺端的数据
                //判断是否是
                ShopDetail shopDetail = shopDetailService.selectByThirdAppId(thirdAppid);
                if (null == shopDetail) {
                    result.setSuccess(false);
                    result.setMessage("参数非法");
                    return result;
                } else {
                    orderList = orderService.selectBaseToThirdListByShopId(shopDetail.getId(), beginDate, endDate);
                }
            }
            //把需要的信息封装起来
            List<Map<String, Object>> ThirdData = new ArrayList<>(1000);
            if (!orderList.isEmpty()) {
                for (Order o : orderList) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("posId", o.getShopDetailId());
                    map.put("addTime", DateUtil.formatDate(o.getCreateTime(),"yyyy-MM-dd HH:mm:ss"));//订单创建时间
                    map.put("posDate", DateUtil.formatDate(o.getPushOrderTime(),"yyyy-MM-dd HH:mm:ss"));//订单推送时间
                    map.put("tableNumber", o.getTableNumber());//座号
                    map.put("serialNumber", o.getSerialNumber());//
                    //订单支付项
                    map.put("payDetail", "");//订单支付项详细
                    if (!o.getOrderPaymentItems().isEmpty()) {
                        Map<Integer, BigDecimal> payMap = new HashedMap();
                        for (OrderPaymentItem oi : o.getOrderPaymentItems()) {
                            addKey(oi.getPaymentModeId(), oi.getPayValue(), payMap);
                        }
                        //合并完支付项后 完整输出
                        map.put("payDetail", getPayDetail(payMap));
                    }
                    List<Map<String, String>> itemList = new ArrayList<>();
                    //订单菜品项
                    if (!o.getOrderItems().isEmpty()) {
                        for (OrderItem orderItem : o.getOrderItems()) {
                            Map<String, String> itemMap = new HashedMap();
                            itemMap.put("menuType", ItemType.getItemTypeName(orderItem.getType()));//订单菜品类型
                            itemMap.put("menuCode", orderItem.getArticleId());//菜品id
                            itemMap.put("menuName", orderItem.getArticleName());//菜品name
                            itemMap.put("quanity", orderItem.getType().toString());//个数
                            // itemMap.put("parentType",orderItem.getParentId());//针对套餐和子品
                            itemList.add(itemMap);
                        }
                    }
                    map.put("itemList", itemList);
                    ThirdData.add(map);
                }
            }
            return getSuccessResult(ThirdData);
            //查询地方报表需要的数据
        }
        return  result;
    }

}
