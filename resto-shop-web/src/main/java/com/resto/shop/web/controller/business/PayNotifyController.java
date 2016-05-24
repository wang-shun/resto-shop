package com.resto.shop.web.controller.business;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.brand.core.alipay.util.AlipayNotify;
import com.resto.brand.core.util.WeChatPayUtils;
import com.resto.brand.web.service.SmsChargeOrderService;

@Controller
@RequestMapping("paynotify")
public class PayNotifyController {
	
	@Resource
	private SmsChargeOrderService smsChargeOrderService;

	Logger log = LoggerFactory.getLogger(getClass());
	
	@RequestMapping("alipay_notify")
	public void alipayNotify(HttpServletRequest request,HttpServletResponse response){
		log.info("支付宝---->  异步    发来贺电");
//		//商户订单号
//		String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"),"UTF-8");
//		//支付宝交易号
//		String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"),"UTF-8");
//		//交易状态
//		String trade_status = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"),"UTF-8");
//		//交易金额
//		String total_fee = new String(request.getParameter("total_fee").getBytes("ISO-8859-1"),"UTF-8");
//		//收款支付宝账号
//		String seller_id = new String(request.getParameter("seller_id").getBytes("ISO-8859-1"),"UTF-8");
		
		//获取支付宝返回的所有参数
		Map<String, String> params = AlipayNotify.getNotifyParams(request, response);
		//返回值
		String returnHtml = "fail";
		if(AlipayNotify.verify(params)){//验证成功
			String out_trade_no = params.get("out_trade_no");//商户订单号
			String trade_no = params.get("trade_no");//支付宝交易号
			String trade_status = params.get("trade_status");//交易状态
			String total_fee = params.get("total_fee");//交易金额
			String seller_id = params.get("seller_id");//收款支付宝账号
			if(trade_status.equals("TRADE_FINISHED") || trade_status.equals("TRADE_SUCCESS")){
				log.info("支付宝充值成功：orderID："+out_trade_no);
				smsChargeOrderService.checkSmsChargeOrder(out_trade_no,trade_no,total_fee,seller_id);
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
	
	@RequestMapping("/alipay_return")
	public void alipayReturn(){//支付宝返回路径
	}
	
	
	@RequestMapping("wxpay_notify")
	@ResponseBody
	public String wx_notify(HttpServletRequest request) throws IOException, DocumentException{
		//微信支付异步通知 参数 详情：https://pay.weixin.qq.com/wiki/doc/api/native.php?chapter=9_7
		log.info("微信---->  异步    发来贺电");
		Map<String,String> resultMap = getResultMap(request);
		Map<String,String> wxResult = new HashMap<String, String>();
		wxResult.put("return_code", "SUCCESS");
		if("SUCCESS".equals(resultMap.get("return_code"))&&"SUCCESS".equals(resultMap.get("result_code"))){
			if(WeChatPayUtils.validSign(resultMap,WeChatPayUtils.RESTO_MCHKEY)){
				try{
					log.info("微信充值成功:"+resultMap);
					String total_fee = resultMap.get("total_fee");
					String out_trade_no = resultMap.get("out_trade_no");
					String transaction_id = resultMap.get("transaction_id");
					String seller_id = resultMap.get("seller_id");
					smsChargeOrderService.checkSmsChargeOrder(out_trade_no,transaction_id,total_fee,seller_id);
				}catch(Exception e){
					log.info("接受微信支付请求失败:"+e.getMessage());
					e.printStackTrace();
					wxResult.put("return_code", "FAIL");
					wxResult.put("return_msg", e.toString());
				}
			}else{
				wxResult.put("return_code", "FAIL");
				wxResult.put("return_msg", "签名失败");
			}
		}
		String wxResultXml = WeChatPayUtils.mapToXml(wxResult);
		log.info("微信支付返回成功信息 id:"+resultMap.get("transaction_id")+" 返回信息："+wxResultXml);
		return wxResultXml;
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
