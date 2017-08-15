package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.OrderRemarkMapper;
import com.resto.shop.web.model.OrderRemark;
import com.resto.shop.web.service.OrderRemarkService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RpcService
public class OrderRemarkServiceImpl extends GenericServiceImpl<OrderRemark, String> implements OrderRemarkService {

	@Resource
	private OrderRemarkMapper orderRemarkMapper;

    @Resource
    private com.resto.brand.web.service.OrderRemarkService boOrderRemarkService;
	
	@Override
	public GenericDao<OrderRemark, String> getDao() {
		return orderRemarkMapper;
	}

    @Override
    public List<OrderRemark> selectOrderRemarkByShopId(String shopId) {
        return orderRemarkMapper.selectOrderRemarkByShopId(shopId);
    }

    @Override
    public List<JSONObject> getShopOrderRemark(String shopId) {
        List<JSONObject> objectList = new ArrayList<>();
        List<OrderRemark> shopOrderRemarks = orderRemarkMapper.selectOrderRemarkByShopId(shopId);
        List<com.resto.brand.web.model.OrderRemark> brandOrderRemarks = boOrderRemarkService.selectOrderRemarks();
        for (com.resto.brand.web.model.OrderRemark boOrderRemark : brandOrderRemarks){
            if (!shopOrderRemarks.isEmpty()){
                for (OrderRemark orderRemark : shopOrderRemarks){
                    if (orderRemark.getBoOrderRemarkId().equalsIgnoreCase(boOrderRemark.getId())){
                        objectList.add(JSON.parseObject(JSON.toJSONString(boOrderRemark)));
                        break;
                    }
                }
            }else{
                break;
            }
        }
        return objectList;
    }

    @Override
    public void deleteByBoOrderRemarkId(String boOrderRemarkId) {
            orderRemarkMapper.deleteByBoOrderRemarkId(boOrderRemarkId);
    }
}
