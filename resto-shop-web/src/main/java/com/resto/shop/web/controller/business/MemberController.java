 package com.resto.shop.web.controller.business;


import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.JOptionPane;

import com.resto.brand.web.model.OrderException;
import com.resto.brand.web.service.OrderExceptionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.brand.core.entity.Result;
import com.resto.brand.core.util.DateUtil;
import com.resto.brand.core.util.JdbcUtils;
import com.resto.brand.core.util.UserOrderExcelUtil;
import com.resto.brand.web.dto.OrderDetailDto;
import com.resto.brand.web.model.DatabaseConfig;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.DatabaseConfigService;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.model.OrderPaymentItem;
import com.resto.shop.web.service.OrderService;

@Controller
@RequestMapping("member")
public class MemberController extends GenericController{
	
	@Resource
	private OrderService orderService;
	
	@Resource
	private BrandService brandService;
	@Resource
	private ShopDetailService shopDetailService;
	
	@Resource
    private OrderExceptionService orderExceptionService;
	
	@Resource
    private DatabaseConfigService databaseConfigService;
	
	@RequestMapping("/myList")
    public String list(HttpServletRequest request){
		request.setAttribute("beginDate",DateUtil.formatDate(new Date(), "yyyy-MM-dd") );
		request.setAttribute("endDate", DateUtil.formatDate(new Date(), "yyyy-MM-dd"));
		return "member/billReport";
    }
	
	//查询当前店铺的所有用户
	@RequestMapping("/userList")
	@ResponseBody
	public Result queryUserData(String brandId,String beginDate,String endDate){
		
		brandId=getCurrentBrandId();
		DatabaseConfig databaseConfig = databaseConfigService.selectByBrandId(brandId);
		JdbcUtils jdbcUtils = new JdbcUtils(databaseConfig.getUsername(), databaseConfig.getPassword(), databaseConfig.getDriverClassName(), databaseConfig.getUrl());
		jdbcUtils.getConnection();
		String sql = "SELECT c.id AS customerId,a.id AS accountId,c.nickname,c.telephone,c.head_photo,c.province,c.city,c.sex,a.remain,AVG(b.order_money) as money,COUNT(b.brand_id) as amount,SUM(b.order_money) as sumMoney FROM	tb_customer c LEFT JOIN tb_account a ON c.account_id = a.id LEFT JOIN tb_order b ON c.id = b.customer_id WHERE c.brand_id = ? and c.regiest_time >= ? and  c.regiest_time <= ? GROUP BY c.id ";
		List<Object> params = new ArrayList<Object>(); 
		params.add(brandId);
		params.add(beginDate);
		params.add(endDate);
		List<Map<String, Object>> resultList = null;
		try {
			resultList = jdbcUtils.findModeResult(sql, params);
			JdbcUtils.close();
			return getSuccessResult(resultList);
		} catch (SQLException e) {
			e.printStackTrace();
			JdbcUtils.close();
			return new Result("查询失败，后台报错了！",false);
		}
	}
	
	 /**
     * 查询当前用户的优惠券 wql
     * 
     * @return
     */
	@RequestMapping("/list_all_shopId")
    @ResponseBody
	public Result list_all_shopId(String customerid){
		customerid="223a0fecba224916aeae800b79602875";
		String brandId=getCurrentBrandId();
		DatabaseConfig databaseConfig=databaseConfigService.selectByBrandId(brandId);
		JdbcUtils jdbcUtils=new JdbcUtils(databaseConfig.getUsername(),databaseConfig.getPassword(), databaseConfig.getDriverClassName(),databaseConfig.getUrl());
		jdbcUtils.getConnection();
		String sql="select c.is_used,c.brand_id,c.shop_detail_id,c.coupon_type,s.name as shopname,cc.coupon_name,cc.name as myname,cc.coupon_validay,cc.use_with_account,c.begin_date,c.end_date,cc.coupon_value,c.begin_time,c.end_time from resto_brand.shop_detail s, shop_manager.tb_coupon c INNER JOIN shop_manager.tb_new_custom_coupon cc where c.brand_id=cc.brand_id and c.customer_id=?";
		List<Object> params=new ArrayList<Object>();
		params.add(customerid);
		List<Map<String,Object>> resultlist=null;
		try {
			resultlist = jdbcUtils.findModeResult(sql, params);
			JdbcUtils.close();
			return getSuccessResult(resultlist);
		} catch (Exception e) {
			e.printStackTrace();
			JdbcUtils.close();
			return new Result("获取失败，后台报错了！",false);	
		}
	}
	
	private Map<String,Object> getResult(String beginDate,String endDate){
		return orderService.selectMoneyAndNumByDate(beginDate,endDate,getCurrentBrandId(),getBrandName(),getCurrentShopDetails());
	}


	@RequestMapping("member_excel")
	@ResponseBody
	public void reportIncome(String beginDate,String endDate,HttpServletRequest request, HttpServletResponse response){

        List<ShopDetail> shopDetailList = getCurrentShopDetails();
        if(shopDetailList==null){
            shopDetailList = shopDetailService.selectByBrandId(getCurrentBrandId());
        }
		//导出文件名
		String fileName = "会员管理列表"+beginDate+"至"+endDate+".xls";
		//定义读取文件的路径
		String path = request.getSession().getServletContext().getRealPath(fileName);
		//定义列
		String[]columns={"customerId","nickname","telephone","province","city","sex","remain","sumMoney","amount","money"};
		//定义数据
		List<OrderPayDto>  result = new LinkedList<>();
		
		Map<String,Object>  resultMap = this.getResult(beginDate, endDate);
		result.addAll((Collection<? extends OrderPayDto>) resultMap.get("shopId"));
		result.add((OrderPayDto) resultMap.get("brandId"));
		
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
		map.put("num", "4");//显示的位置
		map.put("reportTitle", "品牌订单");//表的名字
		map.put("timeType", "yyyy-MM-dd");
		
		String[][] headers = {{"用户ID","25"},{"昵称","25"},{"联系电话","25"},{"省/市","25"},{"城/区","25"},{"性别","25"},{"余额","25"},{"订单总额","25"},{"订单数","25"},{"订单平均金额","25"}};
		//定义excel工具类对象
		ExcelUtil2<OrderPayDto> excelUtil=new ExcelUtil2<OrderPayDto>();
		try{
			OutputStream out = new FileOutputStream(path);
			excelUtil.ExportExcel(headers, columns, result, out, map);
			out.close();
			excelUtil.download(path, response);
			JOptionPane.showMessageDialog(null, "导出成功！");
			log.info("excel导出成功");
		}catch(Exception e){

}
