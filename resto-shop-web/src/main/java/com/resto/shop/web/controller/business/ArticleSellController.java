 package com.resto.shop.web.controller.business;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.JOptionPane;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.brand.core.entity.Result;
import com.resto.brand.core.util.ExcelUtil;
import com.resto.brand.web.dto.ArticleSellDto;
import com.resto.brand.web.dto.ShopArticleReportDto;
import com.resto.brand.web.dto.brandArticleReportDto;
import com.resto.brand.web.model.Brand;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.service.BrandService;
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
	BrandService brandServie;
	
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
	
//	@RequestMapping("/list_all")
//	@ResponseBody
//	public List<SaleReportDto>  list_all(String beginDate,String endDate){
//		List<SaleReportDto> list = new ArrayList<>();
//		SaleReportDto saleReportDto = orderService.selectArticleSumCountByData(beginDate, endDate,getCurrentBrandId());
//		list.add(saleReportDto);
//		return list;
//	}


	@RequestMapping("/list_brand")
	@ResponseBody
	public brandArticleReportDto list_brand(String beginDate,String endDate){
		return orderService.selectBrandArticleNum(beginDate,endDate,getCurrentBrandId(),getBrandName());
	}
	
	
	@RequestMapping("/list_shop")
	@ResponseBody
	public List<ShopArticleReportDto> list_shop(String beginDate,String endDate){
		List<ShopArticleReportDto> list = orderService.selectShopArticleDetails(beginDate,endDate,getCurrentBrandId(),getCurrentShopDetails());
		return list;
	}
	
	
	@RequestMapping("/shop_data")
	@ResponseBody
	public Result shop_data(String beginDate,String endDate,String shopId){
		List<ArticleSellDto> list = orderService.selectShopArticleSellByDate(beginDate, endDate, shopId,"0asc");
		return getSuccessResult(list);
	
	}
	
//	@RequestMapping("/shop_familyId_data")
//	@ResponseBody
//	public Result shop_family_data(String beginDate,String endDate,String shopId){
//		List<ArticleSellDto> list = orderService.selectShopArticleSellByDate(beginDate, endDate, shopId,"0asc");
//		return getSuccessResult(list);
//		
//	}
//	@RequestMapping("/shop_id_data")
//	@ResponseBody
//	public Result shop_id_data(String beginDate,String endDate,String shopId){
//		List<ArticleSellDto> list = orderService.selectShopArticleSellByDate(beginDate, endDate, shopId,"0asc");
//		return getSuccessResult(list);
//	}
//	
//	@RequestMapping("/shop_familyId_data")
//	@ResponseBody
//	public Result shop_family_data(String beginDate,String endDate,String shopId,String sort){
//		List<ArticleSellDto> list = orderService.selectShopArticleSellByDateAndFamilyId(beginDate, endDate, shopId,sort);
//		return getSuccessResult(list);
//		
//	}
	@RequestMapping("/shop_id_data")
	@ResponseBody
	public Result shop_id_data(String beginDate,String endDate,String shopId,String sort){
		List<ArticleSellDto> list = orderService.selectShopArticleSellByDateAndId(beginDate, endDate, shopId,sort);
		return getSuccessResult(list);
	}
	
	@RequestMapping("/brand_id_data")
	@ResponseBody
	
	public Result brand_id_data(String beginDate,String endDate,String sort){
		List<ArticleSellDto> list = orderService.selectBrandArticleSellByDateAndId(getCurrentBrandId(),beginDate, endDate,sort);
		return getSuccessResult(list);
	}
	
//	@RequestMapping("/brand_familyId_data")
//	@ResponseBody
//	
//	public Result brand_familyId_data(String beginDate,String endDate,String sort){
//		List<ArticleSellDto> list = orderService.selectBrandArticleSellByDateAndFamilyId(getCurrentBrandId(),beginDate, endDate, sort);
//		return getSuccessResult(list);
//	}
//	
	
	
	@RequestMapping("/brand_excel")
	@ResponseBody
	public void reportBrandExcel(String beginDate,String endDate,String selectValue,String sort,HttpServletRequest request, HttpServletResponse response){
		//导出文件名
		String fileName = "菜品销售报表"+beginDate+"至"+endDate+".xls";
		//定义读取文件的路径
		String path = request.getSession().getServletContext().getRealPath(fileName);
		//定义列
		String[]columns={"articleFamilyName","articleName","brandSellNum","","",""};
		//定义数据
		List<ArticleSellDto> result = null;
		//定义一个map用来存数据表格的前四项,1.报表类型,2.品牌名称3,.店铺名称4.日期
		Map<String,String> map = new HashMap<>();
		String shopName="";
		for (ShopDetail shopDetail : getCurrentShopDetails()) {
			shopName += shopDetail.getName()+",";
		}
		//去掉最后一个逗号
		shopName.substring(0, shopName.length()-1);
		map.put("brandName", getBrandName());
		map.put("shops", shopName);
		map.put("beginDate", beginDate);
		map.put("reportType", "品牌菜品销售报表");//表的头，第一行内容
		map.put("endDate", endDate);
		map.put("num", "2");//显示的位置
		map.put("reportTitle", "品牌菜品销售");//表的名字
		map.put("timeType", "yyyy-MM-dd");
		
		//定义excel表格的表头
		if(selectValue==null||"".equals(selectValue)){
			selectValue="全部";
			//result = orderService.selectBrandArticleSellByDate(beginDate, endDate,sort);
		}else{
			//根据菜品分类的名称获取菜品分类的id
			String articleFamilyId = articleFamilyService.selectByName(selectValue);
			result = orderService.selectBrandArticleSellByDateAndArticleFamilyId(beginDate, endDate,articleFamilyId,sort);
		}
		String[][] headers = {{"菜品分类("+selectValue+")","25"},{"菜品名称","25"},{"菜品销量(份)","25"}};
		
		//定义excel工具类对象
		ExcelUtil<ArticleSellDto> excelUtil=new ExcelUtil<ArticleSellDto>();
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
	@RequestMapping("/brand_articlefamily_excel")
	@ResponseBody
	public void reportBrandArticleExcel(String beginDate,String endDate,String selectValue,String sort,HttpServletRequest request, HttpServletResponse response){
		//导出文件名
		String fileName = "菜品分类销售报表"+beginDate+"至"+endDate+".xls";
		//定义读取文件的路径
		String path = request.getSession().getServletContext().getRealPath(fileName);
		//定义列
		String[]columns={"articleFamilyName","brandSellNum","salles","salesRatio","totalMoney"};
		//定义数据
		List<ArticleSellDto> result = null;
		//定义一个map用来存数据表格的前四项,1.报表类型,2.品牌名称3,.店铺名称4.日期
		Map<String,String> map = new HashMap<>();
		Brand brand = brandServie.selectById(getCurrentBrandId());
		//获取店铺名称
		List<ShopDetail> shops = shopDetailService.selectByBrandId(getCurrentBrandId());
		String shopName="";
		for (ShopDetail shopDetail : shops) {
			shopName += shopDetail.getName()+",";
		}
		//去掉最后一个逗号
		shopName.substring(0, shopName.length()-1);
		map.put("brandName", brand.getBrandName());
		map.put("shops", shopName);
		map.put("beginDate", beginDate);
		map.put("reportType", "品牌菜品销售报表");//表的头，第一行内容
		map.put("endDate", endDate);
		map.put("num", "2");//显示的位置
		map.put("reportTitle", "品牌菜品销售");//表的名字
		map.put("timeType", "yyyy-MM-dd");
		
		//定义excel表格的表头
//		if(selectValue==null||"".equals(selectValue)){
//			selectValue="全部";
//			result = orderService.selectBrandArticleSellByDateAndFamilyId(getCurrentBrandId(), beginDate, endDate, sort);
//		}else{
//			result = orderService.selectArticleFamilyByBrandAndFamilyName(getCurrentBrandId(),beginDate, endDate,selectValue);
//		}
		
		//暂时先做下载全部
		result = orderService.selectBrandArticleSellByDateAndFamilyId(getCurrentBrandId(), beginDate, endDate, sort);
		String[][] headers = {{"菜品分类("+selectValue+")","25"},{"菜品销量(份)","25"},{"菜品销售额(元)","25"},{"菜品销售占比","25"}};
		
		//定义excel工具类对象
		ExcelUtil<ArticleSellDto> excelUtil=new ExcelUtil<ArticleSellDto>();
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
	
	@RequestMapping("/brand_articleId_excel")
	@ResponseBody
	public void reportBrandArticleIdExcel(String beginDate,String endDate,String selectValue,String sort,HttpServletRequest request, HttpServletResponse response){
		//导出文件名
		String fileName = "品牌菜品销售记录详情报表"+beginDate+"至"+endDate+".xls";
		//定义读取文件的路径
		String path = request.getSession().getServletContext().getRealPath(fileName);
		//定义列
		String[]columns={"articleFamilyName","articleName","brandSellNum","numRatio","salles","salesRatio"};
		//定义数据
		List<ArticleSellDto> result = null;
		//定义一个map用来存数据表格的前四项,1.报表类型,2.品牌名称3,.店铺名称4.日期
		Map<String,String> map = new HashMap<>();
		Brand brand = brandServie.selectById(getCurrentBrandId());
		//获取店铺名称
		List<ShopDetail> shops = shopDetailService.selectByBrandId(getCurrentBrandId());
		String shopName="";
		for (ShopDetail shopDetail : shops) {
			shopName += shopDetail.getName()+",";
		}
		//去掉最后一个逗号
		shopName.substring(0, shopName.length()-1);
		map.put("brandName", brand.getBrandName());
		map.put("shops", shopName);
		map.put("beginDate", beginDate);
		map.put("reportType", "品牌菜品销售详情报表");//表的头，第一行内容
		map.put("endDate", endDate);
		map.put("num", "5");//显示的位置
		map.put("reportTitle", "品牌菜品销售详情");//表的名字
		map.put("timeType", "yyyy-MM-dd");
		
		//定义excel表格的表头
//		if(selectValue==null||"".equals(selectValue)){
//			selectValue="全部";
//			result = orderService.selectBrandArticleSellByDateAndId(getCurrentBrandId(), beginDate, endDate, sort);
//		}else{
//			//根据菜品分类的名称获取菜品分类的id
//			String articleFamilyId = articleFamilyService.selectByName(selectValue);
//			result = orderService.selectBrandFamilyArticleSellByDateAndArticleFamilyId(getCurrentBrandId(),beginDate, endDate,articleFamilyId,sort);
//		}
		//暂时查全部
		result = orderService.selectBrandArticleSellByDateAndId(getCurrentBrandId(), beginDate, endDate, sort);
		String[][] headers = {{"分类","25"},{"菜名","25"},{"销量(份)","25"},{"销量占比","25"},{"销售额(元)","25"},{"销售占比","25"}};

		//定义excel工具类对象
		ExcelUtil<ArticleSellDto> excelUtil=new ExcelUtil<ArticleSellDto>();
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
	
	
	@RequestMapping("/shop_articleId_excel")
	@ResponseBody
	public void reportShopSumArticleIdExcel(String beginDate,String endDate,String sort,HttpServletRequest request, HttpServletResponse response){
		//导出文件名
		String fileName = "店铺菜品销售记录报表"+beginDate+"至"+endDate+".xls";
		//定义读取文件的路径
		String path = request.getSession().getServletContext().getRealPath(fileName);
		//定义列
		String[]columns={"shopName","totalNum","sellIncome","occupy"};
		//定义数据
		List<ShopArticleReportDto> result = null;
		//定义一个map用来存数据表格的前四项,1.报表类型,2.品牌名称3,.店铺名称4.日期
		Map<String,String> map = new HashMap<>();
		//获取店铺名称
		String shopName="";
		for (ShopDetail shopDetail : getCurrentShopDetails()) {
			shopName += shopDetail.getName()+",";
		}
		//去掉最后一个逗号
		shopName.substring(0, shopName.length()-1);
		map.put("brandName", getBrandName());
		map.put("shops", shopName);
		map.put("beginDate", beginDate);
		map.put("reportType", "店铺菜品销售报表");//表的头，第一行内容
		map.put("endDate", endDate);
		map.put("num", "3");//显示的位置
		map.put("reportTitle", "店铺菜品销售");//表的名字
		map.put("timeType", "yyyy-MM-dd");
		
		//定义excel表格的表头
//		if(selectValue==null||"".equals(selectValue)){
//			selectValue="全部";
//			result = orderService.selectBrandArticleSellByDateAndId(getCurrentBrandId(), beginDate, endDate, sort);
//		}else{
//			//根据菜品分类的名称获取菜品分类的id
//			String articleFamilyId = articleFamilyService.selectByName(selectValue);
//			result = orderService.selectBrandFamilyArticleSellByDateAndArticleFamilyId(getCurrentBrandId(),beginDate, endDate,articleFamilyId,sort);
//		}
		//暂时查全部
		result = orderService.selectShopArticleDetails(beginDate,endDate,getCurrentBrandId(),getCurrentShopDetails());
		String[][] headers = {{"店铺名称","25"},{"菜品销量(份)","25"},{"菜品销售额","25"},{"销售额占比","25"}};
		
		//定义excel工具类对象
		ExcelUtil<ShopArticleReportDto> excelUtil=new ExcelUtil<ShopArticleReportDto>();
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
	
	
	
	
	@RequestMapping("/shop_excel")
	@ResponseBody
	public void reportShopArticleExcel(String beginDate,String endDate,String shopId,String sort,HttpServletRequest request, HttpServletResponse response){
		//获取店铺名称
		ShopDetail shopDetail = shopDetailService.selectById(shopId);
		//导出文件名
		String fileName = shopDetail.getName()+"菜品销售报表"+beginDate+"至"+endDate+".xls";
		//定义读取文件的路径
		String path = request.getSession().getServletContext().getRealPath(fileName);
		//定义列
		String[]columns={"articleFamilyName","articleName","shopSellNum","numRatio","salles","salesRatio"};
		//定义数据
		List<ArticleSellDto> result = new ArrayList<>();
		Brand brand = brandServie.selectById(getCurrentBrandId());
		Map<String,String> map = new HashMap<>();
		map.put("brandName", brand.getBrandName());
		map.put("shops", shopDetail.getName());
		map.put("beginDate", beginDate);
		map.put("reportType", "店铺菜品销售报表");//表的头，第一行内容
		map.put("endDate", endDate);
		map.put("num", "5");//显示的位置
		map.put("reportTitle", "店铺菜品销售");//表的名字
		map.put("timeType", "yyyy-MM-dd");
		
		
		
//		if(selectValue==null||"".equals(selectValue)){
//			selectValue="全部";
//			result = orderService.selectShopArticleSellByDate(beginDate, endDate, shopId, sort);
//		}else{
//			//根据菜品分类的名称获取菜品分类的id
//			String articleFamilyId = articleFamilyService.selectByName(selectValue);
//			result = orderService.selectShopArticleSellByDateAndArticleFamilyId(beginDate, endDate,shopId,articleFamilyId,sort);
//		}
		//暂时先查全部
		result = orderService.selectShopArticleSellByDate(beginDate, endDate, shopId, sort);
		
//		for (ArticleSellDto articleSellDto : result) {
//			articleSellDto.setSalesRatio( Double.parseDouble(articleSellDto.getSalesRatio())*100+"%");
//		}
		
		
		//String[][] headers = {{"菜品分类("+selectValue+")","22"},{"菜品名称","20"},{"菜品销量(份)","20"},{"品牌菜品销量(份)","20"},{"销售占比(%)","20"}};
		String[][] headers = {{"分类","22"},{"菜名","20"},{"销量(份)","20"},{"销量占比","20"},{"销售额","20"},{"销售额占比","20"}};
		
		
		//定义excel工具类对象
		ExcelUtil<ArticleSellDto> excelUtil=new ExcelUtil<ArticleSellDto>();
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
	
//	@RequestMapping("/shop_articleId_excel")
//	@ResponseBody
//	public void reportShopArticleIdExcel(String beginDate,String endDate,String selectValue,String sort,HttpServletRequest request, HttpServletResponse response){
//		//导出文件名
//		String fileName = "菜品销售记录报表"+beginDate+"至"+endDate+".xls";
//		//定义读取文件的路径
//		String path = request.getSession().getServletContext().getRealPath(fileName);
//		//定义列
//		String[]columns={"articleFamilyName","articleName","shopSellNum","salles","salesRatio"};
//		//定义数据
//		List<ArticleSellDto> result = null;
//		//定义一个map用来存数据表格的前四项,1.报表类型,2.品牌名称3,.店铺名称4.日期
//		Map<String,String> map = new HashMap<>();
//		Brand brand = brandServie.selectById(getCurrentBrandId());
//		//获取店铺名称
//		List<ShopDetail> shops = shopDetailService.selectByBrandId(getCurrentBrandId());
//		String shopName="";
//		for (ShopDetail shopDetail : shops) {
//			shopName += shopDetail.getName()+",";
//		}
//		//去掉最后一个逗号
//		shopName.substring(0, shopName.length()-1);
//		map.put("brandName", brand.getBrandName());
//		map.put("shops", shopName);
//		map.put("beginDate", beginDate);
//		map.put("reportType", "品牌菜品销售报表");//表的头，第一行内容
//		map.put("endDate", endDate);
//		map.put("num", "2");//显示的位置
//		map.put("reportTitle", "品牌菜品销售");//表的名字
//		map.put("timeType", "yyyy-MM-dd");
//		
//		//定义excel表格的表头
////		if(selectValue==null||"".equals(selectValue)){
////			selectValue="全部";
////			result = orderService.selectBrandArticleSellByDateAndId(getCurrentBrandId(), beginDate, endDate, sort);
////		}else{
////			//根据菜品分类的名称获取菜品分类的id
////			String articleFamilyId = articleFamilyService.selectByName(selectValue);
////			result = orderService.selectBrandFamilyArticleSellByDateAndArticleFamilyId(getCurrentBrandId(),beginDate, endDate,articleFamilyId,sort);
////		}
////		String[][] headers = {{"菜品分类("+selectValue+")","25"},{"菜品名称","25"},{"菜品销量(份)","25"},{"菜品销售额(元)","25"},{"菜品销售占比","25"}};
////		
//		//定义excel工具类对象
//		ExcelUtil<ArticleSellDto> excelUtil=new ExcelUtil<ArticleSellDto>();
//		try{
//			OutputStream out = new FileOutputStream(path);
//			//excelUtil.ExportExcel(headers, columns, result, out, map);
//			out.close();
//			excelUtil.download(path, response);
//			JOptionPane.showMessageDialog(null, "导出成功！");
//			log.info("excel导出成功");
//		}catch(Exception e){
//			e.printStackTrace();
//		}
//	}
//	
//	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
//	@RequestMapping("/shop_excel")
//	@ResponseBody
//	public void reportShopExcel(String beginDate,String endDate,String selectValue,String shopId,String sort,HttpServletRequest request, HttpServletResponse response){
//		//获取店铺名称
//		ShopDetail shopDetail = shopDetailService.selectById(getCurrentShopId());
//		//导出文件名
//		String fileName = shopDetail.getName()+"菜品销售报表"+beginDate+"至"+endDate+".xls";
//		//定义读取文件的路径
//		String path = request.getSession().getServletContext().getRealPath(fileName);
//		//定义列
//		String[]columns={"articleFamilyName","articleName","shopSellNum","brandSellNum","salesRatio"};
//		//定义数据
//		List<ArticleSellDto> result = new ArrayList<>();
//		Brand brand = brandServie.selectById(getCurrentBrandId());
//		Map<String,String> map = new HashMap<>();
//		map.put("brandName", brand.getBrandName());
//		map.put("shops", shopDetail.getName());
//		map.put("beginDate", beginDate);
//		map.put("reportType", "店铺菜品销售报表");//表的头，第一行内容
//		map.put("endDate", endDate);
//		map.put("num", "4");//显示的位置
//		map.put("reportTitle", "店铺菜品销售");//表的名字
//		map.put("timeType", "yyyy-MM-dd");
//		
//		if(selectValue==null||"".equals(selectValue)){
//			selectValue="全部";
//			result = orderService.selectShopArticleSellByDate(beginDate, endDate, shopId, sort);
//		}else{
//			//根据菜品分类的名称获取菜品分类的id
//			String articleFamilyId = articleFamilyService.selectByName(selectValue);
//			result = orderService.selectShopArticleSellByDateAndArticleFamilyId(beginDate, endDate,shopId,articleFamilyId,sort);
//		}
//		for (ArticleSellDto articleSellDto : result) {
//			articleSellDto.setSalesRatio( Double.parseDouble(articleSellDto.getSalesRatio())*100+"%");
//		}
//		String[][] headers = {{"菜品分类("+selectValue+")","22"},{"菜品名称","20"},{"菜品销量(份)","20"},{"品牌菜品销量(份)","20"},{"销售占比(%)","20"}};
//		//定义excel工具类对象
//		ExcelUtil<ArticleSellDto> excelUtil=new ExcelUtil<ArticleSellDto>();
//		try{
//			OutputStream out = new FileOutputStream(path);
//			excelUtil.ExportExcel(headers, columns, result, out, map);
//			out.close();
//			excelUtil.download(path, response);
//			JOptionPane.showMessageDialog(null, "导出成功！");
//			log.info("excel导出成功");
//		}catch(Exception e){
//			e.printStackTrace();
//		}
//		
//	}
//	
	
}
