package com.resto.shop.web.service.impl;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.WeShopScoreMapper;
import com.resto.shop.web.model.WeShopScore;
import com.resto.shop.web.service.WeShopScoreService;
import cn.restoplus.rpc.server.RpcService;

import java.util.Date;

/**
 *
 */
@RpcService
public class WeShopScoreServiceImpl extends GenericServiceImpl<WeShopScore, Integer> implements WeShopScoreService {

    @Resource
    private WeShopScoreMapper weshopscoreMapper;

    @Override
    public GenericDao<WeShopScore, Integer> getDao() {
        return weshopscoreMapper;
    }

    @Override
    public WeShopScore selectByShopIdAndDate(String id, Date beforeYesterDay) {
        return weshopscoreMapper.selectByShopIdAndDate(id,beforeYesterDay);
    }
}
