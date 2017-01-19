package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resto.brand.core.entity.JSONResult;
import com.resto.brand.core.entity.Result;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.*;
import com.resto.brand.web.dto.*;
import com.resto.brand.web.model.*;
import com.resto.brand.web.service.*;
import com.resto.shop.web.constant.*;
import com.resto.shop.web.container.OrderProductionStateContainer;
import com.resto.shop.web.dao.*;
import com.resto.shop.web.datasource.DataSourceContextHolder;
import com.resto.shop.web.exception.AppException;
import com.resto.shop.web.model.*;
import com.resto.shop.web.model.Employee;
import com.resto.shop.web.producer.MQMessageProducer;
import com.resto.shop.web.service.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
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

    private static final String NUMBER = "0123456789";

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private OrderItemMapper orderitemMapper;

    private static final ObjectMapper MAPPER = new ObjectMapper();

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
    private EmployeeMapper employeeMapper;

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

    @Autowired
    AppraiseService appraiseService;

    @Resource
    MealItemService mealItemService;

    @Resource
    ChargeOrderService chargeOrderService;

    @Resource
    MealAttrMapper mealAttrMapper;

    @Resource
    ArticlePriceMapper articlePriceMapper;

    @Resource
    private ArticleFamilyMapper articleFamilyMapper;

    @Resource
    private LogBaseService logBaseService;

    @Autowired
    private GetNumberService getNumberService;

    @Autowired
    private CustomerDetailMapper customerDetailMapper;

    @Override
    public GenericDao<Order, String> getDao() {
        return orderMapper;
    }

    @Autowired
    ArticleRecommendMapper articleRecommendMapper;

    @Autowired
    WxServerConfigService wxServerConfigService;

    @Autowired
    AccountLogService accountLogService;

    @Autowired
    OffLineOrderMapper offLineOrderMapper;

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

    public static String generateString(int length) {
        StringBuffer sb = new StringBuffer();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(NUMBER.charAt(random.nextInt(NUMBER.length())));
        }
        return sb.toString();
    }


    @Override
    public JSONResult repayOrder(Order order) throws AppException {
        JSONResult jsonResult = new JSONResult();
        Order old = orderMapper.selectByPrimaryKey(order.getId());

        if (old.getServicePrice() == null) {
            old.setServicePrice(new BigDecimal(0));
        }

        if (old.getMealFeePrice() == null) {
            old.setMealFeePrice(new BigDecimal(0));
        }

        //先获取菜品金额
        BigDecimal articleTotalPirce = old.getPaymentAmount().subtract(old.getServicePrice()).subtract(old.getMealFeePrice());

        //重新计算这单子的现有价格
        if (order.getServicePrice() == null) {
            old.setServicePrice(new BigDecimal(0));
        } else {
            old.setServicePrice(order.getServicePrice());
        }
        if (order.getMealFeePrice() == null) {
            old.setMealFeePrice(new BigDecimal(0));
        } else {
            old.setMealFeePrice(order.getMealFeePrice());
        }

        //重新计算订单价格
        BigDecimal orderMoney = articleTotalPirce.add(old.getMealFeePrice()).add(old.getServicePrice());

        old.setOriginalAmount(orderMoney);
        old.setOrderMoney(orderMoney);

        //计算订单应付金额
        BigDecimal payment = orderMoney;


        // 等位红包
        ShopDetail detail = shopDetailService.selectById(old.getShopDetailId());
        if (order.getWaitMoney().doubleValue() > 0) {
            OrderPaymentItem item = new OrderPaymentItem();
            item.setId(ApplicationUtils.randomUUID());
            item.setOrderId(order.getId());
            item.setPaymentModeId(PayMode.WAIT_MONEY);
            item.setPayTime(order.getCreateTime());
            item.setPayValue(order.getWaitMoney());
            item.setRemark("等位红包支付:" + order.getWaitMoney());
            item.setResultData(order.getWaitId());
            orderPaymentItemService.insert(item);

            GetNumber getNumber = getNumberService.selectById(order.getWaitId());
            getNumber.setState(WaitModerState.WAIT_MODEL_NUMBER_THREE);
            getNumberService.update(getNumber);

            payment.subtract(order.getWaitMoney());
        }
        Customer customer = customerService.selectById(old.getCustomerId());


        if (detail.getShopMode() != 5) {
            if (order.getUseCoupon() != null) {
                Coupon coupon = couponService.useCoupon(orderMoney, old);
                OrderPaymentItem item = new OrderPaymentItem();
                item.setId(ApplicationUtils.randomUUID());
                item.setOrderId(order.getId());
                item.setPaymentModeId(PayMode.COUPON_PAY);
                item.setPayTime(order.getCreateTime());
                item.setPayValue(coupon.getValue());
                item.setRemark("优惠卷支付:" + item.getPayValue());
                item.setResultData(coupon.getId());
                orderPaymentItemService.insert(item);
                payment = payment.subtract(item.getPayValue()).setScale(2, BigDecimal.ROUND_HALF_UP);
            }
            // 使用余额
            if (payment.doubleValue() > 0 && order.isUseAccount()) {
                BigDecimal payValue = accountService.payOrder(old, payment, customer);
//			    BigDecimal payValue = accountService.useAccount(payMoney, account,AccountLog.SOURCE_PAYMENT);
                if (payValue.doubleValue() > 0) {
                    payment = payment.subtract(payValue.setScale(2, BigDecimal.ROUND_HALF_UP));

                }
            }
        }

        if (payment.doubleValue() < 0) {
            payment = BigDecimal.ZERO;
        }
        old.setPaymentAmount(payment);

        if (old.getPaymentAmount().doubleValue() == 0) {
            payOrderSuccess(old);
        }
        update(old);
        jsonResult.setSuccess(Boolean.TRUE);
        jsonResult.setData(old);
        if (old.getOrderMode() == ShopMode.HOUFU_ORDER) {
            if (old.getParentOrderId() != null) {  //子订单
                Order parent = selectById(old.getParentOrderId());
                int articleCountWithChildren = selectArticleCountById(parent.getId(), old.getOrderMode());
                if (parent.getLastOrderTime() == null || parent.getLastOrderTime().getTime() < old.getCreateTime().getTime()) {
                    parent.setLastOrderTime(old.getCreateTime());
                }
                Double amountWithChildren = orderMapper.selectParentAmount(parent.getId(), parent.getOrderMode());
                parent.setCountWithChild(articleCountWithChildren);
                parent.setAmountWithChildren(new BigDecimal(amountWithChildren));
                update(parent);

            }
        }
        return jsonResult;
    }

    /**
     * 计算菜品折扣
     *
     * @param price       价格
     * @param discount    当前菜品的折扣
     * @param wxdiscount  微信前端传入的折扣
     * @param articleName 菜品名称
     * @return
     * @throws AppException
     */
    private BigDecimal discount(BigDecimal price, int discount, int wxdiscount, String articleName) throws AppException {
        if (price != null) {
            if (discount != wxdiscount) {
                //折扣不匹配
                throw new AppException(AppException.DISCOUNT_TIMEOUT, articleName + "折扣活动已结束，请重新选购餐品~");
            }
            return price.multiply(new BigDecimal(discount)).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP);
        } else {
            return price;
        }
    }

    public JSONResult createOrder(Order order) throws AppException {
        JSONResult jsonResult = new JSONResult();
        String orderId = ApplicationUtils.randomUUID();
        order.setId(orderId);
        Customer customer = customerService.selectById(order.getCustomerId());
        if (customer == null) {
            throw new AppException(AppException.CUSTOMER_NOT_EXISTS);
        } else if (order.getOrderItems().isEmpty()) {
            throw new AppException(AppException.ORDER_ITEMS_EMPTY);
        }

//        List<OrderItem> orderItems = new ArrayList<OrderItem>();


        List<Article> articles = articleService.selectList(order.getShopDetailId());
        List<ArticlePrice> articlePrices = articlePriceService.selectList(order.getShopDetailId());
        Map<String, Article> articleMap = ApplicationUtils.convertCollectionToMap(String.class, articles);
        Map<String, ArticlePrice> articlePriceMap = ApplicationUtils.convertCollectionToMap(String.class,
                articlePrices);

        if (customer != null && customer.getTelephone() != null) {
            order.setVerCode(customer.getTelephone().substring(7));
        } else {
            if (org.springframework.util.StringUtils.isEmpty(order.getParentOrderId())) {
                order.setVerCode(generateString(5));
            } else {
                Order p = getOrderInfo(order.getParentOrderId());
                order.setVerCode(p.getVerCode());
            }
        }


        order.setId(orderId);
        order.setCreateTime(new Date());
        BigDecimal totalMoney = BigDecimal.ZERO;
        BigDecimal originMoney = BigDecimal.ZERO;
        int articleCount = 0;
        for (OrderItem item : order.getOrderItems()) {
            Article a = null;
            BigDecimal org_price = null;
            int mealFeeNumber = 0;
            BigDecimal price = null;
            BigDecimal fans_price = null;
            item.setId(ApplicationUtils.randomUUID());
            String remark = null;
            switch (item.getType()) {
                case OrderItemType.ARTICLE://无规格单品
                    // 查出 item对应的 商品信息，并将item的原价，单价，总价，商品名称，商品详情 设置为对应的
                    a = articleMap.get(item.getArticleId());
                    item.setArticleName(a.getName());
                    org_price = a.getPrice();
                    price = discount(a.getPrice(), a.getDiscount(), item.getDiscount(), a.getName());                      //计算折扣
                    fans_price = discount(a.getFansPrice(), a.getDiscount(), item.getDiscount(), a.getName());       //计算折扣
                    mealFeeNumber = a.getMealFeeNumber() == null ? 0 : a.getMealFeeNumber();
                    remark = a.getDiscount() + "%";          //设置菜品当前折扣
                    break;
                case OrderItemType.RECOMMEND://推荐餐品
                    // 查出 item对应的 商品信息，并将item的原价，单价，总价，商品名称，商品详情 设置为对应的
                    a = articleMap.get(item.getArticleId());
                    item.setArticleName(a.getName());
                    org_price = a.getPrice();
                    price = a.getPrice();
                    fans_price = a.getFansPrice();
                    mealFeeNumber = a.getMealFeeNumber() == null ? 0 : a.getMealFeeNumber();
                    break;
                case OrderItemType.UNITPRICE://老规格
                    ArticlePrice p = articlePriceMap.get(item.getArticleId());
                    a = articleMap.get(p.getArticleId());
                    item.setArticleName(a.getName() + p.getName());
                    org_price = p.getPrice();
                    price = discount(p.getPrice(), a.getDiscount(), item.getDiscount(), p.getName());                      //计算折扣
                    fans_price = discount(p.getFansPrice(), a.getDiscount(), item.getDiscount(), p.getName());       //计算折扣
                    remark = a.getDiscount() + "%";          //设置菜品当前折扣
                    mealFeeNumber = a.getMealFeeNumber() == null ? 0 : a.getMealFeeNumber();
                    break;
                case OrderItemType.UNIT_NEW://新规格
                    //判断折扣是否匹配，如果不匹配则不允许买单
                    a = articleMap.get(item.getArticleId());
                    item.setArticleName(item.getName());
                    org_price = item.getPrice();
                    price = item.getPrice();
                    fans_price = item.getPrice();
                    mealFeeNumber = a.getMealFeeNumber() == null ? 0 : a.getMealFeeNumber();
                    break;
                case OrderItemType.SETMEALS://套餐主品
                    a = articleMap.get(item.getArticleId());
                    item.setArticleName(a.getName());
                    org_price = a.getPrice();
                    price = discount(a.getPrice(), a.getDiscount(), item.getDiscount(), a.getName());
                    fans_price = discount(a.getFansPrice(), a.getDiscount(), item.getDiscount(), a.getName());
                    remark = a.getDiscount() + "%";//设置菜品当前折扣
                    Integer[] mealItemIds = item.getMealItems();
                    List<MealItem> items = mealItemService.selectByIds(mealItemIds);
                    item.setChildren(new ArrayList<OrderItem>());
                    mealFeeNumber = a.getMealFeeNumber() == null ? 0 : a.getMealFeeNumber();
                    for (MealItem mealItem : items) {
                        OrderItem child = new OrderItem();
                        Article ca = articleMap.get(mealItem.getArticleId());
                        child.setId(ApplicationUtils.randomUUID());
                        child.setMealItemId(mealItem.getId());
                        child.setArticleName(mealItem.getName());
                        child.setMealFeeNumber(0);
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
            item.setMealFeeNumber(mealFeeNumber);
            item.setArticleDesignation(a.getDescription());
            item.setOriginalPrice(org_price);
            item.setStatus(1);
            item.setSort(0);
            item.setRemark(remark);
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
            originMoney = originMoney.add(item.getOriginalPrice()).setScale(2, BigDecimal.ROUND_HALF_UP);
            Result check = new Result();
            if (item.getType() == OrderItemType.ARTICLE) {
                check = checkArticleList(item, item.getCount());
            } else if (item.getType() == OrderItemType.UNITPRICE) {
                check = checkArticleList(item, item.getCount());
            } else if (item.getType() == OrderItemType.SETMEALS) {
                check = checkArticleList(item, articleCount);
            } else if (item.getType() == OrderItemType.UNIT_NEW) {
                check = checkArticleList(item, item.getCount());
            } else if (item.getType() == OrderItemType.RECOMMEND) {
                check = checkArticleList(item, item.getCount());
            }

            jsonResult.setMessage(check.getMessage());
            jsonResult.setSuccess(check.isSuccess());

            if (!check.isSuccess()) {
                break;
            }
        }


        if (!jsonResult.isSuccess()) {
            return jsonResult;
        }

        if (order.getServicePrice() == null) {
            order.setServicePrice(new BigDecimal(0));
        }
        if (order.getMealFeePrice() == null) {
            order.setMealFeePrice(new BigDecimal(0));
        }
        orderItemService.insertItems(order.getOrderItems());
        BigDecimal payMoney = totalMoney.add(order.getServicePrice());
        payMoney = payMoney.add(order.getMealFeePrice());

        // 使用优惠卷
        ShopDetail detail = shopDetailService.selectById(order.getShopDetailId());
        if (order.getWaitMoney().doubleValue() > 0) {
            OrderPaymentItem item = new OrderPaymentItem();
            item.setId(ApplicationUtils.randomUUID());
            item.setOrderId(orderId);
            item.setPaymentModeId(PayMode.WAIT_MONEY);
            item.setPayTime(order.getCreateTime());
            item.setPayValue(order.getWaitMoney());
            item.setRemark("等位红包支付:" + order.getWaitMoney());
            item.setResultData(order.getWaitId());
            orderPaymentItemService.insert(item);

            GetNumber getNumber = getNumberService.selectById(order.getWaitId());
            log.error(order.getWaitId() + "-----------222222222222222");
            getNumber.setState(WaitModerState.WAIT_MODEL_NUMBER_THREE);
            getNumberService.update(getNumber);
        }

        payMoney = payMoney.subtract(order.getWaitMoney());

        if (detail.getShopMode() != ShopMode.HOUFU_ORDER && order.getPayType() != PayType.NOPAY) {
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
//			    BigDecimal payValue = accountService.useAccount(payMoney, account,AccountLog.SOURCE_PAYMENT);
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
            //如果是余额不满足时，使用现金或者银联支付
            if (payMoney.compareTo(BigDecimal.ZERO) > 0 && order.getPayMode() == 3) {
                OrderPaymentItem item = new OrderPaymentItem();
                item.setId(ApplicationUtils.randomUUID());
                item.setOrderId(orderId);
                item.setPaymentModeId(PayMode.BANK_CART_PAY);
                item.setPayTime(order.getCreateTime());
                item.setPayValue(payMoney);
                item.setRemark("银联支付:" + item.getPayValue());
                orderPaymentItemService.insert(item);
                payMoney = BigDecimal.ZERO;
            } else if (payMoney.compareTo(BigDecimal.ZERO) > 0 && order.getPayMode() == 4) {
                OrderPaymentItem item = new OrderPaymentItem();
                item.setId(ApplicationUtils.randomUUID());
                item.setOrderId(orderId);
                item.setPaymentModeId(PayMode.MONEY_PAY);
                item.setPayTime(order.getCreateTime());
                item.setPayValue(payMoney);
                item.setRemark("现金支付:" + item.getPayValue());
                orderPaymentItemService.insert(item);
                payMoney = BigDecimal.ZERO;
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
        order.setOriginalAmount(originMoney.add(order.getServicePrice()).add(order.getMealFeePrice()));// 原价
        order.setReductionAmount(BigDecimal.ZERO);// 折扣金额
        order.setOrderMoney(totalMoney.add(order.getServicePrice()).add(order.getMealFeePrice())); // 订单实际金额
        order.setPaymentAmount(payMoney); // 订单剩余需要维修支付的金额
        order.setPrintTimes(0);

        order.setOrderMode(detail.getShopMode());
        if (order.getOrderMode() == ShopMode.CALL_NUMBER) {
            order.setTableNumber(order.getVerCode());
        }

//        if(!order.getOrderMode().equals(ShopMode.HOUFU_ORDER)){
        if (!StringUtils.isEmpty(order.getTableNumber())) {
            if (order.getParentOrderId() != null) {
                Order parentOrder = selectById(order.getParentOrderId());
                order.setTableNumber(parentOrder.getTableNumber());
                order.setVerCode(parentOrder.getVerCode());
                order.setCustomerCount(parentOrder.getCustomerCount());
            } else {
                BrandSetting brandSetting = brandSettingService.selectByBrandId(order.getBrandId());
                Order lastOrder = orderMapper.getLastOrderByCustomer(customer.getId(), order.getShopDetailId(), brandSetting.getCloseContinueTime());
                if (lastOrder != null && lastOrder.getParentOrderId() != null) {
                    Order parent = orderMapper.selectByPrimaryKey(lastOrder.getParentOrderId());
                    if (parent != null && parent.getAllowContinueOrder()) {
                        order.setParentOrderId(parent.getId());
                        order.setTableNumber(parent.getTableNumber());
                        order.setVerCode(parent.getVerCode());
                        order.setCustomerCount(parent.getCustomerCount());
                    }
                } else {
                    if (lastOrder != null && lastOrder.getAllowContinueOrder()) {
                        order.setParentOrderId(lastOrder.getId());
                        Order parentOrder = selectById(order.getParentOrderId());
                        order.setTableNumber(parentOrder.getTableNumber());
                        order.setVerCode(parentOrder.getVerCode());
                        order.setCustomerCount(parentOrder.getCustomerCount());
                    }
                }
            }
        }
        //判断是否是后付款模式或者稍后支付模式
        if (order.getOrderMode() == ShopMode.HOUFU_ORDER || order.getPayType() == PayType.NOPAY) {
            order.setOrderState(OrderState.SUBMIT);
            order.setProductionStatus(ProductionStatus.NOT_ORDER);
            order.setAllowContinueOrder(true);
        } else {
            order.setOrderState(OrderState.SUBMIT);
            order.setProductionStatus(ProductionStatus.NOT_ORDER);
        }
        if (order.getDistributionModeId() == DistributionType.TAKE_IT_SELF && detail.getContinueOrderScan() == Common.NO) {
            order.setTableNumber(order.getVerCode());
        }

        if (order.getDistributionModeId() == DistributionType.TAKE_IT_SELF && detail.getContinueOrderScan() == Common.YES) {
            order.setNeedScan(Common.YES);
        } else if (order.getDistributionModeId() != DistributionType.TAKE_IT_SELF && order.getOrderMode() == ShopMode.TABLE_MODE
                && StringUtils.isEmpty(order.getTableNumber())) {
            order.setNeedScan(Common.YES);
        } else if (order.getDistributionModeId() != DistributionType.TAKE_IT_SELF && order.getOrderMode() == ShopMode.HOUFU_ORDER
                && StringUtils.isEmpty(order.getTableNumber())) {
            order.setNeedScan(Common.YES);
        }

        insert(order);
        customerService.changeLastOrderShop(order.getShopDetailId(), order.getCustomerId());
        if (order.getPaymentAmount().doubleValue() == 0) {
            payOrderSuccess(order);
        }

        jsonResult.setData(order);
        if (order.getOrderMode() == ShopMode.HOUFU_ORDER ) {
            if (order.getParentOrderId() != null) {  //子订单
                Order parent = selectById(order.getParentOrderId());
                int articleCountWithChildren = selectArticleCountById(parent.getId(), order.getOrderMode());
                if (parent.getLastOrderTime() == null || parent.getLastOrderTime().getTime() < order.getCreateTime().getTime()) {
                    parent.setLastOrderTime(order.getCreateTime());
                }
                Double amountWithChildren = orderMapper.selectParentAmount(parent.getId(), parent.getOrderMode());
                parent.setCountWithChild(articleCountWithChildren);
                parent.setAmountWithChildren(new BigDecimal(amountWithChildren));
                update(parent);
            }
        }else if(order.getPayType() == PayType.NOPAY && order.getOrderMode() == ShopMode.BOSS_ORDER){
            if (order.getParentOrderId() != null) {  //子订单
                Order parent = selectById(order.getParentOrderId());
                int articleCountWithChildren = orderMapper.selectArticleCountByIdBossOrder(parent.getId());
                if (parent.getLastOrderTime() == null || parent.getLastOrderTime().getTime() < order.getCreateTime().getTime()) {
                    parent.setLastOrderTime(order.getCreateTime());
                }
                Double amountWithChildren = orderMapper.selectParentAmountByBossOrder(parent.getId());
                parent.setCountWithChild(articleCountWithChildren);
                parent.setAmountWithChildren(new BigDecimal(amountWithChildren));
                update(parent);
            }
        }
        return jsonResult;
    }


    @Override
    public void updateOrderChild(String orderId) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        Order parent = selectById(order.getParentOrderId());
        int articleCountWithChildren = selectArticleCountById(parent.getId(), parent.getOrderMode());
        if (parent.getLastOrderTime() == null || parent.getLastOrderTime().getTime() < order.getCreateTime().getTime()) {
            parent.setLastOrderTime(order.getCreateTime());
        }
        Double amountWithChildren = orderMapper.selectParentAmount(parent.getId(), parent.getOrderMode());
        parent.setCountWithChild(articleCountWithChildren);
        if (amountWithChildren == parent.getOrderMoney().doubleValue()) {
            parent.setAmountWithChildren(new BigDecimal(0.0));
        } else {
            parent.setAmountWithChildren(new BigDecimal(amountWithChildren));
        }

        update(parent);
    }

    public Result checkArticleList(OrderItem orderItem, int count) {

        Boolean result = true;
        String msg = "";

        //订单菜品不可为空
        //有任何一个菜品售罄则不能出单
        Result check = checkStock(orderItem, count);
        if (!check.isSuccess()) {
            result = false;
            msg = check.getMessage();
        }
        return new Result(msg, result);
    }

    public Order payOrderSuccess(Order order) {
        if (order.getOrderMode() != ShopMode.HOUFU_ORDER) {
            order.setOrderState(OrderState.PAYMENT);
            order.setIsPay(OrderPayState.PAYED);
            update(order);
        }

        if (order.getParentOrderId() != null) {  //子订单
            Order parent = selectById(order.getParentOrderId());
            int articleCountWithChildren = selectArticleCountById(parent.getId(), parent.getOrderMode());
            if (parent.getLastOrderTime() == null || parent.getLastOrderTime().getTime() < order.getCreateTime().getTime()) {
                parent.setLastOrderTime(order.getCreateTime());
            }
            Double amountWithChildren = orderMapper.selectParentAmount(parent.getId(), parent.getOrderMode());
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

    private int selectArticleCountById(String id, Integer shopMode) {
        return orderMapper.selectArticleCountById(id, shopMode);
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
            if (order.getParentOrderId() != null && (order.getOrderState() != OrderState.SUBMIT || order.getOrderMode() == ShopMode.HOUFU_ORDER)) {
                return findCustomerNewOrder(customerId, shopId, order.getParentOrderId());
            }
            List<OrderItem> itemList = orderItemService.listByOrderId(order.getId());
            order.setOrderItems(itemList);
            if ((order.getOrderState() != OrderState.SUBMIT || order.getOrderMode() == ShopMode.HOUFU_ORDER)) {
                List<String> childIds = selectChildIdsByParentId(order.getId());
                List<OrderItem> childItems = orderItemService.listByOrderIds(childIds);
                order.getOrderItems().addAll(childItems);
            }

        }
        return order;
    }

    private List<String> selectChildIdsByParentId(String id) {
        Order order = orderMapper.selectByPrimaryKey(id);
        if (order.getOrderMode() == ShopMode.HOUFU_ORDER) {
            return orderMapper.selectChildIdsByParentIdByFive(id);
        } else {
            return orderMapper.selectChildIdsByParentId(id);
        }

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

            //拒绝订单后还原库存
            Boolean addStockSuccess = false;
            addStockSuccess = addStock(getOrderInfo(orderId));
            if (!addStockSuccess) {
                log.info("库存还原失败:" + order.getId());
            }
            orderMapper.setStockBySuit(order.getShopDetailId());//自动更新套餐数量
            return true;
        } else {
            log.warn("取消订单失败，订单状态订单状态或者订单可取消字段为false" + order.getId());
            return false;
        }
    }

    @Override
    public boolean cancelExceptionOrder(String orderId) {
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
    public List<Order> selectNeedCacelOrderList(String brandId, String beginTime, String endTime) {
        Date begin = DateUtil.getformatBeginDate(beginTime);
        Date end = DateUtil.getformatEndDate(endTime);
        return orderMapper.selectNeedCacelOrderList(brandId, begin, end);
    }


    @Override
    public Boolean checkRefundLimit(Order order) {
        Integer orderMode = order.getOrderMode();
        Boolean result = false;
        switch (orderMode) {
            case ShopMode.MANUAL_ORDER: //验证码下单
            case ShopMode.CALL_NUMBER: //电视叫号
            case ShopMode.TABLE_MODE: //坐下点餐
                result = (((order.getOrderState().equals(OrderState.CONFIRM) ||
                        order.getOrderState().equals(OrderState.PAYMENT))
                        &&
                        order.getProductionStatus().equals(ProductionStatus.NOT_PRINT))
                        || (order.getOrderState().equals(OrderState.PAYMENT) &&
                        order.getProductionStatus().equals(ProductionStatus.NOT_ORDER)))
                        || (order.getOrderState().equals(OrderState.SUBMIT) && order.getProductionStatus().equals(ProductionStatus.NOT_ORDER))
                ;
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

    @Override
    public Result refundPaymentByUnfinishedOrder(String orderId) {
        return new Result(autoRefundOrder(orderId));
//        Result result = new Result();
//
//        //首先获得订单
//        Order order = orderMapper.selectByPrimaryKey(orderId);
//        if(order.getOrderState() != OrderState.SUBMIT || order.getOrderMode() == ShopMode.HOUFU_ORDER){
//            //如果订单状态不是1 或者 是后付模式
//            result.setMessage("订单状态不符合退款条件");
//            result.setSuccess(false);
//            return result;
//        }
//        ShopDetail shopDetail = shopDetailService.selectByPrimaryKey(order.getShopDetailId());
//        List<OrderPaymentItem> payItemsList = orderPaymentItemService.selectByOrderId(orderId);
//        for (OrderPaymentItem item : payItemsList) {
//            String newPayItemId = ApplicationUtils.randomUUID();
//            switch (item.getPaymentModeId()) {
//                case PayMode.COUPON_PAY:
//                    couponService.refundCoupon(item.getResultData());
//                    break;
//                case PayMode.ACCOUNT_PAY:
//                    accountService.addAccount(item.getPayValue(), item.getResultData(), "取消订单返还", AccountLog.SOURCE_CANCEL_ORDER);
//                    break;
//                case PayMode.CHARGE_PAY:
//                    chargeOrderService.refundCharge(item.getPayValue(), item.getResultData());
//                    break;
//                case PayMode.REWARD_PAY:
//                    chargeOrderService.refundReward(item.getPayValue(), item.getResultData());
//                    break;
//                case PayMode.WEIXIN_PAY:
//                    WechatConfig config = wechatConfigService.selectByBrandId(DataSourceContextHolder.getDataSourceName());
//                    JSONObject obj = new JSONObject(item.getResultData());
//                    int refund = obj.getInt("total_fee");
//                    Map<String, String> jsonObject = null;
//                    if(shopDetail.getWxServerId() == null){
//                        jsonObject  = WeChatPayUtils.refund(newPayItemId, obj.getString("transaction_id"),
//                                obj.getInt("total_fee"),refund , config.getAppid(), config.getMchid(),
//                                config.getMchkey(), config.getPayCertPath());
//                    }else{
//                        WxServerConfig wxServerConfig = wxServerConfigService.selectById(shopDetail.getWxServerId());
//
//                        jsonObject = WeChatPayUtils.refundNew(newPayItemId, obj.getString("transaction_id"),
//                                obj.getInt("total_fee"),refund, wxServerConfig.getAppid(), wxServerConfig.getMchid(),
//                                StringUtils.isEmpty(shopDetail.getMchid()) ? config.getMchid() : shopDetail.getMchid(), wxServerConfig.getMchkey(), wxServerConfig.getPayCertPath());
//                    }
//                    item.setResultData(new JSONObject(jsonObject).toString());
//                    break;
//                case PayMode.WAIT_MONEY:
//                    getNumberService.refundWaitMoney(order);
//                    break;
//                case PayMode.ALI_PAY: //如果是支付宝支付
//                    BrandSetting brandSetting = brandSettingService.selectByBrandId(order.getBrandId());
//                    AliPayUtils.connection(StringUtils.isEmpty(shopDetail.getAliAppId()) ?  brandSetting.getAliAppId() : shopDetail.getAliAppId().trim() ,
//                            StringUtils.isEmpty(shopDetail.getAliPrivateKey()) ?  brandSetting.getAliPrivateKey().trim() : shopDetail.getAliPrivateKey().trim(),
//                            StringUtils.isEmpty(shopDetail.getAliPublicKey()) ?  brandSetting.getAliPublicKey().trim() : shopDetail.getAliPublicKey().trim());
//                    Map map = new HashMap();
//                    map.put("out_trade_no", order.getId());
//                    map.put("refund_amount", item.getPayValue());
//                    String resultJson = AliPayUtils.refundPay(map);
//                    item.setResultData(new JSONObject(resultJson).toString());
//                    break;
//            }
//            order.setPaymentAmount(order.getPaymentAmount().add(item.getPayValue()));
//            item.setId(newPayItemId);
//            item.setPayValue(item.getPayValue().multiply(new BigDecimal(-1)));
//            orderPaymentItemService.insert(item);
//        }
//        order.setIsPay(OrderPayState.NOT_PAY);
//        update(order);
//        logBaseService.insertLogBaseInfoState(shopDetail, customerService.selectById(order.getCustomerId()), order, LogBaseState.NOT_PAYMENT_ORDER);
//        result.setSuccess(true);
//        return result;
    }

    private void refundOrder(Order order) {
        List<OrderPaymentItem> payItemsList = orderPaymentItemService.selectByOrderId(order.getId());
        ShopDetail shopDetail = shopDetailService.selectByPrimaryKey(order.getShopDetailId());
        for (OrderPaymentItem item : payItemsList) {
            String newPayItemId = ApplicationUtils.randomUUID();
            int refundTotal = 0;
            BigDecimal aliRefund = new BigDecimal(0);
            BigDecimal aliPay = new BigDecimal(0);
            if (item.getPaymentModeId() == PayMode.WEIXIN_PAY) {
                BigDecimal sum = orderMapper.getRefundSumByOrderId(order.getId(), PayMode.WEIXIN_PAY);
                if (sum != null) {
                    refundTotal = sum.multiply(new BigDecimal(100)).intValue();
                }

            } else if (item.getPaymentModeId() == PayMode.ALI_PAY) {
                BigDecimal sum = orderMapper.getRefundSumByOrderId(order.getId(), PayMode.ALI_PAY);
                aliPay = orderMapper.getAliPayment(order.getId());
                if (sum != null) {
                    aliRefund = sum;
                }
            }

            if (item.getPaymentModeId() == PayMode.WEIXIN_PAY && item.getPayValue().doubleValue() < 0) {
                continue;
            }
            if (item.getPaymentModeId() == PayMode.ALI_PAY && item.getPayValue().doubleValue() < 0) {
                continue;
            }


            if (refundTotal != 0 && refundTotal == order.getPaymentAmount().multiply(new BigDecimal(-100)).intValue()) { //如果已经全部退款完毕
                continue;
            }

            if (aliRefund.doubleValue() < 0 && aliRefund.doubleValue() == aliPay.multiply(new BigDecimal(-1)).doubleValue()) { //如果已经全部退款完毕
                continue;
            }


            switch (item.getPaymentModeId()) {
                case PayMode.COUPON_PAY:
                    couponService.refundCoupon(item.getResultData());
                    item.setPayValue(item.getPayValue().multiply(new BigDecimal(-1)));
                    break;
                case PayMode.ACCOUNT_PAY:
                    accountService.addAccount(item.getPayValue(), item.getResultData(), "取消订单返还", AccountLog.SOURCE_CANCEL_ORDER);
                    item.setPayValue(item.getPayValue().multiply(new BigDecimal(-1)));
                    break;
                case PayMode.CHARGE_PAY:
                    chargeOrderService.refundCharge(item.getPayValue(), item.getResultData());
                    item.setPayValue(item.getPayValue().multiply(new BigDecimal(-1)));
                    break;
                case PayMode.REWARD_PAY:
                    chargeOrderService.refundReward(item.getPayValue(), item.getResultData());
                    item.setPayValue(item.getPayValue().multiply(new BigDecimal(-1)));
                    break;
                case PayMode.WEIXIN_PAY:
                    WechatConfig config = wechatConfigService.selectByBrandId(DataSourceContextHolder.getDataSourceName());
                    JSONObject obj = new JSONObject(item.getResultData());
                    int refund = obj.getInt("total_fee") + refundTotal;
                    Map<String, String> result = null;
                    if (shopDetail.getWxServerId() == null) {
                        result = WeChatPayUtils.refund(newPayItemId, obj.getString("transaction_id"),
                                obj.getInt("total_fee"), refund, config.getAppid(), config.getMchid(),
                                config.getMchkey(), config.getPayCertPath());
                    } else {
                        WxServerConfig wxServerConfig = wxServerConfigService.selectById(shopDetail.getWxServerId());

                        result = WeChatPayUtils.refundNew(newPayItemId, obj.getString("transaction_id"),
                                obj.getInt("total_fee"), refund, wxServerConfig.getAppid(), wxServerConfig.getMchid(),
                                StringUtils.isEmpty(shopDetail.getMchid()) ? config.getMchid() : shopDetail.getMchid(), wxServerConfig.getMchkey(), wxServerConfig.getPayCertPath());
                    }


                    item.setPayValue(new BigDecimal(refund).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(-1)));
                    item.setResultData(new JSONObject(result).toString());

                    break;
                case PayMode.WAIT_MONEY:
                    getNumberService.refundWaitMoney(order);
                    item.setPayValue(item.getPayValue().multiply(new BigDecimal(-1)));
                    break;
                case PayMode.ALI_PAY: //如果是支付宝支付
                    BrandSetting brandSetting = brandSettingService.selectByBrandId(order.getBrandId());
                    AliPayUtils.connection(StringUtils.isEmpty(shopDetail.getAliAppId()) ? brandSetting.getAliAppId() : shopDetail.getAliAppId().trim(),
                            StringUtils.isEmpty(shopDetail.getAliPrivateKey()) ? brandSetting.getAliPrivateKey().trim() : shopDetail.getAliPrivateKey().trim(),
                            StringUtils.isEmpty(shopDetail.getAliPublicKey()) ? brandSetting.getAliPublicKey().trim() : shopDetail.getAliPublicKey().trim());
                    Map map = new HashMap();
                    map.put("out_trade_no", order.getId());
                    map.put("refund_amount", aliPay.add(aliRefund));
                    map.put("out_request_no", newPayItemId);
                    String resultJson = AliPayUtils.refundPay(map);
                    item.setResultData(new JSONObject(resultJson).toString());
                    item.setPayValue(aliPay.add(aliRefund).multiply(new BigDecimal(-1)));
                    break;
                case PayMode.ARTICLE_BACK_PAY:
                    Customer customer = customerService.selectById(order.getCustomerId());

                    if (item.getPayValue().doubleValue() < 0) {
                        accountService.addAccount(item.getPayValue(), customer.getAccountId(), "取消订单扣除", -1);
                    }
                    item.setPayValue(item.getPayValue().multiply(new BigDecimal(-1)));
                    break;


            }
            item.setId(newPayItemId);

            orderPaymentItemService.insert(item);
        }
    }

    @Override
    public Order orderWxPaySuccess(OrderPaymentItem item) {

        Order order = selectById(item.getOrderId());
        OrderPaymentItem historyItem = orderPaymentItemService.selectById(item.getId());
        if (historyItem == null) {
            orderPaymentItemService.insert(item);
            if (order.getOrderMode() == ShopMode.HOUFU_ORDER) {
                order.setPaymentAmount(item.getPayValue());
                update(order);
            }
            payOrderSuccess(order);
        } else {
            log.warn("该笔支付记录已经处理过:" + item.getId());
        }
        return order;
    }

    @Override
    public Order pushOrder(String orderId) throws AppException {
        Order order = selectById(orderId);
        //如果是后付款模式 不验证直接进行修改模式
        if (order.getOrderMode() == ShopMode.HOUFU_ORDER) {
            order.setProductionStatus(ProductionStatus.HAS_ORDER);
            order.setPushOrderTime(new Date());
            update(order);
        } else if (validOrderCanPush(order)) {
            order.setProductionStatus(ProductionStatus.HAS_ORDER);
            order.setPushOrderTime(new Date());
            update(order);
            return order;
        }
        return order;
    }

    private boolean validOrderCanPush(Order order) throws AppException {
        if (order.getPayMode() != null && order.getPayMode() == OrderPayMode.ALI_PAY
                && order.getProductionStatus().equals(ProductionStatus.NOT_ORDER) && order.getOrderState().equals(OrderState.SUBMIT)) {
            return true;
        }

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
        if (StringUtils.isEmpty(order.getParentOrderId())) {
            log.info("打印成功，订单为主订单，允许加菜-:" + order.getId());
            if (order.getOrderMode() != ShopMode.CALL_NUMBER) {
                order.setAllowContinueOrder(true);
            }
        } else {
            log.info("打印成功，订单为子订单:" + order.getId() + " pid:" + order.getParentOrderId());
            order.setAllowContinueOrder(false);
            order.setAllowAppraise(false);
        }
        order.setProductionStatus(ProductionStatus.PRINTED);
        order.setPrintOrderTime(new Date());
        order.setAllowCancel(false);
        update(order);
//            ShopDetail shopDetail = shopDetailService.selectById(order.getShopDetailId());
//            Customer customer = customerService.selectById(order.getCustomerId());
//            logBaseService.insertLogBaseInfoState(shopDetail, customer, orderId, LogBaseState.PRINT);
        return order;
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
        Map<String, List<String>> recommendMap = new HashMap<>();
        ShopDetail shopDetail = shopDetailService.selectById(order.getShopDetailId());
        BrandSetting setting = brandSettingService.selectByBrandId(order.getBrandId());
        //遍历 订单集合
        for (OrderItem item : articleList) {
            //得到当前菜品 所关联的厨房信息
            String articleId = item.getArticleId();
            if (item.getType() == OrderItemType.UNITPRICE) { //单品
                if (articleId.length() > 32) {
                    articleId = item.getArticleId().substring(0, 32);
                } else {
                    ArticlePrice price = articlePriceService.selectById(articleId);
                    if (price != null) {
                        articleId = price.getArticleId();
                    }
                }
            } else if (item.getType() == OrderItemType.MEALS_CHILDREN) {  // 套餐子品
//                continue;
                if (setting.getPrintType().equals(PrinterType.TOTAL) && shopDetail.getPrintType().equals(PrinterType.TOTAL)) { //总单出
                    continue;
                } else {
                    Kitchen kitchen = kitchenService.getItemKitchenId(item);
                    if (kitchen != null) {
                        String kitchenId = kitchen.getId().toString();
                        kitchenMap.put(kitchenId, kitchen);
                        if (!kitchenArticleMap.containsKey(kitchenId)) {
                            //如果没有 则新建
                            kitchenArticleMap.put(kitchenId, new ArrayList<OrderItem>());
                        }
                        kitchenArticleMap.get(kitchenId).add(item);
                    }
                }
            }

            if (OrderItemType.SETMEALS == item.getType()) { //如果类型是套餐那么continue
                if (setting.getPrintType().equals(PrinterType.TOTAL) && shopDetail.getPrintType().equals(PrinterType.TOTAL)) { //总单出
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
                    continue;
                }
            } else {
                if (item.getType() != OrderItemType.MEALS_CHILDREN) {
                    if (item.getType() == OrderItemType.RECOMMEND) {
                        ArticleRecommend articleRecommend = articleRecommendMapper.getRecommendById(item.getRecommendId());
                        if (articleRecommend.getPrintType() == PrinterType.KITCHEN) {
                            String kitchenId = articleRecommend.getKitchenId();
                            Kitchen kitchen = kitchenService.selectById(Integer.valueOf(kitchenId));
                            kitchenMap.put(kitchenId, kitchen);
                            if (!recommendMap.containsKey(kitchenId)) {
                                recommendMap.put(kitchenId, new ArrayList<String>());
                            }
                            if (!recommendMap.get(kitchenId).contains(item.getRecommendId())) {
                                recommendMap.get(kitchenId).add(item.getRecommendId());
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


            }
        }

        //桌号
        String tableNumber = order.getTableNumber() != null ? order.getTableNumber() : "";
        //打印线程集合
        List<Map<String, Object>> printTask = new ArrayList<Map<String, Object>>();

        String modeText = getModeText(order);//就餐模式
        String serialNumber = order.getSerialNumber();//序列号
        ShopDetail shop = shopDetailService.selectById(order.getShopDetailId());
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
                        List<OrderItem> list = orderitemMapper.getListBySort(article.getId(), article.getArticleId());
                        for (OrderItem obj : list) {
                            Map<String, Object> child_item = new HashMap<String, Object>();
                            child_item.put("ARTICLE_NAME", obj.getArticleName());
                            if (order.getIsRefund() != null && order.getIsRefund() == Common.YES) {
                                child_item.put("ARTICLE_COUNT", obj.getRefundCount());
                            } else {
                                child_item.put("ARTICLE_COUNT", obj.getCount());
                            }

                            items.add(child_item);
                        }
                    }
                }

                //保存基本信息
                Map<String, Object> print = new HashMap<String, Object>();
                print.put("PORT", printer.getPort());
                print.put("IP", printer.getIp());
                String print_id = ApplicationUtils.randomUUID();
                print.put("PRINT_TASK_ID", print_id);
                Map<String, Object> data = new HashMap<String, Object>();
                data.put("ORDER_ID", serialNumber);
                data.put("DATETIME", DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
                data.put("DISTRIBUTION_MODE", modeText);
                data.put("TABLE_NUMBER", order.getTableNumber());
                data.put("ORDER_NUMBER", nextNumber(order.getShopDetailId(), order.getId()));
                data.put("ITEMS", items);
                Appraise appraise = appraiseService.selectAppraiseByCustomerId(order.getCustomerId(), order.getShopDetailId());
                StringBuilder star = new StringBuilder();
                BigDecimal level = new BigDecimal(0);
                if (appraise != null) {
                    if (appraise != null && appraise.getLevel() < 5) {
                        for (int i = 0; i < appraise.getLevel(); i++) {
                            star.append("★");
                        }
                        for (int i = 0; i < 5 - appraise.getLevel(); i++) {
                            star.append("☆");
                        }
                    } else if (appraise != null && appraise.getLevel() == 5) {
                        star.append("★★★★★");
                    }
                    Map<String, Object> appriseCount = appraiseService.selectCustomerAppraiseAvg(order.getCustomerId());
                    level = new BigDecimal(Integer.valueOf(appriseCount.get("sum").toString()))
                            .divide(new BigDecimal(Integer.valueOf(appriseCount.get("count").toString())), 2, BigDecimal.ROUND_HALF_UP);
                } else {
                    star.append("☆☆☆☆☆");
                }
                StringBuilder gao = new StringBuilder();
                if (shopDetail.getIsUserIdentity().equals(1)) {
                    //得到有限制的情况下用户的订单数
                    int gaoCount = orderMapper.selectByCustomerCount(order.getCustomerId(), shopDetail.getConsumeConfineUnit(), shopDetail.getConsumeConfineTime());
                    //得到无限制情况下用户的订单数
                    int gaoCountlong = orderMapper.selectByCustomerCount(order.getCustomerId(), shopDetail.getConsumeConfineUnit(), 0);
                    if (shopDetail.getConsumeNumber() > 0 && gaoCount > shopDetail.getConsumeNumber() && shopDetail.getConsumeConfineUnit() != 3) {
                        gao.append("【高频】");
                    }//无限制的时候
                    else if (shopDetail.getConsumeConfineUnit() == 3 && gaoCountlong > shopDetail.getConsumeNumber()) {
                        gao.append("【高频】");
                    }
                }
                data.put("CUSTOMER_SATISFACTION", star.toString());
                data.put("CUSTOMER_SATISFACTION_DEGREE", level);
                Account account = accountService.selectAccountAndLogByCustomerId(order.getCustomerId());
                StringBuffer customerStr = new StringBuffer();
                if (account != null) {
                    customerStr.append("余额：" + account.getRemain() + " ");
                } else {
                    customerStr.append("余额：0 ");
                }
                customerStr.append("" + gao.toString() + " ");
                Customer customer = customerService.selectById(order.getCustomerId());
                CustomerDetail customerDetail = customerDetailMapper.selectByPrimaryKey(customer.getCustomerDetailId());
                if (customerDetail != null) {
                    if (customerDetail.getBirthDate() != null) {
                        if (DateUtil.formatDate(customerDetail.getBirthDate(), "MM-dd")
                                .equals(DateUtil.formatDate(new Date(), "MM-dd"))) {
                            customerStr.append("★" + DateUtil.formatDate(customerDetail.getBirthDate(), "yyyy-MM-dd") + "★");
                        }
                    }
                }
                data.put("CUSTOMER_PROPERTY", customerStr.toString());
                print.put("DATA", data);
                print.put("STATUS", "0");
                print.put("TICKET_TYPE", TicketType.KITCHEN);
                //保存打印配置信息
//                print.put("ORDER_ID", serialNumber);
//                print.put("KITCHEN_NAME", kitchen.getName());
//                print.put("TABLE_NO", tableNumber);
                //添加到 打印集合
                printTask.add(print);
            }
        }

        for (String kitchenId : recommendMap.keySet()) {
            Kitchen kitchen = kitchenMap.get(kitchenId);//得到厨房 信息
            Printer printer = printerService.selectById(kitchen.getPrinterId());//得到打印机信息
            if (printer == null) {
                continue;
            }


            //生成厨房小票
            for (String recommendId : recommendMap.get(kitchenId)) {
                //保存 菜品的名称和数量
                List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();

                List<OrderItem> orderItems = orderitemMapper.getOrderItemByRecommendId(recommendId, order.getId());
                for (OrderItem orderItem : orderItems) {
                    Map<String, Object> item = new HashMap<String, Object>();
                    item.put("ARTICLE_NAME", orderItem.getArticleName());
                    item.put("ARTICLE_COUNT", orderItem.getCount());
                    items.add(item);
                }


                //保存基本信息
                Map<String, Object> print = new HashMap<String, Object>();
                print.put("PORT", printer.getPort());
                print.put("IP", printer.getIp());
                String print_id = ApplicationUtils.randomUUID();
                print.put("PRINT_TASK_ID", print_id);
                Map<String, Object> data = new HashMap<String, Object>();
                data.put("ORDER_ID", serialNumber);
                data.put("DATETIME", DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
                data.put("DISTRIBUTION_MODE", modeText);
                data.put("TABLE_NUMBER", tableNumber);
                //添加当天打印订单的序号
                data.put("ORDER_NUMBER", nextNumber(order.getShopDetailId(), order.getId()));
                data.put("ITEMS", items);
                Appraise appraise = appraiseService.selectAppraiseByCustomerId(order.getCustomerId(), order.getShopDetailId());
                StringBuilder star = new StringBuilder();
                BigDecimal level = new BigDecimal(0);
                if (appraise != null) {
                    if (appraise != null && appraise.getLevel() < 5) {
                        for (int i = 0; i < appraise.getLevel(); i++) {
                            star.append("★");
                        }
                        for (int i = 0; i < 5 - appraise.getLevel(); i++) {
                            star.append("☆");
                        }
                    } else if (appraise != null && appraise.getLevel() == 5) {
                        star.append("★★★★★");
                    }
                    Map<String, Object> appriseCount = appraiseService.selectCustomerAppraiseAvg(order.getCustomerId());
                    level = new BigDecimal(Integer.valueOf(appriseCount.get("sum").toString()))
                            .divide(new BigDecimal(Integer.valueOf(appriseCount.get("count").toString())), 2, BigDecimal.ROUND_HALF_UP);
                } else {
                    star.append("☆☆☆☆☆");
                }
                StringBuilder gao = new StringBuilder();
                if (shopDetail.getIsUserIdentity().equals(1)) {
                    //得到有限制的情况下用户的订单数
                    int gaoCount = orderMapper.selectByCustomerCount(order.getCustomerId(), shopDetail.getConsumeConfineUnit(), shopDetail.getConsumeConfineTime());
                    //得到无限制情况下用户的订单数
                    int gaoCountlong = orderMapper.selectByCustomerCount(order.getCustomerId(), shopDetail.getConsumeConfineUnit(), 0);
                    if (shopDetail.getConsumeNumber() > 0 && gaoCount > shopDetail.getConsumeNumber() && shopDetail.getConsumeConfineUnit() != 3) {
                        gao.append("【高频】");
                    }//无限制的时候
                    else if (shopDetail.getConsumeConfineUnit() == 3 && gaoCountlong > shopDetail.getConsumeNumber()) {
                        gao.append("【高频】");
                    }
                }
                data.put("CUSTOMER_SATISFACTION", star.toString());
                data.put("CUSTOMER_SATISFACTION_DEGREE", level);
                Account account = accountService.selectAccountAndLogByCustomerId(order.getCustomerId());
                StringBuffer customerStr = new StringBuffer();
                if (account != null) {
                    customerStr.append("余额：" + account.getRemain() + " ");
                } else {
                    customerStr.append("余额：0 ");
                }
                customerStr.append("" + gao.toString() + " ");
                Customer customer = customerService.selectById(order.getCustomerId());
                CustomerDetail customerDetail = customerDetailMapper.selectByPrimaryKey(customer.getCustomerDetailId());
                if (customerDetail != null) {
                    if (customerDetail.getBirthDate() != null) {
                        if (DateUtil.formatDate(customerDetail.getBirthDate(), "MM-dd")
                                .equals(DateUtil.formatDate(new Date(), "MM-dd"))) {
                            customerStr.append("★" + DateUtil.formatDate(customerDetail.getBirthDate(), "yyyy-MM-dd") + "★");
                        }
                    }
                }
                data.put("CUSTOMER_PROPERTY", customerStr.toString());
                print.put("DATA", data);
                print.put("STATUS", "0");
                print.put("TICKET_TYPE", TicketType.KITCHEN);
                //保存打印配置信息
//                print.put("ORDER_ID", serialNumber);
//                print.put("KITCHEN_NAME", kitchen.getName());
//                print.put("TABLE_NO", tableNumber);
                //添加到 打印集合
                printTask.add(print);
            }
        }


//        logBaseService.insertLogBaseInfoState(shop, customerService.selectById(order.getCustomerId()),order.getId(),LogBaseState.PRINT_KITCHEN);
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
        //如果是 未打印状态 或者  异常状态则改变 生产状态和打印时间
        if (ProductionStatus.HAS_ORDER == order.getProductionStatus() || ProductionStatus.NOT_PRINT == order.getProductionStatus()) {
            order.setProductionStatus(ProductionStatus.PRINTED);
            order.setPrintOrderTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
        }
        //查询店铺
        ShopDetail shopDetail = shopDetailService.selectById(order.getShopDetailId());
        // 查询订单菜品
        List<OrderItem> orderItems = orderItemService.listByOrderId(orderId);

//        if (order.getOrderMode() == ShopMode.HOUFU_ORDER) {
        List<OrderItem> child = orderItemService.listByParentId(orderId);
        for (OrderItem orderItem : child) {
            orderItem.setArticleName(orderItem.getArticleName() + "(加)");
            order.setOrderMoney(order.getOrderMoney().add(orderItem.getFinalPrice()));
            if (order.getOrderState() == OrderState.SUBMIT) {
                order.setPaymentAmount(order.getPaymentAmount().add(orderItem.getFinalPrice()));
            }

        }
        orderItems.addAll(child);
//        }

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
        List<Map<String, Object>> refundItems = new ArrayList<>();
        for (OrderItem article : orderItems) {
            Map<String, Object> item = new HashMap<>();
            item.put("SUBTOTAL", article.getUnitPrice().multiply(new BigDecimal(article.getOrginCount())));
            item.put("ARTICLE_NAME", article.getArticleName());
            item.put("ARTICLE_COUNT", article.getOrginCount());
            items.add(item);
            if (article.getRefundCount() != 0) {
                Map<String, Object> refundItem = new HashMap<>();
                refundItem.put("SUBTOTAL", -article.getUnitPrice().multiply(new BigDecimal(article.getRefundCount())).doubleValue());
                if (article.getArticleName().contains("加")) {
                    article.setArticleName(article.getArticleName().substring(0, article.getArticleName().indexOf("(") - 1));
                }
                refundItem.put("ARTICLE_NAME", article.getArticleName() + "(退)");
                refundItem.put("ARTICLE_COUNT", -article.getRefundCount());
                refundItems.add(refundItem);
            }
        }
        BrandSetting brandSetting = brandSettingService.selectByBrandId(order.getBrandId());
        Brand brand = brandService.selectBrandBySetting(brandSetting.getId());

        if (order.getDistributionModeId() == 1) {
            if (order.getBaseCustomerCount() != null && order.getBaseCustomerCount() != 0
                    && StringUtils.isBlank(order.getParentOrderId())) {
                Map<String, Object> item = new HashMap<>();
                item.put("SUBTOTAL", brandSetting.getServicePrice().multiply(new BigDecimal(order.getBaseCustomerCount())));
                item.put("ARTICLE_NAME", brandSetting.getServiceName());
                if ("27f56b31669f4d43805226709874b530".equals(brand.getId())) {
                    item.put("ARTICLE_NAME", "就餐人数");
                }
                item.put("ARTICLE_COUNT", order.getBaseCustomerCount());
                items.add(item);
                if (order.getBaseCustomerCount() != order.getCustomerCount()) {
                    Map<String, Object> refundItem = new HashMap<>();
                    refundItem.put("SUBTOTAL", -brandSetting.getServicePrice().multiply(new BigDecimal((order.getBaseCustomerCount() - order.getCustomerCount()))).doubleValue());
                    refundItem.put("ARTICLE_NAME", brandSetting.getServiceName() + "(退)");
                    if ("27f56b31669f4d43805226709874b530".equals(brand.getId())) {
                        refundItem.put("ARTICLE_NAME", "就餐人数" + "(退)");
                    }
                    refundItem.put("ARTICLE_COUNT", -(order.getBaseCustomerCount() - order.getCustomerCount()));
                    refundItems.add(refundItem);
                }
            }
        } else if (order.getDistributionModeId() == 3 || order.getDistributionModeId() == 2) {
            if (order.getBaseMealAllCount() != null && order.getBaseMealAllCount() != 0) {
                Map<String, Object> item = new HashMap<>();
                item.put("SUBTOTAL", shopDetail.getMealFeePrice().multiply(new BigDecimal(order.getBaseMealAllCount())));
                item.put("ARTICLE_NAME", shopDetail.getMealFeeName());
                item.put("ARTICLE_COUNT", order.getBaseMealAllCount());
                items.add(item);
                if (order.getBaseMealAllCount() != order.getMealAllNumber()) {
                    Map<String, Object> refundItem = new HashMap<>();
                    refundItem.put("SUBTOTAL", -shopDetail.getMealFeePrice().multiply(new BigDecimal(order.getBaseMealAllCount() - order.getMealAllNumber())).doubleValue());
                    refundItem.put("ARTICLE_NAME", shopDetail.getMealFeeName() + "(退)");
                    refundItem.put("ARTICLE_COUNT", -(order.getBaseMealAllCount() - order.getMealAllNumber()));
                    refundItems.add(refundItem);
                }
            }
        }
        Map<String, Object> print = new HashMap<>();
        String tableNumber = order.getTableNumber() != null ? order.getTableNumber() : "";
        print.put("TABLE_NO", tableNumber);
        print.put("KITCHEN_NAME", printer.getName());
        print.put("PORT", printer.getPort());
        print.put("ORDER_ID", order.getSerialNumber());
        print.put("IP", printer.getIp());
        String print_id = ApplicationUtils.randomUUID();
        print.put("PRINT_TASK_ID", print_id);
        print.put("ADD_TIME", new Date());

        Map<String, Object> data = new HashMap<>();
        data.put("ORDER_ID", order.getSerialNumber() + "-" + order.getVerCode());
        data.put("ORDER_NUMBER", nextNumber(order.getShopDetailId(), order.getId()));
        if (refundItems.size() != 0) {
            Map<String, Object> map = new HashMap<String, Object>();
            for (int i = 0; i < refundItems.size(); i++) {
                map = refundItems.get(i);
                items.add(map);
            }
        }
        data.put("ITEMS", items);
        Appraise appraise = appraiseService.selectAppraiseByCustomerId(order.getCustomerId(), order.getShopDetailId());
        StringBuilder star = new StringBuilder();
        BigDecimal level = new BigDecimal(0);
        if (appraise != null) {
            if (appraise != null && appraise.getLevel() < 5) {
                for (int i = 0; i < appraise.getLevel(); i++) {
                    star.append("★");
                }
                for (int i = 0; i < 5 - appraise.getLevel(); i++) {
                    star.append("☆");
                }
            } else if (appraise != null && appraise.getLevel() == 5) {
                star.append("★★★★★");
            }
            Map<String, Object> appriseCount = appraiseService.selectCustomerAppraiseAvg(order.getCustomerId());
            level = new BigDecimal(Integer.valueOf(appriseCount.get("sum").toString()))
                    .divide(new BigDecimal(Integer.valueOf(appriseCount.get("count").toString())), 2, BigDecimal.ROUND_HALF_UP);
        } else {
            star.append("☆☆☆☆☆");
        }
        StringBuilder gao = new StringBuilder();
        if (shopDetail.getIsUserIdentity().equals(1)) {
            //得到有限制的情况下用户的订单数
            int gaoCount = orderMapper.selectByCustomerCount(order.getCustomerId(), shopDetail.getConsumeConfineUnit(), shopDetail.getConsumeConfineTime());
            //得到无限制情况下用户的订单数
            int gaoCountlong = orderMapper.selectByCustomerCount(order.getCustomerId(), shopDetail.getConsumeConfineUnit(), 0);
            if (shopDetail.getConsumeNumber() > 0 && gaoCount > shopDetail.getConsumeNumber() && shopDetail.getConsumeConfineUnit() != 3) {
                gao.append("【高频】");
            }//无限制的时候
            else if (shopDetail.getConsumeConfineUnit() == 3 && gaoCountlong > shopDetail.getConsumeNumber()) {
                gao.append("【高频】");
            }
        }
        String modeText = getModeText(order);
        data.put("DISTRIBUTION_MODE", modeText);
        data.put("ORIGINAL_AMOUNT", order.getOrderMoney());
        data.put("RESTAURANT_ADDRESS", shopDetail.getAddress());
        data.put("REDUCTION_AMOUNT", order.getReductionAmount());
        data.put("RESTAURANT_TEL", shopDetail.getPhone());
        data.put("TABLE_NUMBER", order.getTableNumber());
        data.put("CUSTOMER_COUNT", order.getCustomerCount() == null ? "-" : order.getCustomerCount());
        data.put("PAYMENT_AMOUNT", order.getPaymentAmount());
        data.put("RESTAURANT_NAME", shopDetail.getName());
        data.put("DATETIME", DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
        BigDecimal articleCount = new BigDecimal(order.getArticleCount());
        articleCount = articleCount.add(new BigDecimal(order.getCustomerCount() == null ? 0
                : order.getCustomerCount()));
        articleCount = articleCount.add(new BigDecimal(order.getMealAllNumber() == null ? 0
                : order.getMealAllNumber()));
        List<Order> childList = orderMapper.selectListByParentId(order.getId());
        for (Order child : childList) {
            articleCount = articleCount.add(BigDecimal.valueOf(child.getArticleCount()));
            articleCount = articleCount.add(BigDecimal.valueOf(child.getMealAllNumber() == null ? 0 : child.getMealAllNumber()));
        }

        data.put("ARTICLE_COUNT", articleCount);
        List<Map<String, Object>> patMentItems = new ArrayList<Map<String, Object>>();
        List<OrderPaymentItem> orderPaymentItems = orderPaymentItemService.selectPaymentCountByOrderId(order.getId());
        List<String> child = orderMapper.selectChildIdsByParentId(order.getId());
        if (!CollectionUtils.isEmpty(child)) {
            for (String childId : child) {
                List<OrderPaymentItem> childPay = orderPaymentItemService.selectPaymentCountByOrderId(childId);
                orderPaymentItems.addAll(childPay);
            }
        }

        Map<Integer, BigDecimal> map = new HashMap<>();
        for (OrderPaymentItem orderPaymentItem : orderPaymentItems) {
            if (map.containsKey(orderPaymentItem.getPaymentModeId())) {
                BigDecimal newValue = map.get(orderPaymentItem.getPaymentModeId()).add(orderPaymentItem.getPayValue());
                map.put(orderPaymentItem.getPaymentModeId(), newValue);
            } else {
                map.put(orderPaymentItem.getPaymentModeId(), orderPaymentItem.getPayValue());
            }
        }


        if (!map.isEmpty()) {
            for (Integer key : map.keySet()) {
                Map<String, Object> patMentItem = new HashMap<String, Object>();
                patMentItem.put("SUBTOTAL", map.get(key));
                patMentItem.put("PAYMENT_MODE", PayMode.getPayModeName(key));
                patMentItems.add(patMentItem);
            }
        } else {
            Map<String, Object> patMentItem = new HashMap<String, Object>();
            patMentItem.put("SUBTOTAL", 0);
            patMentItem.put("PAYMENT_MODE", "");
            patMentItems.add(patMentItem);
        }
        data.put("PAYMENT_ITEMS", patMentItems);
        data.put("CUSTOMER_SATISFACTION", star.toString());
        data.put("CUSTOMER_SATISFACTION_DEGREE", level);
        Account account = accountService.selectAccountAndLogByCustomerId(order.getCustomerId());
        StringBuffer customerStr = new StringBuffer();
        if (account != null) {
            customerStr.append("余额：" + account.getRemain() + " ");
        } else {
            customerStr.append("余额：0 ");
        }
        customerStr.append("" + gao.toString() + " ");
        Customer customer = customerService.selectById(order.getCustomerId());
        CustomerDetail customerDetail = customerDetailMapper.selectByPrimaryKey(customer.getCustomerDetailId());
        if (customerDetail != null) {
            if (customerDetail.getBirthDate() != null) {
                if (DateUtil.formatDate(customerDetail.getBirthDate(), "MM-dd")
                        .equals(DateUtil.formatDate(new Date(), "MM-dd"))) {
                    customerStr.append("★" + DateUtil.formatDate(customerDetail.getBirthDate(), "yyyy-MM-dd") + "★");
                }
            }
        }
        data.put("CUSTOMER_PROPERTY", customerStr.toString());
        print.put("DATA", data);
        print.put("STATUS", 0);
        print.put("TICKET_TYPE", TicketType.RECEIPT);
        return print;
    }


    @Override
    public Order confirmOrder(Order order) {
        order = selectById(order.getId());
        log.info("开始确认订单:" + order.getId());
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
            log.info("订单已确认:" + order.getId() + "评论:" + order.getAllowAppraise());
            return order;
        }
        return null;
    }

    @Override
    public Order getOrderInfo(String orderId) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            return null;
        }
        List<OrderItem> orderItems = orderItemService.listByOrderId(orderId);
        order.setOrderItems(orderItems);
        Customer cus = customerService.selectById(order.getCustomerId());
        order.setCustomer(cus);
        return order;
    }


    @Override
    public List<Order> selectHistoryOrderList(String currentShopId, Date date, Integer shopMode) {
            return orderMapper.listHoufuFinishedOrder(currentShopId);
    }

    @Override
    public List<Order> selectErrorOrderList(String currentShopId, Date date) {
        Date begin = DateUtil.getDateBegin(date);
        Date end = DateUtil.getDateEnd(date);
        return orderMapper.selectErrorOrderList(currentShopId, begin, end);
    }


    @Override
    public List<Order> getOrderNoPayList(String currentShopId, Date date) {
        Date begin = DateUtil.getDateBegin(date);
        Date end = DateUtil.getDateEnd(date);
        return orderMapper.getOrderNoPayList(currentShopId, begin, end);
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

            //拒绝订单后还原库存
            Boolean addStockSuccess = false;
            addStockSuccess = addStock(getOrderInfo(orderId));
            if (!addStockSuccess) {
                log.info("库存还原失败:" + order.getId());
            }
            orderMapper.setStockBySuit(order.getShopDetailId());//自动更新套餐数量
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
        List<Map<String, Object>> printTask = new ArrayList<>();
        if ((order.getPrintOrderTime() != null || order.getProductionStatus() >= 2) && order.getOrderMode() != ShopMode.HOUFU_ORDER) {
            return printTask;
        }

        ShopDetail shop = shopDetailService.selectById(order.getShopDetailId());
        List<OrderItem> items = orderItemService.listByOrderId(orderId);


        List<Printer> ticketPrinter = printerService.selectByShopAndType(shop.getId(), PrinterType.RECEPTION);
        ShopDetail shopDetail = shopDetailService.selectById(order.getShopDetailId());
        BrandSetting setting = brandSettingService.selectByBrandId(order.getBrandId());
        if (setting.getAutoPrintTotal().intValue() == 0 && shopDetail.getAutoPrintTotal() == 0 &&
                (order.getOrderMode() != ShopMode.HOUFU_ORDER || (order.getOrderState() == OrderState.SUBMIT && order.getOrderMode() == ShopMode.HOUFU_ORDER))) {
            List<OrderItem> child = orderItemService.listByParentId(orderId);
            for (OrderItem orderItem : child) {
                order.setOriginalAmount(order.getOriginalAmount().add(orderItem.getFinalPrice()));
//                order.setPaymentAmount(order.getPaymentAmount().add(orderItem.getFinalPrice()));
            }
            child.addAll(items);
            for (Printer printer : ticketPrinter) {
                Map<String, Object> ticket = printTicket(order, items, shop, printer);
                if (ticket != null) {
                    printTask.add(ticket);
                }

            }
        }

        if (order.getOrderMode().equals(ShopMode.HOUFU_ORDER) && order.getOrderState().equals(OrderState.PAYMENT)
                && setting.getIsPrintPayAfter().equals(Common.YES) && shopDetail.getIsPrintPayAfter().equals(Common.YES)) {
            List<OrderItem> child = orderItemService.listByParentId(orderId);
            for (OrderItem orderItem : child) {
                order.setOriginalAmount(order.getOriginalAmount().add(orderItem.getFinalPrice()));
//                order.setPaymentAmount(order.getPaymentAmount().add(orderItem.getFinalPrice()));
            }
            child.addAll(items);

            for (Printer printer : ticketPrinter) {
                Map<String, Object> ticket = printTicket(order, child, shop, printer);
                if (ticket != null) {
                    printTask.add(ticket);
                }
            }
        }

        List<Map<String, Object>> kitchenTicket = printKitchen(order, items);


        //如果是外带，添加一张外带小票
        if (order.getDistributionModeId().equals(DistributionType.TAKE_IT_SELF) && setting.getIsPrintPayAfter().equals(Common.NO)) {
            List<Printer> packagePrinter = printerService.selectByShopAndType(order.getShopDetailId(), PrinterType.PACKAGE); //查找外带的打印机
            for (Printer printer : packagePrinter) {
                Map<String, Object> packageTicket = printTicket(order, items, shop, printer);
                if (packageTicket != null) {
                    printTask.add(packageTicket);
                }
            }
        }

        if (!kitchenTicket.isEmpty() && order.getOrderMode() == ShopMode.HOUFU_ORDER && order.getProductionStatus() == ProductionStatus.HAS_ORDER) {
            printTask.addAll(kitchenTicket);
        }
        if (!kitchenTicket.isEmpty() && order.getOrderMode() != ShopMode.HOUFU_ORDER) {
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

            if (articleSellDto.getType() == 3) {
                articleSellDto.setTypeName("套餐");
            } else {
                articleSellDto.setTypeName("单品");
            }

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
    /**
     * 2016-11-2
     */
    public brandArticleReportDto selectBrandArticleNum(String beginDate, String endDate, String brandId, String brandName) {
        Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);
        Integer totalNum = 0;
        //brandArticleReportDto bo = orderMapper.selectArticleSumCountByData(begin, end, brandId);
        //totalNum = orderMapper.selectArticleSumCountByData(begin, end, brandId);
        /**
         * 菜品总数单独算是因为 要出去套餐的数量
         */
        totalNum = orderMapper.selectBrandArticleNum(begin, end, brandId);
        //查询菜品总额，退菜总数，退菜金额
        brandArticleReportDto bo = orderMapper.selectConfirmMoney(begin, end, brandId);
        bo.setTotalNum(totalNum);
        bo.setBrandName(brandName);
        return bo;
    }


    @Override
    public List<ShopArticleReportDto> selectShopArticleDetails(String beginDate, String endDate, String brandId, List<ShopDetail> shopDetails) {
        Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);
        if (shopDetails.isEmpty()) {
            shopDetails = shopDetailService.selectByBrandId(brandId);
        }
        //查询品牌下每个店铺的菜品销量，菜品销售额 和占品牌销售的比率(和品牌一样需要分开计算数量和总价--套餐问题)

        //查询每个店铺的菜品销量的和
        List<ShopArticleReportDto> listShopNum = orderMapper.selectShopArticleSum(begin, end, brandId);

        //查询每个店铺的菜品销售的和
        List<ShopArticleReportDto> list = orderMapper.selectShopArticleSell(begin, end, brandId);

        if (!list.isEmpty()) {
            for (ShopArticleReportDto s1 : list) {
                for (ShopArticleReportDto s2 : listShopNum) {
                    if (s2.getShopId().equals(s1.getShopId())) {
                        s1.setTotalNum(s2.getTotalNum());
                    }
                }
            }
        }

        List<ShopArticleReportDto> listArticles = new ArrayList<>();

        for (ShopDetail shop : shopDetails) {
            ShopArticleReportDto st = new ShopArticleReportDto(shop.getId(), shop.getName(), 0, BigDecimal.ZERO, "0.00%", 0, BigDecimal.ZERO);
            listArticles.add(st);
        }

        //计算所有店铺的菜品销售的和
        BigDecimal sum = new BigDecimal(0);
        //计算所有店铺的菜品销售的和
        if (!list.isEmpty()) {
            for (ShopArticleReportDto shopArticleReportDto2 : list) {
                //计算减去退菜销售额
                sum = sum.add(shopArticleReportDto2.getSellIncome().subtract(shopArticleReportDto2.getRefundTotal()));
            }

            for (ShopArticleReportDto shopArticleReportDto : listArticles) {
                for (ShopArticleReportDto shopArticleReportDto2 : list) {
                    if (shopArticleReportDto2.getShopId().equals(shopArticleReportDto.getShopId())) {
                        shopArticleReportDto.setSellIncome(shopArticleReportDto2.getSellIncome());
                        shopArticleReportDto.setTotalNum(shopArticleReportDto2.getTotalNum());
                        shopArticleReportDto.setRefundCount(shopArticleReportDto2.getRefundCount());
                        shopArticleReportDto.setRefundTotal(shopArticleReportDto2.getRefundTotal());
                        //当前店铺的销售总额 同样减去退菜的总数
                        BigDecimal current = shopArticleReportDto2.getSellIncome().subtract(shopArticleReportDto2.getRefundTotal());

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
            double c = articleSellDto.getSalles().divide(temp, 2, BigDecimal.ROUND_HALF_UP).doubleValue() * 100;
            DecimalFormat myformat = new DecimalFormat("0.00");
            String str = myformat.format(c);
            str = str + "%";
            articleSellDto.setSalesRatio(str);
        }

        return list;
    }

    @Override
    public List<ArticleSellDto> selectBrandArticleSellByDateAndId(String brandId, String beginDate, String endDate, String sort) {
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
                //新增减去退菜的数量
                num += (articleSellDto.getBrandSellNum().doubleValue() - articleSellDto.getRefundCount());

            }
            //计算总销售额
            //新增减去退菜金额
            temp = add(temp, (articleSellDto.getSalles().subtract(articleSellDto.getRefundTotal())));
        }

        for (ArticleSellDto articleSellDto : list) {
            //判断菜品的类型

            if (articleSellDto.getType() == 3) {
                articleSellDto.setTypeName("套餐");
            } else {
                articleSellDto.setTypeName("单品");
            }

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
            DecimalFormat myformat = new DecimalFormat("0.00");
            String str = myformat.format(c);
            str = str + "%";
            articleSellDto.setSalesRatio(str);
        }

        return list;
    }

    @Override
    /**
     * 2016-11-2
     */
    public Map<String, Object> selectMoneyAndNumByDate(String beginDate, String endDate, String brandId, String brandName, List<ShopDetail> shopDetailList) {
        Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);

        //查询后台数据
        List<Order> list = orderMapper.selectMoneyAndNumByDate(begin, end, brandId);

        //封装品牌的数据
        OrderPayDto brandPayDto = new OrderPayDto(brandName, BigDecimal.ZERO, 0, BigDecimal.ZERO, "");
        //品牌订单总额初始值
        BigDecimal d = BigDecimal.ZERO;
        //品牌实际支付初始值
        BigDecimal d1 = BigDecimal.ZERO;
        //品牌虚拟支付初始值
        BigDecimal d2 = BigDecimal.ZERO;
        //品牌平均订单金额
        BigDecimal average = BigDecimal.ZERO;
        //品牌订单数目初始值
        int number = 0;
        //品牌订单营销撬动率
        String marketPrize = "";
        Set<String> ids = new HashSet<>();
        for (Order o : list) {
            //封装品牌的数据
            //1.订单金额
            //判断是否是后付款模式
            if (o.getOrderMode() == 5) {
                if (o.getAmountWithChildren().compareTo(BigDecimal.ZERO) != 0) {
                    d = d.add(o.getAmountWithChildren());
                } else {
                    d = d.add(o.getOrderMoney());
                }
            } else {
                d = d.add(o.getOrderMoney());
            }
            //品牌订单数目 加菜订单和父订单算一个订单

            if (o.getParentOrderId() == null) {
                ids.add(o.getId());
            }

            if (!o.getOrderPaymentItems().isEmpty()) {
                for (OrderPaymentItem oi : o.getOrderPaymentItems()) {
                    //品牌实际支付  微信支付+
                    if (oi.getPaymentModeId() == PayMode.WEIXIN_PAY || oi.getPaymentModeId() == 6 || oi.getPaymentModeId() == 9 || oi.getPaymentModeId() == 10 || oi.getPaymentModeId() == 11) {
                        d1 = d1.add(oi.getPayValue());
                    }
                    //品牌虚拟支付(加上等位红包支付)
                    if (oi.getPaymentModeId() == 2 || oi.getPaymentModeId() == 3 || oi.getPaymentModeId() == 7 || oi.getPaymentModeId() == 8) {
                        d2 = d2.add(oi.getPayValue());
                    }
                }
            }
        }

        if (d2.compareTo(BigDecimal.ZERO) > 0) {
            //marketPrize = d1.divide(d2.setScale(2, BigDecimal.ROUND_HALF_UP))+"";
            marketPrize = d1.divide(d2, 2, BigDecimal.ROUND_HALF_UP) + "";
        }
        brandPayDto.setOrderMoney(d);
        if (ids != null && ids.size() > 0) {
            number = ids.size();
        }

        //品牌订单数目
        brandPayDto.setNumber(number);
        //品牌订单平均值
        if (number > 0) {
            //average = d.divide(new BigDecimal(String.valueOf(number)).setScale(2, BigDecimal.ROUND_HALF_UP));
            average = d.divide(new BigDecimal(String.valueOf(number)), 2, BigDecimal.ROUND_HALF_UP);
        }
        brandPayDto.setAverage(average);
        //品牌营销撬动率
        brandPayDto.setMarketPrize(marketPrize);

        //封装店铺的数据
        List<OrderPayDto> shopPayDto = new ArrayList<>();
        for (ShopDetail shopDetail : shopDetailList) {
            OrderPayDto ot = new OrderPayDto(shopDetail.getId(), shopDetail.getName(), BigDecimal.ZERO, 0, BigDecimal.ZERO, "");
            shopPayDto.add(ot);
        }

        //遍历订单中的是否含有这个店铺的id 有的话说明这个店铺有订单那么修改初始值
        for (OrderPayDto sd : shopPayDto) {
            //店铺订单的总额初始值
            BigDecimal ds1 = BigDecimal.ZERO;
            //店铺实际支付初始值
            BigDecimal ds2 = BigDecimal.ZERO;
            //店铺虚拟支付初始值
            BigDecimal ds3 = BigDecimal.ZERO;
            //店铺平均订单金额
            BigDecimal saverage = BigDecimal.ZERO;
            //店铺订单数目初始值
            int snumber = 0;

            Set<String> sids = new HashSet<>();
            for (Order os : list) {
                if (sd.getShopDetailId().equals(os.getShopDetailId())) {
                    if (!os.getOrderPaymentItems().isEmpty()) {
                        for (OrderPaymentItem oi : os.getOrderPaymentItems()) {
                            //店铺实际支付
                            if (oi.getPaymentModeId() == 1 || oi.getPaymentModeId() == 6 || oi.getPaymentModeId() == 9 || oi.getPaymentModeId() == 10 || oi.getPaymentModeId() == 11) {
                                ds2 = ds2.add(oi.getPayValue());
                            }
                            //店铺虚拟支付
                            if (oi.getPaymentModeId() == 2 || oi.getPaymentModeId() == 3 || oi.getPaymentModeId() == 7 || oi.getPaymentModeId() == 8) {
                                ds3 = ds3.add(oi.getPayValue());
                            }
                        }
                    }

                    //计算店铺订单总额
//                    if(os.getAmountWithChildren().compareTo(BigDecimal.ZERO)!=0){
//                        ds1=ds1.add(os.getAmountWithChildren());
//                    }else {
//                        ds1 = ds1.add(os.getOrderMoney());
//                    }

                    //判断是否是后付款模式
                    if (os.getOrderMode() == 5) {
                        if (os.getAmountWithChildren().compareTo(BigDecimal.ZERO) != 0) {
                            ds1 = ds1.add(os.getAmountWithChildren());
                        } else {
                            ds1 = ds1.add(os.getOrderMoney());
                        }
                    } else {
                        ds1 = ds1.add(os.getOrderMoney());
                    }

                    //计算店铺的订单数目
                    if (os.getParentOrderId() == null) {
                        sids.add(os.getId());
                    }
                    if (sids.size() > 0) {
                        snumber = sids.size();
                    }
                }
            }

            // String smarketPrize="";//营销撬动率

            //赋值店铺订单总额
            sd.setOrderMoney(ds1);

            //赋值店铺订单数目
            sd.setNumber(snumber);

            //赋值店铺的订单平均金额
            if (snumber > 0) {
                //sd.setAverage(ds1.divide(new BigDecimal(String.valueOf(snumber))).setScale(2, BigDecimal.ROUND_HALF_UP));
                sd.setAverage(ds1.divide(new BigDecimal(String.valueOf(snumber)), 2, BigDecimal.ROUND_HALF_UP));
            }

            //赋值店铺营销撬动率
            if (ds3.compareTo(BigDecimal.ZERO) > 0) {
                //sd.setMarketPrize(ds2.divide(ds3).setScale(2, BigDecimal.ROUND_HALF_UP)+"");
                sd.setMarketPrize(ds2.divide(ds3, 2, BigDecimal.ROUND_HALF_UP) + "");
            }
        }
        Map<String, Object> map = new HashMap<>();

        map.put("shopId", shopPayDto);
        map.put("brandId", brandPayDto);

        return map;
    }

    @Override
    public List<ArticleSellDto> selectShopArticleSellByDateAndFamilyId(String beginDate, String endDate, String shopId, String sort) {
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
            //double c = articleSellDto.getSalles().divide(temp, BigDecimal.ROUND_HALF_UP).doubleValue() * 100;
            double c = articleSellDto.getSalles().divide(temp, 2, BigDecimal.ROUND_HALF_UP).doubleValue() * 100;
            DecimalFormat myformat = new DecimalFormat("0.00");
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
        //计算总菜品数
        double num = 0;

        BigDecimal temp = BigDecimal.ZERO;
        for (ArticleSellDto articleSellDto : list) {
            //计算总销量 不能加上套餐的数量
            if (articleSellDto.getType() != 3) {
                num += articleSellDto.getShopSellNum().doubleValue();
            }
            temp = add(temp, articleSellDto.getSalles());
        }
        for (ArticleSellDto articleSellDto : list) {
            //销售额占比
            BigDecimal d = articleSellDto.getSalles().divide(temp, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
            articleSellDto.setSalesRatio(d + "%");
            if (num != 0) {
                double d1 = articleSellDto.getShopSellNum().doubleValue();
                double d2 = d1 / num * 100;
                //保留三位小数
                BigDecimal b = new BigDecimal(d2);
                double f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                articleSellDto.setNumRatio(f1 + "%");
            }
        }

        return list;
    }

    @Override
    public List<Order> selectListByTime(String beginDate, String endDate, String shopId,int start,int length,String search) {
        Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);
        return orderMapper.selectListByTime(begin, end, shopId,start,length,search);

    }

    @Override
    public Order selectOrderDetails(String orderId) {
        Order o = orderMapper.selectOrderDetails(orderId);
        ShopDetail shop = shopDetailService.selectById(o.getShopDetailId());
        if (shop != null) {
            o.setShopName(shop.getName());
        }

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
    public List<Order> selectListByShopId(String beginDate, String endDate, String shopId) {
        Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);
        return orderMapper.selectListByShopId(begin, end, shopId);
    }


    @Override
    public List<Order> selectAppraiseByShopId(String beginDate, String endDate, String shopId) {
        Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);
        return orderMapper.selectAppraiseByShopId(begin, end, shopId);
    }


    @Override
    public Order getOrderAccount(String shopId) {
        Order order = null;
        ShopDetail shopDetail = shopDetailService.selectByPrimaryKey(shopId);
        if (shopDetail.getShopMode() == ShopMode.HOUFU_ORDER) {
            order = orderMapper.getOrderAccountHoufu(shopId);
        }else if (shopDetail.getShopMode() == ShopMode.BOSS_ORDER){
            order = orderMapper.getOrderAccountBoss(shopId);
        } else {
            order = orderMapper.getOrderAccount(shopId);
        }

        return order;
    }

    @Override
    public void autoRefundMoney() {
        log.debug("开始退款");
    }


    @Override
    public List<Map<String, Object>> printTotal(String shopId) {

        List<Map<String, Object>> printTask = new ArrayList<>();
        ShopDetail shop = shopDetailService.selectById(shopId);

        List<Printer> ticketPrinter = printerService.selectByShopAndType(shop.getId(), PrinterType.RECEPTION);
        for (Printer printer : ticketPrinter) {
            Map<String, Object> ticket = printTotal(shop, printer);
            if (ticket != null) {
                printTask.add(ticket);
            }

        }

        return printTask;
    }


    @Override
    public List<Map<String, Object>> printKitchenReceipt(String orderId) {
        log.info("打印订单全部:" + orderId);
        Order order = selectById(orderId);
        //如果是 未打印状态 或者  异常状态则改变 生产状态和打印时间
        if (ProductionStatus.HAS_ORDER == order.getProductionStatus() || ProductionStatus.NOT_PRINT == order.getProductionStatus()) {
            order.setProductionStatus(ProductionStatus.PRINTED);
            order.setPrintOrderTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
        }
        ShopDetail shop = shopDetailService.selectById(order.getShopDetailId());
        List<OrderItem> items = orderItemService.listByOrderId(orderId);
        List<Map<String, Object>> printTask = new ArrayList<>();
//        List<Printer> ticketPrinter = printerService.selectByShopAndType(shop.getId(), PrinterType.RECEPTION);
//        BrandSetting setting = brandSettingService.selectByBrandId(order.getBrandId());
//        if(setting.getAutoPrintTotal().intValue() == 0){
//            for (Printer printer : ticketPrinter) {
//                Map<String, Object> ticket = printTicket(order, items, shop, printer);
//                if (ticket != null) {
//                    printTask.add(ticket);
//                }
//
//            }
//        }

//        if (order.getOrderMode() == ShopMode.HOUFU_ORDER) {
        List<OrderItem> child = orderItemService.listByParentId(orderId);
        for (OrderItem item : child) {
            item.setArticleName(item.getArticleName() + "(加)");
        }
        items.addAll(child);
//        }


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

//        }
        if (!kitchenTicket.isEmpty()) {
            printTask.addAll(kitchenTicket);
        }
        return printTask;
    }

    /**
     * 订单菜品的数据(用于中间数据库)
     *
     * @param brandId
     * @return
     */
    @Override
    public List<OrderArticleDto> selectOrderArticle(String brandId, String beginDate, String endDate) {
        Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);
        return orderMapper.selectOrderArticle(brandId, begin, end);
    }

    /**
     * 封装品牌菜品数据 用于中间数据库
     *
     * @param brandId
     * @param beginDate
     * @param endDate
     * @return
     */
    @Override
    public List<Map<String, Object>> selectBrandArticleSellList(String brandId, String beginDate, String endDate) {
        Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);

        List<Map<String, Object>> list = orderMapper.selectBrandArticleSellList(brandId, begin, end);
        //计算总菜品销售额,//菜品总销售额
        int num = 0;

        BigDecimal temp = BigDecimal.ZERO;
        for (Map<String, Object> map : list) {
            //计算总销量 不能加上套餐的数量
            if ((Integer) map.get("type") != 3) {
                // num += articleSellDto.getBrandSellNum().doubleValue();
                num += Integer.parseInt(map.get("salles").toString());
            }
            //计算总销售额
            temp = add(temp, new BigDecimal(map.get("sell").toString()));
        }

        for (Map<String, Object> map2 : list) {
            //销售额占比
            // BigDecimal d = new BigDecimal(map2.get("selles").toString()).divide(temp, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
            BigDecimal sell = new BigDecimal(map2.get("sell").toString());
            BigDecimal d = sell.divide(temp, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
            //map2.setSalesRatio(d + "%");
            map2.put("sell_occupies", d + "%");

            if (num != 0) {
                //double d1 = articleSellDto.getBrandSellNum().doubleValue();
                double d1 = Double.parseDouble(map2.get("salles").toString());
                double d2 = d1 / num * 100;

                //保留两位小数
                BigDecimal b = new BigDecimal(d2);
                double f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                // articleSellDto.setNumRatio(f1 + "%");
                map2.put("salles_occupies", f1 + "%");
            }
        }

        for (Map<String, Object> map3 : list) {

            map3.remove("type");
            map3.remove("sort");
            map3.remove("order_id");
        }


        return list;
    }

    @Override
    public List<Map<String, Object>> selectShopArticleSellList(String brandId, String beginDate, String endDate) {
        Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);
        List<ShopDetail> shops = shopDetailService.selectByBrandId(brandId);
        List<Map<String, Object>> list = new ArrayList<>();

        for (ShopDetail s : shops) {
            List<Map<String, Object>> list2 = orderMapper.selectShopArticleSellList(s.getId(), begin, end);
            if (list2 != null && list2.size() > 0) {
                //计算总菜品销售额,//菜品总销售额
                int num = 0;

                BigDecimal temp = BigDecimal.ZERO;
                for (Map<String, Object> map2 : list2) {
                    //计算总销量 不能加上套餐的数量
                    if ((Integer) map2.get("type") != 3) {
                        // num += articleSellDto.getBrandSellNum().doubleValue();
                        num += Integer.parseInt(map2.get("salles").toString());
                    }
                    //计算总销售额
                    temp = add(temp, new BigDecimal(map2.get("sell").toString()));
                }

                for (Map<String, Object> map3 : list2) {
                    //销售额占比
                    // BigDecimal d = new BigDecimal(map2.get("selles").toString()).divide(temp, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                    BigDecimal sell = new BigDecimal(map3.get("sell").toString());
                    BigDecimal d = sell.divide(temp, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                    //map2.setSalesRatio(d + "%");
                    map3.put("sell_occupies", d + "%");

                    if (num != 0) {
                        //double d1 = articleSellDto.getBrandSellNum().doubleValue();
                        double d1 = Double.parseDouble(map3.get("salles").toString());
                        double d2 = d1 / num * 100;

                        //保留两位小数
                        BigDecimal b = new BigDecimal(d2);
                        double f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                        // articleSellDto.setNumRatio(f1 + "%");
                        map3.put("salles_occupies", f1 + "%");
                    }
                }
                for (Map<String, Object> map4 : list2) {
                    map4.remove("sort");
                    map4.remove("type");
                    map4.remove("order_id");
                }
                list.addAll(list2);

            }
        }

        return list;
    }

    /**
     * 查询订单详情的数据 用于中间数据库
     *
     * @param beginDate
     * @param endDate
     * @param brandId
     * @return
     */
    @Override
    public List<Order> selectListByTimeAndBrandId(String brandId, String beginDate, String endDate) {
        Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);
        return orderMapper.selectListByTimeAndBrandId(brandId, begin, end);
    }

    @Override
    public List<Order> selectAllAlreadyConsumed(String brandId, String beginDate, String endDate) {
        Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);
        return orderMapper.selectMoneyAndNumByDate(begin, end, brandId);
    }

    @Override
    public List<Order> getTableNumberAll(String shopId) {
        return orderMapper.getTableNumberAll(shopId);
    }


    public Map<String, Object> printTotal(ShopDetail shopDetail, Printer printer) {
        if (printer == null) {
            return null;
        }
        Order order = null;
        if (shopDetail.getShopMode() == ShopMode.HOUFU_ORDER) {
            order = orderMapper.getOrderAccountHoufu(shopDetail.getId());
        } else {
            order = orderMapper.getOrderAccount(shopDetail.getId());
        }
        Map<String, Object> print = new HashMap<>();
        print.put("KITCHEN_NAME", printer.getName());
        print.put("PORT", printer.getPort());
        print.put("IP", printer.getIp());
        String print_id = ApplicationUtils.randomUUID();
        print.put("PRINT_TASK_ID", print_id);
        print.put("ADD_TIME", new Date());
        Map<String, Object> data = new HashMap<>();
        data.put("RESTAURANT_NAME", shopDetail.getName());
        data.put("DATE", DateUtil.formatDate(new Date(), "yyyy-MM-dd"));
        BigDecimal totalAmount = new BigDecimal(0);
        if (order.getOrderTotal() != null) {
            totalAmount = totalAmount.add(order.getOrderTotal());
        }
        data.put("TOTAL_AMOUNT", totalAmount);
        BigDecimal orderAmount = new BigDecimal(0);
        if (new BigDecimal(order.getOrderCount()) != null) {
            orderAmount = orderAmount.add(new BigDecimal(order.getOrderCount()));
        }
        data.put("ORDER_AMOUNT", orderAmount);
        DecimalFormat df = new DecimalFormat("######0.00");
        double average = orderAmount.equals(BigDecimal.ZERO) ? 0 :
                totalAmount.doubleValue() / orderAmount.doubleValue();
        data.put("ORDER_AVERAGE", df.format(average));
        BigDecimal customerAmount = new BigDecimal(0);
        if (order.getCustomerCount() != null) {
            customerAmount = customerAmount.add(new BigDecimal(order.getCustomerCount()));
        }
        data.put("CUSTOMER_AMOUNT", customerAmount);
        double customerAverage = customerAmount.equals(BigDecimal.ZERO) ? 0 :
                totalAmount.doubleValue() / customerAmount.doubleValue();
        data.put("CUSTOMER_AVERAGE", df.format(customerAverage));
        BigDecimal wxPay = orderMapper.getPayment(PayMode.WEIXIN_PAY, shopDetail.getId());
        BigDecimal chargePay = orderMapper.getPayment(PayMode.CHARGE_PAY, shopDetail.getId());
        BigDecimal aliPay = orderMapper.getPayment(PayMode.ALI_PAY, shopDetail.getId());
        BigDecimal otherPay = orderMapper.getPayment(PayMode.MONEY_PAY, shopDetail.getId());
//        BigDecimal articlePay = orderMapper.getPayment(PayMode.ARTICLE_BACK_PAY, shopDetail.getId());
        BigDecimal incomeAmount = wxPay.add(chargePay).add(aliPay).add(otherPay);
//        BigDecimal incomeAmount = wxPay.add(chargePay).add(aliPay).add(otherPay).add(articlePay);
        data.put("INCOME_AMOUNT", incomeAmount == null ? 0 : incomeAmount);
        List<Map<String, Object>> incomeItems = new ArrayList<>();
        Map<String, Object> wxItem = new HashMap<>();
        wxItem.put("SUBTOTAL", wxPay == null ? 0 : wxPay);
        wxItem.put("PAYMENT_MODE", "微信支付");
        Map<String, Object> chargeItem = new HashMap<>();
        chargeItem.put("SUBTOTAL", chargePay == null ? 0 : chargePay);
        chargeItem.put("PAYMENT_MODE", "充值支付");
        Map<String, Object> aliPayment = new HashMap<>();
        aliPayment.put("SUBTOTAL", aliPay == null ? 0 : aliPay);
        aliPayment.put("PAYMENT_MODE", "支付宝支付");
        Map<String, Object> otherPayment = new HashMap<>();
        otherPayment.put("SUBTOTAL", otherPay == null ? 0 : otherPay);
        otherPayment.put("PAYMENT_MODE", "其他方式支付");
//        Map<String, Object> articleBackPay = new HashMap<>();
//        articleBackPay.put("SUBTOTAL", articlePay == null ? 0 : articlePay);
//        articleBackPay.put("PAYMENT_MODE", "退菜红包");
        incomeItems.add(wxItem);
        incomeItems.add(aliPayment);
        incomeItems.add(otherPayment);
        incomeItems.add(chargeItem);
//        incomeItems.add(articleBackPay);
        data.put("INCOME_ITEMS", incomeItems);
        BigDecimal accountPay = orderMapper.getPayment(PayMode.ACCOUNT_PAY, shopDetail.getId());
        BigDecimal couponPay = orderMapper.getPayment(PayMode.COUPON_PAY, shopDetail.getId());
        BigDecimal rewardPay = orderMapper.getPayment(PayMode.REWARD_PAY, shopDetail.getId());
        BigDecimal waitMoney = orderMapper.getPayment(PayMode.WAIT_MONEY, shopDetail.getId());
        BigDecimal articlePay = orderMapper.getPayment(PayMode.ARTICLE_BACK_PAY, shopDetail.getId());
        BigDecimal discountAmount = accountPay.add(couponPay).add(rewardPay).add(waitMoney).add(articlePay);
        data.put("DISCOUNT_AMOUNT", discountAmount == null ? 0 : discountAmount);
        List<Map<String, Object>> discountItems = new ArrayList<>();
        Map<String, Object> accountPayItem = new HashMap<>();
        accountPayItem.put("SUBTOTAL", accountPay == null ? 0 : accountPay);
        accountPayItem.put("PAYMENT_MODE", "红包支付");
        discountItems.add(accountPayItem);
        Map<String, Object> couponPayItem = new HashMap<>();
        couponPayItem.put("SUBTOTAL", couponPay == null ? 0 : couponPay);
        couponPayItem.put("PAYMENT_MODE", "优惠券支付");
        discountItems.add(couponPayItem);
        Map<String, Object> rewardPayItem = new HashMap<>();
        rewardPayItem.put("SUBTOTAL", rewardPay == null ? 0 : rewardPay);
        rewardPayItem.put("PAYMENT_MODE", "充值赠送支付");
        discountItems.add(rewardPayItem);
        Map<String, Object> waitMoneyItem = new HashMap<>();
        waitMoneyItem.put("SUBTOTAL", waitMoney == null ? 0 : waitMoney);
        waitMoneyItem.put("PAYMENT_MODE", "等位红包支付");
        discountItems.add(waitMoneyItem);
        Map<String, Object> articleBackPay = new HashMap<>();
        articleBackPay.put("SUBTOTAL", articlePay == null ? 0 : articlePay);
        articleBackPay.put("PAYMENT_MODE", "退菜红包");
        discountItems.add(articleBackPay);
        data.put("DISCOUNT_ITEMS", discountItems);
        List<Map<String, Object>> chargeOrders = chargeOrderService.selectByShopToDay(shopDetail.getId());
        data.put("STORED_VALUE_COUNT", chargeOrders.size());
        BigDecimal chargeAmount = new BigDecimal(0);
        List<Map<String, Object>> storedValueItems = new ArrayList<Map<String, Object>>();
        if (chargeOrders.size() > 0) {
            for (Map<String, Object> chargeOrder : chargeOrders) {
                Map<String, Object> chargeMap = new HashMap<String, Object>();
                chargeMap.put("SUBTOTAL", chargeOrder.get("chargeMoney"));
                chargeMap.put("TEL", chargeOrder.get("telephone"));
                chargeAmount = chargeAmount.add(new BigDecimal(chargeOrder.get("chargeMoney").toString()));
                storedValueItems.add(chargeMap);
            }
        }
        data.put("STORED_VALUE_AMOUNT", chargeAmount);
        data.put("STORED_VALUE_ITEMS", storedValueItems);
        BigDecimal saledProductAmount = new BigDecimal(0);
        BigDecimal canceledProductCount = new BigDecimal(0);
        BigDecimal canceledProductAmount = new BigDecimal(0);
        BigDecimal canceledOrderCount = new BigDecimal(0);
        Map<String, Object> canceledOrderMap = new HashMap<>();
        List<Map<String, Object>> saledProducts = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> canceledProducts = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> canceledOrders = new ArrayList<Map<String, Object>>();
        if (StringUtils.isNotBlank(order.getId())) {
            BrandSetting brandSetting = brandSettingService.selectByBrandId(shopDetail.getBrandId());
            Brand brand = brandService.selectBrandBySetting(brandSetting.getId());
            String[] orderIds = order.getId().split(",");
            Map<String, Object> selectOrderMap = new HashMap<String, Object>();
            selectOrderMap.put("orderIds", orderIds);
            List<Order> orders = orderMapper.selectOrderByOrderIds(selectOrderMap);
            BigDecimal nowService = new BigDecimal(0);
            BigDecimal oldService = new BigDecimal(0);
            BigDecimal nowMeal = new BigDecimal(0);
            BigDecimal oldMeal = new BigDecimal(0);
            Map<String, Object> serviceMap = new HashMap<>();
            if ("27f56b31669f4d43805226709874b530".equals(brand.getId())) {
                serviceMap.put("serviceName", "就餐人数");
            } else {
                serviceMap.put("serviceName", brandSetting.getServiceName());
            }
            Map<String, Object> mealMap = new HashMap<>();
            mealMap.put("mealName", shopDetail.getMealFeeName());
            for (Order orderAll : orders) {
                BigDecimal nowCustomerCount = new BigDecimal(orderAll.getCustomerCount() == null ? 0 : orderAll.getCustomerCount());
                BigDecimal oldCustomerCount = new BigDecimal(orderAll.getBaseCustomerCount() == null ? 0 : orderAll.getBaseCustomerCount());
                if (orderAll.getDistributionModeId().equals(DistributionType.RESTAURANT_MODE_ID)) {
                    if (StringUtils.isBlank(orderAll.getParentOrderId())) {
                        serviceMap.put(orderAll.getId(), 0);
                    } else {
                        nowService = nowService.add(nowCustomerCount);
                        oldService = oldService.add(oldCustomerCount);
                        serviceMap.put(orderAll.getId(), oldCustomerCount.subtract(nowCustomerCount));
                    }
                } else if (orderAll.getDistributionModeId().equals(DistributionType.TAKE_IT_SELF) || orderAll.getDistributionModeId().equals(DistributionType.DELIVERY_MODE_ID)) {
                    BigDecimal nowMealAllNumber = new BigDecimal(orderAll.getMealAllNumber() == null ? 0 : orderAll.getMealAllNumber());
                    BigDecimal oldMealAllNumber = new BigDecimal(orderAll.getBaseMealAllCount() == null ? 0 : orderAll.getBaseMealAllCount());
                    nowMeal = nowMeal.add(nowMealAllNumber);
                    oldMeal = oldMeal.add(oldMealAllNumber);
                    mealMap.put(orderAll.getId(), oldMealAllNumber.subtract(nowMealAllNumber));
                }
                selectOrderMap.clear();
                selectOrderMap.put("orderId", orderAll.getId());
                selectOrderMap.put("count", "refund_count != 0");
                List<OrderItem> canceledOrderItems = orderItemService.selectOrderItemByOrderId(selectOrderMap);
                BigDecimal refundPrice = new BigDecimal(0);
                if (canceledOrderItems.size() != 0) {
                    String orderId = "";
                    for (OrderItem orderItem : canceledOrderItems) {
                        if (!orderId.equals(orderItem.getOrderId())) {
                            refundPrice = BigDecimal.ZERO;
                        }
                        refundPrice = refundPrice.add(orderItem.getUnitPrice().multiply(new BigDecimal(orderItem.getRefundCount())));
                        if (orderAll.getDistributionModeId().equals(DistributionType.RESTAURANT_MODE_ID)) {
                            if (!orderId.equals(orderItem.getOrderId())) {
                                refundPrice = refundPrice.add(new BigDecimal(serviceMap.get(orderItem.getOrderId()).toString()).multiply(brandSetting.getServicePrice()));
                            }
                        } else if (orderAll.getDistributionModeId().equals(DistributionType.TAKE_IT_SELF) || orderAll.getDistributionModeId().equals(DistributionType.DELIVERY_MODE_ID)) {
                            if (!orderId.equals(orderItem.getOrderId())) {
                                refundPrice = refundPrice.add(new BigDecimal(mealMap.get(orderItem.getOrderId()).toString()).multiply(shopDetail.getMealFeePrice()));
                            }
                        }
                        canceledOrderMap.put(orderItem.getOrderId(), refundPrice);
                        orderId = orderItem.getOrderId();
                    }
                } else if (!oldCustomerCount.equals(nowCustomerCount)) {
                    refundPrice = refundPrice.add(oldCustomerCount.subtract(nowCustomerCount).multiply(brandSetting.getServicePrice()));
                    canceledOrderMap.put(orderAll.getId(), refundPrice);
                }
            }
            selectOrderMap.clear();
            selectOrderMap.put("orderIds", orderIds);
            selectOrderMap.put("count", "count != 0");
            List<OrderItem> saledOrderItems = orderItemService.selectOrderItemByOrderIds(selectOrderMap);
            for (OrderItem orderItem : saledOrderItems) {
                saledProductAmount = saledProductAmount.add(new BigDecimal(orderItem.getCount()));
                Map<String, Object> itemMap = new HashMap<String, Object>();
                itemMap.put("PRODUCT_NAME", orderItem.getArticleName());
                itemMap.put("SUBTOTAL", orderItem.getCount());
                saledProducts.add(itemMap);
            }
            selectOrderMap.clear();
            selectOrderMap.put("orderIds", orderIds);
            selectOrderMap.put("count", "refund_count != 0");
            List<OrderItem> canceledOrderItems = orderItemService.selectOrderItemByOrderIds(selectOrderMap);
            for (OrderItem orderItem : canceledOrderItems) {
                canceledProductCount = canceledProductCount.add(new BigDecimal(orderItem.getRefundCount()));
                Map<String, Object> itemMap = new HashMap<String, Object>();
                itemMap.put("PRODUCT_NAME", orderItem.getArticleName());
                itemMap.put("SUBTOTAL", orderItem.getRefundCount());
                canceledProducts.add(itemMap);
            }
            if (!nowService.equals(BigDecimal.ZERO)) {
                Map<String, Object> itemMap = new HashMap<String, Object>();
                itemMap.put("PRODUCT_NAME", serviceMap.get("serviceName"));
                itemMap.put("SUBTOTAL", nowService);
                saledProducts.add(itemMap);
                saledProductAmount = saledProductAmount.add(nowService);
            }
            if (!nowMeal.equals(BigDecimal.ZERO)) {
                Map<String, Object> itemMap = new HashMap<String, Object>();
                itemMap.put("PRODUCT_NAME", mealMap.get("mealName"));
                itemMap.put("SUBTOTAL", nowMeal);
                saledProducts.add(itemMap);
                saledProductAmount = saledProductAmount.add(nowMeal);
            }
            if (!oldService.subtract(nowService).equals(BigDecimal.ZERO)) {
                Map<String, Object> itemMap = new HashMap<String, Object>();
                itemMap.put("PRODUCT_NAME", serviceMap.get("serviceName"));
                itemMap.put("SUBTOTAL", oldService.subtract(nowService));
                canceledProducts.add(itemMap);
                canceledProductCount = canceledProductCount.add(oldService.subtract(nowService));
            }
            if (!oldMeal.subtract(nowMeal).equals(BigDecimal.ZERO)) {
                Map<String, Object> itemMap = new HashMap<String, Object>();
                itemMap.put("PRODUCT_NAME", mealMap.get("mealName"));
                itemMap.put("SUBTOTAL", oldMeal.subtract(nowMeal));
                canceledProducts.add(itemMap);
                canceledProductCount = canceledProductCount.add(oldMeal.subtract(nowMeal));
            }
            canceledOrderCount = canceledOrderCount.add(new BigDecimal(canceledOrderMap.size()));
            for (Map.Entry<String, Object> map : canceledOrderMap.entrySet()) {
                Map<String, Object> canceledMap = new HashMap<>();
                Order canceledOrder = orderMapper.selectOrderDetails(map.getKey());
                canceledMap.put("ORDER_NUMBER", map.getKey());
                canceledMap.put("TEL", StringUtils.isBlank(canceledOrder.getCustomer().getTelephone()) ? "--" : canceledOrder.getCustomer().getTelephone());
                canceledMap.put("SUBTOTAL", map.getValue());
                canceledOrders.add(canceledMap);
                canceledProductAmount = canceledProductAmount.add(new BigDecimal(map.getValue().toString()));
            }
        }
        data.put("SALED_PRODUCT_AMOUNT", saledProductAmount);
        data.put("SALED_PRODUCTS", saledProducts);
        data.put("CANCELED_PRODUCT_COUNT", canceledProductCount);
        data.put("CANCELED_PRODUCTS", canceledProducts);
        data.put("CANCELED_ORDER_AMOUNT", canceledProductAmount);
        data.put("CANCELED_ORDER_COUNT", canceledOrderCount);
        data.put("CANCELED_ORDERS", canceledOrders);
        print.put("DATA", data);
        print.put("STATUS", 0);
        print.put("TICKET_TYPE", TicketType.DAILYREPORT);
        return print;
    }

    @Override
    public Result checkArticleCount(String orderId) {
        Order order = getOrderInfo(orderId);
        if (order == null || CollectionUtils.isEmpty(order.getOrderItems())) {
            return new Result("订单数据异常,请速与服务员联系", false);
        }

        Boolean result = true;
        String msg = "";


        //订单菜品不可为空
        for (OrderItem orderItem : order.getOrderItems()) {
            //有任何一个菜品售罄则不能出单
            Result check = checkStock(orderItem, order.getOrderItems().size());
            if (!check.isSuccess()) {
                result = false;
                msg = check.getMessage();
                break;
            }
        }

        return new Result(msg, result);

    }

    private Result checkStock(OrderItem orderItem, int count) {
        Boolean result = false;
        int current = 0;
        String msg = "";
        int min = 0;
        int endMin = 10000;
        switch (orderItem.getType()) {
            case OrderItemType.ARTICLE:
                //如果是单品无规格，直接判断菜品是否有库存
                current = orderMapper.selectArticleCount(orderItem.getArticleId());
                result = current >= count;
                msg = current == 0 ? orderItem.getArticleName() + "已售罄,请取消订单后重新下单" :
                        current >= count ? "库存足够" : orderItem.getArticleName() + "中单品库存不足,最大购买" + current + "个,请重新选购餐品";
                break;
            case OrderItemType.UNITPRICE:
                //如果是有规则菜品，则判断该规则是否有库存
                current = orderMapper.selectArticlePriceCount(orderItem.getArticleId());
                result = current >= count;
                msg = current == 0 ? orderItem.getArticleName() + "已售罄,请取消订单后重新下单" :
                        current >= count ? "库存足够" : orderItem.getArticleName() + "中单品库存不足,最大购买" + current + "个,请重新选购餐品";
                break;
            case OrderItemType.SETMEALS:
                //如果是套餐,不做判断，只判断套餐下的子品是否有库存
                current = orderMapper.selectArticleCount(orderItem.getArticleId());
                Map<String, Integer> order_items_map = new HashMap<String, Integer>();//用于保存套餐内的子菜品（防止套餐内出现同样餐品，检查库存出现异常）
                for (OrderItem oi : orderItem.getChildren()) {
                    //查询当前菜品，剩余多少份
                    min = orderMapper.selectArticleCount(oi.getArticleId());
                    if (order_items_map.containsKey(oi.getArticleId())) {
                        order_items_map.put(oi.getArticleId(), order_items_map.get(oi.getArticleId()) + oi.getCount());
                        min -= oi.getCount();
                    } else {
                        order_items_map.put(oi.getArticleId(), oi.getCount());
                    }
                    if (min < endMin) {
                        endMin = min;
                    }
                }
                //result = endMin>= count;
                result = endMin >= count && current >= count;
                msg = endMin == 0 ? orderItem.getArticleName() + "套餐单品已售罄,请取消订单后重新下单" :
                        endMin >= count && current >= count ? "库存足够" : orderItem.getArticleName() + "中单品库存不足,最大购买" + endMin + "个,请重新选购餐品";
                // 中单品库存不足,最大购买"+endMin+",个,请取消订单后重新下单
                break;
            case OrderItemType.MEALS_CHILDREN:
                //如果是套餐下的子品 当成单品来判断
                current = orderMapper.selectArticleCount(orderItem.getArticleId());
                result = current >= orderItem.getCount();
                msg = current == 0 ? orderItem.getArticleName() + "已售罄,请取消订单后重新下单" :
                        current >= orderItem.getCount() ? "库存足够" : orderItem.getArticleName() + "库存不足,请重新选购餐品";
                break;
            case OrderItemType.UNIT_NEW:
                //如果是单品无规格，直接判断菜品是否有库存
                current = orderMapper.selectArticleCount(orderItem.getArticleId());
                result = current >= count;
                msg = current == 0 ? orderItem.getArticleName() + "已售罄,请取消订单后重新下单" :
                        current >= count ? "库存足够" : orderItem.getArticleName() + "中单品库存不足,最大购买" + current + "个,请重新选购餐品";
                break;
            case OrderItemType.RECOMMEND:
                //如果是单品无规格，直接判断菜品是否有库存
                current = orderMapper.selectArticleCount(orderItem.getArticleId());
                result = current >= count;
                msg = current == 0 ? orderItem.getArticleName() + "已售罄,请取消订单后重新下单" :
                        current >= count ? "库存足够" : orderItem.getArticleName() + "中单品库存不足,最大购买" + current + "个,请重新选购餐品";
                break;
            default:
                log.debug("未知菜品分类");
                break;
        }
        return new Result(msg, result);
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
                    orderMapper.updateArticleStock(orderItem.getArticleId(), StockType.STOCK_MINUS, orderItem.getCount());
                    orderMapper.setEmpty(orderItem.getArticleId());
                    break;
                case OrderItemType.UNITPRICE:
                    //如果是有规格的单品信息，那么更新该规格的单品库存以及该单品的库存
                    ArticlePrice articlePrice = articlePriceMapper.selectByPrimaryKey(orderItem.getArticleId());
                    orderMapper.updateArticleStock(articlePrice.getArticleId(), StockType.STOCK_MINUS, orderItem.getCount());
                    orderMapper.updateArticlePriceStock(orderItem.getArticleId(), StockType.STOCK_MINUS, orderItem.getCount());
                    orderMapper.setEmpty(articlePrice.getArticleId());
                    orderMapper.setArticlePriceEmpty(articlePrice.getArticleId());
                    break;
                case OrderItemType.SETMEALS:
                    orderMapper.updateArticleStock(orderItem.getArticleId(), StockType.STOCK_MINUS, orderItem.getCount());
                    orderMapper.setEmpty(orderItem.getArticleId());
                    //如果是套餐，那么更新套餐库存
                    break;
                case OrderItemType.MEALS_CHILDREN:
                    //如果是套餐子项，那么更新子项库存
                    orderMapper.updateArticleStock(orderItem.getArticleId(), StockType.STOCK_MINUS, orderItem.getCount());
                    orderMapper.setEmpty(orderItem.getArticleId());
                    break;
                case OrderItemType.UNIT_NEW:
                    //如果是没有规格的单品信息,那么更新该单品的库存
                    orderMapper.updateArticleStock(orderItem.getArticleId(), StockType.STOCK_MINUS, orderItem.getCount());
                    orderMapper.setEmpty(orderItem.getArticleId());
                    break;
                case OrderItemType.RECOMMEND:
                    //如果是没有规格的单品信息,那么更新该单品的库存
                    orderMapper.updateArticleStock(orderItem.getArticleId(), StockType.STOCK_MINUS, orderItem.getCount());
                    orderMapper.setEmpty(orderItem.getArticleId());
                    break;
                default:
                    throw new AppException(AppException.UNSUPPORT_ITEM_TYPE, "不支持的餐品类型:" + orderItem.getType());
            }

        }
        //同时更新套餐库存(套餐库存为 最小库存的单品)
        orderMapper.setStockBySuit(order.getShopDetailId());
        return true;
    }


    @Override
    public Boolean addStock(Order order) {
        //首先验证订单信息
        if (order == null || CollectionUtils.isEmpty(order.getOrderItems())) {
            //throw new AppException(AppException.ORDER_IS_NULL);
            return false;
        }
        //遍历订单商品
        for (OrderItem orderItem : order.getOrderItems()) {
            switch (orderItem.getType()) {
                case OrderItemType.ARTICLE:
                    //如果是没有规格的单品信息,那么更新该单品的库存
                    orderMapper.updateArticleStock(orderItem.getArticleId(), StockType.STOCK_ADD, orderItem.getCount());
                    orderMapper.setEmptyFail(orderItem.getArticleId());

                    break;
                case OrderItemType.UNITPRICE:
                    //如果是有规格的单品信息，那么更新该规格的单品库存以及该单品的库存
                    ArticlePrice articlePrice = articlePriceMapper.selectByPrimaryKey(orderItem.getArticleId());
                    orderMapper.updateArticleStock(articlePrice.getArticleId(), StockType.STOCK_ADD, orderItem.getCount());
                    orderMapper.updateArticlePriceStock(orderItem.getArticleId(), StockType.STOCK_ADD, orderItem.getCount());
                    orderMapper.setEmptyFail(articlePrice.getArticleId());
                    orderMapper.setArticlePriceEmptyFail(articlePrice.getArticleId());
                    break;
                case OrderItemType.SETMEALS:
                    orderMapper.updateArticleStock(orderItem.getArticleId(), StockType.STOCK_ADD, orderItem.getCount());
                    orderMapper.setEmptyFail(orderItem.getArticleId());
                    //如果是套餐，那么更新套餐库存

                    break;
                case OrderItemType.MEALS_CHILDREN:
                    //如果是套餐子项，那么更新子项库存
                    orderMapper.updateArticleStock(orderItem.getArticleId(), StockType.STOCK_ADD, orderItem.getCount());
                    orderMapper.setEmptyFail(orderItem.getArticleId());

                    break;
                case OrderItemType.UNIT_NEW:
                    //如果是没有规格的单品信息,那么更新该单品的库存
                    orderMapper.updateArticleStock(orderItem.getArticleId(), StockType.STOCK_ADD, orderItem.getCount());
                    orderMapper.setEmptyFail(orderItem.getArticleId());

                    break;
                default:
                    //  throw new AppException(AppException.UNSUPPORT_ITEM_TYPE, "不支持的餐品类型:" + orderItem.getType());
                    return false;
            }

        }

        return true;
    }

    @Override
    public List<Order> selectByOrderSatesAndProductionStates(String shopId, String[] orderStates,
                                                             String[] productionStates) {
        ShopDetail shopDetail = shopDetailService.selectByPrimaryKey(shopId);
        if (shopDetail.getShopMode() == ShopMode.HOUFU_ORDER) {
            return orderMapper.listHoufuUnFinishedOrder(shopId);

        } else if (shopDetail.getShopMode() == ShopMode.BOSS_ORDER) {
            return orderMapper.selectOrderByBoss(shopId);
        } else {
            return orderMapper.selectByOrderSatesAndProductionStates(shopId, orderStates, productionStates);
        }


    }


    @Override
    public Order payOrderModeFive(String orderId) {
        BigDecimal totalMoney = BigDecimal.ZERO;//计算订单原价，不使用任何优惠方式
        Order order = orderMapper.selectByPrimaryKey(orderId);
        totalMoney = totalMoney.add(order.getOriginalAmount()).setScale(2, BigDecimal.ROUND_HALF_UP);
//        totalMoney = totalMoney.add(order.getServicePrice() == null ? new BigDecimal(0): order.getServicePrice()).setScale(2, BigDecimal.ROUND_HALF_UP);

        if (order.getOrderState() < OrderState.PAYMENT) {
            order.setOrderState(OrderState.PAYMENT);
            order.setAllowCancel(false);
            order.setAllowContinueOrder(false);
            update(order);
        }

        List<Order> orders = orderMapper.selectByParentId(order.getId());
        for (Order child : orders) {
            if (child.getOrderState() < OrderState.PAYMENT) {
                child.setOrderState(OrderState.PAYMENT);
                child.setAllowCancel(false);
                child.setAllowContinueOrder(false);
                update(child);
                //插入 支付项
//                insertOrderPaymentItem(child, child.getOriginalAmount());
                //计算 订单总额
                totalMoney = totalMoney.add(child.getOriginalAmount()).setScale(2, BigDecimal.ROUND_HALF_UP);
            }
        }

        //设置  【主订单 】 支付方式     子订单为计算
        insertOrderPaymentItem(order, totalMoney);

        return order;
    }

    @Override
    public Order payOrderWXModeFive(String orderId) {
        Order order = orderMapper.selectByPrimaryKey(orderId);

        if (order.getOrderState() < OrderState.PAYMENT) {
            order.setOrderState(OrderState.PAYMENT);
            order.setAllowCancel(false);
            order.setIsPay(OrderPayState.PAYED);
            order.setAllowContinueOrder(false);
            update(order);
        }

        List<Order> orders = orderMapper.selectByParentId(order.getId());
        for (Order child : orders) {
            if (child.getOrderState() < OrderState.PAYMENT) {
                child.setOrderState(OrderState.PAYMENT);
                child.setIsPay(OrderPayState.PAYED);
                child.setAllowCancel(false);
                child.setAllowContinueOrder(false);
                update(child);

            }
        }


        return order;
    }

    public void insertOrderPaymentItem(Order order, BigDecimal totalMoney) {
        OrderPaymentItem item = new OrderPaymentItem();
        item.setId(ApplicationUtils.randomUUID());
        item.setOrderId(order.getId());
        item.setPaymentModeId(PayMode.MONEY_PAY);
        item.setPayTime(order.getCreateTime());
        item.setPayValue(totalMoney);
        item.setRemark("商家在POS端使用其他支付方式确认订单:" + item.getPayValue());
        item.setResultData("其他支付方式");
        orderPaymentItemService.insert(item);
        payContent(order.getId());

    }


    public void payContent(String orderId) {
        Order order = selectById(orderId);
        if (order != null && order.getOrderMode() == ShopMode.HOUFU_ORDER && order.getOrderState() == OrderState.PAYMENT
                && order.getProductionStatus() == ProductionStatus.PRINTED) {
            Customer customer = customerService.selectById(order.getCustomerId());
            WechatConfig config = wechatConfigService.selectByBrandId(customer.getBrandId());
            List<OrderPaymentItem> paymentItems = orderPaymentItemService.selectByOrderId(order.getId());
            String money = "(";
            for (OrderPaymentItem orderPaymentItem : paymentItems) {
                money += PayMode.getPayModeName(orderPaymentItem.getPaymentModeId()) + "： " + orderPaymentItem.getPayValue() + " ";
            }
            StringBuffer msg = new StringBuffer();
            BigDecimal sum = order.getOrderMoney();
            List<Order> orders = selectByParentId(order.getId()); //得到子订单
            for (Order child : orders) { //遍历子订单
                sum = sum.add(child.getOrderMoney());
            }
            BrandSetting setting = brandSettingService.selectByBrandId(order.getBrandId());
//            if(setting.getIsUseServicePrice() == 1){
//                sum = sum.add(order.getServicePrice());
//            }
            msg.append("您的订单").append(order.getSerialNumber()).append("已于").append(DateFormatUtils.format(paymentItems.get(0).getPayTime(), "yyyy-MM-dd HH:mm"));
            msg.append("支付成功。订单金额：").append(sum).append(money).append(") ");
            String result = WeChatUtils.sendCustomerMsg(msg.toString(), customer.getWechatId(), config.getAppid(), config.getAppsecret());
        }
    }

    @Override
    public Order payPrice(BigDecimal factMoney, String orderId) {
        //拿到订单
        Order order = orderMapper.selectByPrimaryKey(orderId);

        Customer customer = customerService.selectById(order.getCustomerId());

        if (order.getOrderState() < OrderState.PAYMENT) {
            accountService.payOrder(order, factMoney, customer);
            order.setOrderState(OrderState.PAYMENT);
            order.setAllowCancel(false);
            order.setPaymentAmount(new BigDecimal(0));
            order.setAllowContinueOrder(false);
            update(order);
            List<Order> orders = orderMapper.selectByParentId(order.getId());
            for (Order child : orders) {
                if (child.getOrderState() < OrderState.PAYMENT) {
                    child.setOrderState(OrderState.PAYMENT);
                    child.setAllowCancel(false);
                    child.setAllowContinueOrder(false);
                    update(child);
                }
            }

        }

        return order;

    }

    @Override
    public void useRedPrice(BigDecimal factMoney, String orderId) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        Customer customer = customerService.selectById(order.getCustomerId());
        accountService.payOrder(order, factMoney, customer);
    }

    @Override
    public void cleanShopOrder(ShopDetail shopDetail, OffLineOrder offLineOrder) {


        String[] orderStates = new String[]{OrderState.SUBMIT + "", OrderState.PAYMENT + ""};//未付款和未全部付款和已付款
        String[] productionStates = new String[]{ProductionStatus.NOT_ORDER + ""};//已付款未下单
        List<Order> orderList = orderMapper.selectByOrderSatesAndProductionStates(shopDetail.getId(), orderStates, productionStates);
        for (Order order : orderList) {
            if (!order.getClosed()) {//判断订单是否已被关闭，只对未被关闭的订单做退单处理
                sendWxRefundMsg(order);
            }
        }
        //查询已付款且有支付项但是生产状态没有改变的订单
        List<Order> orderstates = orderMapper.selectHasPayNoChangeStatus(shopDetail.getId(), DateUtil.getDateBegin(new Date()), DateUtil.getDateEnd(new Date()));
        if (!orderstates.isEmpty()) {
            for (Order o : orderstates) {
                if (o.getOrderMode() == ShopMode.CALL_NUMBER) {
                    o.setProductionStatus(ProductionStatus.HAS_CALL);
                } else {
                    o.setProductionStatus(ProductionStatus.PRINTED);
                }
                orderMapper.updateByPrimaryKeySelective(o);
            }
        }


        //----1.定义时间---

        //本月的开始时间 本月结束时间
        String beginMonth = DateUtil.getMonthBegin();
        String endMonth = DateUtil.getMonthEnd();
        Date begin = DateUtil.getDateBegin(DateUtil.fomatDate(beginMonth));
        Date end = DateUtil.getDateEnd(DateUtil.fomatDate(endMonth));
        //2定义顾客id集合
        //本日之前出现的所有顾客的Id
        List<String> customerBeforeToday = new ArrayList<>();
        //本月上旬之前出现的顾客id
        List<String> customerBeforeFirstOfMonth = new ArrayList<>();
        //本月中旬之前出现的顾客id
        List<String> customerBeforeMiddleOfMonth = new ArrayList<>();
        //本月下旬之前出现的顾客id
        List<String> customerBeforeLastOfMonth = new ArrayList<>();
        //本月之前出现的顾客id
        List<String> customerBeforeMonth = new ArrayList<>();

        //本日出现的顾客id(所有)
        Set<String> customerInToday = new HashSet<>();
        //本月上旬中出现的顾客id(所有)
        Set<String> customerInFirstOfMonth = new HashSet<>();
        //本月中旬中出现的顾客id(所有)
        Set<String> customerInMiddleOfMonth = new HashSet<>();
        //本月下旬中出现的顾客id(所有)
        Set<String> customerInLastOfMonth = new HashSet<>();
        //本月中出现的顾客Id(所有)
        Set<String> customerInMonth = new HashSet<>();

        //本日分享注册的顾客id
        List<String> customerShareInToday = new ArrayList<>();
        //本月上旬分享注册的顾客id
        List<String> customerShareInFirstOfMonth = new ArrayList<>();
        //本月中旬出现的分享注册顾客Id
        List<String> customerShareInMiddleOfMonth = new ArrayList<>();
        //本月下旬出现的分享注册顾客id
        List<String> customerShareInLastOfMonth = new ArrayList<>();
        //本月出现的分享注册顾客id
        List<String> customerShareInMonth = new ArrayList<>();

        //本日回头用id
        List<String> backCustomerToday = new ArrayList<>();//直接用本日之前出现的 和 本日出现的交集
        //本月上旬回头用id
        List<String> backCustomerFirstOfMonth = new ArrayList<>();
        //本月中旬回头用id
        List<String> backCustomerMiddleOfMonth = new ArrayList<>();
        //本月下旬回头用id
        List<String> backCustomerLastOfMonth = new ArrayList<>();
        //本月回头用户id
        List<String> backCustomerMonth = new ArrayList<>();

        //本日新增用户个数
        Set<String> todayNewCutomer = new HashSet<>();
        //上旬新增用户个数
        Set<String> firstOfMonthNewCustomer = new HashSet<>();
        //中旬新增用户个数
        Set<String> middleOfMonthNewCustomer = new HashSet<>();
        //下旬新增用户个数
        Set<String> lastOfMonthNewCustomer = new HashSet<>();
        //本月新增用户个数
        Set<String> monthNewCustomer = new HashSet<>();

        //本日新增自然用户个数
        Set<String> todayNormalNewCustomer = new HashSet<>();//注意如果当order中的customerId被删除了 那么无法判断是分享注册还是自然注册 这是用总的新增-分享用户
        //上旬新增自然用户个数
        Set<String> firstOfMonthNormalCustomer = new HashSet<>();//上旬总新增-上旬新增(分享)
        //中旬新增自然用户个数
        Set<String> middleOfMonthNormalCustomer = new HashSet<>();
        //下旬新增自然用户个数
        Set<String> lastOfMonthNormalCustomer = new HashSet<>();
        //本月新增自然用户个数
        Set<String> monthNormalCustomer = new HashSet<>();

        //本日新增分享用户个数
        Set<String> todayShareNewCutomer = new HashSet<>();
        //上旬新增分享用户个数
        Set<String> firstOfMonthShareCustomer = new HashSet<>();
        //中旬新增分享用户的个数
        Set<String> middleOfMonthShareCustomer = new HashSet<>();
        //下旬新增分享用户的个数
        Set<String> lastOfMonthShareCustomer = new HashSet<>();
        //本月新增分享用户个数
        Set<String> monthShareCustomer = new HashSet<>();

        //本日回头用户个数(包括二次回头和多次回头)
        Set<String> todayBackCustomer = new HashSet<>();
        //上旬新增回头用户个数
        Set<String> firstOfMonthBackCustomer = new HashSet<>();
        //中旬新增回头用户个数
        Set<String> middleOfMonthBackCustomer = new HashSet<>();
        //下旬新增回头用户个数
        Set<String> lastOfMonthBackCustomer = new HashSet<>();
        //本月新增回头用户个数
        Set<String> monthBackCustomer = new HashSet<>();

        //本日二次回头用户个数
        Set<String> todayBackTwoCustomer = new HashSet<>();
        //上旬二次回头用户个数
        Set<String> firstOfMonthBackTwoCustomer = new HashSet<>();
        //中旬二次回头用户个数
        Set<String> middleOfMonthBackTwoCustomer = new HashSet<>();
        //下旬二次回头用户个数
        Set<String> lastOfMonthBackTwoCustomer = new HashSet<>();
        //本月二次回头用户个数
        Set<String> monthBackTwoCustomer = new HashSet<>();

        //本日多次回头用户个数
        Set<String> todayBackTwoMoreCustomer = new HashSet<>();
        //上旬多次回头用户个数
        Set<String> firstOfMonthBackTwoMoreCustomer = new HashSet<>();
        //中旬多次回头用户个数
        Set<String> middleOfMonthBackTwoMoreCustomer = new HashSet<>();
        //下旬多次回头用户个数
        Set<String> lastOfMonthBackTwoMoreCustomer = new HashSet<>();
        //本月多次回头用户个数
        Set<String> monthBackTwoMoreCustomer = new HashSet<>();

        //历史用户数
        Set<String> customerBeforeTodayEnd = new HashSet<>();

        //3定义满意度
        //本日满意度
        String todaySatisfaction = "";
        //上旬满意度
        String firstOfMonthSatisfaction = "";
        //中旬满意度
        String middleOfMonthSatisfaction = "";
        //下旬满意度
        String lastOfMonthSatisfaction = "";
        //本月满意度
        String monthSatisfaction = "";

        //4定义支付
        //本日resto订单总额
        BigDecimal todayRestoTotal = BigDecimal.ZERO;
        //上旬r订单总额
        BigDecimal firstOfMonthRestoTotal = BigDecimal.ZERO;
        //中旬r订单总额
        BigDecimal middleOfMonthRestoTotal = BigDecimal.ZERO;
        //下旬r订单总额
        BigDecimal lastOfMonthRestoTotal = BigDecimal.ZERO;
        //本月r订单总额
        BigDecimal monthRestoTotal = BigDecimal.ZERO;

        //本日线下订单总额
        BigDecimal todayEnterTotal = BigDecimal.ZERO;
        //上旬线下订单总额
        BigDecimal firstOfMonthEnterTotal = BigDecimal.ZERO;
        //中旬线下订单总额
        BigDecimal middleOfMonthEnterTotal = BigDecimal.ZERO;
        //下旬线下订单单总额
        BigDecimal lastOfMonthEnterTotal = BigDecimal.ZERO;
        //本月线下订单总额
        BigDecimal monthEnterTotal = BigDecimal.ZERO;

        //本日resto实收总额
        BigDecimal todayPayRestoTotal = BigDecimal.ZERO;
        //上旬resto实收总额
        BigDecimal firstOfMonthPayRestoTotal = BigDecimal.ZERO;
        //中旬resto实收总额
        BigDecimal middleOfMonthPayRestoTotal = BigDecimal.ZERO;
        //下旬resto实收总额
        BigDecimal lastOfMonthPayRestoTotal = BigDecimal.ZERO;
        //本月resto实收总额
        BigDecimal monthPayRestoTotal = BigDecimal.ZERO;

        //本日新增用户的订单总额
        BigDecimal todayNewCustomerRestoTotal = BigDecimal.ZERO;
        //上旬新增用户的订单总额
        BigDecimal firstOfMonthNewCustomerRestoTotal = BigDecimal.ZERO;
        //中旬新增用户的订单总额
        BigDecimal middleOfMonthNewCustomerRestoTotal = BigDecimal.ZERO;
        //下旬新增用户的订单总额
        BigDecimal lastOfMonthNewCustomerRestoTotal = BigDecimal.ZERO;
        //本月新增用户的订单总额
        BigDecimal monthNewCustomerRestoTotal = BigDecimal.ZERO;

        //本日新增分享用户的订单总额
        BigDecimal todayNewNormalCustomerRestoTotal = BigDecimal.ZERO;
        //上旬新增分享用户的订单总额
        BigDecimal firstOfMonthNewNormalCustomerRestoTotal = BigDecimal.ZERO;
        //中旬新增分享用户的订单总额
        BigDecimal middleOfMonthNewNormalCustomerRestoTotal = BigDecimal.ZERO;
        //下旬新增分享用户的订单总额
        BigDecimal lastOfMonthNewNormalCustomerRestoTotal = BigDecimal.ZERO;
        //本月新增分享用户的订单总额
        BigDecimal monthNewNormalCustomerRestoTotal = BigDecimal.ZERO;

        //本日新增自然用户的订单总额
        BigDecimal todayNewShareCustomerRestoTotal = BigDecimal.ZERO;
        //上旬新增自然用户的订单总额
        BigDecimal firstOfMonthNewShareCustomerRestoTotal = BigDecimal.ZERO;
        //中旬新增自然用户的订单总额
        BigDecimal middleOfMonthNewShareCustomerRestoTotal = BigDecimal.ZERO;
        //下旬新增自然用户的订单总额
        BigDecimal lastOfMonthNewShareCustomerRestoTotal = BigDecimal.ZERO;
        //本月新增自然用户的订单总额
        BigDecimal monthNewShareCustomerRestoTotal = BigDecimal.ZERO;

        //1.本日回头用户的订单总额
        BigDecimal todayBackCustomerRestoTotal = BigDecimal.ZERO;
        //2.上旬回头用户的订单总额
        BigDecimal firstOfMonthBackCustomerRestoTotal = BigDecimal.ZERO;
        //3.中旬回头用户的订单总额
        BigDecimal middleOfMonthBackCustomerRestoTotal = BigDecimal.ZERO;
        // 4.下旬回头用户的订单总额
        BigDecimal lastOfMonthBackCustomerRestoTotal = BigDecimal.ZERO;
        // 5.本月回头用户的订单总额
        BigDecimal monthBackCustomerRestoTotal = BigDecimal.ZERO;

        //1.本日二次回头用户的订单总额
        BigDecimal todayBackTwoCustomerRestoTotal = BigDecimal.ZERO;
        //2.上旬二次回头用户的订单总额
        BigDecimal firstOfMonthBackTwoCustomerRestoTotal = BigDecimal.ZERO;
        //3.中旬二次回头用户的订单总额
        BigDecimal middleOfMonthBackTwoCustomerRestoTotal = BigDecimal.ZERO;
        // 4.下旬二次回头用户的订单总额
        BigDecimal lastOfMonthBackTwoCustomerRestoTotal = BigDecimal.ZERO;
        // 5.本月二次回头用户的订单总额
        BigDecimal monthBackTwoCustomerRestoTotal = BigDecimal.ZERO;

        //1.本日多次回头用户的订单总额
        BigDecimal todayBackTwoMoreCustomerRestoTotal = BigDecimal.ZERO;
        //2.上旬多次回头用户的订单总额
        BigDecimal firstOfMonthBackTwoMoreCustomerRestoTotal = BigDecimal.ZERO;
        //3.中旬多次回头用户的订单总额
        BigDecimal middleOfMonthBackTwoMoreCustomerRestoTotal = BigDecimal.ZERO;
        // 4.下旬多次回头用户的订单总额
        BigDecimal lastOfMonthBackTwoMoreCustomerRestoTotal = BigDecimal.ZERO;
        // 5.本月多次次回头用户的订单总额
        BigDecimal monthBackTwoMoreCustomerRestoTotal = BigDecimal.ZERO;

        //本日resto订单总数
        Set<String> todayRestoCount = new HashSet<>();
        //上旬r订单总数
        Set<String> firstOfMonthRestoCount = new HashSet<>();
        //中询r订单总数
        Set<String> middleOfMonthRestoCount = new HashSet<>();
        //下旬r订单总数
        Set<String> lastOfMonthRestoCount = new HashSet<>();
        //本月r订单总数
        Set<String> monthRestoCount = new HashSet<>();

        //本日线下订单总数
        int todayEnterCount = 0;
        //上旬线下订单总数
        int firstOfMonthEnterCount = 0;
        //中旬线下订单总数
        int middleOfMonthEnterCount = 0;
        //下旬线下订单总数
        int lastOfMonthEnterCount = 0;
        //本月线下订单总数
        int monthEnterCount = 0;

        //查询pos端店铺录入信息
        List<OffLineOrder> offLineOrderList = offLineOrderMapper.selectlistByTimeSourceAndShopId(shopDetail.getId(), begin, end, OfflineOrderSource.OFFLINE_POS);

        if (!offLineOrderList.isEmpty()) {
            for (OffLineOrder of : offLineOrderList) {
                List<Integer> getTime = getDay(of.getCreateTime());
                if (getTime.contains(2)) {//本日中
                    todayEnterCount += of.getEnterCount();
                    todayEnterTotal = todayEnterTotal.add(of.getEnterTotal());
                }

                if (getTime.contains(4)) {//上旬中
                    firstOfMonthEnterCount += of.getEnterCount();
                    firstOfMonthEnterTotal = firstOfMonthEnterTotal.add(of.getEnterTotal());
                }

                if (getTime.contains(6)) {//中旬中
                    middleOfMonthEnterCount += of.getEnterCount();
                    middleOfMonthEnterTotal = middleOfMonthEnterTotal.add(of.getEnterTotal());
                }

                if (getTime.contains(8)) {//下旬中
                    lastOfMonthEnterCount += of.getEnterCount();
                    lastOfMonthEnterTotal = lastOfMonthRestoTotal.add(of.getEnterTotal());
                }

                if (getTime.contains(10)) {//本月中
                    monthEnterCount += of.getEnterCount();
                    monthEnterTotal = monthEnterTotal.add(of.getEnterTotal());
                }
            }
        }

        //查询历史订单和人 目前是以店铺为基础 也就是 指用户在另一个店铺就餐，在当前店铺还是算第一次用户
        List<Order> orderHistoryList = orderMapper.selectOrderHistoryList(shopDetail.getId(), DateUtil.getDateEnd(new Date()));

        //yz 查询本月所有的已消费订单
        List<Order> orders = orderMapper.selectListsmsByShopId(begin, end, shopDetail.getId());
        if (!orders.isEmpty()) {
            for (Order order : orderHistoryList) {
                List<Integer> getTime = getDay(order.getCreateTime());
                if (getTime.contains(1)) {
                    customerBeforeToday.add(order.getCustomerId());
                }
                if (getTime.contains(2)) {
                    customerInToday.add(order.getCustomerId());
                    if (order.getCustomer() != null && !StringUtils.isEmpty(order.getCustomer().getShareCustomer())) {
                        customerShareInToday.add(order.getCustomerId());
                    }
                }
                if (getTime.contains(3)) {
                    customerBeforeFirstOfMonth.add(order.getCustomerId());
                }
                if (getTime.contains(4)) {
                    customerInFirstOfMonth.add(order.getCustomerId());
                    if (order.getCustomer() != null && order.getCustomer().getShareCustomer() != null) {
                        customerShareInFirstOfMonth.add(order.getCustomerId());
                    }
                }
                if (getTime.contains(5)) {
                    customerBeforeMiddleOfMonth.add(order.getCustomerId());
                }
                if (getTime.contains(6)) {
                    customerInMiddleOfMonth.add(order.getCustomerId());
                    if (order.getCustomer() != null && order.getCustomer().getShareCustomer() != null) {
                        customerShareInMiddleOfMonth.add(order.getCustomerId());
                    }
                }
                if (getTime.contains(7)) {
                    customerBeforeLastOfMonth.add(order.getCustomerId());
                }
                if (getTime.contains(8)) {
                    customerInLastOfMonth.add(order.getCustomerId());
                    if (order.getCustomer() != null && order.getCustomer().getShareCustomer() != null) {
                        customerShareInLastOfMonth.add(order.getCustomerId());
                    }
                }
                if (getTime.contains(9)) {
                    customerBeforeMonth.add(order.getCustomerId());
                }
                if (getTime.contains(10)) {
                    customerInMonth.add(order.getCustomerId());
                    if (order.getCustomer() != null && order.getCustomer().getShareCustomer() != null) {
                        customerShareInMonth.add(order.getCustomerId());
                    }
                }
                //历史用户数
                customerBeforeTodayEnd.add(order.getId());
            }

        }


        //计算 新增 (分享注册+自然注册)
        //今日--

        if (!customerInToday.isEmpty()) {
            for (String s1 : customerInToday) {  //今日有 但是今日之前没有的
                if (!customerBeforeToday.contains(s1)) {
                    todayNewCutomer.add(s1);
                }
            }
        }

        if (!customerShareInToday.isEmpty()) {
            for (String s1 : customerShareInToday) {//今日有且是分享注册 但是今日之前没有
                if (!customerBeforeToday.contains(s1)) {
                    todayShareNewCutomer.add(s1);
                }
            }
        }

        if (!todayNewCutomer.isEmpty()) {
            for (String s1 : todayNewCutomer) {
                if (!todayShareNewCutomer.contains(s1)) {
                    todayNormalNewCustomer.add(s1);
                }
            }
        }

        //上旬---
        if (!customerInFirstOfMonth.isEmpty()) {
            for (String s1 : customerInFirstOfMonth) {  //上旬有 但是上旬之前没有的
                if (!customerBeforeFirstOfMonth.contains(s1)) {
                    firstOfMonthNewCustomer.add(s1);
                }
            }
        }

        if (!customerShareInFirstOfMonth.isEmpty()) {
            for (String s1 : customerShareInFirstOfMonth) {//上旬有且是分享注册 但是上旬之前没有
                if (!customerBeforeFirstOfMonth.contains(s1)) {
                    firstOfMonthShareCustomer.add(s1);
                }
            }
        }

        if (!firstOfMonthNewCustomer.isEmpty()) {
            for (String s1 : firstOfMonthNewCustomer) {
                if (!firstOfMonthShareCustomer.contains(s1)) {
                    firstOfMonthNormalCustomer.add(s1);
                }
            }
        }

        //中旬---
        if (!customerInMiddleOfMonth.isEmpty()) {
            for (String s1 : customerInMiddleOfMonth) {  //中旬有 但是中旬之前没有的
                if (!customerBeforeMiddleOfMonth.contains(s1)) {
                    middleOfMonthNewCustomer.add(s1);
                }
            }
        }

        if (!customerShareInMiddleOfMonth.isEmpty()) {
            for (String s1 : customerShareInMiddleOfMonth) {//中旬有且是分享注册 但是中旬之前没有
                if (!customerBeforeMiddleOfMonth.contains(s1)) {
                    middleOfMonthShareCustomer.add(s1);
                }
            }
        }

        if (!middleOfMonthNewCustomer.isEmpty()) {
            for (String s1 : middleOfMonthNewCustomer) {
                if (!middleOfMonthShareCustomer.contains(s1)) {
                    middleOfMonthNormalCustomer.add(s1);
                }
            }
        }

        if (!customerInLastOfMonth.isEmpty()) {
            //下旬---
            for (String s1 : customerInLastOfMonth) {  //下旬有 但是下旬之前没有的
                if (!customerBeforeLastOfMonth.contains(s1)) {
                    lastOfMonthNewCustomer.add(s1);
                }
            }
        }

        for (String s1 : customerShareInLastOfMonth) {//下旬有且是分享注册 但是下旬之前没有
            if (!customerBeforeLastOfMonth.contains(s1)) {
                lastOfMonthShareCustomer.add(s1);
            }
        }

        if (!lastOfMonthNewCustomer.isEmpty()) {
            for (String s1 : lastOfMonthNewCustomer) {
                if (!lastOfMonthShareCustomer.contains(s1)) {
                    lastOfMonthNormalCustomer.add(s1);
                }
            }
        }

        //本月---

        if (!customerInMonth.isEmpty()) {
            for (String s1 : customerInMonth) {  //本月有 但是本月之前没有的
                if (!customerBeforeMonth.contains(s1)) {
                    monthNewCustomer.add(s1);
                }
            }
        }

        if (!customerShareInMonth.isEmpty()) {
            for (String s1 : customerShareInMonth) {//本月有且是分享注册 但是本月之前没有
                if (!customerBeforeMonth.contains(s1)) {
                    monthShareCustomer.add(s1);
                }
            }
        }


        if (!monthNewCustomer.isEmpty()) {
            for (String s1 : monthNewCustomer) {
                if (!monthShareCustomer.contains(s1)) {
                    monthNormalCustomer.add(s1);
                }
            }
        }


        //计算回头用户
        //今日回头
        if (!customerBeforeToday.isEmpty()) {
            for (String s1 : customerBeforeToday) {//以前有 今日也有
                if (customerInToday.contains(s1)) {
                    todayBackCustomer.add(s1);//去重 直接用长度来代替今日回头用户的总数
                    backCustomerToday.add(s1);//不去重
                }
            }
        }

        //定义一个map 来存放当日每个回头用户存在的次数
        Map<String, Integer> todayBackCount = new HashMap();
        if (!backCustomerToday.isEmpty()) {
            for (String s : backCustomerToday) {
                Integer i = todayBackCount.get(s);
                if (i == null) {
                    todayBackCount.put(s, 1);
                } else {
                    todayBackCount.put(s, i + 1);
                }
            }
        }
        if (!todayBackCount.isEmpty()) {
            for (String key : todayBackCount.keySet()) {//求算本日多次回头用户
                if (todayBackCount.get(key) == 1) {
                    todayBackTwoCustomer.add(key);
                } else {
                    todayBackTwoMoreCustomer.add(key);
                }
            }
        }

        //上旬回头
        if (!customerBeforeFirstOfMonth.isEmpty()) {
            for (String s1 : customerBeforeFirstOfMonth) {//上旬有 今日也有
                if (customerInFirstOfMonth.contains(s1)) {
                    firstOfMonthBackCustomer.add(s1);//去重 直接用长度来代替上旬回头用户的总数
                    backCustomerFirstOfMonth.add(s1);//不去重
                }
            }
        }

        //定义一个map 来存放上旬每个回头用户存在的次数
        Map<String, Integer> firstOfMonthBackCount = new HashMap();
        if (!backCustomerFirstOfMonth.isEmpty()) {
            for (String s : backCustomerFirstOfMonth) {
                Integer i = firstOfMonthBackCount.get(s);
                if (i == null) {
                    firstOfMonthBackCount.put(s, 1);
                } else {
                    firstOfMonthBackCount.put(s, i + 1);
                }
            }
        }

        if (!firstOfMonthBackCount.isEmpty()) {
            for (String key : firstOfMonthBackCount.keySet()) {//求算上旬
                // 多次回头用户
                if (firstOfMonthBackCount.get(key) == 1) {
                    firstOfMonthBackTwoCustomer.add(key);
                } else if (firstOfMonthBackCount.get(key) > 1) {
                    firstOfMonthBackTwoMoreCustomer.add(key);
                }
            }
        }

        //中旬回头
        if (!customerBeforeMiddleOfMonth.isEmpty()) {
            for (String s1 : customerBeforeMiddleOfMonth) {//中旬有 今日也有
                if (customerInMiddleOfMonth.contains(s1)) {
                    middleOfMonthBackCustomer.add(s1);//去重 直接用长度来代替上旬回头用户的总数
                    backCustomerMiddleOfMonth.add(s1);//不去重
                }
            }
        }

        //定义一个map 来存放中旬每个回头用户存在的次数
        Map<String, Integer> middleOfMonthBackCount = new HashMap();
        if (!backCustomerMiddleOfMonth.isEmpty()) {
            for (String s : backCustomerMiddleOfMonth) {
                Integer i = middleOfMonthBackCount.get(s);
                if (i == null) {
                    middleOfMonthBackCount.put(s, 1);
                } else {
                    middleOfMonthBackCount.put(s, i + 1);
                }
            }
        }

        if (!middleOfMonthBackCount.isEmpty()) {
            for (String key : middleOfMonthBackCount.keySet()) {//求算中旬
                // 多次回头用户
                if (middleOfMonthBackCount.get(key) == 1) {
                    middleOfMonthBackTwoCustomer.add(key);
                } else if (middleOfMonthBackCount.get(key) > 1) {
                    middleOfMonthBackTwoMoreCustomer.add(key);
                }
            }
        }


        //下旬回头
        if (!customerBeforeLastOfMonth.isEmpty()) {
            for (String s1 : customerBeforeLastOfMonth) {//下旬有 下旬之前也有
                if (customerInLastOfMonth.contains(s1)) {
                    lastOfMonthBackCustomer.add(s1);//去重 直接用长度来代替下旬回头用户的总数
                    backCustomerLastOfMonth.add(s1);//不去重
                }
            }
        }

        //定义一个map 来存放下旬每个回头用户存在的次数
        Map<String, Integer> LastOfMonthBackCount = new HashMap();
        if (!backCustomerLastOfMonth.isEmpty()) {
            for (String s : backCustomerLastOfMonth) {
                Integer i = LastOfMonthBackCount.get(s);
                if (i == null) {
                    LastOfMonthBackCount.put(s, 1);
                } else {
                    LastOfMonthBackCount.put(s, i + 1);
                }
            }
        }

        if (!LastOfMonthBackCount.isEmpty()) {
            for (String key : LastOfMonthBackCount.keySet()) {//求算下旬
                // 多次回头用户
                if (LastOfMonthBackCount.get(key) == 1) {
                    lastOfMonthBackTwoCustomer.add(key);
                } else if (LastOfMonthBackCount.get(key) > 1) {
                    lastOfMonthBackTwoMoreCustomer.add(key);
                }
            }
        }

        //本月回头

        if (!customerBeforeMonth.isEmpty()) {
            for (String s1 : customerBeforeMonth) {//本月有 本月之前也有
                if (customerInMonth.contains(s1)) {
                    monthBackCustomer.add(s1);//去重 直接用长度来代替本月回头用户的总数
                    backCustomerMonth.add(s1);//不去重
                }
            }
        }

        //定义一个map 来存放本月每个回头用户存在的次数
        Map<String, Integer> monthBackCount = new HashMap();
        if (!backCustomerMonth.isEmpty()) {
            for (String s : backCustomerMonth) {
                Integer i = monthBackCount.get(s);
                if (i == null) {
                    monthBackCount.put(s, 1);
                } else {
                    monthBackCount.put(s, i + 1);
                }
            }
        }

        if (!monthBackCount.isEmpty()) {
            for (String key : monthBackCount.keySet()) {//求算本月
                // 多次回头用户
                if (monthBackCount.get(key) == 1) {
                    monthBackTwoCustomer.add(key);
                } else if (monthBackCount.get(key) > 1) {
                    monthBackTwoMoreCustomer.add(key);
                }
            }
        }

        int dayAppraiseNum = 0;//当日评价的总单数
        int firstOfMonthAppraiseNum = 0;//上旬评价的总单数
        int middleOfMonthAppraiseSum = 0;//中旬评价的单数
        int lastOfMonthAppraiseSum = 0;//下旬评价的单数
        int monthAppraiseSum = 0;//本月评价的单数

        int dayAppraiseSum = 0;//当日所有评价的总分数
        int firstOfMonthAppraiseSum = 0;//上旬所有评价的总分数
        int middleOfMonthAppraiseNum = 0;//中旬所有评价的总分数
        int lastOfMonthAppraiseNum = 0;//下旬所有评价的总分数
        int monthAppraiseNum = 0;//本月所有评价的总分数


        if (!orders.isEmpty()) {
            for (Order o : orders) {
                //封装   1.resto订单总额    2.满意度  3.resto订单总数  4订单中的实收总额  5新增用户的订单总额  6自然到店的用户总额  7分享到店的用户总额
                //8回头用户的订单总额  9二次回头用户的订单总额  10多次回头用户的订单总额
                //本日 begin-----------------------
                if (getDay(o.getCreateTime()).contains(2)) {
                    //1.resto订单总额
                    if (o.getOrderMode() == ShopMode.HOUFU_ORDER && o.getAmountWithChildren().compareTo(BigDecimal.ZERO) != 0) {
                        todayRestoTotal = todayRestoTotal.add(o.getAmountWithChildren());
                    } else {
                        todayRestoTotal = todayRestoTotal.add(o.getOrderMoney());
                    }
                    //2.满意度
                    if (null != o.getAppraise()) {
                        if (o.getAppraise().getLevel() != null) {
                            dayAppraiseNum++;
                            dayAppraiseSum += o.getAppraise().getLevel() * 20;
                        }
                    }
                    todaySatisfaction = String.valueOf(dayAppraiseNum != 0 ? dayAppraiseSum / dayAppraiseNum : "");
                    //3.resto的订单总数
                    if (o.getParentOrderId() == null) {
                        todayRestoCount.add(o.getId());
                    }
                    //4.订单中实收总额
                    if (o.getOrderPaymentItems() != null) {
                        for (OrderPaymentItem oi : o.getOrderPaymentItems()) {
                            if (oi.getPaymentModeId() == PayMode.WEIXIN_PAY || oi.getPaymentModeId() == PayMode.ALI_PAY || oi.getPaymentModeId() == PayMode.MONEY_PAY || oi.getPaymentModeId() == PayMode.ARTICLE_BACK_PAY) {
                                todayPayRestoTotal = todayRestoTotal.add(oi.getPayValue());
                            }
                        }
                    }
                    //5.新增用户的订单总额
                    if (todayNewCutomer.contains(o.getCustomerId())) {
                        if (o.getOrderMode() == 5 && o.getAmountWithChildren().compareTo(BigDecimal.ZERO) != 0) {
                            todayNewCustomerRestoTotal = todayNewCustomerRestoTotal.add(o.getAmountWithChildren());
                        } else {
                            todayNewCustomerRestoTotal = todayNewCustomerRestoTotal.add(o.getOrderMoney());
                        }
                    }
                    //6自然到店的用户订单总额
                    if (todayNormalNewCustomer.contains(o.getCustomerId())) {
                        if (o.getOrderMode() == 5 && o.getAmountWithChildren().compareTo(BigDecimal.ZERO) != 0) {
                            todayNewNormalCustomerRestoTotal = todayNewNormalCustomerRestoTotal.add(o.getAmountWithChildren());
                        } else {
                            todayNewNormalCustomerRestoTotal = todayNewNormalCustomerRestoTotal.add(o.getOrderMoney());
                        }
                    }
                    //7.分享到店的用户订单总额
                    if (todayShareNewCutomer.contains(o.getCustomerId())) {
                        if (o.getOrderMode() == 5 && o.getAmountWithChildren().compareTo(BigDecimal.ZERO) != 0) {
                            todayNewShareCustomerRestoTotal = todayNewShareCustomerRestoTotal.add(o.getAmountWithChildren());
                        } else {
                            todayNewShareCustomerRestoTotal = todayNewShareCustomerRestoTotal.add(o.getOrderMoney());
                        }
                    }

                    //8.回头用户的订单总额
                    if (todayBackCustomer.contains(o.getCustomerId())) {
                        if (o.getOrderMode() == 5 && o.getAmountWithChildren().compareTo(BigDecimal.ZERO) != 0) {
                            todayBackCustomerRestoTotal = todayBackCustomerRestoTotal.add(o.getAmountWithChildren());
                        } else {
                            todayBackCustomerRestoTotal = todayBackCustomerRestoTotal.add(o.getOrderMoney());
                        }
                    }

                    //9.二次回头用户的订单总额
                    if (todayBackTwoCustomer.contains(o.getCustomerId())) {
                        if (o.getOrderMode() == 5 && o.getAmountWithChildren().compareTo(BigDecimal.ZERO) != 0) {
                            todayBackTwoCustomerRestoTotal = todayBackTwoCustomerRestoTotal.add(o.getAmountWithChildren());
                        } else {
                            todayBackTwoCustomerRestoTotal = todayBackTwoCustomerRestoTotal.add(o.getOrderMoney());
                        }
                    }

                    //10.多次回头用户的订单总额
                    if (todayBackTwoMoreCustomer.contains(o.getCustomerId())) {
                        if (o.getOrderMode() == 5 && o.getAmountWithChildren().compareTo(BigDecimal.ZERO) != 0) {
                            todayBackTwoMoreCustomerRestoTotal = todayBackTwoMoreCustomerRestoTotal.add(o.getAmountWithChildren());
                        } else {
                            todayBackTwoMoreCustomerRestoTotal = todayBackTwoMoreCustomerRestoTotal.add(o.getOrderMoney());
                        }
                    }

                }
                //本日end----------

                //上旬begin------------------
                if (getDay(o.getCreateTime()).contains(4)) {
                    //1.resto订单总额
                    if (o.getOrderMode() == ShopMode.HOUFU_ORDER && o.getAmountWithChildren().compareTo(BigDecimal.ZERO) != 0) {
                        firstOfMonthRestoTotal = firstOfMonthRestoTotal.add(o.getAmountWithChildren());
                    } else {
                        firstOfMonthRestoTotal = firstOfMonthRestoTotal.add(o.getOrderMoney());
                    }
                    //2.满意度
                    if (null != o.getAppraise()) {
                        if (o.getAppraise().getLevel() != null) {
                            firstOfMonthAppraiseNum++;
                            firstOfMonthAppraiseSum += o.getAppraise().getLevel() * 20;
                        }
                    }
                    firstOfMonthSatisfaction = String.valueOf(dayAppraiseNum != 0 ? firstOfMonthAppraiseSum / firstOfMonthAppraiseNum : "");
                    //3.resto的订单总数
                    if (o.getParentOrderId() == null) {
                        firstOfMonthRestoCount.add(o.getId());
                    }
                    //4.订单中实收总额
                    if (o.getOrderPaymentItems() != null) {
                        for (OrderPaymentItem oi : o.getOrderPaymentItems()) {
                            if (oi.getPaymentModeId() == PayMode.WEIXIN_PAY || oi.getPaymentModeId() == PayMode.ALI_PAY || oi.getPaymentModeId() == PayMode.MONEY_PAY || oi.getPaymentModeId() == PayMode.ARTICLE_BACK_PAY) {
                                firstOfMonthPayRestoTotal = firstOfMonthRestoTotal.add(oi.getPayValue());
                            }
                        }
                    }
                    //5.新增用户的订单总额

                    if (firstOfMonthNewCustomer.contains(o.getCustomerId())) {
                        if (o.getOrderMode() == ShopMode.HOUFU_ORDER && o.getAmountWithChildren().compareTo(BigDecimal.ZERO) != 0) {
                            firstOfMonthNewCustomerRestoTotal = firstOfMonthNewCustomerRestoTotal.add(o.getAmountWithChildren());
                        } else {
                            firstOfMonthNewCustomerRestoTotal = firstOfMonthNewCustomerRestoTotal.add(o.getOrderMoney());
                        }
                    }
                    //6自然到店的用户订单总额
                    if (firstOfMonthNormalCustomer.contains(o.getCustomerId())) {
                        if (o.getOrderMode() == 5 && o.getAmountWithChildren().compareTo(BigDecimal.ZERO) != 0) {
                            firstOfMonthNewNormalCustomerRestoTotal = firstOfMonthNewNormalCustomerRestoTotal.add(o.getAmountWithChildren());
                        } else {
                            firstOfMonthNewNormalCustomerRestoTotal = firstOfMonthNewNormalCustomerRestoTotal.add(o.getOrderMoney());
                        }
                    }
                    //7.分享到店的用户订单总额
                    if (firstOfMonthShareCustomer.contains(o.getCustomerId())) {
                        if (o.getOrderMode() == 5 && o.getAmountWithChildren().compareTo(BigDecimal.ZERO) != 0) {
                            firstOfMonthNewShareCustomerRestoTotal = firstOfMonthNewShareCustomerRestoTotal.add(o.getAmountWithChildren());
                        } else {
                            firstOfMonthNewShareCustomerRestoTotal = firstOfMonthNewShareCustomerRestoTotal.add(o.getOrderMoney());
                        }
                    }

                    //8.回头用户的订单总额
                    if (firstOfMonthBackCustomer.contains(o.getCustomerId())) {
                        if (o.getOrderMode() == 5 && o.getAmountWithChildren().compareTo(BigDecimal.ZERO) != 0) {

                            firstOfMonthBackCustomerRestoTotal = firstOfMonthBackCustomerRestoTotal.add(o.getAmountWithChildren());
                        } else {
                            firstOfMonthBackCustomerRestoTotal = firstOfMonthBackCustomerRestoTotal.add(o.getOrderMoney());
                        }
                    }

                    //9.二次回头用户的订单总额
                    if (firstOfMonthBackTwoCustomer.contains(o.getCustomerId())) {
                        if (o.getOrderMode() == 5 && o.getAmountWithChildren().compareTo(BigDecimal.ZERO) != 0) {
                            firstOfMonthBackTwoCustomerRestoTotal = firstOfMonthBackTwoCustomerRestoTotal.add(o.getAmountWithChildren());
                        } else {
                            firstOfMonthBackTwoCustomerRestoTotal = firstOfMonthBackTwoCustomerRestoTotal.add(o.getOrderMoney());
                        }
                    }

                    //10.多次回头用户的订单总额
                    if (firstOfMonthBackTwoMoreCustomer.contains(o.getCustomerId())) {
                        if (o.getOrderMode() == 5 && o.getAmountWithChildren().compareTo(BigDecimal.ZERO) != 0) {
                            firstOfMonthBackTwoMoreCustomerRestoTotal = firstOfMonthBackTwoMoreCustomerRestoTotal.add(o.getAmountWithChildren());
                        } else {
                            firstOfMonthBackTwoMoreCustomerRestoTotal = firstOfMonthBackTwoMoreCustomerRestoTotal.add(o.getOrderMoney());
                        }
                    }

                }

                //上旬end -------------------

                //中旬begin------------------
                if (getDay(o.getCreateTime()).contains(6)) {
                    //1.resto订单总额
                    if (o.getOrderMode() == ShopMode.HOUFU_ORDER && o.getAmountWithChildren().compareTo(BigDecimal.ZERO) != 0) {
                        middleOfMonthRestoTotal = middleOfMonthRestoTotal.add(o.getAmountWithChildren());
                    } else {
                        middleOfMonthRestoTotal = middleOfMonthRestoTotal.add(o.getOrderMoney());
                    }
                    //2.满意度
                    if (null != o.getAppraise()) {
                        if (o.getAppraise().getLevel() != null) {
                            middleOfMonthAppraiseNum++;
                            middleOfMonthAppraiseSum += o.getAppraise().getLevel() * 20;
                        }
                    }
                    middleOfMonthSatisfaction = String.valueOf(dayAppraiseNum != 0 ? middleOfMonthAppraiseSum / middleOfMonthAppraiseNum : "");
                    //3.resto的订单总数
                    if (o.getParentOrderId() == null) {
                        middleOfMonthRestoCount.add(o.getId());
                    }
                    //4.订单中实收总额
                    if (o.getOrderPaymentItems() != null) {
                        for (OrderPaymentItem oi : o.getOrderPaymentItems()) {
                            if (oi.getPaymentModeId() == PayMode.WEIXIN_PAY || oi.getPaymentModeId() == PayMode.ALI_PAY || oi.getPaymentModeId() == PayMode.MONEY_PAY || oi.getPaymentModeId() == PayMode.ARTICLE_BACK_PAY) {
                                middleOfMonthPayRestoTotal = middleOfMonthRestoTotal.add(oi.getPayValue());
                            }
                        }
                    }
                    //5.新增用户的订单总额

                    if (middleOfMonthNewCustomer.contains(o.getCustomerId())) {
                        if (o.getOrderMode() == ShopMode.HOUFU_ORDER && o.getAmountWithChildren().compareTo(BigDecimal.ZERO) != 0) {
                            middleOfMonthNewCustomerRestoTotal = middleOfMonthNewCustomerRestoTotal.add(o.getAmountWithChildren());
                        } else {
                            middleOfMonthNewCustomerRestoTotal = middleOfMonthNewCustomerRestoTotal.add(o.getOrderMoney());
                        }
                    }
                    //6自然到店的用户订单总额
                    if (middleOfMonthNormalCustomer.contains(o.getCustomerId())) {
                        if (o.getOrderMode() == 5 && o.getAmountWithChildren().compareTo(BigDecimal.ZERO) != 0) {
                            middleOfMonthNewNormalCustomerRestoTotal = middleOfMonthNewNormalCustomerRestoTotal.add(o.getAmountWithChildren());
                        } else {
                            middleOfMonthNewNormalCustomerRestoTotal = middleOfMonthNewNormalCustomerRestoTotal.add(o.getOrderMoney());
                        }
                    }
                    //7.分享到店的用户订单总额
                    if (middleOfMonthShareCustomer.contains(o.getCustomerId())) {
                        if (o.getOrderMode() == 5 && o.getAmountWithChildren().compareTo(BigDecimal.ZERO) != 0) {
                            middleOfMonthNewShareCustomerRestoTotal = middleOfMonthNewShareCustomerRestoTotal.add(o.getAmountWithChildren());
                        } else {
                            middleOfMonthNewShareCustomerRestoTotal = middleOfMonthNewShareCustomerRestoTotal.add(o.getOrderMoney());
                        }
                    }

                    //8.回头用户的订单总额
                    if (middleOfMonthBackCustomer.contains(o.getCustomerId())) {
                        if (o.getOrderMode() == 5 && o.getAmountWithChildren().compareTo(BigDecimal.ZERO) != 0) {

                            middleOfMonthBackCustomerRestoTotal = middleOfMonthBackCustomerRestoTotal.add(o.getAmountWithChildren());
                        } else {
                            middleOfMonthBackCustomerRestoTotal = middleOfMonthBackCustomerRestoTotal.add(o.getOrderMoney());
                        }
                    }

                    //9.二次回头用户的订单总额
                    if (middleOfMonthBackTwoCustomer.contains(o.getCustomerId())) {
                        if (o.getOrderMode() == 5 && o.getAmountWithChildren().compareTo(BigDecimal.ZERO) != 0) {
                            middleOfMonthBackTwoCustomerRestoTotal = middleOfMonthBackTwoCustomerRestoTotal.add(o.getAmountWithChildren());
                        } else {
                            middleOfMonthBackTwoCustomerRestoTotal = middleOfMonthBackTwoCustomerRestoTotal.add(o.getOrderMoney());
                        }
                    }

                    //10.多次回头用户的订单总额
                    if (middleOfMonthBackTwoMoreCustomer.contains(o.getCustomerId())) {
                        if (o.getOrderMode() == 5 && o.getAmountWithChildren().compareTo(BigDecimal.ZERO) != 0) {
                            middleOfMonthBackTwoMoreCustomerRestoTotal = middleOfMonthBackTwoMoreCustomerRestoTotal.add(o.getAmountWithChildren());
                        } else {
                            middleOfMonthBackTwoMoreCustomerRestoTotal = middleOfMonthBackTwoMoreCustomerRestoTotal.add(o.getOrderMoney());
                        }
                    }

                }

                //中旬end -------------------

                //下旬begin------------------
                if (getDay(o.getCreateTime()).contains(8)) {
                    //1.resto订单总额
                    if (o.getOrderMode() == ShopMode.HOUFU_ORDER && o.getAmountWithChildren().compareTo(BigDecimal.ZERO) != 0) {
                        lastOfMonthRestoTotal = lastOfMonthRestoTotal.add(o.getAmountWithChildren());
                    } else {
                        lastOfMonthRestoTotal = lastOfMonthRestoTotal.add(o.getOrderMoney());
                    }
                    //2.满意度
                    if (null != o.getAppraise()) {
                        if (o.getAppraise().getLevel() != null) {
                            lastOfMonthAppraiseNum++;
                            lastOfMonthAppraiseSum += o.getAppraise().getLevel() * 20;
                        }
                    }
                    lastOfMonthSatisfaction = String.valueOf(dayAppraiseNum != 0 ? lastOfMonthAppraiseSum / lastOfMonthAppraiseNum : "");
                    //3.resto的订单总数
                    if (o.getParentOrderId() == null) {
                        lastOfMonthRestoCount.add(o.getId());
                    }
                    //4.订单中实收总额
                    if (o.getOrderPaymentItems() != null) {
                        for (OrderPaymentItem oi : o.getOrderPaymentItems()) {
                            if (oi.getPaymentModeId() == PayMode.WEIXIN_PAY || oi.getPaymentModeId() == PayMode.ALI_PAY || oi.getPaymentModeId() == PayMode.MONEY_PAY || oi.getPaymentModeId() == PayMode.ARTICLE_BACK_PAY) {
                                lastOfMonthPayRestoTotal = lastOfMonthRestoTotal.add(oi.getPayValue());
                            }
                        }
                    }
                    //5.新增用户的订单总额

                    if (lastOfMonthNewCustomer.contains(o.getCustomerId())) {
                        if (o.getOrderMode() == ShopMode.HOUFU_ORDER && o.getAmountWithChildren().compareTo(BigDecimal.ZERO) != 0) {
                            lastOfMonthNewCustomerRestoTotal = lastOfMonthNewCustomerRestoTotal.add(o.getAmountWithChildren());
                        } else {
                            lastOfMonthNewCustomerRestoTotal = lastOfMonthNewCustomerRestoTotal.add(o.getOrderMoney());
                        }
                    }
                    //6自然到店的用户订单总额
                    if (lastOfMonthNormalCustomer.contains(o.getCustomerId())) {
                        if (o.getOrderMode() == 5 && o.getAmountWithChildren().compareTo(BigDecimal.ZERO) != 0) {
                            lastOfMonthNewNormalCustomerRestoTotal = lastOfMonthNewNormalCustomerRestoTotal.add(o.getAmountWithChildren());
                        } else {
                            lastOfMonthNewNormalCustomerRestoTotal = lastOfMonthNewNormalCustomerRestoTotal.add(o.getOrderMoney());
                        }
                    }
                    //7.分享到店的用户订单总额
                    if (lastOfMonthShareCustomer.contains(o.getCustomerId())) {
                        if (o.getOrderMode() == 5 && o.getAmountWithChildren().compareTo(BigDecimal.ZERO) != 0) {
                            lastOfMonthNewShareCustomerRestoTotal = lastOfMonthNewShareCustomerRestoTotal.add(o.getAmountWithChildren());
                        } else {
                            lastOfMonthNewShareCustomerRestoTotal = lastOfMonthNewShareCustomerRestoTotal.add(o.getOrderMoney());
                        }
                    }

                    //8.回头用户的订单总额
                    if (lastOfMonthBackCustomer.contains(o.getCustomerId())) {
                        if (o.getOrderMode() == 5 && o.getAmountWithChildren().compareTo(BigDecimal.ZERO) != 0) {

                            lastOfMonthBackCustomerRestoTotal = lastOfMonthBackCustomerRestoTotal.add(o.getAmountWithChildren());
                        } else {
                            lastOfMonthBackCustomerRestoTotal = lastOfMonthBackCustomerRestoTotal.add(o.getOrderMoney());
                        }
                    }

                    //9.二次回头用户的订单总额
                    if (lastOfMonthBackTwoCustomer.contains(o.getCustomerId())) {
                        if (o.getOrderMode() == 5 && o.getAmountWithChildren().compareTo(BigDecimal.ZERO) != 0) {
                            lastOfMonthBackTwoCustomerRestoTotal = lastOfMonthBackTwoCustomerRestoTotal.add(o.getAmountWithChildren());
                        } else {
                            lastOfMonthBackTwoCustomerRestoTotal = lastOfMonthBackTwoCustomerRestoTotal.add(o.getOrderMoney());
                        }
                    }

                    //10.多次回头用户的订单总额
                    if (lastOfMonthBackTwoMoreCustomer.contains(o.getCustomerId())) {
                        if (o.getOrderMode() == 5 && o.getAmountWithChildren().compareTo(BigDecimal.ZERO) != 0) {
                            lastOfMonthBackTwoMoreCustomerRestoTotal = lastOfMonthBackTwoMoreCustomerRestoTotal.add(o.getAmountWithChildren());
                        } else {
                            lastOfMonthBackTwoMoreCustomerRestoTotal = lastOfMonthBackTwoMoreCustomerRestoTotal.add(o.getOrderMoney());
                        }
                    }

                }
                //下旬end -------------------

                //本月begin---------------
                if (getDay(o.getCreateTime()).contains(10)) {
                    //1.resto订单总额
                    if (o.getOrderMode() == ShopMode.HOUFU_ORDER && o.getAmountWithChildren().compareTo(BigDecimal.ZERO) != 0) {
                        monthRestoTotal = monthRestoTotal.add(o.getAmountWithChildren());
                    } else {
                        monthRestoTotal = monthRestoTotal.add(o.getOrderMoney());
                    }
                    //2.满意度
                    if (null != o.getAppraise()) {
                        if (o.getAppraise().getLevel() != null) {
                            monthAppraiseNum++;
                            monthAppraiseSum += o.getAppraise().getLevel() * 20;
                        }
                    }
                    monthSatisfaction = String.valueOf(dayAppraiseNum != 0 ? monthAppraiseSum / monthAppraiseNum : "");
                    //3.resto的订单总数
                    if (o.getParentOrderId() == null) {
                        monthRestoCount.add(o.getId());
                    }
                    //4.订单中实收总额
                    if (o.getOrderPaymentItems() != null) {
                        for (OrderPaymentItem oi : o.getOrderPaymentItems()) {
                            if (oi.getPaymentModeId() == PayMode.WEIXIN_PAY || oi.getPaymentModeId() == PayMode.ALI_PAY || oi.getPaymentModeId() == PayMode.MONEY_PAY || oi.getPaymentModeId() == PayMode.ARTICLE_BACK_PAY) {
                                monthPayRestoTotal = monthRestoTotal.add(oi.getPayValue());
                            }
                        }
                    }
                    //5.新增用户的订单总额

                    if (monthNewCustomer.contains(o.getCustomerId())) {
                        if (o.getOrderMode() == ShopMode.HOUFU_ORDER && o.getAmountWithChildren().compareTo(BigDecimal.ZERO) != 0) {
                            monthNewCustomerRestoTotal = monthNewCustomerRestoTotal.add(o.getAmountWithChildren());
                        } else {
                            monthNewCustomerRestoTotal = monthNewCustomerRestoTotal.add(o.getOrderMoney());
                        }
                    }
                    //6自然到店的用户订单总额
                    if (monthNormalCustomer.contains(o.getCustomerId())) {
                        if (o.getOrderMode() == 5 && o.getAmountWithChildren().compareTo(BigDecimal.ZERO) != 0) {
                            monthNewNormalCustomerRestoTotal = monthNewNormalCustomerRestoTotal.add(o.getAmountWithChildren());
                        } else {
                            monthNewNormalCustomerRestoTotal = monthNewNormalCustomerRestoTotal.add(o.getOrderMoney());
                        }
                    }
                    //7.分享到店的用户订单总额
                    if (monthShareCustomer.contains(o.getCustomerId())) {
                        if (o.getOrderMode() == 5 && o.getAmountWithChildren().compareTo(BigDecimal.ZERO) != 0) {
                            monthNewShareCustomerRestoTotal = monthNewShareCustomerRestoTotal.add(o.getAmountWithChildren());
                        } else {
                            monthNewShareCustomerRestoTotal = monthNewShareCustomerRestoTotal.add(o.getOrderMoney());
                        }
                    }

                    //8.回头用户的订单总额
                    if (monthBackCustomer.contains(o.getCustomerId())) {
                        if (o.getOrderMode() == 5 && o.getAmountWithChildren().compareTo(BigDecimal.ZERO) != 0) {

                            monthBackCustomerRestoTotal = monthBackCustomerRestoTotal.add(o.getAmountWithChildren());
                        } else {
                            monthBackCustomerRestoTotal = monthBackCustomerRestoTotal.add(o.getOrderMoney());
                        }
                    }

                    //9.二次回头用户的订单总额
                    if (monthBackTwoCustomer.contains(o.getCustomerId())) {
                        if (o.getOrderMode() == 5 && o.getAmountWithChildren().compareTo(BigDecimal.ZERO) != 0) {
                            monthBackTwoCustomerRestoTotal = monthBackTwoCustomerRestoTotal.add(o.getAmountWithChildren());
                        } else {
                            monthBackTwoCustomerRestoTotal = monthBackTwoCustomerRestoTotal.add(o.getOrderMoney());
                        }
                    }

                    //10.多次回头用户的订单总额
                    if (monthBackTwoMoreCustomer.contains(o.getCustomerId())) {
                        if (o.getOrderMode() == 5 && o.getAmountWithChildren().compareTo(BigDecimal.ZERO) != 0) {
                            monthBackTwoMoreCustomerRestoTotal = monthBackTwoMoreCustomerRestoTotal.add(o.getAmountWithChildren());
                        } else {
                            monthBackTwoMoreCustomerRestoTotal = monthBackTwoMoreCustomerRestoTotal.add(o.getOrderMoney());
                        }
                    }

                }

                //本月end---------------
            }

            //查询resto充值(微信充值+pos充值)  实收总额 = (微信支付+支付宝+其他支付)+(pos充值+微信充值)
            List<ChargeOrder> chargeOrderList = chargeOrderService.selectByDateAndShopId(beginMonth, endMonth, shopDetail.getName());
            if (!chargeOrderList.isEmpty()) {
                for (ChargeOrder c : chargeOrderList) {
                    //本日
                    if (getDay(c.getCreateTime()).contains(2)) {
                        todayPayRestoTotal = todayRestoTotal.add(c.getChargeMoney());
                    }
                    //上旬
                    if (getDay(c.getCreateTime()).contains(4)) {
                        firstOfMonthPayRestoTotal = firstOfMonthRestoTotal.add(c.getChargeMoney());
                    }
                    //中旬
                    if (getDay(c.getCreateTime()).contains(6)) {
                        lastOfMonthPayRestoTotal = lastOfMonthRestoTotal.add(c.getChargeMoney());
                    }
                    //下旬
                    if (getDay(c.getCreateTime()).contains(8)) {
                        lastOfMonthPayRestoTotal = lastOfMonthRestoTotal.add(c.getChargeMoney());
                    }
                    //本月
                    if (getDay(c.getCreateTime()).contains(10)) {
                        monthPayRestoTotal = monthRestoTotal.add(c.getChargeMoney());
                    }
                }
            }

            int xun = DateUtil.getEarlyMidLate(new Date());
            //发送本日信息 本月信息 上旬信息
            //本日信息
            StringBuilder todayContent = new StringBuilder();
            todayContent.append("{")
                    .append("shopName:").append("'").append(shopDetail.getName()).append("'").append(",")
                    .append("dateTime:").append("'").append(DateUtil.formatDate(new Date(), "yyyy-MM-dd hh:mm:ss")).append("'").append(",")//
                    .append("totalMoney:").append("'").append(todayRestoTotal.add(todayEnterTotal)).append("'").append(",")
                    .append("orderCount:").append("'").append(todayRestoCount.size() + todayEnterCount).append("'").append(",")
                    .append("paymentAmount:").append("'").append(todayRestoTotal).append("'").append(",")
                    .append("paymentTotal:").append("'").append(todayRestoTotal.add(todayEnterTotal)).append("'").append(",")
                    .append("newCustomerPay:").append("'").append(todayNewCutomer.size() + "位").append("'").append(",")
                    .append("newCustomerPayTotal:").append("'").append("￥:").append(todayNewCustomerRestoTotal).append("'").append(",")
                    .append("orginCustomerPay:").append("'").append(todayNormalNewCustomer.size()).append("位").append("'").append(",")
                    .append("orginCustomerPayTotal:").append("'").append("￥:").append(todayNewNormalCustomerRestoTotal).append("'").append(",")
                    .append("shareCustomerPay:").append("'").append(todayShareNewCutomer.size()).append("位").append("'").append(",")
                    .append("shareCustomerPayTotal:").append("'").append("￥:").append(todayNewShareCustomerRestoTotal).append("'").append(",")
                    .append("backCustomerPay:").append("'").append(todayBackCustomer.size()).append("位").append("'").append(",")
                    .append("backCustomerPayTotal:").append("'").append("￥:").append(todayBackCustomerRestoTotal).append("'").append(",")
                    .append("secondBackCustomerAmount:").append("'").append(todayBackTwoCustomer.size()).append("位").append("'").append(",")
                    .append("secondBackCustomerTotal:").append("'").append("￥:").append(todayBackCustomerRestoTotal).append("'").append(",")
                    .append("backCustomersAmount:").append("'").append(todayBackTwoMoreCustomer.size()).append("位").append("'").append(",")
                    .append("backCustomersTotal:").append("'").append("￥:").append(todayBackTwoMoreCustomerRestoTotal).append("'").append(",")
                    .append("customerPayPercent:").append("'").append(todayRestoTotal).append("/").append(todayRestoTotal.add(todayEnterTotal)).append("'").append(",")
                    .append("newCustomerPercent:").append("'").append(todayNewCutomer.size()).append("/").append((todayBackCustomer.size() + todayNewCutomer.size())).append("'").append(",")
                    //r订单总数/(r订单总数+线下订单总数)
                    .append("payOnlinePercent:").append("'").append(todayRestoCount.size()).append("/").append(todayRestoCount.size() + todayEnterCount).append("'").append(",")
                    .append("satisfied:").append("'").append(todaySatisfaction).append("'")
                    .append("}");

            System.out.println("toadyEnterCount:" + todayEnterCount);

            //发送上旬信息
            StringBuilder firstcontent = new StringBuilder();
            firstcontent.append("{")
                    //满意度 店铺评分
                    .append("lastSatisfied:").append("'").append(firstOfMonthSatisfaction).append("'").append(",")
                    //用户消费占比 r订单总额/(r订单总额+线下订单总额)
                    .append("lastCustomerPayPercent:").append("'").append(firstOfMonthRestoTotal + "/" + firstOfMonthRestoTotal.add(firstOfMonthEnterTotal)).append("'").append(",")
                    //新增用户占比  新增用户总数/上旬总人数
                    .append("lastNewCustomerCount:").append("'").append(firstOfMonthNewCustomer.size() + "/" + customerInFirstOfMonth.size()).append("'").append(",")
                    //在线支付笔数占比 r订单总数/(r订单总数+线下订单总数)
                    .append("lastOnlinePercent:").append("'").append(firstOfMonthRestoCount.size() + "/" + firstOfMonthEnterCount + firstOfMonthRestoCount.size()).append("'").append(",")
                    //总支付金额 微信+支付宝+其他+线下+（pos+微信）充值
                    .append("lasterTotalPayment:").append("'").append(firstOfMonthEnterTotal.add(firstOfMonthRestoTotal)).append("'").append(",")
                    //用户支付金额(微信+支付宝+其他)+(pos+微信)充值
                    .append("lastCustomerPayment:").append("'").append(firstOfMonthPayRestoTotal).append("'").append(",")
                    //新增用户消费 上旬新增用户订单/上旬订单总额
                    .append("lastNewCustomerPay:").append("'").append(firstOfMonthNewCustomer.size()).append("位").append("'").append(",")
                    .append("lastNewCustomerPayTotal:").append("'").append(firstOfMonthNewCustomerRestoTotal).append("'").append(",")
                    //上旬新增 自然用户
                    .append("lastOrginCustomerCount:").append("'").append(firstOfMonthNormalCustomer.size()).append("位").append("/").append("￥:").append(firstOfMonthNewNormalCustomerRestoTotal).append("'").append(",")
                    //上旬新增分享用户
                    .append("lastShareCustomerCount:").append("'").append(firstOfMonthShareCustomer.size()).append("位").append("/").append("￥:").append(firstOfMonthNewShareCustomerRestoTotal).append("'").append(",")
                    //回头用户消费
                    .append("lastBackPayment:").append("'").append(firstOfMonthBackCustomer.size()).append("位").append("/").append("￥:").append(firstOfMonthBackCustomerRestoTotal).append("'").append(",")
                    //新增回头
                    .append("lastNewBackCustomerCount:").append("'").append(firstOfMonthBackTwoCustomer.size()).append("位").append("/").append("￥:").append(firstOfMonthBackTwoCustomerRestoTotal).append("'").append(",")
                    //多次回头用户
                    .append("lastBackCustomersCount:").append("'").append(firstOfMonthBackTwoMoreCustomer.size()).append("位").append("/").append("￥:").append(firstOfMonthBackTwoMoreCustomerRestoTotal).append("'")
                    .append("}");

            //发送本月信息
            StringBuilder monthContent = new StringBuilder();
            monthContent.append("{")
                    //满意度 店铺评分
                    .append("nowSatisfied:").append("'").append(monthSatisfaction).append("'").append(",")
                    //用户消费占比 r订单总额/(r订单总额+线下订单总额)
                    .append("nowCustomerPayPercent:").append("'").append(monthRestoTotal + "/" + monthRestoTotal.add(monthEnterTotal)).append("'").append(",")
                    //新增用户占比  新增用户总数/上月总人数
                    .append("nowNewCustomerCount:").append("'").append(monthNewCustomer.size() + "/" + customerInMonth.size()).append("'").append(",")
                    //在线支付笔数占比 r订单总数/(r订单总数+线下订单总数)
                    .append("nowOnlinePercent:").append("'").append(monthRestoCount.size() + "/" + monthEnterCount + monthRestoCount.size()).append("'").append(",")
                    //总支付金额 微信+支付宝+其他+线下+（pos+微信）充值
                    .append("nowerTotalPayment:").append("'").append(monthEnterTotal.add(monthRestoTotal)).append("'").append(",")
                    //用户支付金额(微信+支付宝+其他)+(pos+微信)充值
                    .append("nowCustomerPayment:").append("'").append(monthPayRestoTotal).append("'").append(",")
                    //新增用户消费 上旬新增用户订单/上旬订单总额
                    .append("nowNewCustomerPay:").append("'").append(monthNewCustomer.size()).append("位").append("'").append(",")
                    .append("nowNewCustomerPayTotal:").append("'").append(monthNewCustomerRestoTotal).append("'").append(",")
                    //上旬新增 自然用户
                    .append("nowOrginCustomerCount:").append("'").append(monthNormalCustomer.size()).append("位").append("/").append("￥:").append(monthNewNormalCustomerRestoTotal).append("'").append(",")
                    //上旬新增分享用户
                    .append("nowShareCustomerCount:").append("'").append(monthShareCustomer.size()).append("位").append("/").append("￥:").append(monthNewShareCustomerRestoTotal).append("'").append(",")
                    //回头用户消费
                    .append("nowBackPayment:").append("'").append(monthBackCustomer.size()).append("位").append("/").append("￥:").append(monthBackCustomerRestoTotal).append("'").append(",")
                    //新增回头
                    .append("nowNewBackCustomerCount:").append("'").append(monthBackTwoCustomer.size()).append("位").append("/").append("￥:").append(monthBackTwoCustomerRestoTotal).append("'").append(",")
                    //多次回头用户
                    .append("nowBackCustomersCount:").append("'").append(monthBackTwoMoreCustomer.size()).append("位").append("/").append("￥:").append(monthBackTwoMoreCustomerRestoTotal).append("'")
                    .append("}");


            //封装中询文本

            StringBuilder middlecontent = new StringBuilder();
            middlecontent.append("{")
                    //满意度 店铺评分
                    .append("middleSatisfied:").append("'").append(middleOfMonthSatisfaction).append("'").append(",")
                    //用户消费占比 r订单总额/(r订单总额+线下订单总额)
                    .append("middleCustomerPayPercent:").append("'").append(middleOfMonthRestoTotal + "/" + middleOfMonthRestoTotal.add(middleOfMonthEnterTotal)).append("'").append(",")
                    //新增用户占比  新增用户总数/上旬总人数
                    .append("middleNewCustomerCount:").append("'").append(middleOfMonthNewCustomer.size() + "/" + customerInMiddleOfMonth.size()).append("'").append(",")
                    //在线支付笔数占比 r订单总数/(r订单总数+线下订单总数)
                    .append("middleOnlinePercent:").append("'").append(middleOfMonthRestoCount.size() + "/" + middleOfMonthEnterCount + middleOfMonthRestoCount.size()).append("'").append(",")
                    //总支付金额 微信+支付宝+其他+线下+（pos+微信）充值
                    .append("middleerTotalPayment:").append("'").append(middleOfMonthEnterTotal.add(middleOfMonthRestoTotal)).append("'").append(",")
                    //用户支付金额(微信+支付宝+其他)+(pos+微信)充值
                    .append("middleCustomerPayment:").append("'").append(middleOfMonthPayRestoTotal).append("'").append(",")
                    //新增用户消费 上旬新增用户订单/上旬订单总额
                    .append("middleNewCustomerPay:").append("'").append(middleOfMonthNewCustomer.size()).append("位").append("'").append(",")
                    .append("middleNewCustomerPayTotal:").append("'").append(middleOfMonthNewCustomerRestoTotal).append("'").append(",")
                    //上旬新增 自然用户
                    .append("middleOrginCustomerCount:").append("'").append(middleOfMonthNormalCustomer.size()).append("位").append("/").append("￥:").append(middleOfMonthNewNormalCustomerRestoTotal).append("'").append(",")
                    //上旬新增分享用户
                    .append("middleShareCustomerCount:").append("'").append(middleOfMonthShareCustomer.size()).append("位").append("/").append("￥:").append(middleOfMonthNewShareCustomerRestoTotal).append("'").append(",")
                    //回头用户消费
                    .append("middleBackPayment:").append("'").append(middleOfMonthBackCustomer.size()).append("位").append("/").append("￥:").append(middleOfMonthBackCustomerRestoTotal).append("'").append(",")
                    //新增回头
                    .append("middleNewBackCustomerCount:").append("'").append(middleOfMonthBackTwoCustomer.size()).append("位").append("/").append("￥:").append(middleOfMonthBackTwoCustomerRestoTotal).append("'").append(",")
                    //多次回头用户
                    .append("middleBackCustomersCount:").append("'").append(middleOfMonthBackTwoMoreCustomer.size()).append("位").append("/").append("￥:").append(middleOfMonthBackTwoMoreCustomerRestoTotal).append("'")
                    .append("}");

            //封装下旬文本
            StringBuilder lastcontent = new StringBuilder();
            lastcontent.append("{")
                    //满意度 店铺评分
                    .append("lastSatisfied:").append("'").append(lastOfMonthSatisfaction).append("'").append(",")
                    //用户消费占比 r订单总额/(r订单总额+线下订单总额)
                    .append("lastCustomerPayPercent:").append("'").append(lastOfMonthRestoTotal + "/" + lastOfMonthRestoTotal.add(lastOfMonthEnterTotal)).append("'").append(",")
                    //新增用户占比  新增用户总数/上旬总人数
                    .append("lastNewCustomerCount:").append("'").append(lastOfMonthNewCustomer.size() + "/" + customerInLastOfMonth.size()).append("'").append(",")
                    //在线支付笔数占比 r订单总数/(r订单总数+线下订单总数)
                    .append("lastOnlinePercent:").append("'").append(lastOfMonthRestoCount.size() + "/" + lastOfMonthEnterCount + lastOfMonthRestoCount.size()).append("'").append(",")
                    //总支付金额 微信+支付宝+其他+线下+（pos+微信）充值
                    .append("lasterTotalPayment:").append("'").append(lastOfMonthEnterTotal.add(lastOfMonthRestoTotal)).append("'").append(",")
                    //用户支付金额(微信+支付宝+其他)+(pos+微信)充值
                    .append("lastCustomerPayment:").append("'").append(lastOfMonthPayRestoTotal).append("'").append(",")
                    //新增用户消费 上旬新增用户订单/上旬订单总额
                    .append("lastNewCustomerPay:").append("'").append(lastOfMonthNewCustomer.size()).append("位").append("'").append(",")
                    .append("lastNewCustomerPayTotal:").append("'").append(lastOfMonthNewCustomerRestoTotal).append("'").append(",")
                    //上旬新增 自然用户
                    .append("lastOrginCustomerCount:").append("'").append(lastOfMonthNormalCustomer.size()).append("位").append("/").append("￥:").append(lastOfMonthNewNormalCustomerRestoTotal).append("'").append(",")
                    //上旬新增分享用户
                    .append("lastShareCustomerCount:").append("'").append(lastOfMonthShareCustomer.size()).append("位").append("/").append("￥:").append(lastOfMonthNewShareCustomerRestoTotal).append("'").append(",")
                    //回头用户消费
                    .append("lastBackPayment:").append("'").append(lastOfMonthBackCustomer.size()).append("位").append("/").append("￥:").append(lastOfMonthBackCustomerRestoTotal).append("'").append(",")
                    //新增回头
                    .append("lastNewBackCustomerCount:").append("'").append(lastOfMonthBackTwoCustomer.size()).append("位").append("/").append("￥:").append(lastOfMonthBackTwoCustomerRestoTotal).append("'").append(",")
                    //多次回头用户
                    .append("lastBackCustomersCount:").append("'").append(lastOfMonthBackTwoMoreCustomer.size()).append("位").append("/").append("￥:").append(lastOfMonthBackTwoMoreCustomerRestoTotal).append("'")
                    .append("}");
            System.out.println(shopDetail.getIsOpenSms());
            System.out.println(shopDetail.getnoticeTelephone());
            System.err.println(1 == shopDetail.getIsOpenSms() && null != shopDetail.getnoticeTelephone());


            if (1 == shopDetail.getIsOpenSms() && null != shopDetail.getnoticeTelephone()) {
                //截取电话号码
                String[] telephones = shopDetail.getnoticeTelephone().split("，");
                for (String tel : telephones) {
                    SMSUtils.sendMessage(tel, todayContent.toString(), "餐加", "SMS_37160073");//推送本日信息
                    SMSUtils.sendMessage(tel, firstcontent.toString(), "餐加", "SMS_37030070");//推送上旬信息
                    SMSUtils.sendMessage(tel, monthContent.toString(), "餐加", "SMS_37685377");//推本月消息
                }

                if (xun == 1) {
                    //代表今天是上旬

                } else if (xun == 2) {//代表是中旬
                    for (String tel : telephones) {
                        //发送中旬信息
                        SMSUtils.sendMessage(tel, middlecontent.toString(), "餐加", "SMS_37065121");
                    }

                } else if (xun == 3) {//代表是下旬
                    for (String tel : telephones) {
                        //发送中旬和下旬信息
                        SMSUtils.sendMessage(tel, middlecontent.toString(), "餐加", "SMS_37065121");
                        SMSUtils.sendMessage(tel, middlecontent.toString(), "餐加", "SMS_36965049");
                    }
                }
            }

        }
    }


    //根据当前的订单时间来判断 是属于哪个时间段(可能是多属于：是今日之前 也是 本月之前)
    // 1.今日之前 2.今日中 3.上旬之前 4.上旬中 5 中旬之前 6中旬中 7.下旬之前 8下旬中 9本月之前 10本月中
    public static List<Integer> getDay(Date createTime) {
        //本月的开始时间 本月结束时间
        String beginMonth = DateUtil.getMonthBegin();
        Date begin = DateUtil.getDateBegin(DateUtil.fomatDate(beginMonth));//当月开始
        //本日开始时间戳
        Long dayBefore = DateUtil.getDateBegin(new Date()).getTime();
        //本日结束时间戳
        Long dayAfter = DateUtil.getDateEnd(new Date()).getTime();

        //本月上旬开始 也就是本月的第一天开始时间戳
        Long firstBeginOfMonth = begin.getTime();
        //本月上旬的结束时间戳
        Long firstEndOfMonth = DateUtil.getDateEnd(DateUtil.getAfterDayDate(begin, 9)).getTime();
        //本月中旬开始时间 -- 上旬结束时间
        Long middleBeginOfMonth = DateUtil.getDateBegin(DateUtil.getAfterDayDate(begin, 10)).getTime();
        //本月中旬结束时间
        Long middelEndOfMonth = DateUtil.getDateEnd(DateUtil.getAfterDayDate(begin, 19)).getTime();
        //本月下旬开始时间  -- 也就是中旬结束时间
        Long lastBeginOfMonth = DateUtil.getDateBegin(DateUtil.getAfterDayDate(begin, 20)).getTime();
        //本月下旬结束时间 -- 如果当天发送数据有下旬那么结束时间就是当天
        Long lastEndOfMonth = dayAfter;
        List<Integer> list = new ArrayList<>();
        if (createTime.getTime() < dayBefore) {
            list.add(1);//本日之前
        }
        if (createTime.getTime() < dayAfter && createTime.getTime() > dayBefore) {
            list.add(2); //本日中
        }
        if (createTime.getTime() < firstBeginOfMonth) {
            list.add(3); //上旬之前
            list.add(9);//本月之前
        }
        if (createTime.getTime() > firstBeginOfMonth && createTime.getTime() < firstEndOfMonth) {
            list.add(4); //上旬中
        }
        if (createTime.getTime() < middleBeginOfMonth) {
            list.add(5);//中旬之前
        }
        if (createTime.getTime() > middleBeginOfMonth && createTime.getTime() < middelEndOfMonth) {
            list.add(6);//中旬中
        }
        if (createTime.getTime() < lastBeginOfMonth) {
            list.add(7);//下旬之前
        }
        if (createTime.getTime() > lastBeginOfMonth && createTime.getTime() < lastEndOfMonth) {
            list.add(8);
        }
        if (createTime.getTime() > firstBeginOfMonth && createTime.getTime() < dayAfter) {
            list.add(10);
        }
        return list;
    }


//    public static void main(String[] args){
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//        StringBuilder content = new StringBuilder();
//        content
//                .append("门店:简厨凌空SHOH").append("\n")
//                .append("日报:2016.11.20").append("\n")
//                .append("堂吃支付金额:10000元").append("\n")
//                .append("商户录取").append("\n")
//                 .append("堂吃消费笔数:64").append("\n")
//                .append("商户录入").append("\n")
//                .append("用户支付消费:62/9500").append("\n")
//                .append("----------------").append("\n")
//                .append("新增用户消费:12/2600").append("\n")
//                .append("其中：").append("\n")
//                .append("自然用户消费:9/1700").append("\n")
//                .append("分享用户消费:3/900").append("\n")
//                .append("-----------------").append("\n")
//                .append("回头用户消费:50/6900").append("\n")
//                .append("其中:").append("\n")
//                .append("二次回头用户:20/3000").append("\n")
//                .append("多次回头用户:30/3900").append("\n")
//                .append("-----------------").append("\n")
//                .append("用户消费占比:96.85%").append("\n")
//                .append("(用户交易笔数/堂吃交易笔数)").append("\n")
//                .append("新增用户比率:85.76%").append("\n")
//                .append("新增消费用户/(堂吃交易笔数-回头用户交易笔数)").append("\n")
//                .append("在线支付比例:95%").append("\n")
//                .append("在线支付金额/堂吃支付金额").append("\n")
//                .append("--------------------").append("\n")
//                .append("本日满意度:99.15分").append("\n")
//                .append("------上旬合计------").append("\n")
//                .append("上旬满意度:97.5").append("\n")
//                .append("用户消费占比:96.56%").append("\n")
//                .append("新增用户占比:80.03%").append("\n")
//                .append("在线支付占比:93%").append("\n")
//                .append("总支付金额:2000000").append("\n")
//                .append("用户支付金额:111111").append("\n")
//                .append("新增用户消费:121/18500").append("\n")
//                .append("新增自然用户").append("\n")
//                .append("新增分享用户").append("\n")
//                .append("回头用户消费").append("\n")
//                .append("新增回头用户").append("\n")
//                .append("多次回头用户").append("\n")
//                .append("------中旬合计------").append("\n")
//                .append("中旬满意度:97.5").append("\n")
//                .append("用户消费占比:96.56%").append("\n")
//                .append("新增用户占比:80.03%").append("\n")
//                .append("在线支付占比:93%").append("\n")
//                .append("总支付金额:2000000").append("\n")
//                .append("用户支付金额:111111").append("\n")
//                .append("新增用户消费:121/18500").append("\n")
//                .append("新增自然用户").append("\n")
//                .append("新增分享用户").append("\n")
//                .append("回头用户消费").append("\n")
//                .append("新增回头用户").append("\n")
//                .append("多次回头用户").append("\n")
//                .append("------下旬合计------").append("\n")
//                .append("下旬满意度:97.5").append("\n")
//                .append("用户消费占比:96.56%").append("\n")
//                .append("新增用户占比:80.03%").append("\n")
//                .append("在线支付占比:93%").append("\n")
//                .append("总支付金额:2000000").append("\n")
//                .append("用户支付金额:111111").append("\n")
//                .append("新增用户消费:121/18500").append("\n")
//                .append("新增自然用户").append("\n")
//                .append("新增分享用户").append("\n")
//                .append("回头用户消费").append("\n")
//                .append("新增回头用户").append("\n")
//                .append("多次回头用户").append("\n")
//                .append("------本月合计------").append("\n")
//                .append("本月满意度:97.5").append("\n")
//                .append("用户消费占比:96.56%").append("\n")
//                .append("新增用户占比:80.03%").append("\n")
//                .append("在线支付占比:93%").append("\n")
//                .append("总支付金额:2000000").append("\n")
//                .append("用户支付金额:111111").append("\n")
//                .append("新增用户消费:121/18500").append("\n");
//        /**
//         发送客服消息
//         */
//        WeChatUtils.sendCustomerMsgASync(content.toString(), "oBHT9squwPUyTM-zwoWcWyey4PCM", "wx36bd5b9b7d264a8c", "807530431fe6e19e3f2c4a7d1a149465");
//
//    }


    public void sendWxRefundMsg(Order order) {
        if (checkRefundLimit(order)) {
            autoRefundOrder(order.getId());
            log.info("款项自动退还到相应账户:" + order.getId());
            Customer customer = customerService.selectById(order.getCustomerId());
            WechatConfig config = wechatConfigService.selectByBrandId(order.getBrandId());
            StringBuilder sb = null;
            if (order.getOrderState().equals(OrderState.SUBMIT)) {//未支付和未完成支付
                sb = new StringBuilder("亲,今日未完成支付的订单已被商家取消,欢迎下次再来本店消费\n");
            } else {//已支付未消费
                sb = new StringBuilder("亲,今日未消费订单已自动退款,欢迎下次再来本店消费\n");
            }
            sb.append("订单编号:" + order.getSerialNumber() + "\n");
            if (order.getOrderMode() != null) {
                switch (order.getOrderMode()) {
                    case ShopMode.TABLE_MODE:
                        sb.append("桌号:" + (order.getTableNumber() != null ? order.getTableNumber() : "无") + "\n");
                        break;
                    default:
                        sb.append("取餐码：" + (order.getVerCode() != null ? order.getVerCode() : "无") + "\n");
                        break;
                }
            }
            if (order.getShopName() == null || "".equals(order.getShopName())) {
                order.setShopName(shopDetailService.selectById(order.getShopDetailId()).getName());
            }
            sb.append("就餐店铺：" + order.getShopName() + "\n");
            sb.append("订单时间：" + DateFormatUtils.format(order.getCreateTime(), "yyyy-MM-dd HH:mm") + "\n");
            sb.append("订单明细：\n");
            List<OrderItem> orderItem = orderItemService.listByOrderId(order.getId());
            for (OrderItem item : orderItem) {
                sb.append("  " + item.getArticleName() + "x" + item.getCount() + "\n");
            }
            sb.append("订单金额：" + order.getOrderMoney() + "\n");
            WeChatUtils.sendCustomerMsgASync(sb.toString(), customer.getWechatId(), config.getAppid(), config.getAppsecret());
        } else {
            log.info("款项自动退还到相应账户失败，订单状态不是已付款或商品状态不是已付款未下单");
        }
    }

    @Override
    public Order getOrderDetail(String orderId) {
        Order order = orderMapper.getOrderDetail(orderId);
        order.setOrderItems(orderMapper.selectOrderItems(orderId));
        order.setOrderPaymentItems(orderMapper.selectOrderPaymentItems(orderId));
        return order;
    }

    @Override
    public List<Order> getOrderByEmployee(String employeeId, String shopId) {
        List<Order> result = orderMapper.getOrderByEmployee(shopId, employeeId);
        return result;
    }


    /**
     * 服务员点餐
     *
     * @param order
     * @return
     * @throws AppException
     */
    @Override
    public JSONResult createOrderByEmployee(Order order) throws AppException {
        JSONResult jsonResult = new JSONResult();
        String orderId = ApplicationUtils.randomUUID();
        order.setId(orderId);
        Employee employee = employeeMapper.selectByPrimaryKey(order.getEmployeeId());
        if (employee == null) {
            throw new AppException(AppException.CUSTOMER_NOT_EXISTS);
        } else if (order.getOrderItems().isEmpty()) {
            throw new AppException(AppException.ORDER_ITEMS_EMPTY);
        }

        List<Article> articles = articleService.selectList(order.getShopDetailId());
        List<ArticlePrice> articlePrices = articlePriceService.selectList(order.getShopDetailId());
        Map<String, Article> articleMap = ApplicationUtils.convertCollectionToMap(String.class, articles);
        Map<String, ArticlePrice> articlePriceMap = ApplicationUtils.convertCollectionToMap(String.class,
                articlePrices);


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

            Result check = new Result();
            if (item.getType() == OrderItemType.ARTICLE) {
                check = checkArticleList(item, item.getCount());
            } else if (item.getType() == OrderItemType.UNITPRICE) {
                check = checkArticleList(item, item.getCount());
            } else if (item.getType() == OrderItemType.SETMEALS) {
                check = checkArticleList(item, articleCount);
            }


            jsonResult.setMessage(check.getMessage());
            jsonResult.setSuccess(check.isSuccess());

            if (!check.isSuccess()) {
                break;
            }
        }


        if (!jsonResult.isSuccess()) {
            return jsonResult;
        }

        orderItemService.insertItems(order.getOrderItems());
        BigDecimal payMoney = totalMoney;


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
        order.setPrintTimes(0);
        order.setOrderState(order.getPayMode().toString().equals(PayMode.WEIXIN_PAY) ?
                OrderState.SUBMIT : OrderState.PAYMENT);

        order.setProductionStatus(ProductionStatus.HAS_ORDER);
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


        jsonResult.setData(order);
        return jsonResult;
    }

    @Override
    public Order getLastOrderByCustomer(String customerId, String shopId) {
        ShopDetail shopDetail = shopDetailService.selectByPrimaryKey(shopId);
        BrandSetting brandSetting = brandSettingService.selectByBrandId(shopDetail.getBrandId());
        Order order = orderMapper.getLastOrderByCustomer(customerId, shopId, brandSetting.getCloseContinueTime());
        if (order != null && order.getParentOrderId() != null) {
            Order parent = orderMapper.selectByPrimaryKey(order.getParentOrderId());
            if (parent != null && parent.getAllowContinueOrder()) {
                return parent;
            }
        } else {
            return order;
        }

        return null;

    }

    @Override
    public boolean cancelWXPayOrder(String orderId) {
        Order order = selectById(orderId);
        if (order.getAllowCancel() && order.getProductionStatus() != ProductionStatus.PRINTED && (order.getOrderState().equals(OrderState.SUBMIT) || order.getOrderState() == OrderState.PAYMENT)) {
            order.setAllowCancel(false);
            order.setClosed(true);
            order.setAllowAppraise(false);
            order.setAllowContinueOrder(false);
            order.setOrderState(OrderState.CANCEL);
            update(order);
            refundWXPAYOrder(order);
            log.info("取消订单成功:" + order.getId());
            return true;
        } else {
            log.warn("取消订单失败，订单状态订单状态或者订单可取消字段为false" + order.getId());
            return false;
        }
    }

    private void refundWXPAYOrder(Order order) {
        List<OrderPaymentItem> payItemsList = orderPaymentItemService.selectByOrderId(order.getId());
        for (OrderPaymentItem item : payItemsList) {
            String newPayItemId = ApplicationUtils.randomUUID();
            switch (item.getPaymentModeId()) {
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
    public List<Order> selectExceptionOrderListBybrandId(String beginDate, String endDate, String brandId) {
        Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);

        List<Order> list = orderMapper.selectExceptionOrderListBybrandId(begin, end, brandId);

        return list;
    }

    @Override
    public List<Order> selectHasPayListOrderByBrandId(String beginDate, String endDate, String brandId) {
        Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);
        return orderMapper.selectHasPayListOrderByBrandId(begin, end, brandId);
    }

    @Override
    public List<Order> selectHasPayOrderPayMentItemListBybrandId(String beginDate, String endDate, String brandId) {
        Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);
        return orderMapper.selectHasPayOrderPayMentItemListBybrandId(begin, end, brandId);
    }

    @Override
    public Order getLastOrderByTableNumber(String tableNumber) {
        return orderMapper.getLastOrderByTableNumber(tableNumber);
    }

    @Override
    public List<Order> selectOrderListItemByBrandId(String beginDate, String endDate, String brandId) {
        Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);
        return orderMapper.selectOrderListItemByBrandId(begin, end, brandId);
    }

    @Override
    public List<Order> selectListByParentId(String orderId) {
        return orderMapper.selectListByParentId(orderId);
    }

    @Override
    public List<Order> selectHoufuOrderList(String beginDate, String endDate, String brandId) {
        Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);
        return orderMapper.selectMoneyAndNumByDate(begin, end, brandId);
    }


    @Override
    public List<Order> getChildItem(String orderId) {
        List<Order> childs = orderMapper.selectByParentId(orderId);
        List<Order> result = new ArrayList<>();
        for (Order child : childs) {
            Order order = getOrderInfo(child.getId());
            result.add(order);
        }

        return result;

    }

    @Override
    public Result updateOrderItem(String orderId, Integer count, String orderItemId, Integer type) {
        Result result = new Result();
        Order order = orderMapper.selectByPrimaryKey(orderId);
        BrandSetting setting = brandSettingService.selectByBrandId(order.getBrandId());
        if (type == 0) { //如果要修改的是服务费
            order.setCustomerCount(count);
            order.setPaymentAmount(order.getPaymentAmount().subtract(order.getServicePrice()));
            if (order.getAmountWithChildren().doubleValue() > 0) {
                order.setAmountWithChildren(order.getAmountWithChildren().subtract(order.getServicePrice()));
            }
            order.setOrderMoney(order.getOrderMoney().subtract(order.getServicePrice()));
            order.setOriginalAmount(order.getOriginalAmount().subtract(order.getServicePrice()));
            order.setServicePrice(setting.getServicePrice().multiply(new BigDecimal(count)));
            order.setPaymentAmount(order.getPaymentAmount().add(order.getServicePrice()));
            order.setOrderMoney(order.getOrderMoney().add(order.getServicePrice()));
            if (order.getAmountWithChildren().doubleValue() > 0) {
                order.setAmountWithChildren(order.getAmountWithChildren().add(order.getServicePrice()));
            }
            order.setOriginalAmount(order.getOriginalAmount().add(order.getServicePrice()));

            update(order);
        } else { //修改的是菜品


            OrderItem orderItem = orderItemService.selectById(orderItemId); //找到要修改的菜品
            if (orderItem.getType() == OrderItemType.MEALS_CHILDREN) {
                result.setSuccess(false);
                result.setMessage("套餐子品暂不支持修改");
                return result;
            }
            order.setArticleCount(order.getArticleCount() - orderItem.getCount());

            if (order.getParentOrderId() == null) {
                if (order.getArticleCount() == 0 && count == 0) {
                    result.setSuccess(false);
                    result.setMessage("菜品数量不可为空");
                    return result;
                }
            }

            order.setOrderMoney(order.getOrderMoney().subtract(orderItem.getFinalPrice()));
            order.setOriginalAmount(order.getOriginalAmount().subtract(orderItem.getFinalPrice()));
            order.setPaymentAmount(order.getPaymentAmount().subtract(orderItem.getFinalPrice()));
            if (order.getAmountWithChildren().doubleValue() > 0) {
                order.setAmountWithChildren(order.getAmountWithChildren().subtract(orderItem.getFinalPrice()));
            }

            orderItem.setCount(count);
            orderItem.setFinalPrice(orderItem.getUnitPrice().multiply(new BigDecimal(count)));
            orderitemMapper.updateByPrimaryKeySelective(orderItem);
            order.setArticleCount(order.getArticleCount() + orderItem.getCount());
            order.setOrderMoney(order.getOrderMoney().add(orderItem.getFinalPrice()));
            order.setOriginalAmount(order.getOriginalAmount().add(orderItem.getFinalPrice()));
            order.setPaymentAmount(order.getPaymentAmount().add(orderItem.getFinalPrice()));
            if (order.getAmountWithChildren().doubleValue() > 0) {
                order.setAmountWithChildren(order.getAmountWithChildren().add(orderItem.getFinalPrice()));
            }

            if (orderItem.getCount() == 0) {
                orderitemMapper.deleteByPrimaryKey(orderItem.getId());
            }

            update(order);

        }

        if (order.getOrderMode() == ShopMode.HOUFU_ORDER) {
            if (order.getParentOrderId() != null) {  //子订单
                Order parent = selectById(order.getParentOrderId());
                int articleCountWithChildren = selectArticleCountById(parent.getId(), order.getOrderMode());
                if (parent.getLastOrderTime() == null || parent.getLastOrderTime().getTime() < order.getCreateTime().getTime()) {
                    parent.setLastOrderTime(order.getCreateTime());
                }
                Double amountWithChildren = orderMapper.selectParentAmount(parent.getId(), parent.getOrderMode());
                parent.setCountWithChild(articleCountWithChildren);
                parent.setAmountWithChildren(new BigDecimal(amountWithChildren));
                update(parent);

            }
        }
        result.setSuccess(true);
        Customer customer = customerService.selectById(order.getCustomerId());
        WechatConfig config = wechatConfigService.selectByBrandId(customer.getBrandId());

        Order parent = null;
        if (order.getParentOrderId() != null) {
            parent = orderMapper.selectByPrimaryKey(order.getParentOrderId());
        } else {
            parent = order;
        }

        StringBuffer msg = new StringBuffer();
        msg.append("您的订单信息已被商家确认!" + "\n");
        msg.append("订单编号:" + parent.getSerialNumber() + "\n");
        msg.append("桌号：" + parent.getTableNumber() + "\n");
        msg.append("就餐店铺：" + shopDetailService.selectById(parent.getShopDetailId()).getName() + "\n");
        msg.append("订单时间：" + DateFormatUtils.format(parent.getCreateTime(), "yyyy-MM-dd HH:mm") + "\n");
        if (setting.getIsUseServicePrice() == 1) {
            msg.append(setting.getServiceName() + "：" + parent.getServicePrice() + "\n");
        }
        BigDecimal sum = parent.getOrderMoney();
        List<Order> orders = selectByParentId(parent.getId()); //得到子订单
        for (Order child : orders) { //遍历子订单
            sum = sum.add(child.getOrderMoney());
        }
        msg.append("订单明细：\n");
        List<OrderItem> orderItem = orderItemService.listByOrderId(parent.getId());
        if (order.getParentOrderId() != null) {
            List<OrderItem> child = orderItemService.listByParentId(order.getParentOrderId());
            for (OrderItem item : child) {
                order.setOriginalAmount(order.getOriginalAmount().add(item.getFinalPrice()));
                order.setPaymentAmount(order.getPaymentAmount().add(item.getFinalPrice()));
            }
            orderItem.addAll(child);
        }

        for (OrderItem item : orderItem) {
            msg.append("  " + item.getArticleName() + "x" + item.getCount() + "\n");
        }
        msg.append("订单金额：" + sum + "\n");
        WeChatUtils.sendCustomerMsg(msg.toString(), customer.getWechatId(), config.getAppid(), config.getAppsecret());
        return result;
    }

    @Override
    public void refundArticle(Order order) {
        List<OrderPaymentItem> payItemsList = orderPaymentItemService.selectByOrderId(order.getId());
        //退款完成后变更订单项
        Order o = getOrderInfo(order.getId());
        ShopDetail shopDetail = shopDetailService.selectByPrimaryKey(o.getShopDetailId());
        Customer customer = customerService.selectById(o.getCustomerId());
        int refundMoney = order.getRefundMoney().multiply(new BigDecimal(100)).intValue();

        BigDecimal maxWxRefund = new BigDecimal(0);
        for (OrderPaymentItem item : payItemsList) {
            if (item.getPaymentModeId() == PayMode.WEIXIN_PAY) {
                maxWxRefund = maxWxRefund.add(item.getPayValue());
            }
            if (item.getPaymentModeId() == PayMode.ALI_PAY) {
                maxWxRefund = maxWxRefund.add(item.getPayValue());
            }
        }

        if (maxWxRefund.doubleValue() > 0) { //如果微信支付或者支付宝还有钱可以退
            for (OrderPaymentItem item : payItemsList) {
                String newPayItemId = ApplicationUtils.randomUUID();
                switch (item.getPaymentModeId()) {
                    case PayMode.WEIXIN_PAY:
                        if (item.getPayValue().doubleValue() > 0) {
                            WechatConfig config = wechatConfigService.selectByBrandId(DataSourceContextHolder.getDataSourceName());
                            JSONObject obj = new JSONObject(item.getResultData());
                            Map<String, String> result = new HashMap<>();
                            int total = obj.getInt("total_fee");
                            int maxWxPay = maxWxRefund.multiply(new BigDecimal(100)).intValue();
                            if (shopDetail.getWxServerId() == null) {
                                result = WeChatPayUtils.refund(newPayItemId, obj.getString("transaction_id"), total
                                        , maxWxPay > refundMoney ? refundMoney : maxWxPay
                                        , StringUtils.isEmpty(shopDetail.getAppid()) ? config.getAppid() : shopDetail.getAppid(),
                                        StringUtils.isEmpty(shopDetail.getMchid()) ? config.getMchid() : shopDetail.getMchid(),
                                        StringUtils.isEmpty(shopDetail.getMchkey()) ? config.getMchkey() : shopDetail.getMchkey(),
                                        StringUtils.isEmpty(shopDetail.getPayCertPath()) ? config.getPayCertPath() : shopDetail.getPayCertPath());
                            } else {
                                WxServerConfig wxServerConfig = wxServerConfigService.selectById(shopDetail.getWxServerId());

                                result = WeChatPayUtils.refundNew(newPayItemId, obj.getString("transaction_id"),
                                        total, maxWxPay > refundMoney ? refundMoney : maxWxPay, wxServerConfig.getAppid(), wxServerConfig.getMchid(),
                                        StringUtils.isEmpty(shopDetail.getMchid()) ? config.getMchid() : shopDetail.getMchid(), wxServerConfig.getMchkey(), wxServerConfig.getPayCertPath());
                            }

                            BigDecimal realBack = maxWxRefund.doubleValue() > order.getRefundMoney().doubleValue() ? order.getRefundMoney() : maxWxRefund;
                            item.setResultData(new JSONObject(result).toString());
                            item.setId(newPayItemId);
                            item.setPayValue(realBack.multiply(new BigDecimal(-1)));
                            orderPaymentItemService.insert(item);
                            if (maxWxPay < refundMoney) { //如果要退款的金额 比实际微信支付要大
                                int charge = refundMoney - maxWxPay;
                                BigDecimal wxBack = new BigDecimal(maxWxPay).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP);
                                BigDecimal backMoney = new BigDecimal(charge).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP);
                                OrderPaymentItem back = new OrderPaymentItem();
                                back.setId(ApplicationUtils.randomUUID());
                                back.setOrderId(order.getId());
                                back.setPaymentModeId(PayMode.ARTICLE_BACK_PAY);
                                back.setPayTime(new Date());
                                back.setPayValue(new BigDecimal(-1).multiply(backMoney));
                                back.setRemark("退菜红包:" + backMoney);

                                back.setResultData("总退款金额" + order.getRefundMoney() + ",微信支付返回" + wxBack + ",余额返回" + backMoney);
                                orderPaymentItemService.insert(back);
                                accountService.addAccount(backMoney, customer.getAccountId(), "退菜红包", PayMode.ACCOUNT_PAY);
                            }

                        }
                        break;
                    case PayMode.ALI_PAY:
                        if (item.getPayValue().doubleValue() > 0) {
                            BigDecimal refundTotal = maxWxRefund.doubleValue() > order.getRefundMoney().doubleValue() ?
                                    order.getRefundMoney() : maxWxRefund;

                            BrandSetting brandSetting = brandSettingService.selectByBrandId(o.getBrandId());
                            AliPayUtils.connection(StringUtils.isEmpty(shopDetail.getAliAppId()) ? brandSetting.getAliAppId() : shopDetail.getAliAppId().trim(),
                                    StringUtils.isEmpty(shopDetail.getAliPrivateKey()) ? brandSetting.getAliPrivateKey().trim() : shopDetail.getAliPrivateKey().trim(),
                                    StringUtils.isEmpty(shopDetail.getAliPublicKey()) ? brandSetting.getAliPublicKey().trim() : shopDetail.getAliPublicKey().trim());
                            Map map = new HashMap();
                            map.put("out_trade_no", o.getId());
                            map.put("refund_amount", refundTotal);
                            map.put("out_request_no", newPayItemId);
                            String resultJson = AliPayUtils.refundPay(map);
                            item.setId(newPayItemId);
                            item.setResultData(new JSONObject(resultJson).toString());
                            item.setPayValue(refundTotal.multiply(new BigDecimal(-1)));
                            orderPaymentItemService.insert(item);
                            if (maxWxRefund.doubleValue() < order.getRefundMoney().doubleValue()) { //如果最大退款金额 比实际要退的小
                                BigDecimal backMoney = order.getRefundMoney().subtract(maxWxRefund);
                                OrderPaymentItem back = new OrderPaymentItem();
                                back.setId(ApplicationUtils.randomUUID());
                                back.setOrderId(order.getId());
                                back.setPaymentModeId(PayMode.ARTICLE_BACK_PAY);
                                back.setPayTime(new Date());
                                back.setPayValue(new BigDecimal(-1).multiply(backMoney));
                                back.setRemark("退菜红包:" + backMoney);

                                back.setResultData("总退款金额" + order.getRefundMoney() + ",支付宝支付返回" + refundTotal + ",余额返回" + backMoney);
                                orderPaymentItemService.insert(back);
                                accountService.addAccount(backMoney, customer.getAccountId(), "退菜红包", PayMode.ACCOUNT_PAY);
                            }

                        }
                        break;

                }

            }
        } else {
            OrderPaymentItem back = new OrderPaymentItem();
            back.setId(ApplicationUtils.randomUUID());
            back.setOrderId(order.getId());
            back.setPaymentModeId(PayMode.ARTICLE_BACK_PAY);
            back.setPayTime(new Date());
            back.setPayValue(new BigDecimal(-1).multiply(order.getRefundMoney()));
            back.setRemark("退菜红包:" + order.getRefundMoney());

            back.setResultData("总退款金额" + order.getRefundMoney() + "余额返回" + order.getRefundMoney());
            orderPaymentItemService.insert(back);
            accountService.addAccount(order.getRefundMoney(), customer.getAccountId(), "退菜红包", PayMode.ACCOUNT_PAY);
        }


    }

    @Override
    public void refundItem(Order order) {
        Order o = getOrderInfo(order.getId());
        int customerCount = 0;
        BigDecimal servicePrice = new BigDecimal(0);
        BrandSetting setting = brandSettingService.selectByBrandId(o.getBrandId());
        for (OrderItem orderItem : order.getOrderItems()) {
            if (orderItem.getType().equals(ArticleType.ARTICLE)) {
                OrderItem item = orderitemMapper.selectByPrimaryKey(orderItem.getId());
                orderitemMapper.refundArticle(orderItem.getId(), orderItem.getCount());
                if (item.getType() == OrderItemType.SETMEALS) {
                    //如果退了套餐，清空子品
                    orderitemMapper.refundArticleChild(orderItem.getId());
                }

            } else if (orderItem.getType().equals(ArticleType.SERVICE_PRICE)) {
                customerCount = o.getCustomerCount() - orderItem.getCount();
                servicePrice = setting.getServicePrice().multiply(new BigDecimal(customerCount));
                orderMapper.refundServicePrice(o.getId(), servicePrice, customerCount);
            }
        }

    }

    @Override
    public boolean checkOrder(Order order) {
        List<OrderPaymentItem> payItemsList = orderPaymentItemService.selectByOrderId(order.getId());
        BigDecimal sum = new BigDecimal(0);
        for (OrderPaymentItem item : payItemsList) {
            if (item.getPaymentModeId() == PayMode.WEIXIN_PAY) {
                sum = sum.add(item.getPayValue());
            }
        }

        return order.getRefundMoney().doubleValue() <= sum.doubleValue();
    }


    @Override
    public void updateArticle(Order order) {
        BigDecimal total = new BigDecimal(0);
        BigDecimal origin = new BigDecimal(0);
        Order o = getOrderInfo(order.getId());
        ShopDetail shopDetail = shopDetailService.selectByPrimaryKey(o.getShopDetailId());
        BrandSetting brandSetting = brandSettingService.selectByBrandId(o.getBrandId());
        int base = 0;
        int sum = 0;
        BigDecimal mealPrice = BigDecimal.valueOf(0);
        int mealCount = 0;
        BigDecimal mealTotalPrice = BigDecimal.valueOf(0);
        for (OrderItem item : o.getOrderItems()) {
            origin = origin.add(item.getOriginalPrice().multiply(BigDecimal.valueOf(item.getCount())));
            total = total.add(item.getFinalPrice());
            if (o.getDistributionModeId() == DistributionType.TAKE_IT_SELF && brandSetting.getIsMealFee() == Common.YES && shopDetail.getIsMealFee() == Common.YES) {
                mealPrice = shopDetail.getMealFeePrice().multiply(new BigDecimal(item.getCount())).multiply(new BigDecimal(item.getMealFeeNumber())).setScale(2, BigDecimal.ROUND_HALF_UP);
                ;
                mealTotalPrice = mealTotalPrice.add(mealPrice);
                mealCount += item.getCount() * item.getMealFeeNumber();
                total = total.add(mealPrice);
            }
            if (item.getRefundCount() > 0 && item.getType() != OrderItemType.MEALS_CHILDREN) {
                sum += item.getRefundCount();

            }
            if (item.getType() != OrderItemType.MEALS_CHILDREN) {
                base += item.getOrginCount();
            }


        }

        if (o.getServicePrice() == null) {
            o.setServicePrice(new BigDecimal(0));
        }
        o.setMealFeePrice(mealTotalPrice);
        o.setMealAllNumber(mealCount);
        o.setArticleCount(base - sum);
//        o.setPaymentAmount(total.add(o.getServicePrice()));
        o.setOriginalAmount(origin.add(o.getServicePrice()));
        o.setOrderMoney(total.add(o.getServicePrice()));
        if (o.getAmountWithChildren() != null && o.getAmountWithChildren().doubleValue() != 0.0) {
            o.setAmountWithChildren(o.getAmountWithChildren().subtract(order.getRefundMoney()));
        }
        if (o.getParentOrderId() != null) {
            Order parent = selectById(o.getParentOrderId());
            parent.setAmountWithChildren(parent.getAmountWithChildren().subtract(order.getRefundMoney()));
            update(parent);
        }
        update(o);

    }

    @Override
    public void refundArticleMsg(Order order) {
        Order o = getOrderInfo(order.getId());

        Customer customer = customerService.selectById(o.getCustomerId());
        WechatConfig config = wechatConfigService.selectByBrandId(customer.getBrandId());
        ShopDetail shopDetail = shopDetailService.selectByPrimaryKey(o.getShopDetailId());
        StringBuilder msg = new StringBuilder("亲，您");
        msg.append(DateFormatUtils.format(o.getCreateTime(), "yyyy-MM-dd HH:mm")).append("的订单已完成退菜，相关款项")
                .append("会在24小时内退还至您的微信账户，请注意查收！\n");
        msg.append("订单编号:\n");
        msg.append(o.getSerialNumber()).append("\n");
        msg.append("桌号:").append(o.getTableNumber()).append("\n");
        msg.append("就餐店铺:").append(shopDetail.getName()).append("\n");
        msg.append("订单时间:").append(DateFormatUtils.format(o.getCreateTime(), "yyyy-MM-dd HH:mm")).append("\n");
//        msg.append("订单明细:").append("\n");
        BrandSetting brandSetting = brandSettingService.selectByBrandId(o.getBrandId());
//        if (o.getCustomerCount() != null && o.getCustomerCount() != 0) {
//            msg.append("\t").append(brandSetting.getServiceName()).append("X").append(o.getBaseCustomerCount()).append("\n");
//        }
//
//        List<OrderItem> totalItem = new ArrayList<>();
//        List<String> childs = orderMapper.selectChildIdsByParentId(o.getId());
//        if (!CollectionUtils.isEmpty(childs)) {
//            List<OrderItem> item = orderitemMapper.listByOrderIds(childs);
//            totalItem.addAll(item);
//        }
//        totalItem.addAll(o.getOrderItems());
//


//        for (OrderItem orderItem : totalItem) {
//
//
//            if (orderItem.getCount() != 0)
//                msg.append("\t").append(orderItem.getArticleName()).append("X").append(orderItem.getCount()).append("\n");
//        }
//        msg.append("订单金额:").append(o.getAmountWithChildren().doubleValue() != 0.0 ? o.getAmountWithChildren() : o.getOrderMoney()).append("\n");
        msg.append("退菜明细:").append("\n");

//        if (o.getBaseCustomerCount() != null && o.getBaseCustomerCount() != o.getCustomerCount()) {
//            msg.append("\t").append(brandSetting.getServiceName()).append("X").append(o.getBaseCustomerCount() - o.getCustomerCount()).append("\n");
//        }
//        for (OrderItem orderItem : o.getOrderItems()) {
//            if (orderItem.getRefundCount() > 0) {
//                msg.append("\t").append(orderItem.getArticleName()).append("X").append(orderItem.getRefundCount()).append("\n");
//            }
//        }
        for (OrderItem orderItem : order.getOrderItems()) {
            if (orderItem.getType().equals(ArticleType.ARTICLE)) {
                OrderItem item = orderitemMapper.selectByPrimaryKey(orderItem.getId());
                msg.append("\t").append(item.getArticleName()).append("X").append(orderItem.getCount()).append("\n");
                if (item.getType() == OrderItemType.SETMEALS) {
                    List<OrderItem> child = orderitemMapper.getListByParentId(item.getId());
                    for (OrderItem c : child) {
                        //                childItem.setArticleName("|__" + childItem.getArticleName());
                        msg.append("\t").append("|__").append(c.getArticleName()).append("X").append(c.getRefundCount()).append("\n");
                    }
                }

            } else if (orderItem.getType().equals(ArticleType.SERVICE_PRICE)) {
                msg.append("\t").append(brandSetting.getServiceName()).append("X").append(orderItem.getCount()).append("\n");
            }
        }
        msg.append("退菜金额:").append(order.getRefundMoney()).append("\n");
        WeChatUtils.sendCustomerMsg(msg.toString(), customer.getWechatId(), config.getAppid(), config.getAppsecret());
    }

    @Override
    public List<Order> selectWXOrderItems(Map<String, Object> map) {
        return orderMapper.selectWXOrderItems(map);
    }


    /**
     * 会员管理
     * 1订单管理
     */
    @Override
    public List<Order> getCustomerOrderList(String customerId, String beginDate, String endDate) {
        return orderMapper.getCustomerOrderList(customerId, beginDate, endDate);
    }

    @Override
    public Integer selectByCustomerCount(String customerId, int consumeConfineUnit, int consumeConfineTime) {
        return orderMapper.selectByCustomerCount(customerId, consumeConfineUnit, consumeConfineTime);
    }

    @Override
    public List<Order> selectOrderByOrderIds(Map<String, Object> orderIds) {
        return orderMapper.selectOrderByOrderIds(orderIds);
    }

    @Override
    public Map<String, Object> refundOrderPrintReceipt(Order refundOrder) {
        // 根据id查询订单
        Order order = selectById(refundOrder.getId());
        order.setDistributionModeId(DistributionType.REFUND_ORDER);
        order.setBaseCustomerCount(0);
        order.setRefundMoney(refundOrder.getRefundMoney());
        //如果是 未打印状态 或者  异常状态则改变 生产状态和打印时间
        if (ProductionStatus.HAS_ORDER == order.getProductionStatus() || ProductionStatus.NOT_PRINT == order.getProductionStatus()) {
            order.setProductionStatus(ProductionStatus.PRINTED);
            order.setPrintOrderTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
        }
        //查询店铺
        ShopDetail shopDetail = shopDetailService.selectById(order.getShopDetailId());
        // 查询订单菜品
        Map<String, Object> map = new HashMap<String, Object>();
        List<String> orderItemIds = new ArrayList<String>();
        for (OrderItem item : refundOrder.getOrderItems()) {
            if (!item.getType().equals(ArticleType.SERVICE_PRICE)) {
                orderItemIds.add(item.getId());
            } else {
                order.setBaseCustomerCount(item.getCount());
                order.setCustomerCount(0);
            }
        }
        map.put("orderItemIds", orderItemIds);
        List<OrderItem> orderItems = new ArrayList<OrderItem>();
        if (orderItemIds.size() != 0) {
            orderItems = orderItemService.selectRefundOrderItem(map);
        }
        List<Printer> printer = printerService.selectByShopAndType(shopDetail.getId(), PrinterType.RECEPTION);
        if (printer.size() > 0) {
            return refundOrderPrintTicket(order, orderItems, shopDetail, printer.get(0));
        }
        return null;
    }

    @Override
    public List<Map<String, Object>> refundOrderPrintKitChen(Order refundOrder) {
        Order order = selectById(refundOrder.getId());
        order.setDistributionModeId(DistributionType.REFUND_ORDER);
        //如果是 未打印状态 或者  异常状态则改变 生产状态和打印时间
        if (ProductionStatus.HAS_ORDER == order.getProductionStatus() || ProductionStatus.NOT_PRINT == order.getProductionStatus()) {
            order.setProductionStatus(ProductionStatus.PRINTED);
            order.setPrintOrderTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
        }
        //得到退掉的订单明细
        List<String> orderItemIds = new ArrayList<String>();
        for (OrderItem item : refundOrder.getOrderItems()) {
            orderItemIds.add(item.getId());
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("orderItemIds", orderItemIds);
        List<OrderItem> items = orderItemService.selectRefundOrderItem(map);
        for (OrderItem item : items) {
            if (item.getType() == OrderItemType.SETMEALS) {
                List<OrderItem> list = orderitemMapper.getListBySort(item.getId(), item.getArticleId());
                item.setChildren(list);
            }
        }

        //生成打印任务
        List<Map<String, Object>> printTask = new ArrayList<>();
        //得到打印任务
        order.setIsRefund(Common.YES);
        List<Map<String, Object>> kitchenTicket = printKitchen(order, items);
        if (!kitchenTicket.isEmpty()) {
            printTask.addAll(kitchenTicket);
        }
        return printTask;
    }

    public Map<String, Object> refundOrderPrintTicket(Order order, List<OrderItem> orderItems, ShopDetail shopDetail, Printer printer) {
        if (printer == null) {
            return null;
        }
        List<Map<String, Object>> refundItems = new ArrayList<>();
        BigDecimal articleCount = new BigDecimal(0);
        BigDecimal orderMoney = new BigDecimal(0);
        for (OrderItem article : orderItems) {
            Map<String, Object> refundItem = new HashMap<>();
            refundItem.put("SUBTOTAL", -article.getUnitPrice().multiply(new BigDecimal(article.getRefundCount())).doubleValue());
            refundItem.put("ARTICLE_NAME", article.getArticleName() + "(退)");
            refundItem.put("ARTICLE_COUNT", -article.getRefundCount());
            refundItems.add(refundItem);
            articleCount = articleCount.add(new BigDecimal(article.getRefundCount()));
            orderMoney = orderMoney.add(article.getUnitPrice().multiply(new BigDecimal(article.getRefundCount())));
            if (article.getType() != OrderItemType.MEALS_CHILDREN && order.getBaseMealAllCount() != null && order.getBaseMealAllCount() != 0) {
                refundItem = new HashMap<>();
                refundItem.put("SUBTOTAL", -shopDetail.getMealFeePrice().multiply(
                        new BigDecimal(article.getRefundCount()).multiply(new BigDecimal(article.getMealFeeNumber()))).doubleValue());
                refundItem.put("ARTICLE_NAME", shopDetail.getMealFeeName() + "(退)");
                refundItem.put("ARTICLE_COUNT", -(new BigDecimal(article.getRefundCount()).multiply(new BigDecimal(article.getMealFeeNumber()))).doubleValue());
                refundItems.add(refundItem);
                articleCount = articleCount.add(new BigDecimal(article.getRefundCount()).multiply(new BigDecimal(article.getMealFeeNumber())));
                orderMoney = orderMoney.add(shopDetail.getMealFeePrice().multiply(
                        new BigDecimal(article.getRefundCount()).multiply(new BigDecimal(article.getMealFeeNumber()))));
            }
        }
        BrandSetting brandSetting = brandSettingService.selectByBrandId(order.getBrandId());
        Brand brand = brandService.selectBrandBySetting(brandSetting.getId());

        if (order.getBaseCustomerCount() != null && order.getBaseCustomerCount() != 0
                && StringUtils.isBlank(order.getParentOrderId())) {
            Map<String, Object> refundItem = new HashMap<>();
            refundItem.put("SUBTOTAL", -brandSetting.getServicePrice().multiply(new BigDecimal((order.getBaseCustomerCount() - order.getCustomerCount()))).doubleValue());
            refundItem.put("ARTICLE_NAME", brandSetting.getServiceName() + "(退)");
            if ("27f56b31669f4d43805226709874b530".equals(brand.getId())) {
                refundItem.put("ARTICLE_NAME", "就餐人数" + "(退)");
            }
            refundItem.put("ARTICLE_COUNT", -(order.getBaseCustomerCount() - order.getCustomerCount()));
            refundItems.add(refundItem);
            articleCount = articleCount.add(new BigDecimal(order.getBaseCustomerCount() - order.getCustomerCount()));
            orderMoney = orderMoney.add(brandSetting.getServicePrice().multiply(new BigDecimal((order.getBaseCustomerCount() - order.getCustomerCount()))));
        }
        Map<String, Object> print = new HashMap<>();
        String tableNumber = order.getTableNumber() != null ? order.getTableNumber() : "";
        print.put("TABLE_NO", tableNumber);
        print.put("KITCHEN_NAME", printer.getName());
        print.put("PORT", printer.getPort());
        print.put("ORDER_ID", order.getSerialNumber());
        print.put("IP", printer.getIp());
        String print_id = ApplicationUtils.randomUUID();
        print.put("PRINT_TASK_ID", print_id);
        print.put("ADD_TIME", new Date());

        Map<String, Object> data = new HashMap<>();
        data.put("ORDER_ID", order.getSerialNumber() + "-" + order.getVerCode());
        data.put("ORDER_NUMBER", nextNumber(order.getShopDetailId(), order.getId()));
        data.put("ITEMS", refundItems);
        List<Map<String, Object>> patMentItems = new ArrayList<Map<String, Object>>();
        Map<String, Object> payMentItem = new HashMap<String, Object>();
        payMentItem.put("SUBTOTAL", -order.getRefundMoney().doubleValue());
        payMentItem.put("PAYMENT_MODE", "退菜返还金额");
        patMentItems.add(payMentItem);
        data.put("PAYMENT_ITEMS", patMentItems);
        Appraise appraise = appraiseService.selectAppraiseByCustomerId(order.getCustomerId(), order.getShopDetailId());
        StringBuilder star = new StringBuilder();
        BigDecimal level = new BigDecimal(0);
        if (appraise != null) {
            if (appraise != null && appraise.getLevel() < 5) {
                for (int i = 0; i < appraise.getLevel(); i++) {
                    star.append("★");
                }
                for (int i = 0; i < 5 - appraise.getLevel(); i++) {
                    star.append("☆");
                }
            } else if (appraise != null && appraise.getLevel() == 5) {
                star.append("★★★★★");
            }
            Map<String, Object> appriseCount = appraiseService.selectCustomerAppraiseAvg(order.getCustomerId());
            level = new BigDecimal(Integer.valueOf(appriseCount.get("sum").toString()))
                    .divide(new BigDecimal(Integer.valueOf(appriseCount.get("count").toString())), 2, BigDecimal.ROUND_HALF_UP);
        } else {
            star.append("☆☆☆☆☆");
        }
        StringBuilder gao = new StringBuilder();
        if (shopDetail.getIsUserIdentity().equals(1)) {
            //得到有限制的情况下用户的订单数
            int gaoCount = orderMapper.selectByCustomerCount(order.getCustomerId(), shopDetail.getConsumeConfineUnit(), shopDetail.getConsumeConfineTime());
            //得到无限制情况下用户的订单数
            int gaoCountlong = orderMapper.selectByCustomerCount(order.getCustomerId(), shopDetail.getConsumeConfineUnit(), 0);
            if (shopDetail.getConsumeNumber() > 0 && gaoCount > shopDetail.getConsumeNumber() && shopDetail.getConsumeConfineUnit() != 3) {
                gao.append("【高频】");
            }//无限制的时候
            else if (shopDetail.getConsumeConfineUnit() == 3 && gaoCountlong > shopDetail.getConsumeNumber()) {
                gao.append("【高频】");
            }
        }
        String modeText = getModeText(order);
        data.put("DISTRIBUTION_MODE", modeText);
        data.put("ORIGINAL_AMOUNT", -orderMoney.doubleValue());
        data.put("RESTAURANT_ADDRESS", shopDetail.getAddress());
        data.put("REDUCTION_AMOUNT", 0);
        data.put("RESTAURANT_TEL", shopDetail.getPhone());
        data.put("TABLE_NUMBER", order.getTableNumber());
        data.put("CUSTOMER_COUNT", order.getCustomerCount() == null ? "-" : order.getCustomerCount());
        data.put("PAYMENT_AMOUNT", orderMoney.subtract(order.getRefundMoney()));
        data.put("RESTAURANT_NAME", shopDetail.getName());
        data.put("DATETIME", DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
        data.put("ARTICLE_COUNT", -articleCount.intValue());
        data.put("CUSTOMER_SATISFACTION", star.toString());
        data.put("CUSTOMER_SATISFACTION_DEGREE", level);
        Account account = accountService.selectAccountAndLogByCustomerId(order.getCustomerId());
        StringBuffer customerStr = new StringBuffer();
        if (account != null) {
            customerStr.append("余额：" + account.getRemain() + " ");
        } else {
            customerStr.append("余额：0 ");
        }
        customerStr.append("" + gao.toString() + " ");
        Customer customer = customerService.selectById(order.getCustomerId());
        CustomerDetail customerDetail = customerDetailMapper.selectByPrimaryKey(customer.getCustomerDetailId());
        if (customerDetail != null) {
            if (customerDetail.getBirthDate() != null) {
                if (DateUtil.formatDate(customerDetail.getBirthDate(), "MM-dd")
                        .equals(DateUtil.formatDate(new Date(), "MM-dd"))) {
                    customerStr.append("★" + DateUtil.formatDate(customerDetail.getBirthDate(), "yyyy-MM-dd") + "★");
                }
            }
        }
        data.put("CUSTOMER_PROPERTY", customerStr.toString());
        print.put("DATA", data);
        print.put("STATUS", 0);
        print.put("TICKET_TYPE", TicketType.RECEIPT);
        return print;
    }

    public static void main(String[] args) {
        BigDecimal a = new BigDecimal(0.00);
        System.out.println(a.equals(BigDecimal.ZERO));
    }
}
