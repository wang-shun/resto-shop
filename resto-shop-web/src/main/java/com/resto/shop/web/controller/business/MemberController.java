 package com.resto.shop.web.controller.business;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.resto.brand.core.entity.Result;
 import com.resto.brand.core.util.ExcelUtil;
 import com.resto.brand.core.util.UserOrderExcelUtil;
import com.resto.brand.web.dto.MemberUserDto;
 import com.resto.brand.web.dto.OrderDetailDto;
 import com.resto.brand.web.model.ShopDetail;
 import com.resto.brand.web.service.BrandService;
 import com.resto.brand.web.service.DatabaseConfigService;
 import com.resto.brand.web.service.OrderExceptionService;
 import com.resto.brand.web.service.ShopDetailService;
 import com.resto.shop.web.controller.GenericController;
 import com.resto.shop.web.model.Coupon;
 import com.resto.shop.web.model.CouponDto;
 import com.resto.shop.web.model.Order;
 import com.resto.shop.web.model.OrderPaymentItem;
 import com.resto.shop.web.service.CouponService;
 import com.resto.shop.web.service.CustomerService;
 import com.resto.shop.web.service.OrderService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;

 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.swing.*;
 import java.io.FileOutputStream;
 import java.io.OutputStream;
 import java.math.BigDecimal;
 import java.util.ArrayList;
import java.util.HashMap;
 import java.util.List;
 import java.util.Map;

@Controller
@RequestMapping("member")
public class MemberController extends GenericController{
	
	@Resource
	private CustomerService customerService;

	@Resource
	private ShopDetailService shopDetailService;

	@Autowired
	private CouponService couponService;

	
	@RequestMapping("/myList")
	public void list(){
	}
	
	//查询当前店铺的所有用户
	@RequestMapping("/myConList")
	@ResponseBody
	public Result selectAllMath(String beginDate,String endDate){
	    JSONObject object = new JSONObject();
	    try {
            //得到品牌用户信息
            String count = customerService.selectBrandUser();
            String[] counts = new String[0];
            if (StringUtils.isNotBlank(count)) {
                counts = count.split(",");
            }
            Integer customerCount = 0;
            Integer registeredCustomerCount = 0;
            Integer unregisteredCustomerCount = 0;
            Integer maleCustomerCount = 0;
            Integer femaleCustomerCount = 0;
            Integer unknownCustomerCount = 0;
            Integer i = 0;
            for (String str : counts) {
                switch (i) {
                    case 0:
                        customerCount = Integer.valueOf(str);
                        break;
                    case 1:
                        registeredCustomerCount = Integer.valueOf(str);
                        break;
                    case 2:
                        unregisteredCustomerCount = Integer.valueOf(str);
                        break;
                    case 3:
                        maleCustomerCount = Integer.valueOf(str);
                        break;
                    case 4:
                        femaleCustomerCount = Integer.valueOf(str);
                        break;
                    case 5:
                        unknownCustomerCount = Integer.valueOf(str);
                        break;
                    default:
                        break;
                }
                i++;
            }
            JSONObject brandCustomerCount = new JSONObject();
            brandCustomerCount.put("brandName", getBrandName());
            brandCustomerCount.put("customerCount", customerCount);
            brandCustomerCount.put("registeredCustomerCount", registeredCustomerCount);
            brandCustomerCount.put("unregisteredCustomerCount", unregisteredCustomerCount);
            brandCustomerCount.put("maleCustomerCount", maleCustomerCount);
            brandCustomerCount.put("femaleCustomerCount", femaleCustomerCount);
            brandCustomerCount.put("unknownCustomerCount", unknownCustomerCount);
            List<MemberUserDto> memberUserDtos = customerService.selectListMemberUser(beginDate, endDate);
            object.put("brandCustomerCount", brandCustomerCount);
            object.put("memberUserDtos", memberUserDtos);
        }catch (Exception e){
            log.error("查询会员信息报表出错！");
            e.printStackTrace();
            return new Result(false);
        }
		return getSuccessResult(object);
	}
	
	 /**
     * 查询当前用户的优惠券 wql
     * 
     * @return
     */
	
	@RequestMapping("/show/billReport")
	public String showModal(String customerId,HttpServletRequest request){
		request.setAttribute("customerId", customerId);
		return "member/billReport";
	}
	
	
	@RequestMapping("/list_all_shopId")
    @ResponseBody
	public Result list_all_shopId(String customerId){
	    JSONObject object = new JSONObject();
	    try {
            List<Coupon> list =  couponService.getListByCustomerId(customerId);
            List<ShopDetail> shopDetails = shopDetailService.selectByBrandId(getCurrentBrandId());
            object.put("coupons",list);
            object.put("shopDetails",shopDetails);
        }catch (Exception e){
            log.error("查看会员优惠卷信息");
            e.printStackTrace();
            return new Result(false);
        }
		return getSuccessResult(object);
	}


	//下载会员信息报表
	@RequestMapping("member_excel")
	@ResponseBody
	public void reportIncome(String userJson,String beginDate,String endDate,HttpServletRequest request, HttpServletResponse response){
        List<ShopDetail> shopDetailList = getCurrentShopDetails();
        if(shopDetailList==null){
            shopDetailList = shopDetailService.selectByBrandId(getCurrentBrandId());
        }
		//导出文件名
		String fileName = "会员管理列表"+beginDate+"至"+endDate+".xls";
		//定义读取文件的路径
		String path = request.getSession().getServletContext().getRealPath(fileName);
		//定义列
		String[]columns={"customerId","nickname","telephone","birthday","province","city","sex","remain","isCharge","chargeRemain","presentRemain","redRemain","sumMoney","amount","money"};
		//定义数据
		List<MemberUserDto>  result = customerService.selectListMemberUser(beginDate, endDate);
		String shopName="";
		for (ShopDetail shopDetail : shopDetailList) {
			shopName += shopDetail.getName()+",";
		}
		Map<String,String> map = new HashMap<>();
		map.put("brandName", getBrandName());
		map.put("shops", shopName);
		map.put("beginDate", beginDate);
		map.put("reportType", "会员信息报表");//表的头，第一行内容
		map.put("endDate", endDate);
		map.put("num", "14");//显示的位置
		map.put("reportTitle", "会员信息");//表的名字
		map.put("timeType", "yyyy-MM-dd");
		
		String[][] headers = {{"用户ID","25"},{"昵称","25"},{"联系电话","25"},{"生日","25"},{"省/市","25"},{"城/区","25"},{"性别","25"},{"余额","25"},{"储值","25"},{"充值金额","25"},{"充值赠送金额","25"},{"红包金额","25"},{"订单总额","25"},{"订单数","25"},{"订单平均金额","25"}};
		//定义excel工具类对象
		ExcelUtil<MemberUserDto> excelUtil=new ExcelUtil<MemberUserDto>();
		try{
			OutputStream out = new FileOutputStream(path);
			excelUtil.ExportExcel(headers, columns, result, out, map);
			out.close();
			excelUtil.download(path, response);
			JOptionPane.showMessageDialog(null, "导出成功！");
			log.info("excel导出成功");
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@RequestMapping("/show/orderReport")
	public String showModal1(String endDate,String beginDate,String customerId,HttpServletRequest request){
		request.setAttribute("beginDate",beginDate);
		request.setAttribute("endDate", endDate);
		request.setAttribute("customerId", customerId);
		return "orderReport/shopReport";
	}
}
