 package com.resto.shop.web.controller.business;

 import com.resto.brand.core.entity.Result;
 import com.resto.brand.core.util.ExcelUtil;
 import com.resto.brand.core.util.JdbcUtils;
 import com.resto.brand.core.util.UserOrderExcelUtil;
 import com.resto.brand.web.dto.MemberUserDto;
 import com.resto.brand.web.dto.OrderDetailDto;
 import com.resto.brand.web.model.DatabaseConfig;
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
 import java.sql.SQLException;
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
	private OrderService orderService;
	
	@Resource
	private BrandService brandService;
	@Resource
	private ShopDetailService shopDetailService;
	
	@Resource
    private OrderExceptionService orderExceptionService;
	
	@Resource
    private DatabaseConfigService databaseConfigService;

	@Autowired
	private CouponService couponService;

	
	@RequestMapping("/myList")
	public void list(){
	}
	
	//查询当前店铺的所有用户
	@RequestMapping("/userList")
	@ResponseBody
	public Result queryUserData(String brandId,String beginDate,String endDate){
		
		brandId=getCurrentBrandId();
		System.out.println(brandId);
//		将得到的时间格式化
//		因为格式化的时间是当天的00.00.00点，所以加上一天时间86400000毫秒
		DatabaseConfig databaseConfig = databaseConfigService.selectByBrandId(brandId);
		JdbcUtils jdbcUtils = new JdbcUtils(databaseConfig.getUsername(), databaseConfig.getPassword(), databaseConfig.getDriverClassName(), databaseConfig.getUrl());
		jdbcUtils.getConnection();
		String sql = "SELECT c.id AS customerId,a.id AS accountId,c.nickname,c.telephone,c.is_bind_phone as isBindPhone,c.head_photo,c.province,c.city,c.sex,a.remain,AVG(b.order_money) as money,COUNT(b.brand_id) as amount,SUM(b.order_money) as sumMoney FROM	tb_customer c LEFT JOIN tb_account a ON c.account_id = a.id LEFT JOIN tb_order b ON c.id = b.customer_id WHERE c.brand_id = ?  GROUP BY c.id ";
		List<Object> params = new ArrayList<Object>(); 
		params.add(brandId);
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
	
	@RequestMapping("/show/billReport")
	public String showModal(String customerId,HttpServletRequest request){
		request.setAttribute("customerId", customerId);
		System.out.println(customerId);
		return "member/billReport";
	}
	
	
	@RequestMapping("/list_all_shopId")
    @ResponseBody
	public Result list_all_shopId(String customerId){
		List<Coupon> list =  couponService.getListByCustomerId(customerId);
		List<ShopDetail> shopDetails = shopDetailService.selectByBrandId(getCurrentBrandId());
		CouponDto couponDto = new CouponDto();
		couponDto.setCoupons(list);
		couponDto.setShopDetails(shopDetails);
		return getSuccessResult(couponDto);
//		String brandId=getCurrentBrandId();
//		System.out.println("customerId"+customerId);
//		DatabaseConfig databaseConfig=databaseConfigService.selectByBrandId(brandId);
//		JdbcUtils jdbcUtils=new JdbcUtils(databaseConfig.getUsername(),databaseConfig.getPassword(), databaseConfig.getDriverClassName(),databaseConfig.getUrl());
//		jdbcUtils.getConnection();
//		String sql="select c.is_used,c.brand_id,c.shop_detail_id,c.coupon_type,s.name as shopname,cc.coupon_name,cc.name as myname,cc.coupon_validay,cc.use_with_account,c.begin_date,c.end_date,cc.coupon_value,c.begin_time,c.end_time from resto_brand.shop_detail s, shop_manager.tb_coupon c INNER JOIN shop_manager.tb_new_custom_coupon cc where c.brand_id=cc.brand_id and c.customer_id=?";
//		List<Object> params=new ArrayList<Object>();
//		params.add(customerId);
//		List<Map<String,Object>> resultlist=null;
//		try {
//			resultlist = jdbcUtils.findModeResult(sql, params);
//			JdbcUtils.close();
//			return getSuccessResult(resultlist);
//		} catch (Exception e) {
//			e.printStackTrace();
//			JdbcUtils.close();
//			return new Result("获取失败，后台报错了！",false);
//		}
	}


	//下载会员信息报表
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
		List<MemberUserDto>  result = customerService.selectListMemberUser(beginDate, endDate, getCurrentBrandId());
		
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
		map.put("num", "9");//显示的位置
		map.put("reportTitle", "会员信息");//表的名字
		map.put("timeType", "yyyy-MM-dd");
		
		String[][] headers = {{"用户ID","25"},{"昵称","25"},{"联系电话","25"},{"省/市","25"},{"城/区","25"},{"性别","25"},{"余额","25"},{"订单总额","25"},{"订单数","25"},{"订单平均金额","25"}};
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
//		request.setAttribute("beginDate",beginDate );
//		request.setAttribute("endDate", endDate);
		request.setAttribute("customerId", customerId);
		return "member/orderReport";
	}
	
	/**
	 * yjuany 一个用户有多个订单
	 */
	@RequestMapping("orderReport")
	@ResponseBody
	public  List<OrderDetailDto> showUserOrder(String beginDate,String endDate,String customerId,HttpServletRequest request){
		//#
		//订单状态
	   List<OrderDetailDto> listDto = new ArrayList<>(); 
	   
	   //从session中获取该品牌的信息（这里只需要店铺名称）
       List<ShopDetail> shopDetailList = getCurrentShopDetails();//放到session中的
       if(shopDetailList==null){
    	   shopDetailList = shopDetailService.selectByBrandId(getCurrentBrandId());
       }
       
     
      // 用户id='f81d4fe246614796be302fa7593bef1b' and  o.create_time BETWEEN  "2016-12-06"   and "2016-12-12"
	   List<Order> orderList = orderService.getCustomerOrderList(customerId,beginDate,endDate);
	    if(orderList.size()>0){
		 for (Order order : orderList) {
			 OrderDetailDto ot = new OrderDetailDto(order.getId(), order.getShopName(), order.getCreateTime(), "", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,"", "", "","","");
		    //查询店铺名称
			 
	               for (ShopDetail shopDetail : shopDetailList) {///////看下session有
	  				 if(order.getShopDetailId().equals(shopDetail.getId())){
	  					 ot.setShopDetailId(order.getShopDetailId());
	  					 ot.setShopName(shopDetail.getName());
	  				 }
	  			  }
			  
			  
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
					ot.setLevel("——");
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
			//店铺名字
			 listDto.add(ot);
		
			
		 }
		
	    }
		
    
		return listDto;
    }
	
	 /**
	  * 下载用户订单列表
	  * @param beginDate
	  * @param endDate
	  * @param customerId 用户id
	  * @param request
	  * @param response
	  */
	
		@RequestMapping("usershop_excel")
		@ResponseBody
		public void reportUserOrder(String beginDate,String endDate,String customerId,HttpServletRequest request, HttpServletResponse response){
			//导出文件名
			String fileName = "用户订单列表"+beginDate+"至"+endDate+".xls";
			//定义读取文件的路径
			String path = request.getSession().getServletContext().getRealPath(fileName);
			//定义列（导出的行数）
			String[]columns={"shopName","begin","telephone","orderMoney","weChatPay","accountPay","couponPay","chargePay","rewardPay","waitRedPay","incomePrize","level","orderState"};
			//定义数据
			List<OrderDetailDto>  UserOrderResult = this.showUserOrder(beginDate, endDate, customerId, request);
			//获取店铺名称
			//ShopDetail s = shopDetailService.selectById(shopId);
			//excel最顶不的内容
			Map<String,String> map = new HashMap<>();
			map.put("beginDate", beginDate);
			map.put("reportType", "用户订单报表");//表的头，第一行内容
			map.put("endDate", endDate);
			map.put("num", "11");//显示的位置
			map.put("reportTitle", "用户订单");//表的名字
			map.put("timeType", "yyyy-MM-dd");
			
			String[][] headers = {{"店铺名","25"},{"下单时间","25"},{"手机号","25"},{"订单金额(元)","25"},{"微信支付(元)","25"},{"红包支付(元)","25"},{"优惠券支付(元)","25"},{"充值金额支付(元)","25"},{"充值赠送金额支付(元)","25"},{"等位红包支付","25"},{"营销撬动率","25"},{"评价","25"},{"订单状态","25"}};
			
			
			//定义excel工具类对象
			UserOrderExcelUtil<OrderDetailDto> excelUtil=new UserOrderExcelUtil<OrderDetailDto>();//OrderDetailDto订单扩展类（showUserOrder）
			try{
				OutputStream out = new FileOutputStream(path);//导出 （ 输出流  ）路径path:E:\resto-shop\resto-shop-web\src\main\webapp\用户订单列表2016-1-1至2016-12-20.xls
				excelUtil.ExportExcel(headers, columns, UserOrderResult, out, map);
				out.close();
				excelUtil.download(path, response);
				JOptionPane.showMessageDialog(null, "导出成功！");
				log.info("excel导出成功");
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	
	

       /**
        * 
        * 订单详情
        * @param orderId
        * @return
        */
		@RequestMapping("detailInfo")
		@ResponseBody
		public Result showDetail(String orderId){
			Order o = orderService.selectOrderDetails(orderId);
			List<Order> childList = orderService.selectListByParentId(orderId);//是否为套餐
			if(!childList.isEmpty()){
	                o.setChildList(childList);
	        }
           
		
			return getSuccessResult(o);
		}

}
