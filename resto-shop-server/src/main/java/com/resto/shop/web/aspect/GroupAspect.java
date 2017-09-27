package com.resto.shop.web.aspect;

import com.resto.shop.web.model.AppraiseComment;
import com.resto.shop.web.model.TableGroup;
import com.resto.shop.web.producer.MQMessageProducer;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * Created by KONATA on 2017/9/26.
 */
@Component
@Aspect
public class GroupAspect  {

    @Pointcut("execution(* com.resto.shop.web.service.TableGroupService.insertGroup(..))")
    public void insertGroup(){};



    @AfterReturning(value = "insertGroup()", returning = "tableGroup")
    public void insertGroup(TableGroup tableGroup) {
        //创建组后 如果 15分钟内 没有 买单 ，则组自动消失
        MQMessageProducer.removeTableGroup(tableGroup,1 * 60 * 1000);
    }
}
