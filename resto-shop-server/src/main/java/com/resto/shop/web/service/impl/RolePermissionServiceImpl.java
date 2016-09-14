package com.resto.shop.web.service.impl;

import javax.annotation.Resource;

import com.resto.brand.core.entity.Result;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.RolePermissionMapper;
import com.resto.shop.web.model.ERole;
import com.resto.shop.web.model.Permission;
import com.resto.shop.web.model.RolePermission;
import com.resto.shop.web.service.ERoleService;
import com.resto.shop.web.service.PermissionService;
import com.resto.shop.web.service.RolePermissionService;
import cn.restoplus.rpc.server.RpcService;

import java.util.List;

/**
 *
 */
@RpcService
public class RolePermissionServiceImpl extends GenericServiceImpl<RolePermission, Long> implements RolePermissionService {

    @Resource
    private RolePermissionMapper rolepermissionMapper;

    @Resource
    private ERoleService eRoleService;

    @Resource
    private PermissionService permissionService;

    @Override
    public GenericDao<RolePermission, Long> getDao() {
        return rolepermissionMapper;
    }

    @Override
    public Result selectRolePermissionList() {
        //查询所有的角色
        List<ERole> elist = eRoleService.selectList();
        //查询所有的权限
        List<Permission> plist = permissionService.selectList();

        //查询所有的角色权限
        List<ERole> eRoleList =eRoleService.selectRolePermissionList();


        return null;
    }
}
