package com.resto.shop.web.service.impl;

import java.util.List;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.SupportTimeMapper;
import com.resto.shop.web.model.SupportTime;
import com.resto.shop.web.service.SupportTimeService;
import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class SupportTimeServiceImpl extends GenericServiceImpl<SupportTime, Integer> implements SupportTimeService {

    @Resource
    private SupportTimeMapper supporttimeMapper;

    @Override
    public GenericDao<SupportTime, Integer> getDao() {
        return supporttimeMapper;
    }

    @Override
    public List<SupportTime> selectList(String shopDetailId) {
        return supporttimeMapper.selectList(shopDetailId);
    }

	@Override
	public void saveSupportTimes(String articleId, Integer[] supportTimes) {
		supporttimeMapper.deleteArticleSupportTime(articleId);
		supporttimeMapper.insertArticleSupportTime(articleId,supportTimes);
	} 
    
    

}
