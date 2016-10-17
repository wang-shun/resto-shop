package com.resto.shop.web.dao;

import com.resto.shop.web.model.RolePermission;
import com.resto.brand.core.generic.GenericDao;
import org.apache.ibatis.annotations.Param;

public interface RolePermissionMapper  extends GenericDao<RolePermission,Long> {
    int deleteByPrimaryKey(Long id);

    int insert(RolePermission record);

    int insertSelective(RolePermission record);

    RolePermission selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(RolePermission record);

    int updateByPrimaryKey(RolePermission record);

    RolePermission selectByRoleIdAndPermissionId(@Param("roleId") Long roleId,@Param("permissionId") Long permissionId);
}
