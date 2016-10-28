package com.resto.shop.web.service.impl;

import javax.annotation.Resource;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.ERoleMapper;
import com.resto.shop.web.model.ERole;
import com.resto.shop.web.service.ERoleService;
import cn.restoplus.rpc.server.RpcService;

import java.util.List;

/**
 *
 */
@RpcService
class ERoleServiceImpl extends GenericServiceImpl<ERole, Long> implements ERoleService {

    @Resource
    private ERoleMapper eroleMapper;

    @Override
    public GenericDao<ERole, Long> getDao() {
        return eroleMapper;
    }

    @Override
    public List<ERole> selectRolePermissionList() {
        return eroleMapper.selectRolePermissionList();
    }
}
