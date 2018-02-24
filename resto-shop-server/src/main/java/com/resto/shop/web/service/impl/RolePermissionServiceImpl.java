package com.resto.shop.web.service.impl;

import javax.annotation.Resource;

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

import java.util.LinkedList;
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
    public List<ERole> selectRolePermissionList() {
        //查询所有的角色权限
        List<ERole> eRoleList =eRoleService.selectRolePermissionList();
        List<String> ids = new LinkedList<>();

        if(null!=eRoleList&&eRoleList.size()>0){
            for(ERole eRole:eRoleList){
                if(eRole.getPermissions().size()>0&&eRole.getPermissions()!=null){
                    for(Permission p :eRole.getPermissions()){
                        String id = eRole.getId()+","+p.getId();
                        ids.add(id);
                    }
                }
            }

        }


        //查询所有的角色
        List<ERole> elist = eRoleService.selectList();
        //查询所有的权限
        List<Permission> plist = permissionService.selectList();
        if(null!=elist){
            for (Permission ep: plist) {
                ep.setStatus(0);
            }
            if(null!=plist){
                //把所权限加入所有的角色
                for(ERole e : elist){
                    e.setPermissions(plist);
                }
            }
        }

        //
        for(ERole e :elist){
            for(Permission p :e.getPermissions()){
                if(ids.contains(e.getId()+","+p.getId())){
                    p.setStatus(1);
                }
            }
        }
        return  elist;
    }

    @Override
    public RolePermission selectByRoleIdAndPermissionId(Long roleId, Long permissionId) {
        return rolepermissionMapper.selectByRoleIdAndPermissionId(roleId,permissionId);
    }


}
