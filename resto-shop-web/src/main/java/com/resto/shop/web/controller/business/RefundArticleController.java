package com.resto.shop.web.controller.business;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.resto.brand.core.entity.Result;
import com.resto.brand.web.dto.RefundArticleOrder;
import com.resto.brand.web.model.ShopDetail;
import com.resto.shop.web.constant.PayMode;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.OrderItem;
import com.resto.shop.web.model.OrderPaymentItem;
import com.resto.shop.web.service.OrderItemService;
import com.resto.shop.web.service.OrderPaymentItemService;
import com.resto.shop.web.service.OrderService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;

@Controller
@RequestMapping("refundArticle")
public class RefundArticleController extends GenericController{

    @Resource
    OrderService orderService;

    @Resource
    OrderPaymentItemService orderPaymentItemService;

    @Resource
    OrderItemService orderItemService;

    @RequestMapping("/list")
    public void list(){}

    /**
     * 查询退菜报表list
     * @param beginDate
     * @param endDate
     * @return
     */
    @RequestMapping("/getRefundArticleList")
    @ResponseBody
    public Result getRefundArticleList(String beginDate, String endDate){
        try{
            //得到所有退菜单信息
            List<RefundArticleOrder> refundArticleOrders = orderService.addRefundArticleDto(beginDate, endDate);
            for (RefundArticleOrder articleOrder : refundArticleOrders){
                for (ShopDetail shopDetail : getCurrentShopDetails()){
                    if (articleOrder.getShopId().equalsIgnoreCase(shopDetail.getId())){
                        articleOrder.setShopName(shopDetail.getName());
                        break;
                    }
                }
            }
            return getSuccessResult(refundArticleOrders);
        }catch (Exception e){
            e.printStackTrace();
            log.error("查询退菜报表出错" + e.getMessage());
            return new Result(false);
        }
    }

    @RequestMapping("/getRefundArticleDetail")
    @ResponseBody
    public Result getRefundArticleDetail(String orderId){
        try{
            JSONObject jsonObject = new JSONObject();
            //得到退款支付项
            List<OrderPaymentItem> orderPaymentItems = orderPaymentItemService.selectRefundPayMent(orderId);
            //得到退菜的明细
            List<OrderItem> orderItems = orderItemService.selectRefundArticleItem(orderId);
            //组装退款详情
            for (OrderPaymentItem paymentItem : orderPaymentItems){
                if (paymentItem.getPaymentModeId().equals(PayMode.WEIXIN_PAY)){
                    paymentItem.setId(jsonObject.parseObject(paymentItem.getResultData()).getString("out_refund_no"));
                }else if (paymentItem.getPaymentModeId().equals(PayMode.ALI_PAY)){
                    paymentItem.setId(jsonObject.parseObject(paymentItem.getResultData()).getString("out_trade_no"));
                }
                paymentItem.setPayValue(paymentItem.getPayValue().abs());
                paymentItem.setPaymentModeVal(PayMode.getPayModeName(paymentItem.getPaymentModeId()));
            }
            //声明菜品信息数组，存储退菜详情
            JSONArray array = new JSONArray();
            for (OrderItem orderItem : orderItems){
                jsonObject = new JSONObject();
                jsonObject.put("articleName", orderItem.getArticleName());
                jsonObject.put("unitPrice", orderItem.getUnitPrice());
                jsonObject.put("refundCount", orderItem.getOrderRefundRemark().getRefundCount());
                jsonObject.put("refundMoney", orderItem.getUnitPrice().multiply(new BigDecimal(orderItem.getOrderRefundRemark().getRefundCount())));
                jsonObject.put("refundTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(orderItem.getOrderRefundRemark().getCreateTime()));
                jsonObject.put("refundRemark", orderItem.getOrderRefundRemark().getRefundRemark());
                if (StringUtils.isNotBlank(orderItem.getOrderRefundRemark().getRemarkSupply())){
                    jsonObject.put("refundRemark", jsonObject.get("refundRemark").toString().concat("、").concat(orderItem.getOrderRefundRemark().getRemarkSupply()));
                }
                array.add(jsonObject);
            }
            jsonObject = new JSONObject();
            jsonObject.put("refundPayment", orderPaymentItems);
            jsonObject.put("refundItem", orderItems);
            return getSuccessResult(jsonObject);
        }catch (Exception e){
            e.printStackTrace();
            log.error("查询退菜明细出错" + e.getMessage());
            return new Result(false);
        }
    }
}
