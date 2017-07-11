package com.resto.shop.web.service.impl;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.WaitPictureMapper;
import com.resto.shop.web.model.WaitPicture;
import com.resto.shop.web.service.WaitPictureService;
import cn.restoplus.rpc.server.RpcService;

import java.util.List;

/**
 *
 */
@RpcService
public class WaitPictureServiceImpl extends GenericServiceImpl<WaitPicture, Integer> implements WaitPictureService {

    @Resource
    private WaitPictureMapper waitpictureMapper;

    @Override
    public GenericDao<WaitPicture, Integer> getDao() {
        return waitpictureMapper;
    }

    @Override
    public int updateStateById(WaitPicture record) {
        return waitpictureMapper.updateStateById(record);
    }

    @Override
    public List<WaitPicture> getWaitPictureList(String shopId) {
        return waitpictureMapper.getWaitPictureList(shopId);
    }
}
