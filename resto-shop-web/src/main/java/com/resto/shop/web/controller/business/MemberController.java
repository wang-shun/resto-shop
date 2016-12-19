 package com.resto.shop.web.controller.business;


import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.brand.core.entity.Result;
import com.resto.brand.core.util.ExcelUtil;
import com.resto.brand.core.util.JdbcUtils;
import com.resto.brand.web.dto.OrderDetailDto;
import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.DatabaseConfigService;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.model.OrderPaymentItem;
import com.resto.shop.web.service.NewCustomCouponService;
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
    public void list(){
		
		return "member/orderReport";
	}
	
	/**
	 * yjuany 一个用户有多个订单
	 */
	@RequestMapping("orderReport")
	public @ResponseBody List<Map<String,Object>> showUserOrder(String customerId){
		//订单状态
	  List<OrderDetailDto> listDto = new ArrayList<>();//订单状态掉用卷神大哥的方法
		
	  
       List<Map<String,Object>> listMap = new ArrayList<>();
	   List<Order> orderList = orderService.getCustomerOrderList(customerId);
		 for (Order order : orderList) {
			 OrderDetailDto ot = new OrderDetailDto(order.getId(), order.getShopName(), order.getCreateTime(), "", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,"", "", "","","");
			if(order.getCustomer()!=null){//判断是否有这个用户
				//手机号
				if(order.getCustomer().getTelephone()!=null&&order.getCustomer().getTelephone()!=""){
					ot.setTelephone(order.getCustomer().getTelephone());
				}
			}
			  
			if(order.getOrderState()!=null){
				switch (order.getOrderState()) {
				case 1:
					ot.setOrderState("未支付");
					break;
				case 2:
					if(order.getProductionStatus()==0){
						ot.setOrderState("已付款");
					}else if(order.getProductionStatus()==2){
						ot.setOrderState("已消费");
					}else if(order.getProductionStatus()==5){
						ot.setOrderState("异常订单");
					}
					break;
				case 9:
					ot.setOrderState("已取消");
					break;
				case 10:
					if(order.getProductionStatus()==5){
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
			
			if(null!=order.getAppraise()){
				switch (order.getAppraise().getLevel()) {
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
			if(order.getOrderPaymentItems()!=null){
				if(!order.getOrderPaymentItems().isEmpty()){
					for(OrderPaymentItem oi : order.getOrderPaymentItems()){
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
                                case 8:
                                    ot.setWaitRedPay(oi.getPayValue());
							default:
								break;
							}
						}
					}
				}
			}
			//设置营销撬动率  实际/虚拟
			BigDecimal real = ot.getChargePay().add(ot.getWeChatPay());//充值金额支付+微信支付
			BigDecimal temp = order.getOrderMoney().subtract(real);  //subtract 减法 (order.getOrderMoney()-real)
			String incomPrize = "";
			if(temp.compareTo(BigDecimal.ZERO)>0){//temp>0
				incomPrize = real.divide(temp,2,BigDecimal.ROUND_HALF_UP)+"";//除法   （temp/2）
			}
			ot.setIncomePrize(incomPrize);
			//订单金额
			ot.setOrderMoney(order.getOrderMoney());
			listDto.add(ot);
			
			  Map<String,Object> map = new HashMap<>();
			   map.put("shopName", order.getShopName());
			   map.put("beginTime", ot.getBeginTime() );
			   map.put("telephone", ot.getTelephone());
			   map.put("orderMoney", ot.getOrderMoney());//订单金额
			   map.put("weChatPay", ot.getWeChatPay());//微信支付
			   map.put("accountPay", ot.getAccountPay());//红包支付
			   map.put("couponPay", ot.getCouponPay());
			   map.put("chargePay", ot.getChargePay());//充值金额支付
			   map.put("rewardPay", ot.getRewardPay());///充值赠送支付
			   map.put("incomePrize", 1);//营销撬动率
			   map.put("level", ot.getLevel());//评价
			   map.put("orderState", ot.getOrderState());//订单状态
			   listMap.add(map);
			
			
		 }
		
	  
		
    
		return listMap;
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
	
	private Map<String,Object> getResult(String beginDate,String endDate){
		return orderService.selectMoneyAndNumByDate(beginDate,endDate,getCurrentBrandId(),getBrandName(),getCurrentShopDetails());
	}
//	
//	
//	//下载品牌订单报表
//	
	@SuppressWarnings("unchecked")
	@RequestMapping("member_excel")
	@ResponseBody
	public void reportIncome(String beginDate,String endDate,HttpServletRequest request, HttpServletResponse response){

}
