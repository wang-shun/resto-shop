package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.ApplicationUtils;
import com.resto.brand.web.model.ShopDetail;
import com.resto.shop.web.constant.LogBaseState;
import com.resto.shop.web.dao.LogBaseMapper;
import com.resto.shop.web.model.Customer;
import com.resto.shop.web.model.LogBase;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.model.ShopCart;
import com.resto.shop.web.service.LogBaseService;

import javax.annotation.Resource;
import java.util.Date;

/**
 * Created by carl on 2016/11/14.
 */
@RpcService
public class LogBaseServiceImpl extends GenericServiceImpl<LogBase, String> implements LogBaseService {

    @Resource
    private LogBaseMapper logBaseMapper;

    @Override
    public GenericDao<LogBase, String> getDao() {
        return logBaseMapper;
    }

    @Override
    public void insertLogBaseInfoState(ShopDetail shopDetail, Customer customer, Order order, ShopCart shopCart, Integer type) {
        if(type == LogBaseState.INTO){
            intoLog(shopDetail, customer, order, shopCart);
        } else if (type == LogBaseState.REPLACE){
            replaceLog(shopDetail, customer, order, shopCart);
        }
    }

    //当用户进入店铺是记录log
    public void intoLog(ShopDetail shopDetail, Customer customer, Order order, ShopCart shopCart){
        LogBase logBase = new LogBase();
        GeneralRecord(logBase, shopDetail, customer);
        logBase.setRemark(customer.getNickname()+"进入了店铺");
        logBase.setDesc("当前店铺为"+shopDetail.getName());
        insert(logBase);
    }

    //当用户切换店铺的时候记录log
    public void replaceLog(ShopDetail shopDetail, Customer customer, Order order, ShopCart shopCart){
        LogBase logBase = new LogBase();
        GeneralRecord(logBase, shopDetail, customer);
        logBase.setRemark(customer.getNickname()+"切换了店铺");
        logBase.setDesc("当前店铺为"+shopDetail.getName());
        insert(logBase);
    }

    //通用添加用户店铺时间信息
    public void GeneralRecord(LogBase logBase, ShopDetail shopDetail, Customer customer){
        logBase.setId(ApplicationUtils.randomUUID());
        logBase.setShopId(shopDetail.getId());
        logBase.setShopName(shopDetail.getName());
        logBase.setCustomerId(customer.getId());
        logBase.setNickname(customer.getNickname());
        logBase.setCreateTime(new Date());
    }
}
