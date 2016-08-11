 package com.resto.shop.web.controller.business;


 import com.resto.brand.core.entity.Result;
 import com.resto.brand.web.dto.*;
 import com.resto.brand.web.model.Brand;
 import com.resto.brand.web.model.ShopDetail;
 import com.resto.brand.web.service.BrandService;
 import com.resto.brand.web.service.ShopDetailService;
 import com.resto.shop.web.constant.PayMode;
 import com.resto.shop.web.controller.GenericController;
 import com.resto.shop.web.service.OrderPaymentItemService;
 import com.resto.shop.web.service.OrderService;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;

 import javax.annotation.Resource;
 import java.math.BigDecimal;
 import java.util.*;

 @Controller
 @RequestMapping("syncData")
 public class SyncDataController extends GenericController{

     @Resource
     private OrderService orderService;


     @Resource
     private BrandService brandService;

     @Resource
     OrderPaymentItemService orderpaymentitemService;

     @Resource
     private ShopDetailService shopDetailService;

     public SyncDataController() {
     }

     //订单菜品的数据封装
     @RequestMapping("syncOrderArticle")
     @ResponseBody
     public Result getOrderArticle(String beginDate,String endDate){
        List <OrderArticleDto> oA =  orderService.selectOrderArticle(getCurrentBrandId(),beginDate,endDate);
         List<ShopDetail> list = shopDetailService.selectByBrandId(getCurrentBrandId());
         for (OrderArticleDto ot :oA){

             for (ShopDetail s :list){
                 if(s.getId().equals(ot.getShopId())){
                     ot.setShopName(s.getName());
                 }
             }

         }

         return  getSuccessResult(oA);
     }

     //品牌收入报表导入

     @RequestMapping("syncBrandIncome")
     @ResponseBody
     public Result getBrandIncome(String beginDate,String endDate){

         // 查询品牌和店铺的收入情况
         List<IncomeReportDto> incomeReportList = orderpaymentitemService.selectIncomeList(getCurrentBrandId(),beginDate, endDate);

         // 封装brand所需要的数据结构
         Brand brand = brandService.selectById(getCurrentBrandId());
         List<BrandIncomeDto> brandIncomeList = new ArrayList<>();
         BrandIncomeDto in = new BrandIncomeDto();
         // 初始化品牌的信息
         BigDecimal wechatIncome = BigDecimal.ZERO;
         BigDecimal redIncome = BigDecimal.ZERO;
         BigDecimal couponIncome = BigDecimal.ZERO;
         BigDecimal chargeAccountIncome = BigDecimal.ZERO;
         BigDecimal chargeGifAccountIncome = BigDecimal.ZERO;

         if (!incomeReportList.isEmpty()) {
             for (IncomeReportDto income : incomeReportList) {
                 if (income.getPaymentModeId() == PayMode.WEIXIN_PAY) {
                     wechatIncome = wechatIncome.add(income.getPayValue()).setScale(2);
                 } else if (income.getPayMentModeId() == PayMode.ACCOUNT_PAY) {
                     redIncome = redIncome.add(income.getPayValue()).setScale(2);
                 } else if (income.getPayMentModeId() == PayMode.COUPON_PAY) {
                     couponIncome = couponIncome.add(income.getPayValue()).setScale(2);
                 } else if (income.getPaymentModeId() == PayMode.CHARGE_PAY) {
                     chargeAccountIncome = chargeAccountIncome.add(income.getPayValue()).setScale(2);
                 } else if (income.getPayMentModeId() == PayMode.REWARD_PAY) {
                     chargeGifAccountIncome = chargeGifAccountIncome.add(income.getPayValue()).setScale(2);
                 }
             }
         }
         in.setBrandId(brand.getId());
         in.setBrandName(brand.getBrandName());
         in.setWechatIncome(wechatIncome);
         in.setRedIncome(redIncome);
         in.setCouponIncome(couponIncome);
         in.setChargeAccountIncome(chargeAccountIncome);
         in.setChargeGifAccountIncome(chargeGifAccountIncome);
         in.setTotalIncome(in.getWechatIncome(), in.getRedIncome(), in.getCouponIncome(), in.getChargeAccountIncome(),
                 in.getChargeGifAccountIncome());
         brandIncomeList.add(in);
         return  getSuccessResult(brandIncomeList);
     }




 }
