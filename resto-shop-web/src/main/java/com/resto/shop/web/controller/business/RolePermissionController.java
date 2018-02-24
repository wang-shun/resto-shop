package com.resto.shop.web.controller.business;

import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;

import com.resto.shop.web.model.ERole;
import com.resto.shop.web.model.Permission;
import com.resto.shop.web.service.ERoleService;
import com.resto.shop.web.service.PermissionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.shop.web.controller.GenericController;
import com.resto.brand.core.entity.Result;
import com.resto.shop.web.model.RolePermission;
import com.resto.shop.web.service.RolePermissionService;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("rolepermission")
public class RolePermissionController extends GenericController{

	@Resource
	private RolePermissionService rolepermissionService;

     @Resource
     private ERoleService eRoleService;

     @Resource
     private PermissionService permissionService;
	
	@RequestMapping("/list")
//    public void list(){
//    }
    public ModelAndView list(){
        ModelAndView mv = new ModelAndView("rolepermission/list");
        List<ERole> roleList = eRoleService.selectList();			//列出所有的角色
        List<Permission> permissionList= permissionService.selectList();			//列出所有按钮
        List<RolePermission> rolePermissionList =rolepermissionService.selectList();	//列出所有角色按钮关联数据
        mv.addObject("roleList", roleList);
        mv.addObject("permissionList", permissionList);
        mv.addObject("rolePermissionList", rolePermissionList);
        return  mv;

    }


	@RequestMapping("/list_all")
	@ResponseBody
	public Result listData(){
	    return getSuccessResult(rolepermissionService.selectRolePermissionList()) ;
	}
	
	@RequestMapping("list_one")
	@ResponseBody
	public Result list_one(Long id){
		RolePermission rolepermission = rolepermissionService.selectById(id);
		return getSuccessResult(rolepermission);
	}
	
	@RequestMapping("create")
	@ResponseBody
	public Result create(@Valid RolePermission rolepermission){
		rolepermissionService.insert(rolepermission);
		return Result.getSuccess();
	}
	
	@RequestMapping("modify")
	@ResponseBody
	public Result modify(@Valid RolePermission rolepermission){
		rolepermissionService.update(rolepermission);
		return Result.getSuccess();
	}
	
	@RequestMapping("delete")
	@ResponseBody
	public Result delete(Long id){
		rolepermissionService.delete(id);
		return Result.getSuccess();
	}

	@RequestMapping("upRolePermision")
     @ResponseBody
     public  Result upRolePermision(Long roleId,long permissionId ,Boolean state){
        //更新角色权限的按钮
        if(state){
            //查询是否有该角色权限，如果是，不做任何操作，如果不是就添加
            RolePermission rp = rolepermissionService.selectByRoleIdAndPermissionId(roleId,permissionId);
            if(rp==null){
                RolePermission rolePermission = new RolePermission();
                rolePermission.setRoleId(roleId);
                rolePermission.setPermissionId(permissionId);
                rolepermissionService.insert(rolePermission);
            }
        }else{
            //选择删除 如果查询有这个角色权限就删除没有就不管
            RolePermission rp = rolepermissionService.selectByRoleIdAndPermissionId(roleId,permissionId);
            if(rp!=null){
                rolepermissionService.delete(rp.getId());
            }
        }
	    return Result.getSuccess();
    }
}
