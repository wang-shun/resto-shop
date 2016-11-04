package com.resto.shop.web.service.impl;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.QueueQrcodeMapper;
import com.resto.shop.web.model.QueueQrcode;
import com.resto.shop.web.service.QueueQrcodeService;
import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class QueueQrcodeServiceImpl extends GenericServiceImpl<QueueQrcode, String> implements QueueQrcodeService {

    @Resource
    private QueueQrcodeMapper queueqrcodeMapper;

    @Override
    public GenericDao<QueueQrcode, String> getDao() {
        return queueqrcodeMapper;
    }

    @Override
    public QueueQrcode selectByIdEndtime(String id) {
        return queueqrcodeMapper.selectByIdEndtime(id);
    }
}
