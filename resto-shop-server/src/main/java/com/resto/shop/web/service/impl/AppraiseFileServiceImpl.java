package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.AppraiseFileMapper;
import com.resto.shop.web.model.AppraiseFile;
import com.resto.shop.web.service.AppraiseFileService;

import javax.annotation.Resource;

/**
 * Created by carl on 2016/11/20.
 */
@RpcService
public class AppraiseFileServiceImpl extends GenericServiceImpl<AppraiseFile, String> implements AppraiseFileService {

    @Resource
    private AppraiseFileMapper appraiseFileMapper;

    @Override
    public GenericDao<AppraiseFile, String> getDao() {
        return appraiseFileMapper;
    }

}
