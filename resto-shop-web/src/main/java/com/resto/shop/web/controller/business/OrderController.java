 package com.resto.shop.web.controller.business;


import java.io.FileOutputStream;
import java.io.OutputStream;
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

import com.resto.brand.core.entity.Result;
import com.resto.brand.core.util.ExcelUtil;
import com.resto.brand.web.dto.OrderPayDto;
import com.resto.brand.web.model.Brand;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.service.OrderService;

@Controller
@RequestMapping("orderReport")
public class OrderController extends GenericController{
	
	@Resource
	private OrderService orderService;
	
	@Resource
	private BrandService brandService;
	@Resource
	private ShopDetailService shopDetailService;
	
	@RequestMapping("/list")
    public void list(){
    }
	
	//查询已消费订单的订单份数和订单金额
	@ResponseBody
	@RequestMapping("brand_data")
	public List<OrderPayDto> selectMoneyAndNumByDate(String beginDate,String endDate){
		return this.getResult(beginDate, endDate);
	}
	
	private List<OrderPayDto> getResult(String beginDate,String endDate){
		return orderService.selectMoneyAndNumByDate(beginDate,endDate,getCurrentBrandId());
	}
	
	
	//下载品牌订单报表
	
	@RequestMapping("brand_excel")
	@ResponseBody
	public void reportIncome(String beginDate,String endDate,HttpServletRequest request, HttpServletResponse response){
		//导出文件名
		String fileName = "品牌订单列表"+beginDate+"至"+endDate+".xls";
		//定义读取文件的路径
		String path = request.getSession().getServletContext().getRealPath(fileName);
		//定义列
		String[]columns={"shopName","number","orderMoney","average"};
		//定义数据
		List<OrderPayDto> result = this.getResult(beginDate, endDate);
		Brand brand = brandService.selectById(getCurrentBrandId());
		//获取店铺名称
		List<ShopDetail> shops = shopDetailService.selectByBrandId(getCurrentBrandId());
		String shopName="";
		for (ShopDetail shopDetail : shops) {
			shopName += shopDetail.getName()+",";
		}
		Map<String,String> map = new HashMap<>();
		map.put("brandName", brand.getBrandName());
		map.put("shops", shopName);
		map.put("beginDate", beginDate);
		map.put("reportType", "品牌订单报表");//表的头，第一行内容
		map.put("endDate", endDate);
		map.put("num", "3");//显示的位置
		map.put("reportTitle", "品牌订单");//表的名字
		map.put("timeType", "yyyy-MM-dd");
		
		String[][] headers = {{"店铺","25"},{"已消费订单数(份)","25"},{"已消费订单金额(元)","25"},{"订单平均金额(元)","25"}};
		//定义excel工具类对象
		ExcelUtil<OrderPayDto> excelUtil=new ExcelUtil<OrderPayDto>();
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
	
	
	@RequestMapping("/show/shopReport")
	public String showModal(String beginDate,String endDate,String shopId,HttpServletRequest request){
		request.setAttribute("beginDate", beginDate);
		request.setAttribute("endDate", endDate);
		ShopDetail shop = shopDetailService.selectById(shopId);
		if(shopId!=null){
			request.setAttribute("shopId", shopId);
			request.setAttribute("shopName", shop.getName());
		}
		return "orderReport/shopReport";
	}
	
	@RequestMapping("AllOrder")
	@ResponseBody
	public List<Order> selectAllOrder(String beginDate,String endDate,String shopId){
		//return orderService.selectListByTime(beginDate,endDate,shopId);
		//查询店铺名称
		ShopDetail shop = shopDetailService.selectById(shopId);
		
		List<Order> list = orderService.selectListByTime(beginDate,endDate,shopId);
		for (Order order : list) {
			order.setShopName(shop.getName());
			if(order.getTelephone()==null){
				order.setTelephone("");
			}
		}
		return list;
	}
	
	@RequestMapping("detailInfo")
	@ResponseBody
	public Result showDetail(String orderId){
		Order o = orderService.selectOrderDetails(orderId);
		return getSuccessResult(o);
	}
	
	
	
	
}
