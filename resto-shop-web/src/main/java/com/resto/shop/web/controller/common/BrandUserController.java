package com.resto.shop.web.controller.common;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.brand.core.entity.Result;
import com.resto.brand.core.util.ApplicationUtils;
import com.resto.brand.web.model.BrandUser;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.service.BrandUserService;
import com.resto.brand.web.service.RoleService;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.shop.web.config.SessionKey;
import com.resto.shop.web.controller.GenericController;

/**
 * 商家用户控制器
 **/
@Controller
@RequestMapping(value = "/branduser")
public class BrandUserController extends GenericController{
	
    @Resource
    private BrandUserService brandUserService;
    
    @Resource
    private ShopDetailService shopDetailService;

    @Resource
    private RoleService roleService;
    
    /**
     * 用户登录
     * 
     * @param brandUser
     * @param result
     * @return
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(@Valid BrandUser brandUser, BindingResult result, Model model, HttpServletRequest request,String redirect) {
        try {
        	if(redirect == null){
        		redirect = "";
        	}
            Subject subject = SecurityUtils.getSubject(); //获取shiro管理的用户对象 主要储存了用户的角色和用户的权限
            // 已登陆则 跳到首页
            if (subject.isAuthenticated()) {
                return "redirect:/"+redirect;
            }
            if (result.hasErrors()) {
                model.addAttribute("error", "参数错误！");
                return "login";
            }
            // 身份验证
            subject.login(new UsernamePasswordToken(brandUser.getUsername(),ApplicationUtils.pwd( brandUser.getPassword())));
            // 验证成功在Session中保存用户信息
            final BrandUser authUserInfo = brandUserService.selectByUsername(brandUser.getUsername());
            
            
            ShopDetail shopDetail = shopDetailService.selectById(authUserInfo.getShopDetailId());
            
            HttpSession session = request.getSession();
            session.setAttribute(SessionKey.USER_INFO, authUserInfo);
            session.setAttribute(SessionKey.CURRENT_BRAND_ID,authUserInfo.getBrandId());
            session.setAttribute(SessionKey.CURRENT_SHOP_ID,authUserInfo.getShopDetailId());
            session.setAttribute(SessionKey.CURRENT_SHOP_NAME, shopDetail.getName());
            
            
        } catch (AuthenticationException e) {
            // 身份验证失败
            model.addAttribute("error", e.getMessage());
            return "login";
        }
        return "redirect:/"+redirect;
    }

    /**
     * 用户登出
     * 
     * @param session
     * @return
     */
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(HttpSession session) {
        session.invalidate();
        // 登出操作
        Subject subject = SecurityUtils.getSubject();
        subject.logout();
        return "login";
    }
    
    /**
     * 显示修改用户信息页面
     */
    @RequestMapping("/updatepage")
    public void updatepage(){
    }
    
    @RequestMapping("/updatePwd")
    @ResponseBody
    public Result updatePwd(String password){
    	brandUserService.updatePwd(getCurrentUserId(), password);
    	return getSuccessResult();
    }
}
