package com.resto.shop.web.controller.business;


import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.JOptionPane;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.resto.brand.core.util.DateUtil;
import com.resto.brand.web.model.DistributionMode;
import com.resto.brand.web.model.OrderException;
import com.resto.brand.web.service.OrderExceptionService;
import com.resto.shop.web.constant.DistributionType;
import com.resto.shop.web.constant.PayMode;
import com.resto.shop.web.model.OrderItem;
import com.resto.shop.web.service.WeItemService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.brand.core.entity.Result;
import com.resto.brand.core.util.ExcelUtil;
import com.resto.brand.web.dto.OrderDetailDto;
import com.resto.brand.web.dto.OrderPayDto;
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

   @Resource
	private WeItemService weItemService;

	@Resource
	private OrderExceptionService orderExceptionService;

	@RequestMapping("/list")
	public void list(){
	}

	//查询已消费订单的订单份数和订单金额
	@ResponseBody
	@RequestMapping("brand_data")
	public Result selectMoneyAndNumByDate(String beginDate,String endDate){
        JSONObject object = new JSONObject();
        try {
            Map<String,Object> resultMap = this.getResult(beginDate, endDate);
            object.put("result",resultMap);
        }catch (Exception e){
            log.error("查看订单报表出错！");
            e.printStackTrace();
            return new Result(false);
        }
		return getSuccessResult(object);
	}

	private Map<String,Object> getResult(String beginDate,String endDate){
		return orderService.selectMoneyAndNumByDate(beginDate,endDate,getCurrentBrandId(),getBrandName(),getCurrentShopDetails());
	}


	//生成品牌订单报表
	@SuppressWarnings("unchecked")
	@RequestMapping("create_brand_excel")
	@ResponseBody
	public Result create_brand_excel(String beginDate,String endDate,OrderPayDto orderPayDto,HttpServletRequest request){

		List<ShopDetail> shopDetailList = getCurrentShopDetails();
		if(shopDetailList==null){
			shopDetailList = shopDetailService.selectByBrandId(getCurrentBrandId());
		}
		//导出文件名
		String fileName = "品牌订单列表"+beginDate+"至"+endDate+".xls";
		//定义读取文件的路径
		String path = request.getSession().getServletContext().getRealPath(fileName);
		//定义列
		String[]columns={"name","number","orderMoney","average","marketPrize"};
		//定义数据
		List<OrderPayDto>  result = new ArrayList<>();
        SimplePropertyPreFilter filter = new SimplePropertyPreFilter();
        filter.getExcludes().add("brandOrderDto");
        filter.getExcludes().add("shopOrderDtos");
        String json = JSON.toJSONString(orderPayDto.getBrandOrderDto(), filter);
        OrderPayDto brandOrderDto = JSON.parseObject(json, OrderPayDto.class);
        result.add(brandOrderDto);
        json = JSON.toJSONString(orderPayDto.getShopOrderDtos(), filter);
        List<OrderPayDto> shopOrderDtos = JSON.parseObject(json, new TypeReference<List<OrderPayDto>>(){});
        result.addAll(shopOrderDtos);
		String shopName="";
		for (ShopDetail shopDetail : shopDetailList) {
			shopName += shopDetail.getName()+",";
		}
        shopName = shopName.substring(0, shopName.length() - 1);
		Map<String,String> map = new HashMap<>();
		map.put("brandName", getBrandName());
		map.put("shops", shopName);
		map.put("beginDate", beginDate);
		map.put("reportType", "品牌订单报表");//表的头，第一行内容
		map.put("endDate", endDate);
		map.put("num", "4");//显示的位置
		map.put("reportTitle", "品牌订单");//表的名字
		map.put("timeType", "yyyy-MM-dd");

		String[][] headers = {{"品牌名称/店铺名称","25"},{"订单总数(份)","25"},{"订单金额(元)","25"},{"订单平均金额(元)","25"},{"营销撬动率","25"}};
		//定义excel工具类对象
		ExcelUtil<OrderPayDto> excelUtil=new ExcelUtil<OrderPayDto>();
		try{
			OutputStream out = new FileOutputStream(path);
			excelUtil.ExportExcel(headers, columns, result, out, map);
			out.close();
		}catch(Exception e){
		    log.error("生成品牌订单报表出错！");
			e.printStackTrace();
            return new Result(false);
		}
		return getSuccessResult(path);
	}

    /**
     * 下载品牌订单报表
     * @param path
     * @param response
     */
	@RequestMapping("/downloadBrandOrderExcel")
	public void downloadBrandOrderExcel(String path, HttpServletResponse response){
        //定义excel工具类对象
        ExcelUtil<OrderPayDto> excelUtil=new ExcelUtil<OrderPayDto>();
	    try {
			excelUtil.download(path, response);
			JOptionPane.showMessageDialog(null, "导出成功！");
			log.info("excel导出成功");
        }catch (Exception e){
            log.error("下载品牌订单报表出错！");
            e.printStackTrace();
        }
    }

	//进入店铺订单报表页面
	@RequestMapping("/show/shopReport")
	public String showModal(String beginDate,String endDate,String shopId,HttpServletRequest request){
		request.setAttribute("beginDate", beginDate);
		request.setAttribute("endDate", endDate);
        request.setAttribute("shopId", shopId);
		return "orderReport/shopReport";
	}


	@RequestMapping("AllOrder")
	@ResponseBody
	public Result selectAllOrder(String beginDate,String endDate,String shopId){
	    JSONObject object = new JSONObject();
        try {
            List<OrderDetailDto> orderDetailDtos = this.listResult(beginDate, endDate, shopId);
            object.put("result",orderDetailDtos);
        }catch (Exception e){
            log.error("查询店铺订单明细出错！");
            e.printStackTrace();
            return new Result(false);
        }
        return getSuccessResult(object);
	}

	public List<OrderDetailDto> listResult(String beginDate,String endDate,String shopId){
		//查询店铺名称
		ShopDetail shop = shopDetailService.selectById(shopId);
		List<OrderDetailDto> listDto = new ArrayList<>();
		List<Order> list = orderService.selectListByTime(beginDate,endDate,shopId);
		for (Order o : list) {
			OrderDetailDto ot = new OrderDetailDto(o.getShopDetailId(),o.getId(),shop.getName(),o.getCreateTime(),"--",BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO
            ,"0",false,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,1,"--","--","--","--");
            ot.setCreateTime(DateUtil.formatDate(o.getCreateTime(),"yyyy-MM-dd HH:mm:ss"));
			if(o.getCustomer()!=null){
				//手机号
				if(StringUtils.isNotBlank(o.getCustomer().getTelephone())){
					ot.setTelephone(o.getCustomer().getTelephone());
				}
			}
			//订单支付
			if(o.getOrderPaymentItems()!=null){
				if(!o.getOrderPaymentItems().isEmpty()){
					for(OrderPaymentItem oi : o.getOrderPaymentItems()){
						if(null!=oi.getPaymentModeId()){
							switch (oi.getPaymentModeId()) {
								case PayMode.WEIXIN_PAY:
									ot.setWeChatPay(ot.getWeChatPay().add(oi.getPayValue()));
									break;
								case PayMode.ACCOUNT_PAY:
									ot.setAccountPay(ot.getAccountPay().add(oi.getPayValue()));
									break;
								case PayMode.COUPON_PAY:
									ot.setCouponPay(ot.getCouponPay().add(oi.getPayValue()));
									break;
								case PayMode.CHARGE_PAY:
									ot.setChargePay(ot.getChargePay().add(oi.getPayValue()));
									break;
								case PayMode.REWARD_PAY:
									ot.setRewardPay(ot.getRewardPay().add(oi.getPayValue()));
									break;
								case PayMode.WAIT_MONEY:
									ot.setWaitRedPay(ot.getWaitRedPay().add(oi.getPayValue()));
								case PayMode.ALI_PAY:
									ot.setAliPayment(ot.getAliPayment().add(oi.getPayValue()));
									break;
                                case PayMode.CRASH_PAY:
                                    ot.setMoneyPay(ot.getMoneyPay().add(oi.getPayValue()));
                                    break;
                                case PayMode.BANK_CART_PAY:
                                    ot.setBackCartPay(ot.getBackCartPay().add(oi.getPayValue()));
                                    break;
								case PayMode.ARTICLE_BACK_PAY:
									ot.setArticleBackPay(ot.getArticleBackPay().add(oi.getPayValue()).abs());
									break;
								default:
									break;
							}
						}
					}
				}
			}
			//设置营销撬动率  实际/虚拟
			BigDecimal real = ot.getChargePay().add(ot.getWeChatPay()).add(ot.getAliPayment()).add(ot.getMoneyPay()).add(ot.getBackCartPay());
			BigDecimal temp = o.getOrderMoney().subtract(real);
			if(temp.compareTo(BigDecimal.ZERO)>0){
                ot.setIncomePrize(real.divide(temp,2,BigDecimal.ROUND_HALF_UP)+"");
			}
			//订单金额
			ot.setOrderMoney(o.getOrderMoney());
			listDto.add(ot);
		}
		return listDto;
	}



	@RequestMapping("detailInfo")
	@ResponseBody
	public Result showDetail(String orderId){
	    JSONObject object = new JSONObject();
        try{
            Order o = orderService.selectOrderDetails(orderId);
            object.put("shopName",o.getShopName());
            object.put("orderId",o.getId());
            for (OrderPaymentItem paymentItem : o.getOrderPaymentItems()){
                if (paymentItem.getPaymentModeId().equals(PayMode.WEIXIN_PAY)){
                    object.put("wechatPayId",paymentItem.getId());
                }
            }
            object.put("orderTime",DateUtil.formatDate(o.getCreateTime(),"yyyy-MM-dd HH:mm:ss"));
            object.put("modeText", DistributionType.getModeText(o.getDistributionModeId()));
            object.put("varCode",o.getVerCode());
            object.put("")
        }catch (Exception e){
            log.error("查询订单明细出错！");
            e.printStackTrace();
            return new Result(false);
        }
		return getSuccessResult(object);
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
		String[]columns={"shopName","begin","telephone","orderMoney","weChatPay","accountPay","couponPay","chargePay","rewardPay","waitRedPay","aliPayment","backCartPay","moneyPay","articleBackPay","incomePrize","level","orderState"};
		//定义数据
		List<OrderDetailDto> result = this.listResult(beginDate, endDate, shopId);
		//获取店铺名称
		ShopDetail s = shopDetailService.selectById(shopId);
		Map<String,String> map = new HashMap<>();
		map.put("brandName", getBrandName());
		map.put("shops", s.getName());
		map.put("beginDate", beginDate);
		map.put("reportType", "店铺订单报表");//表的头，第一行内容
		map.put("endDate", endDate);
		map.put("num", "16");//显示的位置
		map.put("reportTitle", "品牌订单");//表的名字
		map.put("timeType", "yyyy-MM-dd");

		String[][] headers = {{"店铺","25"},{"下单时间","25"},{"手机号","25"},{"订单金额(元)","25"},{"微信支付(元)","25"},{"红包支付(元)","25"},{"优惠券支付(元)","25"},{"充值金额支付(元)","25"},{"充值赠送金额支付(元)","25"},{"等位红包支付(元)","25"},{"支付宝支付(元)","25"},{"现金支付(元)","25"},{"银联支付(元)","25"},{"退菜金额(元)","25"},{"营销撬动率","25"},{"评价","25"},{"订单状态","25"}};
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

	@RequestMapping("houFuChekc")
	@ResponseBody
	public  Result checkHoufu(String beginDate,String endDate){
		List<Order> orderList = orderService.selectOrderListItemByBrandId(beginDate,endDate,getCurrentBrandId());
		if(!orderList.isEmpty()){
			for(Order o :orderList){
				BigDecimal temp = BigDecimal.ZERO;
				for(OrderItem oi:o.getOrderItems()){
					temp=temp.add(oi.getFinalPrice());//查询所有的菜品的总价
				}
				//判断当前的订单的总额orderMoney 是否=菜品价格+服务费
				if(o.getOrderMoney().compareTo(temp.add(o.getServicePrice()))!=0){
					OrderException orderException = new OrderException();
					orderException.setOrderId(o.getId());
					orderException.setOrderMoney(o.getOrderMoney());
					orderException.setWhy("服务费+菜品费不等于订单价格");
					orderException.setShopName("花千锅");
					orderException.setCreateTime(o.getCreateTime());
					orderException.setBrandName(getBrandName());
					orderExceptionService.insert(orderException);

				}
			}
		}


		List<Order> orderPayList = orderService.selectHoufuOrderList(beginDate, endDate, getCurrentBrandId());
		if(!orderPayList.isEmpty()){
			for(Order od : orderPayList ){
				BigDecimal temp = BigDecimal.ZERO;
				if(od.getAmountWithChildren().compareTo(BigDecimal.ZERO)!=0){
					temp = od.getAmountWithChildren();
				}else {
					temp= od.getOrderMoney();
				}

				BigDecimal tempoi = BigDecimal.ZERO;
				for( OrderPaymentItem oi :od.getOrderPaymentItems()){
					tempoi=tempoi.add(oi.getPayValue());
				}
				if(temp.compareTo(tempoi)!=0){
					OrderException orderException2 = new OrderException();
					orderException2.setOrderId(od.getId());
					orderException2.setOrderMoney(od.getOrderMoney());
					orderException2.setWhy("支付项比支付金额");
					orderException2.setShopName("花千锅");
					orderException2.setCreateTime(od.getCreateTime());
					orderException2.setBrandName(getBrandName());
					orderExceptionService.insert(orderException2);

				}


			}
		}

		return  getSuccessResult();

	}

}
