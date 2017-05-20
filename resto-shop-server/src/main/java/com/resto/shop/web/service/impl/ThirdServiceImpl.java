package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.enums.PlatformKey;
import com.resto.brand.core.util.*;
import com.resto.brand.web.dto.LogType;
import com.resto.brand.web.model.Brand;
import com.resto.brand.web.model.BrandSetting;
import com.resto.brand.web.model.Platform;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.PlatformService;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.shop.web.constant.*;
import com.resto.shop.web.dao.*;
import com.resto.shop.web.exception.AppException;
import com.resto.shop.web.model.*;
import com.resto.shop.web.producer.MQMessageProducer;
import com.resto.shop.web.service.*;
import eleme.openapi.sdk.api.entity.other.OMessage;
import eleme.openapi.sdk.api.entity.order.OGoodsGroup;
import eleme.openapi.sdk.api.entity.order.OGoodsItem;
import eleme.openapi.sdk.api.entity.order.OOrder;
import eleme.openapi.sdk.api.exception.ServiceException;
import eleme.openapi.sdk.config.Config;
import eleme.openapi.sdk.oauth.OAuthClient;
import eleme.openapi.sdk.oauth.response.Token;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.resto.brand.core.util.HttpClient.doPostAnsc;
import static com.resto.brand.core.util.LogUtils.url;

/**
 * Created by KONATA on 2016/10/28.
 * 饿了吗接口实现
 */
@RpcService
public class ThirdServiceImpl implements ThirdService {


    Logger log = LoggerFactory.getLogger(getClass());
    //用来添加打印小票的序号
    //添加两个Map 一个是订单纬度,一个是店铺纬度
    private static final Map<String, Map<String, Integer>> NUMBER_ORDER_MAP = new ConcurrentHashMap<>();

    private static final Map<String, Map<String, Integer>> NUMBER_SHOP_MAP = new ConcurrentHashMap<>();

    private static Config config = null;

    private static Token token = null;

    private static OAuthClient client = null;
    // 设置是否沙箱环境
    private static final boolean isSandbox = true;

    // 设置APP KEY
    private static final String key = "o6ph8ACwrY";

    // 设置APP SECRET
    private static final String secret = "11b1d008b10ecb1510dbdf100d1c97e1";

    // 回调地址
    private static String callbackUrl = "https://ecosystem.restoplus.cn/wechat/order/new/third/version2.0/test";

    static {
        config = new Config(isSandbox, key, secret);
        client = new OAuthClient(config);
        token = client.getTokenByCode("", "");
    }

    @Resource
    OrderPaymentItemService orderPaymentItemService;

    @Resource
    private PlatformOrderMapper platformorderMapper;

    @Resource
    private PlatformOrderDetailMapper platformorderdetailMapper;

    @Resource
    private PlatformOrderExtraMapper platformorderextraMapper;

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

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private BrandService brandService;
    @Resource
    private PlatformOrderService platformOrderService;
    @Resource
    private PlatformOrderDetailService platformOrderDetailService;
    @Resource
    private PlatformOrderExtraService platformOrderExtraService;


    @Override
    public List<Map<String, Object>> printOrderByPlatform(String platformId, Integer type) {
        List<Map<String, Object>> result = null;
        switch (type) {
            case PlatformKey.ELEME:
                result = printPlatformOrder(platformId, type);
                break;
            case PlatformKey.MEITUAN:
                result = printPlatformOrder(platformId, type);
                break;
            default:
                break;
        }
        return result;
    }

    /**
     * 打印第三方外卖订单（饿了么，美团，百度）
     */
    private List<Map<String, Object>> printPlatformOrder(String platformOrderId, int type) {
        List<Map<String, Object>> printTask = new ArrayList<>();
        PlatformOrder order = platformOrderService.selectByPlatformOrderId(platformOrderId, type);
        List<PlatformOrderDetail> orderDetailList = platformOrderDetailService.selectByPlatformOrderId(platformOrderId);
        List<PlatformOrderExtra> orderExtraList = platformOrderExtraService.selectByPlatformOrderId(platformOrderId);

        ShopDetail shopDetail = shopDetailService.selectByPrimaryKey(order.getShopDetailId());

        List<Printer> ticketPrinter = printerService.selectByShopAndType(order.getShopDetailId(), PrinterType.RECEPTION);

        for (Printer printer : ticketPrinter) {
            Map<String, Object> ticket = printPlatformOrderTicket(order, orderDetailList, orderExtraList, shopDetail, printer);
            if (ticket != null) {
                printTask.add(ticket);
            }
        }

        List<Map<String, Object>> kitchenTicket = printPlatformOrderKitchen(order, orderDetailList, shopDetail);
        if (!kitchenTicket.isEmpty()) {
            printTask.addAll(kitchenTicket);
        }

        return printTask;
    }

    private Map<String, Object> printPlatformOrderTicket(PlatformOrder order, List<PlatformOrderDetail> orderDetailList, List<PlatformOrderExtra> orderExtraList, ShopDetail shopDetail, Printer printer) {
        if (printer == null) {
            return null;
        }
        int sum = 0;
        List<Map<String, Object>> items = new ArrayList<>();
        for (PlatformOrderDetail orderDetail : orderDetailList) {
            Map<String, Object> item = new HashMap<>();
            item.put("SUBTOTAL", orderDetail.getPrice().doubleValue() * orderDetail.getQuantity());
            item.put("ARTICLE_NAME", orderDetail.getShowName());
            item.put("ARTICLE_COUNT", orderDetail.getQuantity());
            sum += orderDetail.getQuantity();
            items.add(item);
        }

        for (PlatformOrderExtra orderExtra : orderExtraList) {
            Map<String, Object> item = new HashMap<>();
            item.put("ARTICLE_NAME", orderExtra.getName());
            item.put("ARTICLE_COUNT", orderExtra.getQuantity());
            item.put("SUBTOTAL", orderExtra.getPrice().doubleValue());
            items.add(item);
        }

        Map<String, Object> print = new HashMap<>();
        print.put("TABLE_NO", "");
        print.put("KITCHEN_NAME", printer.getName());
        print.put("PORT", printer.getPort());
        print.put("ORDER_ID", order.getPlatformOrderId());
        print.put("IP", printer.getIp());
        String print_id = ApplicationUtils.randomUUID();
        print.put("PRINT_TASK_ID", print_id);
        print.put("ADD_TIME", new Date().getTime());
//
        Map<String, Object> data = new HashMap<>();
        data.put("ORDER_ID", order.getPlatformOrderId());
        data.put("ORDER_NUMBER", MemcachedUtils.get(order.getId() + "orderNumber"));
//        data.put("ORDER_NUMBER", nextNumber(order.getShopDetailId(), order.getPlatformOrderId()));
        data.put("ITEMS", items);
//
        data.put("DISTRIBUTION_MODE", "外卖");
        data.put("ORIGINAL_AMOUNT", order.getOriginalPrice());
        data.put("RESTAURANT_ADDRESS", shopDetail.getAddress());
        data.put("REDUCTION_AMOUNT", order.getOriginalPrice().subtract(order.getTotalPrice()));
        data.put("RESTAURANT_TEL", shopDetail.getPhone());
        data.put("TABLE_NUMBER", "");
        data.put("CUSTOMER_COUNT", 0);
        data.put("PAYMENT_AMOUNT", order.getTotalPrice());
        data.put("RESTAURANT_NAME", shopDetail.getName());
        data.put("DATETIME", DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
        data.put("ARTICLE_COUNT", sum);
//
        List<Map<String, Object>> paymentList = new ArrayList<>();
        Map<String, Object> payment = new HashMap<>();
        payment.put("PAYMENT_MODE", "222");
        payment.put("SUBTOTAL", 0);
        paymentList.add(payment);
        data.put("PAYMENT_ITEMS", paymentList);
        data.put("CUSTOMER_SATISFACTION_DEGREE", 0);
        data.put("CUSTOMER_SATISFACTION", "");
        data.put("CUSTOMER_PROPERTY", "");
        data.put("ALREADY_PAYED", order.getPayType());
        data.put("DELIVERY_SOURCE", PlatformKey.getPlatformName(order.getType()));
        data.put("DELIVERY_ADDRESS", order.getAddress() + "\n\n【备注】：" + order.getRemark());

        String phone = order.getPhone().replace("\"", "").replace("[", "").replace("]", "");

        data.put("CONTACT_NAME", order.getName());
        data.put("CONTACT_TEL", phone);
//
        print.put("DATA", data);
        print.put("STATUS", 0);
//
        print.put("TICKET_TYPE", TicketType.DeliveryReceipt);
        Brand brand = brandService.selectById(shopDetail.getBrandId());
        JSONObject json = new JSONObject(print);
//        UserActionUtils.writeToFtp(LogType.POS_LOG, brand.getBrandName(), shopDetail.getName()
//                , "订单:"+order.getOrderId()+"返回打印总单模版"+json.toString());
        log.info("订单:" + order.getPlatformOrderId() + "返回打印总单模版" + json.toString());
        Map map = new HashMap(4);
        map.put("brandName", brand.getBrandName());
        map.put("fileName", shopDetail.getName());
        map.put("type", "posAction");
        map.put("content", "外卖订单:" + order.getPlatformOrderId() + "返回打印外卖总单模版" + json.toString() + ",请求服务器地址为:" + MQSetting.getLocalIP());
        doPostAnsc(url, map);
        MemcachedUtils.put(print_id, print);
        List<String> printList = (List<String>) MemcachedUtils.get(shopDetail.getId() + "printList");
        if (printList == null) {
            printList = new ArrayList<>();
        }
        printList.add(print_id);
        MemcachedUtils.put(shopDetail.getId() + "printList", printList);
        return print;
    }

    public List<Map<String, Object>> printPlatformOrderKitchen(PlatformOrder order, List<PlatformOrderDetail> orderDetailList, ShopDetail shopDetail) {
        //每个厨房 所需制作的   菜品信息
        Map<String, List<PlatformOrderDetail>> kitchenArticleMap = new HashMap<String, List<PlatformOrderDetail>>();
        //厨房信息
        Map<String, Kitchen> kitchenMap = new HashMap<String, Kitchen>();
        //遍历 订单集合
        for (PlatformOrderDetail detail : orderDetailList) {
            //得到当前菜品 所关联的厨房信息
            Article article = articleMapper.selectByName(detail.getName(), shopDetail.getId());
            if (article == null) {
                continue;
            }
            String articleId = article.getId();
            List<Kitchen> kitchenList = kitchenService.selectInfoByArticleId(articleId);

            for (Kitchen kitchen : kitchenList) {
                String kitchenId = kitchen.getId().toString();
                kitchenMap.put(kitchenId, kitchen);//保存厨房信息
                //判断 厨房集合中 是否已经包含当前厨房信息
                if (!kitchenArticleMap.containsKey(kitchenId)) {
                    //如果没有 则新建
                    kitchenArticleMap.put(kitchenId, new ArrayList<PlatformOrderDetail>());
                }
                kitchenArticleMap.get(kitchenId).add(detail);
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
            if (printer.getTicketType() == TicketType.PRINT_TICKET) {
                for (PlatformOrderDetail article : kitchenArticleMap.get(kitchenId)) {
                    Map<String, Object> print = new HashMap<String, Object>();
                    print.put("PORT", printer.getPort());
                    print.put("IP", printer.getIp());
                    String print_id = ApplicationUtils.randomUUID();
                    print.put("PRINT_TASK_ID", print_id);
                    Map<String, Object> data = new HashMap<String, Object>();
                    data.put("ORDER_ID", order.getPlatformOrderId());
                    data.put("DATETIME", DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
                    data.put("DISTRIBUTION_MODE", "外卖");
                    data.put("TABLE_NUMBER", PlatformKey.getPlatformName(order.getType()));
                    data.put("ORDER_NUMBER", MemcachedUtils.get(order.getId() + "orderNumber"));
//                    data.put("ORDER_NUMBER", nextNumber(order.getShopDetailId(), order.getId()));
                    Map<String, Object> items = new HashMap<String, Object>();
                    items.put("ARTICLE_COUNT", article.getQuantity());
                    items.put("ARTICLE_NAME", article.getShowName());
                    data.put("ITEMS", items);
                    data.put("CUSTOMER_SATISFACTION", "暂无信息");
                    data.put("CUSTOMER_SATISFACTION_DEGREE", 0);
                    data.put("CUSTOMER_PROPERTY", "");
                    print.put("DATA", data);
                    print.put("STATUS", "0");
                    print.put("TICKET_TYPE", TicketType.KITCHEN);
                    printTask.add(print);
                    MemcachedUtils.put(print_id, print);
                    List<String> printList = (List<String>) MemcachedUtils.get(shopDetail.getId() + "printList");
                    if (printList == null) {
                        printList = new ArrayList<>();
                    }
                    printList.add(print_id);
                    MemcachedUtils.put(shopDetail.getId() + "printList", printList);
                }
            } else {
                for (PlatformOrderDetail article : kitchenArticleMap.get(kitchenId)) {
                    for (int i = 0; i < article.getQuantity(); i++) {
                        Map<String, Object> print = new HashMap<String, Object>();
                        print.put("TABLE_NO", "");
                        print.put("KITCHEN_NAME", printer.getName());
                        print.put("PORT", printer.getPort());
                        print.put("ORDER_ID", order.getPlatformOrderId());
                        print.put("IP", printer.getIp());
                        String print_id = ApplicationUtils.randomUUID();
                        print.put("PRINT_TASK_ID", print_id);
                        print.put("ADD_TIME", new Date());
                        Map<String, Object> data = new HashMap<String, Object>();
                        data.put("ORDER_ID", order.getPlatformOrderId());
                        data.put("ARTICLE_NAME", article.getName());
                        data.put("ARTICLE_NUMBER", i + "/" + article.getQuantity());
                        data.put("DISTRIBUTION_MODE", PlatformKey.getPlatformName(order.getType()));
                        data.put("ORIGINAL_AMOUNT", order.getOriginalPrice());
                        data.put("CUSTOMER_ADDRESS", order.getAddress());
                        data.put("REDUCTION_AMOUNT", order.getOriginalPrice().subtract(order.getTotalPrice()));
                        data.put("CUSTOMER_TEL", order.getPhone());
                        data.put("TABLE_NUMBER", "");
                        data.put("PAYMENT_AMOUNT", order.getTotalPrice());
                        data.put("RESTAURANT_NAME", shopDetail.getName());
                        data.put("DATETIME", DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
                        data.put("ARTICLE_COUNT", 1);
                        print.put("DATA", data);
                        print.put("STATUS", 0);
                        print.put("TICKET_TYPE", TicketType.DELIVERYLABEL);
                        printTask.add(print);
                        MemcachedUtils.put(print_id, print);
                        List<String> printList = (List<String>) MemcachedUtils.get(shopDetail.getId() + "printList");
                        if (printList == null) {
                            printList = new ArrayList<>();
                        }
                        printList.add(print_id);
                        MemcachedUtils.put(shopDetail.getId() + "printList", printList);
                    }

                }

            }

        }
        Brand brand = brandService.selectById(shopDetail.getBrandId());
        JSONArray json = new JSONArray(printTask);
//        UserActionUtils.writeToFtp(LogType.POS_LOG, brand.getBrandName(), shopDetail.getName()
//                , "订单:"+order.getOrderId()+"返回打印厨打模版"+json.toString());
        log.info("订单:" + order.getPlatformOrderId() + "返回打印厨打模版" + json.toString());
        Map map = new HashMap(4);
        map.put("brandName", brand.getBrandName());
        map.put("fileName", shopDetail.getName());
        map.put("type", "posAction");
        map.put("content", "外卖订单:" + order.getPlatformOrderId() + "返回打印外卖厨打模版" + json.toString() + ",请求服务器地址为:" + MQSetting.getLocalIP());
        doPostAnsc(url, map);
        return printTask;
    }


    private List<Map<String, Object>> printElemeOrder(String orderId) {
        List<Map<String, Object>> printTask = new ArrayList<>();
        HungerOrder hungerOrder = hungerOrderMapper.selectByOrderId(orderId);
        List<HungerOrderDetail> details = hungerOrderMapper.selectDetailsById(hungerOrder.getOrderId());
        ShopDetail shopDetail = shopDetailService.selectByRestaurantId(hungerOrder.getRestaurantId());
        Brand brand = brandService.selectById(shopDetail.getBrandId());
        UserActionUtils.writeToFtp(LogType.POS_LOG, brand.getBrandName(), shopDetail.getName()
                , "订单:" + orderId + "接收到饿了么订单！");
        List<Printer> ticketPrinter = printerService.selectByShopAndType(shopDetail.getId(), PrinterType.RECEPTION);
        for (Printer printer : ticketPrinter) {
            Map<String, Object> ticket = printTicket(hungerOrder, details, shopDetail, printer);
            if (ticket != null) {
                printTask.add(ticket);
            }

        }

        List<Map<String, Object>> kitchenTicket = printKitchen(hungerOrder, details, shopDetail);
        if (!kitchenTicket.isEmpty()) {
            printTask.addAll(kitchenTicket);
        }

        return printTask;
    }


    public List<Map<String, Object>> printKitchen(HungerOrder order, List<HungerOrderDetail> articleList, ShopDetail shopDetail) {
        //每个厨房 所需制作的   菜品信息
        Map<String, List<HungerOrderDetail>> kitchenArticleMap = new HashMap<String, List<HungerOrderDetail>>();
        //厨房信息
        Map<String, Kitchen> kitchenMap = new HashMap<String, Kitchen>();
        //遍历 订单集合
        for (HungerOrderDetail item : articleList) {
            //得到当前菜品 所关联的厨房信息
            Article article = articleMapper.selectByName(item.getName(), shopDetail.getId());
            if (article == null) {
                continue;
            }
            String articleId = article.getId();
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
            if (printer.getTicketType() == TicketType.PRINT_TICKET) {
                for (HungerOrderDetail article : kitchenArticleMap.get(kitchenId)) {
                    Map<String, Object> print = new HashMap<String, Object>();
                    print.put("PORT", printer.getPort());
                    print.put("IP", printer.getIp());
                    String print_id = ApplicationUtils.randomUUID();
                    print.put("PRINT_TASK_ID", print_id);
                    Map<String, Object> data = new HashMap<String, Object>();
                    data.put("ORDER_ID", order.getOrderId());
                    data.put("DATETIME", DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
                    data.put("DISTRIBUTION_MODE", "外卖");
                    data.put("TABLE_NUMBER", "饿了么");
                    data.put("ORDER_NUMBER", MemcachedUtils.get(order.getId() + "orderNumber"));
//                    data.put("ORDER_NUMBER", nextNumber(order.getRestaurantId().toString(), order.getId().toString()));
                    Map<String, Object> items = new HashMap<String, Object>();
                    items.put("ARTICLE_COUNT", article.getQuantity());
                    items.put("ARTICLE_NAME", article.getName());
                    data.put("ITEMS", items);
                    data.put("CUSTOMER_SATISFACTION", "暂无信息");
                    data.put("CUSTOMER_SATISFACTION_DEGREE", 0);
                    data.put("CUSTOMER_PROPERTY", "");
                    print.put("DATA", data);
                    print.put("STATUS", "0");
                    print.put("TICKET_TYPE", TicketType.KITCHEN);
                    printTask.add(print);
                    MemcachedUtils.put(print_id, print);
                    List<String> printList = (List<String>) MemcachedUtils.get(shopDetail.getId() + "printList");
                    if (printList == null) {
                        printList = new ArrayList<>();
                    }
                    printList.add(print_id);
                    MemcachedUtils.put(shopDetail.getId() + "printList", printList);
                }
            } else {
                for (HungerOrderDetail article : kitchenArticleMap.get(kitchenId)) {
                    for (int i = 0; i < article.getQuantity(); i++) {
                        Map<String, Object> print = new HashMap<String, Object>();
                        print.put("TABLE_NO", "");
                        print.put("KITCHEN_NAME", printer.getName());
                        print.put("PORT", printer.getPort());
                        print.put("ORDER_ID", order.getOrderId());
                        print.put("IP", printer.getIp());
                        String print_id = ApplicationUtils.randomUUID();
                        print.put("PRINT_TASK_ID", print_id);
                        print.put("ADD_TIME", new Date());
                        Map<String, Object> data = new HashMap<String, Object>();
                        data.put("ORDER_ID", order.getOrderId());
                        data.put("ARTICLE_NAME", article.getName());
                        data.put("ARTICLE_NUMBER", i + "/" + article.getQuantity());
                        data.put("DISTRIBUTION_MODE", "饿了吗外卖");
                        data.put("ORIGINAL_AMOUNT", order.getOriginalPrice());
                        data.put("CUSTOMER_ADDRESS", order.getAddress());
                        data.put("REDUCTION_AMOUNT", order.getOriginalPrice().subtract(order.getTotalPrice()));
                        data.put("CUSTOMER_TEL", order.getPhoneList());
                        data.put("TABLE_NUMBER", "");
                        data.put("PAYMENT_AMOUNT", order.getTotalPrice());
                        data.put("RESTAURANT_NAME", shopDetail.getName());
                        data.put("DATETIME", DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
                        data.put("ARTICLE_COUNT", 1);
                        print.put("DATA", data);
                        print.put("STATUS", 0);
                        print.put("TICKET_TYPE", TicketType.DELIVERYLABEL);
                        printTask.add(print);
                        MemcachedUtils.put(print_id, print);
                        List<String> printList = (List<String>) MemcachedUtils.get(shopDetail.getId() + "printList");
                        if (printList == null) {
                            printList = new ArrayList<>();
                        }
                        printList.add(print_id);
                        MemcachedUtils.put(shopDetail.getId() + "printList", printList);
                    }

                }

            }

        }
        Brand brand = brandService.selectById(shopDetail.getBrandId());
        JSONArray json = new JSONArray(printTask);
//        UserActionUtils.writeToFtp(LogType.POS_LOG, brand.getBrandName(), shopDetail.getName()
//                , "订单:"+order.getOrderId()+"返回打印厨打模版"+json.toString());
        log.info("订单:" + order.getOrderId() + "返回打印厨打模版" + json.toString());
        Map map = new HashMap(4);
        map.put("brandName", brand.getBrandName());
        map.put("fileName", shopDetail.getName());
        map.put("type", "posAction");
        map.put("content", "外卖订单:" + order.getOrderId() + "返回打印外卖厨打模版" + json.toString() + ",请求服务器地址为:" + MQSetting.getLocalIP());
        doPostAnsc(url, map);
        return printTask;
    }

    public Map<String, Object> printTicket(HungerOrder order, List<HungerOrderDetail> orderItems, ShopDetail shopDetail, Printer printer) {
        if (printer == null) {
            return null;
        }
        int sum = 0;
        List<Map<String, Object>> items = new ArrayList<>();
        for (HungerOrderDetail article : orderItems) {
            String articleName = article.getName();
            if (article.getSpecs() != null) {
                JSONArray jsonArray = new JSONArray(article.getSpecs());
                for (int i = 0; i < jsonArray.length(); i++) {
                    articleName += ("(" + jsonArray.get(i) + ")");
                }
            }
            Map<String, Object> item = new HashMap<>();
            item.put("SUBTOTAL", article.getPrice().doubleValue() * article.getQuantity());
            item.put("ARTICLE_NAME", articleName);
            item.put("ARTICLE_COUNT", article.getQuantity());
            sum += article.getQuantity();
            items.add(item);
        }

        List<HungerOrderExtra> extras = hungerOrderMapper.getExtra(order.getOrderId().toString());
        for (HungerOrderExtra extra : extras) {
            Map<String, Object> item = new HashMap<>();
            item.put("SUBTOTAL", extra.getPrice().doubleValue() * extra.getQuantity());
            item.put("ARTICLE_NAME", extra.getName());
            item.put("ARTICLE_COUNT", extra.getQuantity());
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
        print.put("ADD_TIME", new Date().getTime());
//
        Map<String, Object> data = new HashMap<>();
        data.put("ORDER_ID", order.getOrderId());


        String orderNumber = (String) MemcachedUtils.get(order.getId() + "orderNumber");
        Integer orderTotal = (Integer) MemcachedUtils.get(order.getShopDetailId()+"deliveryCount");
        if(orderTotal == null){
            orderTotal = 0;
        }else if (orderNumber == null){
            orderTotal++;
        }
        MemcachedUtils.put(order.getShopDetailId()+"deliveryCount",orderTotal);


        String number;
        if(orderTotal < 10){
            number = "00"+orderTotal;
        }else if(orderTotal < 100){
            number = "0"+orderTotal;
        }else{
            number = ""+orderTotal;
        }

        if(org.apache.commons.lang3.StringUtils.isEmpty(orderNumber)){
            orderNumber = number;
        }
        MemcachedUtils.put(order.getId()+"orderNumber",orderNumber);




        data.put("ORDER_NUMBER",orderNumber);
        data.put("ITEMS", items);
//
        data.put("DISTRIBUTION_MODE", "外卖");
        data.put("ORIGINAL_AMOUNT", order.getOriginalPrice());
        data.put("RESTAURANT_ADDRESS", shopDetail.getAddress());
        data.put("REDUCTION_AMOUNT", order.getOriginalPrice().subtract(order.getTotalPrice()));
        data.put("RESTAURANT_TEL", shopDetail.getPhone());
        data.put("TABLE_NUMBER", "");
        data.put("CUSTOMER_COUNT", 0);
        data.put("PAYMENT_AMOUNT", order.getTotalPrice());
        data.put("RESTAURANT_NAME", shopDetail.getName());
        data.put("DATETIME", DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
        data.put("ARTICLE_COUNT", sum);
//
        List<Map<String, Object>> paymentList = new ArrayList<>();
        Map<String, Object> payment = new HashMap<>();
        payment.put("PAYMENT_MODE", "222");
        payment.put("SUBTOTAL", 0);
        paymentList.add(payment);
        data.put("PAYMENT_ITEMS", paymentList);
        data.put("CUSTOMER_SATISFACTION_DEGREE", 0);
        data.put("CUSTOMER_SATISFACTION", "");
        data.put("CUSTOMER_PROPERTY", "");
        data.put("ALREADY_PAYED", "已在线支付");
        data.put("DELIVERY_SOURCE", "饿了吗");
        data.put("DELIVERY_ADDRESS", order.getAddress());

        String phone = order.getPhoneList().replace("\"", "");
        phone = phone.replace("[", "");
        phone = phone.replace("]", "");

        data.put("CONTACT_NAME", order.getConsignee());
        data.put("CONTACT_TEL", phone);
//
        print.put("DATA", data);
        print.put("STATUS", 0);
//
        print.put("TICKET_TYPE", TicketType.DeliveryReceipt);
        Brand brand = brandService.selectById(shopDetail.getBrandId());
        JSONObject json = new JSONObject(print);
//        UserActionUtils.writeToFtp(LogType.POS_LOG, brand.getBrandName(), shopDetail.getName()
//                , "订单:"+order.getOrderId()+"返回打印总单模版"+json.toString());
        log.info("订单:" + order.getOrderId() + "返回打印总单模版" + json.toString());
        Map map = new HashMap(4);
        map.put("brandName", brand.getBrandName());
        map.put("fileName", shopDetail.getName());
        map.put("type", "posAction");
        map.put("content", "外卖订单:" + order.getOrderId() + "返回打印外卖总单模版" + json.toString() + ",请求服务器地址为:" + MQSetting.getLocalIP());
        doPostAnsc(url, map);
        MemcachedUtils.put(print_id, print);
        List<String> printList = (List<String>) MemcachedUtils.get(shopDetail.getId() + "printList");
        if (printList == null) {
            printList = new ArrayList<>();
        }
        printList.add(print_id);
        MemcachedUtils.put(shopDetail.getId() + "printList", printList);
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
                        result = hungerPush(map, brandSetting, brandId);
                    }
                    break;
                case PushType.HUNGER_VERSION_2:
                    check = false;
                    for (Platform platform : platformList) {
                        if (platform.getName().equals(PlatformName.E_LE_ME)) {
                            check = true;
                            break;
                        }
                    }
                    if (check) {
                        result = hungerPushVersion2(map, brandId);
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            return false;
        }
        return result;
    }

    private Boolean hungerPushVersion2(Map map, String brandId) throws ServiceException {
        String oMessage =  map.get("oMessage").toString();
        log.info("oMessage   信息：" + oMessage);
        com.alibaba.fastjson.JSONObject messageJson = com.alibaba.fastjson.JSONObject.parseObject(oMessage);
        Integer type = Integer.parseInt(messageJson.getString("type"));
        log.info("type  信息：" + type);
        String message = messageJson.getString("message");
        log.info("message   信息：" + message);
        message = message.replaceAll("\\\\","");
        log.info("message 2222  信息：" + message);
        com.alibaba.fastjson.JSONObject mj = com.alibaba.fastjson.JSONObject.parseObject(message);
        String orderId = mj.getString("orderId");
        if(type == ElemeType.NEW_ORDER){
            addHungerOrderVersion2(orderId);
        }else if(type == ElemeType.RECEIVE_ORDER){
            String shopId = shopDetailService.selectByOOrderShopId(Long.parseLong(map.get("shopId").toString())).getId();
            MQMessageProducer.sendPlatformOrderMessage(orderId, PlatformType.E_LE_ME, brandId, shopId);
        }
        return true;
    }

    private Boolean hungerPush(Map map, BrandSetting brandSetting, String brandId) throws Exception {
        String pushAction;
        if (StringUtils.isEmpty(map.get("push_action"))) {
            return false;
        } else {
            pushAction = map.get("push_action").toString();

        }
        if (pushAction.equals(PushAction.NEW_ORDER)) { //新订   单
            addHungerOrder(map, brandSetting);
            //扣除库存
//            for (String id : ids) {
//                HungerOrder order = hungerOrderMapper.selectByOrderId(id);
//                String shopId = shopDetailService.selectByRestaurantId(order.getRestaurantId()).getId();
//                MQMessageProducer.sendPlatformOrderMessage(id, PlatformType.E_LE_ME, brandId, shopId);
//            }

        } else if (pushAction.equals(PushAction.ORDER_STATUS_UPDATGE)) { //订单状态更新
            updateHungerOrder(map.get("eleme_order_id").toString(), Integer.valueOf(map.get("new_status").toString()));
            if (Integer.valueOf(map.get("new_status").toString()).equals(ProductionStatus.PRINTED)) {
                HungerOrder order = hungerOrderMapper.selectByOrderId(map.get("eleme_order_id").toString());
                String shopId = shopDetailService.selectByRestaurantId(order.getRestaurantId()).getId();
                MQMessageProducer.sendPlatformOrderMessage(map.get("eleme_order_id").toString(), PlatformType.E_LE_ME, brandId, shopId);
            }
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
//                    for (HungerOrderDetail detail : details) {
//                        updateStock(detail.getName(), shopId, detail.getQuantity(), StockType.STOCK_ADD);
//                    }
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
                "/order/" + orderId + "/", brandSetting.getConsumerKey(), brandSetting.getConsumerSecret(), RequestMethod.GET));
        if (json.optString("code").equals(CodeType.SUCCESS)) {
            JSONObject order = json.getJSONObject("data");
            HungerOrder hungerOrder = new HungerOrder(order);
            return hungerOrder;
        } else {
            return null;
        }

    }

    private void addHungerOrderVersion2(String orderId) throws ServiceException {
        token.setAccessToken("9298323a28d096bea2564c1a8527ed62");
        token.setRefreshToken("8db12491e26826aad5d67551da32b104");
        token.setExpires(86400);
        token.setTokenType("bearer");

        eleme.openapi.sdk.api.service.OrderService orderService = new eleme.openapi.sdk.api.service.OrderService(config, token);
        OOrder order = orderService.getOrder(orderId);
        log.info("当前order信息获取成功：" + order.getShopId());
        String shopId = shopDetailService.selectByOOrderShopId(order.getShopId()).getId();
        log.info("当前餐加系统shopId：" + shopId);
        PlatformOrder platformOrder = new PlatformOrder(order,shopId);
        log.info("init初始化PlatformOrder成功！");
        platformorderMapper.insertSelective(platformOrder);

        List<OGoodsGroup> group = order.getGroups();
        if (group != null) {
            for (int i = 0; i < group.size(); i++) {
                OGoodsGroup g = group.get(i);
                for(int j = 0; j < g.getItems().size(); j++){
                    OGoodsItem detail = g.getItems().get(j);
                    PlatformOrderDetail platformOrderDetail = new PlatformOrderDetail(detail, order.getId());
                    platformorderdetailMapper.insertSelective(platformOrderDetail);
                }

            }
        }
        if(order.getServiceFee() > 0){
            PlatformOrderExtra platformOrderExtra = new PlatformOrderExtra(order, 1);
            platformorderextraMapper.insertSelective(platformOrderExtra);
        }
        if(order.getPackageFee() > 0){
            PlatformOrderExtra platformOrderExtra = new PlatformOrderExtra(order, 2);
            platformorderextraMapper.insertSelective(platformOrderExtra);
        }
    }

    private String[] addHungerOrder(Map map, BrandSetting brandSetting) throws Exception {
        String orderIds = map.get("eleme_order_ids").toString();
        String[] ids = orderIds.split(","); //得到饿了吗的新增订单列表
        for (String id : ids) {
            JSONObject json = new JSONObject(HungerUtil.HungerConnection(new HashMap<String, String>(),
                    "/order/" + id + "/", brandSetting.getConsumerKey(), brandSetting.getConsumerSecret(), RequestMethod.GET));
            if (json.optString("code").equals(CodeType.SUCCESS)) {
                JSONObject order = json.getJSONObject("data");
                HungerOrder hungerOrder = new HungerOrder(order);
                String shopId = shopDetailService.selectByRestaurantId(hungerOrder.getRestaurantId()).getId();
                hungerOrder.setShopDetailId(shopId);
                PlatformOrder platformOrder = new PlatformOrder(hungerOrder);
                platformorderMapper.insertSelective(platformOrder);
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
                            PlatformOrderExtra platformOrderExtra = new PlatformOrderExtra(extra);
                            platformorderextraMapper.insertSelective(platformOrderExtra);
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
                                    PlatformOrderDetail platformOrderDetail = new PlatformOrderDetail(orderDetail);
                                    platformorderdetailMapper.insertSelective(platformOrderDetail);
                                    hungerOrderMapper.insertHungerOrderDetail(orderDetail);
//                                    updateStock(orderDetail.getName(), shopId, orderDetail.getQuantity(), StockType.STOCK_MINUS);
                                    JSONArray garnish = orderDetailJson.optJSONArray("garnish");
                                    if (garnish != null) {
                                        for (int o = 0; o < garnish.length(); o++) {
                                            HungerOrderGarnish orderGarnish = new HungerOrderGarnish(garnish.getJSONObject(o), orderDetailJson.optString("id"),
                                                    order.optString("order_id"), orderGroup.getId());
                                            hungerOrderMapper.insertHungerOrderGarnish(orderGarnish);
//                                            updateStock(orderGarnish.getName(), shopId, orderGarnish.getQuantity(), StockType.STOCK_MINUS);
                                        }

                                    }

                                }
                            }
                        }
                    }
                }
                ShopDetail shopDetail = shopDetailService.selectByRestaurantId(hungerOrder.getRestaurantId());
                Brand brand = brandService.selectById(shopDetail.getBrandId());
                Map addHungermap = new HashMap(4);
                addHungermap.put("brandName", brand.getBrandName());
                addHungermap.put("fileName", shopDetail.getName());
                addHungermap.put("type", "posAction");
                addHungermap.put("content", "店铺:" + shopDetail.getName() + "接收到新增的饿了么订单:" + hungerOrder.getId() + ",请求服务器地址为:" + MQSetting.getLocalIP());
                doPostAnsc(url, addHungermap);
            }

        }
        return ids;
    }


    private Boolean updateStock(String name, String shopId, Integer count, String type) throws AppException {
        Article article = articleMapper.selectByName(name, shopId);
        if (article == null) {
            return null;
        }
        orderMapper.updateArticleStock(article.getId(), type, count);
        orderMapper.setEmpty(article.getId());

        //同时更新套餐库存(套餐库存为 最小库存的单品)
        orderMapper.setStockBySuit(shopId);
        return true;
    }


    @Override
    public List<HungerOrder> getOutFoodList(String shopId) {
        ShopDetail shopDetail = shopDetailService.selectByPrimaryKey(shopId);
        List<HungerOrder> hungerOrders = hungerOrderMapper.getOutFoodList(shopDetail.getId());
        return hungerOrders;
    }


    @Override
    public HungerOrder getOutFoodInfo(String id) {
//        PlatformOrder order = platformorderMapper.selectOrderInfo(id);
        HungerOrder order = hungerOrderMapper.selectById(id);

//        List<HungerOrderDetail> orderDetail = hungerOrderMapper.selectDetailsById(order.getOrderId());
//        order.setDetails(orderDetail);
        order.setShopName(shopDetailService.selectById(order.getShopDetailId()).getName());
        return order;
    }


    public Map<String, Object> printReceipt(String orderId, Integer selectPrinterId) {


        PlatformOrder order = platformOrderService.selectById(orderId);
        List<PlatformOrderDetail> orderDetailList = platformOrderDetailService.selectByPlatformOrderId(order.getPlatformOrderId());
        List<PlatformOrderExtra> orderExtraList = platformOrderExtraService.selectByPlatformOrderId(order.getPlatformOrderId());

        ShopDetail shopDetail = shopDetailService.selectByPrimaryKey(order.getShopDetailId());


        if (selectPrinterId == null) {
            List<Printer> printer = printerService.selectByShopAndType(shopDetail.getId(), PrinterType.RECEPTION);
            if (printer.size() > 0) {
                return printPlatformOrderTicket(order, orderDetailList, orderExtraList, shopDetail, printer.get(0));
            }
        } else {
            Printer p = printerService.selectById(selectPrinterId);
            return printPlatformOrderTicket(order, orderDetailList, orderExtraList, shopDetail, p);
        }
        return null;
    }


    @Override
    public List<Map<String, Object>> printKitchenReceipt(String oid) {
        List<Map<String, Object>> printTask = new ArrayList<>();
        PlatformOrder order = platformOrderService.selectById(oid);
        List<PlatformOrderDetail> orderDetailList = platformOrderDetailService.selectByPlatformOrderId(order.getPlatformOrderId());

        ShopDetail shopDetail = shopDetailService.selectByPrimaryKey(order.getShopDetailId());




        List<Map<String, Object>> kitchenTicket = printPlatformOrderKitchen(order, orderDetailList, shopDetail);
        if (!kitchenTicket.isEmpty()) {
            printTask.addAll(kitchenTicket);
        }

        return printTask;
    }


    //根据店铺id和订单id获取订单序号的方法
    private String nextNumber(String sid, String oid) {
        //定义number
        int number;
        //先从订单map中查找
        String key = DateUtil.formatDate(new Date(), "yyyy-MM-dd");
        //查看orderMap中是否有值
        Map<String, Integer> ordermap = NUMBER_ORDER_MAP.get(key);
        if (ordermap == null) {
            NUMBER_ORDER_MAP.clear();
            ordermap = new HashMap<>();
            NUMBER_ORDER_MAP.put(key, ordermap);
        }
        Map<String, Integer> shopmap = NUMBER_SHOP_MAP.get(key);
        if (shopmap == null) {
            NUMBER_SHOP_MAP.clear();
            shopmap = new HashMap<>();
            NUMBER_SHOP_MAP.put(key, shopmap);
        }
        //从ordermap里面找有没有number，有就返回
        //没有的话，找shopmap里面的数字是多少，如果没有就是1，如果有就+1 并分别存入shopmap和ordermap
        Integer num1 = ordermap.get(oid);
        if (num1 != null) {
            number = num1.intValue();
        } else {
            Integer num2 = shopmap.get(sid);
            if (num2 != null) {
                number = num2.intValue() + 1;
                ordermap.put(oid, number);
                shopmap.put(sid, number);
            } else {
                shopmap.put(sid, 1);
                ordermap.put(oid, 1);
                number = 1;
            }
        }
        return numberToString(number);
    }

    //int转String('001')
    public String numberToString(int num) {
        Format f = new DecimalFormat("000");
        return f.format(num);
    }


}
