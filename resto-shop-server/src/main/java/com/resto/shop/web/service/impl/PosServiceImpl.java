package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.alibaba.fastjson.JSON;
import com.resto.brand.web.model.AccountSetting;
import com.resto.brand.web.model.Brand;
import com.resto.brand.web.model.BrandSetting;
import com.resto.brand.web.service.AccountSettingService;
import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.BrandSettingService;
import com.resto.shop.web.constant.Common;
import com.resto.shop.web.exception.AppException;
import com.resto.shop.web.model.*;
import com.resto.shop.web.posDto.ArticleStockDto;
import com.resto.shop.web.posDto.OrderDto;
import com.resto.shop.web.producer.MQMessageProducer;
import com.resto.shop.web.service.*;
import com.resto.shop.web.util.RedisUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by KONATA on 2017/8/9.
 */
@RpcService
public class PosServiceImpl implements PosService {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private OrderPaymentItemService orderPaymentItemService;

    @Autowired
    private PlatformOrderService platformOrderService;

    @Autowired
    private PlatformOrderDetailService platformOrderDetailService;

    @Autowired
    private PlatformOrderExtraService platformOrderExtraService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private BrandSettingService brandSettingService;

    @Autowired
    private AccountSettingService accountSettingService;

    @Autowired
    private CustomerAddressService customerAddressService;

    @Override
    public String syncArticleStock(String shopId) {
        Map<String, Object> result = new HashMap<>();
        result.put("dataType", "article");

        List<Article> articleList = articleService.selectList(shopId);
        List<ArticleStockDto> articleStockDtoList = new ArrayList<>();
        for (Article article : articleList) {
            Integer count = (Integer) RedisUtil.get(article.getId() + Common.KUCUN);
            if (count != null) {
                article.setCurrentWorkingStock(count);
            }
            ArticleStockDto articleStockDto = new ArticleStockDto(article.getId(), article.getCurrentWorkingStock());
            articleStockDtoList.add(articleStockDto);
        }
        result.put("articleList", articleStockDtoList);
        return new JSONObject(result).toString();
    }

    @Override
    public String shopMsgChange(String shopId) {
        return shopId;
    }

    @Override
    public String syncOrderCreated(String orderId) {
        Order order = orderService.selectById(orderId);
        OrderDto orderDto = new OrderDto(order);
        JSONObject jsonObject = new JSONObject(orderDto);
        jsonObject.put("dataType", "orderCreated");
        List<OrderItem> orderItems = orderItemService.listByOrderId(orderId);
        jsonObject.put("orderItem", orderItems);
        List<OrderPaymentItem> payItemsList = orderPaymentItemService.selectByOrderId(order.getId());
        jsonObject.put("orderPayment", payItemsList);
        CustomerAddress customerAddress = customerAddressService.selectByPrimaryKey(order.getCustomerAddressId());
        if(customerAddress == null){
            jsonObject.put("customerAddress","");
        }else{
            jsonObject.put("customerAddress",new JSONObject(customerAddress));
        }

        return jsonObject.toString();
    }

    @Override
    public String syncOrderPay(String orderId) {
        Order order = orderService.selectById(orderId);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("dataType", "orderPay");
        jsonObject.put("payMode", order.getPayMode());
        List<OrderPaymentItem> payItemsList = orderPaymentItemService.selectByOrderId(order.getId());
        jsonObject.put("orderPayment", payItemsList);
        return jsonObject.toString();
    }

    @Override
    public String syncPlatform(String orderId) {
        Map<String, Object> result = new HashMap<>();
        result.put("dataType", "platform");
        PlatformOrder platformOrder = platformOrderService.selectByPlatformOrderId(orderId, null);
        List<PlatformOrderDetail> platformOrderDetails = platformOrderDetailService.selectByPlatformOrderId(orderId);
        List<PlatformOrderExtra> platformOrderExtras = platformOrderExtraService.selectByPlatformOrderId(orderId);
        result.put("order", platformOrder);
        result.put("orderDetail", platformOrderDetails);
        result.put("orderExtra", platformOrderExtras);
        return new JSONObject(result).toString();
    }

    @Override
    public void articleActived(String articleId, Integer actived) {
        articleService.setActivated(articleId,actived);
    }

    @Override
    public void articleEmpty(String articleId) {
        Article article = articleService.selectById(articleId);
        articleService.clearStock(articleId,article.getShopDetailId());
    }

    @Override
    public void articleEdit(String articleId, Integer count) {
        Article article = articleService.selectById(articleId);
        articleService.editStock(articleId,count,article.getShopDetailId());
    }

    @Override
    public void printSuccess(String orderId) {
        Order order = orderService.selectById(orderId);
        Brand brand = brandService.selectById(order.getBrandId());
        BrandSetting brandSetting = brandSettingService.selectByBrandId(brand.getId());
        AccountSetting accountSetting = accountSettingService.selectByBrandSettingId(brandSetting.getId());
        try {
            orderService.printSuccess(orderId,brandSetting.getOpenBrandAccount() == 1,accountSetting);
        } catch (AppException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void syncPosOrder(String data) {
        JSONObject json = new JSONObject(data);
        if(json.get("type") != "orderCreated"){
            return;
        }
        OrderDto orderDto = JSON.parseObject(json.get("order").toString(), OrderDto.class);
        Order order = new Order(orderDto);

    }

    @Override
    public void syncPosOrderPay(String data) {

    }

    @Override
    public void syncPosRefundOrder(String data) {

    }

    @Override
    public void test() {
        Order order = new Order();
        order.setId("00b8a27437cf460c93910bdc2489d061");
        order.setBrandId("31946c940e194311b117e3fff5327215");
        order.setShopDetailId("31164cebcc4b422685e8d9a32db12ab8");
        MQMessageProducer.sendCreateOrderMessage(order);
    }

}
