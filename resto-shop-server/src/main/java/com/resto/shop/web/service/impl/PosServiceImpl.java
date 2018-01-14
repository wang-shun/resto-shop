package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.alibaba.fastjson.JSON;
import com.resto.brand.core.util.DateUtil;
import com.resto.brand.core.util.SMSUtils;
import com.resto.brand.web.model.AccountSetting;
import com.resto.brand.web.model.Brand;
import com.resto.brand.web.model.BrandSetting;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.service.AccountSettingService;
import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.BrandSettingService;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.shop.web.constant.*;
import com.resto.shop.web.dao.OrderMapper;
import com.resto.shop.web.dao.PosMapper;
import com.resto.shop.web.exception.AppException;
import com.resto.shop.web.model.*;
import com.resto.shop.web.posDto.*;
import com.resto.shop.web.producer.MQMessageProducer;
import com.resto.shop.web.service.*;
import com.resto.shop.web.util.RedisUtil;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.resto.shop.web.service.impl.OrderServiceImpl.generateString;

/**
 * Created by KONATA on 2017/8/9.
 */
@RpcService
public class PosServiceImpl implements PosService {

    Logger log = LoggerFactory.getLogger(getClass());

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

    @Autowired
    private ShopDetailService shopDetailService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ArticlePriceService articlePriceService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private PosMapper posMapper;

    @Autowired
    private OrderRefundRemarkService orderRefundRemarkService;

    @Autowired
    private CloseShopService closeShopService;

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
        Map map = new HashMap();
        map.put("orderId",orderId);
        map.put("count","count > 0");
        List<OrderItem> orderItems = orderItemService.selectOrderItemByOrderId(map);
        List<OrderItemDto> orderItemDtos = new ArrayList<>();
        for(OrderItem orderItem : orderItems){
            OrderItemDto orderItemDto = new OrderItemDto(orderItem);
            orderItemDtos.add(orderItemDto);
        }
        Customer customer = customerService.selectById(order.getCustomerId());
        jsonObject.put("customer", new JSONObject(new CustomerDto(customer)));
        jsonObject.put("orderItem", orderItemDtos);
        if(order.getPayMode() == OrderPayMode.YUE_PAY || order.getPayMode() == OrderPayMode.XJ_PAY
                || order.getPayMode() == OrderPayMode.YL_PAY){
            List<OrderPaymentItem> payItemsList = orderPaymentItemService.selectByOrderId(order.getId());
            if(!CollectionUtils.isEmpty(payItemsList)){
                List<OrderPaymentDto> orderPaymentDtos = new ArrayList<>();
                for(OrderPaymentItem orderPaymentItem : payItemsList){
                    OrderPaymentDto orderPaymentDto = new OrderPaymentDto(orderPaymentItem);
                    if(orderPaymentDto.getPaymentModeId() == PayMode.ACCOUNT_PAY){
                        orderPaymentDto.setResultData("手机端完成的余额支付");
                    }
                    if(orderPaymentDto.getPaymentModeId() == PayMode.COUPON_PAY){
                        orderPaymentDto.setResultData("手机端完成的优惠券支付");
                    }
                    if(orderPaymentDto.getPaymentModeId() == PayMode.REWARD_PAY){
                        orderPaymentDto.setResultData("手机端完成的充值赠送金额支付");
                    }
                    orderPaymentDtos.add(orderPaymentDto);
                }
                jsonObject.put("orderPayment", orderPaymentDtos);
            }
        }


        CustomerAddress customerAddress = customerAddressService.selectByPrimaryKey(order.getCustomerAddressId());

        if(customerAddress != null){
            CustomerAddressDto customerAddressDto = new CustomerAddressDto(customerAddress);
            jsonObject.put("customerAddress",new JSONObject(customerAddressDto));
        }
        return jsonObject.toString();
    }

    @Override
    public String syncOrderPay(String orderId) {
        Order order = orderService.selectById(orderId);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("dataType", "orderPay");
        jsonObject.put("orderId",order.getId());
        jsonObject.put("payMode", order.getPayMode());
        jsonObject.put("isPosPay", order.getIsPosPay());
        List<OrderPaymentItem> payItemsList = orderPaymentItemService.selectByOrderId(order.getId());
        List<OrderPaymentDto> orderPaymentDtos = new ArrayList<>();
        for(OrderPaymentItem  paymentItem: payItemsList){
            OrderPaymentDto orderPaymentDto = new OrderPaymentDto(paymentItem);
            if(orderPaymentDto.getPaymentModeId() == PayMode.WEIXIN_PAY || orderPaymentDto.getPaymentModeId() == PayMode.ALI_PAY){
                orderPaymentDto.setResultData("请在服务器查看");
            }
            if(orderPaymentDto.getPaymentModeId() == PayMode.ACCOUNT_PAY){
                orderPaymentDto.setResultData("手机端完成的余额支付");
            }
            if(orderPaymentDto.getPaymentModeId() == PayMode.COUPON_PAY){
                orderPaymentDto.setResultData("手机端完成的优惠券支付");
            }
            if(orderPaymentDto.getPaymentModeId() == PayMode.REWARD_PAY){
                orderPaymentDto.setResultData("手机端完成的充值赠送金额支付");
            }
            orderPaymentDtos.add(orderPaymentDto);
        }
        jsonObject.put("orderPayment", orderPaymentDtos);
        return jsonObject.toString();
    }

    @Override
    public String syncPlatform(String orderId) {
        Map<String, Object> result = new HashMap<>();
        result.put("dataType", "platform");
        PlatformOrder platformOrder = platformOrderService.selectByPlatformOrderId(orderId, null);
        if(platformOrder == null){
            return null;
        }
        List<PlatformOrderDetail> platformOrderDetails = platformOrderDetailService.selectByPlatformOrderId(orderId);
        List<PlatformOrderDetailDto> platformOrderDetailDtos = new ArrayList<>();
        for(PlatformOrderDetail platformOrderDetail : platformOrderDetails){
            PlatformOrderDetailDto detailDto = new PlatformOrderDetailDto(platformOrderDetail);
            platformOrderDetailDtos.add(detailDto);
        }
        List<PlatformOrderExtra> platformOrderExtras = platformOrderExtraService.selectByPlatformOrderId(orderId);
        List<PlatformOrderExtraDto> extraDtos = new ArrayList<>();
        for(PlatformOrderExtra platformOrderExtra : platformOrderExtras){
            PlatformOrderExtraDto extraDto = new PlatformOrderExtraDto(platformOrderExtra);
            extraDtos.add(extraDto);
        }
        result.put("order", new PlatformOrderDto(platformOrder));
        result.put("orderDetail", platformOrderDetailDtos);
        result.put("orderExtra", extraDtos);
        return new JSONObject(result).toString();
    }

    @Override
    public void articleActived(String articleId, Integer actived) {
        articleService.setActivated(articleId,actived);
    }

    @Override
    public void articleEmpty(String articleId) {
        Article article = articleService.selectById(articleId);
        if (articleId.indexOf("@") > -1) { //老规格下的子品
            RedisUtil.set(articleId + Common.KUCUN, 0);
            String aid = articleId.substring(0, articleId.indexOf("@"));
            //得到这个老规格下的所有属性
            List<ArticlePrice> articlePrices = articlePriceService.selectByArticleId(aid);
            int sum = 0;
            if (!CollectionUtils.isEmpty(articlePrices)) {
                for (ArticlePrice articlePrice : articlePrices) {
                    Integer ck = (Integer) RedisUtil.get(articlePrice.getId() + Common.KUCUN);
                    if (ck != null) {
                        sum += ck;
                    } else {
                        sum += articlePrice.getCurrentWorkingStock();
                    }
                }
                RedisUtil.set(aid + Common.KUCUN, sum);
                if (sum == 0) {
                    articleService.setEmpty(aid);
                } else {
                    articleService.setEmptyFail(aid);
                }
            }
        }else{
            RedisUtil.set(articleId + Common.KUCUN, 0);
            articleService.setEmpty(articleId);
            List<ArticlePrice> articlePrices = articlePriceService.selectByArticleId(articleId);
            if (!CollectionUtils.isEmpty(articlePrices)) {
                for (ArticlePrice articlePrice : articlePrices) {
                    RedisUtil.set(articlePrice.getId() + Common.KUCUN, 0);
                }
            }
        }


    }

    @Override
    public void articleEdit(String articleId, Integer count) {
        String shopId;
        if (articleId.indexOf("@") > -1) { //老规格下的子品
            String aid = articleId.substring(0, articleId.indexOf("@"));
            Article article = articleService.selectById(aid);
            shopId = article.getShopDetailId();
        }else{
            Article article = articleService.selectById(articleId);
            shopId = article.getShopDetailId();
        }

        articleService.editStock(articleId,count,shopId);
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
    public void syncPosCreateOrder(String data) {
        JSONObject json = new JSONObject(data);
        OrderDto orderDto = JSON.parseObject(json.get("order").toString(), OrderDto.class);
        Order serverDataBaseOrder = orderMapper.selectByPrimaryKey(orderDto.getId());
        if(serverDataBaseOrder != null){  //  判断服务器数据库是否已经存在此订单
            return;
        }
        if(StringUtils.isNotEmpty(orderDto.getParentOrderId())){
            Order order = orderService.selectById(orderDto.getParentOrderId());
            orderDto.setCustomerId(order.getCustomerId());
        }
        orderDto.setShopDetailId(json.getString("shopId"));
        Order order = new Order(orderDto);
        ShopDetail shopDetail = shopDetailService.selectByPrimaryKey(json.getString("shopId"));
        order.setOrderMode(shopDetail.getShopMode());
        order.setReductionAmount(BigDecimal.valueOf(0));
        order.setBrandId(json.getString("brandId"));
        List<OrderItemDto> orderItemDtos =  orderDto.getOrderItem();
        List<OrderItem> orderItems = new ArrayList<>();
        for(OrderItemDto orderItemDto : orderItemDtos){
            OrderItem orderItem = new OrderItem(orderItemDto);
            orderItems.add(orderItem);
        }
        order.setOrderItems(orderItems);
        if(!StringUtils.isEmpty(orderDto.getParentOrderId())){
            //子订单
            Order parent = orderService.selectById(order.getParentOrderId());
            order.setVerCode(parent.getVerCode());
            orderService.insert(order);
            orderItemService.insertItems(orderItems);
            updateParent(order);

        }else{
            //主订单
            if(StringUtils.isEmpty(order.getVerCode())){
                order.setVerCode(generateString(5));
            }
            orderService.insert(order);
            orderItemService.insertItems(orderItems);
        }
        // 更新库存
        Boolean updateStockSuccess = false;
        try {
            updateStockSuccess = orderService.updateStock(orderService.getOrderInfo(order.getId()));
        } catch (AppException e) {
            e.printStackTrace();
        }
        if (!updateStockSuccess) {
            log.info("库存变更失败:" + order.getId());
        }
    }

    @Override
    public void syncPosOrderPay(String data) {
        JSONObject json = new JSONObject(data);
        Order order = orderService.selectById(json.getString("orderId"));
        if(order != null && order.getOrderState() == OrderState.SUBMIT){

            List<OrderPaymentDto> orderPaymentDtos = JSON.parseArray(json.get("orderPayment").toString(), OrderPaymentDto.class);
            for(OrderPaymentDto orderPaymentDto : orderPaymentDtos){
                OrderPaymentItem orderPaymentItem = new OrderPaymentItem(orderPaymentDto);
                orderPaymentItemService.insert(orderPaymentItem);
            }
            //  根据 pos 传输的数据为准
            if(json.has("isPosPay")){
                order.setIsPosPay(json.getInt("isPosPay"));
            }
            order.setOrderState(OrderState.PAYMENT);
            order.setPaymentAmount(BigDecimal.valueOf(0));
            order.setAllowCancel(false);
            order.setAllowContinueOrder(false);
            orderService.update(order);
            if(!StringUtils.isEmpty(order.getParentOrderId())){
                updateParent(order);
            }
            updateChild(order);

            RedisUtil.set(order.getShopDetailId()+order.getTableNumber()+"status",true);
            orderService.confirmBossOrder(order);
        }
    }

    private void updateParent(Order order){
        Order parent = orderService.selectById(order.getParentOrderId());
        int articleCountWithChildren = 0;
        Double amountWithChildren = 0.0;
        articleCountWithChildren = orderMapper.selectArticleCountByIdBossOrder(parent.getId());
        amountWithChildren = orderMapper.selectParentAmountByBossOrder(parent.getId());
        parent.setCountWithChild(articleCountWithChildren);
        parent.setAmountWithChildren(new BigDecimal(amountWithChildren));
        orderService.update(parent);
    }


    private void updateChild(Order order) {
        List<Order> orders = orderService.selectByParentId(order.getId(), order.getPayType());
        if(!CollectionUtils.isEmpty(orders)){
            for (Order child : orders) {
                if (child.getOrderState() < OrderState.PAYMENT) {
                    child.setOrderState(OrderState.PAYMENT);
                    child.setPaymentAmount(BigDecimal.valueOf(0));
                    child.setAllowCancel(false);
                    child.setAllowContinueOrder(false);
                    orderService.update(child);
                }
            }
        }

    }

    @Override
    public String syncPosRefundOrder(String data) {
        JSONObject result = new JSONObject();
        result.put("success", true);
        try {
            JSONObject json = new JSONObject(data);
            Order order = JSON.parseObject(json.get("refund").toString(), Order.class);
            //标识为pos2.0退菜
            order.setPosRefundArticleType(Common.YES);
            Order refundOrder = orderService.getOrderInfo(order.getId());
            if (refundOrder.getOrderState() == OrderState.SUBMIT) {
                for (OrderItem orderItem : order.getOrderItems()) {
                    OrderItem item = orderItemService.selectById(orderItem.getId());
                    orderService.updateOrderItem(item.getOrderId(), item.getCount() - orderItem.getCount(), orderItem.getId(), 1);
                }
            } else {
                orderService.refundItem(order);
                result.put("data", com.alibaba.fastjson.JSONObject.toJSONString(order.getRefundPaymentList()));
                orderService.refundArticleMsg(order);
            }
            //退菜后再重新更新一下主订单信息， 用来判断是否一退光
//            refundOrder = orderService.getOrderInfo(order.getId());
            //判断是否清空
            boolean flag = true;
            //原逻辑为articleCount为0则改成退菜取消，现改成如果orderMoney为0则改为退菜取消(注：其实orderMoney为0就是菜品及服务费退完了)
            if (refundOrder.getOrderMoney().compareTo(BigDecimal.ZERO) == 0) {
                flag = false;
            }
            if (flag) {
                //如果当前订单为主订单
                if (refundOrder.getParentOrderId() == null) {
                    List<Order> orders = orderService.selectByParentId(refundOrder.getId(), 1); //得到子订单
                    for (Order child : orders) { //遍历子订单
                        child = orderService.getOrderInfo(child.getId());
                        if (child.getOrderMoney().compareTo(BigDecimal.ZERO) == 0) {
                            flag = false;
                        }
                    }
                    if (flag) {
                        refundOrderArticleNull(refundOrder);
                    }
                } else {
                    refundOrderArticleNull(refundOrder);
                }
                //存在退菜之后订单菜品是为负的情况  所以这边添加此校验  若为负则为零
                if (refundOrder.getArticleCount() != null && refundOrder.getArticleCount() < 0) {
                    refundOrder.setArticleCount(0);
                } else if (refundOrder.getCountWithChild() != null && refundOrder.getCountWithChild() < 0) {
                    refundOrder.setCountWithChild(0);
                }
                orderService.update(refundOrder);
            }
            // 插入退款备注
            orderRefundRemarkService.posSyncInsertList(order.getOrderRefundRemarks());
            // 还原库存
            Boolean addStockSuccess = false;
            try {
                addStockSuccess = orderService.addStock(order);
            } catch (AppException e) {
                e.printStackTrace();
            }
            if (!addStockSuccess) {
                log.info("库存还原失败:" + order.getId());
            }
        }catch (Exception e) {
            String errorMsg = "退菜失败";
            //如果报错信息不为空
            if (com.resto.brand.core.util.StringUtils.isNotBlank(e.getMessage())){
                com.alibaba.fastjson.JSONObject object = JSON.parseObject(e.getMessage());
                if (object != null){
                    if (com.resto.brand.core.util.StringUtils.isNotBlank(object.getString("err_code_des"))) { //微信退款失败
                        errorMsg = object.getString("err_code_des");
                    }
                }
            }
            result.put("success", false);
            result.put("message", errorMsg);
        }
        return result.toString();
    }

    private void refundOrderArticleNull(Order refundOrder) {
        if (refundOrder.getServicePrice().doubleValue() <= 0) {
            refundOrder.setAllowAppraise(false);
            refundOrder.setAllowContinueOrder(false);
            refundOrder.setIsRefundOrder(true);
            refundOrder.setProductionStatus(ProductionStatus.REFUND_ARTICLE);
        }
    }

    @Override
    public void syncPosConfirmOrder(String orderId) {
        Order order = orderService.selectById(orderId);
        if(order != null && order.getOrderState() == OrderState.SUBMIT ){
            orderService.confirmOrderPos(orderId);
        }
    }



    @Override
    public void test() {
//        Order order = new Order();
//        order.setId("00b8a27437cf460c93910bdc2489d061");
//        order.setBrandId("31946c940e194311b117e3fff5327215");
//        order.setShopDetailId("31164cebcc4b422685e8d9a32db12ab8");
        MQMessageProducer.sendPlatformOrderMessage("1210056817231407326",1,"2f83afee7a0e4822a6729145dd53af33","8565844c69b94b0dbde38b0861df62c8");
    }

    @Override
    public List<ArticleSupport> syncArticleSupport(String shopId) {
        return posMapper.selectArticleSupport(shopId);
    }

    @Override
    public void syncChangeTable(String orderId, String tableNumber) {
        Order order = orderService.selectById(orderId);
        if(order == null){
            return;
        }
        order.setTableNumber(tableNumber);
        orderService.update(order);
    }

    @Override
    public void syncOpenTable(String shopId,String tableNumber) {
        RedisUtil.set(shopId+tableNumber+"status",false);
    }

    @Override
    public void syncTableState(String shopId, String tableNumber, boolean state) {
        RedisUtil.set(shopId+tableNumber+"status", state);
    }

    @Override
    public boolean syncPosLocalOrder(String data) {
        JSONObject json = new JSONObject(data);
        OrderDto orderDto = JSON.parseObject(json.get("order").toString(), OrderDto.class);
        ShopDetail shopDetail = shopDetailService.selectByPrimaryKey(orderDto.getShopDetailId());
        syncPosLocalOrder(orderDto, shopDetail);
        for(OrderDto childrenOrderDto : orderDto.getChildrenOrders()){
            syncPosLocalOrder(childrenOrderDto, shopDetail);
        }
        return true;
    }

    @Override
    public void posCheckOut(String brandId,String shopId, OffLineOrder offLineOrder) {
//        offLineOrder = new OffLineOrder(ApplicationUtils.randomUUID(), shopId, brandId , 1, BigDecimal.ZERO, 0, 0, BigDecimal.ZERO, 0, new Date(), new Date(), 1);
        Brand brand = brandService.selectByPrimaryKey(brandId);
        ShopDetail shopDetail = shopDetailService.selectByPrimaryKey(shopId);
        closeShopService.cleanShopOrder(shopDetail, offLineOrder, brand);
    }

    @Override
    public void posCancelOrder(String shopId, String orderId) {
        try {
            Order order = orderService.selectById(orderId);
            if(order != null){
                //查询是否存在子订单
                List<Order> childrenOrders = orderService.selectByParentId(orderId, order.getPayType());
                for (Order childrenOrder : childrenOrders) {
                    if (!childrenOrder.getClosed()) {
                        orderService.cancelOrderPos(childrenOrder.getId());//取消子订单
                    }
                }
                orderService.cancelOrderPos(orderId);//取消父订单

                //  释放桌位
                if(StringUtils.isEmpty(order.getParentOrderId())){
                    //  如果绑定的有桌位，则释放桌位
                    if(StringUtils.isNotEmpty(order.getTableNumber())){
                        RedisUtil.set(shopId+order.getTableNumber()+"status", true);
                    }
                }else{
                    //  如果父订单的状态 不是 未支付，并且绑定了桌位，则释放主订单的桌位
                    Order parentOrder = orderService.selectById(order.getParentOrderId());
                    if(parentOrder.getOrderState() != OrderState.SUBMIT){
                        if(StringUtils.isNotEmpty(parentOrder.getTableNumber())){
                            RedisUtil.set(shopId+order.getTableNumber()+"status", true);
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("pos端拒绝订单失败！");
            e.printStackTrace();
        }
    }

    @Override
    public void serverError(String brandId, String shopId) {
        RedisUtil.set(shopId + "loginStatus", false);
        com.alibaba.fastjson.JSONObject param = new com.alibaba.fastjson.JSONObject();
        Brand brand = brandService.selectByPrimaryKey(brandId);
        ShopDetail shopDetail = shopDetailService.selectByPrimaryKey(shopId);
        param.put("service" , "【" + brand.getBrandName() + "】-" + shopDetail.getName() + "  Pos2.0系统");
        SMSUtils.sendMessage("17671111590",param ,SMSUtils.SIGN, SMSUtils.SMS_SERVER_ERROR);
    }

    @Override
    public void sendMockMQMessage(String shopId, String type, String orderId, Integer platformType) {
        Order order = new Order();
        if(StringUtils.isNotEmpty(orderId)){
            order = orderService.selectById(orderId);
        }
        switch (type){
            case "createOrder":
                MQMessageProducer.sendCreateOrderMessage(order);
                break;
            case "platform":
                platformType = platformType != null ? platformType : 1;
                ShopDetail shopDetail = shopDetailService.selectByPrimaryKey(shopId);
                if(shopDetail != null){
                    MQMessageProducer.sendPlatformOrderMessage(orderId, platformType, shopDetail.getBrandId(), shopId);
                }
                break;
            case "orderPay":
                MQMessageProducer.sendOrderPay(order);
                break;
            case "cancelOrder":
                MQMessageProducer.sendCancelOrder(order);
                break;
            case "change":
                MQMessageProducer.sendShopChangeMessage(shopId);
                break;
            default:
                log.info("【sendMockMQMessage】未匹配~");
                break;
        }
        log.info("\n\n  shopId：" + shopId + "\n   type：" + type + "\n   orderId：" + orderId + "\n   platformType" + platformType);
    }

    @Override
    public void sendServerCommand(String shopId, String type, String sql) {
        com.alibaba.fastjson.JSONObject obj  = new com.alibaba.fastjson.JSONObject();
        obj.put("shopId", shopId);
        obj.put("dataType", type);
        obj.put("data", sql);
        MQMessageProducer.sendServerCommandToNewPos(obj);
        log.info("\n\n  shopId：" + shopId + "\n   type：" + type + "\n   sql：" + sql);
    }

    @Override
    public List<String> getServerOrderIds(String shopId) {
        Date today = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String beginDate = format.format(DateUtil.getDateBegin(today));
        String endDate = format.format(DateUtil.getDateEnd(today));
        return orderService.posSelectNotCancelledOrdersIdByDate(shopId, beginDate, endDate);
    }

    public void syncPosLocalOrder(OrderDto orderDto, ShopDetail shopDetail){
        String orderId = orderDto.getId();
        StringBuffer backUps = new StringBuffer();
        // 备份老数据
        Order orderBackUps = orderService.posSyncSelectById(orderId);
        if(orderBackUps != null){
            List<OrderItem> orderItemListBackUps = orderItemService.posSyncListByOrderId(orderId);
            List<OrderPaymentItem> orderPaymentItemListBackUps = orderPaymentItemService.posSyncListByOrderId(orderId);
            List<OrderRefundRemark> orderRefundRemarkListBackUps = orderRefundRemarkService.posSyncListByOrderId(orderId);
            orderBackUps.setOrderItems(orderItemListBackUps);
            orderBackUps.setOrderPaymentItems(orderPaymentItemListBackUps);
            orderBackUps.setOrderRefundRemarks(orderRefundRemarkListBackUps);
            backUps.append(DateUtil.getTime()).append("___").append(JSON.toJSONString(orderBackUps));
        }

        // 清除老数据
        orderService.delete(orderId);
        orderItemService.posSyncDeleteByOrderId(orderId);
        orderPaymentItemService.posSyncDeleteByOrderId(orderId);
        orderRefundRemarkService.posSyncDeleteByOrderId(orderId);
        //  插入新数据
        Order order = new Order(orderDto);
        order.setOrderMode(shopDetail.getShopMode());
        order.setReductionAmount(BigDecimal.valueOf(0));
        order.setBrandId(shopDetail.getBrandId());
        order.setPosBackUps(StringUtils.isEmpty(backUps.toString()) ? null : backUps.toString());
        // 如果 服务器端数据状态 为 已确认 或者 已评论，则以服务器为基准
        if(orderBackUps != null && (orderBackUps.getOrderState() == OrderState.CONFIRM || orderBackUps.getOrderState() == OrderState.HASAPPRAISE)){
            order.setOrderState(orderBackUps.getOrderState());
        }
        //  订单
        orderService.insert(order);
        //  订单项
        List<OrderItemDto> orderItemDtos =  orderDto.getOrderItem();
        List<OrderItem> orderItems = new ArrayList<>();
        for(OrderItemDto orderItemDto : orderItemDtos){
            if(StringUtils.isNotEmpty(orderItemDto.getId())){
                OrderItem orderItem = new OrderItem(orderItemDto);
                orderItems.add(orderItem);
            }
        }
        if(orderItems.size() > 0){
            orderItemService.insertItems(orderItems);
        }
        //  订单支付项
        List<OrderPaymentDto> orderPaymentDtos = orderDto.getOrderPayment();
        for(OrderPaymentDto orderPaymentDto : orderPaymentDtos){
            if(StringUtils.isNotEmpty(orderPaymentDto.getId())){
                orderPaymentItemService.insert(new OrderPaymentItem(orderPaymentDto));
            }
        }
        //  订单退菜备注
        List<OrderRefundRemark> orderRefundRemarks = orderDto.getOrderRefundRemarks();
        for(OrderRefundRemark orderRefundRemark: orderRefundRemarks){
            if(orderRefundRemark.getId() != null){
                orderRefundRemarkService.insert(orderRefundRemark);
            }
        }
    }
}
