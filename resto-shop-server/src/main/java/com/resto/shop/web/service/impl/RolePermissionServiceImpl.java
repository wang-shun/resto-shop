package com.resto.shop.web.service.impl;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.RolePermissionMapper;
import com.resto.shop.web.model.RolePermission;
import com.resto.shop.web.service.RolePermissionService;
import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class RolePermissionServiceImpl extends GenericServiceImpl<RolePermission, Long> implements RolePermissionService {

    @Resource
    private RolePermissionMapper rolepermissionMapper;

    @Override
    public GenericDao<RolePermission, Long> getDao() {
        return rolepermissionMapper;
    } 

}
