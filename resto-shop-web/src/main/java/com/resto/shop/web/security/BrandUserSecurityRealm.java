package com.resto.shop.web.security;

import java.util.List;

import javax.annotation.Resource;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.stereotype.Component;

import com.resto.brand.web.model.BrandUser;
import com.resto.brand.web.model.Permission;
import com.resto.brand.web.model.Role;
import com.resto.brand.web.service.PermissionService;
import com.resto.brand.web.service.RoleService;
import com.resto.brand.web.service.BrandUserService;

/**
 * 用户身份验证，以及
 * @author Diamond
 * @date 2016年3月24日 
 */
@Component(value="securityRealm")
public class BrandUserSecurityRealm extends AuthorizingRealm {

	@Resource
    private BrandUserService brandUserService;

	@Resource
    private RoleService roleService;

	@Resource
    private PermissionService permissionService;

    /**
     * 权限检查
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        String username = String.valueOf(principals.getPrimaryPrincipal());
        final BrandUser brandUser = brandUserService.selectByUsername(username);
        Role role= roleService.selectById(brandUser.getRoleId());
            // 添加角色
        System.err.println(role);
        authorizationInfo.addRole(role.getRoleSign());

        final List<Permission> permissions = permissionService.selectPermissionsByRoleId(role.getId());
        for (Permission permission : permissions) {
            // 添加权限
            authorizationInfo.addStringPermission(permission.getPermissionSign());
        }
       
        return authorizationInfo;
    }

    /**
     * 登录验证
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        String username = String.valueOf(token.getPrincipal());
        String password = new String((char[]) token.getCredentials());        
        
    	// 通过数据库进行验证
    	final BrandUser authentication = brandUserService.authentication(new BrandUser(username, password));
    	if (authentication == null) {
    		throw new AuthenticationException("用户名或密码错误.");
    	}
        
        SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(username, password, getName());
        return authenticationInfo;
    }

}
