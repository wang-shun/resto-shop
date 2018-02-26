package com.resto.shop.web.service;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.ERole;
import com.resto.shop.web.model.RolePermission;

import java.util.List;

public interface RolePermissionService extends GenericService<RolePermission, Long> {

    List<ERole> selectRolePermissionList();

    RolePermission selectByRoleIdAndPermissionId(Long roleId, Long permissionId);
}
