package com.resto.shop.web.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.ApplicationUtils;
import com.resto.shop.web.dao.NoticeMapper;
import com.resto.shop.web.model.Notice;
import com.resto.shop.web.service.NoticeService;

import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class NoticeServiceImpl extends GenericServiceImpl<Notice, String> implements NoticeService {

    @Resource
    private NoticeMapper noticeMapper;

    @Override
    public GenericDao<Notice, String> getDao() {
        return noticeMapper;
    } 
    
    @Override
    public void create(Notice notice) {
    	notice.setId(ApplicationUtils.randomUUID());
    	notice.setCreateDate(new Date());
    	insert(notice);
    }

	@Override
	public List<Notice> selectListByShopId(String shopId,String customerId, Integer noticeType) {
		return noticeMapper.selectListByShopId(shopId,noticeType,customerId);
	}

	@Override
	public List<Notice> selectListByShopId(String currentShopId) {
		return noticeMapper.selectListByShopId(currentShopId,null,null);
	}

	@Override
	public void addNoticeHistory(String customerId, String noticeId) {
		noticeMapper.addNoticeHistory(customerId, noticeId);
	}
}
