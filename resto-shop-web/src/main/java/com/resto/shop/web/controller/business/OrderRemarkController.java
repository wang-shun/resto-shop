package com.resto.shop.web.controller.business;

import com.resto.brand.core.entity.Result;
import com.resto.brand.core.util.ApplicationUtils;
import com.resto.shop.web.constant.Common;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.OrderRemark;
import com.resto.shop.web.service.OrderRemarkService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/orderRemark")
public class OrderRemarkController extends GenericController{

    @Resource
    OrderRemarkService orderRemarkService;

    @Resource
    com.resto.brand.web.service.OrderRemarkService boOrderRemarkService;

    @RequestMapping("/list")
    public void list(){}

    @RequestMapping("/selectAll")
    @ResponseBody
    public Result selectAll(){
        try{
            List<com.resto.brand.web.model.OrderRemark> boOrderRemarks = boOrderRemarkService.selectOrderRemarks();
            List<OrderRemark> orderRemarks = orderRemarkService.selectOrderRemarkByShopId(getCurrentShopId());
            for (com.resto.brand.web.model.OrderRemark boOrderRemark : boOrderRemarks){
                boolean flg = false;
                if (orderRemarks != null) {
                    for (OrderRemark orderRemark : orderRemarks) {
                        if (orderRemark.getBoOrderRemarkId().equalsIgnoreCase(boOrderRemark.getId())) {
                            flg = true;
                            break;
                        }
                    }
                }
                if (flg){
                    boOrderRemark.setState(Common.YES);
                }else{
                    boOrderRemark.setState(Common.NO);
                }
            }
            return getSuccessResult(boOrderRemarks);
        }catch (Exception e){
            e.printStackTrace();
            log.error("查询全部订单备注出错！");
            return new Result(false);
        }
    }

    @RequestMapping("/update")
    @ResponseBody
    public Result create(String boOrderRemarkId, Integer type){
        try{
            if (type.equals(Common.YES)) {
                OrderRemark orderRemark = new OrderRemark();
                orderRemark.setId(ApplicationUtils.randomUUID());
                orderRemark.setBoOrderRemarkId(boOrderRemarkId);
                orderRemark.setCreateTime(new Date());
                orderRemark.setShopDetailId(getCurrentShopId());
                orderRemark.setBrandId(getCurrentBrandId());
                orderRemarkService.insert(orderRemark);
            }else{
                orderRemarkService.deleteByBoOrderRemarkId(boOrderRemarkId);
            }
            return getSuccessResult();
        }catch (Exception e){
            e.printStackTrace();
            log.error("创建订单备注出错！");
            return new Result(false);
        }
    }
}
