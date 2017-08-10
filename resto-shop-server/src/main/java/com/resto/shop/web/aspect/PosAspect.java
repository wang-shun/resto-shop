package com.resto.shop.web.aspect;

import com.resto.brand.core.entity.JSONResult;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import static com.resto.shop.web.producer.MQMessageProducer.sendShopChangeMessage;

/**
 * Created by KONATA on 2017/8/10.
 */
@Component
@Aspect
public class PosAspect {

    @Pointcut("execution(* com.resto.shop.web.service.PosService.shopMsgChange(..))")
    public void shopMsgChange() {
    }


    @AfterReturning(value = "shopMsgChange()", returning = "shopId")
    public void shopMsgChange(String shopId) throws Throwable {
        sendShopChangeMessage(shopId);
    }


}
