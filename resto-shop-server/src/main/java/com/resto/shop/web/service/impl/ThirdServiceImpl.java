package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.util.ApplicationUtils;
import com.resto.brand.core.util.DateUtil;
import com.resto.brand.core.util.HungerUtil;
import com.resto.brand.web.model.BrandSetting;
import com.resto.brand.web.model.Platform;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.service.PlatformService;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.shop.web.constant.*;
import com.resto.shop.web.dao.ArticleMapper;
import com.resto.shop.web.dao.HungerOrderMapper;
import com.resto.shop.web.dao.OrderMapper;
import com.resto.shop.web.exception.AppException;
import com.resto.shop.web.model.*;
import com.resto.shop.web.producer.MQMessageProducer;
import com.resto.shop.web.service.KitchenService;
import com.resto.shop.web.service.OrderPaymentItemService;
import com.resto.shop.web.service.PrinterService;
import com.resto.shop.web.service.ThirdService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

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

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ShopDetailService shopDetailService;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private PlatformService platformService;

    @Autowired
    private PrinterService printerService;

    @Autowired
    private KitchenService kitchenService;


    @Override
    public List<Map<String, Object>> printOrderByPlatform(String platformId, Integer type) {
        List<Map<String, Object>> result = null;
        switch (type) {
            case PlatformType.E_LE_ME:
                result = printElemeOrder(platformId);
                break;
            default:
                break;
        }
        return result;
    }


    private List<Map<String, Object>> printElemeOrder(String orderId) {
        List<Map<String, Object>> printTask = new ArrayList<>();
        HungerOrder hungerOrder = hungerOrderMapper.selectById(orderId);
        List<HungerOrderDetail> details = hungerOrderMapper.selectDetailsById(hungerOrder.getOrderId());
        ShopDetail shopDetail = shopDetailService.selectByRestaurantId(hungerOrder.getRestaurantId());
        List<Printer> ticketPrinter = printerService.selectByShopAndType(shopDetail.getId(), PrinterType.RECEPTION);
        for (Printer printer : ticketPrinter) {
            Map<String, Object> ticket = printTicket(hungerOrder, details, shopDetail, printer);
            if (ticket != null) {
                printTask.add(ticket);
            }

        }

        List<Map<String, Object>> kitchenTicket = printKitchen(hungerOrder, details,shopDetail);
        if (!kitchenTicket.isEmpty()) {
            printTask.addAll(kitchenTicket);
        }

        return printTask;
    }


    public List<Map<String, Object>> printKitchen(HungerOrder order, List<HungerOrderDetail> articleList,ShopDetail shopDetail) {
        //每个厨房 所需制作的   菜品信息
        Map<String, List<HungerOrderDetail>> kitchenArticleMap = new HashMap<String, List<HungerOrderDetail>>();
        //厨房信息
        Map<String, Kitchen> kitchenMap = new HashMap<String, Kitchen>();
        //遍历 订单集合
        int sum = 0;
        for (HungerOrderDetail item : articleList) {
            //得到当前菜品 所关联的厨房信息
            Article article = articleMapper.selectByName(item.getName());
            String articleId = article.getId();
            sum += item.getQuantity();
            List<Kitchen> kitchenList = kitchenService.selectInfoByArticleId(articleId);
            for (Kitchen kitchen : kitchenList) {
                String kitchenId = kitchen.getId().toString();
                kitchenMap.put(kitchenId, kitchen);//保存厨房信息
                //判断 厨房集合中 是否已经包含当前厨房信息
                if (!kitchenArticleMap.containsKey(kitchenId)) {
                    //如果没有 则新建
                    kitchenArticleMap.put(kitchenId, new ArrayList<HungerOrderDetail>());
                }
                kitchenArticleMap.get(kitchenId).add(item);
            }

        }


        //打印线程集合
        List<Map<String, Object>> printTask = new ArrayList<Map<String, Object>>();


        //编列 厨房菜品 集合
        for (String kitchenId : kitchenArticleMap.keySet()) {
            Kitchen kitchen = kitchenMap.get(kitchenId);//得到厨房 信息
            Printer printer = printerService.selectById(kitchen.getPrinterId());//得到打印机信息
            if (printer == null) {
                continue;
            }



            //生成厨房小票
            for (HungerOrderDetail article : kitchenArticleMap.get(kitchenId)) {
                //保存 菜品的名称和数量
                List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
                Map<String, Object> item = new HashMap<String, Object>();
                item.put("SUBTOTAL", article.getQuantity() * article.getPrice().doubleValue());
                item.put("ARTICLE_NAME", article.getName());
                item.put("ARTICLE_COUNT", article.getQuantity());
                items.add(item);

                //保存基本信息


                Map<String, Object> print = new HashMap<String, Object>();
                print.put("TABLE_NO", "");

                print.put("KITCHEN_NAME", kitchen.getName());
                print.put("PORT", printer.getPort());
                print.put("ORDER_ID", order.getOrderId());
                print.put("IP", printer.getIp());
                String print_id = ApplicationUtils.randomUUID();
                print.put("PRINT_TASK_ID", print_id);
                print.put("ADD_TIME", new Date());

                Map<String, Object> data = new HashMap<String, Object>();
                data.put("ORDER_ID", order.getOrderId());
                data.put("ITEMS", items);
                data.put("DISTRIBUTION_MODE", "饿了么订单");
                data.put("ORIGINAL_AMOUNT", order.getOriginalPrice());
                data.put("RESTAURANT_ADDRESS", shopDetail.getAddress());
                data.put("REDUCTION_AMOUNT", order.getOriginalPrice().subtract(order.getTotalPrice()));
                data.put("RESTAURANT_TEL", shopDetail.getPhone());
                data.put("TABLE_NUMBER", "");
                data.put("PAYMENT_AMOUNT", order.getTotalPrice());
                data.put("RESTAURANT_NAME", shopDetail.getName());


                data.put("DATETIME", DateUtil.formatDate(new Date(), "MM-dd HH:mm"));
                data.put("ARTICLE_COUNT", sum);
                print.put("DATA", data);

                print.put("STATUS", 0);

                print.put("TICKET_TYPE", TicketType.KITCHEN);


//
                //添加当天打印订单的序号
                data.put("ORDER_NUMBER", "");

                printTask.add(print);
            }
        }

        return printTask;
    }

    public Map<String, Object> printTicket(HungerOrder order, List<HungerOrderDetail> orderItems, ShopDetail shopDetail, Printer printer) {
        if (printer == null) {
            return null;
        }
        int sum = 0;
        List<Map<String, Object>> items = new ArrayList<>();
        for (HungerOrderDetail article : orderItems) {
            Map<String, Object> item = new HashMap<>();
            item.put("SUBTOTAL", article.getPrice().doubleValue() * article.getQuantity());
            item.put("ARTICLE_NAME", article.getName());
            item.put("ARTICLE_COUNT", article.getQuantity());
            sum += article.getQuantity();
            items.add(item);
        }


        Map<String, Object> print = new HashMap<>();
        print.put("TABLE_NO", "");
        print.put("KITCHEN_NAME", printer.getName());
        print.put("PORT", printer.getPort());
        print.put("ORDER_ID", order.getOrderId());
        print.put("IP", printer.getIp());
        String print_id = ApplicationUtils.randomUUID();
        print.put("PRINT_TASK_ID", print_id);
        print.put("ADD_TIME", new Date());

        Map<String, Object> data = new HashMap<>();
        data.put("ORDER_ID", order.getOrderId());
        data.put("ITEMS", items);

        data.put("DISTRIBUTION_MODE", "饿了么订单");
        data.put("ORIGINAL_AMOUNT", order.getOriginalPrice());
        data.put("RESTAURANT_ADDRESS", shopDetail.getAddress());
        data.put("REDUCTION_AMOUNT", order.getOriginalPrice().subtract(order.getTotalPrice()));
        data.put("RESTAURANT_TEL", shopDetail.getPhone());
        data.put("TABLE_NUMBER", "");
        data.put("PAYMENT_AMOUNT", order.getTotalPrice());
        data.put("RESTAURANT_NAME", shopDetail.getName());
        data.put("DATETIME", DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
        data.put("ARTICLE_COUNT", sum);
        print.put("DATA", data);
        print.put("STATUS", 0);

        print.put("TICKET_TYPE", TicketType.RECEIPT);
        data.put("ORDER_NUMBER", "");

        return print;
    }


    @Override
    public Boolean orderAccept(Map map, BrandSetting brandSetting) {
        String pushType = map.get("pushType").toString();
        String brandId = map.get("brandId").toString();
        List<Platform> platformList = platformService.selectByBrandId(brandId);
        Boolean result = false;
        if (CollectionUtils.isEmpty(platformList)) {
            return false;
        }

        try {
            switch (pushType) {
                case PushType.HUNGER:
                    //饿了吗推送接口
                    Boolean check = false;
                    for (Platform platform : platformList) {
                        if (platform.getName().equals(PlatformName.E_LE_ME)) {
                            check = true;
                            break;
                        }
                    }
                    if (check) {
                        result = hungerPush(map, brandSetting,brandId);
                    }
                    break;
                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
        return result;
    }

    private Boolean hungerPush(Map map, BrandSetting brandSetting,String brandId) throws Exception {
        String pushAction;
        if (StringUtils.isEmpty(map.get("push_action"))) {
            return false;
        } else {
            pushAction = map.get("push_action").toString();

        }
        if (pushAction.equals(PushAction.NEW_ORDER)) { //新订   单
            String[] ids =  addHungerOrder(map, brandSetting);
            //扣除库存
            for(String id : ids){
                HungerOrder order = hungerOrderMapper.selectById(id);
                String shopId = shopDetailService.selectByRestaurantId(order.getRestaurantId()).getId();
                MQMessageProducer.sendPlatformOrderMessage(id,PlatformType.E_LE_ME,brandId,shopId);
            }

        } else if (pushAction.equals(PushAction.ORDER_STATUS_UPDATGE)) { //订单状态更新
            updateHungerOrder(map.get("eleme_order_id").toString(), Integer.valueOf(map.get("new_status").toString()));
        } else if (pushAction.equals(PushAction.REFUND_ORDER)) { //退单
            String orderId = map.get("eleme_order_id").toString();
            updateHungerOrder(map.get("eleme_order_id").toString(), Integer.valueOf(map.get("refund_status").toString()));
            //退单的时候 加入 记录
            if (map.get("refund_status").toString().equals(HungerStatus.REFUND_SUCCESS)) { //退款成功
                HungerOrder hungerOrder = getHungerOrderById(orderId, brandSetting);
                OrderPaymentItem item = new OrderPaymentItem();
                item.setId(ApplicationUtils.randomUUID());
                item.setOrderId(orderId);
                item.setPaymentModeId(PayMode.HUNGER_MONEY);
                item.setPayTime(new Date());
                item.setPayValue(hungerOrder.getOriginalPrice());
                item.setRemark("饿了么退款:" + hungerOrder.getOriginalPrice().multiply(new BigDecimal(-1)));
                item.setResultData("原价:" + hungerOrder.getOriginalPrice() + "，订单总价(减去优惠后的价格)：" + hungerOrder.getTotalPrice());
                orderPaymentItemService.insert(item);

                List<HungerOrderDetail> details = hungerOrderMapper.selectDetailsById(orderId);
                String shopId = shopDetailService.selectByRestaurantId(hungerOrder.getRestaurantId()).getId();
                if (!CollectionUtils.isEmpty(details)) {
                    for (HungerOrderDetail detail : details) {
                        updateStock(detail.getName(), shopId, detail.getQuantity(), StockType.STOCK_ADD);
                    }
                }
            }


            //还原库存
        } else if (pushAction.equals(PushAction.DELIVERY)) { //配送状态
            //目前对于配送状态推送不做处理
        }
        return true;
    }


    private void updateHungerOrder(String orderId, Integer orderState) {
        hungerOrderMapper.updateHungerOrder(orderId, orderState);
    }

    private HungerOrder getHungerOrderById(String orderId, BrandSetting brandSetting) throws Exception {
        JSONObject json = new JSONObject(HungerUtil.HungerConnection(new HashMap<String, String>(),
                "/order/" + orderId + "/", brandSetting.getConsumerKey(), brandSetting.getConsumerSecret()));
        if (json.optString("code").equals(CodeType.SUCCESS)) {
            JSONObject order = json.getJSONObject("data");
            HungerOrder hungerOrder = new HungerOrder(order);
            return hungerOrder;
        } else {
            return null;
        }

    }


    private String[] addHungerOrder(Map map, BrandSetting brandSetting) throws Exception {
        String orderIds = map.get("eleme_order_ids").toString();
        String[] ids = orderIds.split(","); //得到饿了吗的新增订单列表
        for (String id : ids) {
            JSONObject json = new JSONObject(HungerUtil.HungerConnection(new HashMap<String, String>(),
                    "/order/" + id + "/", brandSetting.getConsumerKey(), brandSetting.getConsumerSecret()));
            if (json.optString("code").equals(CodeType.SUCCESS)) {
                JSONObject order = json.getJSONObject("data");
                HungerOrder hungerOrder = new HungerOrder(order);
                hungerOrderMapper.insertHungerOrder(hungerOrder);

                OrderPaymentItem item = new OrderPaymentItem();
                item.setId(ApplicationUtils.randomUUID());
                item.setOrderId(id);
                item.setPaymentModeId(PayMode.HUNGER_MONEY);
                item.setPayTime(new Date());
                item.setPayValue(hungerOrder.getOriginalPrice());
                item.setRemark("饿了么付款:" + hungerOrder.getOriginalPrice());
                item.setResultData("原价:" + hungerOrder.getOriginalPrice() + "，订单总价(减去优惠后的价格)：" + hungerOrder.getTotalPrice());
                orderPaymentItemService.insert(item);

                JSONObject detail = order.optJSONObject("detail");
                if (detail != null) {
                    JSONArray array = detail.optJSONArray("extra");
                    if (array != null) {
                        for (int i = 0; i < array.length(); i++) {
                            HungerOrderExtra extra = new HungerOrderExtra(array.getJSONObject(i), order.optString("order_id"));
                            hungerOrderMapper.insertHungerExtra(extra);
                        }
                    }

                    JSONArray group = detail.optJSONArray("group");
                    if (group != null) {
                        for (int i = 0; i < group.length(); i++) {
                            HungerOrderGroup orderGroup = new HungerOrderGroup();
                            orderGroup.setOrderId(order.optString("order_id"));
                            hungerOrderMapper.insertHungerGroup(orderGroup);
                            JSONArray details = group.getJSONArray(i);
                            if (details != null) {
                                for (int k = 0; k < details.length(); k++) {
                                    JSONObject orderDetailJson = details.getJSONObject(k);
                                    HungerOrderDetail orderDetail = new HungerOrderDetail(orderDetailJson, orderGroup.getId(), order.optString("order_id"));
                                    String shopId = shopDetailService.selectByRestaurantId(hungerOrder.getRestaurantId()).getId();
                                    hungerOrderMapper.insertHungerOrderDetail(orderDetail);
                                    updateStock(orderDetail.getName(), shopId, orderDetail.getQuantity(), StockType.STOCK_MINUS);
                                    JSONArray garnish = orderDetailJson.optJSONArray("garnish");
                                    if (garnish != null) {
                                        for (int o = 0; o < garnish.length(); o++) {
                                            HungerOrderGarnish orderGarnish = new HungerOrderGarnish(garnish.getJSONObject(o), orderDetailJson.optString("id"),
                                                    order.optString("order_id"), orderGroup.getId());
                                            hungerOrderMapper.insertHungerOrderGarnish(orderGarnish);
                                            updateStock(orderGarnish.getName(), shopId, orderGarnish.getQuantity(), StockType.STOCK_MINUS);
                                        }

                                    }

                                }
                            }
                        }
                    }
                }
            }

        }
        return ids;
    }


    private Boolean updateStock(String name, String shopId, Integer count, String type) throws AppException {
        Article article = articleMapper.selectByName(name);
        orderMapper.updateArticleStock(article.getId(), type, count);
        orderMapper.setEmpty(article.getId());

        //同时更新套餐库存(套餐库存为 最小库存的单品)
        orderMapper.setStockBySuit(shopId);
        return true;
    }

}
