package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resto.brand.core.entity.JSONResult;
import com.resto.brand.core.entity.Result;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.*;
import com.resto.brand.web.dto.*;
import com.resto.brand.web.model.*;
import com.resto.brand.web.model.TableQrcode;
import com.resto.brand.web.service.*;
import com.resto.brand.web.service.TableQrcodeService;
import com.resto.shop.web.constant.*;
import com.resto.shop.web.container.OrderProductionStateContainer;
import com.resto.shop.web.dao.*;
import com.resto.shop.web.datasource.DataSourceContextHolder;
import com.resto.shop.web.exception.AppException;
import com.resto.shop.web.model.*;
import com.resto.shop.web.model.Employee;
import com.resto.shop.web.producer.MQMessageProducer;
import com.resto.shop.web.service.*;
import com.resto.shop.web.util.JdbcSmsUtils;
import com.resto.shop.web.service.OrderRemarkService;
import com.resto.shop.web.util.LogTemplateUtils;
import com.resto.shop.web.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.resto.brand.core.util.HttpClient.doPostAnsc;
import static com.resto.brand.core.util.LogUtils.url;
import static com.resto.brand.core.util.OrderCountUtils.formatDouble;
import static com.resto.brand.core.util.OrderCountUtils.getOrderMoney;

/**
 *
 */
@RpcService
@Component
public class OrderServiceImpl extends GenericServiceImpl<Order, String> implements OrderService {

    //用来添加打印小票的序号
    //添加两个Map 一个是订单纬度,一个是店铺纬度
    private static final Map<String, Map<String, Integer>> NUMBER_ORDER_MAP = new ConcurrentHashMap<>();

    private static final Map<String, Map<String, Integer>> NUMBER_SHOP_MAP = new ConcurrentHashMap<>();

    private static final String NUMBER = "0123456789";

    private static final List<String> orderList = new ArrayList<>();

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private OrderItemMapper orderitemMapper;

    @Resource
    private RedPacketService redPacketService;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Resource
    private CustomerService customerService;

    @Resource
    private ArticleService articleService;

    @Resource
    private ArticlePriceService articlePriceService;

    @Value("#{configProperties2['logPath']}")
    public static String logPath;

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

    @Resource
    private  WetherService wetherService;

    @Autowired
    private CustomerDetailMapper customerDetailMapper;

    @Resource
    private OrderRefundRemarkMapper orderRefundRemarkMapper;

    @Autowired
    private DayDataMessageService dayDataMessageService;

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

    @Autowired
    VirtualProductsService virtualProductsService;

    @Resource
    ArticleTopService articleTopService;

    @Resource
    OrderRemarkService orderRemarkService;

    @Autowired
    private TableQrcodeService tableQrcodeService;

    @Autowired
    private AreaService areaService;

    @Autowired
    private SmsLogService smsLogService;

    @Autowired
    private DayAppraiseMessageService dayAppraiseMessageService;


    Logger log = LoggerFactory.getLogger(getClass());

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


        if (detail.getShopMode() != ShopMode.HOUFU_ORDER) {
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
                BigDecimal payValue = accountService.payOrder(old, payment, customer, null, null);
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


    /**
     * @Author: KONATA
     * @Description
     * @Date: 15:42 2017/3/30
     */
    public JSONResult createOrder(Order order) throws AppException {
        JSONResult jsonResult = new JSONResult();
        String orderId = ApplicationUtils.randomUUID();
        order.setId(orderId);
        Customer customer = customerService.selectById(order.getCustomerId());

        if (customer == null && "wechat".equals(order.getCreateOrderByAddress())) {
            throw new AppException(AppException.CUSTOMER_NOT_EXISTS);
        } else if (order.getOrderItems().isEmpty()) {
            throw new AppException(AppException.ORDER_ITEMS_EMPTY);
        }


        if (!StringUtils.isEmpty(order.getTableNumber()) && order.getTableNumber().length() > 5) {
            jsonResult.setSuccess(false);
            jsonResult.setMessage("桌号异常,请扫码正确的二维码！");
            return jsonResult;
        }

        Brand brand = brandService.selectById(order.getBrandId());
        ShopDetail shopDetail = shopDetailService.selectById(order.getShopDetailId());
        BrandSetting brandSetting = brandSettingService.selectByBrandId(brand.getId());
        if (order.getOrderItems().isEmpty()) {
            throw new AppException(AppException.ORDER_ITEMS_EMPTY);
        }

        if (brandSetting.getIsUseServicePrice() == Common.YES && shopDetail.getIsUseServicePrice() == Common.YES
                && (order.getCustomerCount() == null || order.getCustomerCount() == 0)
                && order.getDistributionModeId() == DistributionType.RESTAURANT_MODE_ID) {
            jsonResult.setSuccess(false);
            jsonResult.setMessage("请输入就餐人数！");
            return jsonResult;
        }


        if (!StringUtils.isEmpty(order.getTableNumber())) { //如果存在桌号
            int orderCount = orderMapper.checkTableNumber(order.getShopDetailId(), order.getTableNumber(), order.getCustomerId(), brandSetting.getCloseContinueTime());
            if (orderCount > 0) {
                jsonResult.setSuccess(false);
                jsonResult.setMessage("不好意思，这桌有人了");
                return jsonResult;
            }
        } else if ((order.getDistributionModeId() == 3 && shopDetail.getContinueOrderScan() == 1 && StringUtils.isEmpty(order.getTableNumber()) && shopDetail.getShopMode() == ShopMode.BOSS_ORDER)
                || order.getDistributionModeId() == 1 && StringUtils.isEmpty(order.getTableNumber()) && shopDetail.getShopMode() == ShopMode.BOSS_ORDER) {
            jsonResult.setSuccess(false);
            jsonResult.setMessage("桌号不得为空");
            return jsonResult;
        }
        if (!StringUtils.isEmpty(order.getParentOrderId())) {  //如果是加菜订单
            Order farOrder = orderMapper.selectByPrimaryKey(order.getParentOrderId());
            if (farOrder.getOrderState() == OrderState.SUBMIT && (farOrder.getPayMode() == OrderPayMode.YL_PAY || farOrder.getPayMode() == OrderPayMode.XJ_PAY ||
                    farOrder.getPayMode() == OrderPayMode.SHH_PAY || farOrder.getPayMode() == OrderPayMode.JF_PAY)) {
                jsonResult.setSuccess(false);
                jsonResult.setMessage("付款中的订单，请等待服务员确认后在进行加菜");
                return jsonResult;
            }
            if (farOrder.getOrderState() == OrderState.SUBMIT && farOrder.getPayType() == PayType.NOPAY && farOrder.getIsPay() == OrderPayState.ALIPAYING) {
                jsonResult.setSuccess(false);
                jsonResult.setMessage("请先支付完选择支付宝支付的订单，再进行加菜！");
                return jsonResult;
            }
            if((farOrder.getOrderState() == OrderState.PAYMENT ||  farOrder.getOrderState() == OrderState.CONFIRM ||
                    farOrder.getOrderState() == OrderState.HASAPPRAISE || farOrder.getOrderState() == OrderState.SHARED)
                    && farOrder.getPayType() == PayType.NOPAY && order.getPayType() == PayType.NOPAY){
                jsonResult.setSuccess(false);
                jsonResult.setMessage("下单失败，订单金额变动，请重新下单！");
                return jsonResult;
            }
        }
//        List<OrderItem> orderItems = new ArrayList<OrderItem>();
        List<Article> articles = articleService.selectList(order.getShopDetailId());
        List<ArticlePrice> articlePrices = articlePriceService.selectList(order.getShopDetailId());
        Map<String, Article> articleMap = ApplicationUtils.convertCollectionToMap(String.class, articles);
        Map<String, ArticlePrice> articlePriceMap = ApplicationUtils.convertCollectionToMap(String.class,
                articlePrices);

        if (customer != null && customer.getTelephone() != null) {
            order.setVerCode(customer.getTelephone().substring(7));
        } else if (customer == null && order.getOrderMode() == ShopMode.CALL_NUMBER && order.getTableNumber() != null) {
            order.setVerCode(order.getTableNumber());
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
        Map map = new HashMap(4);
        map.put("brandName", brand.getBrandName());
        map.put("fileName", order.getId());
        map.put("type", "orderAction");
        map.put("content", "订单:" + order.getId() + "已创建,请求服务器地址为:" + MQSetting.getLocalIP());
        doPostAnsc(url, map);
        if (customer != null) {
            Map customerMap = new HashMap(4);
            customerMap.put("brandName", brand.getBrandName());
            customerMap.put("fileName", customer.getId());
            customerMap.put("type", "UserAction");
            customerMap.put("content", "用户:" + customer.getNickname() + "创建了订单Id为:" + order.getId() + ",请求服务器地址为:" + MQSetting.getLocalIP());
            doPostAnsc(url, customerMap);
        }
        BigDecimal totalMoney = BigDecimal.ZERO;
        BigDecimal originMoney = BigDecimal.ZERO;
        int articleCount = 0;
        BigDecimal extraMoney = BigDecimal.ZERO;

//记录订单菜品-------------------------------
        for (OrderItem item : order.getOrderItems()) {
            Article a = null;
            BigDecimal org_price = null;
            int mealFeeNumber = 0;
            BigDecimal price = null;
            BigDecimal fans_price = null;
            item.setId(ApplicationUtils.randomUUID());
            String remark = "";
            switch (item.getType()) {
                case OrderItemType.ARTICLE://无规格单品
                    // 查出 item对应的 商品信息，并将item的原价，单价，总价，商品名称，商品详情 设置为对应的
                    a = articleMap.get(item.getArticleId());
                    item.setArticleName(a.getName());
                    org_price = a.getPrice();
                    price = discount(a.getPrice(), a.getDiscount(), item.getDiscount(), a.getName());                      //计算折扣
                    if (a.getDiscount() != 100) {
                        fans_price = discount(a.getPrice(), a.getDiscount(), item.getDiscount(), a.getName());       //计算折扣 （update：粉丝价 更改为 原价*折扣  2017年4月18日 14:08:04  ---lmx）
                    } else {
                        fans_price = a.getFansPrice();
                    }
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
                    if (a.getDiscount() != 100) {
                        fans_price = discount(p.getPrice(), a.getDiscount(), item.getDiscount(), p.getName());       //计算折扣 （update：粉丝价 更改为 原价*折扣  2017年4月18日 14:08:04  ---lmx）
                    } else {
                        fans_price = p.getFansPrice();
                    }
                    remark = a.getDiscount() + "%";          //设置菜品当前折扣
                    mealFeeNumber = a.getMealFeeNumber() == null ? 0 : a.getMealFeeNumber();
                    break;
                case OrderItemType.UNIT_NEW://新规格
                    //判断折扣是否匹配，如果不匹配则不允许买单
                    a = articleMap.get(item.getArticleId());
                    item.setArticleName(item.getName());
                    if (a.getFansPrice() != null) {
                        org_price = item.getPrice().subtract(a.getFansPrice()).add(a.getPrice());
                    } else {
                        org_price = item.getPrice();
                    }
                    price = item.getPrice();
                    fans_price = item.getPrice();
                    mealFeeNumber = a.getMealFeeNumber() == null ? 0 : a.getMealFeeNumber();
                    break;
                case OrderItemType.SETMEALS://套餐主品
                    a = articleMap.get(item.getArticleId());
                    if (a.getIsEmpty()) {
                        jsonResult.setSuccess(false);
                        jsonResult.setMessage("菜品供应时间变动，请重新购买");
                        return jsonResult;
                    }
                    item.setArticleName(a.getName());
                    org_price = a.getPrice();
                    price = discount(a.getPrice(), a.getDiscount(), item.getDiscount(), a.getName());
                    if (a.getDiscount() != 100) {
                        fans_price = discount(a.getPrice(), a.getDiscount(), item.getDiscount(), a.getName());  //计算折扣 （update：粉丝价 更改为 原价*折扣  2017年4月18日 14:08:04  ---lmx）
                    } else {
                        fans_price = a.getFansPrice();
                    }
                    remark = a.getDiscount() + "%";//设置菜品当前折扣
                    Integer[] mealItemIds = item.getMealItems();
                    List<MealAttr> mealAttrs = mealAttrMapper.selectList(item.getArticleId());
                    boolean checkMeal = true;
                    for (MealAttr mealAttr : mealAttrs) {
                        if (mealAttr.getChoiceType() == 0) {
                            //必选
                            List<MealItem> mealItems = mealItemService.selectByAttrId(mealAttr.getId());
                            //找到这个属性下所有的菜品
                            int count = 0;
                            for (MealItem mealItem : mealItems) {
                                Integer redisCount = (Integer) RedisUtil.get(mealItem.getArticleId() + Common.KUCUN);
                                if (redisCount == null) {
                                    Article article = articleService.selectById(mealItem.getArticleId());
                                    redisCount = article.getCurrentWorkingStock();
                                }
                                if (redisCount > 0) {
                                    count++;
                                }
                            }
                            if (count < mealAttr.getChoiceCount()) {
                                checkMeal = false;
                            }
                        }
                    }
                    if (!checkMeal) {
                        jsonResult.setSuccess(false);
                        jsonResult.setMessage("万分抱歉,您购买的套餐" + item.getName() + "已售罄,请重新下单");
                        articleService.setEmpty(item.getArticleId());
                        if (customer != null) {
                            shopCartService.deleteCustomerArticle(customer.getId(), item.getArticleId());
                        }
                        return jsonResult;
                    }
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
                        if (finalMoney != null && finalMoney.doubleValue() > 0) {
                            extraMoney = extraMoney.add(finalMoney);
                        }
                        child.setFinalPrice(finalMoney);
                        child.setOrderId(orderId);
                        totalMoney = totalMoney.add(finalMoney).setScale(2, BigDecimal.ROUND_HALF_UP);
                        item.getChildren().add(child);
                    }
                    break;
                default:
                    throw new AppException(AppException.UNSUPPORT_ITEM_TYPE, "不支持的餐品类型:" + item.getType());
            }
            if (!a.getShopDetailId().equals(order.getShopDetailId())) {
                jsonResult.setSuccess(false);
                jsonResult.setMessage("门店选择错误，请尝试扫描桌号二维码进行点餐！");
                return jsonResult;
            }
            item.setMealFeeNumber(mealFeeNumber);
            item.setArticleDesignation(a.getDescription());
            item.setOriginalPrice(org_price);
            item.setStatus(1);
            item.setSort(0);
            if (remark.equals("0%")) {
                jsonResult.setSuccess(false);
                jsonResult.setMessage(a.getName() + "供应时间发生改变，请重新购买");
                return jsonResult;
            }
            item.setRemark(remark);
            if (fans_price != null && "pos".equals(order.getCreateOrderByAddress()) && shopDetail.getPosPlusType() == 1) {
                item.setUnitPrice(price);
            } else if (fans_price != null && "pos".equals(order.getCreateOrderByAddress()) && shopDetail.getPosPlusType() != 1) {
                item.setUnitPrice(fans_price);
            } else if (fans_price != null && "wechat".equals(order.getCreateOrderByAddress())) {
                item.setUnitPrice(fans_price);
            } else {
                item.setUnitPrice(price);
            }
            BigDecimal finalMoney = item.getUnitPrice().multiply(new BigDecimal(item.getCount())).setScale(2, BigDecimal.ROUND_HALF_UP);
            articleCount += item.getCount();
            item.setFinalPrice(finalMoney);
            item.setOrderId(orderId);
            totalMoney = totalMoney.add(finalMoney).setScale(2, BigDecimal.ROUND_HALF_UP);
            originMoney = originMoney.add(item.getOriginalPrice().multiply(BigDecimal.valueOf(item.getCount()))).setScale(2, BigDecimal.ROUND_HALF_UP);
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
        //记录订单菜品 yz 2017-03-27
        LogTemplateUtils.getOrderItemLogByOrderType(brand.getBrandName(), order.getId(), order.getOrderItems());

        BigDecimal payMoney = totalMoney.add(order.getServicePrice());
        payMoney = payMoney.add(order.getMealFeePrice());

        if (customer != null) {
            ShopDetail detail = shopDetailService.selectById(order.getShopDetailId());
            if (order.getWaitId() != null && !"".equals(order.getWaitId())) {
                OrderPaymentItem item = new OrderPaymentItem();
                item.setId(ApplicationUtils.randomUUID());
                item.setOrderId(orderId);
                item.setPaymentModeId(PayMode.WAIT_MONEY);
                item.setPayTime(order.getCreateTime());
                item.setPayValue(order.getWaitMoney());
                item.setRemark("等位红包支付:" + order.getWaitMoney());
                item.setResultData(order.getWaitId());
                if (order.getWaitMoney().doubleValue() > 0) {
                    orderPaymentItemService.insert(item);
                }
//            UserActionUtils.writeToFtp(LogType.ORDER_LOG, brand.getBrandName(), shopDetail.getName(), order.getId(),
//                    "订单使用等位红包支付了：" + item.getPayValue());
//                Map waitPayMap = new HashMap(4);
//                waitPayMap.put("brandName", brand.getBrandName());
//                waitPayMap.put("fileName", order.getId());
//                waitPayMap.put("type", "orderAction");
//                waitPayMap.put("content", "订单:"+order.getId()+"使用等位红包支付了：" + item.getPayValue() +",请求服务器地址为:" + MQSetting.getLocalIP());
//                doPostAnsc(url, waitPayMap);
//                Map CustomerWaitPayMap = new HashMap(4);
//                CustomerWaitPayMap.put("brandName", brand.getBrandName());
//                CustomerWaitPayMap.put("fileName", customer.getId());
//                CustomerWaitPayMap.put("type", "UserAction");
//                CustomerWaitPayMap.put("content", "用户:"+customer.getNickname()+"使用等位红包支付了：" + item.getPayValue() +"订单Id为:"+order.getId()+",请求服务器地址为:" + MQSetting.getLocalIP());
//                doPostAnsc(url, CustomerWaitPayMap);
                LogTemplateUtils.getWaitMoneyLogByOrderType(brand.getBrandName(), order.getId(), item.getPayValue());
                LogTemplateUtils.getWaitMoneyLogByUserType(brand.getBrandName(), order.getId(), item.getPayValue(), customer.getNickname());

                GetNumber getNumber = getNumberService.selectById(order.getWaitId());
                log.error(order.getWaitId() + "-----------222222222222222");
                getNumber.setState(WaitModerState.WAIT_MODEL_NUMBER_THREE);
                getNumberService.update(getNumber);
            }

            payMoney = payMoney.subtract(order.getWaitMoney());

            if (detail.getShopMode() != ShopMode.HOUFU_ORDER && order.getPayType() != PayType.NOPAY) {
                if (order.getUseCoupon() != null && order.getParentOrderId() == null) {
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
//                UserActionUtils.writeToFtp(LogType.ORDER_LOG, brand.getBrandName(), shopDetail.getName(), order.getId(),
//                        "订单使用优惠卷支付了：" + item.getPayValue());
//                    Map couponPaymap = new HashMap(4);
//                    couponPaymap.put("brandName", brand.getBrandName());
//                    couponPaymap.put("fileName", order.getId());
//                    couponPaymap.put("type", "orderAction");
//                    couponPaymap.put("content", "订单:"+order.getId()+"订单使用优惠卷支付了：" + item.getPayValue() +",请求服务器地址为:" + MQSetting.getLocalIP());
//                    doPostAnsc(url, couponPaymap);
//                    Map CustomerCouponPaymap = new HashMap(4);
//                    CustomerCouponPaymap.put("brandName", brand.getBrandName());
//                    CustomerCouponPaymap.put("fileName", customer.getId());
//                    CustomerCouponPaymap.put("type", "UserAction");
//                    CustomerCouponPaymap.put("content", "用户:"+customer.getNickname()+"使用优惠卷支付了：" + item.getPayValue() +"订单Id为:"+order.getId()+",请求服务器地址为:" + MQSetting.getLocalIP());
//                    doPostAnsc(url, CustomerCouponPaymap);
                    LogTemplateUtils.getCouponByOrderType(brand.getBrandName(), order.getId(), item.getPayValue());
                    LogTemplateUtils.getCouponByUserType(brand.getBrandName(), customer.getId(), customer.getNickname(), item.getPayValue());
                    payMoney = payMoney.subtract(item.getPayValue()).setScale(2, BigDecimal.ROUND_HALF_UP);
                }
                // 使用余额
                if (payMoney.doubleValue() > 0 && order.isUseAccount() && order.getPayType() != PayType.NOPAY) {
                    BigDecimal payValue = accountService.payOrder(order, payMoney, customer, brand, shopDetail);
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
            }

            //如果是余额不满足时，使用现金或者银联支付
            if (payMoney.compareTo(BigDecimal.ZERO) > 0 && order.getPayMode() == OrderPayMode.YL_PAY) {
                OrderPaymentItem item = new OrderPaymentItem();
                item.setId(ApplicationUtils.randomUUID());
                item.setOrderId(orderId);
                item.setPaymentModeId(PayMode.BANK_CART_PAY);
                item.setPayTime(order.getCreateTime());
                item.setPayValue(payMoney);
                item.setRemark("银联支付:" + item.getPayValue());
                orderPaymentItemService.insert(item);
//            UserActionUtils.writeToFtp(LogType.ORDER_LOG, brand.getBrandName(), shopDetail.getName(), order.getId(),
//                    "订单使用银联支付了：" + item.getPayValue());
//                Map cartPayMap = new HashMap(4);
//                cartPayMap.put("brandName", brand.getBrandName());
//                cartPayMap.put("fileName", order.getId());
//                cartPayMap.put("type", "orderAction");
//                cartPayMap.put("content", "订单:"+order.getId()+"订单使用银联支付了：" + item.getPayValue() +",请求服务器地址为:" + MQSetting.getLocalIP());
//                doPostAnsc(url, cartPayMap);
                LogTemplateUtils.getBankByOrderType(brand.getBrandName(), order.getId(), item.getPayValue());

//                Map CustomerCartPayMap = new HashMap(4);
//                CustomerCartPayMap.put("brandName", brand.getBrandName());
//                CustomerCartPayMap.put("fileName", customer.getId());
//                CustomerCartPayMap.put("type", "UserAction");
//                CustomerCartPayMap.put("content", "用户:"+customer.getNickname()+"使用银联支付了：" + item.getPayValue() +"订单Id为:"+order.getId()+",请求服务器地址为:" + MQSetting.getLocalIP());
//                doPostAnsc(url, CustomerCartPayMap);
                LogTemplateUtils.getBankByUserType(brand.getBrandName(), customer.getId(), customer.getNickname(), item.getPayValue());
                order.setAllowContinueOrder(false);
            } else if (payMoney.compareTo(BigDecimal.ZERO) > 0 && order.getPayMode() == OrderPayMode.XJ_PAY) {
                OrderPaymentItem item = new OrderPaymentItem();
                item.setId(ApplicationUtils.randomUUID());
                item.setOrderId(orderId);
                item.setPaymentModeId(PayMode.CRASH_PAY);
                item.setPayTime(order.getCreateTime());
                item.setPayValue(payMoney);
                item.setRemark("现金支付:" + item.getPayValue());
                orderPaymentItemService.insert(item);
//            UserActionUtils.writeToFtp(LogType.ORDER_LOG, brand.getBrandName(), shopDetail.getName(), order.getId(),
//                    "订单使用银联支付了：" + item.getPayValue());
//                Map crashPayMap = new HashMap(4);
//                crashPayMap.put("brandName", brand.getBrandName());
//                crashPayMap.put("fileName", order.getId());
//                crashPayMap.put("type", "orderAction");
//                crashPayMap.put("content", "订单:"+order.getId()+"订单使用现金支付了：" + item.getPayValue() +",请求服务器地址为:" + MQSetting.getLocalIP());
//                doPostAnsc(url, crashPayMap);
                LogTemplateUtils.getMoneyByOrderType(brand.getBrandName(), order.getId(), item.getPayValue());
                LogTemplateUtils.getMoneyByUserType(brand.getBrandName(), customer.getId(), customer.getNickname(), item.getPayValue());

//                Map CustomerCrashPayMap = new HashMap(4);
//                CustomerCrashPayMap.put("brandName", brand.getBrandName());
//                CustomerCrashPayMap.put("fileName", customer.getId());
//                CustomerCrashPayMap.put("type", "UserAction");
//                CustomerCrashPayMap.put("content", "用户:"+customer.getNickname()+"使用现金支付了：" + item.getPayValue() +"订单Id为:"+order.getId()+",请求服务器地址为:" + MQSetting.getLocalIP());
//                doPostAnsc(url, CustomerCrashPayMap);
                order.setAllowContinueOrder(false);
            } else if (payMoney.compareTo(BigDecimal.ZERO) > 0 && order.getPayMode() == OrderPayMode.SHH_PAY) {
                OrderPaymentItem item = new OrderPaymentItem();
                item.setId(ApplicationUtils.randomUUID());
                item.setOrderId(orderId);
                item.setPaymentModeId(PayMode.SHANHUI_PAY);
                item.setPayTime(order.getCreateTime());
                item.setPayValue(payMoney);
                item.setRemark("闪惠支付:" + item.getPayValue());
                orderPaymentItemService.insert(item);
                UserActionUtils.writeToFtp(LogType.ORDER_LOG, brand.getBrandName(), shopDetail.getName(), order.getId(),
                        "订单使用银联支付了：" + item.getPayValue());
                order.setAllowContinueOrder(false);
            } else if (payMoney.compareTo(BigDecimal.ZERO) > 0 && order.getPayMode() == OrderPayMode.JF_PAY) {
                OrderPaymentItem item = new OrderPaymentItem();
                item.setId(ApplicationUtils.randomUUID());
                item.setOrderId(orderId);
                item.setPaymentModeId(PayMode.INTEGRAL_PAY);
                item.setPayTime(order.getCreateTime());
                item.setPayValue(payMoney);
                item.setRemark("会员支付:" + item.getPayValue());
                orderPaymentItemService.insert(item);
                Map crashPayMap = new HashMap(4);
                crashPayMap.put("brandName", brand.getBrandName());
                crashPayMap.put("fileName", order.getId());
                crashPayMap.put("type", "orderAction");
                crashPayMap.put("content", "订单:" + order.getId() + "订单使用会员支付了：" + item.getPayValue() + ",请求服务器地址为:" + MQSetting.getLocalIP());
                doPostAnsc(url, crashPayMap);
                Map CustomerCrashPayMap = new HashMap(4);
                CustomerCrashPayMap.put("brandName", brand.getBrandName());
                CustomerCrashPayMap.put("fileName", customer.getId());
                CustomerCrashPayMap.put("type", "UserAction");
                CustomerCrashPayMap.put("content", "用户:" + customer.getNickname() + "使用会员支付了：" + item.getPayValue() + "订单Id为:" + order.getId() + ",请求服务器地址为:" + MQSetting.getLocalIP());
                doPostAnsc(url, CustomerCrashPayMap);
                order.setAllowContinueOrder(false);
            }

            if (payMoney.doubleValue() < 0) {
                payMoney = BigDecimal.ZERO;
            }
            //yz 记录订单支付项 2017-03-27
            order.setAccountingTime(order.getCreateTime()); // 财务结算时间

            order.setAllowCancel(true); // 订单是否允许取消
            order.setAllowAppraise(false);
            order.setArticleCount(articleCount); // 订单餐品总数
            order.setClosed(false); // 订单是否关闭 否
            order.setSerialNumber(DateFormatUtils.format(new Date(), "yyyyMMddHHmmssSSSS")); // 流水号
            order.setOriginalAmount(originMoney.add(order.getServicePrice()).add(order.getMealFeePrice()).add(extraMoney));// 原价
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
            //判断是否是后付款模式
            if (order.getOrderMode() == ShopMode.HOUFU_ORDER) {
                order.setOrderState(OrderState.SUBMIT);
                order.setProductionStatus(ProductionStatus.NOT_ORDER);
                if (order.getDistributionModeId() != 3) {
                    order.setAllowContinueOrder(true);
                }
            } else {
                order.setOrderState(OrderState.SUBMIT);
                order.setProductionStatus(ProductionStatus.NOT_ORDER);
            }
            if (order.getDistributionModeId() == DistributionType.TAKE_IT_SELF && detail.getContinueOrderScan() == Common.NO) {
                order.setTableNumber(order.getVerCode());
            }
            if (order.getDistributionModeId() == DistributionType.DELIVERY_MODE_ID) {
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

            if (order.getOrderMode() == ShopMode.MANUAL_ORDER) {
                order.setNeedScan(Common.YES);
            }
            insert(order);
            customerService.changeLastOrderShop(order.getShopDetailId(), order.getCustomerId());
            if (order.getPaymentAmount().doubleValue() == 0) {
                payOrderSuccess(order);
            }

            jsonResult.setData(order);
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
            } else if (order.getPayType() == PayType.NOPAY && order.getOrderMode() == ShopMode.BOSS_ORDER) {
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
        } else {
            //pos开台支付不存在用户的时候执行
            OrderPaymentItem item = null;
            order.setOrderState(OrderState.PAYMENT);
            order.setAllowContinueOrder(false);
            order.setAccountingTime(order.getCreateTime()); // 财务结算时间
            order.setAllowCancel(true); // 订单是否允许取消
            order.setAllowAppraise(false);
            order.setArticleCount(articleCount); // 订单餐品总数
            order.setClosed(false); // 订单是否关闭 否
            order.setSerialNumber(DateFormatUtils.format(new Date(), "yyyyMMddHHmmssSSSS")); // 流水号
            order.setOriginalAmount(originMoney.add(extraMoney));// 原价
            order.setReductionAmount(BigDecimal.ZERO);// 折扣金额
            order.setOrderMoney(totalMoney); // 订单实际金额
            order.setPrintTimes(0);
            order.setCustomerId("0");
            order.setOrderMode(ShopMode.CALL_NUMBER);
            order.setProductionStatus(ProductionStatus.NOT_ORDER);
            insert(order);
            jsonResult.setData(order);
            switch (order.getPayMode()) {
                case OrderPayMode.WX_PAY:
                    item = new OrderPaymentItem();
                    item.setId(ApplicationUtils.randomUUID());
                    item.setOrderId(orderId);
                    item.setPaymentModeId(PayMode.WEIXIN_PAY);
                    item.setPayTime(order.getCreateTime());
                    item.setPayValue(order.getPaymentAmount());
                    item.setRemark("微信支付:" + order.getPaymentAmount());
                    orderPaymentItemService.insert(item);
                    break;
                case OrderPayMode.ALI_PAY:
                    item = new OrderPaymentItem();
                    item.setId(ApplicationUtils.randomUUID());
                    item.setOrderId(orderId);
                    item.setPaymentModeId(PayMode.ALI_PAY);
                    item.setPayTime(order.getCreateTime());
                    item.setPayValue(order.getPaymentAmount());
                    item.setRemark("支付宝支付:" + order.getPaymentAmount());
                    orderPaymentItemService.insert(item);
                    break;
                case OrderPayMode.YL_PAY:
                    item = new OrderPaymentItem();
                    item.setId(ApplicationUtils.randomUUID());
                    item.setOrderId(orderId);
                    item.setPaymentModeId(PayMode.BANK_CART_PAY);
                    item.setPayTime(order.getCreateTime());
                    item.setPayValue(order.getPaymentAmount());
                    item.setRemark("银联支付:" + order.getPaymentAmount());
                    orderPaymentItemService.insert(item);
                    break;
                case OrderPayMode.XJ_PAY:
                    item = new OrderPaymentItem();
                    item.setId(ApplicationUtils.randomUUID());
                    item.setOrderId(orderId);
                    item.setPaymentModeId(PayMode.CRASH_PAY);
                    item.setPayTime(order.getCreateTime());
                    item.setPayValue(order.getPaymentAmount());
                    item.setRemark("现金支付:" + order.getPaymentAmount());
                    orderPaymentItemService.insert(item);
                    break;
                case OrderPayMode.SHH_PAY:
                    item = new OrderPaymentItem();
                    item.setId(ApplicationUtils.randomUUID());
                    item.setOrderId(orderId);
                    item.setPaymentModeId(PayMode.SHANHUI_PAY);
                    item.setPayTime(order.getCreateTime());
                    item.setPayValue(order.getPaymentAmount());
                    item.setRemark("大众点评支付:" + order.getPaymentAmount());
                    orderPaymentItemService.insert(item);
                    break;
                case OrderPayMode.JF_PAY:
                    item = new OrderPaymentItem();
                    item.setId(ApplicationUtils.randomUUID());
                    item.setOrderId(orderId);
                    item.setPaymentModeId(PayMode.INTEGRAL_PAY);
                    item.setPayTime(order.getCreateTime());
                    item.setPayValue(order.getPaymentAmount());
                    item.setRemark("大众点评支付:" + order.getPaymentAmount());
                    orderPaymentItemService.insert(item);
                    break;
            }
            if (order.getGiveChange().doubleValue() > 0) {
                item = new OrderPaymentItem();
                item.setId(ApplicationUtils.randomUUID());
                item.setOrderId(orderId);
                item.setPaymentModeId(PayMode.GIVE_CHANGE);
                item.setPayTime(order.getCreateTime());
                item.setPayValue(order.getGiveChange().multiply(new BigDecimal(-1)));
                item.setRemark("找零:" + order.getGiveChange());
                orderPaymentItemService.insert(item);
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

        if (order.getOrderMode() == ShopMode.BOSS_ORDER && order.getParentOrderId() == null && order.getPayType() == PayType.NOPAY) {
            updateChild(order);
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
            if (order.getParentOrderId() != null && (order.getOrderState() != OrderState.SUBMIT || order.getOrderMode() == ShopMode.HOUFU_ORDER
                    || (order.getOrderMode() == ShopMode.BOSS_ORDER && order.getPayType() == PayType.NOPAY))) {
                return findCustomerNewOrder(customerId, shopId, order.getParentOrderId());
            }
            List<OrderItem> itemList = orderItemService.listByOrderId(order.getId());
            order.setOrderItems(itemList);
            if (order.getOrderState() != OrderState.SUBMIT || order.getOrderMode() == ShopMode.HOUFU_ORDER
                    || (order.getOrderMode() == ShopMode.BOSS_ORDER && order.getPayType() == PayType.NOPAY)) {
                List<String> childIds = selectChildIdsByParentId(order.getId());
                List<OrderItem> childItems = orderItemService.listByOrderIds(childIds);
                order.getOrderItems().addAll(childItems);
            }

        }
        return order;
    }

    private List<String> selectChildIdsByParentId(String id) {
        Order order = orderMapper.selectByPrimaryKey(id);
        if (order.getOrderMode() == ShopMode.HOUFU_ORDER ||
                (order.getOrderMode() == ShopMode.BOSS_ORDER && order.getPayType() == PayType.NOPAY)) {
            return orderMapper.selectChildIdsByParentIdByFive(id);
        } else {
            return orderMapper.selectChildIdsByParentId(id);
        }

    }

    @Override
    public List<Order> selectByParentId(String parentOrderId, Integer parentOrderPayType) {
        return orderMapper.selectByParentId(parentOrderId, parentOrderPayType);
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
//            orderMapper.setStockBySuit(order.getShopDetailId());//自动更新套餐数量
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
        Result result = new Result();
        Order order = selectById(orderId);
        if (order.getOrderState() != OrderState.SUBMIT) {
            return new Result(false);
        }
        if (order.getIsPay() != OrderPayState.ALIPAYING) {
            order.setIsPay(OrderPayState.NOT_PAY);
        }
        if (order.getPayMode() == 2) {
            if (order.getIsPay() != OrderPayState.ALIPAYING) {
                order.setIsPay(OrderPayState.NOT_PAY);
            }
            orderMapper.updateByPrimaryKeySelective(order);
            return new Result("支付宝订单更改为微信支付，支付时点击关闭不取消订单", false);
        }
//        Brand brand = brandService.selectById(order.getBrandId());
//        ShopDetail shopDetail = shopDetailService.selectByPrimaryKey(order.getShopDetailId());
//        UserActionUtils.writeToFtp(LogType.ORDER_LOG, brand.getBrandName(), shopDetail.getName(), order.getId(),
//                "微信点X取消订单！");
//      LogTemplateUtils.getRefundWechatByUserType(order,brand,shopDetail.getName());
        if (order.getOrderMode() == ShopMode.BOSS_ORDER && order.getProductionStatus() == ProductionStatus.PRINTED) {
            refundOrderHoufu(order);
            result.setSuccess(true);
            BigDecimal hasPay = orderMapper.getPayHoufu(orderId);
            if (hasPay == null) {
                hasPay = BigDecimal.valueOf(0);
            }
            order.setPaymentAmount(order.getOrderMoney().subtract(hasPay));
        } else {
            if (!order.getOperatorId().equals("sb")) {
                result.setSuccess(autoRefundOrder(orderId));
            }
        }
        update(order);
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

        //拒绝订单后还原库存
        if (order.getPayType() != PayType.NOPAY) {
            Boolean addStockSuccess = false;
            addStockSuccess = addStock(getOrderInfo(orderId));
            if (!addStockSuccess) {
                log.info("库存还原失败:" + order.getId());
            }
//            orderMapper.setStockBySuit(order.getShopDetailId());//自动更新套餐数量
        }
        return result;
    }

    private void refundOrder(Order order) {
        List<OrderPaymentItem> payItemsList = orderPaymentItemService.selectByOrderId(order.getId());
        Brand brand = brandService.selectById(order.getBrandId());
        ShopDetail shopDetail = shopDetailService.selectByPrimaryKey(order.getShopDetailId());
//        UserActionUtils.writeToFtp(LogType.ORDER_LOG, brand.getBrandName(), shopDetail.getName(), order.getId(),
//                "订单已取消！");
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
                    accountService.addAccount(item.getPayValue(), item.getResultData(), "取消订单返还", AccountLog.SOURCE_CANCEL_ORDER, order.getShopDetailId());
                    item.setPayValue(item.getPayValue().multiply(new BigDecimal(-1)));
                    break;
                case PayMode.APPRAISE_RED_PAY:
                    redPacketService.refundRedPacket(item.getPayValue(), item.getResultData());
                    item.setPayValue(item.getPayValue().multiply(new BigDecimal(-1)));
                    break;
                case PayMode.SHARE_RED_PAY:
                    redPacketService.refundRedPacket(item.getPayValue(), item.getResultData());
                    item.setPayValue(item.getPayValue().multiply(new BigDecimal(-1)));
                    break;
                case PayMode.REFUND_ARTICLE_RED_PAY:
                    redPacketService.refundRedPacket(item.getPayValue(), item.getResultData());
                    item.setPayValue(item.getPayValue().multiply(new BigDecimal(-1)));
                    break;
                case PayMode.CHARGE_PAY:
                    chargeOrderService.refundCharge(item.getPayValue(), item.getResultData(), order.getShopDetailId());
                    item.setPayValue(item.getPayValue().multiply(new BigDecimal(-1)));
                    break;
                case PayMode.REWARD_PAY:
                    chargeOrderService.refundReward(item.getPayValue(), item.getResultData(), order.getShopDetailId());
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
                    if (result.containsKey("ERROR")) {
                        throw new RuntimeException("微信退款异常！" + new JSONObject(result).toString());
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
                    if (new JSONObject(resultJson).toString().indexOf("ERROR") != -1) {
                        throw new RuntimeException("支付宝退款异常！" + resultJson.toString());
                    }
                    item.setResultData(new JSONObject(resultJson).toString());
                    item.setPayValue(aliPay.add(aliRefund).multiply(new BigDecimal(-1)));
                    break;
                case PayMode.ARTICLE_BACK_PAY:
                    Customer customer = customerService.selectById(order.getCustomerId());

                    if (item.getPayValue().doubleValue() < 0) {
                        accountService.addAccount(item.getPayValue(), customer.getAccountId(), "取消订单扣除", -1, order.getShopDetailId());
                    }
                    item.setPayValue(item.getPayValue().multiply(new BigDecimal(-1)));
                    break;
                case PayMode.BANK_CART_PAY:
                    item.setPayValue(item.getPayValue().multiply(new BigDecimal(-1)));
                    break;
                case PayMode.CRASH_PAY:
                    item.setPayValue(item.getPayValue().multiply(new BigDecimal(-1)));
                    break;
                case PayMode.SHANHUI_PAY:
                    item.setPayValue(item.getPayValue().multiply(new BigDecimal(-1)));
                    break;
                case PayMode.INTEGRAL_PAY:
                    accountService.addAccount(item.getPayValue(), item.getResultData(), "取消订单返还", AccountLog.SOURCE_CANCEL_ORDER, order.getShopDetailId());
                    item.setPayValue(item.getPayValue().multiply(new BigDecimal(-1)));
                    break;
            }
            item.setId(newPayItemId);
            orderPaymentItemService.insert(item);
        }
        Map map = new HashMap(4);
        map.put("brandName", brand.getBrandName());
        map.put("fileName", order.getId());
        map.put("type", "orderAction");
        map.put("content", "订单:" + order.getId() + "已取消,请求服务器地址为:" + MQSetting.getLocalIP());
        doPostAnsc(url, map);
    }

    @Override
    public String fixedRefund(String brandId, String shopId,
                              int total, int refund, String transaction_id, String mchid, String id) {
        WechatConfig config = wechatConfigService.selectByBrandId(brandId);
        ShopDetail shopDetail = shopDetailService.selectByPrimaryKey(shopId);
        Map<String, String> result = null;
        String newPayItemId = ApplicationUtils.randomUUID();
        if (shopDetail.getWxServerId() == null) {
            result = WeChatPayUtils.refund(newPayItemId, transaction_id,
                    total, refund, config.getAppid(), config.getMchid(),
                    config.getMchkey(), config.getPayCertPath());
        } else {
            WxServerConfig wxServerConfig = wxServerConfigService.selectById(shopDetail.getWxServerId());

            result = WeChatPayUtils.refundNew(newPayItemId, transaction_id,
                    total, refund, wxServerConfig.getAppid(), wxServerConfig.getMchid(),
                    StringUtils.isEmpty(shopDetail.getMchid()) ? config.getMchid() : shopDetail.getMchid(), wxServerConfig.getMchkey(), wxServerConfig.getPayCertPath());
        }
//        OrderPaymentItem orderPaymentItem = orderPaymentItemService.selectById(id);
//        orderPaymentItem.setResultData(new JSONObject(result).toString());
//        orderPaymentItemService.update(orderPaymentItem);
        return new JSONObject(result).toString();
    }

    @Override
    public List<OrderItem> selectListByShopIdAndTime(String zuoriDay, String id) {
        Date beginTime = DateUtil.getformatBeginDate(zuoriDay);
        Date endTime = DateUtil.getformatEndDate(zuoriDay);
        return orderMapper.selectListByShopIdAndTime(beginTime, endTime, id);
    }

    @Override
    public List<OrderItem> selectCustomerListByShopIdAndTime(String zuoriDay, String id) {
        Date beginTime = DateUtil.getformatBeginDate(zuoriDay);
        Date endTime = DateUtil.getformatEndDate(zuoriDay);
        return orderMapper.selectCustomerListByShopIdAndTime(beginTime, endTime, id);
    }

    @Override
    public void alipayRefund(String orderId, BigDecimal refundTotal) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        ShopDetail shopDetail = shopDetailService.selectByPrimaryKey(order.getShopDetailId());
        String newPayItemId = ApplicationUtils.randomUUID();
        BrandSetting brandSetting = brandSettingService.selectByBrandId(order.getBrandId());
        AliPayUtils.connection(StringUtils.isEmpty(shopDetail.getAliAppId()) ? brandSetting.getAliAppId() : shopDetail.getAliAppId().trim(),
                StringUtils.isEmpty(shopDetail.getAliPrivateKey()) ? brandSetting.getAliPrivateKey().trim() : shopDetail.getAliPrivateKey().trim(),
                StringUtils.isEmpty(shopDetail.getAliPublicKey()) ? brandSetting.getAliPublicKey().trim() : shopDetail.getAliPublicKey().trim());
        Map map = new HashMap();
        map.put("out_trade_no", order.getId());
        map.put("refund_amount", refundTotal);
        map.put("out_request_no", newPayItemId);
        String resultJson = AliPayUtils.refundPay(map);

        OrderPaymentItem back = new OrderPaymentItem();
        back.setId(ApplicationUtils.randomUUID());
        back.setOrderId(order.getId());
        back.setPaymentModeId(PayMode.ARTICLE_BACK_PAY);
        back.setPayTime(new Date());
        back.setPayValue(new BigDecimal(-1).multiply(refundTotal));
        back.setRemark("退菜红包:" + refundTotal);

        back.setResultData("支付宝支付返回" + refundTotal + "金额");
        orderPaymentItemService.insert(back);
    }

    private void refundOrderHoufu(Order order) {
        List<OrderPaymentItem> payItemsList = orderPaymentItemService.selectByOrderId(order.getId());
        for (OrderPaymentItem item : payItemsList) {
            String newPayItemId = ApplicationUtils.randomUUID();
            switch (item.getPaymentModeId()) {
                case PayMode.ACCOUNT_PAY:
                    accountService.addAccount(item.getPayValue(), item.getResultData(), "取消订单返还", AccountLog.SOURCE_CANCEL_ORDER, order.getShopDetailId());
                    item.setPayValue(item.getPayValue().multiply(new BigDecimal(-1)));
                    item.setId(newPayItemId);
                    orderPaymentItemService.insert(item);
                    break;
                case PayMode.CHARGE_PAY:
                    chargeOrderService.refundCharge(item.getPayValue(), item.getResultData(), order.getShopDetailId());
                    item.setPayValue(item.getPayValue().multiply(new BigDecimal(-1)));
                    item.setId(newPayItemId);
                    orderPaymentItemService.insert(item);
                    break;
                case PayMode.REWARD_PAY:
                    chargeOrderService.refundReward(item.getPayValue(), item.getResultData(), order.getShopDetailId());
                    item.setPayValue(item.getPayValue().multiply(new BigDecimal(-1)));
                    item.setId(newPayItemId);
                    orderPaymentItemService.insert(item);
                    break;

            }

        }
        Brand brand = brandService.selectById(order.getBrandId());
        Map map = new HashMap(4);
        map.put("brandName", brand.getBrandName());
        map.put("fileName", order.getId());
        map.put("type", "orderAction");
        map.put("content", "订单:" + order.getId() + "已取消,请求服务器地址为:" + MQSetting.getLocalIP());
        doPostAnsc(url, map);
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
            if (order.getPayMode() == OrderPayMode.ALI_PAY && order.getIsPay() == OrderPayState.ALIPAYING) {
                order.setIsPay(OrderPayState.ALIPAYED);
                update(order);
            } else if (order.getPayMode() != OrderPayMode.WX_PAY && order.getIsPay() == OrderPayState.ALIPAYING) {
                order.setIsPay(OrderPayState.PAYED);
                update(order);
            } else if (order.getPayMode() != OrderPayMode.ALI_PAY && order.getIsPay() == OrderPayState.ALIPAYING) {
                order.setIsPay(OrderPayState.NOT_PAY);
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
        String time = DateUtil.formatDate(new Date(), "yyyy-MM-dd hh:mm:ss");
        Order order = selectById(orderId);
        //如果是后付款模式 不验证直接进行修改模式
        if (order.getOrderMode() == ShopMode.HOUFU_ORDER) {
            log.info("后付款模式：pushOrder修改生产状态：" + ProductionStatus.HAS_ORDER + "订单id为：" + orderId + "当前时间为：" + time);
            order.setProductionStatus(ProductionStatus.HAS_ORDER);
            order.setPushOrderTime(new Date());
            update(order);
//        }
        } else if (validOrderCanPush(order)) {
            log.info("pushOrder时候支付宝支付修改状态：" + ProductionStatus.HAS_ORDER + "订单id为：" + orderId + "当前时间为：" + time);
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


        switch (order.getOrderMode()) {
//            case 1:
//                if (order.getTableNumber() == null) {
//                    throw new AppException(AppException.ORDER_MODE_CHECK, "桌号不得为空");
//                }
//                break;
            case ShopMode.BOSS_ORDER:
                if (order.getPayType() == PayType.PAY) {
                    if (order.getOrderState() != OrderState.PAYMENT || ProductionStatus.NOT_ORDER != order.getProductionStatus()) {
                        log.error("立即下单失败: " + order.getId());
                        throw new AppException(AppException.ORDER_STATE_ERR);
                    }
                } else if (order.getPayType() == PayType.NOPAY) {
                    if (order.getOrderState() != OrderState.SUBMIT || ProductionStatus.NOT_ORDER != order.getProductionStatus()) {
                        log.error("立即下单失败: " + order.getId());
                        throw new AppException(AppException.ORDER_STATE_ERR);
                    }
                }
                break;
            case ShopMode.CALL_NUMBER:
                if (order.getOrderState() != OrderState.PAYMENT || ProductionStatus.NOT_ORDER != order.getProductionStatus()) {
                    log.error("立即下单失败: " + order.getId());
                    throw new AppException(AppException.ORDER_STATE_ERR);
                }
                break;
            case ShopMode.MANUAL_ORDER:
                if (order.getOrderState() != OrderState.PAYMENT || ProductionStatus.NOT_ORDER != order.getProductionStatus()) {
                    log.error("立即下单失败: " + order.getId());
                    throw new AppException(AppException.ORDER_STATE_ERR);
                }
                break;
        }
        return true;
    }

    @Override
    public Order callNumber(String orderId) {
        Order order = selectById(orderId);
//        if (order.getCallNumberTime() == null) {
        order.setProductionStatus(ProductionStatus.HAS_CALL);
        order.setCallNumberTime(new Date());
        update(order);
//        }
        ShopDetail shopDetail = shopDetailService.selectById(order.getShopDetailId());
        Brand brand = brandService.selectById(order.getBrandId());
        Map map = new HashMap(4);
        map.put("brandName", brand.getBrandName());
        map.put("fileName", shopDetail.getName());
        map.put("type", "posAction");
        map.put("content", "订单:" + order.getId() + "被叫号推送微信就餐提醒并修改productionStatus为3,请求服务器地址为:" + MQSetting.getLocalIP());
        doPostAnsc(url, map);
        Map orderMap = new HashMap(4);
        orderMap.put("brandName", brand.getBrandName());
        orderMap.put("fileName", order.getId());
        orderMap.put("type", "orderAction");
        orderMap.put("content", "订单:" + order.getId() + "被叫号推送微信就餐提醒并修改productionStatus为3,请求服务器地址为:" + MQSetting.getLocalIP());
        doPostAnsc(url, orderMap);
        return order;
    }

    @Override
    public List<Map<String, Object>> getPrintData(String orderId) {

        return null;
    }

    @Override
    public Order printSuccess(String orderId) throws AppException {
        Order order = selectById(orderId);
        Brand brand = brandService.selectById(order.getBrandId());
        ShopDetail shopDetail = shopDetailService.selectById(order.getShopDetailId());
        if (StringUtils.isEmpty(order.getParentOrderId())) {
            log.info("打印成功，订单为主订单，允许加菜-:" + order.getId());
            LogTemplateUtils.getParentOrderPrintSuccessByOrderType(brand.getBrandName(), order.getId(), order.getProductionStatus());
            LogTemplateUtils.getParentOrderPrintSuccessByPOSType(brand.getBrandName(), order.getId(), order.getProductionStatus());
            //现金 银联 闪惠 积分 支付的时候  在付款中 服务员尚未确定的时候  不可加菜  有一段加菜真空期！  已废弃  wyj
//            if (order.getOrderMode() != ShopMode.CALL_NUMBER && order.getPayMode() != OrderPayMode.YL_PAY && order.getPayMode() != OrderPayMode.XJ_PAY
//                    && order.getPayMode() != OrderPayMode.SHH_PAY && order.getPayMode() != OrderPayMode.JF_PAY) {
            if (order.getOrderMode() != ShopMode.CALL_NUMBER) {
                if (order.getPayType() == PayType.NOPAY && order.getOrderState() == OrderState.PAYMENT) {

                } else {
                    if (order.getDistributionModeId() == DistributionType.RESTAURANT_MODE_ID) {
                        order.setAllowContinueOrder(true);
                    }
                }
            }
        } else {
            log.info("打印成功，订单为子订单:" + order.getId() + " pid:" + order.getParentOrderId());
            order.setAllowContinueOrder(false);
            order.setAllowAppraise(false);
            LogTemplateUtils.getChildOrderPrintSuccessByOrderType(brand.getBrandName(), order.getId(), order.getProductionStatus());
            LogTemplateUtils.getChildOrderPrintSuccessByPOSType(brand.getBrandName(), order.getId(), order.getProductionStatus());
        }
        order.setProductionStatus(ProductionStatus.PRINTED);
        order.setPrintOrderTime(new Date());
        order.setAllowCancel(false);
        update(order);
//        Map map = new HashMap(4);
//        map.put("brandName", brand.getBrandName());
//        map.put("fileName", shopDetail.getName());
//        map.put("type", "posAction");
//        map.put("content", "订单:"+order.getId()+"打印完成,请求服务器地址为:"+ MQSetting.getLocalIP());
//        doPostAnsc(url, map);
//        Map orderMap = new HashMap(4);
//        orderMap.put("brandName", brand.getBrandName());
//        orderMap.put("fileName", order.getId());
//        orderMap.put("type", "orderAction");
//        orderMap.put("content", "订单:"+order.getId()+"打印完成,请求服务器地址为:"+ MQSetting.getLocalIP());
//        doPostAnsc(url, orderMap);
//        UserActionUtils.writeToFtp(LogType.ORDER_LOG, brand.getBrandName(), shopDetail.getName(), order.getId(),
//                "printSuccess订单打印完成");
        //ShopDetail shopDetail = shopDetailService.selectById(order.getShopDetailId());
        //Customer customer = customerService.selectById(order.getCustomerId());
        //logBaseService.insertLogBaseInfoState(shopDetail, customer, orderId, LogBaseState.PRINT);
        return order;
    }
    @Override
    public int printUpdate(String orderId){
        Order o=new Order();
        o.setId(orderId);
        o.setProductionStatus(4);
        int count=orderMapper.updateByPrimaryKeySelective(o);
        return count;
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

            if (articleId.length() > 32) {
                articleId = item.getArticleId().substring(0, 32);
            }

            Article article = articleService.selectById(articleId);
            if (article.getVirtualId() != null && item.getType() != OrderItemType.MEALS_CHILDREN) {
                VirtualProducts virtualProducts = virtualProductsService.getVirtualProductsById(article.getVirtualId());
                if (virtualProducts != null && virtualProducts.getIsUsed() == Common.NO) {
                    //启用
                    List<VirtualProductsAndKitchen> virtualProductsAndKitchens =
                            virtualProductsService.getVirtualProductsAndKitchenById(article.getVirtualId());
                    for (VirtualProductsAndKitchen virtual : virtualProductsAndKitchens) {

                        String kitchenId = String.valueOf(virtual.getKitchenId());
                        Kitchen kitchen = kitchenService.selectById(virtual.getKitchenId());
                        kitchenMap.put(kitchenId, kitchen);//保存厨房信息
                        //判断 厨房集合中 是否已经包含当前厨房信息
                        if (!kitchenArticleMap.containsKey(kitchenId)) {
                            //如果没有 则新建
                            kitchenArticleMap.put(kitchenId, new ArrayList<OrderItem>());
                            kitchenArticleMap.get(kitchenId).add(item);
                        } else {
                            if (CollectionUtils.isEmpty(kitchenArticleMap.get(kitchenId).get(0).getChildren())) {
                                List<OrderItem> child = new ArrayList<>();
                                child.add(item);
                                kitchenArticleMap.get(kitchenId).get(0).setChildren(child);
                            } else {
                                List<OrderItem> child = kitchenArticleMap.get(kitchenId).get(0).getChildren();
                                child.add(item);
                                kitchenArticleMap.get(kitchenId).get(0).setChildren(child);
                            }

                        }

                    }
                    continue;
                }

            }

            if (item.getType() == OrderItemType.MEALS_CHILDREN) {  // 套餐子品
//                continue;
                Kitchen kitchen = kitchenService.getItemKitchenId(item);
                if (kitchen == null) {
                    continue;
                }
                String kitchenId = kitchen.getId().toString();
                Printer printer = printerService.selectById(kitchen.getPrinterId());
                if (printer.getTicketType() == TicketType.PRINT_TICKET && shopDetail.getPrintType().equals(PrinterType.TOTAL)) { //总单出
                    continue;
                } else {
                    if (kitchen != null) {

                        kitchenMap.put(kitchenId, kitchen);
                        if (!kitchenArticleMap.containsKey(kitchenId)) {
                            //如果没有 则新建
                            kitchenArticleMap.put(kitchenId, new ArrayList<OrderItem>());
                        }
                        if (printer.getTicketType() == TicketType.PRINT_LABEL) {
                            OrderItem parent = orderItemService.selectById(item.getParentId());
                            if (!kitchenArticleMap.get(kitchenId).contains(parent)) {
                                kitchenArticleMap.get(kitchenId).add(parent);
                            }
                        }
                        kitchenArticleMap.get(kitchenId).add(item);
                    } else {
//                        item.setPrintFailFlag(PrintStatus.PRINT_SUCCESS);
//                        orderItemService.update(item);
                    }


                }
            }

            if (OrderItemType.SETMEALS == item.getType()) { //如果类型是套餐那么continue
                if (shopDetail.getPrintType().equals(PrinterType.TOTAL)) { //总单出
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
//                    item.setPrintFailFlag(PrintStatus.PRINT_SUCCESS);
//                    orderItemService.update(item);
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
                            if (CollectionUtils.isEmpty(kitchenList)) {
//                                item.setPrintFailFlag(PrintStatus.PRINT_SUCCESS);
//                                orderItemService.update(item);
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
                            if (shopDetail.getSplitKitchen() == Common.YES) {
                                int count = item.getCount();
                                for (int i = 0; i < count; i++) {
                                    item.setCount(1);
                                    kitchenArticleMap.get(kitchenId).add(item);
                                }
                            } else {
                                kitchenArticleMap.get(kitchenId).add(item);
                            }

                        }
                        if (CollectionUtils.isEmpty(kitchenList)) {
//                            item.setPrintFailFlag(PrintStatus.PRINT_SUCCESS);
//                            orderItemService.update(item);
                        }

                    }
                }


            }
        }
        Boolean check = true;
        for (OrderItem item : articleList) {
            if (item.getPrintFailFlag() == PrintStatus.PRINT_ERROR || item.getPrintFailFlag() == PrintStatus.UNPRINT) {
                check = false;
            }
        }
        if (check) {
            order.setPrintKitchenFlag(PrintStatus.PRINT_SUCCESS);
            update(order);
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
            Map<String, Integer> countMap = new HashMap<>();
            for (OrderItem article : kitchenArticleMap.get(kitchenId)) {

                if (printer.getTicketType() == TicketType.PRINT_TICKET) {
                    //小票
                    if (shopDetail.getIsPosNew() == Common.POS_NEW) {
                        getKitchenModelNew(article, order, printer, shopDetail, printTask);
                    } else {
                        getKitchenModel(article, order, printer, shopDetail, printTask);
                    }

                } else {
                    //贴纸
                    for (int i = 0; i < article.getCount(); i++) {
                        if (article.getType() == OrderItemType.SETMEALS) {
                            countMap.put(article.getArticleId(), 0);
                        } else if (article.getType() == OrderItemType.MEALS_CHILDREN) {
                            OrderItem parent = orderItemService.selectById(article.getParentId());
                            countMap.put(parent.getArticleId(), countMap.get(parent.getArticleId()) + 1);
                            countMap.put(article.getArticleId(), countMap.get(parent.getArticleId()));
                        } else if (countMap.containsKey(article.getArticleId())) {
                            countMap.put(article.getArticleId(), countMap.get(article.getArticleId()) + 1);
                        } else {
                            countMap.put(article.getArticleId(), 1);
                        }
                        if (shopDetail.getIsPosNew() == Common.POS_NEW) {
                            getKitchenLabelNew(article, order, printer, shopDetail, printTask, countMap, kitchenArticleMap.get(kitchenId));
                        } else {
                            getKitchenLabel(article, order, printer, shopDetail, printTask, countMap, kitchenArticleMap.get(kitchenId));
                        }

                    }

                }
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
                if (shopDetail.getIsPosNew() == Common.YES) {
                    getRecommendModelNew(recommendId, order, printer, shopDetail, printTask);
                } else {
                    getRecommendModel(recommendId, order, printer, shopDetail, printTask);
                }

            }
        }
        Brand brand = brandService.selectById(order.getBrandId());
        JSONArray json = new JSONArray(printTask);
//        UserActionUtils.writeToFtp(LogType.POS_LOG, brand.getBrandName(), shopDetail.getName()
//                , "订单:" + order.getId() + "返回打印厨打模版" + json.toString());
        Map map = new HashMap(4);
        map.put("brandName", brand.getBrandName());
        map.put("fileName", shopDetail.getName());
        map.put("type", "posAction");
        map.put("content", "订单:" + order.getId() + "返回打印厨打模版" + json.toString() + ",请求服务器地址为:" + MQSetting.getLocalIP());
        doPostAnsc(url, map);

        return printTask;
    }


    @Override
    public List<Map<String, Object>> printTurnTable(Order order,String oldtableNumber){
        ShopDetail shopDetail = shopDetailService.selectById(order.getShopDetailId());
        List<Map<String, Object>> printTask = new ArrayList<>();
        List<Printer> ticketPrinter = printerService.selectByShopAndType(order.getShopDetailId(), PrinterType.RECEPTION);
        for (Printer printer : ticketPrinter) {
            if (shopDetail.getIsPosNew() == Common.YES) {
                getTurnTableModelNew(order, printer,shopDetail,printTask,oldtableNumber);
            } else {
                getTurnTableModel(order, printer, printTask,oldtableNumber);
            }
        }
        Brand brand = brandService.selectById(order.getBrandId());
        JSONArray json = new JSONArray(printTask);
        Map map = new HashMap(4);
        map.put("brandName", brand.getBrandName());
        map.put("fileName", shopDetail.getName());
        map.put("type", "posAction");
        map.put("content", "订单:" + order.getId() + "返回打印厨打模版" + json.toString() + ",请求服务器地址为:" + MQSetting.getLocalIP());
        doPostAnsc(url, map);
        return printTask;
    }

    private void getTurnTableModel(Order order, Printer printer, List<Map<String, Object>> printTask,String oldtableNumber) {
        String tableNumber = order.getTableNumber() != null ? order.getTableNumber() : "";//桌号
        String serialNumber = order.getSerialNumber();//序列号
        String modeText = "转台";

        //保存基本信息
        Map<String, Object> print = new HashMap<String, Object>();
        print.put("PORT", printer.getPort());
        print.put("OID", order.getId());
        print.put("IP", printer.getIp());
        String print_id = ApplicationUtils.randomUUID();
        print.put("PRINT_TASK_ID", print_id);
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("ORDER_ID", serialNumber);
        data.put("DATETIME", DateUtil.formatDate(order.getCreateTime(), "yyyy-MM-dd HH:mm"));
        data.put("DISTRIBUTION_MODE", modeText);
        data.put("TABLE_NUMBER", oldtableNumber);
        //添加当天打印订单的序号
        TableQrcode tableQrcode = tableQrcodeService.selectByTableNumberShopId(order.getShopDetailId(), Integer.valueOf(order.getTableNumber()));
        if (tableQrcode == null) {
            data.put("ORDER_NUMBER",  "---");
        } else {
            if (tableQrcode.getAreaId() == null) {
                data.put("ORDER_NUMBER", "---");
            } else {
                Area area = areaService.selectById(tableQrcode.getAreaId());
                if (area == null) {
                    data.put("ORDER_NUMBER", "---");
                } else {
                    data.put("ORDER_NUMBER", area.getName());
                }
            }

        }
        List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
        Map<String, Object> item = new HashMap<String, Object>();
        Map<String, Object> itemOld = new HashMap<String, Object>();
        itemOld.put("ARTICLE_COUNT","台号");
        itemOld.put("ARTICLE_NAME","               "+oldtableNumber);
        items.add(itemOld);
        Map<String, Object> itemNew = new HashMap<String, Object>();
        itemNew.put("ARTICLE_COUNT","转至");
        itemNew.put("ARTICLE_NAME","               "+tableNumber);
        items.add(itemNew);
        data.put("ITEMS", items);
        data.put("CUSTOMER_SATISFACTION", "");
        data.put("CUSTOMER_SATISFACTION_DEGREE", 0);
        data.put("CUSTOMER_PROPERTY", "");
        print.put("DATA", data);
        print.put("STATUS", "0");
        print.put("TICKET_TYPE", TicketType.KITCHEN);
        //添加到 打印集合
        printTask.add(print);
        RedisUtil.set(print_id, print);
    }

    private void getTurnTableModelNew(Order order, Printer printer,ShopDetail shopDetail, List<Map<String, Object>> printTask,String oldtableNumber) {
        String tableNumber = order.getTableNumber() != null ? order.getTableNumber() : "";//桌号
        String serialNumber = order.getSerialNumber();//序列号
        String modeText = "转台";

        //保存基本信息
        Map<String, Object> print = new HashMap<String, Object>();
        print.put("PORT", printer.getPort());
        print.put("OID", order.getId());
        print.put("IP", printer.getIp());
        print.put("PRINT_STATUS", order.getPrintKitchenFlag());
        String print_id = ApplicationUtils.randomUUID();
        print.put("PRINT_TASK_ID",print_id);
        print.put("TASK_ID", "");
        print.put("TASK_ORDER_ID", order.getId());
        print.put("LINE_WIDTH", shopDetail.getPageSize() == 0 ? 48 : 42);
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("ORDER_ID", serialNumber);
        data.put("DATETIME", DateUtil.formatDate(order.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
        data.put("DISTRIBUTION_MODE", modeText);
        data.put("TABLE_NUMBER", oldtableNumber);
        //添加当天打印订单的序号
        TableQrcode tableQrcode = tableQrcodeService.selectByTableNumberShopId(order.getShopDetailId(), Integer.valueOf(order.getTableNumber()));
        if (tableQrcode == null) {
            data.put("ORDER_NUMBER", "---");
        } else {
            if (tableQrcode.getAreaId() == null) {
                data.put("ORDER_NUMBER", "---");
            } else {
                Area area = areaService.selectById(tableQrcode.getAreaId());
                if (area == null) {
                    data.put("ORDER_NUMBER", "---");
                } else {
                    data.put("ORDER_NUMBER", area.getName());
                }
            }

        }
        List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
        Map<String, Object> itemOld = new HashMap<String, Object>();
        itemOld.put("ARTICLE_COUNT","台号");
        itemOld.put("ARTICLE_NAME","               "+oldtableNumber);
        items.add(itemOld);
        Map<String, Object> itemNew = new HashMap<String, Object>();
        itemNew.put("ARTICLE_COUNT","转至");
        itemNew.put("ARTICLE_NAME","               "+tableNumber);
        items.add(itemNew);
        data.put("ITEMS", items);
        data.put("CUSTOMER_SATISFACTION", "");
        data.put("CUSTOMER_SATISFACTION_DEGREE", 0);
        data.put("CUSTOMER_PROPERTY", "");
        print.put("DATA", data);
        print.put("STATUS", "0");
        print.put("TICKET_TYPE", TicketTypeNew.TICKET);
        print.put("TICKET_MODE", TicketTypeNew.KITCHEN_TICKET);
        //添加到 打印集合
        printTask.add(print);
        RedisUtil.set(print_id, print);
    }


    private void getRecommendModel(String recommendId, Order order, Printer printer, ShopDetail shopDetail, List<Map<String, Object>> printTask) {
        //桌号
        String tableNumber = order.getTableNumber() != null ? order.getTableNumber() : "";
        List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
        List<OrderItem> orderItems = orderitemMapper.getOrderItemByRecommendId(recommendId, order.getId());
        StringBuilder sb = new StringBuilder();
        for (OrderItem orderItem : orderItems) {
            String articleId = orderItem.getArticleId();

            if (articleId.length() > 32) {
                articleId = orderItem.getArticleId().substring(0, 32);
            }
            Article article = articleService.selectById(articleId);
            if (article.getVirtualId() == null) {
                Map<String, Object> item = new HashMap<String, Object>();
                item.put("ARTICLE_NAME", orderItem.getArticleName());
                sb.append(orderItem.getArticleName() + " ");
                item.put("ARTICLE_COUNT", orderItem.getCount());
                items.add(item);
            }

        }
        String serialNumber = order.getSerialNumber();//序列号
        String modeText = getModeText(order);//就餐模式

        //保存基本信息
        Map<String, Object> print = new HashMap<String, Object>();
        print.put("PORT", printer.getPort());
        print.put("OID", order.getId());
        print.put("IP", printer.getIp());
        String print_id = ApplicationUtils.randomUUID();
        print.put("PRINT_TASK_ID", print_id);
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("ORDER_ID", serialNumber);
        data.put("DATETIME", DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
        data.put("DISTRIBUTION_MODE", modeText);
        data.put("TABLE_NUMBER", tableNumber);
        //添加当天打印订单的序号

        data.put("ORDER_NUMBER", RedisUtil.get(order.getId() + "orderNumber"));
//        data.put("ORDER_NUMBER", nextNumber(order.getShopDetailId(), order.getId()));
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
        if (customer != null) {
            CustomerDetail customerDetail = customerDetailMapper.selectByPrimaryKey(customer.getCustomerDetailId());
            if (customerDetail != null) {
                if (customerDetail.getBirthDate() != null) {
                    if (DateUtil.formatDate(customerDetail.getBirthDate(), "MM-dd")
                            .equals(DateUtil.formatDate(new Date(), "MM-dd"))) {
                        customerStr.append("★" + DateUtil.formatDate(customerDetail.getBirthDate(), "yyyy-MM-dd") + "★");
                    }
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
        RedisUtil.set(print_id, print);
        List<String> printList = (List<String>) RedisUtil.get(shopDetail.getId() + "printList");
        if (printList == null) {
            printList = new ArrayList<>();
        }
        printList.add(print_id);
        if (customer != null) {
            RedisUtil.set(print_id + "customer", customer);
        }
        RedisUtil.set(print_id + "article", sb.toString());
        RedisUtil.set(shopDetail.getId() + "printList", printList);
    }

    private void getRecommendModelNew(String recommendId, Order order, Printer printer, ShopDetail shopDetail, List<Map<String, Object>> printTask) {
        //桌号
        String tableNumber = order.getTableNumber() != null ? order.getTableNumber() : "";
        List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
        List<OrderItem> orderItems = orderitemMapper.getOrderItemByRecommendId(recommendId, order.getId());
        StringBuilder sb = new StringBuilder();
        for (OrderItem orderItem : orderItems) {
            String articleId = orderItem.getArticleId();

            if (articleId.length() > 32) {
                articleId = orderItem.getArticleId().substring(0, 32);
            }
            Article article = articleService.selectById(articleId);
            if (article.getVirtualId() == null) {
                Map<String, Object> item = new HashMap<String, Object>();
                item.put("ARTICLE_NAME", orderItem.getArticleName());
                sb.append(orderItem.getArticleName() + " ");
                item.put("ARTICLE_COUNT", orderItem.getCount());
                items.add(item);
            }

        }
        String serialNumber = order.getSerialNumber();//序列号
        String modeText = getModeText(order);//就餐模式

        //保存基本信息
        Map<String, Object> print = new HashMap<String, Object>();
        print.put("PORT", printer.getPort());
        print.put("OID", order.getId());
        print.put("IP", printer.getIp());
        String print_id = ApplicationUtils.randomUUID();
        print.put("PRINT_STATUS", order.getPrintKitchenFlag());
        ArticleRecommend articleRecommend = articleRecommendMapper.getRecommendById(recommendId);
        print.put("PRINT_TASK_ID", ApplicationUtils.randomUUID());
        print.put("TASK_ID", recommendId);

        print.put("TASK_ORDER_ID", order.getId());
        print.put("LINE_WIDTH", shopDetail.getPageSize() == 0 ? 48 : 42);
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("ORDER_ID", serialNumber);
        data.put("DATETIME", DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
        data.put("DISTRIBUTION_MODE", modeText);
        data.put("TABLE_NUMBER", tableNumber);
        //添加当天打印订单的序号

        data.put("ORDER_NUMBER", RedisUtil.get(order.getId() + "orderNumber"));
//        data.put("ORDER_NUMBER", nextNumber(order.getShopDetailId(), order.getId()));
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
        if (customer != null) {
            CustomerDetail customerDetail = customerDetailMapper.selectByPrimaryKey(customer.getCustomerDetailId());
            if (customerDetail != null) {
                if (customerDetail.getBirthDate() != null) {
                    if (DateUtil.formatDate(customerDetail.getBirthDate(), "MM-dd")
                            .equals(DateUtil.formatDate(new Date(), "MM-dd"))) {
                        customerStr.append("★" + DateUtil.formatDate(customerDetail.getBirthDate(), "yyyy-MM-dd") + "★");
                    }
                }
            }
        }
        data.put("CUSTOMER_PROPERTY", customerStr.toString());
        print.put("DATA", data);
        print.put("STATUS", "0");
        print.put("TICKET_TYPE", TicketTypeNew.TICKET);
        print.put("TICKET_MODE", TicketTypeNew.KITCHEN_TICKET);
        //保存打印配置信息
//                print.put("ORDER_ID", serialNumber);
//                print.put("KITCHEN_NAME", kitchen.getName());
//                print.put("TABLE_NO", tableNumber);
        //添加到 打印集合
        printTask.add(print);
        RedisUtil.set(print_id, print);
        List<String> printList = (List<String>) RedisUtil.get(shopDetail.getId() + "printList");
        if (printList == null) {
            printList = new ArrayList<>();
        }
        printList.add(print_id);
        if (customer != null) {
            RedisUtil.set(print_id + "customer", customer);
        }
        RedisUtil.set(print_id + "article", sb.toString());
        RedisUtil.set(shopDetail.getId() + "printList", printList);
    }


    private void getKitchenLabel(OrderItem article, Order order, Printer printer,
                                 ShopDetail shopDetail, List<Map<String, Object>> printTask, Map map, List<OrderItem> orderItems) {
        int currentCount = (int) map.get(article.getArticleId());
        int i = 0;

        for (OrderItem orderItem : orderItems) {
            if (article == null) {
                continue;
            }

            if (article.getType() == OrderItemType.SETMEALS
                    && orderItem.getParentId() != null && orderItem.getParentId().equals(article.getId())) {
                i++;
            } else if (article.getType() == OrderItemType.MEALS_CHILDREN
                    && article.getParentId().equals(orderItem.getParentId())) {
                i++;
            } else if (article.getType() != OrderItemType.SETMEALS
                    && article.getType() != OrderItemType.MEALS_CHILDREN && article.getArticleId().equals(orderItem.getArticleId())) {
                i += article.getCount().intValue();
            }
        }


        String tableNumber = order.getTableNumber() != null ? order.getTableNumber() : "";
        String modeText = getModeText(order);//就餐模式
        Map<String, Object> print = new HashMap<String, Object>();
        print.put("TABLE_NO", tableNumber);
        print.put("OID", order.getId());
        print.put("KITCHEN_NAME", printer.getName());
        print.put("PORT", printer.getPort());
        print.put("ORDER_ID", order.getSerialNumber());
        print.put("IP", printer.getIp());
        String print_id = ApplicationUtils.randomUUID();
        print.put("PRINT_TASK_ID", print_id);
        print.put("ADD_TIME", new Date());
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("ORDER_ID", order.getSerialNumber());
        data.put("ARTICLE_NAME", article.getArticleName());
        data.put("ARTICLE_NUMBER", currentCount + "/" + i);
        data.put("DISTRIBUTION_MODE", modeText);
        data.put("ORIGINAL_AMOUNT", order.getOriginalAmount());
        data.put("RESTAURANT_ADDRESS", shopDetail.getAddress());
        data.put("REDUCTION_AMOUNT", order.getOriginalAmount().subtract(order.getAmountWithChildren().doubleValue() == 0.0 ? order.getOrderMoney() : order.getAmountWithChildren()));
        Customer customer = customerService.selectById(order.getCustomerId());
        String phone = order.getVerCode();
        if (customer != null) {
            phone = StringUtils.isEmpty(customer.getTelephone()) ? order.getVerCode() : customer.getTelephone();
        }

        data.put("CUSTOMER_TEL", phone);
        data.put("TABLE_NUMBER", order.getTableNumber());
        data.put("PAYMENT_AMOUNT", order.getOrderMoney());
        data.put("RESTAURANT_NAME", shopDetail.getName());

        data.put("DATETIME", DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
        data.put("ARTICLE_COUNT", order.getArticleCount());
        print.put("DATA", data);
        print.put("STATUS", 0);
        print.put("TICKET_TYPE", TicketType.RESTAURANTLABEL);
        printTask.add(print);
        RedisUtil.set(print_id, print);
        if (customer != null) {
            RedisUtil.set(print_id + "customer", customer);
        }
        RedisUtil.set(print_id + "article", article.getArticleName());
        List<String> printList = (List<String>) RedisUtil.get(shopDetail.getId() + "printList");
        if (printList == null) {
            printList = new ArrayList<>();
        }
        printList.add(print_id);
        RedisUtil.set(shopDetail.getId() + "printList", printList);

    }


    private void getKitchenLabelNew(OrderItem article, Order order, Printer printer,
                                    ShopDetail shopDetail, List<Map<String, Object>> printTask, Map map, List<OrderItem> orderItems) {
        int currentCount = (int) map.get(article.getArticleId());
        int i = 0;

        for (OrderItem orderItem : orderItems) {
            if (article == null) {
                continue;
            }

            if (article.getType() == OrderItemType.SETMEALS
                    && article.getId().equals(orderItem.getParentId())) {
                i++;
            } else if (article.getType() == OrderItemType.MEALS_CHILDREN
                    && article.getParentId() != null && article.getParentId().equals(orderItem.getParentId())) {
                i++;
            } else if (article.getType() != OrderItemType.SETMEALS
                    && article.getType() != OrderItemType.MEALS_CHILDREN && article.getArticleId().equals(orderItem.getArticleId())) {
                i += article.getCount().intValue();
            }
        }


        String tableNumber = order.getTableNumber() != null ? order.getTableNumber() : "";
        String modeText = getModeText(order);//就餐模式
        Map<String, Object> print = new HashMap<String, Object>();
        print.put("PRINT_STATUS", order.getPrintKitchenFlag());
        print.put("PRINT_TASK_ID", ApplicationUtils.randomUUID());
        print.put("TASK_ID", article.getId());
        print.put("TASK_ORDER_ID", article.getOrderId());
        print.put("TABLE_NO", tableNumber);
        print.put("OID", order.getId());
        print.put("KITCHEN_NAME", printer.getName());
        print.put("PORT", printer.getPort());
        print.put("ORDER_ID", order.getSerialNumber());
        print.put("IP", printer.getIp());
        String print_id = ApplicationUtils.randomUUID();
        print.put("ADD_TIME", new Date().getTime());
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("ORDER_ID", order.getSerialNumber());
        data.put("ARTICLE_NAME", article.getArticleName());
        data.put("ARTICLE_NUMBER", currentCount + "/" + i);
        data.put("DISTRIBUTION_MODE", modeText);
        data.put("ORIGINAL_AMOUNT", order.getOriginalAmount());
        data.put("RESTAURANT_ADDRESS", shopDetail.getAddress());
        data.put("REDUCTION_AMOUNT", order.getOriginalAmount().subtract(order.getAmountWithChildren().doubleValue() == 0.0 ? order.getOrderMoney() : order.getAmountWithChildren()));
        Customer customer = customerService.selectById(order.getCustomerId());
        String phone = order.getVerCode();
        if (customer != null) {
            phone = StringUtils.isEmpty(customer.getTelephone()) ? order.getVerCode() : customer.getTelephone();
        }

        data.put("CUSTOMER_TEL", phone);
        data.put("TABLE_NUMBER", order.getTableNumber());
        data.put("PAYMENT_AMOUNT", order.getOrderMoney());
        data.put("RESTAURANT_NAME", shopDetail.getName());

        data.put("DATETIME", DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
        data.put("ARTICLE_COUNT", order.getArticleCount());
        print.put("DATA", data);
        print.put("STATUS", 0);
        print.put("TICKET_TYPE", TicketTypeNew.LABEL);
        print.put("TICKET_MODE", TicketTypeNew.RESTAURANT_LABEL);
        printTask.add(print);
        RedisUtil.set(print_id, print);
        if (customer != null) {
            RedisUtil.set(print_id + "customer", customer);
        }
        RedisUtil.set(print_id + "article", article.getArticleName());
        List<String> printList = (List<String>) RedisUtil.get(shopDetail.getId() + "printList");
        if (printList == null) {
            printList = new ArrayList<>();
        }
        printList.add(print_id);
        RedisUtil.set(shopDetail.getId() + "printList", printList);

    }


    private void getKitchenModel(OrderItem article, Order order, Printer printer, ShopDetail shopDetail, List<Map<String, Object>> printTask) {
        //保存 菜品的名称和数量
        List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
        StringBuilder sb = new StringBuilder();
        Map<String, Object> item = new HashMap<String, Object>();
        sb.append(article.getArticleName() + " ");
        item.put("ARTICLE_NAME", article.getArticleName());
        item.put("ARTICLE_COUNT", article.getCount());
        items.add(item);
        String serialNumber = order.getSerialNumber();//序列号
        String modeText = getModeText(order);//就餐模式


        if (article.getType() == OrderItemType.SETMEALS) {
            if (article.getChildren() != null && !article.getChildren().isEmpty()) {
                List<OrderItem> list = orderitemMapper.getListBySort(article.getId(), article.getArticleId());
                for (OrderItem obj : list) {
                    Map<String, Object> child_item = new HashMap<String, Object>();
                    child_item.put("ARTICLE_NAME", obj.getArticleName());
                    sb.append(obj.getArticleName() + " ");
                    if (order.getIsRefund() != null && order.getIsRefund() == Common.YES) {
                        child_item.put("ARTICLE_COUNT", obj.getRefundCount());
                    } else {
                        child_item.put("ARTICLE_COUNT", obj.getCount());
                    }

                    items.add(child_item);
                }
            }
        } else {
            if (!CollectionUtils.isEmpty(article.getChildren())) {
                for (OrderItem obj : article.getChildren()) {
                    Map<String, Object> child_item = new HashMap<String, Object>();
                    child_item.put("ARTICLE_NAME", obj.getArticleName());
                    sb.append(obj.getArticleName() + " ");
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
        print.put("OID", order.getId());
        print.put("IP", printer.getIp());
        String print_id = ApplicationUtils.randomUUID();
        print.put("PRINT_TASK_ID", print_id);
        print.put("ORDER_ID", serialNumber);
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("ORDER_ID", serialNumber);
        data.put("DATETIME", DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
        data.put("DISTRIBUTION_MODE", modeText);
        data.put("TABLE_NUMBER", order.getTableNumber());
        if (StringUtils.isNotBlank(order.getRemark())) {
            data.put("MEMO", "备注：" + order.getRemark());
        }
//        data.put("ORDER_NUMBER", nextNumber(order.getShopDetailId(), order.getId()));
        data.put("ORDER_NUMBER", RedisUtil.get(order.getId() + "orderNumber"));
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
        if (customer != null) {
            CustomerDetail customerDetail = customerDetailMapper.selectByPrimaryKey(customer.getCustomerDetailId());
            if (customerDetail != null) {
                if (customerDetail.getBirthDate() != null) {
                    if (DateUtil.formatDate(customerDetail.getBirthDate(), "MM-dd")
                            .equals(DateUtil.formatDate(new Date(), "MM-dd"))) {
                        customerStr.append("★" + DateUtil.formatDate(customerDetail.getBirthDate(), "yyyy-MM-dd") + "★");
                    }
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

        RedisUtil.set(print_id, print);
        if (customer != null) {
            RedisUtil.set(print_id + "customer", customer);
        }
        RedisUtil.set(print_id + "article", sb.toString());
        List<String> printList = (List<String>) RedisUtil.get(shopDetail.getId() + "printList");
        if (printList == null) {
            printList = new ArrayList<>();
        }
        printList.add(print_id);
        RedisUtil.set(shopDetail.getId() + "printList", printList);
    }


    private void getKitchenModelNew(OrderItem article, Order order, Printer printer, ShopDetail shopDetail, List<Map<String, Object>> printTask) {
        //保存 菜品的名称和数量
        List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
        StringBuilder sb = new StringBuilder();
        Map<String, Object> item = new HashMap<String, Object>();
        sb.append(article.getArticleName() + " ");
        item.put("ARTICLE_NAME", article.getArticleName());
        item.put("ARTICLE_COUNT", article.getCount());
        items.add(item);
        String serialNumber = order.getSerialNumber();//序列号
        String modeText = getModeText(order);//就餐模式


        if (article.getType() == OrderItemType.SETMEALS) {
            if (article.getChildren() != null && !article.getChildren().isEmpty()) {
                List<OrderItem> list = orderitemMapper.getListBySort(article.getId(), article.getArticleId());
                for (OrderItem obj : list) {
                    Map<String, Object> child_item = new HashMap<String, Object>();
                    child_item.put("ARTICLE_NAME", obj.getArticleName());
                    sb.append(obj.getArticleName() + " ");
                    if (order.getIsRefund() != null && order.getIsRefund() == Common.YES) {
                        child_item.put("ARTICLE_COUNT", obj.getRefundCount());
                    } else {
                        child_item.put("ARTICLE_COUNT", obj.getCount());
                    }

                    items.add(child_item);
                }
            }
        } else {
            if (!CollectionUtils.isEmpty(article.getChildren())) {
                for (OrderItem obj : article.getChildren()) {
                    Map<String, Object> child_item = new HashMap<String, Object>();
                    child_item.put("ARTICLE_NAME", obj.getArticleName());
                    sb.append(obj.getArticleName() + " ");
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
        print.put("TABLE_NO", order.getTableNumber());
        print.put("OID", order.getId());
        print.put("IP", printer.getIp());
        String print_id = ApplicationUtils.randomUUID();
        print.put("PRINT_STATUS", order.getPrintKitchenFlag());
        print.put("PRINT_TASK_ID", ApplicationUtils.randomUUID());
        print.put("TASK_ID", article.getId());
        print.put("TASK_ORDER_ID", article.getOrderId());
        print.put("LINE_WIDTH", shopDetail.getPageSize() == 0 ? 48 : 42);
        print.put("ADD_TIME", new Date().getTime());
        print.put("ORDER_ID", serialNumber);
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("ORDER_ID", serialNumber);
        data.put("DATETIME", DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
        data.put("DISTRIBUTION_MODE", modeText);
        data.put("TABLE_NUMBER", order.getTableNumber());
        if (StringUtils.isNotBlank(order.getRemark())) {
            data.put("MEMO", "备注：" + order.getRemark());
        }
//        data.put("ORDER_NUMBER", nextNumber(order.getShopDetailId(), order.getId()));
        data.put("ORDER_NUMBER", RedisUtil.get(order.getId() + "orderNumber"));
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
        if (customer != null) {
            CustomerDetail customerDetail = customerDetailMapper.selectByPrimaryKey(customer.getCustomerDetailId());
            if (customerDetail != null) {
                if (customerDetail.getBirthDate() != null) {
                    if (DateUtil.formatDate(customerDetail.getBirthDate(), "MM-dd")
                            .equals(DateUtil.formatDate(new Date(), "MM-dd"))) {
                        customerStr.append("★" + DateUtil.formatDate(customerDetail.getBirthDate(), "yyyy-MM-dd") + "★");
                    }
                }
            }
        }
        data.put("CUSTOMER_PROPERTY", customerStr.toString());
        print.put("DATA", data);
        print.put("STATUS", "0");
        print.put("TICKET_TYPE", TicketTypeNew.TICKET);
        print.put("TICKET_MODE", TicketTypeNew.KITCHEN_TICKET);
        //保存打印配置信息
//                print.put("ORDER_ID", serialNumber);
//                print.put("KITCHEN_NAME", kitchen.getName());
//                print.put("TABLE_NO", tableNumber);
        //添加到 打印集合
        printTask.add(print);

        RedisUtil.set(print_id, print);
        if (customer != null) {
            RedisUtil.set(print_id + "customer", customer);
        }
        RedisUtil.set(print_id + "article", sb.toString());
        List<String> printList = (List<String>) RedisUtil.get(shopDetail.getId() + "printList");
        if (printList == null) {
            printList = new ArrayList<>();
        }
        printList.add(print_id);
        RedisUtil.set(shopDetail.getId() + "printList", printList);
    }


    private String getModeText(Order order) {
        if (order == null) {
            return "";
        }
        String text = DistributionType.getModeText(order.getDistributionModeId());
        if (order.getParentOrderId() != null && !order.getDistributionModeId().equals(DistributionType.REFUND_ORDER)) {  //如果是加菜的订单，会出现加的字样
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
            order.setOriginalAmount(order.getOriginalAmount().add(orderItem.getOriginalPrice().multiply(BigDecimal.valueOf(orderItem.getCount()))));
            order.setOrderMoney(order.getOrderMoney().add(orderItem.getFinalPrice()));
        }
        child.addAll(orderItems);

        List<Printer> printer = printerService.selectByShopAndType(shopDetail.getId(), PrinterType.RECEPTION);
        if (selectPrinterId == null) {
            if (printer.size() > 0) {
                if (shopDetail.getIsPosNew() == Common.YES) {
                    return printTicketPosNew(order, child, shopDetail, printer.get(0));
                } else {
                    return printTicket(order, child, shopDetail, printer.get(0));
                }

            }
        } else {
            Printer p = printerService.selectById(selectPrinterId);
            if (shopDetail.getIsPosNew() == Common.YES) {
                return printTicketPosNew(order, child, shopDetail, printer.get(0));
            } else {
                return printTicket(order, child, shopDetail, p);
            }

        }
        return null;
    }


    public Map<String, Object> printTicket(Order order, List<OrderItem> orderItems, ShopDetail shopDetail, Printer printer) {
        if (printer == null) {
            return null;
        }
        if (shopDetail.getIsPosNew() == Common.POS_NEW) {
            //如果是新版本pos
            return printTicketPosNew(order, orderItems, shopDetail, printer);
        }


        List<Map<String, Object>> items = new ArrayList<>();
        List<Map<String, Object>> refundItems = new ArrayList<>();
        List<String> articleIds = new ArrayList<>();
        for (OrderItem article : orderItems) {
            if (!article.getType().equals(OrderItemType.MEALS_CHILDREN)) {
                if (article.getArticleId().contains("@")) {
                    articleIds.add(article.getArticleId().substring(0, article.getArticleId().indexOf("@")));
                } else {
                    articleIds.add(article.getArticleId());
                }
            }
        }
        Map<String, Object> selectMap = new HashMap<>();
        selectMap.put("articleIds", articleIds);
        List<String> articleSort = articleService.selectArticleSort(selectMap);
        for (String articleId : articleSort) {
            for (OrderItem article : orderItems) {
                if (article.getType().equals(OrderItemType.SETMEALS) && articleId.equalsIgnoreCase(article.getArticleId())) {
                    getOrderItems(article, items, refundItems);
                    getOrderItemMeal(orderItems, items, refundItems, article.getId());
                } else if (!article.getType().equals(OrderItemType.MEALS_CHILDREN)) {
                    if (article.getArticleId().contains("@")) {
                        if (articleId.equalsIgnoreCase(article.getArticleId().substring(0, article.getArticleId().indexOf("@")))) {
                            getOrderItems(article, items, refundItems);
                        }
                    } else {
                        if (articleId.equalsIgnoreCase(article.getArticleId())) {
                            getOrderItems(article, items, refundItems);
                        }
                    }
                }
            }
        }
        BrandSetting brandSetting = brandSettingService.selectByBrandId(order.getBrandId());
        Brand brand = brandService.selectBrandBySetting(brandSetting.getId());

        if (order.getDistributionModeId() == 1) {
            if (order.getBaseCustomerCount() != null && order.getBaseCustomerCount() != 0
                    && StringUtils.isBlank(order.getParentOrderId())) {
                Map<String, Object> item = new HashMap<>();
                item.put("SUBTOTAL", shopDetail.getServicePrice().multiply(new BigDecimal(order.getBaseCustomerCount())));
                item.put("ARTICLE_NAME", shopDetail.getServiceName());
                if ("27f56b31669f4d43805226709874b530".equals(brand.getId())) {
                    item.put("ARTICLE_NAME", "就餐人数");
                }
                item.put("ARTICLE_COUNT", order.getBaseCustomerCount());
                items.add(item);
                if (order.getBaseCustomerCount() != order.getCustomerCount()) {
                    Map<String, Object> refundItem = new HashMap<>();
                    refundItem.put("SUBTOTAL", -shopDetail.getServicePrice().multiply(new BigDecimal((order.getBaseCustomerCount() - order.getCustomerCount()))).doubleValue());
                    refundItem.put("ARTICLE_NAME", shopDetail.getServiceName() + "(退)");
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
                List<String> childs = orderMapper.selectChildIdsByParentId(order.getId());
                BigDecimal mealCount = new BigDecimal(order.getBaseMealAllCount());
                BigDecimal mealAllNumber = BigDecimal.valueOf(order.getMealAllNumber());
                if (!CollectionUtils.isEmpty(childs)) {
                    for (String c : childs) {
                        Order childOrder = selectById(c);
                        mealCount = mealCount.add(BigDecimal.valueOf(childOrder.getBaseMealAllCount()));
                        mealAllNumber = mealAllNumber.add(BigDecimal.valueOf(childOrder.getMealAllNumber()));
                    }
                }
                item.put("SUBTOTAL", shopDetail.getMealFeePrice().multiply(mealCount));
                item.put("ARTICLE_NAME", shopDetail.getMealFeeName());
                item.put("ARTICLE_COUNT", mealCount);
                items.add(item);
                if (mealCount.doubleValue() != mealAllNumber.doubleValue()) {
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
        print.put("PRINT_STATE", order.getPrintFailFlag());
        print.put("KITCHEN_NAME", printer.getName());
        print.put("PORT", printer.getPort());
        print.put("ORDER_ID", order.getSerialNumber());
        print.put("IP", printer.getIp());
        String print_id = ApplicationUtils.randomUUID();
        print.put("PRINT_TASK_ID", print_id);
        print.put("PRINT_STATUS", order.getPrintFailFlag());
        print.put("ADD_TIME", new Date());

        Map<String, Object> data = new HashMap<>();
        if (StringUtils.isNotBlank(order.getRemark())) {
            data.put("MEMO", "备注：" + order.getRemark());
        }
        data.put("ORDER_ID", order.getSerialNumber() + "-" + order.getVerCode());
        String orderNumber = (String) RedisUtil.get(order.getId() + "orderNumber");
        Integer orderTotal = (Integer) RedisUtil.get(order.getShopDetailId() + "orderCount");
        if (orderTotal == null) {
            orderTotal = 1;
        } else if (orderNumber == null) {
            orderTotal++;
        }
        RedisUtil.set(order.getShopDetailId() + "orderCount", orderTotal);


        String number;
        if (orderTotal < 10) {
            number = "00" + orderTotal;
        } else if (orderTotal < 100) {
            number = "0" + orderTotal;
        } else {
            number = "" + orderTotal;
        }

        if (StringUtils.isEmpty(orderNumber)) {
            orderNumber = number;
        }
        RedisUtil.set(order.getId() + "orderNumber", orderNumber);

//        nextNumber(order.getShopDetailId(), order.getId())
        if (!brand.getId().equals("da7ffe9e6f74447f880d82a284a11cae")) {
            data.put("ORDER_NUMBER", orderNumber);
        }
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
            /*if (shopDetail.getConsumeNumber() > 0 && gaoCount > shopDetail.getConsumeNumber() && shopDetail.getConsumeConfineUnit() != 3) {
                gao.append("【高频】");
            }//无限制的时候
            else if (shopDetail.getConsumeConfineUnit() == 3 && gaoCountlong > shopDetail.getConsumeNumber()) {
                gao.append("【高频】");
            }*/
            if(gaoCount!=0){
                gao.append("消费"+gaoCount+"次");
            }else{
                gao.append("新顾客");
            }
        }
        String modeText = getModeText(order);
        data.put("DISTRIBUTION_MODE", modeText);
        if (order.getAmountWithChildren().doubleValue() > 0.0 && order.getPrintTimes() == 1 && order.getOrderMode() == ShopMode.HOUFU_ORDER) {
            data.put("ORIGINAL_AMOUNT", order.getAmountWithChildren());
        } else {
            data.put("ORIGINAL_AMOUNT", order.getOriginalAmount());
        }
        data.put("RESTAURANT_ADDRESS", shopDetail.getAddress());
        data.put("REDUCTION_AMOUNT", order.getOriginalAmount().subtract(order.getAmountWithChildren().doubleValue() == 0.0 ? order.getOrderMoney() : order.getAmountWithChildren()));
        data.put("RESTAURANT_TEL", shopDetail.getPhone());
        data.put("TABLE_NUMBER", order.getTableNumber());
        data.put("CUSTOMER_COUNT", order.getCustomerCount() == null ? "-" : order.getCustomerCount());
        data.put("PAYMENT_AMOUNT", order.getOrderMoney());
        if (order.getPayType() == PayType.NOPAY && (order.getOrderState() == OrderState.PAYMENT || order.getOrderState() == OrderState.CONFIRM)) {
            data.put("RESTAURANT_NAME", shopDetail.getName() + " (结账单)");
        } else if (order.getPayType() == PayType.NOPAY && order.getPayMode() != OrderPayMode.YUE_PAY && order.getOrderState() == OrderState.SUBMIT) {
            data.put("RESTAURANT_NAME", shopDetail.getName() + " (结账单)");
        } else if (order.getOrderState() == OrderState.SUBMIT && order.getPayType() == PayType.NOPAY) {
            data.put("RESTAURANT_NAME", shopDetail.getName() + " (消费清单)");
        } else {
            if (order.getParentOrderId() != null) {
                //加菜的话  判断他主订单  如果主订单是后付  则显示(结账单)
                Order faOrder = orderMapper.selectByPrimaryKey(order.getParentOrderId());
                if (faOrder.getPayType() == PayType.NOPAY) {
                    data.put("RESTAURANT_NAME", shopDetail.getName() + " (结账单)");
                } else {
                    data.put("RESTAURANT_NAME", shopDetail.getName());
                }
            } else {
                data.put("RESTAURANT_NAME", shopDetail.getName());
            }
        }

        data.put("DATETIME", DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
        BigDecimal articleCount = new BigDecimal(order.getArticleCount());
        if (order.getParentOrderId() == null) {
            articleCount = articleCount.add(new BigDecimal(order.getCustomerCount() == null ? 0
                    : order.getCustomerCount()));
            articleCount = articleCount.add(new BigDecimal(order.getMealAllNumber() == null ? 0
                    : order.getMealAllNumber()));
        }

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
        if (customer != null) {
            CustomerDetail customerDetail = customerDetailMapper.selectByPrimaryKey(customer.getCustomerDetailId());
            if (customerDetail != null) {
                if (customerDetail.getBirthDate() != null) {
                    if (DateUtil.formatDate(customerDetail.getBirthDate(), "MM-dd")
                            .equals(DateUtil.formatDate(new Date(), "MM-dd"))) {
                        customerStr.append("★" + DateUtil.formatDate(customerDetail.getBirthDate(), "yyyy-MM-dd") + "★");
                    }
                }
            }
        }
        data.put("CUSTOMER_PROPERTY", customerStr.toString());
        print.put("DATA", data);
        print.put("STATUS", 0);
        print.put("TICKET_TYPE", TicketType.RECEIPT);

        JSONObject json = new JSONObject(print);
        Map logMap = new HashMap(4);
        logMap.put("brandName", brand.getBrandName());
        logMap.put("fileName", shopDetail.getName());
        logMap.put("type", "posAction");
        logMap.put("content", "订单:" + order.getId() + "返回打印总单模版" + json.toString() + ",请求服务器地址为:" + MQSetting.getLocalIP());
        doPostAnsc(url, logMap);
        RedisUtil.set(print_id, print);
        List<String> printList = (List<String>) RedisUtil.get(shopDetail.getId() + "printList");
        if (printList == null) {
            printList = new ArrayList<>();
        }
        printList.add(print_id);
        RedisUtil.set(shopDetail.getId() + "printList", printList);
        return print;
    }


    public Map<String, Object> printTicketPosNew(Order order, List<OrderItem> orderItems, ShopDetail shopDetail, Printer printer) {
        List<Map<String, Object>> items = new ArrayList<>();
        List<Map<String, Object>> refundItems = new ArrayList<>();
        List<String> articleIds = new ArrayList<>();
        for (OrderItem article : orderItems) {
            if (!article.getType().equals(OrderItemType.MEALS_CHILDREN)) {
                if (article.getArticleId().contains("@")) {
                    articleIds.add(article.getArticleId().substring(0, article.getArticleId().indexOf("@")));
                } else {
                    articleIds.add(article.getArticleId());
                }
            }
        }
        Map<String, Object> selectMap = new HashMap<>();
        selectMap.put("articleIds", articleIds);
        List<String> articleSort = articleService.selectArticleSort(selectMap);
        for (String articleId : articleSort) {
            for (OrderItem article : orderItems) {
                if (article.getType().equals(OrderItemType.SETMEALS) && articleId.equalsIgnoreCase(article.getArticleId())) {
                    getOrderItems(article, items, refundItems);
                    getOrderItemMeal(orderItems, items, refundItems, article.getId());
                } else if (!article.getType().equals(OrderItemType.MEALS_CHILDREN)) {
                    if (article.getArticleId().contains("@")) {
                        if (articleId.equalsIgnoreCase(article.getArticleId().substring(0, article.getArticleId().indexOf("@")))) {
                            getOrderItems(article, items, refundItems);
                        }
                    } else {
                        if (articleId.equalsIgnoreCase(article.getArticleId())) {
                            getOrderItems(article, items, refundItems);
                        }
                    }
                }
            }
        }
        BrandSetting brandSetting = brandSettingService.selectByBrandId(order.getBrandId());
        Brand brand = brandService.selectBrandBySetting(brandSetting.getId());

        if (order.getDistributionModeId() == 1) {
            if (order.getBaseCustomerCount() != null && order.getBaseCustomerCount() != 0
                    && StringUtils.isBlank(order.getParentOrderId())) {
                Map<String, Object> item = new HashMap<>();
                item.put("SUBTOTAL", shopDetail.getServicePrice().multiply(new BigDecimal(order.getBaseCustomerCount())));
                item.put("ARTICLE_NAME", shopDetail.getServiceName());
                if ("27f56b31669f4d43805226709874b530".equals(brand.getId())) {
                    item.put("ARTICLE_NAME", "就餐人数");
                }
                item.put("ARTICLE_COUNT", order.getBaseCustomerCount());
                items.add(item);
                if (order.getBaseCustomerCount() != order.getCustomerCount()) {
                    Map<String, Object> refundItem = new HashMap<>();
                    refundItem.put("SUBTOTAL", -shopDetail.getServicePrice().multiply(new BigDecimal((order.getBaseCustomerCount() - order.getCustomerCount()))).doubleValue());
                    refundItem.put("ARTICLE_NAME", shopDetail.getServiceName() + "(退)");
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
                List<String> childs = orderMapper.selectChildIdsByParentId(order.getId());
                BigDecimal mealCount = new BigDecimal(order.getBaseMealAllCount());
                BigDecimal mealAllNumber = BigDecimal.valueOf(order.getMealAllNumber());
                if (!CollectionUtils.isEmpty(childs)) {
                    for (String c : childs) {
                        Order childOrder = selectById(c);
                        mealCount = mealCount.add(BigDecimal.valueOf(childOrder.getBaseMealAllCount()));
                        mealAllNumber = mealAllNumber.add(BigDecimal.valueOf(childOrder.getMealAllNumber()));
                    }
                }
                item.put("SUBTOTAL", shopDetail.getMealFeePrice().multiply(mealCount));
                item.put("ARTICLE_NAME", shopDetail.getMealFeeName());
                item.put("ARTICLE_COUNT", mealCount);
                items.add(item);
                if (mealCount.doubleValue() != mealAllNumber.doubleValue()) {
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
        print.put("TASK_ORDER_ID", order.getId());
        print.put("KITCHEN_NAME", printer.getName());
        print.put("PORT", printer.getPort());
        print.put("PRINT_STATUS", order.getPrintFailFlag());
        print.put("LINE_WIDTH", 42);
        print.put("ORDER_ID", order.getSerialNumber());
        print.put("IP", printer.getIp());
        String print_id = ApplicationUtils.randomUUID();
        print.put("PRINT_TASK_ID", print_id);
        print.put("ADD_TIME", new Date().getTime());

        Map<String, Object> data = new HashMap<>();
        if (StringUtils.isNotBlank(order.getRemark())) {
            data.put("MEMO", "备注：" + order.getRemark());
        }
        data.put("ORDER_ID", order.getSerialNumber() + "-" + order.getVerCode());
        String orderNumber = (String) RedisUtil.get(order.getId() + "orderNumber");
        Integer orderTotal = (Integer) RedisUtil.get(order.getShopDetailId() + "orderCount");
        if (orderTotal == null) {
            orderTotal = 1;
        } else if (orderNumber == null) {
            orderTotal++;
        }
        RedisUtil.set(order.getShopDetailId() + "orderCount", orderTotal);


        String number;
        if (orderTotal < 10) {
            number = "00" + orderTotal;
        } else if (orderTotal < 100) {
            number = "0" + orderTotal;
        } else {
            number = "" + orderTotal;
        }

        if (StringUtils.isEmpty(orderNumber)) {
            orderNumber = number;
        }
        RedisUtil.set(order.getId() + "orderNumber", orderNumber);

//        nextNumber(order.getShopDetailId(), order.getId())
        data.put("ORDER_NUMBER", orderNumber);
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
        if (order.getAmountWithChildren().doubleValue() > 0.0 && order.getPrintTimes() == 1 && order.getOrderMode() == ShopMode.HOUFU_ORDER) {
            data.put("ORIGINAL_AMOUNT", order.getAmountWithChildren());
        } else {
            data.put("ORIGINAL_AMOUNT", order.getOriginalAmount());
        }
        data.put("RESTAURANT_ADDRESS", shopDetail.getAddress());
        data.put("REDUCTION_AMOUNT", order.getOriginalAmount().subtract(order.getAmountWithChildren().doubleValue() == 0.0 ? order.getOrderMoney() : order.getAmountWithChildren()));
        data.put("RESTAURANT_TEL", shopDetail.getPhone());
        data.put("TABLE_NUMBER", order.getTableNumber());
        data.put("CUSTOMER_COUNT", order.getCustomerCount() == null ? "-" : order.getCustomerCount());
        data.put("PAYMENT_AMOUNT", order.getOrderMoney());
        if (order.getPayType() == PayType.NOPAY && (order.getOrderState() == OrderState.PAYMENT || order.getOrderState() == OrderState.CONFIRM)) {
            data.put("RESTAURANT_NAME", shopDetail.getName() + " (结账单)");
        } else if (order.getPayType() == PayType.NOPAY && order.getPayMode() != OrderPayMode.YUE_PAY && order.getOrderState() == OrderState.SUBMIT) {
            data.put("RESTAURANT_NAME", shopDetail.getName() + " (结账单)");
        } else if (order.getOrderState() == OrderState.SUBMIT && order.getPayType() == PayType.NOPAY) {
            data.put("RESTAURANT_NAME", shopDetail.getName() + " (消费清单)");
        } else {
            if (order.getParentOrderId() != null) {
                //加菜的话  判断他主订单  如果主订单是后付  则显示(结账单)
                Order faOrder = orderMapper.selectByPrimaryKey(order.getParentOrderId());
                if (faOrder.getPayType() == PayType.NOPAY) {
                    data.put("RESTAURANT_NAME", shopDetail.getName() + " (结账单)");
                } else {
                    data.put("RESTAURANT_NAME", shopDetail.getName());
                }
            } else {
                data.put("RESTAURANT_NAME", shopDetail.getName());
            }
        }

        data.put("DATETIME", DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
        BigDecimal articleCount = new BigDecimal(order.getArticleCount());
        if (order.getParentOrderId() == null) {
            articleCount = articleCount.add(new BigDecimal(order.getCustomerCount() == null ? 0
                    : order.getCustomerCount()));
            articleCount = articleCount.add(new BigDecimal(order.getMealAllNumber() == null ? 0
                    : order.getMealAllNumber()));
        }

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
        if (customer != null) {
            CustomerDetail customerDetail = customerDetailMapper.selectByPrimaryKey(customer.getCustomerDetailId());
            if (customerDetail != null) {
                if (customerDetail.getBirthDate() != null) {
                    if (DateUtil.formatDate(customerDetail.getBirthDate(), "MM-dd")
                            .equals(DateUtil.formatDate(new Date(), "MM-dd"))) {
                        customerStr.append("★" + DateUtil.formatDate(customerDetail.getBirthDate(), "yyyy-MM-dd") + "★");
                    }
                }
            }
        }
        data.put("CUSTOMER_PROPERTY", customerStr.toString());
        print.put("DATA", data);
        print.put("STATUS", 0);
        print.put("TICKET_TYPE", TicketTypeNew.TICKET);
        print.put("TICKET_MODE", TicketTypeNew.RESTAURANT_RECEIPT);
        JSONObject json = new JSONObject(print);
        Map logMap = new HashMap(4);
        logMap.put("brandName", brand.getBrandName());
        logMap.put("fileName", shopDetail.getName());
        logMap.put("type", "posAction");
        logMap.put("content", "订单:" + order.getId() + "返回打印总单模版" + json.toString() + ",请求服务器地址为:" + MQSetting.getLocalIP());
        doPostAnsc(url, logMap);
        RedisUtil.set(print_id, print);
        List<String> printList = (List<String>) RedisUtil.get(shopDetail.getId() + "printList");
        if (printList == null) {
            printList = new ArrayList<>();
        }
        printList.add(print_id);
        RedisUtil.set(shopDetail.getId() + "printList", printList);
        return print;
    }



    public void getOrderItems(OrderItem article, List<Map<String, Object>> items, List<Map<String, Object>> refundItems) {
        Map<String, Object> item = new HashMap<>();
        item.put("SUBTOTAL", article.getOriginalPrice().multiply(new BigDecimal(article.getChangeCount() != null && article.getChangeCount().compareTo(0) > 0 ? article.getChangeCount() : article.getOrginCount())));
        item.put("ARTICLE_NAME", article.getArticleName());
        item.put("ARTICLE_COUNT", article.getChangeCount() != null && article.getChangeCount().compareTo(0) > 0 ? article.getChangeCount() : article.getOrginCount());
        items.add(item);
        if (article.getRefundCount() != 0) {
            Map<String, Object> refundItem = new HashMap<>();
            refundItem.put("SUBTOTAL", -article.getOriginalPrice().multiply(new BigDecimal(article.getRefundCount())).doubleValue());
            if (article.getArticleName().contains("加")) {
                article.setArticleName(article.getArticleName().substring(0, article.getArticleName().indexOf("(") - 1));
            }
            refundItem.put("ARTICLE_NAME", article.getArticleName() + "(退)");
            refundItem.put("ARTICLE_COUNT", -article.getRefundCount());
            refundItems.add(refundItem);
        }
    }

    public void getOrderItemMeal(List<OrderItem> orderItems, List<Map<String, Object>> items, List<Map<String, Object>> refundItems, String articleId) {
        for (OrderItem article : orderItems) {
            if (article.getParentId() != null && article.getParentId().equals(articleId)) {
                Map<String, Object> item = new HashMap<>();
                item.put("SUBTOTAL", article.getOriginalPrice().multiply(new BigDecimal(article.getOrginCount())));
                item.put("ARTICLE_NAME", article.getArticleName());
                item.put("ARTICLE_COUNT", article.getOrginCount());
                items.add(item);
                if (article.getRefundCount() != 0) {
                    Map<String, Object> refundItem = new HashMap<>();
                    refundItem.put("SUBTOTAL", -article.getOriginalPrice().multiply(new BigDecimal(article.getRefundCount())).doubleValue());
                    if (article.getArticleName().contains("加")) {
                        article.setArticleName(article.getArticleName().substring(0, article.getArticleName().indexOf("(") - 1));
                    }
                    refundItem.put("ARTICLE_NAME", article.getArticleName() + "(退)");
                    refundItem.put("ARTICLE_COUNT", -article.getRefundCount());
                    refundItems.add(refundItem);
                }
            }
        }
    }

    @Override
    public Order confirmOrder(Order order) {
        order = selectById(order.getId());
        if (order.getOrderState() != OrderState.PAYMENT) {
            return null;
        }
        if (order.getProductionStatus() == ProductionStatus.REFUND_ARTICLE) {
            return null;
        }
        Brand brand = brandService.selectById(order.getBrandId());
        ShopDetail shopDetail = shopDetailService.selectById(order.getShopDetailId());
        log.info("开始确认订单:" + order.getId());
        Integer orginState = order.getOrderState();//订单开始确认的状体
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
            //Map orderMap = new HashMap(4);
//            orderMap.put("brandName", brand.getBrandName());
//            orderMap.put("fileName", order.getId());
//            orderMap.put("type", "orderAction");
//            orderMap.put("content", "订单:" + order.getId() + "被确认订单状态更改为10,请求服务器地址为:" + MQSetting.getLocalIP());
//            doPostAnsc(url, orderMap);
            /**
             * 记录订单自动确认2-10过程
             */
            LogTemplateUtils.getConfirmByOrderType(brand.getBrandName(), order, orginState, "confirmOrder");

            return order;
        }
        return null;
    }

    @Override
    public Order confirmWaiMaiOrder(Order order) {
        order = selectById(order.getId());
        if (order.getProductionStatus() == ProductionStatus.REFUND_ARTICLE) {
            return null;
        }
        Brand brand = brandService.selectById(order.getBrandId());
        ShopDetail shopDetail = shopDetailService.selectById(order.getShopDetailId());
        log.info("开始确认订单:" + order.getId());
        Integer orginState = order.getOrderState();//订单开始确认的状体
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
            //Map orderMap = new HashMap(4);
//            orderMap.put("brandName", brand.getBrandName());
//            orderMap.put("fileName", order.getId());
//            orderMap.put("type", "orderAction");
//            orderMap.put("content", "订单:" + order.getId() + "被确认订单状态更改为10,请求服务器地址为:" + MQSetting.getLocalIP());
//            doPostAnsc(url, orderMap);
            /**
             * 记录订单自动确认2-10过程
             */
            LogTemplateUtils.getConfirmByOrderType(brand.getBrandName(), order, orginState, "confirmOrder");

            return order;
        }
        return null;
    }

    @Override
    public Order confirmBossOrder(Order order) {
        order = selectById(order.getId());
        if (order.getOrderState() != OrderState.PAYMENT) {
            return null;
        }
        if (order.getProductionStatus() == ProductionStatus.REFUND_ARTICLE) {
            return null;
        }
        Integer orginState = order.getOrderState();
        Brand brand = brandService.selectById(order.getBrandId());
        ShopDetail shopDetail = shopDetailService.selectById(order.getShopDetailId());
        log.info("开始确认订单:" + order.getId());
        if (order.getConfirmTime() == null && !order.getClosed()) {
            order.setOrderState(OrderState.CONFIRM);
            order.setConfirmTime(new Date());
            order.setAllowCancel(false);
            order.setAllowContinueOrder(false);
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
//            Map orderMap = new HashMap(4);
//            orderMap.put("brandName", brand.getBrandName());
//            orderMap.put("fileName", order.getId());
//            orderMap.put("type", "orderAction");
//            orderMap.put("content", "订单:" + order.getId() + "被确认订单状态更改为10,请求服务器地址为:" + MQSetting.getLocalIP());
//            doPostAnsc(url, orderMap);
            LogTemplateUtils.getConfirmByOrderType(brand.getBrandName(), order, orginState, "confirmBossOrder");
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
        return orderMapper.listHoufuFinishedOrder(currentShopId, shopMode);
    }

    @Override
    public List<Order> selectErrorOrderList(String currentShopId, Date date) {
        Date begin = DateUtil.getDateBegin(date);
        Date end = DateUtil.getDateEnd(date);
        return orderMapper.selectErrorOrderList(currentShopId, begin, end);
    }

    @Override
    public List<Order> selectErrorOrder(Date date) {
        Date begin = DateUtil.getDateBegin(date);
        Date end = DateUtil.getDateEnd(date);
        return orderMapper.selectErrorOrder(begin, end);
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
        Brand brand = brandService.selectById(order.getBrandId());
        ShopDetail shopDetail = shopDetailService.selectById(order.getShopDetailId());
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
//            orderMapper.setStockBySuit(order.getShopDetailId());//自动更新套餐数量
//            Map map = new HashMap(4);
//            map.put("brandName", brand.getBrandName());
//            map.put("fileName", shopDetail.getName());
//            map.put("type", "posAction");
//            map.put("content", "店铺:"+shopDetail.getName()+"在pos端执行拒绝订单:" + order.getId() + ",请求服务器地址为:" + MQSetting.getLocalIP());
//            doPostAnsc(url, map);
            LogTemplateUtils.cancelOrderByOrderType(brand.getBrandName(), orderId);
            LogTemplateUtils.cancleOrderByPosType(brand.getBrandName(), shopDetail.getName(), order.getId());

//            Map orderMap = new HashMap(4);
//            orderMap.put("brandName", brand.getBrandName());
//            orderMap.put("fileName", orderId);
//            orderMap.put("type", "orderAction");
//            orderMap.put("content", "订单:" + order.getId() + "在pos端被拒绝,请求服务器地址为:" + MQSetting.getLocalIP());
//            doPostAnsc(url, map);
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
        List<Map<String, Object>> printTask = new ArrayList<>();
        log.info("打印订单全部:" + orderId);
        Order order = selectById(orderId);
        if (order.getPrintTimes() != 1) {
            if (orderList.contains(orderId)) {
                return printTask;
            } else {
                orderList.add(orderId);
            }

        }
        List<OrderItem> items = orderItemService.listByOrderId(orderId);


        TableQrcode tableQrcode = tableQrcodeService.selectByTableNumberShopId(order.getShopDetailId(), Integer.valueOf(order.getTableNumber()));


        ShopDetail shopDetail = shopDetailService.selectById(order.getShopDetailId());
        BrandSetting setting = brandSettingService.selectByBrandId(order.getBrandId());
        List<Printer> ticketPrinter = new ArrayList<>();
        if (tableQrcode == null || order.getDistributionModeId() == DistributionType.TAKE_IT_SELF) {
            ticketPrinter = printerService.selectByShopAndType(shopDetail.getId(), PrinterType.RECEPTION);
        } else {
            if (tableQrcode.getAreaId() == null) {
                ticketPrinter = printerService.selectQiantai(shopDetail.getId(), PrinterRange.QIANTAI);
            } else {
                Area area = areaService.selectById(tableQrcode.getAreaId());
                ticketPrinter = printerService.selectQiantai(shopDetail.getId(), PrinterRange.QIANTAI);
                if (area == null) {

                } else {
                    Printer printer = printerService.selectById(area.getPrintId().intValue());
                    ticketPrinter.add(printer);
                }
            }

        }
        if (order.getOrderMode() == ShopMode.BOSS_ORDER && order.getPrintTimes() == 1) {

            List<OrderItem> child = orderItemService.listByParentId(orderId);
            for (OrderItem orderItem : child) {
                order.setOriginalAmount(order.getOriginalAmount().add(orderItem.getOriginalPrice().multiply(BigDecimal.valueOf(orderItem.getCount()))));
                order.setOrderMoney(order.getOrderMoney().add(orderItem.getFinalPrice()));
            }
            child.addAll(items);

            for (Printer printer : ticketPrinter) {
                Map<String, Object> ticket = null;
                if (shopDetail.getIsPosNew() == Common.YES) {
                    ticket = printTicketPosNew(order, child, shopDetail, printer);
                } else {
                    ticket = printTicket(order, child, shopDetail, printer);
                }

                if (ticket != null) {
                    printTask.add(ticket);
                }
            }
            return printTask;
        }


        if ((order.getPrintOrderTime() != null || order.getProductionStatus() >= 2) && order.getOrderMode() != ShopMode.HOUFU_ORDER) {
            return printTask;
        }


        if (setting.getAutoPrintTotal().intValue() == 0 && shopDetail.getAutoPrintTotal() == 0 &&
                (order.getOrderMode() != ShopMode.HOUFU_ORDER || (order.getOrderState() == OrderState.SUBMIT && order.getOrderMode() == ShopMode.HOUFU_ORDER))) {
            List<OrderItem> child = orderItemService.listByParentId(orderId);
            for (OrderItem orderItem : child) {
                order.setOriginalAmount(order.getOriginalAmount().add(orderItem.getOriginalPrice().multiply(BigDecimal.valueOf(orderItem.getCount()))));
//                order.setPaymentAmount(order.getPaymentAmount().add(orderItem.getFinalPrice()));
            }
            child.addAll(items);
            for (Printer printer : ticketPrinter) {
                Map<String, Object> ticket = null;
                if (shopDetail.getIsPosNew() == Common.YES) {
                    ticket = printTicketPosNew(order, child, shopDetail, printer);
                } else {
                    ticket = printTicket(order, child, shopDetail, printer);
                }
                if (ticket != null) {
                    printTask.add(ticket);
                }

            }
        }

        if ((order.getOrderMode().equals(ShopMode.HOUFU_ORDER)) && order.getOrderState().equals(OrderState.PAYMENT)
                && setting.getIsPrintPayAfter().equals(Common.YES) && shopDetail.getIsPrintPayAfter().equals(Common.YES)) {
            List<OrderItem> child = orderItemService.listByParentId(orderId);
            for (OrderItem orderItem : child) {
                order.setOriginalAmount(order.getOriginalAmount().add(orderItem.getFinalPrice()));
//                order.setPaymentAmount(order.getPaymentAmount().add(orderItem.getFinalPrice()));
            }
            child.addAll(items);

            for (Printer printer : ticketPrinter) {
                Map<String, Object> ticket = null;
                if (shopDetail.getIsPosNew() == Common.YES) {
                    ticket = printTicketPosNew(order, child, shopDetail, printer);
                } else {
                    ticket = printTicket(order, child, shopDetail, printer);
                }
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
                Map<String, Object> packageTicket = null;
                if (shopDetail.getIsPosNew() == Common.YES) {
                    packageTicket = printTicketPosNew(order, items, shopDetail, printer);
                } else {
                    packageTicket = printTicket(order, items, shopDetail, printer);
                }
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
        BigDecimal sellIncome = new BigDecimal(0);
        BigDecimal refundTotal = new BigDecimal(0);
        BigDecimal discountTotal = new BigDecimal(0);
        Integer refundCount = 0;
        //brandArticleReportDto bo = orderMapper.selectArticleSumCountByData(begin, end, brandId);
        //totalNum = orderMapper.selectArticleSumCountByData(begin, end, brandId);
        /**
         * 菜品总数单独算是因为 要出去套餐的数量
         */
        List<Integer> totalNums = orderMapper.selectBrandArticleNum(begin, end, brandId);
        //查询菜品总额，退菜总数，退菜金额
        brandArticleReportDto bo = new brandArticleReportDto();
        bo.setSellIncome(sellIncome);
        bo.setRefundCount(refundCount);
        bo.setDiscountTotal(discountTotal);
        bo.setRefundTotal(refundTotal);
        List<brandArticleReportDto> articleReportDto = orderMapper.selectConfirmMoney(begin, end, brandId);
        if (articleReportDto != null && !articleReportDto.isEmpty()) {
            for (brandArticleReportDto reportDto : articleReportDto) {
                bo.setSellIncome(bo.getSellIncome().add(reportDto.getSellIncome()));
                bo.setRefundCount(bo.getRefundCount() + reportDto.getRefundCount());
                bo.setDiscountTotal(bo.getDiscountTotal().add(reportDto.getDiscountTotal()));
                bo.setRefundTotal(bo.getRefundTotal().add(reportDto.getRefundTotal()));
            }
        }
        if (totalNums != null && !totalNums.isEmpty()) {
            for (Integer num : totalNums) {
                totalNum += num;
            }
        }
        bo.setTotalNum(totalNum == null ? 0 : totalNum);
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
        //查询每个店铺的菜品销售的和
        List<ShopArticleReportDto> list = orderMapper.selectShopArticleSell(begin, end, brandId);
        List<ShopArticleReportDto> listArticles = new ArrayList<>();
        for (ShopDetail shop : shopDetails) {
            ShopArticleReportDto st = new ShopArticleReportDto(shop.getId(), shop.getName(), 0, BigDecimal.ZERO, "0.00%", 0, BigDecimal.ZERO, BigDecimal.ZERO);
            listArticles.add(st);
        }
        //计算所有店铺的菜品销售的和
        BigDecimal sum = new BigDecimal(0);
        //计算所有店铺的菜品销售的和
        if (!list.isEmpty()) {
            for (ShopArticleReportDto shopArticleReportDto2 : list) {
                //计算减去退菜销售额
                sum = sum.add(shopArticleReportDto2.getSellIncome());
            }
            for (ShopArticleReportDto shopArticleReportDto : listArticles) {
                for (ShopArticleReportDto shopArticleReportDto2 : list) {
                    if (shopArticleReportDto2.getShopId().equals(shopArticleReportDto.getShopId())) {
                        shopArticleReportDto.setSellIncome(shopArticleReportDto.getSellIncome().add(shopArticleReportDto2.getSellIncome()));
                        shopArticleReportDto.setDiscountTotal(shopArticleReportDto.getDiscountTotal().add(shopArticleReportDto2.getDiscountTotal()));
                        shopArticleReportDto.setTotalNum(shopArticleReportDto.getTotalNum() + shopArticleReportDto2.getTotalNum());
                        shopArticleReportDto.setRefundCount(shopArticleReportDto.getRefundCount() + shopArticleReportDto2.getRefundCount());
                        shopArticleReportDto.setRefundTotal(shopArticleReportDto.getRefundTotal().add(shopArticleReportDto2.getRefundTotal()));
                    }
                }
                String occupy = shopArticleReportDto.getSellIncome() == null ? "0" : shopArticleReportDto.getSellIncome().divide(sum, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP)
                        .toString();
                shopArticleReportDto.setOccupy(occupy + "%");
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
    public Map<String, Object> selectMoneyAndNumByDate(String beginDate, String endDate, String brandId, String brandName, List<ShopDetail> shopDetailList) {
        //封装品牌的数据
        OrderPayDto brandPayDto = new OrderPayDto(brandName, BigDecimal.ZERO, 0, BigDecimal.ZERO, "0");
        //封装店铺的数据
        List<OrderPayDto> shopPayDto = new ArrayList<>();
        for (ShopDetail shopDetail : shopDetailList) {
            OrderPayDto ot = new OrderPayDto(shopDetail.getId(), shopDetail.getName(), BigDecimal.ZERO, 0, BigDecimal.ZERO, "0");
            shopPayDto.add(ot);
        }
        //用来接收分段查询出来的订单金额信息
        List<ShopIncomeDto> shopIncomeDtosItem = new ArrayList<>();
        shopIncomeDtosItem.add(new ShopIncomeDto());
        //用来累加分段查询出来的订单金额信息
        List<ShopIncomeDto> shopIncomeDtosItems = new ArrayList<>();
        //用来接收分段查询出来的订单支付项信息
        List<ShopIncomeDto> shopIncomeDtosPayMent = new ArrayList<>();
        shopIncomeDtosPayMent.add(new ShopIncomeDto());
        //用来累加分段查询出来的订单支付项信息
        List<ShopIncomeDto> shopIncomeDtosPayMents = new ArrayList<>();
        Map<String, Object> selectMap = new HashMap<>();
        selectMap.put("beginDate", beginDate);
        selectMap.put("endDate", endDate);
        for (int pageNo = 0; (shopIncomeDtosItem != null && !shopIncomeDtosItem.isEmpty())
                || (shopIncomeDtosPayMent != null && !shopIncomeDtosPayMent.isEmpty()); pageNo ++){
            selectMap.put("pageNo", pageNo * 1000);
            shopIncomeDtosItem = orderMapper.selectDayAllOrderItem(selectMap);
            shopIncomeDtosPayMent = orderMapper.selectDayAllOrderPayMent(selectMap);
            shopIncomeDtosItems.addAll(shopIncomeDtosItem);
            shopIncomeDtosPayMents.addAll(shopIncomeDtosPayMent);
        }
        BigDecimal brandActualPayment = BigDecimal.ZERO;
        BigDecimal brandVirtualPayment = BigDecimal.ZERO;
        BigDecimal shopActualPayment = BigDecimal.ZERO;
        BigDecimal shopVirtualPayment = BigDecimal.ZERO;
        for (OrderPayDto shopOrderPayDto : shopPayDto){
            //循环累加店铺订单总额、订单数
            for (ShopIncomeDto shopIncomeDtoItem : shopIncomeDtosItems){
                if (shopOrderPayDto.getShopDetailId().equalsIgnoreCase(shopIncomeDtoItem.getShopDetailId())){
                    shopOrderPayDto.setOrderMoney(shopOrderPayDto.getOrderMoney().add(shopIncomeDtoItem.getTotalIncome()));
                    if (StringUtils.isBlank(shopIncomeDtoItem.getParentOrderId())){
                        shopOrderPayDto.setNumber(shopOrderPayDto.getNumber() + 1);
                    }
                }
            }
            //循环累加得到店铺的实际支付、虚拟支付的值
            for (ShopIncomeDto shopIncomeDtoPayMent : shopIncomeDtosPayMents){
                if (shopOrderPayDto.getShopDetailId().equalsIgnoreCase(shopIncomeDtoPayMent.getShopDetailId())){
                    shopActualPayment = shopActualPayment.add(shopIncomeDtoPayMent.getWechatIncome()).add(shopIncomeDtoPayMent.getAliPayment())
                            .add(shopIncomeDtoPayMent.getChargeAccountIncome()).add(shopIncomeDtoPayMent.getBackCartPay()).add(shopIncomeDtoPayMent.getMoneyPay());
                    shopVirtualPayment = shopVirtualPayment.add(shopIncomeDtoPayMent.getRedIncome()).add(shopIncomeDtoPayMent.getCouponIncome()).add(shopIncomeDtoPayMent.getChargeGifAccountIncome())
                            .add(shopIncomeDtoPayMent.getWaitNumberIncome());
                }
            }
            //计算店铺订单平均金额
            if (shopOrderPayDto.getNumber().equals(0)){
                shopOrderPayDto.setAverage(shopOrderPayDto.getOrderMoney());
            }else {
                shopOrderPayDto.setAverage(shopOrderPayDto.getOrderMoney().divide(new BigDecimal(shopOrderPayDto.getNumber()), 2, BigDecimal.ROUND_HALF_UP));
            }
            //计算店铺营销撬动率
            if (shopVirtualPayment.equals(BigDecimal.ZERO) || shopVirtualPayment.intValue() == 0){
                shopOrderPayDto.setMarketPrize("0");
            }else {
                shopOrderPayDto.setMarketPrize((shopActualPayment.divide(shopVirtualPayment, 2, BigDecimal.ROUND_HALF_UP)).toString());
            }
            //累加得到品牌实际支付的值
            brandActualPayment = brandActualPayment.add(shopActualPayment);
            //累加得到品牌虚拟支付的值
            brandVirtualPayment = brandVirtualPayment.add(shopVirtualPayment);
            //店铺虚拟、实际支付的值归零
            shopActualPayment = BigDecimal.ZERO;
            shopVirtualPayment = BigDecimal.ZERO;
            brandPayDto.setOrderMoney(brandPayDto.getOrderMoney().add(shopOrderPayDto.getOrderMoney()));
            brandPayDto.setNumber(brandPayDto.getNumber() + shopOrderPayDto.getNumber());
        }
        //计算品牌订单平均金额
        if (brandPayDto.getNumber().equals(0)){
            brandPayDto.setAverage(brandPayDto.getOrderMoney());
        }else {
            brandPayDto.setAverage(brandPayDto.getOrderMoney().divide(new BigDecimal(brandPayDto.getNumber()), 2, BigDecimal.ROUND_HALF_UP));
        }
        //计算品牌营销撬动率
        if (brandVirtualPayment.equals(BigDecimal.ZERO) || brandVirtualPayment.intValue() == 0){
            brandPayDto.setMarketPrize("0");
        }else {
            brandPayDto.setMarketPrize((brandActualPayment.divide(brandVirtualPayment, 2, BigDecimal.ROUND_HALF_UP)).toString());
        }
        //封装返回Map集
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

//    @Override
//    public List<Order> selectListByTime(String beginDate, String endDate, String shopId) {
////        Date begin = DateUtil.getformatBeginDate(beginDate);
////        Date end = DateUtil.getformatEndDate(endDate);
////        return orderMapper.selectListByTime(begin, end, shopId);
//
//
//    }

    @Override
    public List<Order> selectListByTime(String beginDate, String endDate, String shopId, String customerId) {
        Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);
        return orderMapper.selectListByTime(begin, end, shopId, customerId);

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
        } else if (shopDetail.getShopMode() == ShopMode.BOSS_ORDER) {
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
    public List<Map<String, Object>> printTotal(String shopId, String beginDate, String endDate) {

        List<Map<String, Object>> printTask = new ArrayList<>();
        ShopDetail shop = shopDetailService.selectById(shopId);
        Brand brand = brandService.selectById(shop.getBrandId());
        BrandSetting brandSetting = brandSettingService.selectByBrandId(brand.getId());
        List<Printer> ticketPrinter = printerService.selectByShopAndType(shop.getId(), PrinterType.RECEPTION);
        for (Printer printer : ticketPrinter) {
            Map<String, Object> ticket = printTotal(brandSetting, shop, printer, beginDate, endDate);
            if (ticket != null) {
                printTask.add(ticket);
            }

        }
        Map map = new HashMap(4);
        map.put("brandName", brand.getBrandName());
        map.put("fileName", shop.getName());
        map.put("type", "posAction");
        map.put("content", "店铺:" + shop.getName() + "在pos端打印了日结小票返回模版为:" + printTask.toString() + ",请求服务器地址为:" + MQSetting.getLocalIP());
        doPostAnsc(url, map);
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
        List<OrderItem> list = new ArrayList<>();
        if (order.getPrintKitchenFlag() == Common.YES) {
            for (OrderItem item : items) {
                if (item.getPrintFailFlag() == PrintStatus.PRINT_ERROR || item.getPrintFailFlag() == PrintStatus.UNPRINT) {
                    list.add(item);
                }
            }
        } else {
            list.addAll(items);
        }

        List<Map<String, Object>> kitchenTicket = printKitchen(order, list);

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


    public Map<String, Object> printTotal(BrandSetting setting, ShopDetail shopDetail, Printer printer, String beginDate, String endDate) {
        if (printer == null) {
            return null;
        }
        Map<String, Object> selectMap = new HashMap<>();
        selectMap.put("shopId", shopDetail.getId());
        selectMap.put("beginDate", beginDate);
        selectMap.put("endDate", endDate);
        String orderAccountId = "";
        BigDecimal originalMoney = new BigDecimal(0);
        BigDecimal orderMoney = new BigDecimal(0);
        BigDecimal discountMoney = new BigDecimal(0);
        BigDecimal orderCount = new BigDecimal(0);
        BigDecimal customerCount = new BigDecimal(0);
        List<Order> orderList = orderMapper.getOrderAccountByTime(selectMap);
        for (Order orderAccount : orderList) {
            orderAccountId = orderAccountId.concat(orderAccount.getId()).concat(",");
            orderMoney = orderMoney.add(orderAccount.getOrderMoney());
            originalMoney = originalMoney.add(orderAccount.getOriginalAmount());
            discountMoney = discountMoney.add(orderAccount.getDiscountMoney());
            if (StringUtils.isBlank(orderAccount.getParentOrderId()) && orderAccount.getProductionStatus() != ProductionStatus.REFUND_ARTICLE) {
                orderCount = orderCount.add(new BigDecimal(1));
                customerCount = customerCount.add(new BigDecimal(orderAccount.getCustomerCount()));
            }
        }
        if (StringUtils.isNotBlank(orderAccountId)) {
            orderAccountId = orderAccountId.substring(0, orderAccountId.length() - 1);
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
        if (beginDate.equalsIgnoreCase(endDate)) {
            data.put("DATE", beginDate);
        } else {
            data.put("DATE", beginDate + "至" + endDate);
        }
        data.put("ORIGINAL_AMOUNT", originalMoney);
        data.put("TOTAL_AMOUNT", orderMoney);
        data.put("ORDER_AMOUNT", orderCount);
        DecimalFormat df = new DecimalFormat("######0.00");
        double average = orderCount.equals(BigDecimal.ZERO) ? 0 :
                orderMoney.doubleValue() / orderCount.doubleValue();
        data.put("ORDER_AVERAGE", df.format(average));
        data.put("CUSTOMER_AMOUNT", customerCount);
        double customerAverage = customerCount.equals(BigDecimal.ZERO) ? 0 :
                orderMoney.doubleValue() / customerCount.doubleValue();
        data.put("CUSTOMER_AVERAGE", df.format(customerAverage));
        selectMap.put("type", PayMode.WEIXIN_PAY);
        BigDecimal wxPay = orderMapper.getPayment(selectMap);
        wxPay = wxPay == null ? BigDecimal.ZERO : wxPay;
        selectMap.put("type", PayMode.CHARGE_PAY);
        BigDecimal chargePay = orderMapper.getPayment(selectMap);
        chargePay = chargePay == null ? BigDecimal.ZERO : chargePay;
        selectMap.put("type", PayMode.ALI_PAY);
        BigDecimal aliPay = orderMapper.getPayment(selectMap);
        aliPay = aliPay == null ? BigDecimal.ZERO : aliPay;
        selectMap.put("type", PayMode.BANK_CART_PAY);
        BigDecimal bankPay = orderMapper.getPayment(selectMap);
        bankPay = bankPay == null ? BigDecimal.ZERO : bankPay;
        selectMap.put("type", PayMode.GIVE_CHANGE);
        BigDecimal givePay = orderMapper.getPayment(selectMap);
        givePay = givePay == null ? BigDecimal.ZERO : givePay;
        selectMap.put("type", PayMode.CRASH_PAY);
        BigDecimal crashPay = orderMapper.getPayment(selectMap);
        crashPay = crashPay == null ? BigDecimal.ZERO : crashPay.add(givePay);
        selectMap.put("type", PayMode.SHANHUI_PAY);
        BigDecimal shanhuiPay = orderMapper.getPayment(selectMap);
        shanhuiPay = shanhuiPay == null ? BigDecimal.ZERO : shanhuiPay;
        selectMap.put("type", PayMode.INTEGRAL_PAY);
        BigDecimal integralPay = orderMapper.getPayment(selectMap);
        integralPay = integralPay == null ? BigDecimal.ZERO : integralPay;
        List<Map<String, Object>> incomeItems = new ArrayList<>();
        Map<String, Object> wxItem = new HashMap<>();
        wxItem.put("SUBTOTAL", wxPay);
        wxItem.put("PAYMENT_MODE", "微信支付");
        incomeItems.add(wxItem);
        Map<String, Object> chargeItem = new HashMap<>();
        chargeItem.put("SUBTOTAL", chargePay);
        chargeItem.put("PAYMENT_MODE", "充值金额支付");
        incomeItems.add(chargeItem);
        Map<String, Object> aliPayment = new HashMap<>();
        aliPayment.put("SUBTOTAL", aliPay);
        aliPayment.put("PAYMENT_MODE", "支付宝支付");
        incomeItems.add(aliPayment);
        BigDecimal incomeAmount = wxPay.add(chargePay).add(aliPay);
        if ((setting.getOpenUnionPay().equals(Common.YES) && shopDetail.getOpenUnionPay().equals(Common.YES)) || bankPay.compareTo(BigDecimal.ZERO) > 0) {
            Map<String, Object> bankPayment = new HashMap<>();
            bankPayment.put("SUBTOTAL", bankPay);
            bankPayment.put("PAYMENT_MODE", "银联支付");
            incomeAmount = incomeAmount.add(bankPay);
            incomeItems.add(bankPayment);
        }
        if ((setting.getOpenMoneyPay().equals(Common.YES) && shopDetail.getOpenMoneyPay().equals(Common.YES)) || crashPay.compareTo(BigDecimal.ZERO) > 0) {
            Map<String, Object> crashPayment = new HashMap<>();
            crashPayment.put("SUBTOTAL", crashPay);
            crashPayment.put("PAYMENT_MODE", "现金实收");
            incomeAmount = incomeAmount.add(crashPay);
            incomeItems.add(crashPayment);
        }
        if ((setting.getOpenShanhuiPay().equals(Common.YES) && shopDetail.getOpenShanhuiPay().equals(Common.YES)) || shanhuiPay.compareTo(BigDecimal.ZERO) > 0) {
            Map<String, Object> shanhuiPayment = new HashMap<>();
            shanhuiPayment.put("SUBTOTAL", shanhuiPay);
            shanhuiPayment.put("PAYMENT_MODE", "闪惠支付");
            incomeAmount = incomeAmount.add(shanhuiPay);
            incomeItems.add(shanhuiPayment);
        }
        if ((setting.getIntegralPay().equals(Common.YES) && shopDetail.getIntegralPay().equals(Common.YES)) || integralPay.compareTo(BigDecimal.ZERO) > 0) {
            Map<String, Object> integralPayMent = new HashMap<>();
            integralPayMent.put("SUBTOTAL", integralPay);
            integralPayMent.put("PAYMENT_MODE", "会员支付");
            incomeAmount = incomeAmount.add(integralPay);
            incomeItems.add(integralPayMent);
        }
        data.put("INCOME_AMOUNT", incomeAmount);
        data.put("INCOME_ITEMS", incomeItems);
        selectMap.put("type", PayMode.ACCOUNT_PAY);
        BigDecimal accountPay = orderMapper.getPayment(selectMap);
        accountPay = accountPay == null ? BigDecimal.ZERO : accountPay;
        selectMap.put("type", PayMode.COUPON_PAY);
        BigDecimal couponPay = orderMapper.getPayment(selectMap);
        couponPay = couponPay == null ? BigDecimal.ZERO : couponPay;
        selectMap.put("type", PayMode.REWARD_PAY);
        BigDecimal rewardPay = orderMapper.getPayment(selectMap);
        rewardPay = rewardPay == null ? BigDecimal.ZERO : rewardPay;
        selectMap.put("type", PayMode.WAIT_MONEY);
        BigDecimal waitMoney = orderMapper.getPayment(selectMap);
        waitMoney = waitMoney == null ? BigDecimal.ZERO : waitMoney;
        selectMap.put("type", PayMode.ARTICLE_BACK_PAY);
        BigDecimal articlePay = orderMapper.getPayment(selectMap);
        articlePay = articlePay == null ? BigDecimal.ZERO : articlePay;
        BigDecimal discountAmount = accountPay.add(couponPay).add(rewardPay).add(waitMoney).add(articlePay);
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
        articleBackPay.put("SUBTOTAL", articlePay == null ? 0 : articlePay.abs());
        articleBackPay.put("PAYMENT_MODE", "退菜返还红包");
        discountItems.add(articleBackPay);
        if (originalMoney.compareTo(orderMoney) != 0 && shopDetail.getTemplateType().equals(Common.YES)) {
            Map<String, Object> discountMap = new HashMap<>();
            discountAmount = discountAmount.add(originalMoney.subtract(orderMoney));
            discountMap.put("SUBTOTAL", originalMoney.subtract(orderMoney));
            discountMap.put("PAYMENT_MODE", "粉丝价折扣");
            discountItems.add(discountMap);
        }
        data.put("DISCOUNT_AMOUNT", discountAmount == null ? 0 : discountAmount);
        data.put("DISCOUNT_ITEMS", discountItems);
        List<Map<String, Object>> chargeOrders = chargeOrderService.selectByShopToDay(selectMap);
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
        if (StringUtils.isNotBlank(orderAccountId)) {
            BrandSetting brandSetting = brandSettingService.selectByBrandId(shopDetail.getBrandId());
            Brand brand = brandService.selectBrandBySetting(brandSetting.getId());
            String[] orderIds = orderAccountId.split(",");
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
                serviceMap.put("serviceName", shopDetail.getServiceName());
            }
            Map<String, Object> mealMap = new HashMap<>();
            mealMap.put("mealName", shopDetail.getMealFeeName());
            for (Order orderAll : orders) {
                BigDecimal nowCustomerCount = new BigDecimal(orderAll.getCustomerCount() == null ? 0 : orderAll.getCustomerCount());
                BigDecimal oldCustomerCount = new BigDecimal(orderAll.getBaseCustomerCount() == null ? 0 : orderAll.getBaseCustomerCount());
                if (orderAll.getDistributionModeId().equals(DistributionType.RESTAURANT_MODE_ID)) {
                    if (StringUtils.isNotBlank(orderAll.getParentOrderId())) {
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
                                refundPrice = refundPrice.add(new BigDecimal(serviceMap.get(orderItem.getOrderId()).toString()).multiply(shopDetail.getServicePrice() == null ? BigDecimal.ZERO : shopDetail.getServicePrice()));
                            }
                        } else if (orderAll.getDistributionModeId().equals(DistributionType.TAKE_IT_SELF) || orderAll.getDistributionModeId().equals(DistributionType.DELIVERY_MODE_ID)) {
                            if (!orderId.equals(orderItem.getOrderId())) {
                                refundPrice = refundPrice.add(new BigDecimal(mealMap.get(orderItem.getOrderId()).toString()).multiply(shopDetail.getMealFeePrice() == null ? BigDecimal.ZERO : shopDetail.getMealFeePrice()));
                            }
                        }
                        canceledOrderMap.put(orderItem.getOrderId(), refundPrice);
                        orderId = orderItem.getOrderId();
                    }
                } else if (!oldCustomerCount.equals(nowCustomerCount)) {
                    refundPrice = refundPrice.add(oldCustomerCount.subtract(nowCustomerCount).multiply(shopDetail.getServicePrice()));
                    canceledOrderMap.put(orderAll.getId(), refundPrice);
                }
            }
            selectOrderMap.clear();
            selectOrderMap.put("orderIds", orderIds);
            selectOrderMap.put("count", "count != 0");
            List<OrderItem> saledOrderItems = orderItemService.selectOrderItemByOrderIds(selectOrderMap);
            if (shopDetail.getTemplateType().equals(Common.YES)) {
                List<String> articleIds = new ArrayList<>();
                for (OrderItem item : saledOrderItems) {
                    if (item.getArticleId().indexOf("@") != -1) {
                        articleIds.add(item.getArticleId().substring(0, item.getArticleId().indexOf("@")));
                    } else {
                        articleIds.add(item.getArticleId());
                    }
                }
                //排序菜品销售   按照菜品分类进行排序
                List<ArticleFamily> articleFamilies = articleFamilyMapper.selectArticleSort(articleIds);
                for (ArticleFamily articleFamily : articleFamilies) {
                    List<Map<String, Object>> familyArticleMaps = new ArrayList<>();
//                BigDecimal familyCount = BigDecimal.ZERO;
                    for (Article article : articleFamily.getArticleList()) {
                        BigDecimal unitNewCount = BigDecimal.ZERO;
                        Map<String, Map<String, Integer>> unitMaps = new HashMap<>();
                        for (OrderItem orderItem : saledOrderItems) {
                            Map<String, Object> itemMap = new HashMap<>();
                            if (orderItem.getType().equals(OrderItemType.SETMEALS) && orderItem.getArticleId().equalsIgnoreCase(article.getId())) {
                                saledProductAmount = saledProductAmount.add(new BigDecimal(orderItem.getCount()));
                                itemMap.put("PRODUCT_NAME", orderItem.getArticleName());
                                itemMap.put("SUBTOTAL", orderItem.getCount());
                                familyArticleMaps.add(itemMap);
                                selectMap.clear();
                                selectMap.put("articleId", orderItem.getArticleId());
                                selectMap.put("beginDate", beginDate);
                                selectMap.put("endDate", endDate);
                                List<ArticleSellDto> articleSellDtos = mealAttrMapper.queryArticleMealAttr(selectMap);
                                for (ArticleSellDto articleSellDto : articleSellDtos) {
                                    if (orderItem.getArticleId().equalsIgnoreCase(articleSellDto.getArticleId()) && articleSellDto.getBrandSellNum() != 0) {
                                        itemMap = new HashMap<>();
                                        itemMap.put("PRODUCT_NAME", "|_" + articleSellDto.getArticleName());
                                        itemMap.put("SUBTOTAL", articleSellDto.getBrandSellNum());
                                        familyArticleMaps.add(itemMap);
                                    }
                                }
                            } else if (orderItem.getType().equals(OrderItemType.UNITPRICE) && orderItem.getArticleId().substring(0, orderItem.getArticleId().indexOf("@")).equalsIgnoreCase(article.getId())) {
                                Map<String, Integer> map = new HashMap<>();
                                if (unitMaps.containsKey(orderItem.getArticleId().substring(0, orderItem.getArticleId().indexOf("@")))) {
                                    map = unitMaps.get(orderItem.getArticleId().substring(0, orderItem.getArticleId().indexOf("@")));
                                }
                                String formName = orderItem.getArticleName().substring(orderItem.getArticleName().indexOf(article.getName().substring(article.getName().length() - 1)) + 1);
                                formName = formName.substring(1, formName.length() - 1);
                                map.put(formName, orderItem.getCount());
                                unitMaps.put(orderItem.getArticleId().substring(0, orderItem.getArticleId().indexOf("@")), map);
                            } else if (orderItem.getType().equals(OrderItemType.UNIT_NEW) && orderItem.getArticleId().equalsIgnoreCase(article.getId())) {
                                unitNewCount = unitNewCount.add(new BigDecimal(orderItem.getCount()));
                                Map<String, Integer> map = new HashMap<>();
                                if (unitMaps.containsKey(orderItem.getArticleId())) {
                                    map = unitMaps.get(orderItem.getArticleId());
                                }
                                String formName = orderItem.getArticleName().substring(orderItem.getArticleName().indexOf(article.getName().substring(article.getName().length() - 1)) + 1);
                                String[] formNames = formName.split("\\)");
                                for (String name : formNames) {
                                    if (name.length() > 1) {
                                        formName = name.substring(1);
                                    }
                                    if (map.containsKey(formName)) {
                                        Integer count = map.get(formName);
                                        count += orderItem.getCount();
                                        map.put(formName, count);
                                    } else {
                                        map.put(formName, orderItem.getCount());
                                    }
                                }
                                unitMaps.put(orderItem.getArticleId(), map);
                            } else if (orderItem.getArticleId().equalsIgnoreCase(article.getId())) {
//                            familyCount = familyCount.add(new BigDecimal(orderItem.getCount()));
                                saledProductAmount = saledProductAmount.add(new BigDecimal(orderItem.getCount() - orderItem.getPackageNumber()));
                                itemMap.put("PRODUCT_NAME", orderItem.getArticleName());
                                itemMap.put("SUBTOTAL", orderItem.getCount() + "(" + (orderItem.getCount() - orderItem.getPackageNumber()) + "+" + orderItem.getPackageNumber() + ")");
                                familyArticleMaps.add(itemMap);
                            }
                        }
                        if (unitMaps.containsKey(article.getId())) {
                            Map<String, Object> itemMap = new HashMap<>();
                            Map<String, Integer> unitPriceMap = unitMaps.get(article.getId());
                            BigDecimal articleCount = unitNewCount.compareTo(BigDecimal.ZERO) > 0 ? unitNewCount : BigDecimal.ZERO;
                            List<Map<String, Object>> maps = new ArrayList<>();
                            for (Map.Entry<String, Integer> unitMap : unitPriceMap.entrySet()) {
                                Map<String, Object> map = new HashMap<>();
                                map.put("PRODUCT_NAME", "|_" + unitMap.getKey());
                                map.put("SUBTOTAL", unitMap.getValue());
                                if (unitNewCount.compareTo(BigDecimal.ZERO) == 0) {
                                    articleCount = articleCount.add(new BigDecimal(unitMap.getValue()));
                                }
                                maps.add(map);
                            }
//                        familyCount = familyCount.add(articleCount);
                            saledProductAmount = saledProductAmount.add(articleCount);
                            itemMap.put("PRODUCT_NAME", article.getName());
                            itemMap.put("SUBTOTAL", articleCount);
                            familyArticleMaps.add(itemMap);
                            familyArticleMaps.addAll(maps);
                        }
                    }
                    Map<String, Object> itemMap = new HashMap<>();
                    BigDecimal strLength = new BigDecimal(articleFamily.getName().length()).multiply(new BigDecimal(2));
                    Integer length = new BigDecimal(40).subtract(strLength).divide(new BigDecimal(2)).intValue();
                    String string = "-";
                    for (int i = 1; i < length; i++) {
                        string = string.concat("-");
                    }
                    itemMap.put("PRODUCT_NAME", string.concat(articleFamily.getName()).concat(string));
//                itemMap.put("SUBTOTAL", familyCount);
                    saledProducts.add(itemMap);
                    saledProducts.addAll(familyArticleMaps);
                }
            } else {
                for (OrderItem orderItem : saledOrderItems) {
                    saledProductAmount = saledProductAmount.add(new BigDecimal(orderItem.getType().equals(OrderItemType.SETMEALS) ? 0 : orderItem.getCount()));
                    Map<String, Object> itemMap = new HashMap<>();
                    itemMap.put("PRODUCT_NAME", orderItem.getArticleName());
                    itemMap.put("SUBTOTAL", orderItem.getCount());
                    saledProducts.add(itemMap);
                }
            }
            selectOrderMap.clear();
            selectOrderMap.put("orderIds", orderIds);
            selectOrderMap.put("count", "refund_count != 0");
            List<OrderItem> canceledOrderItems = orderItemService.selectOrderItemByOrderIds(selectOrderMap);
            for (OrderItem orderItem : canceledOrderItems) {
                canceledProductCount = canceledProductCount.add(new BigDecimal(orderItem.getRefundCount()));
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("PRODUCT_NAME", orderItem.getArticleName());
                itemMap.put("SUBTOTAL", orderItem.getRefundCount());
                canceledProducts.add(itemMap);
            }
            if (!nowService.equals(BigDecimal.ZERO) || !nowMeal.equals(BigDecimal.ZERO)) {
                Map<String, Object> itemMap = new HashMap<>();
                if (shopDetail.getTemplateType().equals(Common.YES)) {
                    String other = "其他销量";
                    BigDecimal strLength = new BigDecimal(other.length()).multiply(new BigDecimal(2));
                    Integer length = new BigDecimal(48).subtract(strLength).divide(new BigDecimal(2)).intValue();
                    String string = "-";
                    for (int i = 1; i < length; i++) {
                        string = string.concat("-");
                    }
                    itemMap.put("PRODUCT_NAME", string.concat(other).concat(string));
                    saledProducts.add(itemMap);
                }
                if (!nowService.equals(BigDecimal.ZERO)) {
                    itemMap = new HashMap<>();
                    itemMap.put("PRODUCT_NAME", serviceMap.get("serviceName"));
                    itemMap.put("SUBTOTAL", nowService);
                    saledProducts.add(itemMap);
                    //服务费不计入总销量
//                saledProductAmount = saledProductAmount.add(nowService);
                }
                if (!nowMeal.equals(BigDecimal.ZERO)) {
                    itemMap = new HashMap<>();
                    itemMap.put("PRODUCT_NAME", mealMap.get("mealName"));
                    itemMap.put("SUBTOTAL", nowMeal);
                    saledProducts.add(itemMap);
                    //餐盒费不计入总销量    小确幸SB又改了， 又要加上去。 妈的！  拿来怎么多B事 -- 2017-06-22改为计入
                    saledProductAmount = saledProductAmount.add(nowMeal);
                }
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
        Integer ck = (Integer) RedisUtil.get(orderItem.getArticleId() + Common.KUCUN);
        switch (orderItem.getType()) {
            case OrderItemType.ARTICLE:
                //如果是单品无规格，直接判断菜品是否有库存

                if (ck != null) {
                    current = ck;
                } else {
                    current = orderMapper.selectArticleCount(orderItem.getArticleId());
                }
                result = current >= count;
                msg = current == 0 ? orderItem.getArticleName() + "已售罄,请取消订单后重新下单" :
                        current >= count ? "库存足够" : orderItem.getArticleName() + "中单品库存不足,最大购买" + current + "个,请重新选购餐品";
                break;
            case OrderItemType.UNITPRICE:
                //如果是有规则菜品，则判断该规则是否有库存
                if (ck != null) {
                    current = ck;
                } else {
                    current = orderMapper.selectArticlePriceCount(orderItem.getArticleId());
                }

                result = current >= count;
                msg = current == 0 ? orderItem.getArticleName() + "已售罄,请取消订单后重新下单" :
                        current >= count ? "库存足够" : orderItem.getArticleName() + "中单品库存不足,最大购买" + current + "个,请重新选购餐品";
                break;
            case OrderItemType.SETMEALS:
                //如果是套餐,不做判断，只判断套餐下的子品是否有库存
                if (ck != null) {
                    current = ck;
                } else {
                    current = orderMapper.selectArticleCount(orderItem.getArticleId());
                }
                Map<String, Integer> order_items_map = new HashMap<String, Integer>();//用于保存套餐内的子菜品（防止套餐内出现同样餐品，检查库存出现异常）
                for (OrderItem oi : orderItem.getChildren()) {
                    //查询当前菜品，剩余多少份
                    Integer cck = (Integer) RedisUtil.get(oi.getArticleId() + Common.KUCUN);
                    if (cck != null) {
                        min = cck;
                    } else {
                        min = orderMapper.selectArticleCount(oi.getArticleId());
                    }
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
                if (ck != null) {
                    current = ck;
                } else {
                    current = orderMapper.selectArticleCount(orderItem.getArticleId());
                }
//                current = orderMapper.selectArticleCount(orderItem.getArticleId());
                result = current >= orderItem.getCount();
                msg = current == 0 ? orderItem.getArticleName() + "已售罄,请取消订单后重新下单" :
                        current >= orderItem.getCount() ? "库存足够" : orderItem.getArticleName() + "库存不足,请重新选购餐品";
                break;
            case OrderItemType.UNIT_NEW:
                //如果是单品无规格，直接判断菜品是否有库存
                if (ck != null) {
                    current = ck;
                } else {
                    current = orderMapper.selectArticleCount(orderItem.getArticleId());
                }
//                current = orderMapper.selectArticleCount(orderItem.getArticleId());
                result = current >= count;
                msg = current == 0 ? orderItem.getArticleName() + "已售罄,请取消订单后重新下单" :
                        current >= count ? "库存足够" : orderItem.getArticleName() + "中单品库存不足,最大购买" + current + "个,请重新选购餐品";
                break;
            case OrderItemType.RECOMMEND:
                //如果是单品无规格，直接判断菜品是否有库存
                if (ck != null) {
                    current = ck;
                } else {
                    current = orderMapper.selectArticleCount(orderItem.getArticleId());
                }
//                current = orderMapper.selectArticleCount(orderItem.getArticleId());
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
            String articleId = orderItem.getArticleId();
            Article article = articleService.selectById(articleId);
//            Integer articleCount = (Integer) RedisUtil.get(articleId + Common.KUCUN);
            Integer articleCount = (Integer) RedisUtil.get(articleId + Common.KUCUN);
            switch (orderItem.getType()) {
                case OrderItemType.UNITPRICE:
                    //如果是有规格的单品信息，那么更新该规格的单品库存以及该单品的库存
                    ArticlePrice articlePrice = articlePriceMapper.selectByPrimaryKey(orderItem.getArticleId());
                    List<ArticlePrice> articlePrices = articlePriceMapper.selectByArticleId(articlePrice.getArticleId());

                    if (articleCount == null) {
                        if (articlePrice.getCurrentWorkingStock() > orderItem.getCount()) {
//                            RedisUtil.set(articleId + Common.KUCUN, articlePrice.getCurrentWorkingStock() - 1);
                            RedisUtil.set(articleId + Common.KUCUN, articlePrice.getCurrentWorkingStock() - orderItem.getCount());
                        } else {
//                            RedisUtil.set(articleId + Common.KUCUN, 0);
                            RedisUtil.set(articleId + Common.KUCUN, 0);
                            articlePriceService.setArticlePriceEmpty(articlePrice.getArticleId());
                        }
                    } else {
                        if (articleCount > orderItem.getCount()) {
//                            RedisUtil.set(articleId + Common.KUCUN, articleCount - 1);
                            RedisUtil.set(articleId + Common.KUCUN, articleCount - orderItem.getCount());
                        } else {
//                            RedisUtil.set(articleId + Common.KUCUN, 0);
                            RedisUtil.set(articleId + Common.KUCUN, 0);
                            articlePriceService.setArticlePriceEmpty(articlePrice.getArticleId());
                        }
                    }
                    int sum = 0;
                    for (ArticlePrice price : articlePrices) {
//                        Integer count = (Integer) RedisUtil.get(price.getId() + Common.KUCUN);
                        Integer count = (Integer) RedisUtil.get(price.getId() + Common.KUCUN);
                        if (count != null) {
                            sum += count;
                        } else {
                            sum += price.getCurrentWorkingStock();
                        }
                    }
//                    RedisUtil.set(articlePrice.getArticleId() + Common.KUCUN, sum);
                    RedisUtil.set(articlePrice.getArticleId() + Common.KUCUN, sum);
                    if (sum == 0) {
                        articleService.setEmpty(articlePrice.getArticleId());
                    }
                    break;
                case OrderItemType.SETMEALS:
                    //如果是套餐，那么更新套餐库存
                case OrderItemType.MEALS_CHILDREN:
                    //如果是套餐子项，那么更新子项库存
                case OrderItemType.UNIT_NEW:
                    //如果是没有规格的单品信息,那么更新该单品的库存
                case OrderItemType.ARTICLE:
                    //如果是没有规格的单品信息,那么更新该单品的库存
                case OrderItemType.RECOMMEND:
                    //如果是没有规格的单品信息,那么更新该单品的库存
                    if (articleCount == null) {
                        if (article.getCurrentWorkingStock() > orderItem.getCount()) {
//                            RedisUtil.set(articleId + Common.KUCUN, article.getCurrentWorkingStock() - 1);
                            RedisUtil.set(articleId + Common.KUCUN, article.getCurrentWorkingStock() - orderItem.getCount());
                        } else {
//                            RedisUtil.set(articleId + Common.KUCUN, 0);
                            RedisUtil.set(articleId + Common.KUCUN, 0);
                            articleService.setEmpty(orderItem.getArticleId());
                        }
                    } else {
                        if (articleCount > orderItem.getCount()) {
//                            RedisUtil.set(articleId + Common.KUCUN, articleCount - 1);
                            RedisUtil.set(articleId + Common.KUCUN, articleCount - orderItem.getCount());
                        } else {
//                            RedisUtil.set(articleId + Common.KUCUN, 0);
                            RedisUtil.set(articleId + Common.KUCUN, 0);
                            articleService.setEmpty(orderItem.getArticleId());
                        }

                    }
                    break;
                default:
                    throw new AppException(AppException.UNSUPPORT_ITEM_TYPE, "不支持的餐品类型:" + orderItem.getType());
            }


        }
        //同时更新套餐库存(套餐库存为 最小库存的单品)
//        List<Article> taocan = orderMapper.getStockBySuit(order.getShopDetailId());
//        for(Article article : taocan){
//            RedisUtil.set(article.getId()+Common.KUCUN,article.getCount());
//            if(article.getCount() == 0){
//                orderMapper.setEmpty(article.getId());
//            }
//        }
//        orderMapper.setStockBySuit(order.getShopDetailId());
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
            String articleId = orderItem.getArticleId();
            Article article = articleService.selectById(articleId);
//            Integer articleCount = (Integer) RedisUtil.get(articleId + Common.KUCUN);
            Integer articleCount = (Integer) RedisUtil.get(articleId + Common.KUCUN);
            switch (orderItem.getType()) {

                case OrderItemType.UNITPRICE:
                    //如果是有规格的单品信息，那么更新该规格的单品库存以及该单品的库存
                    ArticlePrice articlePrice = articlePriceMapper.selectByPrimaryKey(orderItem.getArticleId());
                    if (articleCount != null) {
//                        RedisUtil.set(articleId + Common.KUCUN, articleCount + 1);
                        RedisUtil.set(articleId + Common.KUCUN, articleCount + orderItem.getCount());
                        if (articleCount == 0) {
                            articlePriceService.setArticlePriceEmptyFail(articlePrice.getArticleId());
                        }
                    } else {
//                        RedisUtil.set(articleId + Common.KUCUN, articlePrice.getCurrentWorkingStock() + 1);
                        RedisUtil.set(articleId + Common.KUCUN, articlePrice.getCurrentWorkingStock() + orderItem.getCount());
                        if (articlePrice.getCurrentWorkingStock() == 0) {
                            articlePriceService.setArticlePriceEmptyFail(articlePrice.getArticleId());
                        }
                    }
                    Integer baseArticle = (Integer) RedisUtil.get(articlePrice.getArticleId() + Common.KUCUN);
                    if (baseArticle != null) {
//                        RedisUtil.set(articlePrice.getArticleId() + Common.KUCUN, baseArticle + 1);
                        RedisUtil.set(articlePrice.getArticleId() + Common.KUCUN, baseArticle + orderItem.getCount());
                        if (baseArticle == 0) {
                            articleService.setEmptyFail(articlePrice.getArticleId());
                        }
                    } else {
                        Article base = articleService.selectById(articlePrice.getArticleId());
//                        RedisUtil.set(articlePrice.getArticleId() + Common.KUCUN, base.getCurrentWorkingStock() + 1);
                        RedisUtil.set(articlePrice.getArticleId() + Common.KUCUN, base.getCurrentWorkingStock() + orderItem.getCount());
                        if (base.getCurrentWorkingStock() == 0) {
                            articleService.setEmptyFail(articlePrice.getArticleId());
                        }
                    }
                    break;
                case OrderItemType.SETMEALS:
                    //如果是套餐，那么更新套餐库存
                case OrderItemType.MEALS_CHILDREN:
                    //如果是套餐子项，那么更新子项库存
                case OrderItemType.ARTICLE:
                    //如果是没有规格的单品信息,那么更新该单品的库存
                case OrderItemType.UNIT_NEW:
                    //如果是没有规格的单品信息,那么更新该单品的库存
                    if (articleCount != null) {
//                        RedisUtil.set(articleId + Common.KUCUN, articleCount + 1);
                        RedisUtil.set(articleId + Common.KUCUN, articleCount + orderItem.getCount());
                        if (articleCount == 0) {
                            articleService.setEmptyFail(orderItem.getArticleId());
                        }
                    } else {
//                        RedisUtil.set(articleId + Common.KUCUN, article.getCurrentWorkingStock() + 1);
                        RedisUtil.set(articleId + Common.KUCUN, article.getCurrentWorkingStock() + orderItem.getCount());
                        if (article.getCurrentWorkingStock() == 0) {
                            articleService.setEmptyFail(orderItem.getArticleId());
                        }
                    }
                    break;
                default:
                    //  throw new AppException(AppException.UNSUPPORT_ITEM_TYPE, "不支持的餐品类型:" + orderItem.getType());
                    return false;
            }

        }
        //同时更新套餐库存(套餐库存为 最小库存的单品)
//        List<Article> taocan = orderMapper.getStockBySuit(order.getShopDetailId());
//        for(Article article : taocan){
//            Integer suit = (Integer) RedisUtil.get(article.getId()+Common.KUCUN);
//            if(suit != null){
//                if(suit == 0 && article.getCount() > 0){
//                    orderMapper.setEmptyFail(article.getId());
//                }
//                RedisUtil.set(article.getId()+Common.KUCUN,article.getCount());
//            }else{
//                if(article.getIsEmpty() && article.getCount() > 0){
//                    orderMapper.setEmptyFail(article.getId());
//                }
//                RedisUtil.set(article.getId()+Common.KUCUN,article.getCount());
//            }
//        }
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
    public List<Order> selectByOrderSatesAndProductionStatesTakeout(String shopId, String[] orderStates,
                                                             String[] productionStates) {
        ShopDetail shopDetail = shopDetailService.selectByPrimaryKey(shopId);
        if (shopDetail.getShopMode() == ShopMode.HOUFU_ORDER) {
            return orderMapper.listHoufuUnFinishedOrder(shopId);

        } else if (shopDetail.getShopMode() == ShopMode.BOSS_ORDER) {
            List<Order> order=orderMapper.selectOrderByBossTakeout(shopId);
            //return orderMapper.selectOrderByBossTakeout(shopId);
            return order;
        } else {
            List<Order> order=orderMapper.selectByOrderSatesAndProductionStatesTakeout(shopId, orderStates, productionStates);
            //return orderMapper.selectByOrderSatesAndProductionStatesTakeout(shopId, orderStates, productionStates);
            return order;
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

        List<Order> orders = orderMapper.selectByParentId(order.getId(), order.getPayType());
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

        List<Order> orders = orderMapper.selectByParentId(order.getId(), order.getPayType());
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
            Brand brand = brandService.selectById(order.getBrandId());
            ShopDetail shopDetail = shopDetailService.selectById(order.getShopDetailId());
            WechatConfig config = wechatConfigService.selectByBrandId(customer.getBrandId());
            List<OrderPaymentItem> paymentItems = orderPaymentItemService.selectByOrderId(order.getId());
            String money = "(";
            for (OrderPaymentItem orderPaymentItem : paymentItems) {
                money += PayMode.getPayModeName(orderPaymentItem.getPaymentModeId()) + "： " + orderPaymentItem.getPayValue() + " ";
            }
            StringBuffer msg = new StringBuffer();
            BigDecimal sum = order.getOrderMoney();
            List<Order> orders = selectByParentId(order.getId(), order.getPayType()); //得到子订单
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
//            UserActionUtils.writeToFtp(LogType.ORDER_LOG, brand.getBrandName(), shopDetail.getName(), order.getId(),
//                    "订单发送推送：" + msg.toString());
            Map map = new HashMap(4);
            map.put("brandName", setting.getBrandName());
            map.put("fileName", customer.getId());
            map.put("type", "UserAction");
            map.put("content", "系统向用户:" + customer.getNickname() + "推送微信消息:" + msg.toString() + ",请求服务器地址为:" + MQSetting.getLocalIP());
            doPostAnsc(LogUtils.url, map);
        }
    }

    @Override
    public Order payPrice(BigDecimal factMoney, String orderId) {
        //拿到订单
        Order order = orderMapper.selectByPrimaryKey(orderId);

        Customer customer = customerService.selectById(order.getCustomerId());

        if (order.getOrderState() < OrderState.PAYMENT) {
            accountService.payOrder(order, factMoney, customer, null, null);
            order.setOrderState(OrderState.PAYMENT);
            order.setAllowCancel(false);
            order.setPaymentAmount(new BigDecimal(0));
            order.setAllowContinueOrder(false);
            update(order);
            List<Order> orders = orderMapper.selectByParentId(order.getId(), order.getPayType());
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
        accountService.payOrder(order, factMoney, customer, null, null);
    }

    @Override
    public void cleanShopOrder(ShopDetail shopDetail, OffLineOrder offLineOrder, Brand brand) {
        //1.结店退款
        refundShopDetailOrder(shopDetail);
        //2查询天气
        Wether wether =wetherService.selectDateAndShopId(shopDetail.getId(),DateUtil.formatDate(new Date(),"yyyy-MM-dd"));
        if(wether==null){//说明没有调用定时任务 ---
            wether = new Wether();
            wether.setDayWeather("---");
            wether.setDayTemperature(-1);
            wether.setWeekady(-1);
        }
        WechatConfig wechatConfig = wechatConfigService.selectByBrandId(brand.getId());
        //短信第一版用来发日结短信
        Map<String, String> dayMapByFirstEdtion = querryDateDataByFirstEdtion(shopDetail, offLineOrder);
        //3发短信推送/微信推送
        pushMessageByFirstEdtion(dayMapByFirstEdtion, shopDetail, wechatConfig, brand.getBrandName());
        //3判断是否需要发送旬短信
        int temp = DateUtil.getEarlyMidLate();
        switch (temp){
            case  1:
                //第一版旬结短信
                Map<String, String> xunMapByFirstEdtion = querryXunDataByFirstEditon(shopDetail);
                pushMessageByFirstEdtion(xunMapByFirstEdtion, shopDetail, wechatConfig, brand.getBrandName());
                break;

            case 2:
                Map<String, String> xunMapByFirstEdtion2 = querryXunDataByFirstEditon(shopDetail);
                pushMessageByFirstEdtion(xunMapByFirstEdtion2, shopDetail, wechatConfig, brand.getBrandName());
                break;

            case 3:
               Map<String, String> xunMapByFirstEdtion3 = querryXunDataByFirstEditon(shopDetail);
               pushMessageByFirstEdtion(xunMapByFirstEdtion3, shopDetail, wechatConfig, brand.getBrandName());

               Map<String, String> monthMapByFirstEdtion = querryMonthDataByFirstEditon(shopDetail, offLineOrder);
               pushMessageByFirstEdtion(monthMapByFirstEdtion, shopDetail, wechatConfig, brand.getBrandName());
               break;

        }

        //第二版短信内容由于模板原因无法发送短信 因此保留第一版短信 第二版数据存到大数据库数据库中
        insertDateData(shopDetail,offLineOrder,wether,brand);


    }

    /**
     * 第一版短信 日结数据封装
     * @param shopDetail
     * @param offLineOrder
     * @return
     */
    private Map<String,String> querryDateDataByFirstEdtion(ShopDetail shopDetail, OffLineOrder offLineOrder) {
        // 查询该店铺是否结过店
        OffLineOrder offLineOrder1 = offLineOrderMapper.selectByTimeSourceAndShopId(OfflineOrderSource.OFFLINE_POS, shopDetail.getId(), DateUtil.getDateBegin(new Date()), DateUtil.getDateEnd(new Date()));
        if (null != offLineOrder1) {
            offLineOrder1.setState(0);
            offLineOrderMapper.updateByPrimaryKeySelective(offLineOrder1);
        }
        offLineOrder.setId(ApplicationUtils.randomUUID());
        offLineOrder.setState(1);
        offLineOrder.setResource(OfflineOrderSource.OFFLINE_POS);
        offLineOrderMapper.insertSelective(offLineOrder);

        //----1.定义时间---
        Date todayBegin = DateUtil.getDateBegin(new Date());
        Date todayEnd = DateUtil.getDateEnd(new Date());
        //本月的开始时间 本月结束时间
        String beginMonth = DateUtil.getMonthBegin();
        Date begin = DateUtil.getDateBegin(DateUtil.fomatDate(beginMonth));
        Date end = todayEnd;
        //三.定义线下订单
        //本日线下订单总数(堂吃)
        int todayEnterCount = 0;
        //本日线下订单总额(堂吃)
        BigDecimal todayEnterTotal = BigDecimal.ZERO;
        //本月线下订单总数
        int monthEnterCount = 0;
        //本月线下订单总额
        BigDecimal monthEnterTotal = BigDecimal.ZERO;

        //4.外卖订单
        //本日外卖订单数
        int todayDeliverOrders = 0;
        //本日外卖订单总额
        BigDecimal todayOrderBooks = BigDecimal.ZERO;
        //本月外卖订单数
        int monthDeliverOrder = 0;
        //本月外卖订单总额
        BigDecimal monthOrderBooks = BigDecimal.ZERO;
        //查询pos端店铺录入信息(线下订单+外卖订单都是pos端录入的)
        List<OffLineOrder> offLineOrderList = offLineOrderMapper.selectlistByTimeSourceAndShopId(shopDetail.getId(), begin, end, OfflineOrderSource.OFFLINE_POS);
        if (!offLineOrderList.isEmpty()) {
            for (OffLineOrder of : offLineOrderList) {
                List<Integer> getTime = DateUtil.getDayByToday(of.getCreateTime());
                if (getTime.contains(2)) {//本日中
                    todayEnterCount += of.getEnterCount();
                    todayEnterTotal = todayEnterTotal.add(of.getEnterTotal());
                    todayDeliverOrders += of.getDeliveryOrders();
                    todayOrderBooks = todayOrderBooks.add(of.getOrderBooks());
                }
                if (getTime.contains(10)) {
                    monthEnterCount += of.getEnterCount();
                    monthEnterTotal = monthEnterTotal.add(of.getEnterTotal());
                    monthDeliverOrder += of.getDeliveryOrders();
                    monthOrderBooks = monthOrderBooks.add(of.getOrderBooks());
                }
            }
        }
        //查询当日新增用户的订单
        List<Order> newCustomerOrders = orderMapper.selectNewCustomerOrderByShopIdAndTime(shopDetail.getId(), todayBegin, todayEnd);
        //新增用户的订单总数
        int newCustomerOrderNum = 0;
        //新增用户的订单总额
        BigDecimal newCustomerOrderTotal = BigDecimal.ZERO;
        //新增分享用户的的订单总数
        int newShareCustomerOrderNum = 0;
        //新增分享用户的订单总额
        BigDecimal newShareCustomerOrderTotal = BigDecimal.ZERO;
        //新增自然用户的订单总数
        int newNormalCustomerOrderNum = 0;
        //新增自然用户的订单总额
        BigDecimal newNormalCustomerOrderTotal = BigDecimal.ZERO;
        if (!newCustomerOrders.isEmpty()) {
            for (Order o : newCustomerOrders) {
                newCustomerOrderNum++;
                newCustomerOrderTotal = newCustomerOrderTotal.add(getOrderMoney(o.getOrderMode(), o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
                if (o.getCustomer() != null && !StringUtils.isEmpty(o.getCustomer().getShareCustomer())) { //是分享用户
                    newShareCustomerOrderNum++;
                    newShareCustomerOrderTotal = newShareCustomerOrderTotal.add(getOrderMoney(o.getOrderMode(), o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
                } else {
                    newNormalCustomerOrderNum++; //是新增用户
                    newNormalCustomerOrderTotal = newNormalCustomerOrderTotal.add(getOrderMoney(o.getOrderMode(), o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
                }
            }
        }
        //查询回头用户的
        List<BackCustomerDto> backCustomerDtos = orderMapper.selectBackCustomerByShopIdAndTime(shopDetail.getId(), todayBegin, todayEnd);
        //回头用户
        Set<String> backCustomerId = new HashSet<>();
        //二次回头用户
        Set<String> backTwoCustomerId = new HashSet<>();
        //多次回头用户
        Set<String> backTwoMoreCustomerId = new HashSet<>();
        if (!backCustomerDtos.isEmpty()) {
            for (BackCustomerDto b : backCustomerDtos) {
                backCustomerId.add(b.getCustomerId());
                if (b.getNum() == 1) { //只要以前出现过一次那么就是二次回头用户 而非 ==2
                    backTwoCustomerId.add(b.getCustomerId());
                } else if (b.getNum() > 1) {
                    backTwoMoreCustomerId.add(b.getCustomerId());
                }
            }
        }
        //查询当日已消费的订单
        //回头用户的订单总数
        int backCustomerOrderNum = 0;
        //二次回头用户的订单总数
        int backTwoCustomerOrderNum = 0;
        //多次回头用户的订单总数
        int backTwoMoreCustomerOderNum = 0;
        //回头用户的订单总额
        BigDecimal backCustomerOrderTotal = BigDecimal.ZERO;
        //二次回头用户的订单总额
        BigDecimal backTwoCustomerOrderTotal = BigDecimal.ZERO;
        //多次回头用户的订单总额
        BigDecimal backTwoMoreCustomerOrderTotal = BigDecimal.ZERO;
        List<Order> orders = orderMapper.selectCompleteByShopIdAndTime(shopDetail.getId(), todayBegin, todayEnd);
        if (!orders.isEmpty()) {
            for (Order o : orders) {
                if (o.getCreateTime().compareTo(todayBegin) > 0 && o.getCreateTime().compareTo(todayEnd) < 0) {//今日内订单
                    if (backCustomerId.contains(o.getCustomerId())) {
                        backCustomerOrderNum++;
                        backCustomerOrderTotal = backCustomerOrderTotal.add(getOrderMoney(o.getOrderMode(), o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
                    }
                    if (backTwoCustomerId.contains(o.getCustomerId())) {
                        backTwoCustomerOrderNum++;
                        backTwoCustomerOrderTotal = backTwoCustomerOrderTotal.add(getOrderMoney(o.getOrderMode(), o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
                    }
                    if (backTwoMoreCustomerId.contains(o.getCustomerId())) {
                        backTwoMoreCustomerOrderTotal = backTwoMoreCustomerOrderTotal.add(getOrderMoney(o.getOrderMode(), o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
                        backTwoMoreCustomerOderNum++;
                    }
                }
            }
        }
        //2定义resto订单
        //本日resto订单总数 新增+回头
        int todayRestoCount = backCustomerOrderNum + newCustomerOrderNum;
        //本日resto订单总额
        BigDecimal todayRestoTotal = BigDecimal.ZERO;
        //本月resto订单总数
        Set<String> monthRestoCount = new HashSet<>();
        //本月resto订单总额
        BigDecimal monthRestoTotal = BigDecimal.ZERO;
        //定义折扣合计
        BigDecimal discountTotal = BigDecimal.ZERO;
        //红包
        BigDecimal redPackTotal = BigDecimal.ZERO;
        //优惠券
        BigDecimal couponTotal = BigDecimal.ZERO;
        //充值赠送
        BigDecimal chargeReturn = BigDecimal.ZERO;
        //折扣比率
        String discountRatio = "";
        //本日用户消费比率
        String todayCustomerRatio = "";
        //回头用户消费比率
        String todayBackCustomerRatio = "";
        //新增用户比率
        String todayNewCustomerRatio = "";

        List<Order> monthOrders = orderMapper.selectListsmsByShopId(begin, end, shopDetail.getId());
        if (!monthOrders.isEmpty()) {
            for (Order o : monthOrders) {
                //封装   1.resto订单总额     3.resto订单总数  4订单中的实收总额  5新增用户的订单总额  6自然到店的用户总额  7分享到店的用户总额
                //8回头用户的订单总额  9二次回头用户的订单总额  10多次回头用户的订单总额 11折扣合计 12红包 13优惠券 14 充值赠送 15折扣比率
                //本日 begin-----------------------
                // if (DateUtil.getDayByToday(o.getCreateTime()).contains(2)) {
                /**
                 * 报表数据中的订单数  如果子订单和父订单算是一个订单
                 * 小程序+每日短信里的子订单和父订单算是两个订单
                 *
                 */

                if (o.getCreateTime().compareTo(todayBegin) > 0 && o.getCreateTime().compareTo(todayEnd) < 0) {//今日内订单
                    //1.resto订单总额
                    todayRestoTotal = todayRestoTotal.add(getOrderMoney(o.getOrderMode(), o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));

                    //11折扣合计 12红包 13优惠券 14 充值赠送 15折扣比率
                    if (!o.getOrderPaymentItems().isEmpty()) {
                        //订单支付项
                        for (OrderPaymentItem oi : o.getOrderPaymentItems()) {
                            if (oi.getPaymentModeId() == PayMode.ACCOUNT_PAY) {
                                redPackTotal = redPackTotal.add(oi.getPayValue());
                            } else if (oi.getPaymentModeId() == PayMode.COUPON_PAY) {
                                couponTotal = couponTotal.add(oi.getPayValue());
                            } else if (oi.getPaymentModeId() == PayMode.REWARD_PAY) {
                                chargeReturn = chargeReturn.add(oi.getPayValue());
                            }
                        }
                    }
                    discountTotal = redPackTotal.add(couponTotal).add(chargeReturn);
                    if (todayRestoTotal.add(discountTotal).compareTo(BigDecimal.ZERO) > 0) {
                        discountRatio = discountTotal.divide(todayRestoTotal.add(discountTotal), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).toString();
                    }
                }
                //本日end----------
                //本月开始------
                //订单总额
                monthRestoTotal = monthRestoTotal.add(getOrderMoney(o.getOrderMode(), o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
                //本月结束
            }
        }

        //本日用户消费比率 R+线下+外卖
        //到店总笔数 线上+线下
        double dmax = todayEnterCount + todayRestoCount;
        if (dmax != 0) {
            //本日用户消费比率
            todayCustomerRatio = formatDouble((todayRestoCount / dmax) * 100);
            //本日新增用户利率
            todayNewCustomerRatio = formatDouble((newCustomerOrderNum / dmax) * 100);
            //本日回头用户的消费比率
            todayBackCustomerRatio = formatDouble((backCustomerOrderNum / dmax) * 100);
        }

        //五星
        int fiveStar = 0;
        //四星
        int fourStar = 0;
        //3星-1星
        int oneToThreeStar = 0;
        //3定义满意度
        //本日满意度
        String todaySatisfaction = "";
        //本旬满意度
        String theTenDaySatisfaction = "";
        //本月满意度
        String monthSatisfaction = "";

        int dayAppraiseNum = 0;//当日评价的总单数
        int xunAppraiseNum = 0;//本旬评价的总单数
        int monthAppraiseSum = 0;//本月评价的单数

        double dayAppraiseSum = 0;//当日所有评价的总分数
        double xunAppraiseSum = 0;//上旬所有评价的总分数
        double monthAppraiseNum = 0;//本月所有评价的总分数

        /**
         * 评价 和 满意度 错误的原因 用户可能今天 下单 但是隔天
         * 去评价 而现在 是查当天下单当天评价
         *
         *
         */

        //单独查询评价和分数

        List<Appraise> appraises = appraiseService.selectByTimeAndShopId(shopDetail.getId(), begin, end);

        if (!appraises.isEmpty()) {
            for (Appraise a : appraises) {
                //本日 begin-----------------------
                if (DateUtil.getDayByToday(a.getCreateTime()).contains(2)) {
                    dayAppraiseNum++;
                    dayAppraiseSum += a.getLevel() * 20;
                    if (a.getLevel() == 5) {
                        fiveStar++;
                    } else if (a.getLevel() == 4) {
                        fourStar++;
                    } else {
                        oneToThreeStar++;
                    }
                }
                //本旬开始
                if (DateUtil.getDayByToday(a.getCreateTime()).contains(12)) {
                    //2.满意度
                    xunAppraiseNum++;
                    xunAppraiseSum += a.getLevel() * 20;
                }
                //本旬结束

                //本月开始------
                //.满意度

                monthAppraiseNum++;
                monthAppraiseSum += a.getLevel() * 20;

                //本月结束
            }
            //循环完之后操作--
            if (dayAppraiseNum != 0) {
                todaySatisfaction = formatDouble(dayAppraiseSum / dayAppraiseNum);
            }
            if (xunAppraiseNum != 0) {
                theTenDaySatisfaction = formatDouble(xunAppraiseSum / xunAppraiseNum);
            }

            if (monthAppraiseNum != 0) {
                monthSatisfaction = formatDouble(monthAppraiseSum / monthAppraiseNum);
            }

            //评论结束------------------------
        }

        //发送本日信息 本月信息 上旬信息
        //本日信息
        StringBuilder todayContent = new StringBuilder();

        todayContent.append("{")
                .append("shopName:").append("'").append(shopDetail.getName()).append("'").append(",")
                .append("datetime:").append("'").append(DateUtil.formatDate(new Date(), "yyyy-MM-dd")).append("'").append(",")
                //到店总笔数(r+线下)-----
                .append("arriveCount:").append("'").append(todayEnterCount + todayRestoCount).append("'").append(",")
                //到店消费总额 我们的总额+线下的总额，不包含外卖金额
                .append("arriveTotalAmount:").append("'").append(todayEnterTotal.add(todayRestoTotal)).append("'").append(",")
                //用户消费笔数  R+订单总数
                .append("customerPayCount:").append("'").append(todayRestoCount).append("'").append(",")
                //用户消费金额: (r+订单总额)
                .append("customerPayAmount:").append("'").append(todayRestoTotal).append("'").append(",")
                //用户消费比率  今日 R+订单总数/（R+订单总数+线下堂吃订单数+外卖订单数））
                .append("userPayPercent:").append("'").append(todayCustomerRatio).append("%").append("'").append(",")
                //回头消费比率 R+多次消费用户数/R+消费用户数）
                .append("userBackPercent:").append("'").append(todayBackCustomerRatio).append("%").append("'").append(",")
                //新增用户比率 （今日 R+新增用户数/R+消费用户数）
                .append("newCustomerPercent:").append("'").append(todayNewCustomerRatio).append("%").append("'").append(",")
                //新用户消费
                .append("newCustomerPay:").append("'").append(newCustomerOrderNum).append("笔/").append(newCustomerOrderTotal).append("'").append(",")
                // 其中自然用户
                .append("natureCustomerPay:").append("'").append(newNormalCustomerOrderNum).append("笔/").append(newNormalCustomerOrderTotal).append("'").append(",")
                //其中分享用户
                .append("shareCustomerPay:").append("'").append(newShareCustomerOrderNum).append("笔/").append(newShareCustomerOrderTotal).append("'").append(",")
                //回头用户消费
                .append("customerBackPay:").append("'").append(backCustomerOrderNum).append("笔/").append(backCustomerOrderTotal).append("'").append(",")
                //二次回头用户
                .append("secondBackPay:").append("'").append(backTwoCustomerOrderNum).append("笔/").append(backTwoCustomerOrderTotal).append("'").append(",")
                //多次回头
                .append("moreBackPay:").append("'").append(backTwoMoreCustomerOderNum).append("笔/").append(backTwoMoreCustomerOrderTotal).append("'").append(",")
                //折扣合计:648.05（使用红包总额+使用优惠券总额+使用充值赠送总额）
                .append("discountTotal:").append("'").append(discountTotal).append("'").append(",")
                //红包
                .append("redPayTotal:").append("'").append(redPackTotal).append("'").append(",")
                //优惠券
                .append("couponTotal:").append("'").append(couponTotal).append("'").append(",")
                //充值赠送
                .append("chargeTotal:").append("'").append(chargeReturn).append("'").append(",")
                //折扣比率2.7%（折扣合计/(堂吃消费总额＋折扣合计)
                .append("discountPercent:").append("'").append(discountRatio).append("%").append("'").append(",")
                //五星评论
                .append("goodCount:").append("'").append(fiveStar).append("'").append(",")
                //本日改进意见
                .append("badCount:").append("'").append(fourStar).append("'").append(",")
                //本日差评投诉
                .append("terribleCount:").append("'").append(oneToThreeStar).append("'").append(",")
                //本日满意度
                .append("satisfied1:").append("'").append(todaySatisfaction).append("'").append(",")
                //本旬满意度
                .append("satisfied2:").append("'").append(theTenDaySatisfaction).append("'").append(",")
                //本月满意度
                .append("satisfied3:").append("'").append(monthSatisfaction).append("'").append(",")
                //今日外卖金额
                .append("outFoodTotal:").append("'").append(todayOrderBooks).append("'").append(",")
                //总营业额
                .append("totalOrderMoney:").append("'").append(todayEnterTotal.add(todayRestoTotal).add(todayOrderBooks)).append("'").append(",")
                //本月总额
                .append("monthTotalMoney:").append("'").append(monthOrderBooks.add(monthEnterTotal).add(monthRestoTotal).add(monthOrderBooks)).append("'")
                .append("}");

        //封装微信推送文本
        StringBuilder sb = new StringBuilder();
        sb
                .append("店铺名称:").append(shopDetail.getName()).append("\n")
                .append("时间:").append(DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss")).append("\n")
                .append("到店总笔数:").append(todayEnterCount + todayRestoCount).append("\n")
                .append("到店消费总额:").append(todayEnterTotal.add(todayRestoTotal)).append("\n")
                .append("---------------------").append("\n")
                .append("用户消费比数:").append(todayRestoCount).append("\n")
                .append("用户消费金额").append(todayRestoTotal).append("\n")
                .append("---------------------").append("\n")
                .append("用户消费比率:").append(todayCustomerRatio).append("%").append("\n")
                .append("回头消费比率:").append(todayBackCustomerRatio).append("%").append("\n")
                .append("新增用户比率:").append(todayNewCustomerRatio).append("%").append("\n")
                .append("---------------------").append("\n")
                .append("新用户消费:").append(newCustomerOrderNum).append("笔/").append(newCustomerOrderTotal).append("\n")
                .append("其中自然用户:").append(newNormalCustomerOrderNum).append("笔/").append(newNormalCustomerOrderTotal).append("\n")
                .append("其中分享用户:").append(newShareCustomerOrderNum).append("笔/").append(newShareCustomerOrderTotal).append("\n")
                .append("回头用户消费:").append(backCustomerOrderNum).append("笔/").append(backCustomerOrderTotal).append("\n")
                .append("二次回头用户:").append(backTwoCustomerOrderNum).append("笔/").append(backTwoCustomerOrderTotal).append("\n")
                .append("多次回头用户:").append(backTwoMoreCustomerOderNum).append("笔/").append(backTwoMoreCustomerOrderTotal).append("\n")
                .append("---------------------").append("\n")
                .append("折扣合计:").append(discountTotal).append("\n")
                .append("红包:").append(redPackTotal).append("\n")
                .append("优惠券:").append(couponTotal).append("\n")
                .append("充值赠送:").append(chargeReturn).append("\n")
                .append("折扣比率").append(discountRatio).append("\n")
                .append("---------------------").append("\n")
                .append("本日五星评论:").append(fiveStar).append("\n")
                .append("本日更改意见:").append(fourStar).append("\n")
                .append("本日差评投诉:").append(oneToThreeStar).append("\n")
                .append("本日满意度:").append(todaySatisfaction).append("\n")
                .append("本旬满意度:").append(theTenDaySatisfaction).append("\n")
                .append("本月满意度:").append(monthSatisfaction).append("\n")
                .append("---------------------").append("\n")
                .append("今日外卖金额:").append(todayOrderBooks).append("\n")
                .append("今日总营业额:").append(todayEnterTotal.add(todayRestoTotal).add(todayOrderBooks)).append("\n")
                .append("本月总额:").append(monthOrderBooks.add(monthEnterTotal).add(monthRestoTotal).add(monthOrderBooks)).append("\n");

        Map<String, String> map = new HashMap<>();
        map.put("sms", todayContent.toString());
        map.put("wechat", sb.toString());
        return map;
    }

    private void insertDateData(ShopDetail shopDetail, OffLineOrder offLineOrder, Wether wether,Brand brand) {
        // 查询该店铺是否结过店
        OffLineOrder offLineOrder1 = offLineOrderMapper.selectByTimeSourceAndShopId(OfflineOrderSource.OFFLINE_POS, shopDetail.getId(), DateUtil.getDateBegin(new Date()), DateUtil.getDateEnd(new Date()));
        if (null != offLineOrder1) {
            offLineOrder1.setState(0);
            offLineOrderMapper.updateByPrimaryKeySelective(offLineOrder1);
        }
        offLineOrder.setId(ApplicationUtils.randomUUID());
        offLineOrder.setState(1);
        offLineOrder.setResource(OfflineOrderSource.OFFLINE_POS);
        offLineOrderMapper.insertSelective(offLineOrder);

        //----1.定义时间---
        Date todayBegin = DateUtil.getDateBegin(new Date());
        Date todayEnd = DateUtil.getDateEnd(new Date());

        //本月的开始时间 本月结束时间
        String begin = DateUtil.getMonthBegin();
        Date monthBegin = DateUtil.getDateBegin(DateUtil.fomatDate(begin));
        Date monthEnd = todayEnd;

        //旬开始时间 旬结束时间

        Date xunBegin = new Date() ;
        Date xunEnd = todayEnd;

        int temp = DateUtil.getEarlyMidLate(new Date());//1.上旬 2.中旬 3下旬
        if(temp==1){
            xunBegin = monthBegin;
        }else if(temp==2){
            xunBegin = DateUtil.getAfterDayDate(monthBegin,10);
        }else if(temp==3){
            xunBegin = DateUtil.getAfterDayDate(monthBegin,20);
        }

        //三.定义线下订单
        //本日线下订单总数(堂吃)
        int todayEnterCount = 0;
        //本日线下订单总额(堂吃)
        BigDecimal todayEnterTotal = BigDecimal.ZERO;

        //本旬线下订单总数(堂吃)
        int xunEnterCount = 0;
        //本旬线下订单总额(堂吃)
        BigDecimal xunEnterTotal = BigDecimal.ZERO;

        //本月线下订单总数
        int monthEnterCount = 0;
        //本月线下订单总额
        BigDecimal monthEnterTotal = BigDecimal.ZERO;

        //4.外卖订单
        //本日外卖订单数
        int todayDeliverOrders = 0;
        //本日外卖订单总额
        BigDecimal todayOrderBooks = BigDecimal.ZERO;

        //本旬外卖订单数
        int xunDeliverOrders = 0;
        //本旬外卖订单总额
        BigDecimal xunOrderBooks = BigDecimal.ZERO;

        //本月外卖订单数
        int monthDeliverOrders = 0;
        //本月外卖订单总额
        BigDecimal monthOrderBooks = BigDecimal.ZERO;
        //查询pos端店铺录入信息(线下订单+外卖订单都是pos端录入的)

        //查询本日店铺录入信息(线下订单+外卖订单都是pos端录入的)
        List<OffLineOrder> todayOffLineOrderList = offLineOrderMapper.selectlistByTimeSourceAndShopId(shopDetail.getId(), todayBegin,todayEnd, OfflineOrderSource.OFFLINE_POS);
        if (!todayOffLineOrderList.isEmpty()) {
            for (OffLineOrder of : todayOffLineOrderList) {
                    todayEnterCount += of.getEnterCount();
                    todayEnterTotal = todayEnterTotal.add(of.getEnterTotal());
                    todayDeliverOrders += of.getDeliveryOrders();
                    todayOrderBooks = todayOrderBooks.add(of.getOrderBooks());
            }
        }

        //查询本旬店铺录入信息(线下订单+外卖订单都是pos端录入的)
        List<OffLineOrder> xunOffLineOrderList = offLineOrderMapper.selectlistByTimeSourceAndShopId(shopDetail.getId(), xunBegin, xunEnd, OfflineOrderSource.OFFLINE_POS);
        if (!xunOffLineOrderList.isEmpty()) {
            for (OffLineOrder of : xunOffLineOrderList) {
                xunEnterCount += of.getEnterCount();
                xunEnterTotal = xunEnterTotal.add(of.getEnterTotal());
                xunDeliverOrders += of.getDeliveryOrders();
                xunOrderBooks = xunOrderBooks.add(of.getOrderBooks());
            }
        }

        //查询本月店铺录入信息(线下订单+外卖订单都是pos端录入的)
        List<OffLineOrder> monthOffLineOrderList = offLineOrderMapper.selectlistByTimeSourceAndShopId(shopDetail.getId(), monthBegin, monthEnd, OfflineOrderSource.OFFLINE_POS);
        if (!monthOffLineOrderList.isEmpty()) {
            for (OffLineOrder of : monthOffLineOrderList) {
                monthEnterCount += of.getEnterCount();
                monthEnterTotal = monthEnterTotal.add(of.getEnterTotal());
                monthDeliverOrders += of.getDeliveryOrders();
                monthOrderBooks = monthOrderBooks.add(of.getOrderBooks());
            }
        }

        //查询当日新增用户的订单
        List<Order> todayNewCustomerOrders = orderMapper.selectNewCustomerOrderByShopIdAndTime(shopDetail.getId(), todayBegin, todayEnd);
        //新增用户的订单总数
        int todayNewCustomerOrderNum = 0;
        //新增用户的订单总额
        BigDecimal todayNewCustomerOrderTotal = BigDecimal.ZERO;
        //新增分享用户的的订单总数
        int todayNewShareCustomerOrderNum = 0;
        //新增分享用户的订单总额
        BigDecimal  todayNewShareCustomerOrderTotal = BigDecimal.ZERO;
        //新增自然用户的订单总数
        int  todayNewNormalCustomerOrderNum = 0;
        //新增自然用户的订单总额
        BigDecimal  todayNewNormalCustomerOrderTotal = BigDecimal.ZERO;
        if (!todayNewCustomerOrders.isEmpty()) {
            for (Order o : todayNewCustomerOrders) {
                todayNewCustomerOrderNum++;
                todayNewCustomerOrderTotal = todayNewCustomerOrderTotal.add(getOrderMoney(o.getOrderMode(), o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
                if (o.getCustomer() != null && !StringUtils.isEmpty(o.getCustomer().getShareCustomer())) { //是分享用户
                    todayNewShareCustomerOrderNum++;
                    todayNewShareCustomerOrderTotal = todayNewShareCustomerOrderTotal.add(getOrderMoney(o.getOrderMode(), o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
                } else {
                    todayNewNormalCustomerOrderNum++; //是新增用户
                    todayNewNormalCustomerOrderTotal = todayNewNormalCustomerOrderTotal.add(getOrderMoney(o.getOrderMode(), o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
                }
            }
        }


        //查询回头用户的
        List<BackCustomerDto> todayBackCustomerDtos = orderMapper.selectBackCustomerByShopIdAndTime(shopDetail.getId(), todayBegin, todayEnd);
        //回头用户
        Set<String> todayBackCustomerId = new HashSet<>();
        //二次回头用户
        Set<String> todayBackTwoCustomerId = new HashSet<>();
        //多次回头用户
        Set<String> todayBackTwoMoreCustomerId = new HashSet<>();
        if (!todayBackCustomerDtos.isEmpty()) {
            for (BackCustomerDto b : todayBackCustomerDtos) {
                todayBackCustomerId.add(b.getCustomerId());
                if (b.getNum() == 1) { //只要以前出现过一次那么就是二次回头用户 而非 ==2
                    todayBackTwoCustomerId.add(b.getCustomerId());
                } else if (b.getNum() > 1) {
                    todayBackTwoMoreCustomerId.add(b.getCustomerId());
                }
            }
        }
        //查询当日已消费的订单
        //回头用户的订单总数
        int todayBackCustomerOrderNum = 0;
        //二次回头用户的订单总数
        int  todayBackTwoCustomerOrderNum = 0;
        //多次回头用户的订单总数
        int  todayBackTwoMoreCustomerOderNum = 0;
        //回头用户的订单总额
        BigDecimal  todayBackCustomerOrderTotal = BigDecimal.ZERO;
        //二次回头用户的订单总额
        BigDecimal todayBackTwoCustomerOrderTotal = BigDecimal.ZERO;
        //多次回头用户的订单总额
        BigDecimal todayBackTwoMoreCustomerOrderTotal = BigDecimal.ZERO;
        List<Order> orders = orderMapper.selectCompleteByShopIdAndTime(shopDetail.getId(), todayBegin, todayEnd);
        if (!orders.isEmpty()) {
            for (Order o : orders) {
                if (todayBackCustomerId.contains(o.getCustomerId())) {
                    todayBackCustomerOrderNum++;
                    todayBackCustomerOrderTotal = todayBackCustomerOrderTotal.add(getOrderMoney(o.getOrderMode(), o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
                }
                if (todayBackTwoCustomerId.contains(o.getCustomerId())) {
                    todayBackTwoCustomerOrderNum++;
                    todayBackTwoCustomerOrderTotal = todayBackTwoCustomerOrderTotal.add(getOrderMoney(o.getOrderMode(), o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
                }
                if (todayBackTwoMoreCustomerId.contains(o.getCustomerId())) {
                    todayBackTwoMoreCustomerOrderTotal = todayBackTwoMoreCustomerOrderTotal.add(getOrderMoney(o.getOrderMode(), o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
                    todayBackTwoMoreCustomerOderNum++;
                }
            }
        }
        //2定义resto订单
        //本日resto订单总数
        int todayRestoCount = todayNewCustomerOrderNum+todayBackCustomerOrderNum;

        //本日resto订单总额
        BigDecimal todayRestoTotal = BigDecimal.ZERO;
        //本月resto订单总数
        Set<String> monthRestoCount = new HashSet<>();
        //本月resto订单总额
        BigDecimal monthRestoTotal = BigDecimal.ZERO;
        //定义折扣合计
        BigDecimal discountTotal = BigDecimal.ZERO;
        //红包
        BigDecimal redPackTotal = BigDecimal.ZERO;
        //优惠券
        BigDecimal couponTotal = BigDecimal.ZERO;
        //充值赠送
        BigDecimal chargeReturn = BigDecimal.ZERO;
        //折扣比率
        String discountRatio = "";
        //本日用户消费比率
        String todayCustomerRatio = "";
        //回头用户消费比率
        String todayBackCustomerRatio = "";
        //新增用户比率
        String todayNewCustomerRatio = "";

        List<Order> monthOrders = orderMapper.selectListsmsByShopId(monthBegin, monthEnd, shopDetail.getId());
        if (!monthOrders.isEmpty()) {
            for (Order o : monthOrders) {
                //封装   1.resto订单总额     3.resto订单总数  4订单中的实收总额  5新增用户的订单总额  6自然到店的用户总额  7分享到店的用户总额
                //8回头用户的订单总额  9二次回头用户的订单总额  10多次回头用户的订单总额 11折扣合计 12红包 13优惠券 14 充值赠送 15折扣比率
                //本日 begin-----------------------
                // if (DateUtil.getDayByToday(o.getCreateTime()).contains(2)) {
                /**
                 * 报表数据中的订单数  如果子订单和父订单算是一个订单
                 * 小程序+每日短信里的子订单和父订单算是两个订单
                 *
                 */
                List<Integer> getTime = DateUtil.getDayByToday(o.getCreateTime());
                if (getTime.contains(2)) {//今日内订单
                    //1.resto订单总额
                    todayRestoTotal = todayRestoTotal.add(getOrderMoney(o.getOrderMode(), o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
                    //11折扣合计 12红包 13优惠券 14 充值赠送 15折扣比率
                    if (!o.getOrderPaymentItems().isEmpty()) {
                        //订单支付项
                        for (OrderPaymentItem oi : o.getOrderPaymentItems()) {
                            if (oi.getPaymentModeId() == PayMode.ACCOUNT_PAY) {
                                redPackTotal = redPackTotal.add(oi.getPayValue());
                            } else if (oi.getPaymentModeId() == PayMode.COUPON_PAY) {
                                couponTotal = couponTotal.add(oi.getPayValue());
                            } else if (oi.getPaymentModeId() == PayMode.REWARD_PAY) {
                                chargeReturn = chargeReturn.add(oi.getPayValue());
                            }
                        }
                    }
                    discountTotal = redPackTotal.add(couponTotal).add(chargeReturn);
                    if (todayRestoTotal.add(discountTotal).compareTo(BigDecimal.ZERO) > 0) {
                        discountRatio = discountTotal.divide(todayRestoTotal.add(discountTotal), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).toString();
                    }
                }
                //本日end----------
                //本月开始------
                //订单总额
                monthRestoTotal = monthRestoTotal.add(getOrderMoney(o.getOrderMode(), o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
                //本月结束
            }
        }

        //本日用户消费比率 R+线下+外卖
        //到店总笔数 线上+线下
        double dmax = todayEnterCount + todayRestoCount;
        if (dmax != 0) {
            //本日用户消费比率
            todayCustomerRatio = formatDouble((todayRestoCount / dmax) * 100);
            //本日新增用户利率
            todayNewCustomerRatio = formatDouble((todayNewCustomerOrderNum / dmax) * 100);
            //本日回头用户的消费比率
            todayBackCustomerRatio = formatDouble((todayBackCustomerOrderNum / dmax) * 100);
        }

        //本日五星
        int todayFiveStar = 0;
        //本日四星
        int todayFourStar = 0;
        //本日3星-1星
        int todayOneToThreeStar = 0;

        //本旬五星
        int xunFiveStar = 0;
        //本旬四星
        int xunFourStar = 0;
        //本旬3星-1星
        int xunOneToThreeStar = 0;

        //本月五星
        int monthFiveStar = 0;
        //本月四星
        int monthFourStar = 0;
        //本月3星-1星
        int monthOneToThreeStar = 0;


        //3定义满意度
        //本日满意度
        String todaySatisfaction = "";
        //本旬满意度
        String xunSatisfaction = "";
        //本月满意度
        String monthSatisfaction = "";

        int todayAppraiseNum = 0;//当日评价的总单数
        int xunAppraiseNum = 0;//本旬评价的总单数
        int monthAppraiseSum = 0;//本月评价的单数

        double todayAppraiseSum = 0;//当日所有评价的总分数
        double xunAppraiseSum = 0;//上旬所有评价的总分数
        double monthAppraiseNum = 0;//本月所有评价的总分数

        /**
         * 评价 和 满意度 错误的原因 用户可能今天 下单 但是隔天
         * 去评价 而现在 是查当天下单当天评价
         *
         *
         */

        //单独查询评价和分数

        //查本日
        List<Appraise> todayAppraises = appraiseService.selectByTimeAndShopId(shopDetail.getId(), todayBegin, todayEnd);
        //存评论数据
        if(!todayAppraises.isEmpty()){
            for(Appraise a:todayAppraises){
                JdbcSmsUtils.saveTodayAppraise(a,brand.getId(),shopDetail.getId());
            }

        }


        if (!todayAppraises.isEmpty()) {
            for (Appraise a : todayAppraises) {
                //本日 begin-----------------------
                todayAppraiseNum++;
                todayAppraiseSum += a.getLevel() * 20;
                if (a.getLevel() == 5) {
                    todayFiveStar++;
                } else if (a.getLevel() == 4) {
                    todayFourStar++;
                } else {
                    todayOneToThreeStar++;
                }
            }

            //循环完之后操作--
            if (todayAppraiseNum != 0) {
                todaySatisfaction = formatDouble(todayAppraiseSum / todayAppraiseNum);
            }

        }

        //查本旬
        List<Appraise> xunAppraises = appraiseService.selectByTimeAndShopId(shopDetail.getId(), xunBegin, xunEnd);

        if (!xunAppraises.isEmpty()) {
            for (Appraise a : xunAppraises) {
                //本旬 begin-----------------------
                xunAppraiseNum++;
                xunAppraiseSum += a.getLevel() * 20;
                if (a.getLevel() == 5) {
                    xunFiveStar++;
                } else if (a.getLevel() == 4) {
                    xunFourStar++;
                } else {
                    xunOneToThreeStar++;
                }
            }

            //循环完之后操作--
            if (xunAppraiseNum != 0) {
                xunSatisfaction = formatDouble(xunAppraiseSum / xunAppraiseNum);
            }

        }

        //查本月
        List<Appraise> monthAppraises = appraiseService.selectByTimeAndShopId(shopDetail.getId(), monthBegin, monthEnd);

        if (!monthAppraises.isEmpty()) {
            for (Appraise a : monthAppraises) {
                //本月 begin-----------------------
                monthAppraiseNum++;
                monthAppraiseSum += a.getLevel() * 20;
                if (a.getLevel() == 5) {
                    monthFiveStar++;
                } else if (a.getLevel() == 4) {
                    monthFourStar++;
                } else {
                    monthOneToThreeStar++;
                }
            }

            //循环完之后操作--
            if (monthAppraiseNum != 0) {
                monthSatisfaction = formatDouble(monthAppraiseSum / monthAppraiseNum);
            }

        }

        //存满意度
        JdbcSmsUtils.saveStations(todaySatisfaction,xunSatisfaction,monthSatisfaction,brand.getId(),shopDetail.getId());


        //查询菜品今日top10
        //1.查询好评的总数(本日)
        int  todayGoodNum = 0;
        todayGoodNum = articleTopService.selectSumGoodByTime(todayBegin, todayEnd, shopDetail.getId());
        //查询差评总数
        int todayBadNum = 0;
        todayBadNum = articleTopService.selectSumBadByTime(todayBegin, todayEnd, shopDetail.getId());

        //查询好评top10
        List<ArticleTopDto> todayGoodList = articleTopService.selectListByTimeAndGoodType(todayBegin, todayEnd, shopDetail.getId());

        //查询差评top10
        List<ArticleTopDto> todayBadList = articleTopService.selectListByTimeAndBadType(todayBegin, todayEnd, shopDetail.getId());

        //yz 2017-07-25 dayDataMessage 被merge了
        //存储结店数据
        int times=1;//默认是今天第一次结店  次数存redis中 之后++
        DayDataMessage ds = new DayDataMessage();
        ds.setId(ApplicationUtils.randomUUID());
        ds.setShopId(shopDetail.getId());
        ds.setType(DayMessageType.DAY_TYPE);//日结
        ds.setShopName(shopDetail.getName());
        ds.setWeekDay(wether.getWeekady());
        ds.setDate(new Date());
        ds.setTimes(times);//当日结店次数
        ds.setWether(wether.getDayWeather());
        ds.setTemperature(wether.getDayTemperature());
        ds.setOrderNumber(todayEnterCount + todayRestoCount);//到店总笔数
        ds.setOrderSum(todayEnterTotal.add(todayRestoTotal));//到店消费总额
        ds.setCustomerOrderNumber(todayRestoCount);
        ds.setCustomerOrderSum(todayRestoTotal);
        ds.setCustomerOrderRatio(todayCustomerRatio+"%");
        ds.setNewCustomerOrderRatio(todayNewCustomerRatio+"%");
        ds.setBackCustomerOrderRatio(todayBackCustomerRatio+"%");
        ds.setNewCuostomerOrderNum(todayNewCustomerOrderNum);//新用户订单数
        ds.setNewCustomerOrderSum(todayNewCustomerOrderTotal);
        ds.setNewNormalCustomerOrderNum(todayNewNormalCustomerOrderNum);
        ds.setNewNormalCustomerOrderSum(todayNewNormalCustomerOrderTotal);
        ds.setNewShareCustomerOrderNum(todayNewShareCustomerOrderNum);
        ds.setNewShareCustomerOrderSum(todayNewShareCustomerOrderTotal);
        ds.setBackCustomerOrderNum(todayBackCustomerOrderNum);
        ds.setBackCustomerOrderSum(todayBackCustomerOrderTotal);
        ds.setBackTwoCustomerOrderNum(todayBackTwoCustomerOrderNum);
        ds.setBackTwoCustomerOrderSum(todayBackTwoCustomerOrderTotal);
        ds.setBackTwoMoreCustomerOrderNum(todayBackTwoMoreCustomerOderNum);
        ds.setBackTwoMoreCustomerOrderSum(todayBackTwoMoreCustomerOrderTotal);
        ds.setDiscountTotal(discountTotal);
        ds.setRedPack(redPackTotal);
        ds.setCoupon(couponTotal);
        ds.setChargeReward(chargeReturn);
        ds.setDiscountRatio(discountRatio);
        ds.setTakeawayTotal(todayOrderBooks);
        ds.setBussinessTotal(todayEnterTotal.add(todayRestoTotal).add(todayOrderBooks));//本日营业总额
        ds.setMonthTotal(monthOrderBooks.add(monthEnterTotal).add(monthRestoTotal));//本月营业总额
        dayDataMessageService.insert(ds);
        JdbcSmsUtils.saveDayDataMessage(ds,shopDetail.getId());


        //存今日goodTop10
        if(todayGoodList!=null&&!todayGoodList.isEmpty()){
            for(int i=0;i<todayGoodList.size();i++){
                JdbcSmsUtils.saveGoodTop(todayGoodList.get(i),brand,shopDetail,MessageType.DAY_MESSAGE,todayGoodNum,(i+1));
            }

        }

        //存今日BadTop10
        if(todayBadList!=null&&!todayBadList.isEmpty()){
            for(int i=0;i<todayBadList.size();i++){
                JdbcSmsUtils.saveBadTop(todayBadList.get(i),brand,shopDetail,MessageType.DAY_MESSAGE,todayBadNum,(i+1));
            }

        }

        //查询菜品本旬top10
        //1.查询好评的总数(本日)
        int  xunGoodNum = 0;
        xunGoodNum = articleTopService.selectSumGoodByTime(xunBegin, xunEnd, shopDetail.getId());
        //查询差评总数
        int xunBadNum = 0;
        xunBadNum = articleTopService.selectSumBadByTime(xunBegin, xunEnd, shopDetail.getId());

        //查询好评top10
        List<ArticleTopDto> xunGoodList = articleTopService.selectListByTimeAndGoodType(xunBegin, xunEnd, shopDetail.getId());

        //查询差评top10
        List<ArticleTopDto> xunBadList = articleTopService.selectListByTimeAndBadType(xunBegin, xunEnd, shopDetail.getId());

        //存本旬goodTop10
        if(xunGoodList!=null&&!xunGoodList.isEmpty()){
            for(int i=0;i<xunGoodList.size();i++){
                JdbcSmsUtils.saveGoodTop(xunGoodList.get(i),brand,shopDetail,MessageType.XUN_MESSAGE,xunGoodNum,(i+1));
            }

        }

        //存本旬BadTop10
        if(xunBadList!=null&&!xunBadList.isEmpty()){
            for(int i=0;i<xunBadList.size();i++){
                JdbcSmsUtils.saveBadTop(xunBadList.get(i),brand,shopDetail,MessageType.XUN_MESSAGE,xunBadNum,(i+1));
            }

        }


        //查询菜品本月top10
        //1.查询好评的总数(本月)
        int  monthGoodNum = 0;
        monthGoodNum = articleTopService.selectSumGoodByTime(monthBegin, monthEnd, shopDetail.getId());
        //查询差评总数
        int monthBadNum = 0;
        monthBadNum = articleTopService.selectSumBadByTime(monthBegin, monthEnd, shopDetail.getId());

        //查询好评top10
        List<ArticleTopDto> monthGoodList = articleTopService.selectListByTimeAndGoodType(monthBegin, monthEnd, shopDetail.getId());

        //查询差评top10
        List<ArticleTopDto> monthBadList = articleTopService.selectListByTimeAndBadType(monthBegin, monthEnd, shopDetail.getId());

        //存本月goodTop10
        if(monthGoodList!=null&&!monthGoodList.isEmpty()){
            for(int i=0;i<monthGoodList.size();i++){
                JdbcSmsUtils.saveGoodTop(monthGoodList.get(i),brand,shopDetail,MessageType.MONTH_MESSAGE,monthGoodNum,(i+1));
            }

        }

        //存本月BadTop10
        if(monthBadList!=null&&!monthBadList.isEmpty()){
            for(int i=0;i<monthBadList.size();i++){
                JdbcSmsUtils.saveBadTop(monthBadList.get(i),brand,shopDetail,MessageType.MONTH_MESSAGE,monthBadNum,(i+1));
            }

        }

        //存评论数据
        DayAppraiseMessageWithBLOBs dm = new DayAppraiseMessageWithBLOBs();
        dm.setId(ApplicationUtils.randomUUID());
        dm.setShopId(shopDetail.getId());
        dm.setShopName(shopDetail.getName());
        dm.setDate(new Date());
        dm.setState(true);
        dm.setWether(wether.getDayWeather());
        dm.setWeekDay(wether.getWeekady());
        dm.setTemperature(wether.getDayTemperature());
        dm.setType(DayMessageType.DAY_TYPE);
        dm.setFiveStar(todayFiveStar);
        dm.setFourStar(todayFourStar);
        dm.setOneThreeStar(todayOneToThreeStar);
        dm.setDaySatisfaction(todaySatisfaction);
        dm.setXunSatisfaction(xunSatisfaction);
        dm.setMonthSatisfaction(monthSatisfaction);
        if (todayGoodNum == 0) {//无好评
            dm.setRedList("----无好评----");
        } else {
            if (!todayGoodList.isEmpty()) {
                com.alibaba.fastjson.JSONObject redJson = new com.alibaba.fastjson.JSONObject();
                for (int i = 0; i < todayGoodList.size(); i++) {
                    //1、27% 剁椒鱼头
                    // sbScore.append("top"+(i + 1)).append("：").append(NumberUtil.getFormat(todayGoodList.get(i).getNum(), todayGoodNum)).append("%").append(" ").append(todayGoodList.get(i).getName())
                    StringBuilder sb = new StringBuilder();
                    sb.append(NumberUtil.getFormat(todayGoodList.get(i).getNum(), todayGoodNum)).append("%").append(" ").append(todayGoodList.get(i).getName());
                    redJson.put("top"+(i+1),sb.toString());
                }
                dm.setRedList(com.alibaba.fastjson.JSONObject.toJSONString(redJson));
            }
        }
        if (todayBadNum == 0) {//无差评
            dm.setBadList("------无差评-----");
        } else {
            if (!todayBadList.isEmpty()) {
                com.alibaba.fastjson.JSONObject bakcJson = new com.alibaba.fastjson.JSONObject();
                for (int i = 0; i < todayBadList.size(); i++) {
                    //1、27% 剁椒鱼头
                    //sbScore.append("top"+(i + 1)).append("：").append(NumberUtil.getFormat(todayBadList.get(i).getNum(), todayBadNum)).append("%").append(" ").append(todayBadList.get(i).getName()).append("\n");
                    StringBuilder sb = new StringBuilder();
                    sb.append(NumberUtil.getFormat(todayBadList.get(i).getNum(), todayBadNum)).append("%").append(" ").append(todayBadList.get(i).getName());
                    bakcJson.put("top"+(i+1),sb.toString());
                }
                dm.setBadList(com.alibaba.fastjson.JSONObject.toJSONString(bakcJson));
            }
        }
        dayAppraiseMessageService.insert(dm);


    }

    private void pushMessageByFirstEdtion(Map<String, String> querryMap, ShopDetail shopDetail, WechatConfig wechatConfig, String brandName) {
        if (1 == shopDetail.getIsOpenSms() && null != shopDetail.getnoticeTelephone()) {
            //截取电话号码
            String telephones = shopDetail.getnoticeTelephone().replaceAll("，", ",");
            String[] tels = telephones.split(",");
            for (String s : tels) {
                String smsResult = SMSUtils.sendMessage(s, querryMap.get("sms"), "餐加", "SMS_46725122", null);//推送本日信息

                System.err.println("短信返回内容："+smsResult);
                //记录日志
                LogTemplateUtils.dayMessageSms(brandName, shopDetail.getName(), s, smsResult);
                Customer c = customerService.selectByTelePhone(s);
                /**
                 发送客服消息
                 */
                if (null != c) {
                    WeChatUtils.sendDayCustomerMsgASync(querryMap.get("wechat"), c.getWechatId(), wechatConfig.getAppid(), wechatConfig.getAppsecret(), s, brandName, shopDetail.getName());
                }
            }

        }
    }

    /**
     * 第一版日结短信 xun 结数据的封装
     * @param shopDetail
     * @return
     */
    private Map<String,String> querryXunDataByFirstEditon(ShopDetail shopDetail) {
        //----1.定义时间---
        Date xunBegin = DateUtil.getAfterDayDate(new Date(), -10);
        Date xunEnd = new Date();
        //三.定义线下订单
        //本旬线下订单总数(堂吃)
        int xunEnterCount = 0;
        //本旬线下订单总额(堂吃)
        BigDecimal xunEnterTotal = BigDecimal.ZERO;
        //4.外卖订单
        //本旬外卖订单数
        int xunDeliverOrders = 0;
        //本旬外卖订单总额
        BigDecimal xunOrderBooks = BigDecimal.ZERO;

        //查询pos端店铺录入信息(线下订单+外卖订单都是pos端录入的)
        List<OffLineOrder> offLineOrderList = offLineOrderMapper.selectlistByTimeSourceAndShopId(shopDetail.getId(), xunBegin, xunEnd, OfflineOrderSource.OFFLINE_POS);
        if (!offLineOrderList.isEmpty()) {
            for (OffLineOrder of : offLineOrderList) {
                xunEnterTotal = xunEnterTotal.add(of.getEnterTotal());//
                xunEnterCount += of.getEnterCount();
                xunDeliverOrders += of.getDeliveryOrders();
                xunOrderBooks = xunOrderBooks.add(of.getOrderBooks());
            }
        }
        //查询本旬新增用户的订单
        List<Order> newCustomerOrders = orderMapper.selectNewCustomerOrderByShopIdAndTime(shopDetail.getId(), xunBegin, xunEnd);
        //新增用户的订单总数
        int newCustomerOrderNum = 0;
        //新增用户的订单总额
        BigDecimal newCustomerOrderTotal = BigDecimal.ZERO;
        //新增分享用户的的订单总数
        int newShareCustomerOrderNum = 0;
        //新增分享用户的订单总额
        BigDecimal newShareCustomerOrderTotal = BigDecimal.ZERO;
        //新增自然用户的订单总数
        int newNormalCustomerOrderNum = 0;
        //新增自然用户的订单总额
        BigDecimal newNormalCustomerOrderTotal = BigDecimal.ZERO;
        if (!newCustomerOrders.isEmpty()) {
            for (Order o : newCustomerOrders) {
                newCustomerOrderNum++;
                newCustomerOrderTotal = newCustomerOrderTotal.add(getOrderMoney(o.getOrderMode(), o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
                if (o.getCustomer() != null && !StringUtils.isEmpty(o.getCustomer().getShareCustomer())) { //是分享用户
                    newShareCustomerOrderNum++;
                    newShareCustomerOrderTotal = newShareCustomerOrderTotal.add(getOrderMoney(o.getOrderMode(), o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
                } else {
                    newNormalCustomerOrderNum++; //是新增用户
                    newNormalCustomerOrderTotal = newNormalCustomerOrderTotal.add(getOrderMoney(o.getOrderMode(), o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
                }
            }
        }
        //查询回头用户的
        List<BackCustomerDto> backCustomerDtos = orderMapper.selectBackCustomerByShopIdAndTime(shopDetail.getId(), xunBegin, xunEnd);
        //回头用户
        Set<String> backCustomerId = new HashSet<>();
        //二次回头用户
        Set<String> backTwoCustomerId = new HashSet<>();
        //多次回头用户
        Set<String> backTwoMoreCustomerId = new HashSet<>();
        if (!backCustomerDtos.isEmpty()) {
            for (BackCustomerDto b : backCustomerDtos) {
                backCustomerId.add(b.getCustomerId());
                if (b.getNum() == 1) { //只要以前出现过一次那么就是二次回头用户 而非 ==2
                    backTwoCustomerId.add(b.getCustomerId());
                } else if (b.getNum() > 1) {
                    backTwoMoreCustomerId.add(b.getCustomerId());
                }
            }
        }
        //回头用户的订单总数
        int backCustomerOrderNum = 0;
        //二次回头用户的订单总数
        int backTwoCustomerOrderNum = 0;
        //多次回头用户的订单总数
        int backTwoMoreCustomerOderNum = 0;
        //回头用户的订单总额
        BigDecimal backCustomerOrderTotal = BigDecimal.ZERO;
        //二次回头用户的订单总额
        BigDecimal backTwoCustomerOrderTotal = BigDecimal.ZERO;
        //多次回头用户的订单总额
        BigDecimal backTwoMoreCustomerOrderTotal = BigDecimal.ZERO;
        List<Order> orders = orderMapper.selectCompleteByShopIdAndTime(shopDetail.getId(), xunBegin, xunEnd);
        if (!orders.isEmpty()) {
            for (Order o : orders) {
                if (backCustomerId.contains(o.getCustomerId())) {
                    backCustomerOrderNum++;
                    backCustomerOrderTotal = backCustomerOrderTotal.add(getOrderMoney(o.getOrderMode(), o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
                }
                if (backTwoCustomerId.contains(o.getCustomerId())) {
                    backTwoCustomerOrderNum++;
                    backTwoCustomerOrderTotal = backTwoCustomerOrderTotal.add(getOrderMoney(o.getOrderMode(), o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
                }
                if (backTwoMoreCustomerId.contains(o.getCustomerId())) {
                    backTwoMoreCustomerOrderTotal = backTwoMoreCustomerOrderTotal.add(getOrderMoney(o.getOrderMode(), o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
                    backTwoMoreCustomerOderNum++;
                }
            }
        }
        //2定义resto订单
        //本旬resto订单总数
//        Set<String> xunRestoCount = new HashSet<>();
        int xunRestoCount = newCustomerOrderNum + backCustomerOrderNum;

        //本旬resto订单总额
        BigDecimal xunRestoTotal = BigDecimal.ZERO;

        //定义折扣合计
        BigDecimal discountTotal = BigDecimal.ZERO;
        //红包
        BigDecimal redPackTotal = BigDecimal.ZERO;
        //优惠券
        BigDecimal couponTotal = BigDecimal.ZERO;
        //充值赠送
        BigDecimal chargeReturn = BigDecimal.ZERO;
        //折扣比率
        String discountRatio = "";
        //本旬用户消费比率
        String xunCustomerRatio = "";
        //回头用户消费比率
        String xunBackCustomerRatio = "";
        //新增用户比率
        String xunNewCustomerRatio = "";

        List<Order> xunOrders = orderMapper.selectListsmsByShopId(xunBegin, xunEnd, shopDetail.getId());
        if (!xunOrders.isEmpty()) {
            for (Order o : xunOrders) {
                //封装   1.resto订单总额     3.resto订单总数  4订单中的实收总额  5新增用户的订单总额  6自然到店的用户总额  7分享到店的用户总额
                //8回头用户的订单总额  9二次回头用户的订单总额  10多次回头用户的订单总额 11折扣合计 12红包 13优惠券 14 充值赠送 15折扣比率
                //本日 begin-----------------------
                /**
                 * 报表数据中的订单数  如果子订单和父订单算是一个订单
                 * 小程序+每日短信里的子订单和父订单算是两个订单
                 *
                 */
                //1.resto订单总额
                xunRestoTotal = xunRestoTotal.add(getOrderMoney(o.getOrderMode(), o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
                //11折扣合计 12红包 13优惠券 14 充值赠送 15折扣比率
                if (!o.getOrderPaymentItems().isEmpty()) {
                    //订单支付项
                    for (OrderPaymentItem oi : o.getOrderPaymentItems()) {
                        if (oi.getPaymentModeId() == PayMode.ACCOUNT_PAY) {
                            redPackTotal = redPackTotal.add(oi.getPayValue());
                        } else if (oi.getPaymentModeId() == PayMode.COUPON_PAY) {
                            couponTotal = couponTotal.add(oi.getPayValue());
                        } else if (oi.getPaymentModeId() == PayMode.REWARD_PAY) {
                            chargeReturn = chargeReturn.add(oi.getPayValue());
                        }
                    }
                }
                discountTotal = redPackTotal.add(couponTotal).add(chargeReturn);
                discountRatio = discountTotal.divide(xunRestoTotal.add(discountTotal), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).toString();
            }
        }

        //本旬用户消费比率 R+线下+外卖
        //到店总笔数 线上+线下
        double dmax = xunEnterCount + xunRestoCount;
        if (dmax != 0) {
            //本旬用户消费比率
            xunCustomerRatio = formatDouble((xunRestoCount / dmax) * 100);
            //本旬新增用户利率
            xunNewCustomerRatio = formatDouble((newCustomerOrderNum / dmax) * 100);
            //本日回头用户的消费比率
            xunBackCustomerRatio = formatDouble((backCustomerOrderNum / dmax) * 100);
        }

        //五星
        int fiveStar = 0;
        //四星
        int fourStar = 0;
        //3星-1星
        int oneToThreeStar = 0;
        //3定义满意度
        //本旬满意度
        String theTenDaySatisfaction = "";

        int xunAppraiseNum = 0;//本旬评价的总单数
        double xunAppraiseSum = 0;//本旬所有评价的总分数

        /**
         * 评价 和 满意度 错误的原因 用户可能今天 下单 但是隔天
         * 去评价 而现在 是查当天下单当天评价
         *
         *
         */

        //单独查询评价和分数
        List<Appraise> appraises = appraiseService.selectByTimeAndShopId(shopDetail.getId(), xunBegin, xunEnd);
        if (!appraises.isEmpty()) {
            for (Appraise a : appraises) {
                xunAppraiseNum++;
                xunAppraiseSum += a.getLevel() * 20;
                if (a.getLevel() == 5) {
                    fiveStar++;
                } else if (a.getLevel() == 4) {
                    fourStar++;
                } else {
                    oneToThreeStar++;
                }
            }
            if (xunAppraiseNum != 0) {
                theTenDaySatisfaction = formatDouble(xunAppraiseSum / xunAppraiseNum);
            }
        }

        BigDecimal xunChargeMoney = BigDecimal.ZERO;
        //查询充值
        List<ChargeOrder> chargeOrderList = chargeOrderService.selectByDateAndShopId(DateUtil.formatDate(xunBegin, "yyyy-MM-dd"), DateUtil.formatDate(xunEnd, "yyyy-MM-dd"), shopDetail.getId());
        if (!chargeOrderList.isEmpty()) {
            for (ChargeOrder c : chargeOrderList) {
                xunChargeMoney = xunChargeMoney.add(c.getChargeMoney());
            }
        }

        //查询菜品top10
        //1.查询好评的总数(旬内)
        int goodNum = 0;
        goodNum = articleTopService.selectSumGoodByTime(xunBegin, xunEnd, shopDetail.getId());
        //查询差评总数
        int badNum = 0;
        badNum = articleTopService.selectSumBadByTime(xunBegin, xunEnd, shopDetail.getId());

        //查询好评top10
        List<ArticleTopDto> goodList = articleTopService.selectListByTimeAndGoodType(xunBegin, xunEnd, shopDetail.getId());

        //查询差评top10
        List<ArticleTopDto> badList = articleTopService.selectListByTimeAndBadType(xunBegin, xunEnd, shopDetail.getId());

        //封装微信推送文本
        StringBuilder sb = new StringBuilder();
        sb
                .append("店铺名称:").append(shopDetail.getName()).append("\n")
                .append("时间:").append(DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss")).append("\n")
                .append("本旬总结").append("\n")
                .append("到店总笔数:").append(xunEnterCount + xunRestoCount).append("\n")
                .append("到店消费总额:").append(xunEnterTotal.add(xunRestoTotal)).append("\n")
                .append("---------------------").append("\n")
                .append("Resto+用户消费比数:").append(xunRestoCount).append("\n")
                .append("Resto+用户消费金额").append(xunRestoTotal).append("\n")
                .append("---------------------").append("\n")
                .append("Resto+用户消费比率:").append(xunCustomerRatio).append("%").append("\n")
                .append("Resto+回头消费比率:").append(xunBackCustomerRatio).append("%").append("\n")
                .append("Resto+新增用户比率:").append(xunNewCustomerRatio).append("%").append("\n")
                .append("---------------------").append("\n")
                .append("Resto+新用户消费:").append(newCustomerOrderNum).append("笔/").append(newCustomerOrderTotal).append("\n")
                .append("Resto+其中自然用户:").append(newNormalCustomerOrderNum).append("笔/").append(newNormalCustomerOrderTotal).append("\n")
                .append("Resto+其中分享用户:").append(newShareCustomerOrderNum).append("笔/").append(newShareCustomerOrderTotal).append("\n")
                .append("Resto+回头用户消费:").append(backCustomerOrderNum).append("笔/").append(backCustomerOrderTotal).append("\n")
                .append("Resto+二次回头用户:").append(backTwoCustomerOrderNum).append("笔/").append(backTwoCustomerOrderTotal).append("\n")
                .append("Resto+多次回头用户:").append(backTwoMoreCustomerOderNum).append("笔/").append(backTwoMoreCustomerOrderTotal).append("\n")
                .append("---------------------").append("\n")
                .append("折扣合计:").append(discountTotal).append("\n")
                .append("红包:").append(redPackTotal).append("\n")
                .append("优惠券:").append(couponTotal).append("\n")
                .append("充值赠送:").append(chargeReturn).append("\n")
                .append("折扣比率").append(discountRatio).append("\n")
                .append("---------------------").append("\n")
                .append("本旬五星评论:").append(fiveStar).append("\n")
                .append("本旬更改意见:").append(fourStar).append("\n")
                .append("本旬差评投诉:").append(oneToThreeStar).append("\n")
                .append("本旬满意度:").append(theTenDaySatisfaction).append("\n")
                .append("---------------------").append("\n")
                .append("本旬外卖金额:").append(xunOrderBooks).append("\n")
                .append("本旬实收:").append(xunEnterTotal.add(xunRestoTotal).add(xunOrderBooks)).append("\n")
                .append("本旬充值:").append(xunChargeMoney).append("\n")
                .append("---------------------").append("\n")
                .append("本旬红榜top10：").append("\n");

        //封装好评top10
        if (goodNum == 0) {//无好评
            sb.append("------无-----");
        } else {
            if (!goodList.isEmpty()) {//
                for (int i = 0; i < goodList.size(); i++) {
                    //1、27% 剁椒鱼头
                    sb.append(i + 1).append(".").append(NumberUtil.getFormat(goodList.get(i).getNum(), goodNum)).append("%").append(" ").append(goodList.get(i).getName()).append("\n");
                }
            }
        }


        sb.append("本旬黑榜top10：").append("\n");
        //封装差评top10
        if (badNum == 0) {//无差评
            sb.append("------无-----");
        } else {
            if (!badList.isEmpty()) {//
                for (int i = 0; i < badList.size(); i++) {
                    //1、27% 剁椒鱼头
                    sb.append(i + 1).append(".").append(NumberUtil.getFormat(badList.get(i).getNum(), badNum)).append("%").append(" ").append(badList.get(i).getName()).append("\n");
                }
            }
        }
        Map<String, String> map = new HashMap<>();
        map.put("wechat", sb.toString());
        return map;
    }

    /**
     * 第一版短信 月结数据封装
     * @param shopDetail
     * @param offLineOrder
     * @return
     */
    private Map<String,String> querryMonthDataByFirstEditon(ShopDetail shopDetail, OffLineOrder offLineOrder) {
        //----1.定义时间---
        Date monthBegin =DateUtil.fomatDate(DateUtil.getMonthBegin());
        Date monthEnd = new Date();
        //三.定义线下订单
        //本月线下订单总数(堂吃)
        int monthEnterCount = 0;
        //本月线下订单总额(堂吃)
        BigDecimal monthEnterTotal = BigDecimal.ZERO;
        //4.外卖订单
        //本月外卖订单数
        int monthDeliverOrders = 0;
        //本月外卖订单总额
        BigDecimal monthOrderBooks = BigDecimal.ZERO;

        //查询pos端店铺录入信息(线下订单+外卖订单都是pos端录入的)
        List<OffLineOrder> offLineOrderList = offLineOrderMapper.selectlistByTimeSourceAndShopId(shopDetail.getId(), monthBegin, monthEnd, OfflineOrderSource.OFFLINE_POS);
        if (!offLineOrderList.isEmpty()) {
            for (OffLineOrder of : offLineOrderList) {
                monthEnterTotal = monthEnterTotal.add(of.getEnterTotal());//
                monthEnterCount += of.getEnterCount();
                monthDeliverOrders += of.getDeliveryOrders();
                monthOrderBooks = monthOrderBooks.add(of.getOrderBooks());
            }
        }
        //查询本月新增用户的订单
        List<Order> newCustomerOrders = orderMapper.selectNewCustomerOrderByShopIdAndTime(shopDetail.getId(), monthBegin, monthEnd);
        //新增用户的订单总数
        int newCustomerOrderNum = 0;
        //新增用户的订单总额
        BigDecimal newCustomerOrderTotal = BigDecimal.ZERO;
        //新增分享用户的的订单总数
        int newShareCustomerOrderNum = 0;
        //新增分享用户的订单总额
        BigDecimal newShareCustomerOrderTotal = BigDecimal.ZERO;
        //新增自然用户的订单总数
        int newNormalCustomerOrderNum = 0;
        //新增自然用户的订单总额
        BigDecimal newNormalCustomerOrderTotal = BigDecimal.ZERO;
        if (!newCustomerOrders.isEmpty()) {
            for (Order o : newCustomerOrders) {
                newCustomerOrderNum++;
                newCustomerOrderTotal = newCustomerOrderTotal.add(getOrderMoney(o.getOrderMode(), o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
                if (o.getCustomer() != null && !StringUtils.isEmpty(o.getCustomer().getShareCustomer())) { //是分享用户
                    newShareCustomerOrderNum++;
                    newShareCustomerOrderTotal = newShareCustomerOrderTotal.add(getOrderMoney(o.getOrderMode(), o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
                } else {
                    newNormalCustomerOrderNum++; //是新增用户
                    newNormalCustomerOrderTotal = newNormalCustomerOrderTotal.add(getOrderMoney(o.getOrderMode(), o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
                }
            }
        }
        //查询回头用户的
        List<BackCustomerDto> backCustomerDtos = orderMapper.selectBackCustomerByShopIdAndTime(shopDetail.getId(), monthBegin, monthEnd);
        //回头用户
        Set<String> backCustomerId = new HashSet<>();
        //二次回头用户
        Set<String> backTwoCustomerId = new HashSet<>();
        //多次回头用户
        Set<String> backTwoMoreCustomerId = new HashSet<>();
        if (!backCustomerDtos.isEmpty()) {
            for (BackCustomerDto b : backCustomerDtos) {
                backCustomerId.add(b.getCustomerId());
                if (b.getNum() == 1) { //只要以前出现过一次那么就是二次回头用户 而非 ==2
                    backTwoCustomerId.add(b.getCustomerId());
                } else if (b.getNum() > 1) {
                    backTwoMoreCustomerId.add(b.getCustomerId());
                }
            }
        }
        //回头用户的订单总数
        int backCustomerOrderNum = 0;
        //二次回头用户的订单总数
        int backTwoCustomerOrderNum = 0;
        //多次回头用户的订单总数
        int backTwoMoreCustomerOderNum = 0;
        //回头用户的订单总额
        BigDecimal backCustomerOrderTotal = BigDecimal.ZERO;
        //二次回头用户的订单总额
        BigDecimal backTwoCustomerOrderTotal = BigDecimal.ZERO;
        //多次回头用户的订单总额
        BigDecimal backTwoMoreCustomerOrderTotal = BigDecimal.ZERO;
        List<Order> orders = orderMapper.selectCompleteByShopIdAndTime(shopDetail.getId(), monthBegin, monthEnd);
        if (!orders.isEmpty()) {
            for (Order o : orders) {
                if (backCustomerId.contains(o.getCustomerId())) {
                    backCustomerOrderNum++;
                    backCustomerOrderTotal = backCustomerOrderTotal.add(getOrderMoney(o.getOrderMode(), o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
                }
                if (backTwoCustomerId.contains(o.getCustomerId())) {
                    backTwoCustomerOrderNum++;
                    backTwoCustomerOrderTotal = backTwoCustomerOrderTotal.add(getOrderMoney(o.getOrderMode(), o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
                }
                if (backTwoMoreCustomerId.contains(o.getCustomerId())) {
                    backTwoMoreCustomerOrderTotal = backTwoMoreCustomerOrderTotal.add(getOrderMoney(o.getOrderMode(), o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
                    backTwoMoreCustomerOderNum++;
                }
            }
        }
        //2定义resto订单
        //本月resto订单总数
//        Set<String> MonthRestoCount = new HashSet<>();
        int monthRestoCount = newCustomerOrderNum + backCustomerOrderNum;

        //本月resto订单总额
        BigDecimal monthRestoTotal = BigDecimal.ZERO;

        //定义折扣合计
        BigDecimal discountTotal = BigDecimal.ZERO;
        //红包
        BigDecimal redPackTotal = BigDecimal.ZERO;
        //优惠券
        BigDecimal couponTotal = BigDecimal.ZERO;
        //充值赠送
        BigDecimal chargeReturn = BigDecimal.ZERO;
        //折扣比率
        String discountRatio = "";
        //本月用户消费比率
        String monthCustomerRatio = "";
        //回头用户消费比率
        String monthBackCustomerRatio = "";
        //新增用户比率
        String monthNewCustomerRatio = "";

        List<Order> monthOrders = orderMapper.selectListsmsByShopId(monthBegin, monthEnd, shopDetail.getId());
        if (!monthOrders.isEmpty()) {
            for (Order o : monthOrders) {
                //封装   1.resto订单总额     3.resto订单总数  4订单中的实收总额  5新增用户的订单总额  6自然到店的用户总额  7分享到店的用户总额
                //8回头用户的订单总额  9二次回头用户的订单总额  10多次回头用户的订单总额 11折扣合计 12红包 13优惠券 14 充值赠送 15折扣比率
                //本日 begin-----------------------
                /**
                 * 报表数据中的订单数  如果子订单和父订单算是一个订单
                 * 小程序+每日短信里的子订单和父订单算是两个订单
                 *
                 */
                //1.resto订单总额
                monthRestoTotal = monthRestoTotal.add(getOrderMoney(o.getOrderMode(), o.getPayType(), o.getOrderMoney(), o.getAmountWithChildren()));
                //11折扣合计 12红包 13优惠券 14 充值赠送 15折扣比率
                if (!o.getOrderPaymentItems().isEmpty()) {
                    //订单支付项
                    for (OrderPaymentItem oi : o.getOrderPaymentItems()) {
                        if (oi.getPaymentModeId() == PayMode.ACCOUNT_PAY) {
                            redPackTotal = redPackTotal.add(oi.getPayValue());
                        } else if (oi.getPaymentModeId() == PayMode.COUPON_PAY) {
                            couponTotal = couponTotal.add(oi.getPayValue());
                        } else if (oi.getPaymentModeId() == PayMode.REWARD_PAY) {
                            chargeReturn = chargeReturn.add(oi.getPayValue());
                        }
                    }
                }
                discountTotal = redPackTotal.add(couponTotal).add(chargeReturn);
                discountRatio = discountTotal.divide(monthRestoTotal.add(discountTotal), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).toString();
            }
        }

        //本月用户消费比率 R+线下+外卖
        //到店总笔数 线上+线下
        double dmax = monthEnterCount + monthRestoCount;
        if (dmax != 0) {
            //本月用户消费比率
            monthCustomerRatio = formatDouble((monthRestoCount / dmax) * 100);
            //本月新增用户利率
            monthNewCustomerRatio = formatDouble((newCustomerOrderNum / dmax) * 100);
            //本月回头用户的消费比率
            monthBackCustomerRatio = formatDouble((backCustomerOrderNum / dmax) * 100);
        }

        //五星
        int fiveStar = 0;
        //四星
        int fourStar = 0;
        //3星-1星
        int oneToThreeStar = 0;
        //3定义满意度
        //本月满意度
        String monthSatisfaction = "";

        int monthAppraiseNum = 0;//本月评价的总单数
        double monthAppraiseSum = 0;//本月所有评价的总分数

        /**
         * 评价 和 满意度 错误的原因 用户可能今天 下单 但是隔天
         * 去评价 而现在 是查当天下单当天评价
         *
         *
         */

        //单独查询评价和分数
        List<Appraise> appraises = appraiseService.selectByTimeAndShopId(shopDetail.getId(), monthBegin, monthEnd);
        if (!appraises.isEmpty()) {
            for (Appraise a : appraises) {
                monthAppraiseNum++;
                monthAppraiseSum += a.getLevel() * 20;
                if (a.getLevel() == 5) {
                    fiveStar++;
                } else if (a.getLevel() == 4) {
                    fourStar++;
                } else {
                    oneToThreeStar++;
                }
            }
            if (monthAppraiseNum != 0) {
                monthSatisfaction = formatDouble(monthAppraiseSum / monthAppraiseNum);
            }
        }

        BigDecimal monthChargeMoney = BigDecimal.ZERO;
        //查询充值
        List<ChargeOrder> chargeOrderList = chargeOrderService.selectByDateAndShopId(DateUtil.formatDate(monthBegin, "yyyy-MM-dd"), DateUtil.formatDate(monthEnd, "yyyy-MM-dd"), shopDetail.getId());
        if (!chargeOrderList.isEmpty()) {
            for (ChargeOrder c : chargeOrderList) {
                monthChargeMoney = monthChargeMoney.add(c.getChargeMoney());
            }
        }

        //查询菜品top10
        //1.查询好评的总数(月内)
        int goodNum = 0;
        goodNum = articleTopService.selectSumGoodByTime(monthBegin, monthEnd, shopDetail.getId());
        //查询差评总数
        int badNum = 0;
        badNum = articleTopService.selectSumBadByTime(monthBegin, monthEnd, shopDetail.getId());

        //查询好评top10
        List<ArticleTopDto> goodList = articleTopService.selectListByTimeAndGoodType(monthBegin, monthEnd, shopDetail.getId());

        //查询差评top10
        List<ArticleTopDto> badList = articleTopService.selectListByTimeAndBadType(monthBegin, monthEnd, shopDetail.getId());

        //封装微信推送文本
        StringBuilder sb = new StringBuilder();
        sb
                .append("店铺名称:").append(shopDetail.getName()).append("\n")
                .append("时间:").append(DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss")).append("\n")
                .append("本月总结").append("\n")
                .append("到店总笔数:").append(monthEnterCount + monthRestoCount).append("\n")
                .append("到店消费总额:").append(monthEnterTotal.add(monthRestoTotal)).append("\n")
                .append("---------------------").append("\n")
                .append("Resto+用户消费比数:").append(monthRestoCount).append("\n")
                .append("Resto+用户消费金额").append(monthRestoTotal).append("\n")
                .append("---------------------").append("\n")
                .append("Resto+用户消费比率:").append(monthCustomerRatio).append("%").append("\n")
                .append("Resto+回头消费比率:").append(monthBackCustomerRatio).append("%").append("\n")
                .append("Resto+新增用户比率:").append(monthNewCustomerRatio).append("%").append("\n")
                .append("---------------------").append("\n")
                .append("Resto+新用户消费:").append(newCustomerOrderNum).append("笔/").append(newCustomerOrderTotal).append("\n")
                .append("Resto+其中自然用户:").append(newNormalCustomerOrderNum).append("笔/").append(newNormalCustomerOrderTotal).append("\n")
                .append("Resto+其中分享用户:").append(newShareCustomerOrderNum).append("笔/").append(newShareCustomerOrderTotal).append("\n")
                .append("Resto+回头用户消费:").append(backCustomerOrderNum).append("笔/").append(backCustomerOrderTotal).append("\n")
                .append("Resto+二次回头用户:").append(backTwoCustomerOrderNum).append("笔/").append(backTwoCustomerOrderTotal).append("\n")
                .append("Resto+多次回头用户:").append(backTwoMoreCustomerOderNum).append("笔/").append(backTwoMoreCustomerOrderTotal).append("\n")
                .append("---------------------").append("\n")
                .append("折扣合计:").append(discountTotal).append("\n")
                .append("红包:").append(redPackTotal).append("\n")
                .append("优惠券:").append(couponTotal).append("\n")
                .append("充值赠送:").append(chargeReturn).append("\n")
                .append("折扣比率").append(discountRatio).append("\n")
                .append("---------------------").append("\n")
                .append("本月五星评论:").append(fiveStar).append("\n")
                .append("本月更改意见:").append(fourStar).append("\n")
                .append("本月差评投诉:").append(oneToThreeStar).append("\n")
                .append("本月满意度:").append(monthSatisfaction).append("\n")
                .append("---------------------").append("\n")
                .append("本月外卖金额:").append(monthOrderBooks).append("\n")
                .append("本月实收:").append(monthEnterTotal.add(monthRestoTotal).add(monthOrderBooks)).append("\n")
                .append("本月充值:").append(monthChargeMoney).append("\n")
                .append("---------------------").append("\n")
                .append("本月红榜top10：").append("\n");

                //封装好评top10
                if (goodNum == 0) {//无好评
                    sb.append("------无-----");
                } else {
                    if (!goodList.isEmpty()) {//
                        for (int i = 0; i < goodList.size(); i++) {
                            //1、27% 剁椒鱼头
                            sb.append(i + 1).append(".").append(NumberUtil.getFormat(goodList.get(i).getNum(), goodNum)).append("%").append(" ").append(goodList.get(i).getName()).append("\n");
                        }
                    }
                }

            sb.append("本月黑榜top10：").append("\n");
            //封装差评top10
            if (badNum == 0) {//无差评
                sb.append("------无-----");
            } else {
                if (!badList.isEmpty()) {//
                    for (int i = 0; i < badList.size(); i++) {
                        //1、27% 剁椒鱼头
                        sb.append(i + 1).append(".").append(NumberUtil.getFormat(badList.get(i).getNum(), badNum)).append("%").append(" ").append(badList.get(i).getName()).append("\n");
                    }
                }
            }
            Map<String, String> map = new HashMap<>();
            map.put("wechat", sb.toString());
            return map;
        }


    private void refundShopDetailOrder(ShopDetail shopDetail) {
        String[] orderStates = new String[]{OrderState.SUBMIT + "", OrderState.PAYMENT + ""};//未付款和未全部付款和已付款
        String[] productionStates = new String[]{ProductionStatus.NOT_ORDER + ""};//已付款未下单
        List<Order> orderList = orderMapper.selectByOrderSatesAndProductionStates(shopDetail.getId(), orderStates, productionStates);
        for (Order order : orderList) {
            if (!order.getClosed()) {//判断订单是否已被关闭，只对未被关闭的订单做退单处理
                sendWxRefundMsg(order);
            }
        }
        // 查询已付款且有支付项但是生产状态没有改变的订单
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

    }



    public void sendWxRefundMsg(Order order) {
        if (checkRefundLimit(order)) {
            autoRefundOrder(order.getId());
            log.info("款项自动退还到相应账户:" + order.getId());
            Customer customer = customerService.selectById(order.getCustomerId());
            WechatConfig config = wechatConfigService.selectByBrandId(order.getBrandId());
            Brand brand = brandService.selectById(customer.getBrandId());
            ShopDetail shopDetail = shopDetailService.selectById(order.getShopDetailId());
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
//            UserActionUtils.writeToFtp(LogType.ORDER_LOG, brand.getBrandName(), shopDetail.getName(), order.getId(),
//                    "订单发送推送：" + sb.toString());
            Map map = new HashMap(4);
            map.put("brandName", brand.getBrandName());
            map.put("fileName", customer.getId());
            map.put("type", "UserAction");
            map.put("content", "系统向用户:" + customer.getNickname() + "推送微信消息:" + sb.toString() + ",请求服务器地址为:" + MQSetting.getLocalIP());
            doPostAnsc(LogUtils.url, map);
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

        log.info("服务员点餐时修改订单production：" + ProductionStatus.HAS_ORDER);
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
    public Order getLastOrderByTableNumber(String tableNumber, String shopId) {
        return orderMapper.getLastOrderByTableNumber(tableNumber, shopId);
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
        Order parent = orderMapper.selectByPrimaryKey(orderId);
        List<Order> childs = orderMapper.selectByParentId(orderId, parent.getPayType());
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
        Integer oldDistributionModeId = order.getDistributionModeId();
        StringBuffer pushMessage = new StringBuffer();
        BigDecimal updateCount = new BigDecimal(0);
        List<Map<String, Object>> printTask = new ArrayList<>();
        Brand brand = brandService.selectById(order.getBrandId());
        ShopDetail shopDetail = shopDetailService.selectByPrimaryKey(order.getShopDetailId());
//        UserActionUtils.writeToFtp(LogType.POS_LOG, brand.getBrandName(), shopDetail.getName(),
//                "接收到修改菜品的请求,订单号为 " + order.getId());
        Map map = new HashMap(4);
        map.put("brandName", brand.getBrandName());
        map.put("fileName", shopDetail.getName());
        map.put("type", "posAction");
        map.put("content", "接收到修改菜品的请求,订单号为 " + order.getId() + ",请求服务器地址为:" + MQSetting.getLocalIP());
        doPostAnsc(url, map);
        BrandSetting setting = brandSettingService.selectByBrandId(order.getBrandId());
        if (type == 0) { //如果要修改的是服务费
            BigDecimal baseCustomerCount = new BigDecimal(order.getCustomerCount());
            order.setCustomerCount(count);
            order.setBaseCustomerCount(count);
            order.setPaymentAmount(order.getPaymentAmount().subtract(order.getServicePrice()));
            if (order.getAmountWithChildren().doubleValue() > 0) {
                order.setAmountWithChildren(order.getAmountWithChildren().subtract(order.getServicePrice()));
            }
            order.setOrderMoney(order.getOrderMoney().subtract(order.getServicePrice()));
            order.setOriginalAmount(order.getOriginalAmount().subtract(order.getServicePrice()));
            order.setServicePrice(shopDetail.getServicePrice().multiply(new BigDecimal(count)));
            order.setPaymentAmount(order.getPaymentAmount().add(order.getServicePrice()));
            order.setOrderMoney(order.getOrderMoney().add(order.getServicePrice()));
            if (order.getAmountWithChildren().doubleValue() > 0) {
                order.setAmountWithChildren(order.getAmountWithChildren().add(order.getServicePrice()));
            }
            order.setOriginalAmount(order.getOriginalAmount().add(order.getServicePrice()));

            update(order);
            updateCount = baseCustomerCount.subtract(new BigDecimal(count));
            String message = "";
            Map<String, Object> orderItemMap = new HashMap<>();
            String ARTICLE_NAME = shopDetail.getServiceName();
            BigDecimal ARTICLE_COUNT = updateCount;
            BigDecimal SUBTOTAL = updateCount.multiply(shopDetail.getServicePrice());
            if (updateCount.compareTo(BigDecimal.ZERO) > 0) {
                message = "减" + updateCount + "份";
                order.setDistributionModeId(DistributionType.REFUND_ORDER);
                ARTICLE_NAME = ARTICLE_NAME.concat("(退)");
                ARTICLE_COUNT = updateCount.multiply(new BigDecimal(-1));
                SUBTOTAL = ARTICLE_COUNT.multiply(shopDetail.getServicePrice());
            } else if (updateCount.compareTo(BigDecimal.ZERO) == 0) {
                message = "减" + count + "份";
                order.setDistributionModeId(DistributionType.REFUND_ORDER);
                ARTICLE_NAME = ARTICLE_NAME.concat("(退)");
                ARTICLE_COUNT = new BigDecimal(count).multiply(new BigDecimal(-1));
                SUBTOTAL = ARTICLE_COUNT.multiply(shopDetail.getServicePrice());
            } else {
                message = "加" + updateCount.abs() + "份";
                order.setDistributionModeId(DistributionType.MODIFY_ORDER);
                ARTICLE_NAME = ARTICLE_NAME.concat("(加)");
                ARTICLE_COUNT = updateCount.abs();
                SUBTOTAL = ARTICLE_COUNT.multiply(shopDetail.getServicePrice());
            }
            orderItemMap.put("SUBTOTAL", SUBTOTAL);
            orderItemMap.put("ARTICLE_NAME", ARTICLE_NAME);
            orderItemMap.put("ARTICLE_COUNT", ARTICLE_COUNT);
            if (shopDetail.getModifyOrderPrintReceipt().equals(Common.YES)) {
                List<Printer> printer = printerService.selectByShopAndType(shopDetail.getId(), PrinterType.RECEPTION);
                for (Printer p : printer) {
                    Map<String, Object> ticket = modifyOrderPrintReceipt(order, orderItemMap, p, shopDetail);
                    if (ticket != null) {
                        printTask.add(ticket);
                    }
                }
            }
            pushMessage.append(shopDetail.getServiceName() + "  " + message);
//            UserActionUtils.writeToFtp(LogType.POS_LOG, brand.getBrandName(), shopDetail.getName(),
//                    shopDetail.getName() + "修改了" + shopDetail.getServiceName() + "，数量修改为" + count + ",订单号为:" + order.getId());
            map.put("content", shopDetail.getName() + "修改了" + shopDetail.getServiceName() + "，数量修改为" + count + ",订单号为:" + order.getId()
                    + ",请求服务器地址为:" + MQSetting.getLocalIP());
            doPostAnsc(url, map);
        } else { //修改的是菜品


            OrderItem orderItem = orderItemService.selectById(orderItemId); //找到要修改的菜品
            if (count > orderItem.getCount()) {
                result.setSuccess(false);
                result.setMessage("餐品修改数量有误");
                return result;
            }
            BigDecimal baseArticleCount = new BigDecimal(orderItem.getCount());
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
            order.setOriginalAmount(order.getOriginalAmount().subtract(new BigDecimal(orderItem.getCount()).multiply(orderItem.getOriginalPrice())));
            order.setPaymentAmount(order.getPaymentAmount().subtract(orderItem.getFinalPrice()));
            if (order.getAmountWithChildren().doubleValue() > 0) {
                order.setAmountWithChildren(order.getAmountWithChildren().subtract(orderItem.getFinalPrice()));
            }

            orderItem.setCount(count);
            orderItem.setChangeCount(count);
            orderItem.setFinalPrice(orderItem.getUnitPrice().multiply(new BigDecimal(count)));
            orderitemMapper.updateByPrimaryKeySelective(orderItem);
            List<OrderItem> list = orderitemMapper.getListBySort(orderItem.getId(), orderItem.getArticleId());
            for (OrderItem zpOrderItem : list) {
                zpOrderItem.setCount(count);
                zpOrderItem.setFinalPrice(zpOrderItem.getUnitPrice().multiply(new BigDecimal(count)));
                orderitemMapper.updateByPrimaryKeySelective(zpOrderItem);
            }
            order.setArticleCount(order.getArticleCount() + orderItem.getCount());
            order.setOrderMoney(order.getOrderMoney().add(orderItem.getFinalPrice()));
            order.setOriginalAmount(order.getOriginalAmount().add(new BigDecimal(orderItem.getCount()).multiply(orderItem.getOriginalPrice())));
            order.setPaymentAmount(order.getPaymentAmount().add(orderItem.getFinalPrice()));
            if (order.getAmountWithChildren().doubleValue() > 0) {
                order.setAmountWithChildren(order.getAmountWithChildren().add(orderItem.getFinalPrice()));
            }

            if (orderItem.getCount() == 0) {
                orderitemMapper.deleteByPrimaryKey(orderItem.getId());
                for (OrderItem zpOrderItem : list) {
                    orderitemMapper.deleteByPrimaryKey(zpOrderItem.getId());
                }
            }

            update(order);
            updateCount = baseArticleCount.subtract(new BigDecimal(count));
            String message = "";
            Map<String, Object> orderItemMap = new HashMap<>();
            String ARTICLE_NAME = orderItem.getArticleName();
            BigDecimal ARTICLE_COUNT = updateCount;
            BigDecimal SUBTOTAL = updateCount.multiply(orderItem.getUnitPrice());
            if (updateCount.compareTo(BigDecimal.ZERO) > 0) {
                message = "减" + updateCount + "份";
                order.setDistributionModeId(DistributionType.REFUND_ORDER);
                ARTICLE_NAME = ARTICLE_NAME.concat("(退)");
                ARTICLE_COUNT = updateCount.multiply(new BigDecimal(-1));
                SUBTOTAL = ARTICLE_COUNT.multiply(orderItem.getUnitPrice());
            } else if (updateCount.compareTo(BigDecimal.ZERO) == 0) {
                message = "减" + count + "份";
                order.setDistributionModeId(DistributionType.REFUND_ORDER);
                ARTICLE_NAME = ARTICLE_NAME.concat("(退)");
                ARTICLE_COUNT = new BigDecimal(count).multiply(new BigDecimal(-1));
                SUBTOTAL = ARTICLE_COUNT.multiply(orderItem.getUnitPrice());
            } else {
                message = "加" + updateCount.abs() + "份";
                order.setDistributionModeId(DistributionType.MODIFY_ORDER);
                ARTICLE_NAME = ARTICLE_NAME.concat("(加)");
                ARTICLE_COUNT = updateCount.abs();
                SUBTOTAL = ARTICLE_COUNT.multiply(orderItem.getUnitPrice());
            }
            orderItemMap.put("SUBTOTAL", SUBTOTAL);
            orderItemMap.put("ARTICLE_NAME", ARTICLE_NAME);
            orderItemMap.put("ARTICLE_COUNT", ARTICLE_COUNT);
            if (shopDetail.getModifyOrderPrintReceipt().equals(Common.YES)) {
                List<Printer> printer = printerService.selectByShopAndType(shopDetail.getId(), PrinterType.RECEPTION);
                for (Printer p : printer) {
                    Map<String, Object> ticket = modifyOrderPrintReceipt(order, orderItemMap, p, shopDetail);
                    if (ticket != null) {
                        printTask.add(ticket);
                    }
                }
            }
            if (shopDetail.getModifyOrderPrintKitchen().equals(Common.YES)) {
                List<OrderItem> orderItemList = new ArrayList<>();
                orderItem.setCount(ARTICLE_COUNT.abs().intValue());
                for (OrderItem zpOrderItem : list) {
                    zpOrderItem.setCount(ARTICLE_COUNT.abs().intValue());
                }
                orderItem.setChildren(list);
                orderItemList.add(orderItem);
                printTask.addAll(printKitchen(order, orderItemList));
            }
            pushMessage.append(orderItem.getArticleName() + "  " + message);
//            UserActionUtils.writeToFtp(LogType.POS_LOG, brand.getBrandName(), shopDetail.getName(),
//                    shopDetail.getName() + "修改了菜品" + orderItem.getArticleName() + "，数量修改为" + count + ",订单号为:" + order.getId());
            map.put("content", shopDetail.getName() + "修改了菜品" + orderItem.getArticleName() + "，数量修改为" + count + ",订单号为:" + order.getId()
                    + ",请求服务器地址为:" + MQSetting.getLocalIP());
            doPostAnsc(url, map);
        }

        if (order.getOrderMode() == ShopMode.HOUFU_ORDER || order.getOrderMode() == ShopMode.BOSS_ORDER) {
            if (order.getParentOrderId() != null) {  //子订单
                Order parent = selectById(order.getParentOrderId());
                int articleCountWithChildren = 0;


                if (parent.getLastOrderTime() == null || parent.getLastOrderTime().getTime() < order.getCreateTime().getTime()) {
                    parent.setLastOrderTime(order.getCreateTime());
                }
                Double amountWithChildren = 0.0;
                if (order.getOrderMode() == ShopMode.HOUFU_ORDER) {
                    articleCountWithChildren = selectArticleCountById(parent.getId(), order.getOrderMode());
                    amountWithChildren = orderMapper.selectParentAmount(parent.getId(), parent.getOrderMode());
                } else {
                    articleCountWithChildren = orderMapper.selectArticleCountByIdBossOrder(parent.getId());
                    amountWithChildren = orderMapper.selectParentAmountByBossOrder(parent.getId());
                }

                parent.setCountWithChild(articleCountWithChildren);
                parent.setAmountWithChildren(new BigDecimal(amountWithChildren));
                update(parent);

            }
        }
        result.setSuccess(true);
        result.setMessage(printTask.size() > 0 ? JSON.toJSONString(printTask) : null);
        Customer customer = customerService.selectById(order.getCustomerId());
        WechatConfig config = wechatConfigService.selectByBrandId(customer.getBrandId());

        Order parent = null;
        if (order.getParentOrderId() != null) {
            parent = orderMapper.selectByPrimaryKey(order.getParentOrderId());
        } else {
            parent = order;
        }

        StringBuffer msg = new StringBuffer();
        msg.append("商家已在收银电脑处更新了您的订单信息：" + "\n");
        msg.append(pushMessage.toString());
        WeChatUtils.sendCustomerMsg(msg.toString(), customer.getWechatId(), config.getAppid(), config.getAppsecret());
//        UserActionUtils.writeToFtp(LogType.ORDER_LOG, brand.getBrandName(), shopDetail.getName(), order.getId(),
//                "订单发送推送：" + msg.toString());
        Map customerMap = new HashMap(4);
        customerMap.put("brandName", brand.getBrandName());
        customerMap.put("fileName", customer.getId());
        customerMap.put("type", "UserAction");
        customerMap.put("content", "系统向用户:" + customer.getNickname() + "推送微信消息:" + msg.toString() + ",请求服务器地址为:" + MQSetting.getLocalIP());
        doPostAnsc(LogUtils.url, customerMap);
        Order newOrder = new Order();
        newOrder.setId(order.getId());
        newOrder.setDistributionModeId(oldDistributionModeId);
        orderMapper.updateByPrimaryKeySelective(newOrder);
        return result;
    }

    public Map<String, Object> modifyOrderPrintReceipt(Order order, Map<String, Object> orderItem, Printer printer, ShopDetail shopDetail) {
        if (printer == null) {
            return null;
        }
        List<Map<String, Object>> orderItems = new ArrayList<>();
        orderItems.add(orderItem);
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
        data.put("ORDER_NUMBER", (String) RedisUtil.get(order.getId() + "orderNumber"));
        data.put("ITEMS", orderItems);
        List<Map<String, Object>> patMentItems = new ArrayList<Map<String, Object>>();
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
        data.put("ORIGINAL_AMOUNT", orderItem.get("SUBTOTAL"));
        data.put("RESTAURANT_ADDRESS", shopDetail.getAddress());
        data.put("REDUCTION_AMOUNT", 0);
        data.put("RESTAURANT_TEL", shopDetail.getPhone());
        data.put("TABLE_NUMBER", order.getTableNumber());
        data.put("CUSTOMER_COUNT", order.getCustomerCount() == null ? "-" : order.getCustomerCount());
        data.put("PAYMENT_AMOUNT", new BigDecimal(orderItem.get("SUBTOTAL").toString()).compareTo(BigDecimal.ZERO) > 0 ? orderItem.get("SUBTOTAL") : 0);
        data.put("RESTAURANT_NAME", shopDetail.getName());
        data.put("DATETIME", DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
        data.put("ARTICLE_COUNT", orderItem.get("ARTICLE_COUNT"));
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
    public void refundArticle(Order order) {
        List<OrderPaymentItem> payItemsList = orderPaymentItemService.selectByOrderId(order.getId());
        //退款完成后变更订单项
        Order o = getOrderInfo(order.getId());
        Brand brand = brandService.selectById(o.getBrandId());
        ShopDetail shopDetail = shopDetailService.selectByPrimaryKey(o.getShopDetailId());
        Customer customer = customerService.selectById(o.getCustomerId());
        int refundMoney = order.getRefundMoney().multiply(new BigDecimal(100)).intValue();

        //如果退菜订单是  后付情况下加菜后统一支付  则支付项是在主订单下    修改退菜金额改变的逻辑
        if (o.getParentOrderId() != null && o.getPayType() == PayType.NOPAY) {
            payItemsList = orderPaymentItemService.selectByOrderId(o.getParentOrderId());
        }

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
                            if (result.containsKey("ERROR")) {
                                throw new RuntimeException("微信退款失败！失败信息：" + new JSONObject(result).toString());
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
//                                UserActionUtils.writeToFtp(LogType.ORDER_LOG, brand.getBrandName(), shopDetail.getName(), o.getId(),
//                                        "总退款金额" + order.getRefundMoney() + ",微信支付返回" + wxBack + ",余额返回" + backMoney);
                                accountService.addAccount(backMoney, customer.getAccountId(), "退菜红包", AccountLog.REFUND_ARTICLE_RED_PACKAGE, order.getShopDetailId());
                                RedPacket redPacket = new RedPacket();
                                redPacket.setId(ApplicationUtils.randomUUID());
                                redPacket.setRedMoney(backMoney);
                                redPacket.setCreateTime(new Date());
                                redPacket.setCustomerId(customer.getId());
                                redPacket.setBrandId(order.getBrandId());
                                redPacket.setShopDetailId(order.getShopDetailId());
                                redPacket.setRedRemainderMoney(backMoney);
                                redPacket.setRedType(RedType.REFUND_ARTICLE_RED);
                                redPacketService.insert(redPacket);
                                Map map = new HashMap(4);
                                map.put("brandName", brand.getBrandName());
                                map.put("fileName", shopDetail.getName());
                                map.put("type", "posAction");
                                map.put("content", "订单:" + order.getId() + "在pos端执行退菜返还红包" + backMoney + "元,返还用户Id:" + customer.getId() + ",请求服务器地址为:" + MQSetting.getLocalIP());
                                doPostAnsc(url, map);
                                Map orderMap = new HashMap(4);
                                orderMap.put("brandName", brand.getBrandName());
                                orderMap.put("fileName", order.getId());
                                orderMap.put("type", "orderAction");
                                orderMap.put("content", "订单:" + order.getId() + "在pos端执行退菜返还红包" + backMoney + "元,返还用户Id:" + customer.getId() + ",请求服务器地址为:" + MQSetting.getLocalIP());
                                doPostAnsc(url, orderMap);
                            }
                            Map map = new HashMap(4);
                            map.put("brandName", brand.getBrandName());
                            map.put("fileName", shopDetail.getName());
                            map.put("type", "posAction");
                            map.put("content", "订单:" + order.getId() + "在pos端执行退菜微信返还" + (maxWxPay > refundMoney ? refundMoney : maxWxPay) + "元回调返回信息:" + result.toString() + "" +
                                    ",返还用户Id:" + customer.getId() + ",请求服务器地址为:" + MQSetting.getLocalIP());
                            doPostAnsc(url, map);
                            Map orderMap = new HashMap(4);
                            orderMap.put("brandName", brand.getBrandName());
                            orderMap.put("fileName", order.getId());
                            orderMap.put("type", "orderAction");
                            orderMap.put("content", "订单:" + order.getId() + "在pos端执行退菜微信返还" + (maxWxPay > refundMoney ? refundMoney : maxWxPay) + "元回调返回信息:" + result.toString() + "" +
                                    ",返还用户Id:" + customer.getId() + ",请求服务器地址为:" + MQSetting.getLocalIP());
                            doPostAnsc(url, orderMap);
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
                            if (resultJson.indexOf("ERROR") != -1) {
                                throw new RuntimeException("支付宝退款失败！失败信息：" + resultJson);
                            }
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
//                                UserActionUtils.writeToFtp(LogType.ORDER_LOG, brand.getBrandName(), shopDetail.getName(), o.getId(),
//                                        "总退款金额" + order.getRefundMoney() + ",支付宝支付返回" + refundTotal + ",余额返回" + backMoney);
                                accountService.addAccount(backMoney, customer.getAccountId(), "退菜红包", AccountLog.REFUND_ARTICLE_RED_PACKAGE, order.getShopDetailId());
                                RedPacket redPacket = new RedPacket();
                                redPacket.setId(ApplicationUtils.randomUUID());
                                redPacket.setRedMoney(backMoney);
                                redPacket.setCreateTime(new Date());
                                redPacket.setCustomerId(customer.getId());
                                redPacket.setBrandId(order.getBrandId());
                                redPacket.setShopDetailId(order.getShopDetailId());
                                redPacket.setRedRemainderMoney(backMoney);
                                redPacket.setRedType(RedType.REFUND_ARTICLE_RED);
                                redPacketService.insert(redPacket);
                                Map accountMap = new HashMap(4);
                                accountMap.put("brandName", brand.getBrandName());
                                accountMap.put("fileName", shopDetail.getName());
                                accountMap.put("type", "posAction");
                                accountMap.put("content", "订单:" + order.getId() + "在pos端执行退菜返还红包" + backMoney + "元,返还用户Id:" + customer.getId() + ",请求服务器地址为:" + MQSetting.getLocalIP());
                                doPostAnsc(url, accountMap);
                                Map orderAccountMap = new HashMap(4);
                                orderAccountMap.put("brandName", brand.getBrandName());
                                orderAccountMap.put("fileName", order.getId());
                                orderAccountMap.put("type", "orderAction");
                                orderAccountMap.put("content", "订单:" + order.getId() + "在pos端执行退菜返还红包" + backMoney + "元,返还用户Id:" + customer.getId() + ",请求服务器地址为:" + MQSetting.getLocalIP());
                                doPostAnsc(url, orderAccountMap);
                            }
                            Map aliMap = new HashMap(4);
                            aliMap.put("brandName", brand.getBrandName());
                            aliMap.put("fileName", shopDetail.getName());
                            aliMap.put("type", "posAction");
                            aliMap.put("content", "订单:" + order.getId() + "在pos端执行退菜支付宝返还" + refundTotal + "元回调返回信息:" + resultJson + "" +
                                    ",返还用户Id:" + customer.getId() + ",请求服务器地址为:" + MQSetting.getLocalIP());
                            doPostAnsc(url, aliMap);
                            Map orderAliMap = new HashMap(4);
                            orderAliMap.put("brandName", brand.getBrandName());
                            orderAliMap.put("fileName", order.getId());
                            orderAliMap.put("type", "orderAction");
                            orderAliMap.put("content", "订单:" + order.getId() + "在pos端执行退菜支付宝返还" + refundTotal + "元回调返回信息:" + resultJson + "" +
                                    ",返还用户Id:" + customer.getId() + ",请求服务器地址为:" + MQSetting.getLocalIP());
                            doPostAnsc(url, orderAliMap);
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
//            UserActionUtils.writeToFtp(LogType.ORDER_LOG, brand.getBrandName(), shopDetail.getName(), o.getId(),
//                    "总退款金额" + order.getRefundMoney() + "余额返回" + order.getRefundMoney());
            accountService.addAccount(order.getRefundMoney(), customer.getAccountId(), "退菜红包", AccountLog.REFUND_ARTICLE_RED_PACKAGE, order.getShopDetailId());
            RedPacket redPacket = new RedPacket();
            redPacket.setId(ApplicationUtils.randomUUID());
            redPacket.setRedMoney(order.getRefundMoney());
            redPacket.setCreateTime(new Date());
            redPacket.setCustomerId(customer.getId());
            redPacket.setBrandId(order.getBrandId());
            redPacket.setShopDetailId(order.getShopDetailId());
            redPacket.setRedRemainderMoney(order.getRefundMoney());
            redPacket.setRedType(RedType.REFUND_ARTICLE_RED);
            redPacketService.insert(redPacket);
            Map map = new HashMap(4);
            map.put("brandName", brand.getBrandName());
            map.put("fileName", shopDetail.getName());
            map.put("type", "posAction");
            map.put("content", "订单:" + order.getId() + "在pos端执行退菜返还红包" + order.getRefundMoney() + "元,返还用户Id:" + customer.getId() + ",请求服务器地址为:" + MQSetting.getLocalIP());
            doPostAnsc(url, map);
            Map orderMap = new HashMap(4);
            orderMap.put("brandName", brand.getBrandName());
            orderMap.put("fileName", order.getId());
            orderMap.put("type", "orderAction");
            orderMap.put("content", "订单:" + order.getId() + "在pos端执行退菜返还红包" + order.getRefundMoney() + "元,返还用户Id:" + customer.getId() + ",请求服务器地址为:" + MQSetting.getLocalIP());
            doPostAnsc(url, orderMap);
        }
    }

    public void refundArticleNoPay(Order order){
        OrderPaymentItem back = new OrderPaymentItem();
        back.setId(ApplicationUtils.randomUUID());
        back.setOrderId(order.getId());
        back.setPaymentModeId(PayMode.REFUND_CRASH);
        back.setPayTime(new Date());
        back.setPayValue(new BigDecimal(-1).multiply(order.getRefundMoney()));
        back.setRemark("现金退款:" + order.getRefundMoney());

        back.setResultData("线下现金退款总金额：" + order.getRefundMoney());
        orderPaymentItemService.insert(back);
    }

    @Override
    public void refundItem(Order refundOrder) {
        //修改菜品数量
        Order order = getOrderInfo(refundOrder.getId());
        int customerCount = 0;
        BigDecimal servicePrice = new BigDecimal(0);
        Brand brand = brandService.selectById(order.getBrandId());
        ShopDetail shopDetail = shopDetailService.selectByPrimaryKey(order.getShopDetailId());
        for (OrderItem orderItem : refundOrder.getOrderItems()) {
            if (orderItem.getType().equals(ArticleType.ARTICLE)) {
                OrderItem item = orderitemMapper.selectByPrimaryKey(orderItem.getId());
                if (item.getCount() < orderItem.getCount()) {
                    throw new RuntimeException("退菜数量有误！");
                }
                orderitemMapper.refundArticle(orderItem.getId(), orderItem.getCount());
                OrderRefundRemark orderRefundRemark = new OrderRefundRemark();
                orderRefundRemark.setOrderId(order.getId());
                orderRefundRemark.setArticleId(orderItemService.selectById(orderItem.getId()).getArticleId());
                orderRefundRemark.setRefundRemarkId(refundOrder.getRefundRemark().getId());
                orderRefundRemark.setRefundRemark(refundOrder.getRefundRemark().getName());
                orderRefundRemark.setRemarkSupply(refundOrder.getRemarkSupply());
                orderRefundRemark.setCreateTime(new Date());
                orderRefundRemark.setRefundCount(orderItem.getCount());
                orderRefundRemark.setShopId(order.getShopDetailId());
                orderRefundRemark.setBrandId(order.getBrandId());
                orderRefundRemarkMapper.insertSelective(orderRefundRemark);
//                UserActionUtils.writeToFtp(LogType.ORDER_LOG, brand.getBrandName(), shopDetail.getName(), order.getId(),
//                        "订单退了" + orderItem.getCount() + "份" + item.getArticleName());
                Map articleMap = new HashMap(4);
                articleMap.put("brandName", brand.getBrandName());
                articleMap.put("fileName", shopDetail.getName());
                articleMap.put("type", "posAction");
                articleMap.put("content", "订单:" + order.getId() + "在pos端执行退菜释放" + orderItem.getCount() + "份菜品(" + orderItem.getArticleName() + "),请求服务器地址为:" + MQSetting.getLocalIP());
                Map orderArticleMap = new HashMap(4);
                orderArticleMap.put("brandName", brand.getBrandName());
                orderArticleMap.put("fileName", order.getId());
                orderArticleMap.put("type", "orderAction");
                orderArticleMap.put("content", "订单:" + order.getId() + "退菜释放" + orderItem.getCount() + "份菜品(" + orderItem.getArticleName() + "),请求服务器地址为:" + MQSetting.getLocalIP());
                doPostAnsc(url, orderArticleMap);
                if (item.getType() == OrderItemType.SETMEALS) {
                    //如果退了套餐，清空子品
                    orderitemMapper.refundArticleChild(orderItem.getId());
//                    UserActionUtils.writeToFtp(LogType.ORDER_LOG, brand.getBrandName(), shopDetail.getName(), order.getId(),
//                            "订单中套餐" + orderItem.getArticleName() + "的子品被清空");
                    Map articleFamilyMap = new HashMap(4);
                    articleFamilyMap.put("brandName", brand.getBrandName());
                    articleFamilyMap.put("fileName", shopDetail.getName());
                    articleFamilyMap.put("type", "posAction");
                    articleFamilyMap.put("content", "订单:" + order.getId() + "在pos端执行退菜释放" + orderItem.getCount() + "份套餐(" + orderItem.getArticleName() + ")下的子品项,请求服务器地址为:" + MQSetting.getLocalIP());
                    doPostAnsc(url, articleFamilyMap);
                    Map orderArticleFamilyMap = new HashMap(4);
                    orderArticleFamilyMap.put("brandName", brand.getBrandName());
                    orderArticleFamilyMap.put("fileName", order.getId());
                    orderArticleFamilyMap.put("type", "orderAction");
                    orderArticleFamilyMap.put("content", "订单:" + order.getId() + "退菜释放" + orderItem.getCount() + "份套餐(" + orderItem.getArticleName() + ")下的子品项,请求服务器地址为:" + MQSetting.getLocalIP());
                    doPostAnsc(url, orderArticleFamilyMap);
                }

            } else if (orderItem.getType().equals(ArticleType.SERVICE_PRICE)) {
                customerCount = order.getCustomerCount() - orderItem.getCount();
                servicePrice = shopDetail.getServicePrice().multiply(new BigDecimal(customerCount));
                orderMapper.refundServicePrice(order.getId(), servicePrice, customerCount);
//                UserActionUtils.writeToFtp(LogType.ORDER_LOG, brand.getBrandName(), shopDetail.getName(), order.getId(),
//                        "订单退了" + customerCount + "份服务费");
                Map map = new HashMap(4);
                map.put("brandName", brand.getBrandName());
                map.put("fileName", shopDetail.getName());
                map.put("type", "posAction");
                map.put("content", "订单:" + order.getId() + "在pos端执行退菜退了" + customerCount + "份服务费(" + shopDetail.getServiceName() + "),请求服务器地址为:" + MQSetting.getLocalIP());
                doPostAnsc(url, map);
                Map orderMap = new HashMap(4);
                orderMap.put("brandName", brand.getBrandName());
                orderMap.put("fileName", order.getId());
                orderMap.put("type", "orderAction");
                orderMap.put("content", "订单:" + order.getId() + "退菜退了" + customerCount + "份服务费(" + shopDetail.getServiceName() + "),请求服务器地址为:" + MQSetting.getLocalIP());
                doPostAnsc(url, orderMap);
            }
        }

        //修改支付项
        Map<String, BigDecimal> orders = new HashMap<>();
        List<OrderItem> orderItems = refundOrder.getOrderItems();
        for (OrderItem orderItem : orderItems) {
            BigDecimal itemValue = BigDecimal.valueOf(orderItem.getCount()).multiply(orderItem.getUnitPrice()).add(orderItem.getExtraPrice());
            if (orders.containsKey(orderItem.getOrderId())) {
                orders.put(orderItem.getOrderId(), orders.get(orderItem.getOrderId()).add(itemValue));
            } else {
                orders.put(orderItem.getOrderId(), itemValue);
            }
        }
        for (String id : orders.keySet()) {
            Order o = new Order();
            o.setId(id);
            o.setRefundMoney(orders.get(id));
            o.setOrderItems(refundOrder.getOrderItems());
            refundArticle(o);
            updateArticle(o);
        }

    }

    @Override
    public Order afterPay(String orderId, String couponId, BigDecimal price, BigDecimal pay, BigDecimal waitMoney, Integer payMode) {
        Order order = selectById(orderId);
        if (order.getPrintTimes() == 1) {
            return null;
        }
        if (order.getPayType() == PayType.NOPAY && "sb".equals(order.getOperatorId()) && order.getIsPay() != OrderPayState.ALIPAYING) {
            order.setIsPay(OrderPayState.NOT_PAY);
            orderMapper.updateByPrimaryKeySelective(order);
        }
        Brand brand = brandService.selectById(order.getBrandId());
        ShopDetail shopDetail = shopDetailService.selectById(order.getShopDetailId());
        JSONResult result = new JSONResult<>();
        Customer customer = customerService.selectById(order.getCustomerId());
        BigDecimal totalMoney = order.getAmountWithChildren().doubleValue() == 0.0 ? order.getOrderMoney() : order.getAmountWithChildren();
        try {
            if (!StringUtils.isEmpty(couponId)) { //使用了优惠券
                Boolean usedCouponBefore = couponService.usedCouponBeforeByOrderId(orderId).size() > 0;
                if (!usedCouponBefore) {
                    order.setUseCoupon(couponId);
                    Coupon coupon = couponService.useCoupon(totalMoney, order);
                    OrderPaymentItem item = new OrderPaymentItem();
                    item.setId(ApplicationUtils.randomUUID());
                    item.setOrderId(orderId);
                    item.setPaymentModeId(PayMode.COUPON_PAY);
                    item.setPayTime(new Date());
                    item.setPayValue(coupon.getValue());
                    item.setRemark("优惠卷支付:" + item.getPayValue());
                    price = price.subtract(item.getPayValue());
                    item.setResultData(coupon.getId());
                    orderPaymentItemService.insert(item);
                    UserActionUtils.writeToFtp(LogType.ORDER_LOG, brand.getBrandName(), shopDetail.getName(), order.getId(),
                            "订单使用优惠卷支付了：" + item.getPayValue());
                } else {
//                    Coupon coupon = couponService.selectById(couponId);
//                    pay = pay.subtract(coupon.getValue());
//                    pay = pay.add(price);
//                    price = price.subtract(coupon.getValue());
                }
            }
            if (waitMoney.doubleValue() > 0) { //等位红包支付
                OrderPaymentItem item = new OrderPaymentItem();
                item.setId(ApplicationUtils.randomUUID());
                item.setOrderId(orderId);
                item.setPaymentModeId(PayMode.WAIT_MONEY);
                item.setPayTime(new Date());
                item.setPayValue(order.getWaitMoney());
                item.setRemark("等位红包支付:" + order.getWaitMoney());
                item.setResultData(order.getWaitId());
                orderPaymentItemService.insert(item);
                UserActionUtils.writeToFtp(LogType.ORDER_LOG, brand.getBrandName(), shopDetail.getName(), order.getId(),
                        "订单使用等位红包支付了：" + item.getPayValue());
                GetNumber getNumber = getNumberService.selectById(order.getWaitId());
                getNumber.setState(WaitModerState.WAIT_MODEL_NUMBER_THREE);
                getNumberService.update(getNumber);
            }
            log.info("后付的情况下支付的余额为" + price);
            if (price.doubleValue() > 0) {  //余额支付
                accountService.payOrder(order, price, customer, brand, shopDetail);
            }
            log.info("后付的情况下还需支付" + pay);
            OrderPaymentItem item = new OrderPaymentItem();
            if (pay.doubleValue() > 0) { //还需要支付

                order.setPayMode(payMode);
                switch (payMode) {
                    case OrderPayMode.WX_PAY:
                        order.setPaymentAmount(pay);
//                        order.setPrintTimes(1);
                        break;
                    case OrderPayMode.ALI_PAY:
                        order.setPaymentAmount(pay);
                        order.setIsPay(OrderPayState.ALIPAYING);
                        break;
                    case OrderPayMode.YL_PAY:
                        order.setPaymentAmount(pay);
                        order.setOrderState(OrderState.SUBMIT);
                        order.setPrintTimes(1);
                        order.setAllowContinueOrder(false);
                        item.setId(ApplicationUtils.randomUUID());
                        item.setOrderId(orderId);
                        item.setPaymentModeId(PayMode.BANK_CART_PAY);
                        item.setPayTime(new Date());
                        item.setPayValue(pay);
                        item.setRemark("银联支付:" + item.getPayValue());
                        orderPaymentItemService.insert(item);
//                        UserActionUtils.writeToFtp(LogType.ORDER_LOG, brand.getBrandName(), shopDetail.getName(), order.getId(),
//                                "订单使用银联支付了：" + item.getPayValue());
//                        updateChild(order);
                        break;
                    case OrderPayMode.XJ_PAY:
                        order.setPaymentAmount(pay);
                        order.setOrderState(OrderState.SUBMIT);
                        order.setPrintTimes(1);
                        order.setAllowContinueOrder(false);
                        item.setId(ApplicationUtils.randomUUID());
                        item.setOrderId(orderId);
                        item.setPaymentModeId(PayMode.CRASH_PAY);
                        item.setPayTime(new Date());
                        item.setPayValue(pay);
                        item.setRemark("现金支付:" + item.getPayValue());
                        orderPaymentItemService.insert(item);
//                        UserActionUtils.writeToFtp(LogType.ORDER_LOG, brand.getBrandName(), shopDetail.getName(), order.getId(),
//                                "订单使用现金支付了：" + item.getPayValue());
//                        updateChild(order);
                        break;
                    case OrderPayMode.JF_PAY:
                        order.setPaymentAmount(pay);
                        order.setOrderState(OrderState.SUBMIT);
                        order.setPrintTimes(1);
                        order.setAllowContinueOrder(false);
                        item.setId(ApplicationUtils.randomUUID());
                        item.setOrderId(orderId);
                        item.setPaymentModeId(PayMode.INTEGRAL_PAY);
                        item.setPayTime(new Date());
                        item.setPayValue(pay);
                        item.setRemark("会员支付:" + item.getPayValue());
                        orderPaymentItemService.insert(item);
                    default:
                        break;

                }
                update(order);

            } else { //支付完成
                List<OrderPaymentItem> items = orderPaymentItemService.selectByOrderId(order.getId());
                BigDecimal sum = new BigDecimal(0);
                for (OrderPaymentItem orderPaymentItem : items) {
                    sum = sum.add(orderPaymentItem.getPayValue());
                }
                if (order.getAmountWithChildren().doubleValue() > 0 && sum.doubleValue() < order.getAmountWithChildren().doubleValue()) {
                    throw new RuntimeException("支付异常,支付金额小于订单金额");
                }
                if (order.getAmountWithChildren().doubleValue() <= 0 && sum.doubleValue() < order.getOrderMoney().doubleValue()) {
                    throw new RuntimeException("支付异常,支付金额小于订单金额");
                }
                if (order.getOrderState() < OrderState.PAYMENT) {
                    order.setOrderState(OrderState.PAYMENT);
                    order.setAllowCancel(false);
                    order.setPrintTimes(1);
                    order.setPaymentAmount(BigDecimal.valueOf(0));
                    //后付 付款后立马不可加菜
                    order.setAllowContinueOrder(false);
                    update(order);
                    updateChild(order);
                    //后付 付款后直接确认订单  判断是否可以领取红包
                    confirmOrder(order);

                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return order;

    }


    private void updateChild(Order order) {
        List<Order> orders = orderMapper.selectByParentId(order.getId(), order.getPayType());
        for (Order child : orders) {
            if (child.getOrderState() < OrderState.PAYMENT) {
                child.setOrderState(OrderState.PAYMENT);
                child.setPaymentAmount(BigDecimal.valueOf(0));
                child.setAllowCancel(false);
                child.setAllowContinueOrder(false);
                update(child);
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
            origin = origin.add(item.getOriginalPrice());
            total = total.add(item.getFinalPrice());
            if (o.getDistributionModeId() == DistributionType.TAKE_IT_SELF && brandSetting.getIsMealFee() == Common.YES && shopDetail.getIsMealFee() == Common.YES) {
                mealPrice = shopDetail.getMealFeePrice().multiply(new BigDecimal(item.getCount())).multiply(new BigDecimal(item.getMealFeeNumber())).setScale(2, BigDecimal.ROUND_HALF_UP);
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
            o.setCountWithChild(o.getCountWithChild() - order.getOrderItems().size());
        }
        if (o.getParentOrderId() != null) {
            Order parent = selectById(o.getParentOrderId());
            parent.setAmountWithChildren(parent.getAmountWithChildren().subtract(order.getRefundMoney()));
            parent.setCountWithChild(parent.getCountWithChild() - order.getOrderItems().size());
            update(parent);
            Map map = new HashMap(4);
            map.put("brandName", brandSetting.getBrandName());
            map.put("fileName", shopDetail.getName());
            map.put("type", "posAction");
            map.put("content", "订单:" + order.getId() + "在pos端执行退菜修改父订单:" + o.getParentOrderId() + "amountWithChildren字段的值为:" + parent.getAmountWithChildren().subtract(order.getRefundMoney()) + ",请求服务器地址为:" + MQSetting.getLocalIP());
            doPostAnsc(url, map);
            Map orderMap = new HashMap(4);
            orderMap.put("brandName", brandSetting.getBrandName());
            orderMap.put("fileName", order.getId());
            orderMap.put("type", "orderAction");
            orderMap.put("content", "订单:" + order.getId() + "在pos端执行退菜修改父订单:" + o.getParentOrderId() + "amountWithChildren字段的值为:" + parent.getAmountWithChildren().subtract(order.getRefundMoney()) + ",请求服务器地址为:" + MQSetting.getLocalIP());
            doPostAnsc(url, orderMap);
        }
        update(o);
        Map map = new HashMap(4);
        map.put("brandName", brandSetting.getBrandName());
        map.put("fileName", shopDetail.getName());
        map.put("type", "posAction");
        map.put("content", "订单:" + order.getId() + "在pos端执行退菜修改订单项里的菜品数量,订单退掉的菜品数为:" + sum + ",请求服务器地址为:" + MQSetting.getLocalIP());
        doPostAnsc(url, map);
//        Map orderMap = new HashMap(4);
//        orderMap.put("brandName", brandSetting.getBrandName());
//        orderMap.put("fileName", order.getId());
//        orderMap.put("type", "orderAction");
//        orderMap.put("content", "订单:" + order.getId() + "在pos端执行退菜修改订单项里的菜品数量,订单退掉的菜品数为:"+sum+",请求服务器地址为:" + MQSetting.getLocalIP());
//        doPostAnsc(url, orderMap);
        LogTemplateUtils.getBackArticleByOrderType(brandSetting.getBrandName(), order.getId(), o.getOrderItems());
    }

    @Override
    public void refundArticleMsg(Order order) {
        Order o = getOrderInfo(order.getId());

        Customer customer = customerService.selectById(o.getCustomerId());
        Brand brand = brandService.selectById(o.getBrandId());
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
                msg.append("\t").append(shopDetail.getServiceName()).append("X").append(orderItem.getCount()).append("\n");
            }
        }
        msg.append("退菜金额:").append(order.getRefundMoney()).append("\n");
        WeChatUtils.sendCustomerMsg(msg.toString(), customer.getWechatId(), config.getAppid(), config.getAppsecret());
//        UserActionUtils.writeToFtp(LogType.ORDER_LOG, brand.getBrandName(), shopDetail.getName(), o.getId(),
//                "订单发送推送：" + msg.toString());
        Map customerMap = new HashMap(4);
        customerMap.put("brandName", brand.getBrandName());
        customerMap.put("fileName", customer.getId());
        customerMap.put("type", "UserAction");
        customerMap.put("content", "系统向用户:" + customer.getNickname() + "推送微信消息:" + msg.toString() + ",请求服务器地址为:" + MQSetting.getLocalIP());
        doPostAnsc(LogUtils.url, customerMap);
        Map map = new HashMap(4);
        map.put("brandName", brand.getBrandName());
        map.put("fileName", shopDetail.getName());
        map.put("type", "posAction");
        map.put("content", "订单:" + order.getId() + "pos端执行退菜推送微信消息:" + msg.toString() + ",请求服务器地址为:" + MQSetting.getLocalIP());
        doPostAnsc(url, map);
        Map orderMap = new HashMap(4);
        orderMap.put("brandName", brand.getBrandName());
        orderMap.put("fileName", order.getId());
        orderMap.put("type", "orderAction");
        orderMap.put("content", "订单:" + order.getId() + "pos端执行退菜推送微信消息:" + msg.toString() + ",请求服务器地址为:" + MQSetting.getLocalIP());
        doPostAnsc(url, orderMap);
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
    public List<Map<String, Object>> refundOrderPrintReceipt(Order refundOrder) {
        // 根据id查询订单
        List<Map<String, Object>> printTask = new ArrayList<>();
        Order order = selectById(refundOrder.getId());
        Integer oldDistributionModeId = order.getDistributionModeId();
        //如果是 未打印状态 或者  异常状态则改变 生产状态和打印时间
        if (ProductionStatus.HAS_ORDER == order.getProductionStatus() || ProductionStatus.NOT_PRINT == order.getProductionStatus()) {
            order.setProductionStatus(ProductionStatus.PRINTED);
            order.setPrintOrderTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
        }
        order.setBaseCustomerCount(0);
        order.setRefundMoney(refundOrder.getRefundMoney());
        order.setDistributionModeId(DistributionType.REFUND_ORDER);
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
        List<Printer> printer = new ArrayList<>();
        TableQrcode tableQrcode = tableQrcodeService.selectByTableNumberShopId(order.getShopDetailId(), Integer.valueOf(order.getTableNumber()));
        if (tableQrcode == null || order.getDistributionModeId() == DistributionType.TAKE_IT_SELF) {
            printer = printerService.selectByShopAndType(shopDetail.getId(), PrinterType.RECEPTION);
        } else {
            if (tableQrcode.getAreaId() == null) {
                printer = printerService.selectQiantai(shopDetail.getId(), PrinterRange.QIANTAI);
            } else {
                Area area = areaService.selectById(tableQrcode.getAreaId());
                printer = printerService.selectQiantai(shopDetail.getId(), PrinterRange.QIANTAI);
                if (area == null) {

                } else {
                    Printer p = printerService.selectById(area.getPrintId().intValue());
                    printer.add(p);
                }
            }
        }


        for (Printer p : printer) {
            Map<String, Object> ticket = refundOrderPrintTicket(order, orderItems, shopDetail, p);
            if (ticket != null) {
                printTask.add(ticket);
            }
        }
        Order newOrder = new Order();
        newOrder.setId(refundOrder.getId());
        newOrder.setDistributionModeId(oldDistributionModeId);
        orderMapper.updateByPrimaryKeySelective(newOrder);
        return printTask;
    }

    @Override
    public List<Map<String, Object>> refundOrderPrintKitChen(Order refundOrder) {
        Order order = selectById(refundOrder.getId());
        Integer oldDistributionModeId = order.getDistributionModeId();
        //如果是 未打印状态 或者  异常状态则改变 生产状态和打印时间
        if (ProductionStatus.HAS_ORDER == order.getProductionStatus() || ProductionStatus.NOT_PRINT == order.getProductionStatus()) {
            order.setProductionStatus(ProductionStatus.PRINTED);
            order.setPrintOrderTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
        }
        order.setDistributionModeId(DistributionType.REFUND_ORDER);
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
        Order newOrder = new Order();
        newOrder.setId(refundOrder.getId());
        newOrder.setDistributionModeId(oldDistributionModeId);
        orderMapper.updateByPrimaryKeySelective(newOrder);
        return printTask;
    }

    @Override
    public List<Order> selectHasPayNoChangeStatusByBrandId(String brandId) {
        //查询昨日的数据
        Date create = DateUtil.getAfterDayDate(new Date(), -1);
        Date beginDate = DateUtil.getDateBegin(create);
        Date endDate = DateUtil.getDateEnd(create);
        return orderMapper.selectHasPayNoChangeStatusByBrandId(brandId, beginDate, endDate);

    }

    @Override
    public Double selectAppraiseBybrandId(String brandId, Date beginDate, Date endDate) {
        return orderMapper.selectAppraiseBybrandId(brandId, beginDate, endDate);
    }

    @Override
    public BigDecimal selectOrderMoneyByShopIdAndTime(String shopId, Date beginDate, Date endDate) {
        return orderMapper.selectOrderMoneyByShopIdAndTime(shopId, beginDate, endDate);
    }

    @Override
    public Double selectAppraiseByshopId(String shopId, Date beginDate, Date endDate) {
        return orderMapper.selectAppraiseSumByShopId(shopId, beginDate, endDate);
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
            refundItem.put("SUBTOTAL", -shopDetail.getServicePrice().multiply(new BigDecimal((order.getBaseCustomerCount() - order.getCustomerCount()))).doubleValue());
            refundItem.put("ARTICLE_NAME", shopDetail.getServiceName() + "(退)");
            if ("27f56b31669f4d43805226709874b530".equals(brand.getId())) {
                refundItem.put("ARTICLE_NAME", "就餐人数" + "(退)");
            }
            refundItem.put("ARTICLE_COUNT", -(order.getBaseCustomerCount() - order.getCustomerCount()));
            refundItems.add(refundItem);
            articleCount = articleCount.add(new BigDecimal(order.getBaseCustomerCount() - order.getCustomerCount()));
            orderMoney = orderMoney.add(shopDetail.getServicePrice().multiply(new BigDecimal((order.getBaseCustomerCount() - order.getCustomerCount()))));
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
        data.put("ORDER_NUMBER", (String) RedisUtil.get(order.getId() + "orderNumber"));
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

    public List<Order> selectOrderHistoryList(String id, Date dateEnd) {


        return orderMapper.selectOrderHistoryList(id, dateEnd);
    }

    public List<Order> selectListsmsByShopId(Date begin, Date end, String id) {

        return orderMapper.selectListsmsByShopId(begin, end, id);
    }


    @Override
    public Order getCustomerLastOrder(String customerId) {
        return orderMapper.getCustomerLastOrder(customerId);
    }

    @Override
    public Order confirmOrderPos(String orderId) {
        Order order = selectById(orderId);
        //开始状态
        Integer originState = order.getOrderState();
        Brand brand = brandService.selectByPrimaryKey(order.getBrandId());
        ShopDetail shopDetail = shopDetailService.selectById(order.getShopDetailId());
        Customer customer = customerService.selectById(order.getCustomerId());
        orderMapper.confirmOrderPos(orderId);
        if (order.getPayType() == PayType.NOPAY) {
            confirmOrder(order);
        }
        if (order.getPayType() == PayType.PAY && (order.getPayMode() == OrderPayMode.YL_PAY || order.getPayMode() == OrderPayMode.XJ_PAY
                || order.getPayMode() == OrderPayMode.SHH_PAY || order.getPayMode() == OrderPayMode.JF_PAY)) {
            payOrderSuccess(order);
        }
        updateChild(order);
        Map map = new HashMap(4);
        map.put("brandName", brand.getBrandName());
        map.put("fileName", shopDetail.getName());
        map.put("type", "posAction");
        map.put("content", "订单:" + order.getId() + "在pos端已确认收款订单状态更改为10,请求服务器地址为:" + MQSetting.getLocalIP());
        doPostAnsc(url, map);
        LogTemplateUtils.getConfirmOrderPosByOrderType(brand.getBrandName(), order, originState);
        return order;
    }

    @Override
    public BigDecimal selectPayBefore(String orderId) {
        return orderMapper.selectPayBefore(orderId);
    }

    @Override
    public List<Order> getTodayFinishOrder(String shopId, String beginTime, String endTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date beginDate;
        Date endDate;
        try {
            beginDate = DateUtil.getDateBegin(sdf.parse(beginTime));
            endDate = DateUtil.getDateEnd(sdf.parse(endTime));
        } catch (ParseException e) {
            beginDate = DateUtil.getDateBegin(new Date());
            endDate = DateUtil.getDateEnd(new Date());
        }
        return orderMapper.getTodayFinishOrder(shopId, beginDate, endDate);
    }


    @Override
    public List<String[]> getThirdData(List<Order> orderList, int size, String brandSign) {
        List<String[]> result = new ArrayList<>();

        for (Order o : orderList) {
            List<OrderItem> orderItems = orderItemService.listByOrderId(o.getId());
//            Order order = getOrderInfo(o.getId());
            String[] data = new String[size];
            switch (brandSign) {
                case "test":
                    luroufanModel(data, o);
                    result.add(data);

                    for (OrderItem orderItem : orderItems) {
                        result.add(luroufanArticleModel(orderItem, size));
                    }
                    break;
                case "luroufan":
                    luroufanModel(data, o);
                    result.add(data);
                    for (OrderItem orderItem : orderItems) {
                        result.add(luroufanArticleModel(orderItem, size));
                    }
                    break;
                default:
                    break;
            }


        }
        return result;
    }

    private String[] luroufanArticleModel(OrderItem orderItem, int size) {
        String[] data = new String[size];
        data[LuroufanExcelModel.POSDATE] = "";
        data[LuroufanExcelModel.ADDTIME] = "";
        data[LuroufanExcelModel.ADDNAME] = "";
        data[LuroufanExcelModel.POSID] = "";
        data[LuroufanExcelModel.TABLENO] = "";
        data[LuroufanExcelModel.PFNAME] = "";
        data[LuroufanExcelModel.DEPARTMENT] = "";
        data[LuroufanExcelModel.DEPUTY] = "";
        data[LuroufanExcelModel.MENU_TYPE] = OrderItemType.getPayModeName(orderItem.getType());
        data[LuroufanExcelModel.MENU_CODE] = orderItem.getArticleId();
        data[LuroufanExcelModel.MENU_NAME] = orderItem.getArticleName();
        data[LuroufanExcelModel.QUANTITY] = String.valueOf(orderItem.getCount());
        data[LuroufanExcelModel.AMOUNT_1] = "";
        data[LuroufanExcelModel.AMOUNT_2] = "";
        data[LuroufanExcelModel.ACCOUNT_NAME] = "";
        data[LuroufanExcelModel.PAY_METHOD] = "";
        data[LuroufanExcelModel.REMARK] = "";
        return data;
    }


    private void luroufanModel(String[] data, Order o) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        data[LuroufanExcelModel.POSDATE] = sdf.format(o.getCreateTime());
        data[LuroufanExcelModel.ADDTIME] = o.getPrintOrderTime() == null ? "" : sdf.format(o.getPrintOrderTime());
        data[LuroufanExcelModel.ADDNAME] = "";
        data[LuroufanExcelModel.POSID] = o.getShopDetailId();
        data[LuroufanExcelModel.TABLENO] = o.getTableNumber();
        data[LuroufanExcelModel.PFNAME] = o.getSerialNumber();
        data[LuroufanExcelModel.DEPARTMENT] = "";
        data[LuroufanExcelModel.DEPUTY] = "";
        data[LuroufanExcelModel.MENU_TYPE] = "";
        data[LuroufanExcelModel.MENU_CODE] = "";
        data[LuroufanExcelModel.MENU_NAME] = "";
        data[LuroufanExcelModel.QUANTITY] = String.valueOf(o.getArticleCount());
        data[LuroufanExcelModel.AMOUNT_1] = String.valueOf(o.getPaymentAmount());
        data[LuroufanExcelModel.AMOUNT_2] = String.valueOf(o.getOriginalAmount());
        List<OrderPaymentItem> payItemsList = orderPaymentItemService.selectByOrderId(o.getId());
        StringBuilder accountName = new StringBuilder("(");
        for (OrderPaymentItem payment : payItemsList) {
            accountName.append(payment.getRemark()).append(" ");
        }
        accountName.append(")");
        data[LuroufanExcelModel.ACCOUNT_NAME] = accountName.toString();
        data[LuroufanExcelModel.PAY_METHOD] = OrderPayMode.getPayModeName(o.getPayMode());
        data[LuroufanExcelModel.REMARK] = "";
    }

    @Override
    public List<Order> selectByOrderSatesAndNoPay(String shopId) {
        return orderMapper.selectByOrderSatesAndNoPay(shopId);
    }

    @Override
    public List<Order> selectBaseToThirdList(String brandId, String beginDate, String endDate) {
        Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);
        return orderMapper.selectBaseToThirdList(begin, end, brandId);
    }

    @Override
    public List<Order> selectBaseToThirdListByShopId(String shopId, String beginDate, String endDate) {
        Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);
        return orderMapper.selectBaseToThirdListByShopId(begin, end, shopId);
    }

    @Override
    public List<Order> selectBaseToKCList(String brandId, String beginDate, String endDate) {

        Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);
        return orderMapper.selectBaseToKCList(brandId, begin, end);

    }

    @Override
    public List<Order> selectBaseToKCListByShopId(String shopId, String beginDate, String endDate) {
        Date begin = DateUtil.getformatBeginDate(beginDate);
        Date end = DateUtil.getformatEndDate(endDate);
        return orderMapper.selectBaseToKCListByShopId(shopId, begin, end);
    }


    @Override
    public void changeOrderMode(String orderId) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order.getPayMode() == OrderPayMode.YL_PAY) {
            order.setPayMode(OrderPayMode.XJ_PAY);
        } else if (order.getPayMode() == OrderPayMode.XJ_PAY) {
            order.setPayMode(OrderPayMode.YL_PAY);
        }
        update(order);

        List<OrderPaymentItem> list = orderPaymentItemService.selectByOrderId(orderId);
        for (OrderPaymentItem paymentItem : list) {
            if (paymentItem.getPaymentModeId() == PayMode.CRASH_PAY
                    && paymentItem.getPayValue().doubleValue() > 0) {
                paymentItem.setPaymentModeId(PayMode.BANK_CART_PAY);
                paymentItem.setRemark("银联支付:" + paymentItem.getPayValue());
                orderPaymentItemService.update(paymentItem);
            } else if (paymentItem.getPaymentModeId() == PayMode.BANK_CART_PAY
                    && paymentItem.getPayValue().doubleValue() > 0) {
                paymentItem.setPaymentModeId(PayMode.CRASH_PAY);
                paymentItem.setRemark("现金:" + paymentItem.getPayValue());
                orderPaymentItemService.update(paymentItem);
            }
        }

    }


    @Override
    public Order posPayOrder(String orderId, Integer payMode, String couponId, BigDecimal payValue, BigDecimal giveChange, BigDecimal remainValue, BigDecimal couponValue) {
        Order order = selectById(orderId);
        updateChild(order);
        Customer customer = customerService.selectById(order.getCustomerId());
        Brand brand = brandService.selectById(order.getBrandId());
        ShopDetail shopDetail = shopDetailService.selectById(order.getShopDetailId());
        Order newOrder = new Order();
        newOrder.setId(order.getId());
        newOrder.setOrderState(OrderState.PAYMENT);
        newOrder.setPayMode(payMode);
        newOrder.setPrintTimes(1);
        newOrder.setGiveChange(giveChange);
        newOrder.setIsPosPay(Common.YES);
        update(newOrder);
        OrderPaymentItem paymentItem = new OrderPaymentItem();
        if (payValue.compareTo(BigDecimal.ZERO) > 0) {
            paymentItem.setId(ApplicationUtils.randomUUID());
            paymentItem.setPayTime(new Date());
            paymentItem.setPayValue(payValue);
            Integer orderPayMode = 0;
            String remark = "";
            switch (payMode) {
                case 1:
                    orderPayMode = PayMode.WEIXIN_PAY;
                    remark = "微信支付:" + payValue;
                    break;
                case 2:
                    orderPayMode = PayMode.ALI_PAY;
                    remark = "支付宝支付:" + payValue;
                    break;
                case 3:
                    orderPayMode = PayMode.BANK_CART_PAY;
                    remark = "银联支付:" + payValue;
                    break;
                case 4:
                    orderPayMode = PayMode.CRASH_PAY;
                    remark = "现金支付:" + payValue;
                    break;
                case 5:
                    orderPayMode = PayMode.SHANHUI_PAY;
                    remark = "闪惠支付:" + payValue;
                    break;
                case 6:
                    orderPayMode = PayMode.INTEGRAL_PAY;
                    remark = "积分支付:" + payValue;
                    break;
                default:
                    break;
            }
            paymentItem.setPaymentModeId(orderPayMode);
            paymentItem.setRemark(remark);
            paymentItem.setOrderId(orderId);
            orderPaymentItemService.insert(paymentItem);
        }
        if (remainValue.compareTo(BigDecimal.ZERO) > 0) {
            accountService.payOrder(order, remainValue, customer, brand, shopDetail);
        }
        if (couponValue.compareTo(BigDecimal.ZERO) > 0) {
            couponService.useCouponById(orderId, couponId);
        }
        if (giveChange.compareTo(BigDecimal.ZERO) > 0) {
            paymentItem = new OrderPaymentItem();
            paymentItem.setId(ApplicationUtils.randomUUID());
            paymentItem.setPayTime(new Date());
            paymentItem.setPayValue(giveChange.multiply(new BigDecimal(-1)));
            paymentItem.setRemark("pos端结算订单找零:" + giveChange);
            paymentItem.setPaymentModeId(PayMode.GIVE_CHANGE);
            paymentItem.setOrderId(orderId);
            orderPaymentItemService.insert(paymentItem);
        }
        if (!payMode.equals(1) && !payMode.equals(2)) {
            orderMapper.confirmOrderPos(orderId);
        }
        return order;
    }

    @Override
    public List<Order> selectMonthIncomeDto(Map<String, Object> selectMap) {
        return orderMapper.selectMonthIncomeDto(selectMap);
    }

    @Override
    public Order colseOrder(String orderId) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order.getOrderState() == OrderState.SUBMIT) {
            orderMapper.colseOrder(orderId);
            order.setOrderState(OrderState.CANCEL);
        }
        return order;
    }

    @Override
    public List<Map<String, Object>> reminder(String orderItemId) {
        OrderItem orderItem = orderitemMapper.selectByPrimaryKey(orderItemId);
        List<OrderItem> orderItems = orderitemMapper.getListByParentId(orderItemId);
        orderItems.add(orderItem);
        Order order = orderMapper.selectByPrimaryKey(orderItem.getOrderId());
        order.setDistributionModeId(DistributionType.REMINDER_ORDER);
        List<OrderItem> orderItemList = getOrderItemsWithChild(orderItems);
        return printKitchen(order, orderItemList);
    }

    List<OrderItem> getOrderItemsWithChild(List<OrderItem> orderItems) {
        log.debug("这里查看套餐子项: ");
        Map<String, OrderItem> idItems = ApplicationUtils.convertCollectionToMap(String.class, orderItems);
        for (OrderItem item : orderItems) {
            if (item.getType() == OrderItemType.MEALS_CHILDREN) {
                OrderItem parent = idItems.get(item.getParentId());
                if (parent.getChildren() == null) {
                    parent.setChildren(new ArrayList<OrderItem>());
                }
                parent.getChildren().add(item);
                idItems.remove(item.getId());
            }
        }
        List<OrderItem> items = new ArrayList<>();
        for (OrderItem orderItem : idItems.values()) {
            items.add(orderItem);
            if (orderItem.getChildren() != null && !orderItem.getChildren().isEmpty()) {
//				for (OrderItem childItem:orderItem.getChildren()) {
                List<OrderItem> item = orderitemMapper.getListBySort(orderItem.getId(), orderItem.getArticleId());
                for (OrderItem obj : item) {
                    obj.setArticleName("|_" + obj.getArticleName());
                    items.add(obj);
                }
//                childItem.setArticleName("|__" + childItem.getArticleName());
//                items.add(childItem);
//				}
            }
        }
        return items;
    }





    @Override
    public void fixErrorOrder() {
        orderMapper.fixAllowContinueOrder(new Date());
        List<Order> orders = orderMapper.getAllowAppraise();
        for (Order order : orders) {
            confirmOrder(order);
        }
    }

    @Override
    public List<ShopIncomeDto> selectDayAllOrderItem(Map<String, Object> selectMap) {
        return orderMapper.selectDayAllOrderItem(selectMap);
    }

    @Override
    public List<ShopIncomeDto> selectDayAllOrderPayMent(Map<String, Object> selectMap) {
        return orderMapper.selectDayAllOrderPayMent(selectMap);
    }

    @Override
    public Order customerByOrderForMyPage(String customerId, String shopId) {
        Order order = orderMapper.customerByOrderForMyPage(customerId, shopId);
        return order;
    }

    @Override
    public List<RefundArticleOrder> addRefundArticleDto(String beginDate, String endDate) {
        return orderMapper.addRefundArticleDto(beginDate, endDate);
    }
}
