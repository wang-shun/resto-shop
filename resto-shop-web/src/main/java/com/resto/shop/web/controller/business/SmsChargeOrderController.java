package com.resto.shop.web.controller.business;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dom4j.DocumentException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.zxing.WriterException;
import com.resto.brand.core.alipay.util.AlipaySubmit;
import com.resto.brand.core.entity.Result;
import com.resto.brand.core.enums.PayType;
import com.resto.brand.core.util.QRCodeUtil;
import com.resto.brand.core.util.WeChatPayUtils;
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
	public void smsCharge(String chargeMoney,String paytype,HttpServletRequest request,HttpServletResponse response) throws IOException, WriterException, DocumentException{
		String returnHtml = "<h1>参数错误！</h1>";
		if(StringUtil.isNotEmpty(chargeMoney) && StringUtil.isNotEmpty(paytype)){
			SmsChargeOrder smsChargeOrder = smsChargeOrderService.saveSmsOrder(getCurrentBrandId(), chargeMoney);//创建充值订单
			String out_trade_no = smsChargeOrder.getId();
			if(paytype.equals(PayType.ALI_PAY+"")){//支付宝支付
				String show_url = "";
				String notify_url = getBaseUrl()+"paynotify/alipay_notify";
				String return_url = getBaseUrl()+"paynotify/alipay_return";
				String subject = "短信充值";
				Map<String, String> formParame = AlipaySubmit.createFormParame(out_trade_no, subject, chargeMoney, show_url, notify_url, return_url, null);
				returnHtml = AlipaySubmit.buildRequest(formParame, "post", "确认");
			}else if(paytype.equals(PayType.WECHAT_PAY+"")){//微信支付
				String spbill_create_ip = InetAddress.getLocalHost().getHostAddress();
				String notify_url =  getBaseUrl()+"paynotify/wxpay_notify";
				log.info("微信的通知路径为："+notify_url);
				String body = "短信充值";
				Map<String,String> apiReqeust = WeChatPayUtils.createWxPay(out_trade_no, chargeMoney, spbill_create_ip, notify_url,body);
				if("true".equals(apiReqeust.get("success"))){
					request.getSession().setAttribute("wxPayCode", apiReqeust.get("url"));
					returnHtml = getWxPayHtml();
				}
			}
		}
		outprint(returnHtml, response);
	}
	
	/**
	 * 完成未支付的订单
	 * @param chargeOrderId
	 * @param request
	 * @param response
	 * @throws UnknownHostException 
	 * @throws DocumentException 
	 */
	@RequestMapping("/payAgain")
	public void payAgain(String chargeOrderId,String paytype,HttpServletRequest request,HttpServletResponse response) throws UnknownHostException, DocumentException{
		String returnHtml = "<h1>参数错误！</h1>";
		if(StringUtil.isNotEmpty(chargeOrderId) && StringUtil.isNotEmpty(paytype)){
			SmsChargeOrder smsChargeOrder = smsChargeOrderService.selectById(chargeOrderId);
			String chargeMoney = smsChargeOrder.getChargeMoney().toString();
			if(smsChargeOrder!=null){
				String out_trade_no = smsChargeOrder.getId();
				if(paytype.equals(PayType.ALI_PAY+"")){//支付宝支付
					String show_url = "";//商品展示页面
					String notify_url = getBaseUrl()+"paynotify/alipay_notify";
					String return_url = getBaseUrl()+"paynotify/alipay_return";
					String subject = "短信充值";
					Map<String, String> formParame = AlipaySubmit.createFormParame(out_trade_no, subject, chargeMoney, show_url, notify_url, return_url, null);
					returnHtml = AlipaySubmit.buildRequest(formParame, "post", "确认");
				}else if(paytype.equals(PayType.WECHAT_PAY+"")){//微信支付
					String spbill_create_ip = InetAddress.getLocalHost().getHostAddress();
					String notify_url =  getBaseUrl()+"paynotify/wxpay_notify";
					String body = "短信充值";
					Map<String,String> apiReqeust = WeChatPayUtils.createWxPay(out_trade_no, chargeMoney, spbill_create_ip, notify_url,body);
					if("true".equals(apiReqeust.get("success"))){
						request.getSession().setAttribute("wxPayCode", apiReqeust.get("url"));
						returnHtml = getWxPayHtml();
					}
				}
			}
		}
		outprint(returnHtml, response);
	}
	
	@RequestMapping("/selectSmsUnitPrice")
	@ResponseBody
	public Result selectSmsUnitPrice(){
		BigDecimal smsUnitPrice = smsAcountService.selectSmsUnitPriceByBrandId(getCurrentBrandId());
		return getSuccessResult(smsUnitPrice);
	}
	
	@RequestMapping("/selectInvoiceMoney")
	@ResponseBody
	public Result selectInvoiceMoney(){
		BigDecimal invoiceMoney = smsAcountService.selectInvoiceMoney(getCurrentBrandId());
		return getSuccessResult(invoiceMoney);
	}
	
	/**
	 * 申请发票
	 * @return
	 */
	@RequestMapping("/applyInvoice")
	@ResponseBody
	public Result applyInvoice(){
//		smsTicketService
		return getSuccessResult();
	}
	
	
	/**
	 * 删除短信充值订单
	 * @param id
	 * @return
	 */
	@RequestMapping("/deleteOrder")
	@ResponseBody
	public boolean deleteOrder(String id){
		int row = smsChargeOrderService.delete(id);
		return row>0?true:false;
	}
	
	/**
	 * 生成微信支付的 二维码
	 * @param response
	 * @param request
	 */
	@RequestMapping("/createWxPayCode")
	@ResponseBody
	public void createWxPayCode(HttpServletResponse response,HttpServletRequest request){
		FileInputStream fis = null;
		File file = null;
	    response.setContentType("image/gif");
	    String fileName = System.currentTimeMillis()+"";
	    try {
	    	String content = (String) request.getSession().getAttribute("wxPayCode");
	    	QRCodeUtil.createQRCode(content, getFilePath(request, null), fileName);
	        OutputStream out = response.getOutputStream();
	        file = new File(getFilePath(request, fileName));
	        fis = new FileInputStream(file);
	        byte[] b = new byte[fis.available()];
	        fis.read(b);
	        out.write(b);
	        out.flush();
	    } catch (Exception e) {
	         e.printStackTrace();
	    } finally {
	        if (fis != null) {
	            try {
	               fis.close();
	            } catch (IOException e) {
	            	e.printStackTrace();
	            }   
	        }
	        System.gc();//手动回收垃圾，清空文件占用情况，解决无法删除文件
	        file.delete();
	    }
	}
	
	/**
	 * 得到文件路径
	 * @param request
	 * @param fileName
	 * @return
	 */
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
	
	/**
	 * 生成微信支付的页面
	 * @return
	 */
	public String getWxPayHtml(){
		StringBuffer str = new StringBuffer();
		str.append("<style>.closeBtn{line-height:30px; height:30px; width:163px; color:#ffffff; background-color:#d9534f; font-size:16px; font-weight:normal; font-family:Arial; border:0px solid #dcdcdc; -webkit-border-top-left-radius:3px; -moz-border-radius-topleft:3px; border-top-left-radius:3px; -webkit-border-top-right-radius:3px; -moz-border-radius-topright:3px; border-top-right-radius:3px; -webkit-border-bottom-left-radius:3px; -moz-border-radius-bottomleft:3px; border-bottom-left-radius:3px; -webkit-border-bottom-right-radius:3px; -moz-border-radius-bottomright:3px; border-bottom-right-radius:3px; -moz-box-shadow: inset 0px 0px 0px 0px #ffffff; -webkit-box-shadow: inset 0px 0px 0px 0px #ffffff; box-shadow: inset 0px 0px 0px 0px #ffffff; text-align:center; display:inline-block; text-decoration:none;}.closeBtn:hover{background-color:#c9302c; cursor:pointer;}</style>");
		str.append("<body style='height:100%;overflow:hidden;'>");
		str.append("<div style='position:absolute; left:0; top:0px; width:100%; height:100%; background:#BBB;text-align: center;'>");
		str.append("<img src = 'createWxPayCode' style='margin-top:150px;'>");
		str.append("<p><strong>扫码即可使用微信支付</strong></p>");
		str.append("<button class=\"closeBtn\" onclick=\"javascript:window.opener=null;window.open('','_self');window.close();\">关闭页面</button>");
		str.append("</div></body>");
		return str.toString();
	}
	
	/**
	 * 输出到页面
	 * @param body
	 * @param response
	 */
	public void outprint(String body,HttpServletResponse response){
		try {
			//页面输出
			response.setContentType("text/html;charset=utf-8");
			response.getWriter().write(body);
			response.getWriter().flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
