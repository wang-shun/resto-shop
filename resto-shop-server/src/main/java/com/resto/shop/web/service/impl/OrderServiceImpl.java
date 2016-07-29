package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.entity.Result;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.ApplicationUtils;
import com.resto.brand.core.util.DateUtil;
import com.resto.brand.core.util.WeChatPayUtils;
import com.resto.brand.web.dto.ArticleSellDto;
import com.resto.brand.web.dto.OrderPayDto;
import com.resto.brand.web.dto.ShopArticleReportDto;
import com.resto.brand.web.dto.brandArticleReportDto;
import com.resto.brand.web.model.*;
import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.BrandSettingService;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.brand.web.service.WechatConfigService;
import com.resto.shop.web.constant.*;
import com.resto.shop.web.container.OrderProductionStateContainer;
import com.resto.shop.web.dao.ArticlePriceMapper;
import com.resto.shop.web.dao.MealAttrMapper;
import com.resto.shop.web.dao.OrderMapper;
import com.resto.shop.web.datasource.DataSourceContextHolder;
import com.resto.shop.web.exception.AppException;
import com.resto.shop.web.model.*;
import com.resto.shop.web.producer.MQMessageProducer;
import com.resto.shop.web.service.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.json.JSONObject;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
@RpcService
public class OrderServiceImpl extends GenericServiceImpl<Order, String> implements OrderService {

    //用来添加打印小票的序号
    //添加两个Map 一个是订单纬度,一个是店铺纬度
    private static final Map<String, Map<String, Integer>> NUMBER_ORDER_MAP = new ConcurrentHashMap<>();

    private static final Map<String, Map<String, Integer>> NUMBER_SHOP_MAP = new ConcurrentHashMap<>();

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private CustomerService customerService;

    @Resource
    private ArticleService articleService;

    @Resource
    private ArticlePriceService articlePriceService;

    @Resource
    private CouponService couponService;

    @Resource
    OrderPaymentItemService orderPaymentItemService;

    @Resource
    private AccountService accountService;

    @Resource
    OrderItemService orderItemService;

    @Resource
    ShopCartService shopCartService;

    @Resource
    WechatConfigService wechatConfigService;

    @Resource
    OrderProductionStateContainer orderProductionStateContainer;

    @Resource
    BrandSettingService brandSettingService;

    @Resource
    BrandService brandService;

    @Resource
    ShopDetailService shopDetailService;

    @Resource
    KitchenService kitchenService;

    @Resource
    PrinterService printerService;

    @Resource
    MealItemService mealItemService;

    @Resource
    ChargeOrderService chargeOrderService;

    @Resource
    MealAttrMapper mealAttrMapper;

    @Resource
    ArticlePriceMapper articlePriceMapper;

    @Override
    public GenericDao<Order, String> getDao() {
        return orderMapper;
    }

    @Override
    public List<Order> listOrder(Integer start, Integer datalength, String shopId, String customerId, String ORDER_STATE) {
        String[] states = null;
        if (ORDER_STATE != null) {
            states = ORDER_STATE.split(",");
        }
        return orderMapper.orderList(start, datalength, shopId, customerId, states);
    }

    @Override
    public Order selectOrderStatesById(String orderId) {
        return orderMapper.selectOrderStatesById(orderId);
    }

    @Override
    public Order createOrder(Order order) throws AppException {
        String orderId = ApplicationUtils.randomUUID();
        order.setId(orderId);
        Customer customer = customerService.selectById(order.getCustomerId());
        if (customer == null) {
            throw new AppException(AppException.CUSTOMER_NOT_EXISTS);
        } else if (customer.getTelephone() == null) {
            throw new AppException(AppException.NOT_BIND_PHONE);
        } else if (order.getOrderItems().isEmpty()) {
            throw new AppException(AppException.ORDER_ITEMS_EMPTY);
        }
        List<Article> articles = articleService.selectList(order.getShopDetailId());
        List<ArticlePrice> articlePrices = articlePriceService.selectList(order.getShopDetailId());
        Map<String, Article> articleMap = ApplicationUtils.convertCollectionToMap(String.class, articles);
        Map<String, ArticlePrice> articlePriceMap = ApplicationUtils.convertCollectionToMap(String.class,
                articlePrices);

        order.setVerCode(customer.getTelephone().substring(7));
        order.setId(orderId);
        order.setCreateTime(new Date());
        BigDecimal totalMoney = BigDecimal.ZERO;
        int articleCount = 0;
        for (OrderItem item : order.getOrderItems()) {
            Article a = null;
            BigDecimal org_price = null;
            BigDecimal price = null;
            BigDecimal fans_price = null;
            item.setId(ApplicationUtils.randomUUID());
            switch (item.getType()) {
                case OrderItemType.ARTICLE:
                    // 查出 item对应的 商品信息，并将item的原价，单价，总价，商品名称，商品详情 设置为对应的
                    a = articleMap.get(item.getArticleId());
                    item.setArticleName(a.getName());
                    org_price = a.getPrice();
                    price = a.getPrice();
                    fans_price = a.getFansPrice();
                    break;
                case OrderItemType.UNITPRICE:
                    ArticlePrice p = articlePriceMap.get(item.getArticleId());
                    a = articleMap.get(p.getArticleId());
                    item.setArticleName(a.getName() + p.getName());
                    org_price = p.getPrice();
                    price = p.getPrice();
                    fans_price = p.getFansPrice();
                    break;
                case OrderItemType.SETMEALS:
                    a = articleMap.get(item.getArticleId());
                    item.setArticleName(a.getName());
                    org_price = a.getPrice();
                    price = a.getPrice();
                    fans_price = a.getFansPrice();
                    Integer[] mealItemIds = item.getMealItems();
                    List<MealItem> items = mealItemService.selectByIds(mealItemIds);
                    item.setChildren(new ArrayList<OrderItem>());
                    for (MealItem mealItem : items) {
                        OrderItem child = new OrderItem();
                        Article ca = articleMap.get(mealItem.getArticleId());
                        child.setId(ApplicationUtils.randomUUID());
                        child.setArticleName(mealItem.getName());
                        child.setArticleId(ca.getId());
                        child.setCount(item.getCount());
                        child.setArticleDesignation(ca.getDescription());
                        child.setParentId(item.getId());
                        child.setOriginalPrice(mealItem.getPriceDif());
                        child.setStatus(1);
                        child.setSort(0);
                        child.setUnitPrice(mealItem.getPriceDif());
                        child.setType(OrderItemType.MEALS_CHILDREN);
                        BigDecimal finalMoney = child.getUnitPrice().multiply(new BigDecimal(child.getCount())).setScale(2, BigDecimal.ROUND_HALF_UP);
                        child.setFinalPrice(finalMoney);
                        child.setOrderId(orderId);
                        totalMoney = totalMoney.add(finalMoney).setScale(2, BigDecimal.ROUND_HALF_UP);
                        item.getChildren().add(child);
                    }
                    break;
                default:
                    throw new AppException(AppException.UNSUPPORT_ITEM_TYPE, "不支持的餐品类型:" + item.getType());
            }
            item.setArticleDesignation(a.getDescription());
            item.setOriginalPrice(org_price);
            item.setStatus(1);
            item.setSort(0);
            if (fans_price != null) {
                item.setUnitPrice(fans_price);
            } else {
                item.setUnitPrice(price);
            }
            BigDecimal finalMoney = item.getUnitPrice().multiply(new BigDecimal(item.getCount())).setScale(2, BigDecimal.ROUND_HALF_UP);
            articleCount += item.getCount();
            item.setFinalPrice(finalMoney);
            item.setOrderId(orderId);
            totalMoney = totalMoney.add(finalMoney).setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        orderItemService.insertItems(order.getOrderItems());
        BigDecimal payMoney = totalMoney;

        // 使用优惠卷
        if (order.getUseCoupon() != null) {
            Coupon coupon = couponService.useCoupon(totalMoney, order);
            OrderPaymentItem item = new OrderPaymentItem();
            item.setId(ApplicationUtils.randomUUID());
            item.setOrderId(orderId);
            item.setPaymentModeId(PayMode.COUPON_PAY);
            item.setPayTime(order.getCreateTime());
            item.setPayValue(coupon.getValue());
            item.setRemark("优惠卷支付:" + item.getPayValue());
            item.setResultData(coupon.getId());
            orderPaymentItemService.insert(item);
            payMoney = payMoney.subtract(item.getPayValue()).setScale(2, BigDecimal.ROUND_HALF_UP);
        }

        // 使用余额
        if (payMoney.doubleValue() > 0 && order.isUseAccount()) {
            BigDecimal payValue = accountService.payOrder(order, payMoney, customer);
//			BigDecimal payValue = accountService.useAccount(payMoney, account,AccountLog.SOURCE_PAYMENT);
            if (payValue.doubleValue() > 0) {
                payMoney = payMoney.subtract(payValue.setScale(2, BigDecimal.ROUND_HALF_UP));

//				OrderPaymentItem item = new OrderPaymentItem();
//				item.setId(ApplicationUtils.randomUUID());
//				item.setOrderId(orderId);
//				item.setPaymentModeId(PayMode.ACCOUNT_PAY);
//				item.setPayTime(order.getCreateTime());
//				item.setPayValue(payValue);
//				item.setRemark("余额支付:" + item.getPayValue());
//				item.setResultData(account.getId());
//				orderPaymentItemService.insert(item);
//				payMoney = payMoney.subtract(item.getPayValue()).setScale(2, BigDecimal.ROUND_HALF_UP);
            }
        }

        if (payMoney.doubleValue() < 0) {
            payMoney = BigDecimal.ZERO;
        }
        order.setAccountingTime(order.getCreateTime()); // 财务结算时间
        order.setAllowCancel(true); // 订单是否允许取消
        order.setAllowAppraise(false);
        order.setArticleCount(articleCount); // 订单餐品总数
        order.setClosed(false); // 订单是否关闭 否
        order.setSerialNumber(DateFormatUtils.format(new Date(), "yyyyMMddHHmmssSSSS")); // 流水号
        order.setOriginalAmount(totalMoney);// 原价
        order.setReductionAmount(BigDecimal.ZERO);// 折扣金额
        order.setOrderMoney(totalMoney); // 订单实际金额
        order.setPaymentAmount(payMoney); // 订单剩余需要维修支付的金额
        order.setPrintTimes(0);
        order.setOrderState(OrderState.SUBMIT);
        order.setProductionStatus(ProductionStatus.NOT_ORDER);
        ShopDetail detail = shopDetailService.selectById(order.getShopDetailId());
        order.setOrderMode(detail.getShopMode());
        if (order.getOrderMode() == ShopMode.CALL_NUMBER) {
            order.setTableNumber(order.getVerCode());
        }
        if (order.getParentOrderId() != null) {
            Order parentOrder = selectById(order.getParentOrderId());
            order.setTableNumber(parentOrder.getTableNumber());
        }
        insert(order);
        customerService.changeLastOrderShop(order.getShopDetailId(), order.getCustomerId());
        if (order.getPaymentAmount().doubleValue() == 0) {
            payOrderSuccess(order);
        }
        return order;
    }

    public Order payOrderSuccess(Order order) {
        order.setOrderState(OrderState.PAYMENT);
        update(order);
        if (order.getParentOrderId() != null) {  //子订单
            Order parent = selectById(order.getParentOrderId());
            int articleCountWithChildren = selectArticleCountById(parent.getId());
            if (parent.getLastOrderTime() == null || parent.getLastOrderTime().getTime() < order.getCreateTime().getTime()) {
                parent.setLastOrderTime(order.getCreateTime());
            }
            Double amountWithChildren = orderMapper.selectParentAmount(parent.getId());
            parent.setCountWithChild(articleCountWithChildren);
            parent.setAmountWithChildren(new BigDecimal(amountWithChildren));
            update(parent);
            try {
                order = pushOrder(order.getId());
                log.info("子订单，自动下单成功：" + order.getId());
                MQMessageProducer.sendPlaceOrderMessage(order);
            } catch (AppException e) {
                e.printStackTrace();
                log.error("子订单自动下单失败:" + e.getMessage());
                changePushOrder(order);
            }
        }
        return order;
    }

    private int selectArticleCountById(String id) {
        return orderMapper.selectArticleCountById(id);
    }

    @Override
    public Order findCustomerNewOrder(String customerId, String shopId, String orderId) {
        Date beginDate = DateUtil.getDateBegin(new Date());
        return findCustomerNewOrder(beginDate, customerId, shopId, orderId);
    }

    public Order findCustomerNewOrder(Date beginDate, String customerId, String shopId, String orderId) {
        Integer[] orderState = new Integer[]{OrderState.SUBMIT, OrderState.PAYMENT, OrderState.CONFIRM};
        Order order = orderMapper.findCustomerNewOrder(beginDate, customerId, shopId, orderState, orderId);
        if (order != null) {
            if (order.getParentOrderId() != null) {
                return findCustomerNewOrder(customerId, shopId, order.getParentOrderId());
            }
            List<OrderItem> itemList = orderItemService.listByOrderId(order.getId());
            order.setOrderItems(itemList);
            List<String> childIds = selectChildIdsByParentId(order.getId());
            List<OrderItem> childItems = orderItemService.listByOrderIds(childIds);
            order.getOrderItems().addAll(childItems);
        }
        return order;
    }

    private List<String> selectChildIdsByParentId(String id) {
        return orderMapper.selectChildIdsByParentId(id);
    }

    @Override
    public List<Order> selectByParentId(String parentOrderId) {
        return orderMapper.selectByParentId(parentOrderId);
    }

    @Override
    public boolean cancelOrder(String orderId) {
        Order order = selectById(orderId);
        if (order.getAllowCancel() && order.getProductionStatus() != ProductionStatus.PRINTED && (order.getOrderState().equals(OrderState.SUBMIT) || order.getOrderState() == OrderState.PAYMENT)) {
            order.setAllowCancel(false);
            order.setClosed(true);
            order.setAllowAppraise(false);
            order.setAllowContinueOrder(false);
            order.setOrderState(OrderState.CANCEL);
            update(order);
            refundOrder(order);
            log.info("取消订单成功:" + order.getId());
            return true;
        } else {
            log.warn("取消订单失败，订单状态订单状态或者订单可取消字段为false" + order.getId());
            return false;
        }
    }


    @Override
    public Boolean checkRefundLimit(Order order) {
        Integer orderMode = order.getOrderMode();
        Boolean result = false;
        switch (orderMode) {
            case ShopMode.MANUAL_ORDER: //验证码下单
            case ShopMode.CALL_NUMBER: //电视叫号
            case ShopMode.TABLE_MODE: //坐下点餐
                result = ((order.getOrderState().equals(OrderState.CONFIRM) ||
                        order.getOrderState().equals(OrderState.PAYMENT))
                        &&
                        order.getProductionStatus().equals(ProductionStatus.NOT_PRINT))
                        || (order.getOrderState().equals(OrderState.PAYMENT) &&
                        order.getProductionStatus().equals(ProductionStatus.NOT_ORDER));
                break;
            default:
                log.info("未知的店铺模式:" + orderMode);
                break;
        }

        return result;
    }

    @Override
    public boolean autoRefundOrder(String orderId) {
        Order order = selectById(orderId);
        if (order.getAllowCancel()) {
            order.setAllowCancel(false);
            order.setClosed(true);
            order.setAllowAppraise(false);
            order.setAllowContinueOrder(false);
            order.setOrderState(OrderState.CANCEL);
            update(order);
            refundOrder(order);
            log.info("自动退款成功:" + order.getId());
            return true;
        } else {
            log.warn("款项自动退还到相应账户失败，订单状态不是已付款或商品状态不是已付款未下单" + order.getId());
            return false;
        }

    }

    private void refundOrder(Order order) {
        List<OrderPaymentItem> payItemsList = orderPaymentItemService.selectByOrderId(order.getId());
        for (OrderPaymentItem item : payItemsList) {
            String newPayItemId = ApplicationUtils.randomUUID();
            switch (item.getPaymentModeId()) {
                case PayMode.COUPON_PAY:
                    couponService.refundCoupon(item.getResultData());
                    break;
                case PayMode.ACCOUNT_PAY:
                    accountService.addAccount(item.getPayValue(), item.getResultData(), "取消订单返还", AccountLog.SOURCE_CANCEL_ORDER);
                    break;
                case PayMode.CHARGE_PAY:
                    chargeOrderService.refundCharge(item.getPayValue(), item.getResultData());
                    break;
                case PayMode.REWARD_PAY:
                    chargeOrderService.refundReward(item.getPayValue(), item.getResultData());
                    break;
                case PayMode.WEIXIN_PAY:
                    WechatConfig config = wechatConfigService.selectByBrandId(DataSourceContextHolder.getDataSourceName());
                    JSONObject obj = new JSONObject(item.getResultData());
                    Map<String, String> result = WeChatPayUtils.refund(newPayItemId, obj.getString("transaction_id"),
                            obj.getInt("total_fee"), obj.getInt("total_fee"), config.getAppid(), config.getMchid(),
                            config.getMchkey(), config.getPayCertPath());
                    item.setResultData(new JSONObject(result).toString());
                    break;
            }
            item.setId(newPayItemId);
            item.setPayValue(item.getPayValue().multiply(new BigDecimal(-1)));
            orderPaymentItemService.insert(item);
        }
    }

    @Override
    public Order orderWxPaySuccess(OrderPaymentItem item) {
        Order order = selectById(item.getOrderId());
        OrderPaymentItem historyItem = orderPaymentItemService.selectById(item.getId());
        if (historyItem == null) {
            orderPaymentItemService.insert(item);
            payOrderSuccess(order);
        } else {
            log.warn("该笔支付记录已经处理过:" + item.getId());
        }
        return order;
    }

    @Override
    public Order pushOrder(String orderId) throws AppException {
        Order order = selectById(orderId);
        if (validOrderCanPush(order)) {
            order.setProductionStatus(ProductionStatus.HAS_ORDER);
            order.setPushOrderTime(new Date());
            update(order);
            return order;
        }
        return null;
    }

    private boolean validOrderCanPush(Order order) throws AppException {
        if (order.getOrderState() != OrderState.PAYMENT || ProductionStatus.NOT_ORDER != order.getProductionStatus()) {
            log.error("立即下单失败: " + order.getId());
            throw new AppException(AppException.ORDER_STATE_ERR);
        }
        switch (order.getOrderMode()) {
            case 1:
                if (order.getTableNumber() == null) {
                    throw new AppException(AppException.ORDER_MODE_CHECK, "桌号不得为空");
                }
                break;
        }
        return true;
    }

    @Override
    public Order callNumber(String orderId) {
        Order order = selectById(orderId);
        if (order.getCallNumberTime() == null) {
            order.setProductionStatus(ProductionStatus.HAS_CALL);
            order.setCallNumberTime(new Date());
            update(order);
        }
        return order;
    }

    @Override
    public List<Map<String, Object>> getPrintData(String orderId) {

        return null;
    }

    @Override
    public Order printSuccess(String orderId) throws AppException {
        Order order = selectById(orderId);
        if (order.getPrintOrderTime() == null) {
            if (StringUtils.isEmpty(order.getParentOrderId())) {
                log.info("打印成功，订单为主订单，允许加菜-:" + order.getId());
                order.setAllowContinueOrder(true);
            } else {
                log.info("打印成功，订单为子订单:" + order.getId() + " pid:" + order.getParentOrderId());
                order.setAllowContinueOrder(false);
                order.setAllowAppraise(false);
            }
            order.setProductionStatus(ProductionStatus.PRINTED);
            order.setPrintOrderTime(new Date());
            order.setAllowCancel(false);
            update(order);
            return order;
        }
        throw new AppException(AppException.ORDER_IS_PRINTED);
    }

    @Override
    public List<Order> selectTodayOrder(String shopId, int[] proStatus) {
        Date date = DateUtil.getDateBegin(new Date());
        List<Order> orderList = orderMapper.selectShopOrderByDateAndProductionStates(shopId, date, proStatus);
        return orderList;
    }

    @Override
    public List<Order> selectReadyOrder(String currentShopId) {
        List<Order> order = orderMapper.selectReadyList(currentShopId);
        return order;
    }

    @Override
    public List<Order> selectPushOrder(String currentShopId, Long lastTime) {
        return orderProductionStateContainer.getPushOrderList(currentShopId, lastTime);
    }

    @Override
    public List<Order> selectCallOrder(String currentBrandId, Long lastTime) {
        return orderProductionStateContainer.getCallNowList(currentBrandId, lastTime);
    }


    @Override
    public List<Map<String, Object>> printKitchen(Order order, List<OrderItem> articleList) {
        //每个厨房 所需制作的   菜品信息
        Map<String, List<OrderItem>> kitchenArticleMap = new HashMap<String, List<OrderItem>>();
        //厨房信息
        Map<String, Kitchen> kitchenMap = new HashMap<String, Kitchen>();
        //遍历 订单集合
        for (OrderItem item : articleList) {
            //得到当前菜品 所关联的厨房信息
            String articleId = item.getArticleId();
            if (item.getType() == OrderItemType.UNITPRICE) {
                if (articleId.length() > 32) {
                    articleId = item.getArticleId().substring(0, 32);
                } else {
                    ArticlePrice price = articlePriceService.selectById(articleId);
                    if (price != null) {
                        articleId = price.getArticleId();
                    }
                }
            } else if (item.getType() == OrderItemType.MEALS_CHILDREN) {
                continue;
            }

            if (OrderItemType.SETMEALS == item.getType()) {
                Kitchen kitchen = kitchenService.selectMealKitchen(item);
                if (kitchen != null) {
                    String kitchenId = kitchen.getId().toString();
                    kitchenMap.put(kitchenId, kitchen);
                    if (!kitchenArticleMap.containsKey(kitchenId)) {
                        //如果没有 则新建
                        kitchenArticleMap.put(kitchenId, new ArrayList<OrderItem>());
                    }
                    kitchenArticleMap.get(kitchenId).add(item);
                }
            } else {
                List<Kitchen> kitchenList = kitchenService.selectInfoByArticleId(articleId);
                for (Kitchen kitchen : kitchenList) {
                    String kitchenId = kitchen.getId().toString();
                    kitchenMap.put(kitchenId, kitchen);//保存厨房信息
                    //判断 厨房集合中 是否已经包含当前厨房信息
                    if (!kitchenArticleMap.containsKey(kitchenId)) {
                        //如果没有 则新建
                        kitchenArticleMap.put(kitchenId, new ArrayList<OrderItem>());
                    }
                    kitchenArticleMap.get(kitchenId).add(item);
                }
            }
        }

        //桌号
        String tableNumber = order.getTableNumber() != null ? order.getTableNumber() : "";
        //打印线程集合
        List<Map<String, Object>> printTask = new ArrayList<Map<String, Object>>();

        String modeText = getModeText(order);//就餐模式
        String serialNumber = order.getSerialNumber();//序列号

        //编列 厨房菜品 集合
        for (String kitchenId : kitchenArticleMap.keySet()) {
            Kitchen kitchen = kitchenMap.get(kitchenId);//得到厨房 信息
            Printer printer = printerService.selectById(kitchen.getPrinterId());//得到打印机信息
            if (printer == null) {
                continue;
            }
            //生成厨房小票
            for (OrderItem article : kitchenArticleMap.get(kitchenId)) {
                //保存 菜品的名称和数量
                List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
                Map<String, Object> item = new HashMap<String, Object>();
                item.put("ARTICLE_NAME", article.getArticleName());
                item.put("ARTICLE_COUNT", article.getCount());
                items.add(item);
                if (article.getType() == OrderItemType.SETMEALS) {
                    if (article.getChildren() != null && !article.getChildren().isEmpty()) {
                        for (OrderItem child : article.getChildren()) {
                            Map<String, Object> child_item = new HashMap<String, Object>();
                            child_item.put("ARTICLE_NAME", child.getArticleName());
                            child_item.put("ARTICLE_COUNT", child.getCount());
                            items.add(child_item);
                        }
                    }
                }
                //保存基本信息
                Map<String, Object> data = new HashMap<String, Object>();
                data.put("KITCHEN_NAME", kitchen.getName());
                data.put("DISTRIBUTION_MODE", modeText);
                data.put("TABLE_NUMBER", tableNumber);
                data.put("ORDER_ID", serialNumber);
                data.put("DATE", DateUtil.formatDate(new Date(), "MM-dd HH:mm"));
                data.put("ITEMS", items);
                //添加当天打印订单的序号
                data.put("NUMBER", nextNumber(order.getShopDetailId(), order.getId()));
                //保存打印配置信息
                Map<String, Object> print = new HashMap<String, Object>();
                String print_id = ApplicationUtils.randomUUID();
                print.put("PRINT_TASK_ID", print_id);
                print.put("STATUS", 0);
                print.put("ORDER_ID", serialNumber);
                print.put("KITCHEN_NAME", kitchen.getName());
                print.put("DATA", data);
                print.put("TABLE_NO", tableNumber);
                print.put("IP", printer.getIp());
                print.put("PORT", printer.getPort());
                print.put("ADD_TIME", new Date());
                print.put("TICKET_TYPE", TicketType.KITCHEN);
                //添加到 打印集合
                printTask.add(print);
            }
        }

        return printTask;
    }


    private String getModeText(Order order) {
        if (order == null) {
            return "";
        }
        String text = DistributionType.getModeText(order.getDistributionModeId());
        if (order.getParentOrderId() != null) {  //如果是加菜的订单，会出现加的字样
            text += " (加)";
        }
        return text;
    }

    @Override
    public Map<String, Object> printReceipt(String orderId, Integer selectPrinterId) {
        // 根据id查询订单
        Order order = selectById(orderId);
        //查询店铺
        ShopDetail shopDetail = shopDetailService.selectById(order.getShopDetailId());
        // 查询订单菜品
        List<OrderItem> orderItems = orderItemService.listByOrderId(orderId);
        if (selectPrinterId == null) {
            List<Printer> printer = printerService.selectByShopAndType(shopDetail.getId(), PrinterType.RECEPTION);
            if (printer.size() > 0) {
                return printTicket(order, orderItems, shopDetail, printer.get(0));
            }
        } else {
            Printer p = printerService.selectById(selectPrinterId);
            return printTicket(order, orderItems, shopDetail, p);
        }
        return null;
    }


    public Map<String, Object> printTicket(Order order, List<OrderItem> orderItems, ShopDetail shopDetail, Printer printer) {
        if (printer == null) {
            return null;
        }
        List<Map<String, Object>> items = new ArrayList<>();
        for (OrderItem article : orderItems) {
            Map<String, Object> item = new HashMap<>();
            item.put("ARTICLE_NAME", article.getArticleName());
            item.put("ARTICLE_COUNT", article.getCount());
            item.put("SUBTOTAL", article.getFinalPrice());
            items.add(item);
        }

        Map<String, Object> data = new HashMap<>();
        String modeText = getModeText(order);
        data.put("DISTRIBUTION_MODE", modeText);

        data.put("ARTICLE_COUNT", order.getArticleCount());
        data.put("RESTAURANT_NAME", shopDetail.getName());
        data.put("RESTAURANT_ADDRESS", shopDetail.getAddress());
        data.put("RESTAURANT_TEL", shopDetail.getPhone());
        data.put("TABLE_NUMBER", order.getTableNumber());
        data.put("ORDER_ID", order.getSerialNumber() + "-" + order.getVerCode());
        data.put("DATETIME", DateUtil.formatDate(new Date(), "MM-dd HH:mm"));
        data.put("ITEMS", items);
        data.put("ORIGINAL_AMOUNT", order.getOriginalAmount());
        data.put("REDUCTION_AMOUNT", order.getReductionAmount());
        data.put("PAYMENT_AMOUNT", order.getPaymentAmount());
        //添加当天小票的打印的序号
        data.put("NUMBER", nextNumber(shopDetail.getId(), order.getId()));

        // 根据shopDetailId查询出打印机类型为2的打印机(前台打印机)
        Map<String, Object> print = new HashMap<>();
        String print_id = ApplicationUtils.randomUUID();
        print.put("PRINT_TASK_ID", print_id);
        print.put("STATUS", 0);
        print.put("ORDER_ID", order.getSerialNumber());

        print.put("KITCHEN_NAME", printer.getName());
        print.put("DATA", data);
        print.put("TABLE_NO", order.getTableNumber());
        print.put("IP", printer.getIp());
        print.put("PORT", printer.getPort());
        print.put("ADD_TIME", new Date());
        print.put("TICKET_TYPE", TicketType.RECEIPT);
        return print;
    }


    @Override
    public Order confirmOrder(Order order) {
        order = selectById(order.getId());
        if (order.getConfirmTime() == null && !order.getClosed()) {
            order.setOrderState(OrderState.CONFIRM);
            order.setConfirmTime(new Date());
            order.setAllowCancel(false);
            BrandSetting setting = brandSettingService.selectByBrandId(order.getBrandId());
            if (order.getParentOrderId() == null) {
                log.info("如果订单金额大于 评论金额 则允许评论" + order.getId());
                if (setting.getAppraiseMinMoney().compareTo(order.getOrderMoney()) <= 0 || setting.getAppraiseMinMoney().compareTo(order.getAmountWithChildren()) <= 0) {
                    order.setAllowAppraise(true);
                }
            } else {
                log.info("最小评论金额为:" + setting.getAppraiseMinMoney() + ", oid:" + order.getId());
                order.setAllowAppraise(false);
            }
            update(order);
            return order;
        }
        return null;
    }

    @Override
    public Order getOrderInfo(String orderId) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        List<OrderItem> orderItems = orderItemService.listByOrderId(orderId);
        order.setOrderItems(orderItems);
        Customer cus = customerService.selectById(order.getCustomerId());
        order.setCustomer(cus);
        return order;
    }

    @Override
    public List<Order> selectHistoryOrderList(String currentShopId, Date date) {
        Date begin = DateUtil.getDateBegin(date);
        Date end = DateUtil.getDateEnd(date);
        return orderMapper.selectHistoryOrderList(currentShopId, begin, end);

    }

    @Override
    public List<Order> selectErrorOrderList(String currentShopId, Date date) {
        Date begin = DateUtil.getDateBegin(date);
        Date end = DateUtil.getDateEnd(date);
        return orderMapper.selectErrorOrderList(currentShopId, begin, end);
    }

    @Override
    public Order cancelOrderPos(String orderId) throws AppException {
        Order order = selectById(orderId);
        if (order.getClosed()) {
            throw new AppException(AppException.ORDER_IS_CLOSED);
        } else {
            order.setClosed(true);
            order.setAllowAppraise(false);
            order.setAllowContinueOrder(false);
            order.setAllowCancel(false);
            order.setOrderState(OrderState.CANCEL);
            update(order);
            refundOrder(order);
            log.info("取消订单成功:" + order.getId());
        }
        return order;
    }

    @Override
    public void changePushOrder(Order order) {
        order = selectById(order.getId());
        if (order.getProductionStatus() == ProductionStatus.HAS_ORDER) { //如果还是已下单状态，则将订单状态改为未下单,并且订单改为可以取消
            orderMapper.clearPushOrder(order.getId(), ProductionStatus.NOT_ORDER);
        }
    }

    @Override
    public List<Map<String, Object>> printOrderAll(String orderId) {
        log.info("打印订单全部:" + orderId);
        Order order = selectById(orderId);
        ShopDetail shop = shopDetailService.selectById(order.getShopDetailId());
        List<OrderItem> items = orderItemService.listByOrderId(orderId);
        List<Map<String, Object>> printTask = new ArrayList<>();
        List<Printer> ticketPrinter = printerService.selectByShopAndType(shop.getId(), PrinterType.RECEPTION);
        for (Printer printer : ticketPrinter) {
            Map<String, Object> ticket = printTicket(order, items, shop, printer);
            if (ticket != null) {
                printTask.add(ticket);
            }

        }
        List<Map<String, Object>> kitchenTicket = printKitchen(order, items);

        //如果是外带，添加一张外带小票
        if (order.getDistributionModeId().equals(DistributionType.TAKE_IT_SELF)) {
            List<Printer> packagePrinter = printerService.selectByShopAndType(order.getShopDetailId(), PrinterType.PACKAGE); //查找外带的打印机
            for (Printer printer : packagePrinter) {
                Map<String, Object> packageTicket = printTicket(order, items, shop, printer);
                if (packageTicket != null) {
                    printTask.add(packageTicket);
                }
            }
        }

        if (!kitchenTicket.isEmpty()) {
            printTask.addAll(kitchenTicket);
        }
        return printTask;
    }

    @Override
    public void setTableNumber(String orderId, String tableNumber) {
        orderMapper.setOrderNumber(orderId, tableNumber);
    }

    @Override
    public List<Order> selectOrderByVercode(String vercode, String shopId) {
        List<Order> orderList = orderMapper.selectOrderByVercode(vercode, shopId);
        return orderList;
    }

    @Override
    public List<Order> selectOrderByTableNumber(String tableNumber, String shopId) {
        List<Order> orderList = orderMapper.selectOrderByTableNumber(tableNumber, shopId);
        return orderList;
    }

    @Override
    public void updateDistributionMode(Integer modeId, String orderId) {
        Order order = selectById(orderId);
        order.setDistributionModeId(modeId);
        orderMapper.updateByPrimaryKeySelective(order);
    }

    @Override
    public void clearNumber(String currentShopId) {
        orderProductionStateContainer.clearMap(currentShopId);

    }

    @Override
    public List<Order> listOrderByStatus(String currentShopId, Date begin, Date end, int[] productionStatus,
                                         int[] orderState) {
        return orderMapper.listOrderByStatus(currentShopId, begin, end, productionStatus, orderState);
    }

    @Override
    public void updateAllowContinue(String id, boolean b) {
        orderMapper.changeAllowContinue(id, b);
    }

    @Override
    public Order findCustomerNewPackage(String currentCustomerId, String currentShopId) {
        String oid = orderMapper.selectNewCustomerPackageId(currentCustomerId, currentShopId);
        Order order = null;
        if (StringUtils.isNoneBlank(oid)) {
            Date beginDate = DateUtil.getAfterDayDate(new Date(), -15);
            order = findCustomerNewOrder(beginDate, null, null, oid);
        }
        return order;
    }

//	@Override
//	public SaleReportDto selectArticleSumCountByData(String beginDate,String endDate,String brandId) {
//		Date begin = DateUtil.getformatBeginDate(beginDate);
//		Date end = DateUtil.getformatEndDate(endDate);
//		List<ShopDetail> list_shopDetail = shopDetailService.selectByBrandId(brandId);
//		int totalNum = 0;
//		for(ShopDetail shop : list_shopDetail){
//			int sellNum = orderMapper.selectArticleSumCountByData(begin, end, shop.getId());
//			totalNum += sellNum;
//			shop.setArticleSellNum(sellNum);
//		}
//		SaleReportDto saleReport = new SaleReportDto(list_shopDetail.get(0).getBrandName(), totalNum, list_shopDetail);
//		return saleReport;
//	}

    @Override
    public List<ArticleSellDto> selectShopArticleSellByDate(String beginDate, String endDate, String shopId, String sort) {
        Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);
        if ("0asc".equals(sort)) {
            sort = "r.peference ,r.sort";
        } else if ("2".equals(sort.substring(0, 1))) {
            sort = "r.shopSellNum" + " " + sort.substring(1, sort.length());
        } else if ("3".equals(sort.subSequence(0, 1))) {
            sort = "r.brandSellNum" + " " + sort.substring(1, sort.length());
        }
        //ShopDetail shop = shopDetailService.selectById(shopId);


//		else if("4".equals(sort.substring(0,1))){
//			sort="salesRatio"+" "+sort.substring(1,sort.length());
//		}

        List<ArticleSellDto> list = orderMapper.selectShopArticleSellByDate(begin, end, shopId, sort);


        //计算总菜品销售额,//菜品总销售额
        double num = 0;

        BigDecimal temp = BigDecimal.ZERO;
        for (ArticleSellDto articleSellDto : list) {
            //计算总销量 不能加上套餐的数量
            if (articleSellDto.getType() != 3) {
                num += articleSellDto.getShopSellNum().doubleValue();
            }
            //计算总销售额
            temp = add(temp, articleSellDto.getSalles());
        }

        for (ArticleSellDto articleSellDto : list) {
            //销售额占比
            BigDecimal d = articleSellDto.getSalles().divide(temp, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
            articleSellDto.setSalesRatio(d + "%");

            if (num != 0) {
                double d1 = articleSellDto.getShopSellNum().doubleValue();
                double d2 = d1 / num * 100;

                //保留两位小数
                BigDecimal b = new BigDecimal(d2);
                double f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                articleSellDto.setNumRatio(f1 + "%");
            }

        }

        return list;

    }

    @Override
    public List<ArticleSellDto> selectBrandArticleSellByDate(String beginDate, String endDate, String sort) {
        Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);
        if ("0".equals(sort)) {
            sort = "f.peference ,a.sort";
        } else if ("desc".equals(sort)) {
            sort = "brand_report.brandSellNum desc";
        } else if ("asc".equals(sort)) {
            sort = "brand_report.brandSellNum asc";
        }
        List<ArticleSellDto> list = orderMapper.selectBrandArticleSellByDate(begin, end, sort);
        return list;
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

    @Override
    public List<ArticleSellDto> selectBrandArticleSellByDateAndArticleFamilyId(String beginDate, String endDate,
                                                                               String articleFamilyId, String sort) {
        Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);
        if ("0".equals(sort)) {
            sort = "f.peference ,a.sort";
        } else if ("desc".equals(sort)) {
            sort = "brand_report.brandSellNum desc";
        } else if ("asc".equals(sort)) {
            sort = "brand_report.brandSellNum asc";
        }
        return orderMapper.selectBrandArticleSellByDateAndArticleFamilyId(begin, end, articleFamilyId, sort);
    }

    @Override
    public List<ArticleSellDto> selectShopArticleSellByDateAndArticleFamilyId(String beginDate, String endDate, String shopId, String articleFamilyId, String sort) {
        Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);
        if ("0asc".equals(sort)) {
            sort = "f.peference ,a.sort";
        } else if ("2".equals(sort.substring(0, 1))) {
            sort = "shop_report.shopSellNum" + sort.substring(1, sort.length());
        } else if ("3".equals(sort.subSequence(0, 1))) {
            sort = "brand_report.brandSellNum" + sort.substring(1, sort.length());
        } else if ("4".equals(sort.substring(0, 1))) {
            sort = "salesRatio" + sort.substring(1, sort.length());
        }

        return orderMapper.selectShopArticleSellByDateAndArticleFamilyId(begin, end, shopId, articleFamilyId, sort);
    }

    @Override
    public List<ArticleSellDto> selectShopArticleByDate(String shopId, String beginDate, String endDate,
                                                        String sort) {
        Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);
        if ("0".equals(sort)) {
            sort = "peference ,sort";
        } else if ("desc".equals(sort)) {
            sort = "shopSellNum desc";
        } else if ("asc".equals(sort)) {
            sort = "shopSellNum asc";
        }
        List<ArticleSellDto> list = orderMapper.selectShopArticleByDate(shopId, begin, end, sort);
        return list;

    }

    @Override
    public List<ArticleSellDto> selectShopArticleByDateAndArcticleFamilyId(String beginDate, String endDate, String shopId,
                                                                           String articleFamilyId, String sort) {
        Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);
        if ("0".equals(sort)) {
            sort = "f.peference ,a.sort";
        } else if ("desc".equals(sort)) {
            sort = "shop_report.shopSellNum desc";
        } else if ("asc".equals(sort)) {
            sort = "shop_report.shopSellNum asc";
        }
        return orderMapper.selectShopArticleByDateAndArticleFamilyId(begin, end, shopId, articleFamilyId, sort);
    }


    @Override
    public Boolean checkShop(String orderId, String shopId) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            return false;
        } else {
            return order.getShopDetailId().equals(shopId);
        }
    }


    @Override
    public brandArticleReportDto selectBrandArticleNum(String beginDate, String endDate, String brandId) {
        Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);
        Brand brand = brandService.selectById(brandId);
        int totalNum = 0;
        totalNum = orderMapper.selectArticleSumCountByData(begin, end, brandId);
        brandArticleReportDto bo = new brandArticleReportDto(brand.getBrandName(), totalNum);
        return bo;
    }

    @Override
    public List<ShopArticleReportDto> selectShopArticleDetails(String beginDate, String endDate, String brandId) {
        Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);
        List<ShopDetail> list_shopDetail = shopDetailService.selectByBrandId(brandId);
        List<ShopArticleReportDto> list = orderMapper.selectShopArticleDetails(begin, end, brandId);
        List<ShopArticleReportDto> pFood = orderMapper.selectShopArticleCom(begin, end, brandId);

        List<ShopArticleReportDto> listArticles = new ArrayList<>();

        for (ShopDetail shop : list_shopDetail) {
            ShopArticleReportDto st = new ShopArticleReportDto(shop.getId(), shop.getName(), 0, BigDecimal.ZERO, "0.00%");
            listArticles.add(st);
        }

        BigDecimal sum = new BigDecimal(0);
        for (ShopArticleReportDto shopArticleReportDto2 : list) {
            for (ShopArticleReportDto shopArticleReportDto : pFood) {
                if (shopArticleReportDto2.getShopId().equals(shopArticleReportDto.getShopId())) {
                    shopArticleReportDto2.setSellIncome(shopArticleReportDto2.getSellIncome().add(shopArticleReportDto.getSellIncome()));
                    shopArticleReportDto2.setTotalNum(shopArticleReportDto2.getTotalNum() + shopArticleReportDto.getTotalNum());
                }
            }
            sum = sum.add(shopArticleReportDto2.getSellIncome());
        }


        if (!list.isEmpty()) {
            for (ShopArticleReportDto shopArticleReportDto : listArticles) {
                for (ShopArticleReportDto shopArticleReportDto2 : list) {
                    if (shopArticleReportDto2.getShopId().equals(shopArticleReportDto.getShopId())) {
                        shopArticleReportDto.setSellIncome(shopArticleReportDto2.getSellIncome());
                        shopArticleReportDto.setTotalNum(shopArticleReportDto2.getTotalNum());
                        BigDecimal current = shopArticleReportDto2.getSellIncome();

                        String occupy = current == null ? "0" : current.divide(sum, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP)
                                .toString();
                        shopArticleReportDto.setOccupy(occupy + "%");
                    }
                }
            }

        }


        return listArticles;
    }

    private BigDecimal add(BigDecimal temp, BigDecimal sellIncome) {
        // TODO Auto-generated method stub
        return temp.add(sellIncome);
    }

    @Override
    public List<ArticleSellDto> selectBrandArticleSellByDateAndFamilyId(String brandid, String beginDate, String endDate, String sort) {
        Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);
        if ("0".equals(sort)) {
            sort = "peference";
        } else if ("desc".equals(sort)) {
            sort = "brandSellNum desc";
        } else if ("asc".equals(sort)) {
            sort = "brandSellNum asc";
        }
        List<ArticleSellDto> list = orderMapper.selectBrandArticleSellByDateAndFamilyId(brandid, begin, end, sort);
        //计算总菜品销售额
        BigDecimal temp = BigDecimal.ZERO;
        for (ArticleSellDto articleSellDto : list) {
            temp = add(temp, articleSellDto.getSalles());
        }
        for (ArticleSellDto articleSellDto : list) {
            double c = articleSellDto.getSalles().divide(temp, BigDecimal.ROUND_HALF_UP).doubleValue() * 100;
            java.text.DecimalFormat myformat = new java.text.DecimalFormat("0.00");
            String str = myformat.format(c);
            str = str + "%";
            articleSellDto.setSalesRatio(str);
        }

        return list;
    }

    @Override
    public List<ArticleSellDto> selectBrandArticleSellByDateAndId(String brandId, String beginDate, String endDate,
                                                                  String sort) {
        Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);
        if ("0".equals(sort)) {
            sort = "peference , sort";
        } else if ("desc".equals(sort)) {
            sort = "brandSellNum desc";
        } else if ("asc".equals(sort)) {
            sort = "brandSellNum asc";
        }
        List<ArticleSellDto> list = orderMapper.selectBrandArticleSellByDateAndId(brandId, begin, end, sort);
        //计算总菜品销售额,//菜品总销售额
        double num = 0;

        BigDecimal temp = BigDecimal.ZERO;
        for (ArticleSellDto articleSellDto : list) {
            //计算总销量 不能加上套餐的数量
            if (articleSellDto.getType() != 3) {
                num += articleSellDto.getBrandSellNum().doubleValue();
            }
            //计算总销售额
            temp = add(temp, articleSellDto.getSalles());
        }

        for (ArticleSellDto articleSellDto : list) {
            //销售额占比
            BigDecimal d = articleSellDto.getSalles().divide(temp, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
            articleSellDto.setSalesRatio(d + "%");

            if (num != 0) {
                double d1 = articleSellDto.getBrandSellNum().doubleValue();
                double d2 = d1 / num * 100;

                //保留两位小数
                BigDecimal b = new BigDecimal(d2);
                double f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                articleSellDto.setNumRatio(f1 + "%");
            }


        }

        return list;
    }

    @Override
    public List<ArticleSellDto> selectArticleFamilyByBrandAndFamilyName(String brandId, String beginDate, String endDate, String articleFamilyName) {
        Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);
        List<ArticleSellDto> list = orderMapper.selectArticleFamilyByBrandAndFamilyName(brandId, begin, end, articleFamilyName);
        //计算总菜品销售额
        BigDecimal temp = BigDecimal.ZERO;
        for (ArticleSellDto articleSellDto : list) {
            temp = add(temp, articleSellDto.getSalles());
        }
        for (ArticleSellDto articleSellDto : list) {
            double c = articleSellDto.getSalles().divide(temp, BigDecimal.ROUND_HALF_UP).doubleValue() * 100;
            java.text.DecimalFormat myformat = new java.text.DecimalFormat("0.00");
            String str = myformat.format(c);
            str = str + "%";
            articleSellDto.setSalesRatio(str);
        }

        return list;
    }

    @Override
    public List<OrderPayDto> selectMoneyAndNumByDate(String beginDate, String endDate, String brandId) {
        // TODO Auto-generated method stub
        Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);
        //查询出所有店铺并设置默认值
        List<ShopDetail> shopLists = shopDetailService.selectByBrandId(brandId);
        List<OrderPayDto> orderList = new ArrayList<>();
        DecimalFormat df = new DecimalFormat("0.00"); // 保留几位小数
        for (ShopDetail shopDetail : shopLists) {
            OrderPayDto ot = new OrderPayDto(shopDetail.getId(), shopDetail.getName(), new BigDecimal(df.format(0)), 0, new BigDecimal(df.format(0)));
            orderList.add(ot);
        }
        //查询后台数据
        List<OrderPayDto> list = orderMapper.selectMoneyAndNumByDate(begin, end, brandId);

        //如果查询出来有店铺数据则更新这个店铺的数据
        for (OrderPayDto orderPayDto : list) {
            for (OrderPayDto orderPayDto2 : orderList) {
                if (orderPayDto.getShopDetailId().equals(orderPayDto2.getShopDetailId())) {
                    orderPayDto2.setNumber(orderPayDto.getNumber());
                    orderPayDto2.setOrderMoney(orderPayDto.getOrderMoney());
                    BigDecimal v = new BigDecimal(orderPayDto2.getNumber());
                    //orderPayDto2.setAverage(orderPayDto2.getOrderMoney().divide(v).setScale(2, BigDecimal.ROUND_HALF_UP));
                    //theNum.divide(new BigDecimal(3),2,BigDecimal.ROUND_HALF_UP);
                    orderPayDto2.setAverage(orderPayDto2.getOrderMoney().divide(v, 2, BigDecimal.ROUND_HALF_UP));
                }
            }
        }

        return orderList;
    }

    @Override
    public List<ArticleSellDto> selectShopArticleSellByDateAndFamilyId(String beginDate, String endDate, String shopId,
                                                                       String sort) {
        Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);
        if ("0".equals(sort)) {
            sort = "ap.peference";
        } else if ("desc".equals(sort)) {
            sort = "ap.shopSellNum desc";
        } else if ("asc".equals(sort)) {
            sort = "ap.shopSellNum asc";
        }
        List<ArticleSellDto> list = orderMapper.selectShopArticleSellByDateAndFamilyId(shopId, begin, end, sort);
        //计算总菜品销售额
        BigDecimal temp = BigDecimal.ZERO;
        for (ArticleSellDto articleSellDto : list) {
            temp = add(temp, articleSellDto.getSalles());
        }
        for (ArticleSellDto articleSellDto : list) {
            double c = articleSellDto.getSalles().divide(temp, BigDecimal.ROUND_HALF_UP).doubleValue() * 100;
            java.text.DecimalFormat myformat = new java.text.DecimalFormat("0.00");
            String str = myformat.format(c);
            str = str + "%";
            articleSellDto.setSalesRatio(str);
        }
        return list;
    }

    @Override
    public Boolean setOrderPrintFail(String orderId) {
        return orderMapper.setOrderPrintFail(orderId) > 0;
    }

    @Override
    public List<ArticleSellDto> selectShopArticleSellByDateAndId(String beginDate, String endDate, String shopId,
                                                                 String sort) {
        Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);
        if ("0".equals(sort)) {
            sort = "f.peference , a.sort";
        } else if ("desc".equals(sort)) {
            sort = "shop_report.shopSellNum desc";
        } else if ("asc".equals(sort)) {
            sort = "shop_report.shopSellNum asc";
        }
        List<ArticleSellDto> list = orderMapper.selectShopArticleSellByDateAndId(shopId, begin, end, sort);
        //计算总菜品销售额
        BigDecimal temp = BigDecimal.ZERO;
        for (ArticleSellDto articleSellDto : list) {
            temp = add(temp, articleSellDto.getSalles());
        }
        for (ArticleSellDto articleSellDto : list) {
            double c = articleSellDto.getSalles().divide(temp, BigDecimal.ROUND_HALF_UP).doubleValue() * 100;
            java.text.DecimalFormat myformat = new java.text.DecimalFormat("0.00");
            String str = myformat.format(c);
            str = str + "%";
            articleSellDto.setSalesRatio(str);
        }

        return list;
    }

    @Override
    public List<Order> selectListByTime(String beginDate, String endDate, String shopId) {
        Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);
        return orderMapper.selectListByTime(begin, end, shopId);

    }

    @Override
    public Order selectOrderDetails(String orderId) {
        Order o = orderMapper.selectOrderDetails(orderId);
        ShopDetail shop = shopDetailService.selectById(o.getShopDetailId());
        o.setShopName(shop.getName());
        return o;
    }

    @Override
    public OrderPayDto selectBytimeAndState(String beginDate, String endDate, String brandId) {
        Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);
        return orderMapper.selectBytimeAndState(begin, end, brandId);
    }


    @Override
    public List<Order> selectListBybrandId(String beginDate, String endDate, String brandId) {
        Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);
        return orderMapper.selectListBybrandId(begin, end, brandId);
    }

    @Override
    public List<Order> selectAppraiseByShopId(String beginDate, String endDate, String shopId) {
        Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);
        return orderMapper.selectAppraiseByShopId(begin, end, shopId);
    }


    @Override
    public Order getOrderAccount(String shopId) {
        Order order = orderMapper.getOrderAccount(shopId);
        return order;
    }

    @Override
    public void autoRefundMoney() {
        log.debug("开始退款");
    }

    @Override
    public Result checkArticleCount(String orderId) {
        Order order = getOrderInfo(orderId);
        if (order == null || CollectionUtils.isEmpty(order.getOrderItems())) {
            return new Result("订单数据异常,请速与服务员联系", false);
        }

        Boolean result = true;

        String articleName = "";

        //订单菜品不可为空
        for (OrderItem orderItem : order.getOrderItems()) {
            //有任何一个菜品售罄则不能出单
            if (!checkStock(orderItem)) {
                result = false;
                articleName = orderItem.getArticleName() + "已售罄,请取消订单后重新下单";
                break;
            }
        }

        return new Result(articleName, result);

    }

    private Boolean checkStock(OrderItem orderItem) {
        Boolean result = false;
        switch (orderItem.getType()) {
            case OrderItemType.ARTICLE:
                //如果是单品无规格，直接判断菜品是否有库存
                result = orderMapper.selectArticleCount(orderItem.getArticleId()) > 0;
                break;
            case OrderItemType.UNITPRICE:
                //如果是有规则菜品，则判断该规则是否有库存
                result = orderMapper.selectArticlePriceCount(orderItem.getArticleId()) > 0;
                break;
            case OrderItemType.SETMEALS:
                //如果是套餐,不做判断，只判断套餐下的子品是否有库存
                result = true;
                break;
            case OrderItemType.MEALS_CHILDREN:
                //如果是套餐下的子品 当成单品来判断
                result = orderMapper.selectArticleCount(orderItem.getArticleId()) > 0;
                break;
            default:
                log.debug("未知菜品分类");
                break;
        }
        return result;
    }


    @Override
    public Boolean updateStock(Order order) throws AppException {
        //首先验证订单信息
        if (order == null || CollectionUtils.isEmpty(order.getOrderItems())) {
            throw new AppException(AppException.ORDER_IS_NULL);
        }
        //遍历订单商品
        for (OrderItem orderItem : order.getOrderItems()) {
            switch (orderItem.getType()) {
                case OrderItemType.ARTICLE:
                    //如果是没有规格的单品信息,那么更新该单品的库存
                    orderMapper.updateArticleStock(orderItem.getArticleId(), StockType.STOCK_MINUS);
                    orderMapper.setEmpty(orderItem.getArticleId());
                    break;
                case OrderItemType.UNITPRICE:
                    //如果是有规格的单品信息，那么更新该规格的单品库存以及该单品的库存
                    ArticlePrice articlePrice = articlePriceMapper.selectByPrimaryKey(orderItem.getArticleId());
                    orderMapper.updateArticleStock(articlePrice.getArticleId(),StockType.STOCK_MINUS);
                    orderMapper.updateArticlePriceStock(orderItem.getArticleId(),StockType.STOCK_MINUS);
                    orderMapper.setEmpty(articlePrice.getArticleId());
                    break;
                case OrderItemType.SETMEALS:
                    orderMapper.updateArticleStock(orderItem.getArticleId(),StockType.STOCK_MINUS);
                    orderMapper.setEmpty(orderItem.getArticleId());
                    //如果是套餐，那么更新套餐库存
                    break;
                case OrderItemType.MEALS_CHILDREN:
                    //如果是套餐子项，那么更新子项库存
                    orderMapper.updateArticleStock(orderItem.getArticleId(),StockType.STOCK_MINUS);
                    orderMapper.setEmpty(orderItem.getArticleId());
                    break;
                default:
                    throw new AppException(AppException.UNSUPPORT_ITEM_TYPE, "不支持的餐品类型:" + orderItem.getType());
            }

        }

        return true;
    }


    @Override
    public Boolean addStock(Order order) throws AppException {
        //首先验证订单信息
        if (order == null || CollectionUtils.isEmpty(order.getOrderItems())) {
            throw new AppException(AppException.ORDER_IS_NULL);
        }
        //遍历订单商品
        for (OrderItem orderItem : order.getOrderItems()) {
            switch (orderItem.getType()) {
                case OrderItemType.ARTICLE:
                    //如果是没有规格的单品信息,那么更新该单品的库存
                    orderMapper.updateArticleStock(orderItem.getArticleId(), StockType.STOCK_ADD);
                    orderMapper.setEmptyFail(orderItem.getArticleId());
                    break;
                case OrderItemType.UNITPRICE:
                    //如果是有规格的单品信息，那么更新该规格的单品库存以及该单品的库存
                    ArticlePrice articlePrice = articlePriceMapper.selectByPrimaryKey(orderItem.getArticleId());
                    orderMapper.updateArticleStock(articlePrice.getArticleId(),StockType.STOCK_ADD);
                    orderMapper.updateArticlePriceStock(orderItem.getArticleId(),StockType.STOCK_ADD);
                    orderMapper.setEmptyFail(articlePrice.getArticleId());
                    break;
                case OrderItemType.SETMEALS:
                    orderMapper.updateArticleStock(orderItem.getArticleId(),StockType.STOCK_ADD);
                    orderMapper.setEmptyFail(orderItem.getArticleId());
                    //如果是套餐，那么更新套餐库存
                    break;
                case OrderItemType.MEALS_CHILDREN:
                    //如果是套餐子项，那么更新子项库存
                    orderMapper.updateArticleStock(orderItem.getArticleId(),StockType.STOCK_ADD);
                    orderMapper.setEmptyFail(orderItem.getArticleId());
                    break;
                default:
                    throw new AppException(AppException.UNSUPPORT_ITEM_TYPE, "不支持的餐品类型:" + orderItem.getType());
            }

        }

        return true;
    }
}
