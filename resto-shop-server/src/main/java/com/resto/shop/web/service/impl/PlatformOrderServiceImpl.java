package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.enums.PlatformKey;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.ApplicationUtils;
import com.resto.brand.web.dto.MeiTuanOrderDto;
import com.resto.shop.web.dao.PlatformOrderMapper;
import com.resto.shop.web.model.PlatformOrder;
import com.resto.shop.web.service.PlatformOrderDetailService;
import com.resto.shop.web.service.PlatformOrderExtraService;
import com.resto.shop.web.service.PlatformOrderService;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;

/**
 *
 */
@RpcService
public class PlatformOrderServiceImpl extends GenericServiceImpl<PlatformOrder, String> implements PlatformOrderService {

    @Resource
    private PlatformOrderMapper platformorderMapper;
    @Resource
    private PlatformOrderDetailService platformOrderDetailService;
    @Resource
    private PlatformOrderExtraService platformOrderExtraService;


    @Override
    public GenericDao<PlatformOrder, String> getDao() {
        return platformorderMapper;
    }

    @Override
    public PlatformOrder selectByPlatformOrderId(String platformOrderId,int type) {
        return platformorderMapper.selectByPlatformOrderId(platformOrderId,type);
    }

    @Override
    public void meituanNewOrder(MeiTuanOrderDto orderDto) {
        PlatformOrder platformOrder = meituanConvertToPlatformOrder(orderDto);;
        platformorderMapper.insertSelective(platformOrder);
        platformOrderDetailService.meituanOrderDetail(orderDto.getOrderId(),orderDto.getDetail());
        platformOrderExtraService.meituanOrderExtra(orderDto.getOrderId(),orderDto.getExtras());
    }

    public PlatformOrder meituanConvertToPlatformOrder(MeiTuanOrderDto orderDto){
        PlatformOrder platformOrder = new PlatformOrder();
        platformOrder.setId(ApplicationUtils.randomUUID());
        platformOrder.setType(PlatformKey.MEITUAN);
        platformOrder.setPlatformOrderId(orderDto.getOrderId());
        platformOrder.setShopDetailId(orderDto.getePoiId());
        platformOrder.setOriginalPrice(new BigDecimal(Double.toString(orderDto.getOriginalPrice())));
        platformOrder.setTotalPrice(new BigDecimal(Double.toString(orderDto.getTotal())));
        platformOrder.setAddress(orderDto.getRecipientAddress());
        platformOrder.setName(orderDto.getRecipientName());
        platformOrder.setPhone(orderDto.getRecipientPhone());
        platformOrder.setOrderCreateTime(new Date( orderDto.getCtime() * 1000));
        platformOrder.setCreateTime(new Date());
        platformOrder.setPayType(orderDto.getPayType()==1?"货到付款":"在线支付");
        platformOrder.setRemark(orderDto.getCaution());
        platformOrder.setSourceText(orderDto.getSourceText());
        return platformOrder;
    }

}
