 package com.resto.shop.web.controller.business;


import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
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
import com.resto.brand.web.dto.OrderDetailDto;
import com.resto.brand.web.dto.OrderPayDto;
import com.resto.brand.web.model.Brand;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.model.OrderPaymentItem;
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
	public Map<String,Object> selectMoneyAndNumByDate(String beginDate,String endDate){
		
		return this.getResult(beginDate, endDate);
	}
	
	private Map<String,Object> getResult(String beginDate,String endDate){
		return orderService.selectMoneyAndNumByDate(beginDate,endDate,getCurrentBrandId());
	}
	
	
	//下载品牌订单报表
	
	@SuppressWarnings("unchecked")
	@RequestMapping("brand_excel")
	@ResponseBody
	public void reportIncome(String beginDate,String endDate,HttpServletRequest request, HttpServletResponse response){
		//导出文件名
		String fileName = "品牌订单列表"+beginDate+"至"+endDate+".xls";
		//定义读取文件的路径
		String path = request.getSession().getServletContext().getRealPath(fileName);
		//定义列
		String[]columns={"name","number","orderMoney","average","marketPrize"};
		//定义数据
		//List<OrderPayDto> result = this.getResult(beginDate, endDate);
		List<OrderPayDto>  result = new LinkedList<>();
		
		Map<String,Object>  resultMap = this.getResult(beginDate, endDate);
		result.addAll((Collection<? extends OrderPayDto>) resultMap.get("shopId"));
		result.add((OrderPayDto) resultMap.get("brandId"));
		
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
		map.put("num", "4");//显示的位置
		map.put("reportTitle", "品牌订单");//表的名字
		map.put("timeType", "yyyy-MM-dd");
		
		String[][] headers = {{"名称","25"},{"订单总数(份)","25"},{"订单金额(元)","25"},{"订单平均金额(元)","25"},{"营销撬动率","25"}};
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
	public List<OrderDetailDto> selectAllOrder(String beginDate,String endDate,String shopId){
		
		return this.listResult(beginDate, endDate, shopId);
	}
	
	public List<OrderDetailDto> listResult(String beginDate,String endDate,String shopId){
		//return orderService.selectListByTime(beginDate,endDate,shopId);
				//查询店铺名称
				ShopDetail shop = shopDetailService.selectById(shopId);
				
				List<OrderDetailDto> listDto = new ArrayList<>();
				
			
				List<Order> list = orderService.selectListByTime(beginDate,endDate,shopId);
				for (Order o : list) {
					OrderDetailDto ot = new OrderDetailDto(o.getId(),o.getShopDetailId(), shop.getName(), o.getCreateTime(), "", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "", "", "");			
					
					if(o.getCustomer()!=null){
						//手机号
						if(o.getCustomer().getTelephone()!=null&&o.getCustomer().getTelephone()!=""){
							ot.setTelephone(o.getCustomer().getTelephone());
						}
					}
					
					//订单状态
					if(o.getOrderState()!=null){
						switch (o.getOrderState()) {
						case 1:
							ot.setOrderState("未支付");
							break;
						case 2:
							if(o.getProductionStatus()==0){
								ot.setOrderState("已付款");
							}else if(o.getProductionStatus()==2){
								ot.setOrderState("已打印");
							}else if(o.getProductionStatus()==5){
								ot.setOrderState("异常订单");
							}
							break;
						case 9:
							ot.setOrderState("已取消");
							break;
						case 10:
							if(o.getProductionStatus()==5){
								ot.setOrderState("异常订单");
							}else {
								ot.setOrderState("已消费");
							}
							break;
						case 11:
							ot.setOrderState("已评价");
							break;
						case 12:
							ot.setOrderState("已分享");
							break;

						default:
							break;
						}
						
						
					}
					
					
					//订单评价
					//判断是否为空，不是所有订单都评价
					
					if(null!=o.getAppraise()){
						switch (o.getAppraise().getLevel()) {
						case 1:
							ot.setLevel("一星");
							break;
						case 2:
							ot.setLevel("二星");
							break;
						case 3:
							ot.setLevel("三星");
							break;
						case 4:
							ot.setLevel("四星");
							break;
						case 5:
							ot.setLevel("五星");
							break;

						default:
							break;
						}
						
					}
				
						
					//订单支付
					
					if(o.getOrderPaymentItems()!=null){
						
						if(!o.getOrderPaymentItems().isEmpty()){
							

							for(OrderPaymentItem oi : o.getOrderPaymentItems()){
								
								if(null!=oi.getPaymentModeId()){
									switch (oi.getPaymentModeId()) {
									case 1:
										ot.setWeChatPay(oi.getPayValue());
										break;
									case 2:
										ot.setAccountPay(oi.getPayValue());
										break;
									case 3:
										ot.setCouponPay(oi.getPayValue());
										break;
									case 6:
										ot.setChargePay(oi.getPayValue());
										break;
									case 7:
										ot.setRewardPay(oi.getPayValue());
										break;
									default:
										break;
									}
								}
								
							}
						
							
						}
							
						
					}
					//设置营销撬动率  (订单金额-实际支付金额)/实际支付的金额
					
					BigDecimal real = ot.getChargePay().add(ot.getWeChatPay());
					
					BigDecimal temp = o.getOrderMoney().subtract(real);
					
					String incomPrize = "";
					
					if(temp.compareTo(BigDecimal.ZERO)!=0){
						incomPrize = real.divide(temp,2,BigDecimal.ROUND_HALF_UP)+"";
					}
					
					ot.setIncomePrize(incomPrize);
					
					//订单金额
					ot.setOrderMoney(o.getOrderMoney());
					
					listDto.add(ot);
					
				}
				return listDto;
	}
	
	
	
	@RequestMapping("detailInfo")
	@ResponseBody
	public Result showDetail(String orderId){
		Order o = orderService.selectOrderDetails(orderId);
		return getSuccessResult(o);
	}
	
	//下载店铺订单列表
	
	@RequestMapping("shop_excel")
	@ResponseBody
	public void reportOrder(String beginDate,String endDate,String shopId,HttpServletRequest request, HttpServletResponse response){
		//导出文件名
		String fileName = "店铺订单列表"+beginDate+"至"+endDate+".xls";
		//定义读取文件的路径
		String path = request.getSession().getServletContext().getRealPath(fileName);
		//定义列
		String[]columns={"shopName","begin","telephone","orderMoney","weChatPay","accountPay","couponPay","chargePay","rewardPay","incomePrize","level","orderState"};
		//定义数据
		List<OrderDetailDto> result = this.listResult(beginDate, endDate, shopId);
		Brand brand = brandService.selectById(getCurrentBrandId());
		//获取店铺名称
		ShopDetail s = shopDetailService.selectById(shopId);
		Map<String,String> map = new HashMap<>();
		map.put("brandName", brand.getBrandName());
		map.put("shops", s.getName());
		map.put("beginDate", beginDate);
		map.put("reportType", "店铺订单报表");//表的头，第一行内容
		map.put("endDate", endDate);
		map.put("num", "11");//显示的位置
		map.put("reportTitle", "品牌订单");//表的名字
		map.put("timeType", "yyyy-MM-dd");
		
		String[][] headers = {{"店铺","25"},{"下单时间","25"},{"手机号","25"},{"订单金额(元)","25"},{"微信支付(元)","25"},{"红包支付(元)","25"},{"优惠券支付(元)","25"},{"充值金额支付(元)","25"},{"充值赠送金额支付(元)","25"},{"营销撬动率","25"},{"评价","25"},{"订单状态","25"}};
		//定义excel工具类对象
		ExcelUtil<OrderDetailDto> excelUtil=new ExcelUtil<OrderDetailDto>();
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



	@RequestMapping("/refund")
	public void refund(String orderId){
		orderService.cancelOrder(orderId);
	}
	
}
