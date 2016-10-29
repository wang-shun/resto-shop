 package com.resto.shop.web.controller.business;

 import com.resto.brand.core.entity.Result;
 import com.resto.shop.web.constant.OrderState;
 import com.resto.shop.web.controller.GenericController;
 import com.resto.shop.web.model.Order;
 import com.resto.shop.web.service.OrderService;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;

 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 import java.util.List;

 @Controller
 @RequestMapping("orderException")
 public class OrderExceptionController extends GenericController{

     @Resource
     private OrderService orderService;
     /**
      *手动取消已提交但未支付的订单
      * @return
      */
         @RequestMapping("cancelExceptionOrder")
         @ResponseBody
     public Result executeCancelOrder(String beginDate, String endDate, HttpServletRequest request){
             //获取时间
             String begin = request.getParameter("beginDate");
             String end = request.getParameter("endDate");

             //查询所有已提交但未支付的定的那
             List<Order> orderList = orderService.selectNeedCacelOrderList(getCurrentBrandId(),begin,end);
             for(Order order :orderList){
                 if (order.getOrderState() == OrderState.SUBMIT) {
                     System.err.println("自动取消订单:" + order.getId());
                     orderService.cancelExceptionOrder(order.getId());
                 } else {
                     log.info("自动取消订单失败，订单状态不是已提交");
                 }

             }
         return Result.getSuccess();
     }



 }
