package com.resto.shop.web.aspect;

import com.resto.shop.web.model.AppraisePraise;
import com.resto.shop.web.model.GetNumber;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by carl on 2016/11/22.
 */
@Component
@Aspect
public class AppraiseAspect {
    Logger log = LoggerFactory.getLogger(getClass());


    @Pointcut("execution(* com.resto.shop.web.service.AppraisePraiseService.updateCancelPraise(..))")
    public void updateCancelPraise(){};

    @AfterReturning(value = "updateCancelPraise()", returning = "appraisePraise")
    public void updateCancelPraiseAfter(AppraisePraise appraisePraise) {

    }

}
