package com.resto.shop.web.service.impl;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.PermissionMapper;
import com.resto.shop.web.model.Permission;
import com.resto.shop.web.service.PermissionService;
import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class PermissionServiceImpl extends GenericServiceImpl<Permission, Long> implements PermissionService {

    @Resource
    private PermissionMapper permissionMapper;

    @Override
    public GenericDao<Permission, Long> getDao() {
        return permissionMapper;
    } 

}
