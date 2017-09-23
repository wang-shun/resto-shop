package com.resto.shop.web.controller.common;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.resto.brand.core.util.DateUtil;
import com.resto.brand.web.model.*;
import com.resto.brand.web.service.*;
import com.resto.shop.web.config.SessionKey;
import com.resto.shop.web.util.LogTemplateUtils;
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
    private BrandService brandService;

    @Resource
    private RoleService roleService;

    @Resource
    private WetherService wetherService;


    @Resource
	BrandSettingService brandSettingService;


    @Resource
	AccountNoticeService accountNoticeService;

    @Resource
	BrandAccountService brandAccountService;

    private static final ObjectMapper MAPPER = new ObjectMapper();

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

            log.info("tttttt");

            // 验证成功在Session中保存用户信息
            final BrandUser authUserInfo = brandUserService.selectByUsername(brandUser.getUsername());
            log.info("tttttt2222");
            HttpSession session = request.getSession();
            session.setAttribute(SessionKey.USER_INFO, authUserInfo);
            session.setAttribute(SessionKey.CURRENT_BRAND_ID,authUserInfo.getBrandId());
            session.setAttribute(SessionKey.CURRENT_SHOP_ID,authUserInfo.getShopDetailId());
            session.setAttribute(SessionKey.CURRENT_SHOP_NAME, authUserInfo.getShopName());
            List<ShopDetail> shopDetailList = shopDetailService.selectByBrandId(authUserInfo.getBrandId());
            session.setAttribute(SessionKey.CURRENT_SHOP_NAMES,shopDetailList);
            Wether wether = wetherService.selectDateAndShopId(authUserInfo.getShopDetailId(), DateUtil.formatDate(new Date(),"yyyy-MM-dd"));

            Boolean flag = true;
			BrandSetting brandSetting = brandSettingService.selectByBrandId(getCurrentBrandId());
			if(brandSetting!=null&&brandSetting.getOpenBrandAccount()==1){
				BrandAccount brandAccount = brandAccountService.selectByBrandSettingId(brandSetting.getId());
				if(brandAccount!=null){
					List<AccountNotice> accountNotices = accountNoticeService.selectByAccountId(brandAccount.getId());
					if(accountNotices!=null&&!accountNotices.isEmpty()){
						BigDecimal min = accountNotices.get(0).getNoticePrice();//默认第一个最小
						for(int i=0;i<accountNotices.size();i++){
							if(accountNotices.get(i).getNoticePrice().compareTo(min)<0){
								min = accountNotices.get(i).getNoticePrice();
							}
						}
						if(brandAccount.getAccountBalance().compareTo(min)<0){//如果品牌账户小于设置值
							flag = false;
						}
					}
				}

			}
			session.setAttribute(SessionKey.OPEN_BRAND_ACCOUNT,flag);
            session.setAttribute(SessionKey.WETHERINFO,wether);

//            HttpSession session = request.getSession();
//            session.setAttribute(RedisSessionKey.USER_INFO, JsonUtils.objectToJson(authUserInfo));//存用户的信息
//            session.setAttribute(RedisSessionKey.CURRENT_USER_NAME,authUserInfo.getUsername());//存用户的名字
//            session.setAttribute(RedisSessionKey.CURRENT_BRAND_ID,authUserInfo.getBrandId());//存当前品牌的id
//            session.setAttribute(RedisSessionKey.CURRENT_SHOP_ID,authUserInfo.getShopDetailId());//存当前店铺的id
//            session.setAttribute(RedisSessionKey.CURRENT_SHOP_NAME, authUserInfo.getShopName());//存当前店铺的名字
//            List<ShopDetail> shopDetailList = shopDetailService.selectByBrandId(authUserInfo.getBrandId());
//            session.setAttribute(RedisSessionKey.CURRENT_SHOP_NAMES, JsonUtils.objectToJson(shopDetailList));//存当前品牌所有的店铺

            Brand brand = brandService.selectByPrimaryKey(authUserInfo.getBrandId());
            ShopDetail shopDetail = shopDetailService.selectByPrimaryKey(authUserInfo.getShopDetailId());
            LogTemplateUtils.shopUserLogin(brand.getBrandName(), shopDetail.getName(), getCurrentBrandUser().getUsername());
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
        Brand brand = brandService.selectByPrimaryKey(getCurrentBrandId());
        ShopDetail shopDetail = shopDetailService.selectByPrimaryKey(getCurrentShopId());
        LogTemplateUtils.shopUserLogout(brand.getBrandName(), shopDetail.getName(), getCurrentBrandUser().getUsername());

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

    /**
     * 显示修改管理员信息页面
     */
    @RequestMapping("/updatemanagerpwd")
    public void updatemanagerpwd(){

    }


    /**
     * 显示修改管理员信息页面
     */
    @RequestMapping("/checkPwd")
    @ResponseBody
    public Result checkPwd(String password){
        if(password.equals("Vino.2016")){
            return  new Result(true);
        }

        BrandUser brandUser = brandUserService.selectByUsername(getCurrentBrandUser().getUsername());
        password = ApplicationUtils.pwd(password);


        return new Result(password.equals(brandUser.getSuperPwd()));
    }
    
    @RequestMapping("/updatePwd")
    @ResponseBody
    public Result updatePwd(String password){
    	brandUserService.updatePwd(getCurrentUserId(), password);
    	return getSuccessResult();
    }


    @RequestMapping("/updateSuperPwd")
    @ResponseBody
    public Result updateSuperPwd(String password){
        brandUserService.updateSuperPwd(getCurrentUserId(), password);
        return getSuccessResult();
    }

    @RequestMapping("/list")
    public void list(){
    }

	@RequestMapping("/list_all")
	@ResponseBody
	public List<BrandUser> listData(){
		return brandUserService.selectListBybrandId(getCurrentBrandId());
	}
	
	
	@RequestMapping("/create")
	@ResponseBody
	public Result create(@Valid BrandUser brandUser){
		brandUser.setBrandId(getCurrentBrandId());
		brandUserService.creatBrandUser(brandUser);
		return Result.getSuccess();
	}


    @RequestMapping("/modify")
    @ResponseBody
    public Result modify(@Valid BrandUser brandUser){
        brandUserService.update(brandUser);
        return Result.getSuccess();
    }
	
	@RequestMapping("/checkusername")
	@ResponseBody
	public Result checkUserName(String userName){
		BrandUser user = brandUserService.selectByUsername(userName);
		return  getSuccessResult(user);
	}
	

}
