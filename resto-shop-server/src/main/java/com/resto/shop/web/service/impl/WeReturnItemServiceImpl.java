package com.resto.shop.web.service.impl;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.DateUtil;
import com.resto.shop.web.dao.WeReturnItemMapper;
import com.resto.shop.web.model.WeReturnItem;
import com.resto.shop.web.service.WeReturnItemService;
import cn.restoplus.rpc.server.RpcService;

import java.util.Date;
import java.util.List;

/**
 *
 */
@RpcService
public class WeReturnItemServiceImpl extends GenericServiceImpl<WeReturnItem, Long> implements WeReturnItemService {

    @Resource
    private WeReturnItemMapper wereturnitemMapper;

    @Override
    public GenericDao<WeReturnItem, Long> getDao() {
        return wereturnitemMapper;
    }

    @Override
    public List<WeReturnItem> selectByShopIdAndTime(String zuoriDay, String id) {
        Date beginDate = DateUtil.getformatBeginDate(zuoriDay);
        Date endDate = DateUtil.getformatEndDate(zuoriDay);
        return wereturnitemMapper.selectByShopIdAndTime(id,beginDate,endDate);
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        wereturnitemMapper.deleteByIds(ids);
    }
}
