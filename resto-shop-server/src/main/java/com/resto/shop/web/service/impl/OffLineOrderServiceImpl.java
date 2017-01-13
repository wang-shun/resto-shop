package com.resto.shop.web.service.impl;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.DateUtil;
import com.resto.shop.web.dao.OffLineOrderMapper;
import com.resto.shop.web.model.OffLineOrder;
import com.resto.shop.web.service.OffLineOrderService;
import cn.restoplus.rpc.server.RpcService;

import java.util.Date;

/**
 *
 */
@RpcService
public class OffLineOrderServiceImpl extends GenericServiceImpl<OffLineOrder, String> implements OffLineOrderService {

    @Resource
    private OffLineOrderMapper offlineorderMapper;

    @Override
    public GenericDao<OffLineOrder, String> getDao() {
        return offlineorderMapper;
    }

    @Override
    public OffLineOrder selectByTimeSourceAndShopId(Integer source, String shopId) {
        Date begin = DateUtil.getDateBegin(new Date());
        Date end  = DateUtil.getDateEnd(new Date());

        return offlineorderMapper.selectByTimeSourceAndShopId(source,shopId,begin,end);

    }
}
