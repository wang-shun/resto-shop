package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.alibaba.fastjson.JSON;
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
import com.resto.shop.web.exception.AppException;
import com.resto.shop.web.model.*;
import com.resto.shop.web.posDto.*;
import com.resto.shop.web.producer.MQMessageProducer;
import com.resto.shop.web.service.*;
import com.resto.shop.web.util.RedisUtil;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.resto.shop.web.service.impl.OrderServiceImpl.generateString;

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

    @Autowired
    private ShopDetailService shopDetailService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ArticlePriceService articlePriceService;

    @Autowired
    private OrderMapper orderMapper;

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
        List<OrderPaymentItem> payItemsList = orderPaymentItemService.selectByOrderId(order.getId());
        List<OrderPaymentDto> orderPaymentDtos = new ArrayList<>();
        for(OrderPaymentItem  paymentItem: payItemsList){
            OrderPaymentDto orderPaymentDto = new OrderPaymentDto(paymentItem);
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
    public void syncPosOrder(String data) {
        JSONObject json = new JSONObject(data);

        OrderDto orderDto = JSON.parseObject(json.get("order").toString(), OrderDto.class);
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
            order.setOrderState(OrderState.PAYMENT);
            order.setPaymentAmount(BigDecimal.valueOf(0));
            order.setAllowCancel(false);
            order.setAllowContinueOrder(false);
            orderService.update(order);
            if(!StringUtils.isEmpty(order.getParentOrderId())){
                updateParent(order);
            }
            updateChild(order);


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
        JSONObject json = new JSONObject(data);
        Order order = JSON.parseObject(json.get("refund").toString(), Order.class);
        Order refundOrder = orderService.getOrderInfo(order.getId());
        if(refundOrder.getOrderState() == OrderState.SUBMIT){
            for(OrderItem orderItem : order.getOrderItems()){
                OrderItem item = orderItemService.selectById(orderItem.getId());
                orderService.updateOrderItem(orderItem.getOrderId(),item.getCount() - orderItem.getCount(),orderItem.getId(), 1);
            }

        }else{
            orderService.refundItem(order);
            orderService.refundArticleMsg(order);
        }

        //判断是否清空

        boolean flag = true;
        for (OrderItem item : refundOrder.getOrderItems()) {
            if (item.getCount() > 0) {
                flag = false;
            }
        }
        if (flag) {
            if (refundOrder.getParentOrderId() == null) {
                List<Order> orders = orderService.selectByParentId(refundOrder.getId(), 1); //得到子订单
                if (orders.size() > 0) {
                    for (Order child : orders) { //遍历子订单
                        child = orderService.getOrderInfo(child.getId());
                        for (OrderItem item : child.getOrderItems()) {
                            if (item.getCount() > 0) {
                                flag = false;
                            }
                        }
                    }
                    if (flag) {
                        refundOrderArticleNull(refundOrder);
                    }
                } else {
                    refundOrderArticleNull(refundOrder);
                }
            } else {
                refundOrderArticleNull(refundOrder);
            }
            //存在退菜之后订单菜品是为负的情况  所以这边添加此校验  若为负则为零
            if(refundOrder.getArticleCount() != null && refundOrder.getArticleCount() < 0){
                refundOrder.setArticleCount(0);
            }else if(refundOrder.getCountWithChild() != null && refundOrder.getCountWithChild() < 0){
                refundOrder.setCountWithChild(0);
            }
            orderService.update(refundOrder);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("dataType","updatePayment");
        jsonObject.put("orderId",order.getId());
        List<OrderPaymentItem> payItemsList = orderPaymentItemService.selectByOrderId(order.getId());
        if(!CollectionUtils.isEmpty(payItemsList)){
            List<OrderPaymentDto> orderPaymentDtos = new ArrayList<>();
            for(OrderPaymentItem orderPaymentItem : payItemsList){
                OrderPaymentDto orderPaymentDto = new OrderPaymentDto(orderPaymentItem);
                orderPaymentDtos.add(orderPaymentDto);
            }
            jsonObject.put("orderPayment", orderPaymentDtos);
        }
        return jsonObject.toString();
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

}
