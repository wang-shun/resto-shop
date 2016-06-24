 package com.resto.shop.web.controller.business;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFHyperlink;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.hssf.util.HSSFColor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.brand.web.dto.BrandIncomeDto;
import com.resto.brand.web.dto.IncomeReportDto;
import com.resto.brand.web.dto.ShopIncomeDto;
import com.resto.brand.web.model.Brand;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.shop.web.constant.PayMode;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.service.OrderPaymentItemService;

@Controller
@RequestMapping("totalRevenue")
public class TotalRevenueController extends GenericController{
	
	@Resource
	BrandService brandService;
	
	@Resource
	ShopDetailService shopDetailService;
	
	@Resource
	OrderPaymentItemService orderpaymentitemService;
	
	@RequestMapping("/list")
        public void list(){
        }
	//封装品牌和店铺收入需要的数据
	
	@RequestMapping("reportIncome")
	@ResponseBody
	public Map<String,Object> selectIncomeReportList(@RequestParam("beginDate")String beginDate,@RequestParam("endDate")String endDate){
		//查询品牌和店铺的收入情况
		List<IncomeReportDto> incomeReportList = orderpaymentitemService.selectIncomeList(getCurrentBrandId(),beginDate,endDate);
		//封装店铺所需要的数据结构
		List<ShopDetail> listShop = shopDetailService.selectByBrandId(getCurrentBrandId());
		List<ShopIncomeDto> shopIncomeList = new ArrayList<>();
		Map<String,ShopIncomeDto> hm = new HashMap<>();
		for (int i = 0; i < listShop.size(); i++) {//实际有多少个店铺显示多少个数据
			ShopIncomeDto sin = new ShopIncomeDto();
			sin.setShopDetailId(listShop.get(i).getId());
			sin.setShopName(listShop.get(i).getName());
			//设置每个店铺初始营业额为零
			BigDecimal temp = BigDecimal.ZERO;
			sin.setWechatIncome(temp);
			sin.setRedIncome(temp);
			sin.setCouponIncome(temp);
			sin.setChargeAccountIncome(temp);
			sin.setChargeGifAccountIncome(temp);
			sin.setTotalIncome(temp, temp, temp,temp,temp);
			String s = ""+i;
			hm.put(s, sin);
			if(!incomeReportList.isEmpty()){
				for(IncomeReportDto in : incomeReportList){
			        if(hm.get(s).getShopDetailId().equals(in.getShopDetailId())){
			            switch (in.getPayMentModeId()) {
						case PayMode.WEIXIN_PAY:
							hm.get(s).setWechatIncome(in.getPayValue());
							break;
						case PayMode.ACCOUNT_PAY:
							hm.get(s).setRedIncome(in.getPayValue());
							break;
						case PayMode.COUPON_PAY:
							hm.get(s).setCouponIncome(in.getPayValue());
							break;
						case PayMode.CHARGE_PAY:
							hm.get(s).setChargeAccountIncome(in.getPayValue());
							break;
						case PayMode.REWARD_PAY:
							hm.get(s).setChargeGifAccountIncome(in.getPayValue());
							break;
							
						default:
							break;
						}
			            hm.get(s).setTotalIncome(hm.get(s).getWechatIncome(),hm.get(s).getRedIncome(),hm.get(s).getCouponIncome(),hm.get(s).getChargeAccountIncome(),hm.get(s).getChargeGifAccountIncome());
			        }
				}
			}
			shopIncomeList.add(hm.get(s));
		}
		//封装brand所需要的数据结构
		
		Brand brand = brandService.selectById(getCurrentBrandId());
		List<BrandIncomeDto> brandIncomeList = new ArrayList<>();
		BrandIncomeDto in = new BrandIncomeDto();
		//初始化品牌的信息
		BigDecimal wechatIncome = BigDecimal.ZERO;
		BigDecimal redIncome = BigDecimal.ZERO;
		BigDecimal couponIncome = BigDecimal.ZERO;
		BigDecimal chargeAccountIncome = BigDecimal.ZERO;
		BigDecimal chargeGifAccountIncome = BigDecimal.ZERO;
		
		if(!incomeReportList.isEmpty()){
			for(IncomeReportDto income : incomeReportList){
				if(income.getPaymentModeId()==PayMode.WEIXIN_PAY){
					wechatIncome=wechatIncome.add(income.getPayValue()).setScale(2);
				}else if(income.getPayMentModeId()==PayMode.ACCOUNT_PAY){
					redIncome=redIncome.add(income.getPayValue()).setScale(2);
				}else if(income.getPayMentModeId()==PayMode.COUPON_PAY){
					couponIncome=couponIncome.add(income.getPayValue()).setScale(2);
				}else if(income.getPaymentModeId()==PayMode.CHARGE_PAY){
					chargeAccountIncome=chargeAccountIncome.add(income.getPayValue()).setScale(2);
				}else if (income.getPayMentModeId()==PayMode.REWARD_PAY){
					chargeGifAccountIncome=chargeGifAccountIncome.add(income.getPayValue()).setScale(2);
				}
			}
		}
		in.setBrandName(brand.getBrandName());
		in.setWechatIncome(wechatIncome);
		in.setRedIncome(redIncome);
		in.setCouponIncome(couponIncome);
		in.setChargeAccountIncome(chargeAccountIncome);
		in.setChargeGifAccountIncome(chargeGifAccountIncome);
		in.setTotalIncome(in.getWechatIncome(), in.getRedIncome(), in.getCouponIncome(),in.getChargeAccountIncome(),in.getChargeGifAccountIncome());
		brandIncomeList.add(in);
		Map<String,Object> map = new HashMap<>();
		map.put("shopIncome", shopIncomeList);
		map.put("brandIncome", brandIncomeList);
		return map;
	}
	
	@SuppressWarnings("deprecation")
	@ResponseBody
	@RequestMapping("reportExcel")
	public void reportExcel() throws IOException{
		
		//品牌数据
		BrandIncomeDto brandIncomeDto = new BrandIncomeDto(new BigDecimal("100"), new BigDecimal("50"), new BigDecimal("100"), new BigDecimal("100"), new BigDecimal("100"), new BigDecimal("100"), null, null, null, "测试品牌");
		
		//店铺数据
		List<ShopIncomeDto> list = new ArrayList<>();
		ShopIncomeDto shopIncomeDto = new ShopIncomeDto(new BigDecimal("100"), new BigDecimal("100"), new BigDecimal("100"), new BigDecimal("100"), new BigDecimal("100"), new BigDecimal("100"), null, "测试店铺1", null, null, null);
		ShopIncomeDto shopIncomeDto2 = new ShopIncomeDto(new BigDecimal("100"), new BigDecimal("100"), new BigDecimal("100"), new BigDecimal("100"), new BigDecimal("100"), new BigDecimal("100"), null, "测试店铺2", null, null, null);
		list.add(shopIncomeDto);
		list.add(shopIncomeDto2);
		
		
		//第一创建一个webbook,对应一个Excel文件
		HSSFWorkbook wb = new HSSFWorkbook();
		//第二步，在webbook中添加一个sheet,对应Excel文件中的sheet
		HSSFSheet sheet = wb.createSheet("品牌表");
		//第三步，在sheet中添加表头第0行
		HSSFRow row = sheet.createRow(0);
		//第四步，创建单元格,并设置值表头,设置表头居中
		HSSFCellStyle style = wb.createCellStyle();
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);//创建一个居中格式
		
		HSSFCell cell = row.createCell(0);
		cell.setCellValue("品牌名称");
		cell.setCellStyle(style);
		
		cell = row.createCell(1);
		cell.setCellValue("总收入");
		cell.setCellStyle(style);
		
		cell = row.createCell(2);
		cell.setCellValue("红包收入");
		cell.setCellStyle(style);
		
		cell = row.createCell(3);
		cell.setCellValue("系统账户收入");
		cell.setCellStyle(style);
		
		cell = row.createCell(4);
		cell.setCellValue("优惠券收入");
		cell.setCellStyle(style);
		
		cell = row.createCell(5);
		cell.setCellValue("充值金额支付");
		cell.setCellStyle(style);
		
		cell = row.createCell(6);
		cell.setCellValue("充值赠送的金额支付");
		cell.setCellStyle(style);
		
		//第五步写入实体数据
		row = sheet.createRow(1);
		row.createCell(0).setCellValue(brandIncomeDto.getBrandName());
		row.createCell(0).setCellValue(brandIncomeDto.getBrandName());
		row.createCell(0).setCellValue(brandIncomeDto.getBrandName());
		row.createCell(0).setCellValue(brandIncomeDto.getBrandName());
		
	    
	    
	    
	    
	  
	}  
	
}
	
	
	
	

