package com.resto.shop.web.controller.business;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.brand.core.alipay.util.AlipayNotify;
import com.resto.brand.core.entity.Result;
import com.resto.brand.web.model.SmsChargeOrder;
import com.resto.brand.web.service.SmsAcountService;
import com.resto.brand.web.service.SmsChargeOrderService;
import com.resto.shop.web.controller.GenericController;

@Controller
@RequestMapping("smschargeorder")
public class SmsChargeOrderController extends GenericController {
	
	@Resource
	private SmsChargeOrderService smsChargeOrderService;

	@Resource
	private SmsAcountService smsAcountService;

	@RequestMapping("/list")
	public void smscharge(){
	}
	
	@RequestMapping("/list_all")
	@ResponseBody
	public Result list_all(){
		List<SmsChargeOrder> list = smsChargeOrderService.selectByBrandId(getCurrentBrandId());
		return getSuccessResult(list);
	}
	
	@RequestMapping("/smsCharge")
	public void smsCharge(String chargeMoney,HttpServletRequest request,HttpServletResponse response){
		String returnHtml = "<h1>参数错误！</h1>";
		if(chargeMoney!=null && chargeMoney!=""){
			String url = request.getScheme()+"://"+ request.getServerName()+request.getRequestURI()+"?"+request.getQueryString();
			String orderName = "短信充值";
			returnHtml = smsChargeOrderService.createSmsChargeOrder(orderName, chargeMoney, url, getCurrentBrandId());
		}
		try {
			//页面输出
			response.setContentType("text/html;charset=utf-8");
			response.getWriter().write(returnHtml);
			response.getWriter().flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@RequestMapping("/selectSmsUnitPrice")
	@ResponseBody
	public Result selectSmsUnitPrice(){
		BigDecimal smsUnitPrice = smsAcountService.selectSmsUnitPriceByBrandId(getCurrentBrandId());
		return getSuccessResult(smsUnitPrice);
	}
	
	@RequestMapping("/alipayReturn")
	public void alipayReturn(){//支付宝返回路径
	}
	
	@RequestMapping("/alipayNotify")//支付宝异步通知路径
	public void alipayNotify(HttpServletRequest request,HttpServletResponse response) throws UnsupportedEncodingException{
		//获取支付宝POST过来反馈信息
		Map<String,String> params = new HashMap<String,String>();
		Map requestParams = request.getParameterMap();
		for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			String[] values = (String[]) requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i]
						: valueStr + values[i] + ",";
			}
			//乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
			//valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
			params.put(name, valueStr);
		}
		
		//获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以下仅供参考)//
		//商户订单号
		String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"),"UTF-8");
		//支付宝交易号
		String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"),"UTF-8");
		//交易状态
		String trade_status = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"),"UTF-8");
		//交易金额
		String total_fee = new String(request.getParameter("total_fee").getBytes("ISO-8859-1"),"UTF-8");
		//收款支付宝账号
		String seller_id = new String(request.getParameter("seller_id").getBytes("ISO-8859-1"),"UTF-8");
		//获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以上仅供参考)//
		//返回值
		String returnHtml = "fail";
		if(AlipayNotify.verify(params)){//验证成功
			if(trade_status.equals("TRADE_FINISHED") || trade_status.equals("TRADE_SUCCESS")){
				smsChargeOrderService.checkSmsChargeOrder(out_trade_no,trade_no,total_fee,seller_id);
				//注意：
				//退款日期超过可退款期限后（如三个月可退款），支付宝系统发送该交易状态通知
			}
			returnHtml = "success";	//请不要修改或删除
		}else{//验证失败
			returnHtml = "fail";
		}
		//返回
		try {
			response.setContentType("text/html;charset=utf-8");
			response.getWriter().write(returnHtml);
			response.getWriter().flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
