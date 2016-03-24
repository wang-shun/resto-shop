package com.resto.shop.web.service.impl;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
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

}
