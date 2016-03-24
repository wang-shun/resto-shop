package com.resto.shop.web.controller.common;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.resto.brand.core.enums.UserGroupSign;
import com.resto.brand.web.model.Permission;
import com.resto.brand.web.service.PermissionService;

/**
 * 公共视图控制器
 **/
@Controller
public class CommonController {
	
	@Resource
	PermissionService permissionService;
	
    /**
     * 首页
     * @param request
     * @return
     */
    @RequestMapping("/")
    public String index(HttpServletRequest request) {
    	List<Permission> allMenu = permissionService.selectFullStructMenu(UserGroupSign.BRAND_GROUP);
    	request.setAttribute("allMenu", allMenu);
        return "index";
    }

}