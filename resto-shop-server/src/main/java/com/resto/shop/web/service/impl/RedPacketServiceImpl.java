package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.RedPacketMapper;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.model.RedPacket;
import com.resto.shop.web.service.RedPacketService;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 *
 */
@RpcService
public class RedPacketServiceImpl extends GenericServiceImpl<RedPacket, String> implements RedPacketService {

    @Resource
    private RedPacketMapper redPacketMapper;

    @Override
    public GenericDao<RedPacket, String> getDao() {
        return redPacketMapper;
    }

    @Override
    public void useRedPacketPay(BigDecimal redPay, String customerId, Order order) {
        //扣除红包，扣除顺序 评论红包-->分享红包-->退菜红包
        Integer[] redType = {0,1,2};
        for(Integer type : redType){
            useRedPacket(type,redPay,customerId,order);
        }
    }

    private void useRedPacket(Integer redType, BigDecimal redPay, String customerId, Order order){

    }
}
