package com.resto.shop.web.controller.business;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
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
import com.resto.brand.web.dto.RechargeLogDto;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.service.ChargePaymentService;
import com.resto.shop.web.service.CustomerService;


@Controller
@RequestMapping("recharge")

public class RechargeLogController extends GenericController{
	@Resource
	ChargePaymentService chargepaymentService;

	@Resource
	BrandService brandService;

	@Resource
	ShopDetailService shopDetailService;
	
	@Resource
	private CustomerService customerService;
	
	@RequestMapping("/list")
	public void list(){
	}
	
	@RequestMapping("/rechargeLog")
	@ResponseBody
	public Result selectBrandOrShopRecharge(String beginDate,String endDate){
		return this.getResult(beginDate, endDate);
	}

	private Result getResult(String beginDate, String endDate) {
		return getSuccessResult(this.RechargeList(beginDate, endDate));
	}
	
	public Map<String, Object> RechargeList(String beginDate,String endDate){
		BigDecimal initZero=BigDecimal.ZERO;
		//初始化品牌充值记录
		RechargeLogDto brandInit = new RechargeLogDto(getBrandName(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
		RechargeLogDto rechargeLogDto=chargepaymentService.selectRechargeLog(beginDate, endDate,getCurrentBrandId());
		if(rechargeLogDto!=null){
			brandInit.setBrandName(getBrandName());
			//判断为空则为0
			if(rechargeLogDto.getRechargeCount() == null){
				brandInit.setRechargeCount(initZero);
			}else{
				brandInit.setRechargeCount(rechargeLogDto.getRechargeCount());
			}
			if(rechargeLogDto.getRechargeCsNum() == null){
				brandInit.setRechargeCsNum(initZero);
			}else{
				brandInit.setRechargeCsNum(rechargeLogDto.getRechargeCsNum());
			}
			if(rechargeLogDto.getRechargeGaCsNum() == null){
				brandInit.setRechargeGaCsNum(initZero);
			}else{
				brandInit.setRechargeGaCsNum(rechargeLogDto.getRechargeGaCsNum());
			}
			if(rechargeLogDto.getRechargeGaNum() == null){
				brandInit.setRechargeGaNum(initZero);
			}else{
				brandInit.setRechargeGaNum(rechargeLogDto.getRechargeGaNum());
			}
			if(rechargeLogDto.getRechargeGaSpNum() == null){
				brandInit.setRechargeGaSpNum(initZero);
			}else{
				brandInit.setRechargeGaSpNum(rechargeLogDto.getRechargeGaSpNum());
			}
			if(rechargeLogDto.getRechargeNum() == null){
				brandInit.setRechargeNum(initZero);
			}else{
				brandInit.setRechargeNum(rechargeLogDto.getRechargeNum());
			}
			if(rechargeLogDto.getRechargePos() == null){
				brandInit.setRechargePos(initZero);
			}else{
				brandInit.setRechargePos(rechargeLogDto.getRechargePos());
			}
			if(rechargeLogDto.getRechargeSpNum() == null){
				brandInit.setRechargeSpNum(initZero);
			}else{
				brandInit.setRechargeSpNum(rechargeLogDto.getRechargeSpNum());
			}
			if(rechargeLogDto.getRechargeWeChat() == null){
				brandInit.setRechargeWeChat(initZero);
			}else{
				brandInit.setRechargeWeChat(rechargeLogDto.getRechargeWeChat());
			}
		}
		
		List<RechargeLogDto> shopRrchargeLogs=new ArrayList<>();
		
		List<ShopDetail> shoplist = getCurrentShopDetails();
        if(!shoplist.isEmpty()){
            shoplist = shopDetailService.selectByBrandId(getCurrentBrandId());
        }
        for (ShopDetail shopDetail : shoplist) {
        	//初始化店铺充值记录
        	RechargeLogDto shopInit=new RechargeLogDto(shopDetail.getId(),shopDetail.getName(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        	RechargeLogDto shopRechargeLogDto=chargepaymentService.selectShopRechargeLog(beginDate, endDate, shopDetail.getId());
        	shopInit.setShopId(shopDetail.getId());
        	shopInit.setShopName(shopDetail.getName());
        	//判断为空则为0
        	if(shopRechargeLogDto.getShopCount()==null){
        		shopInit.setShopCount(initZero);
        	}else{
            	shopInit.setShopCount(shopRechargeLogDto.getShopCount()); 		
        	}
        	if(shopRechargeLogDto.getShopGaNum()==null){
        		shopInit.setShopNum(initZero);
        	}else{
            	shopInit.setShopNum(shopRechargeLogDto.getShopNum());
        	}
        	if(shopRechargeLogDto.getShopGaNum() == null){
        		shopInit.setShopGaNum(initZero);
        	}else{
            	shopInit.setShopGaNum(shopRechargeLogDto.getShopGaNum());
        	}
        	if(shopRechargeLogDto.getShopWeChat() == null){
        		shopInit.setShopWeChat(initZero);
        	}else{
            	shopInit.setShopWeChat(shopRechargeLogDto.getShopWeChat());
        	}
        	if(shopRechargeLogDto.getShopPos() == null){
        		shopInit.setShopPos(initZero);
        	}else{
            	shopInit.setShopPos(shopRechargeLogDto.getShopPos());
        	}
        	if(shopRechargeLogDto.getShopCsNum() == null){
        		shopInit.setShopCsNum(initZero);
        	}else{
            	shopInit.setShopCsNum(shopRechargeLogDto.getShopCsNum());
        	}
        	if(shopRechargeLogDto.getShopGaCsNum() == null){
        		shopInit.setShopGaCsNum(initZero);
	    	}else{
	    		shopInit.setShopGaCsNum(shopRechargeLogDto.getShopGaCsNum());
        	}
        	shopRrchargeLogs.add(shopInit);
		}
        
		Map<String, Object> map=new HashMap<>();
		map.put("brandInit", brandInit);
		map.put("shopRrchargeLogs", shopRrchargeLogs);
		
		return map;
	}

	
	//下载充值报表
    @SuppressWarnings("unchecked")
	@RequestMapping("brandOrShop_excel")
	@ResponseBody
	public void reportIncome(String userJson,String beginDate,String endDate,HttpServletRequest request, HttpServletResponse response){
    	List<ShopDetail> shopDetailList = getCurrentShopDetails();
        if(shopDetailList==null){
            shopDetailList = shopDetailService.selectByBrandId(getCurrentBrandId());
        }
        List<RechargeLogDto> result = new LinkedList<>();
//        for (ShopDetail shopDetail : shopDetailList) {
//        	RechargeLogDto shopRechargeLogDto=chargepaymentService.selectShopRechargeLog(beginDate, endDate, shopDetail.getId());
//        	shopRechargeLogDto.setShopName(shopDetail.getName());
//        	result.add(shopRechargeLogDto);
//        }
//        RechargeLogDto brandRechargeLogDto=(RechargeLogDto) RechargeList(beginDate,endDate).get("brandInit");
//        result.add(brandRechargeLogDto);
		List<RechargeLogDto> shopRechargeLogDto=(List<RechargeLogDto>) RechargeList(beginDate,endDate).get("shopRrchargeLogs");
        for (RechargeLogDto rechargeLogDto : shopRechargeLogDto) {
        	RechargeLogDto shoprecharge=new RechargeLogDto();
        	shoprecharge.setShopName(rechargeLogDto.getShopName());
        	shoprecharge.setShopCount(rechargeLogDto.getShopCount()); 		
        	shoprecharge.setShopNum(rechargeLogDto.getShopNum());
        	shoprecharge.setShopGaNum(rechargeLogDto.getShopGaNum());
        	shoprecharge.setShopWeChat(rechargeLogDto.getShopWeChat());
        	shoprecharge.setShopPos(rechargeLogDto.getShopPos());
        	shoprecharge.setShopCsNum(rechargeLogDto.getShopCsNum());
        	shoprecharge.setShopGaCsNum(rechargeLogDto.getShopGaCsNum());
        	result.add(shoprecharge);
		}
		//导出文件名
		String fileName = "充值报表"+beginDate+"至"+endDate+".xls";
		//定义读取文件的路径
		String path = request.getSession().getServletContext().getRealPath(fileName);
		//定义列
		String[]columns={"shopName","shopCount","shopNum","shopGaNum","shopWeChat","shopPos","shopCsNum","shopGaCsNum"};
		//定义数据
		String shopName="";
		for (ShopDetail shopDetail : shopDetailList) {
			shopName += shopDetail.getName()+",";
		}
		Map<String,String> map = new HashMap<>();
		map.put("brandName", getBrandName());
		map.put("shops", shopName);
		map.put("beginDate", beginDate);
		map.put("reportType", "充值报表");//表的头，第一行内容
		map.put("endDate", endDate);
		map.put("num", "8");//显示的位置
		map.put("reportTitle", "充值报表");//表的名字
		map.put("timeType", "yyyy-MM-dd");
		
		String[][] headers = {{"店铺名","25"},{"充值单数","25"},{"店铺充值总额","25"},{"店铺充值赠送总额","25"},{"店铺微信充值总额","25"},{"店铺POS端充值总额","25"},{"店铺充值消费","25"},{"店铺充值赠送消费","25"}};
		//定义excel工具类对象
		ExcelUtil<RechargeLogDto> excelUtil=new ExcelUtil<RechargeLogDto>();
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
	


}