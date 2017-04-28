package com.resto.shop.web.controller.business;

import com.resto.brand.core.entity.Result;
import com.resto.brand.core.util.ApplicationUtils;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.OrderRemark;
import com.resto.shop.web.service.OrderRemarkService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

@Controller
@RequestMapping("/orderRemark")
public class OrderRemarkController extends GenericController{

    @Resource
    OrderRemarkService orderRemarkService;

    @RequestMapping("/list")
    public void list(){}

    @RequestMapping("/selectAll")
    @ResponseBody
    public Result selectAll(){
        try{
            List<OrderRemark> orderRemarks = orderRemarkService.selectOrderRemarkAll(getCurrentShopId());
            return getSuccessResult(orderRemarks);
        }catch (Exception e){
            e.printStackTrace();
            log.error("查询全部订单备注出错！");
            return new Result(false);
        }
    }

    @RequestMapping("/selectOne")
    @ResponseBody
    public Result selectOne(String orderRemarkId){
        try{
            OrderRemark orderRemark = orderRemarkService.selectById(orderRemarkId);
            return getSuccessResult(orderRemark);
        }catch (Exception e){
            e.printStackTrace();
            log.error("查询单个订单备注出错！");
            return new Result(false);
        }
    }

    @RequestMapping("/deleteOrderRemark")
    @ResponseBody
    public Result delete(String orderRemarkId){
        try {
            orderRemarkService.delete(orderRemarkId);
        }catch (Exception e){
            e.printStackTrace();
            log.error("删除单个订单备注出错！");
            return new Result(false);
        }
        return getSuccessResult();
    }

    @RequestMapping("/create")
    @ResponseBody
    public Result create(OrderRemark orderRemark){
        try{
            orderRemark.setId(ApplicationUtils.randomUUID());
            orderRemark.setShopDetailId(getCurrentShopId());
            orderRemark.setBrandId(getCurrentBrandId());
            orderRemarkService.insert(orderRemark);
        }catch (Exception e){
            e.printStackTrace();
            log.error("创建订单备注出错！");
            return new Result(false);
        }
        return getSuccessResult();
    }

    @RequestMapping("/update")
    @ResponseBody
    public Result update(String id, String remarkName, Integer sort, Integer state){
        try{
            OrderRemark orderRemark = new OrderRemark();
            orderRemark.setId(id);
            orderRemark.setRemarkName(remarkName);
            orderRemark.setSort(sort);
            orderRemark.setState(state);
            orderRemarkService.update(orderRemark);
        }catch (Exception e){
            e.printStackTrace();
            log.error("创建订单备注出错！");
            return new Result(false);
        }
        return getSuccessResult();
    }
}
