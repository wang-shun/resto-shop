package com.resto.shop.web.aspect;

import com.resto.brand.core.entity.JSONResult;
import com.resto.brand.core.util.DateUtil;
import com.resto.brand.web.model.BrandSetting;
import com.resto.brand.web.model.ShopMode;
import com.resto.shop.web.constant.*;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.model.Receipt;
import com.resto.shop.web.producer.MQMessageProducer;
import com.resto.shop.web.util.RedisUtil;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;

/**
* 发票管理功能切面
* @author xielc
* @date 2017年9月11日
*/
@Component
@Aspect
public class ReceiptAspect {

    Logger log = LoggerFactory.getLogger(getClass());

    @Pointcut("execution(* com.resto.shop.web.service.ReceiptService.insertSelective(..))")
    public void createReceipt() {
    }

    @AfterReturning(value = "createReceipt()", returning = "receipt")
    public void createReceipt(Receipt receipt) throws Throwable {
        log.info("进入发票自动出单切面");
        if (receipt!=null) {
            MQMessageProducer.sendReceiptPrintSuccess(receipt.getShopId(),receipt.getOrderNumber());
        }
    }
}
