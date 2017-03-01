 package com.resto.shop.web.controller.business;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.JOptionPane;

import com.alibaba.fastjson.JSONObject;
import com.resto.brand.core.util.AppendToExcelUtil;
import org.apache.commons.lang3.StringUtils;
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
import com.resto.shop.web.constant.ArticleType;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.service.ArticleFamilyService;
import com.resto.shop.web.service.ArticleService;
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
	
	@Resource
	ArticleService articleService;
	
	
	@RequestMapping("/list")
    public void list(){
    }
	
	@RequestMapping("/brandList")
    public void brandList(){
    }
	
	@RequestMapping("/shopList")
    public void shopList(){
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

    /**
     * 查询品牌销售报表
     * @param beginDate
     * @param endDate
     * @param type
     * @return
     */
	@RequestMapping("/queryOrderArtcile")
	@ResponseBody
	public Result queryOrderArtcile(String beginDate, String endDate, Integer type){
        JSONObject object = new JSONObject();
	    try {
            Map<String, Object> selectMap = new HashMap<String, Object>();
            brandArticleReportDto brandCount = orderService.selectBrandArticleNum(beginDate, endDate, getCurrentBrandId(), getBrandName());
            object.put("brandReport", brandCount);
            selectMap.put("beginDate", beginDate);
            selectMap.put("endDate", endDate);
            selectMap.put("type", ArticleType.SIMPLE_ARTICLE);
            List<ArticleSellDto> articleUnitSellDtos = articleService.queryOrderArtcile(selectMap);
            List<ArticleSellDto> articleUnitSell = articleService.selectArticleByType(selectMap);
            Map<String, Object> unitMap = articleService.selectArticleOrderCount(selectMap);
            for (ArticleSellDto articleUnitSellDto : articleUnitSellDtos){
                if (unitMap.get("sellNum").toString().equalsIgnoreCase("0")){
                    articleUnitSellDto.setNumRatio("0.00%");
                }else{
                    articleUnitSellDto.setNumRatio(new BigDecimal(articleUnitSellDto.getBrandSellNum()).divide(new BigDecimal(
                            unitMap.get("sellNum").toString()),2,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)) +"%");
                }
                if (unitMap.get("salles").toString().equalsIgnoreCase("0")){
                    articleUnitSellDto.setSalesRatio("0.00%");
                }else{
                    articleUnitSellDto.setSalesRatio(articleUnitSellDto.getSalles().divide(new BigDecimal(
                            unitMap.get("salles").toString()),2,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)) +"%");
                }
                for (ArticleSellDto articleSellDto : articleUnitSell){
                    if (articleSellDto.getArticleId().equalsIgnoreCase(articleUnitSellDto.getArticleId())){
                        articleUnitSell.remove(articleSellDto);
                        break;
                    }
                }
            }
            for (ArticleSellDto articleSellDto : articleUnitSell){
                articleSellDto.setBrandSellNum(0);
                articleSellDto.setNumRatio("0.00%");
                articleSellDto.setSalles(BigDecimal.ZERO);
                articleSellDto.setSalesRatio("0.00%");
                articleSellDto.setDiscountMoney(BigDecimal.ZERO);
                articleSellDto.setRefundCount(0);
                articleSellDto.setRefundTotal(BigDecimal.ZERO);
                articleUnitSellDtos.add(articleSellDto);
            }
            object.put("brandArticleUnit", articleUnitSellDtos);
            selectMap.put("type", ArticleType.TOTAL_ARTICLE);
            List<ArticleSellDto> articleFamilySellDtos = articleService.queryOrderArtcile(selectMap);
            List<ArticleSellDto> articleFamilySell = articleService.selectArticleByType(selectMap);
            Map<String, Object> familyMap = articleService.selectArticleOrderCount(selectMap);
            for (ArticleSellDto articleFamilySellDto : articleFamilySellDtos){
                if (familyMap.get("sellNum").toString().equalsIgnoreCase("0")){
                    articleFamilySellDto.setNumRatio("0.00%");
                }else{
                    articleFamilySellDto.setNumRatio(new BigDecimal(articleFamilySellDto.getBrandSellNum()).divide(new BigDecimal(
                            familyMap.get("sellNum").toString()),2,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)) +"%");
                }
                if (familyMap.get("salles").toString().equalsIgnoreCase("0")){
                    articleFamilySellDto.setSalesRatio("0.00%");
                }else{
                    articleFamilySellDto.setSalesRatio(articleFamilySellDto.getSalles().divide(new BigDecimal(
                            familyMap.get("salles").toString()),2,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)) +"%");
                }
                for (ArticleSellDto articleSellDto : articleFamilySell){
                    if (articleSellDto.getArticleId().equalsIgnoreCase(articleFamilySellDto.getArticleId())){
                        articleFamilySell.remove(articleSellDto);
                        break;
                    }
                }
            }
            for (ArticleSellDto articleSellDto : articleFamilySell){
                articleSellDto.setBrandSellNum(0);
                articleSellDto.setNumRatio("0.00%");
                articleSellDto.setSalles(BigDecimal.ZERO);
                articleSellDto.setSalesRatio("0.00%");
                articleSellDto.setDiscountMoney(BigDecimal.ZERO);
                articleSellDto.setRefundCount(0);
                articleSellDto.setRefundTotal(BigDecimal.ZERO);
                articleFamilySellDtos.add(articleSellDto);
            }
            object.put("brandArticleFamily", articleFamilySellDtos);
        }catch (Exception e){
            log.error("查询菜品销售报表出错！");
            e.printStackTrace();
            return new Result(false);
        }
		return getSuccessResult(object);
	}
	
	@RequestMapping("/showMealAttr")
	public String showMealAttr(HttpServletRequest request, String articleId, String beginDate, String endDate, String shopId){
		request.setAttribute("articleId", articleId);
		request.setAttribute("beginDate", beginDate);
		request.setAttribute("endDate", endDate);
		request.setAttribute("shopId", shopId);
		return "articleSell/mealAttr";
	}
	
	@RequestMapping("/showShopArticle")
	public String showShopArticle(HttpServletRequest request, String beginDate, String endDate, String shopId){
		request.setAttribute("beginDate", beginDate);
		request.setAttribute("endDate", endDate);
		request.setAttribute("shopId", shopId);
		return "articleSell/shopArticle";
	}

    /**
     * 查询套餐子项销量
     * @param articleId
     * @param beginDate
     * @param endDate
     * @param shopId
     * @return
     */
	@RequestMapping("/queryArticleMealAttr")
	@ResponseBody
	public Result queryArticleMealAttr(String articleId, String beginDate, String endDate, String shopId){
		Map<String, Object> selectMap = new HashMap<String, Object>();
		selectMap.put("articleId", articleId);
		selectMap.put("beginDate", beginDate);
		selectMap.put("endDate", endDate);
		List<ArticleSellDto> articleSellDtos = articleService.queryArticleMealAttr(selectMap);
		return getSuccessResult(articleSellDtos);
	}
	
	/**
	 * 生成品牌菜品销售表(单品/套餐)
	 */
	@RequestMapping("/createBrnadArticle")
	@ResponseBody
	public Result createBrnadArticle(HttpServletRequest request, ArticleSellDto articleSellDto,
			String beginDate, String endDate, Integer type){
		//导出文件名
		String fileName = null;
		//定义读取文件的路径
		String path = null;
		Brand brand = brandServie.selectById(getCurrentBrandId());//定义列
		String[]columns={"typeName","articleFamilyName","articleName","brandSellNum","numRatio","salles","discountMoney","salesRatio","refundCount","refundTotal","likes"};
		String[][] headers = {{"品牌名称/菜品类型","25"},{"菜名类别","25"},{"菜品名称","25"},{"销量(份)","25"},{"销量占比","25"},{"销售额(元)","25"},{"折扣金额(元)","25"},{"销售额占比","25"},{"退菜数量","25"},{"退菜金额","25"},{"点赞数量","25"}};
		//获取店铺名称
		List<ShopDetail> shops = shopDetailService.selectByBrandId(getCurrentBrandId());
		String shopName="";
		for (ShopDetail shopDetail : shops) {
			shopName += shopDetail.getName()+",";
		}
		//去掉最后一个逗号
        shopName = shopName.substring(0, shopName.length()-1);
		//定义数据
		List<ArticleSellDto> result = new ArrayList<ArticleSellDto>();
        if (articleSellDto.getBrandReport() != null) {
            ArticleSellDto brandReport = new ArticleSellDto();
            Map<String, Object> brandReportMap = articleSellDto.getBrandReport();
            brandReport.setTypeName(brandReportMap.get("brandName").toString());
            brandReport.setBrandSellNum(Integer.valueOf(brandReportMap.get("totalNum").toString()));
            brandReport.setSalles(new BigDecimal(brandReportMap.get("sellIncome").toString()));
            brandReport.setRefundCount(Integer.valueOf(brandReportMap.get("refundCount").toString()));
            brandReport.setRefundTotal(new BigDecimal(brandReportMap.get("refundTotal").toString()));
            brandReport.setDiscountMoney(new BigDecimal(brandReportMap.get("discountTotal").toString()));
            result.add(brandReport);
        }
		Map<String,String> map = new HashMap<>();
		//定义一个map用来存数据表格的前四项,1.报表类型,2.品牌名称3,.店铺名称4.日期
		map.put("brandName", brand.getBrandName());
		map.put("shops", shopName);
		map.put("beginDate", beginDate);
		map.put("endDate", endDate);
		map.put("num", "10");//显示的位置
		map.put("timeType", "yyyy-MM-dd");
		//如果是单品
		if(type.equals(ArticleType.SIMPLE_ARTICLE)){
			//导出文件名
			fileName = "品牌菜品销售报表(单品)"+beginDate+"至"+endDate+".xls";
			path = request.getSession().getServletContext().getRealPath(fileName);
			map.put("reportType", "品牌菜品销售报表(单品)");//表的头，第一行内容
			map.put("reportTitle", "品牌菜品销售报表(单品)");//表的名字
			//定义数据
            if(articleSellDto.getBrandArticleUnit() != null) {
                for (Map articleMap : articleSellDto.getBrandArticleUnit()) {
                    ArticleSellDto article = new ArticleSellDto();
                    article.setTypeName(articleMap.get("typeName").toString());
                    article.setArticleFamilyName(articleMap.get("articleFamilyName").toString());
                    article.setArticleName(articleMap.get("articleName").toString());
                    article.setBrandSellNum(Integer.valueOf(articleMap.get("brandSellNum").toString()));
                    article.setNumRatio(articleMap.get("numRatio").toString());
                    article.setSalles(new BigDecimal(articleMap.get("salles").toString()));
                    article.setSalesRatio(articleMap.get("salesRatio").toString());
                    article.setDiscountMoney(new BigDecimal(articleMap.get("discountMoney").toString()));
                    article.setRefundCount(Integer.valueOf(articleMap.get("refundCount").toString()));
                    article.setRefundTotal(new BigDecimal(articleMap.get("refundTotal").toString()));
                    article.setLikes(Integer.valueOf(articleMap.get("likes").toString()));
                    result.add(article);
                }
            }
		}else if(type.equals(ArticleType.TOTAL_ARTICLE)){
			//导出文件名
			fileName = "品牌菜品销售报表(套餐)"+beginDate+"至"+endDate+".xls";
			path = request.getSession().getServletContext().getRealPath(fileName);
			map.put("reportType", "品牌菜品销售报表(套餐)");//表的头，第一行内容
			map.put("reportTitle", "品牌菜品销售报表(套餐)");//表的名字
			//定义数据
            if(articleSellDto.getBrandArticleFamily() != null) {
                for (Map articleMap : articleSellDto.getBrandArticleFamily()) {
                    ArticleSellDto article = new ArticleSellDto();
                    article.setTypeName(articleMap.get("typeName").toString());
                    article.setArticleFamilyName(articleMap.get("articleFamilyName").toString());
                    article.setArticleName(articleMap.get("articleName").toString());
                    article.setBrandSellNum(Integer.valueOf(articleMap.get("brandSellNum").toString()));
                    article.setNumRatio(articleMap.get("numRatio").toString());
                    article.setSalles(new BigDecimal(articleMap.get("salles").toString()));
                    article.setSalesRatio(articleMap.get("salesRatio").toString());
                    article.setDiscountMoney(new BigDecimal(articleMap.get("discountMoney").toString()));
                    article.setRefundCount(Integer.valueOf(articleMap.get("refundCount").toString()));
                    article.setRefundTotal(new BigDecimal(articleMap.get("refundTotal").toString()));
                    article.setLikes(Integer.valueOf(articleMap.get("likes").toString()));
                    result.add(article);
                }
            }
		}
		//定义excel工具类对象
		ExcelUtil<ArticleSellDto> excelUtil=new ExcelUtil<ArticleSellDto>();
		try{
			OutputStream out = new FileOutputStream(path);
			excelUtil.ExportExcel(headers, columns, result, out, map);
			out.close();
		}catch(Exception e){
		    log.error("生成菜品销售报表出错！");
			e.printStackTrace();
            return new Result(false);
		}
		return getSuccessResult(path);
	}

    /**
     * 向报表追加记录
     * @param path
     * @param startPosition
     * @param type
     * @param articleSellDto
     * @return
     */
	@RequestMapping("/appendToExcel")
    @ResponseBody
	public Result appendToExcel(String path, Integer startPosition, Integer type, ArticleSellDto articleSellDto){
	    try{
            String[][] items;
            items =new String[articleSellDto.getBrandArticleUnit().size()][];
            int i = 0;
            //如果是单品
            if(type.equals(ArticleType.SIMPLE_ARTICLE)){
                //定义数据
                for(Map articleMap : articleSellDto.getBrandArticleUnit()){
                    items[i] = new String[11];
                    items[i][0] = articleMap.get("typeName").toString();
                    items[i][1] = articleMap.get("articleFamilyName").toString();
                    items[i][2] = articleMap.get("articleName").toString();
                    items[i][3] = articleMap.get("brandSellNum").toString();
                    items[i][4] = articleMap.get("numRatio").toString();
                    items[i][5] = articleMap.get("salles").toString();
                    items[i][6] = articleMap.get("salesRatio").toString();
                    items[i][7] = articleMap.get("discountMoney").toString();
                    items[i][8] = articleMap.get("refundCount").toString();
                    items[i][9] = articleMap.get("refundTotal").toString();
                    items[i][10] = articleMap.get("likes").toString();
                    i++;
                }
            }else if(type.equals(ArticleType.TOTAL_ARTICLE)){
                //定义数据
                for(Map articleMap : articleSellDto.getBrandArticleFamily()){
                    items[i][0] = articleMap.get("typeName").toString();
                    items[i][1] = articleMap.get("articleFamilyName").toString();
                    items[i][2] = articleMap.get("articleName").toString();
                    items[i][3] = articleMap.get("brandSellNum").toString();
                    items[i][4] = articleMap.get("numRatio").toString();
                    items[i][5] = articleMap.get("salles").toString();
                    items[i][6] = articleMap.get("salesRatio").toString();
                    items[i][7] = articleMap.get("discountMoney").toString();
                    items[i][8] = articleMap.get("refundCount").toString();
                    items[i][9] = articleMap.get("refundTotal").toString();
                    items[i][10] = articleMap.get("likes").toString();
                    i++;
                }
            }
            AppendToExcelUtil.insertRows(path,startPosition,items);
        }catch (Exception e){
            log.error("追加菜品销售报表出粗！");
            e.printStackTrace();
            return new Result(false);
        }
	    return getSuccessResult();
    }

	/**
	 * 下载品牌菜品销售表
	 */
	@RequestMapping("/downloadBrnadArticle")
	@ResponseBody
	public void downloadBrnadArticle( HttpServletResponse response,String path){
		//定义excel工具类对象
		ExcelUtil<Object> excelUtil=new ExcelUtil<>();
		try{
			excelUtil.download(path, response);
			JOptionPane.showMessageDialog(null, "导出成功！");
			log.info("excel导出成功");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	@RequestMapping("/queryShopOrderArtcile")
	@ResponseBody
	public Result queryShopOrderArtcile(String beginDate, String endDate, Integer type, String shopId){
		Map<String, Object> selectMap = new HashMap<String, Object>();
		selectMap.put("beginDate", beginDate);
		selectMap.put("endDate", endDate);
		selectMap.put("type", type);
		selectMap.put("shopDetailId", shopId);
		List<ArticleSellDto> articleSellDtos = articleService.queryOrderArtcile(selectMap);
		return getSuccessResult(articleSellDtos);
	}
	
	/**
	 * 下载店铺菜品销售表(单品/套餐)
	 */
	@RequestMapping("/downloadShopArticle")
	@ResponseBody
	public void downloadShopArticle(HttpServletRequest request, HttpServletResponse response,
			String beginDate, String endDate, Integer type, String shopId){
		//查询条件
		Map<String, Object> selectMap = new HashMap<String, Object>();
		selectMap.put("beginDate", beginDate);
		selectMap.put("endDate", endDate);
		selectMap.put("type", type);
		selectMap.put("shopDetailId", shopId);
		//导出文件名
		String fileName = null;
		//定义读取文件的路径
		String path = null;
		Brand brand = brandServie.selectById(getCurrentBrandId());//定义列
		String[]columns={"typeName","articleFamilyName","articleName","shopSellNum","numRatio","salles","discountMoney","salesRatio","refundCount","refundTotal","likes"};
		String[][] headers = {{"菜品类型","25"},{"菜名类别","25"},{"菜品名称","25"},{"销量(份)","25"},{"销量占比","25"},{"销售额(元)","25"},{"折扣金额(元)","25"},{"销售额占比","25"},{"退菜数量","25"},{"退菜金额","25"},{"点赞数量","25"}};
		String shopName= shopDetailService.selectById(shopId).getName();
		//定义数据
		List<ArticleSellDto> result = new ArrayList<ArticleSellDto>();
		Map<String,String> map = new HashMap<>();
		//定义一个map用来存数据表格的前四项,1.报表类型,2.品牌名称3,.店铺名称4.日期
		map.put("brandName", brand.getBrandName());
		map.put("shops", shopName);
		map.put("beginDate", beginDate);
		map.put("endDate", endDate);
		map.put("num", "10");//显示的位置
		map.put("timeType", "yyyy-MM-dd");
		//如果是单品
		if(type.equals(ArticleType.SIMPLE_ARTICLE)){
			//导出文件名
			fileName = "店铺菜品销售报表(单品)"+beginDate+"至"+endDate+".xls";
			path = request.getSession().getServletContext().getRealPath(fileName);
			map.put("reportType", "店铺菜品销售报表(单品)");//表的头，第一行内容
			map.put("reportTitle", "店铺菜品销售报表(单品)");//表的名字
			//定义数据
			result = articleService.queryOrderArtcile(selectMap);
		}else if(type.equals(ArticleType.TOTAL_ARTICLE)){
			//导出文件名
			fileName = "店铺菜品销售报表(套餐)"+beginDate+"至"+endDate+".xls";
			path = request.getSession().getServletContext().getRealPath(fileName);
			map.put("reportType", "店铺菜品销售报表(套餐)");//表的头，第一行内容
			map.put("reportTitle", "店铺菜品销售报表(套餐)");//表的名字
			//定义数据
			result = articleService.queryOrderArtcile(selectMap);
		}
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

    /**
     * 查询菜品销售报表
     * @param beginDate
     * @param endDate
     * @return
     */
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
		String[]columns={"articleFamilyName","articleName","typeName","brandSellNum","numRatio","salles","salesRatio","refundCount","refundTotal"};
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
		map.put("num", "8");//显示的位置
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
		String[][] headers = {{"分类","25"},{"菜名","25"},{"菜品类型","25"},{"销量(份)","25"},{"销量占比","25"},{"销售额(元)","25"},{"销售占比","25"},{"退菜数量","25"},{"退菜金额","25"}};

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


    /**
     * 生成店铺销售报表
     * @param beginDate
     * @param endDate
     * @param sort
     * @param request
     * @param response
     */
	@RequestMapping("/shop_articleId_excel")
	@ResponseBody
	public void reportShopSumArticleIdExcel(String beginDate,String endDate,String sort,HttpServletRequest request, HttpServletResponse response){
		//导出文件名
		String fileName = "店铺菜品销售记录报表"+beginDate+"至"+endDate+".xls";
		//定义读取文件的路径
		String path = request.getSession().getServletContext().getRealPath(fileName);
		//定义列
		String[]columns={"shopName","totalNum","sellIncome","occupy","refundCount","refundTotal"};
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
		map.put("num", "5");//显示的位置
		map.put("reportTitle", "店铺菜品销售");//表的名字
		map.put("timeType", "yyyy-MM-dd");
		//暂时查全部
		result = orderService.selectShopArticleDetails(beginDate,endDate,getCurrentBrandId(),getCurrentShopDetails());
		String[][] headers = {{"店铺名称","25"},{"菜品销量(份)","25"},{"菜品销售额","25"},{"销售额占比","25"},{"退菜总数","25"},{"退菜金额","25"}};
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
		String[]columns={"articleFamilyName","articleName","typeName","shopSellNum","numRatio","salles","salesRatio","refundCount","refundTotal"};
		//定义数据
		List<ArticleSellDto> result = new ArrayList<>();
		Brand brand = brandServie.selectById(getCurrentBrandId());
		Map<String,String> map = new HashMap<>();
		map.put("brandName", brand.getBrandName());
		map.put("shops", shopDetail.getName());
		map.put("beginDate", beginDate);
		map.put("reportType", "店铺菜品销售报表");//表的头，第一行内容
		map.put("endDate", endDate);
		map.put("num", "7");//显示的位置
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
		String[][] headers = {{"分类","22"},{"菜名","20"},{"菜品类型","20"},{"销量(份)","20"},{"销量占比","20"},{"销售额","20"},{"销售额占比","20"},{"退菜总数","20"},{"退菜金额","20"}};
		
		
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
