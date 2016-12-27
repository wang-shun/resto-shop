package com.resto.shop.web.controller.business;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.JOptionPane;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.resto.brand.core.entity.Result;
import com.resto.brand.core.util.ExcelUtil;
import com.resto.brand.web.dto.ArticleSellDto;
import com.resto.brand.web.dto.BrandMarketing;
import com.resto.brand.web.model.ShopDetail;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.AccountLog;
import com.resto.shop.web.model.Coupon;
import com.resto.shop.web.model.OrderPaymentItem;
import com.resto.shop.web.service.AccountLogService;
import com.resto.shop.web.service.CouponService;
import com.resto.shop.web.service.OrderPaymentItemService;

@Controller
@RequestMapping("/brandMarketing")
public class BrandMarketingController extends GenericController{

	@Resource
	private AccountLogService accountLogService;
	
	@Resource
	private OrderPaymentItemService orderPaymentItemService;
	
	@Resource
	private CouponService couponService;
	
	@RequestMapping("/list")
	public void list(){}
	
	@RequestMapping("/selectAll")
	@ResponseBody
	public Result list_all(String beginDate, String endDate){
		Result result = new Result();
		try{
			Map<String, String> selectMap = new HashMap<String, String>();
			selectMap.put("beginDate", beginDate);
			selectMap.put("endDate", endDate);
			List<AccountLog> accountLogs = accountLogService.selectAccountLog(selectMap);
			List<OrderPaymentItem> orderPaymentItems = orderPaymentItemService.selectOrderPayMentItem(selectMap);
			List<Coupon> coupons = couponService.selectCoupon(selectMap);
			JSONObject object = new JSONObject();
			object.put("brandName", getBrandName());
			object.put("plRedMoney", 0);
			object.put("czRedMoney", 0);
			object.put("fxRedMoney", 0);
			object.put("dwRedMoney", 0);
			object.put("tcRedMoney", 0);
			object.put("zcCouponMoney", 0);
			object.put("yqCouponMoney", 0);
			BigDecimal redMoneyAll = new BigDecimal(0);
			BigDecimal couponAllMoney = new BigDecimal(0);
			for(AccountLog accountLog : accountLogs){
				if(accountLog.getSource().equals(1)){
					object.put("plRedMoney", accountLog.getMoney());
					redMoneyAll = redMoneyAll.add(accountLog.getMoney());
				}else if(accountLog.getSource().equals(3)){
					object.put("czRedMoney", accountLog.getMoney());
					redMoneyAll = redMoneyAll.add(accountLog.getMoney());
				}else if(accountLog.getSource().equals(4)){
					object.put("fxRedMoney", accountLog.getMoney());
					redMoneyAll = redMoneyAll.add(accountLog.getMoney());
				}
			}
			for(OrderPaymentItem paymentItem : orderPaymentItems){
				if(paymentItem.getPaymentModeId().equals(8)){
					object.put("dwRedMoney", paymentItem.getPayValue());
					redMoneyAll = redMoneyAll.add(paymentItem.getPayValue());
				}else if(paymentItem.getPaymentModeId().equals(11)){
					object.put("tcRedMoney", paymentItem.getPayValue());
					redMoneyAll = redMoneyAll.add(paymentItem.getPayValue());
				}
			}
			object.put("redMoneyAll", redMoneyAll);
			for(Coupon coupon : coupons){
				if(coupon.getCouponType().equals(0)){
					object.put("zcCouponMoney", coupon.getValue());
					couponAllMoney = couponAllMoney.add(coupon.getValue());
				}else if(coupon.getCouponType().equals(1)){
					object.put("yqCouponMoney", coupon.getValue());
					couponAllMoney = couponAllMoney.add(coupon.getValue());
				}
			}
			object.put("couponAllMoney", couponAllMoney);
			return getSuccessResult(object);
		}catch (Exception ex) {
			log.error(ex.getMessage()+"查询营销报表出错!");
			log.debug("查询营销报表出错!");
			result.setSuccess(false);
		}
		return result;
	}
	
	
	@RequestMapping("/downloadBrandExcel")
	@ResponseBody
	public void downloadBrandExcel(String brandJson, String beginDate, String endDate, HttpServletRequest request, HttpServletResponse response){
		//导出文件名
		String fileName = "品牌营销报表"+beginDate+"至"+endDate+".xls";
		//定义读取文件的路径
		String path = request.getSession().getServletContext().getRealPath(fileName);
		//定义数据
		List<BrandMarketing> result = new ArrayList<BrandMarketing>();
		result.add(JSON.parseObject(brandJson, BrandMarketing.class));
		//定义列
		String[]columns={"brandName","redMoneyAll","plRedMoney","czRedMoney","fxRedMoney","dwRedMoney","tcRedMoney","couponAllMoney","zcCouponMoney","yqCouponMoney"};
		//定义一个map用来存数据表格的前四项 1.报表类型,2.品牌名称,3.店铺名称4.日期
		Map<String,String> map = new HashMap<>();
		String shopName="";
		for (ShopDetail shopDetail : getCurrentShopDetails()) {
			shopName += shopDetail.getName()+",";
		}
		//去掉最后一个逗号
		shopName.substring(0, shopName.length()-1);
		map.put("brandName", getBrandName());
		map.put("shops", shopName);
		map.put("beginDate", beginDate);
		map.put("reportType", "品牌营销报表");//表的头,第一行内容
		map.put("endDate", endDate);
		map.put("num", "9");//显示的位置
		map.put("reportTitle", "品牌营销报表");//表的名字
		map.put("timeType", "yyyy-MM-dd");
		
		String[][] headers = {{"品牌名称","25"},{"红包总额(元)","25"},{"评论红包(元)","25"},{"充值赠送红包(元)","25"},{"分享返利红包(元)","25"},{"等位红包(元)","25"},{"退菜红包(元)","25"},{"优惠券总额(元)","25"},{"注册优惠券(元)","25"},{"邀请优惠券(元)","25"}};
		
		//定义excel工具类对象
		ExcelUtil<BrandMarketing> excelUtil=new ExcelUtil<BrandMarketing>();
		try{
			OutputStream out = new FileOutputStream(path);
			excelUtil.ExportExcel(headers, columns, result, out, map);
			out.close();
			excelUtil.download(path, response);
			JOptionPane.showMessageDialog(null, "导出成功！");
			log.debug("excel导出成功");
		}catch(Exception e){
			JOptionPane.showMessageDialog(null, "导出失败！");
			log.error(e.getMessage()+"excel导出失败");
			e.printStackTrace();
		}
	}
}
