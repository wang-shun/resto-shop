package com.resto.shop.web.controller.business;

import com.resto.brand.core.alipay.util.AlipayNotify;
import com.resto.brand.core.util.WeChatPayUtils;
import com.resto.brand.web.service.AccountChargeOrderService;
import com.resto.brand.web.service.SmsChargeOrderService;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 账户zhifu
 *
 */
@Controller
@RequestMapping("account_paynotify")
public class AccountPayNotifyController {
	
	@Resource
	AccountChargeOrderService accountChargeOrderService;
	
	Logger log = LoggerFactory.getLogger(getClass());
	
	@RequestMapping("alipay_notify")
	public void alipayNotify(HttpServletRequest request,HttpServletResponse response){
		log.info("支付宝---->  异步    发来贺电");
		//获取支付宝返回的所有参数
		Map<String, String> resultMap = AlipayNotify.getNotifyParams(request, response);
		//返回值
		String returnHtml = "fail";
		if(AlipayNotify.verify(resultMap)){//验证成功
			String trade_status = resultMap.get("trade_status");//交易状态
			if(trade_status.equals("TRADE_FINISHED") || trade_status.equals("TRADE_SUCCESS")){
				log.info("支付宝充值成功返回的参数为:"+resultMap);
				boolean flag = accountChargeOrderService.checkAccountChargeOrder_AliPay(resultMap);
				returnHtml = flag?"success":"fail";	//请不要修改或删除
			}else{
				//accountChargeOrderService.saveResultParam(resultMap, "支付宝");//保存参数
			}
		}else{//验证失败
			//accountChargeOrderService.saveResultParam(resultMap, "支付宝");//保存参数
			returnHtml = "fail";
		}
		//返回
		try {
			log.info("给支付宝支付异步请求的返回的信息为："+returnHtml);
			response.setContentType("text/html;charset=utf-8");
			response.getWriter().write(returnHtml);
			response.getWriter().flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@RequestMapping("/alipay_return")
	public String alipayReturn(HttpServletRequest request,HttpServletResponse response){//支付宝返回路径
		log.info("支付宝---->  充值完成   页面跳转");
		Map<String, String> params = AlipayNotify.getNotifyParams(request, response);
		if(AlipayNotify.verify(params)){//验证成功
			String trade_status = params.get("trade_status");//交易状态
			if(trade_status.equals("TRADE_FINISHED") || trade_status.equals("TRADE_SUCCESS")){
				log.info("支付宝充值成功：orderID："+params.get("out_trade_no"));
				params.put("restoResut", "true");
			}
		}else{//验证失败
			params.put("restoResut", "false");
		}
		request.setAttribute("returnParams", params);
		return "smschargeorder/alipayReturn";
	}
	
	

	
	private Map<String, String> getResultMap(HttpServletRequest request) throws IOException, DocumentException {
		InputStream input = request.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		String line = "";
		StringBuffer xmlData = new StringBuffer();
		while((line=reader.readLine())!=null){
			xmlData.append(line);
		}
		log.info("receive weixin pay nofity :"+xmlData);
		return WeChatPayUtils.xmlToMap(xmlData.toString());
	}


}
