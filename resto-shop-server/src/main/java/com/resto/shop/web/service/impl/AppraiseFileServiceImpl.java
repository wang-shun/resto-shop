package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.model.AppraiseFile;
import com.resto.shop.web.service.AppraiseFileService;

/**
 * Created by carl on 2016/11/20.
 */
@RpcService
public class AppraiseFileServiceImpl extends GenericServiceImpl<AppraiseFile, String> implements AppraiseFileService {
    @Override
    public GenericDao<AppraiseFile, String> getDao() {
        return null;
    }

}
