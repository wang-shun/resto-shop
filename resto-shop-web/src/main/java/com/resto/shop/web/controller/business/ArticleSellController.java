 package com.resto.shop.web.controller.business;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.JOptionPane;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.brand.core.entity.Result;
import com.resto.brand.core.util.ApplicationUtils;
import com.resto.brand.core.util.ExcelUtil;
import com.resto.brand.web.dto.ArticleSellDto;
import com.resto.brand.web.dto.SaleReportDto;
import com.resto.brand.web.dto.ShopIncomeDto;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.service.ArticleFamilyService;
import com.resto.shop.web.service.OrderItemService;
import com.resto.shop.web.service.OrderService;

/**
 * 菜品销售报表
 * @author lmx
 */
@Controller
@RequestMapping("/articleSell")
public class ArticleSellController extends GenericController{
	
	@Resource
	OrderService orderService;
	
	@Resource
	OrderItemService orderItemService;
	
	@Resource
	ShopDetailService shopDetailService;
	
	@Resource
	ArticleFamilyService articleFamilyService;
	
	
	@RequestMapping("/list")
    public void list(){
    }
	
	
	@RequestMapping("/show/{type}")
	public String showModal(@PathVariable("type")String type,String beginDate,String endDate,String shopId,HttpServletRequest request){
		request.setAttribute("beginDate", beginDate);
		request.setAttribute("endDate", endDate);
		if(shopId!=null){
			request.setAttribute("shopId", shopId);
		}
		return "articleSell/"+type;
	}
	
	@RequestMapping("/list_all")
	@ResponseBody
	public SaleReportDto list_all(String beginDate,String endDate){
		SaleReportDto saleReportDto = orderService.selectArticleSumCountByData(beginDate, endDate,getCurrentBrandId());
		return saleReportDto;
	}
	
	
	@RequestMapping("/shop_data")
	@ResponseBody
	public Result shop_data(String beginDate,String endDate,String shopId){
		List<ArticleSellDto> list = orderService.selectShopArticleSellByDate(beginDate, endDate, shopId);
		return getSuccessResult(list);
	}
	
	@RequestMapping("/brand_data")
	@ResponseBody
	public Result brand_data(String beginDate,String endDate){
		List<ArticleSellDto> list = orderService.selectBrandArticleSellByDate(beginDate, endDate);
		return getSuccessResult(list);
	}
	
	@RequestMapping("/brand_excel")
	@ResponseBody
	public void reportBrandExcel(String beginDate,String endDate,String str,String selectValue,HttpServletRequest request, HttpServletResponse response){
		//导出文件名
		String fileName = "ArticleSellBrand.xls";
		//定义读取文件的路径
		String path = request.getSession().getServletContext().getRealPath(fileName);
		//定义列
		String[]columns={"articleFamilyName","articleName","brandSellNum"};
		//定义数据
		List<ArticleSellDto> result = null;
		//定义首行日期占的位置
		int num = 2;
		//定义时间参数用来在第一行上显示日期
		String [] params = new String[]{beginDate,endDate};
		//定义excel表格的表头下拉框(选择全部出现下拉框)
		Integer[] palce = null;
		String [] list=null;	
		if(selectValue==null||"".equals(selectValue)){
			selectValue="全部";
			result = orderService.selectBrandArticleSellByDate(beginDate, endDate);
			//设置下拉框加载的位置(1,0单元格) 第一个是行 第二个是列
			params = new String[]{beginDate,endDate};
			//设置下拉框的内容
			list=str.split(",");
		}else{
			//根据菜品分类的名称获取菜品分类的id
			String articleFamilyId = articleFamilyService.selectByName(selectValue);
			result = orderService.selectBrandArticleSellByDateAndArticleFamilyId(beginDate, endDate,articleFamilyId);
		}
		String[][] headers = {{"菜品分类("+selectValue+")","22"},{"菜品名称","20"},{"菜品销量(份)","20"}};
		
		//定义excel工具类对象
		ExcelUtil<ArticleSellDto> excelUtil=new ExcelUtil<ArticleSellDto>();
		try{
			OutputStream out = new FileOutputStream(path);
			excelUtil.ExportExcel("品牌菜品销售", headers, columns, result, out, "",params,num,palce,list);
			out.close();
			excelUtil.download(path, response);
			JOptionPane.showMessageDialog(null, "导出成功！");
			log.info("excel导出成功");
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	@RequestMapping("/shop_excel")
	@ResponseBody
	public void reportShopExcel(String beginDate,String endDate,String str,String selectValue,String shopId,HttpServletRequest request, HttpServletResponse response){
		//导出文件名
		String fileName = "ArticleSellShop.xls";
		//定义读取文件的路径
		String path = request.getSession().getServletContext().getRealPath(fileName);
		//定义列
		String[]columns={"articleFamilyName","articleName","shopSellNum","brandSellNum","salesRatio"};
		//定义数据
		List<ArticleSellDto> result = null;
		//定义首行日期占的位置
		int num = 2;
		//定义时间参数用来在第一行上显示日期
		String [] params = new String[]{beginDate,endDate};
		//定义excel表格的表头下拉框(选择全部出现下拉框)
		Integer[] palce = null;
		String [] list=null;	
		if(selectValue==null||"".equals(selectValue)){
			selectValue="全部";
			result = orderService.selectShopArticleSellByDate(beginDate, endDate, shopId);
			//设置下拉框加载的位置(1,0单元格) 第一个是行 第二个是列
			params = new String[]{beginDate,endDate};
			//设置下拉框的内容
			list=str.split(",");
		}else{
			//根据菜品分类的名称获取菜品分类的id
			String articleFamilyId = articleFamilyService.selectByName(selectValue);
			result = orderService.selectShopArticleSellByDateAndArticleFamilyId(beginDate, endDate,shopId,articleFamilyId);
		}
		String[][] headers = {{"菜品分类("+selectValue+")","22"},{"菜品名称","20"},{"菜品销量(份)","20"},{"品牌菜品销量(份)","20"},{"销售占比","20"}};
		
		//定义excel工具类对象
		ExcelUtil<ArticleSellDto> excelUtil=new ExcelUtil<ArticleSellDto>();
		try{
			OutputStream out = new FileOutputStream(path);
			excelUtil.ExportExcel("店铺菜品销售", headers, columns, result, out, "",params,num,palce,list);
			out.close();
			excelUtil.download(path, response);
			JOptionPane.showMessageDialog(null, "导出成功！");
			log.info("excel导出成功");
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	
}
