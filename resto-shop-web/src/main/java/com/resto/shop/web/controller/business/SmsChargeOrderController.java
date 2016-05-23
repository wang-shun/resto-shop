package com.resto.shop.web.controller.business;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.zxing.WriterException;
import com.resto.brand.core.alipay.util.AlipayNotify;
import com.resto.brand.core.entity.Result;
import com.resto.brand.core.enums.PayType;
import com.resto.brand.core.util.QRCodeUtil;
import com.resto.brand.web.model.SmsChargeOrder;
import com.resto.brand.web.service.SmsAcountService;
import com.resto.brand.web.service.SmsChargeOrderService;
import com.resto.shop.web.controller.GenericController;

import cn.restoplus.rpc.common.util.StringUtil;

@Controller
@RequestMapping("smschargeorder")
public class SmsChargeOrderController extends GenericController {
	
	@Resource
	private SmsChargeOrderService smsChargeOrderService;

	@Resource
	private SmsAcountService smsAcountService;

	@RequestMapping("/list")
	public void list(){
	}
	
	@RequestMapping("/list_all")
	@ResponseBody
	public Result list_all(){
		List<SmsChargeOrder> list = smsChargeOrderService.selectByBrandId(getCurrentBrandId());
		return getSuccessResult(list);
	}
	
	@RequestMapping("/smsCharge")
	public void smsCharge(String chargeMoney,String paytype,HttpServletRequest request,HttpServletResponse response) throws IOException, WriterException{
		String returnHtml = "<h1>参数错误！</h1>";
		
		if(StringUtil.isNotEmpty(chargeMoney) && StringUtil.isNotEmpty(paytype)){
			if(paytype.equals(PayType.ALI_PAY+"")){//支付宝支付
				String url = request.getScheme()+"://"+ request.getServerName()+request.getRequestURI()+"?"+request.getQueryString();
				String orderName = "短信充值";
				returnHtml = smsChargeOrderService.createSmsChargeOrder(orderName, chargeMoney, url, getCurrentBrandId());
			}else if(paytype.equals(PayType.WECHAT_PAY+"")){//微信支付
//				returnHtml = "<center><div style='margin-top: 15%;'>居中对齐</div></center>";
//				createCode("测试内容", response, request);
				String fileName = System.currentTimeMillis()+"";
				QRCodeUtil.createQRCode("测试内容", getFilePath(request, null), fileName);
				String str = getFilePath(request, fileName);
				
				returnHtml = "<img src = \"" + str + "\">";
			}
		}
		System.out.println("---执行到此了");
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
	
	@RequestMapping("/applyInvoice")
	@ResponseBody
	public Result applyInvoice(){
		//待完成 发票记录逻辑
		return getSuccessResult();
	}
	
	public void createCode(String content,HttpServletResponse response,HttpServletRequest request){
//		FileInputStream fis = null;
//		File file = null;
//	    response.setContentType("image/gif");
//	    String fileName = System.currentTimeMillis()+"";
//	    try {
//	    	QRCodeUtil.createQRCode(content, getFilePath(request, null), fileName);
//	        OutputStream out = response.getOutputStream();
//	        file = new File(getFilePath(request, fileName));
//	        fis = new FileInputStream(file);
//	        byte[] b = new byte[fis.available()];
//	        fis.read(b);
//	        out.write(b);
//	        out.flush();
//	    } catch (Exception e) {
//	         e.printStackTrace();
//	    } finally {
//	        if (fis != null) {
//	            try {
//	               fis.close();
//	            } catch (IOException e) {
//	            	e.printStackTrace();
//	            }   
//	        }
//	        System.gc();//手动回收垃圾，清空文件占用情况，解决无法删除文件
//	        file.delete();
//	    }
		try {
			String fileName = System.currentTimeMillis()+"";
			QRCodeUtil.createQRCode(content, getFilePath(request, null), fileName);
			getFilePath(request, fileName);
			
		} catch (IOException | WriterException e) {
			e.printStackTrace();
		}
		
		
	}
	
	public String getFilePath(HttpServletRequest request,String fileName){
		String systemPath = request.getServletContext().getRealPath("");
		systemPath = systemPath.replaceAll("\\\\", "/");
		int lastR = systemPath.lastIndexOf("/");
		systemPath = systemPath.substring(0,lastR)+"/";
		String filePath = "qrCodeFiles/";
		if(fileName!=null){
			filePath += fileName;
		}
		return systemPath+filePath;
	}
}
