package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.ApplicationUtils;
import com.resto.brand.core.util.StringUtils;
import com.resto.shop.web.dao.PlatformOrderExtraMapper;
import com.resto.shop.web.model.PlatformOrderExtra;
import com.resto.shop.web.service.PlatformOrderExtraService;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 *
 */
@RpcService
public class PlatformOrderExtraServiceImpl extends GenericServiceImpl<PlatformOrderExtra, String> implements PlatformOrderExtraService {

    @Resource
    private PlatformOrderExtraMapper platformorderextraMapper;

    @Override
    public GenericDao<PlatformOrderExtra, String> getDao() {
        return platformorderextraMapper;
    }

    @Override
    public List<PlatformOrderExtra> selectByPlatformOrderId(String platformOrderId) {
        return platformorderextraMapper.selectByPlatformOrderId(platformOrderId);
    }

    @Override
    public void meituanOrderExtra(String orderId,String orderExtra) {
        JSONArray jsonArray = JSON.parseArray(orderExtra);
        for (int index=0;index < jsonArray.size();index++){
            JSONObject object = jsonArray.getJSONObject(index);
            if(StringUtils.isNotEmpty(object.getString("remark"))){
                platformorderextraMapper.insertSelective(meituanConvertToPlatformOrderExtra(orderId,object));
            }
        }
    }

    public PlatformOrderExtra meituanConvertToPlatformOrderExtra(String orderId,JSONObject object){
        PlatformOrderExtra  platformOrderExtra = new PlatformOrderExtra();
        platformOrderExtra.setId(ApplicationUtils.randomUUID());
        platformOrderExtra.setPlatformOrderId(orderId);
        platformOrderExtra.setQuantity(1);
        if(object.containsKey("rider_fee")){//如果包含 rider_fee（骑士配送费），则额外添加此服务费
            platformOrderExtra.setName("美团配送费");
            platformOrderExtra.setPrice(new BigDecimal(Float.toString(object.getFloatValue("rider_fee"))));
        }else{
            platformOrderExtra.setName(object.getString("remark"));
            platformOrderExtra.setPrice(new BigDecimal(Float.toString(object.getFloatValue("reduce_fee"))));
        }
        return platformOrderExtra;
    }
}
