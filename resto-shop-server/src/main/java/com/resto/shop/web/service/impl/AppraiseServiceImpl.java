package com.resto.shop.web.service.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.AppraiseMapper;
import com.resto.shop.web.model.Appraise;
import com.resto.shop.web.service.AppraiseService;

import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class AppraiseServiceImpl extends GenericServiceImpl<Appraise, String> implements AppraiseService {

    @Resource
    private AppraiseMapper appraiseMapper;

    @Override
    public GenericDao<Appraise, String> getDao() {
        return appraiseMapper;
    }

	@Override
	public List<Appraise> listAppraise(String currentShopId, Integer currentPage, Integer showCount, Integer maxLevel,
			Integer minLevel) {
		return appraiseMapper.listAppraise(currentShopId, currentPage, showCount, maxLevel, minLevel);
	}

	@Override
	public Map<String, Object> appraiseCount(String currentShopId) {
	    return appraiseMapper.appraiseCount(currentShopId);
	}

	@Override
	public List<Map<String, Object>> appraiseMonthCount(String surrentShopId) {
		return appraiseMapper.appraiseMonthCount(surrentShopId);
	}


}
