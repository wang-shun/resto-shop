 package com.resto.shop.web.controller.business;



import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
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
import com.resto.brand.core.util.DateUtil;
import com.resto.brand.core.util.ExcelUtil;
import com.resto.brand.web.dto.AppraiseDto;
import com.resto.brand.web.dto.AppraiseShopDto;
import com.resto.brand.web.model.Brand;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.service.OrderService;

@Controller
@RequestMapping("appraiseReport")
public class appraiseReportController extends GenericController{
	
	@Resource
	private OrderService orderService;
	
	@Resource
	private BrandService brandService;
	@Resource
	private ShopDetailService shopDetailService;
	
	@RequestMapping("/list")
    public void list(){
    }
	
	
	@RequestMapping("/brand_data")
	@ResponseBody
	public Result selectMoneyAndNumByDate(String beginDate,String endDate){
		
		return this.getResult(beginDate, endDate);
	}


	private Result getResult(String beginDate, String endDate) {
		
		return getSuccessResult(this.getSuccess(beginDate, endDate));
	}

	private Map<String,Object> getSuccess(String beginDate,String endDate){
		List<Order> olist =  orderService.selectListBybrandId(beginDate,endDate,getCurrentBrandId());
		Brand brand = brandService.selectById(getCurrentBrandId());
		
		int appraiseNum=0;//评价单数
		int totalNum = 0;//已消费订单数
		
		BigDecimal orderMoney = BigDecimal.ZERO;//订单总额
		BigDecimal redMoney = BigDecimal.ZERO;//评论红包总额
		
		int oneStart=0;
		int twoStart=0;
		int threeStart=0;
		int fourStart=0;
		int fiveStart=0;
		
		//品牌数据
		AppraiseDto brandAppraise = new AppraiseDto();
		
		for (Order o : olist) {
			//评价的的单数 //评价红包
			if(o.getAppraise()!=null){
				appraiseNum++;
				redMoney = add(redMoney, o.getAppraise().getRedMoney());
				
				if(o.getAppraise().getLevel()==1){
					oneStart++;
				}
				if(o.getAppraise().getLevel()==2){
					twoStart++;
				}
				if(o.getAppraise().getLevel()==3){
					threeStart++;
				}
				if(o.getAppraise().getLevel()==4){
					fourStart++;
				}
				if(o.getAppraise().getLevel()==5){
					fiveStart++;
				}
			}
			//消费的总单数
			totalNum++;
			//消费的总金额
			orderMoney = add(orderMoney,o.getOrderMoney());
			
		}
		
		//设置品牌
		brandAppraise.setBrandName(brand.getBrandName());
		
		//设置评价单数
		brandAppraise.setAppraiseNum(appraiseNum);
		
		//设置评价率
		brandAppraise.setAppraiseRatio(topercent(appraiseNum, totalNum));
		
		//设置订单的总额
		brandAppraise.setTotalMoney(orderMoney);
		
		//设置红包的总额
		
		brandAppraise.setRedMoney(redMoney);
		
		//设置五星,四星...的数量
		brandAppraise.setOnestar(oneStart);
		brandAppraise.setTwostar(twoStart);
		brandAppraise.setThreestar(threeStart);
		brandAppraise.setFourstar(fourStart);
		brandAppraise.setFivestar(fiveStart);
		
		
		//店铺数据
		
		List<ShopDetail> shoplist =  shopDetailService.selectByBrandId(getCurrentBrandId());
		
		List<AppraiseDto> shopAppraiseList = new ArrayList<>();
		
		for (ShopDetail s : shoplist) {
			AppraiseDto shopAppraise = new AppraiseDto(s.getId(),s.getName(), appraiseNum, "", BigDecimal.ZERO, BigDecimal.ZERO, "", 0, 0, 0, 0, 0);
			//每个店铺设置默认值
			int appraiseNum2=0;//评价单数
			int totalNum2 = 0;//已消费订单数
			
			BigDecimal orderMoney2 = BigDecimal.ZERO;//订单总额
			BigDecimal redMoney2 = BigDecimal.ZERO;//评论红包总额
			
			//设置红包撬动率
			
			
			
			int oneStart2=0;
			int twoStart2=0;
			int threeStart2=0;
			int fourStart2=0;
			int fiveStart2=0;
			
			for (Order o : olist) {
				if(o.getShopDetailId().equals(s.getId())){
					//评价的的单数 //评价红包
					if(o.getAppraise()!=null){
						appraiseNum2++;
						redMoney2 = add(redMoney2, o.getAppraise().getRedMoney());
						
						if(o.getAppraise().getLevel()==1){
							oneStart2++;
						}
						if(o.getAppraise().getLevel()==2){
							twoStart2++;
						}
						if(o.getAppraise().getLevel()==3){
							threeStart2++;
						}
						if(o.getAppraise().getLevel()==4){
							fourStart2++;
						}
						if(o.getAppraise().getLevel()==5){
							fiveStart2++;
						}
					}
					//消费的总金额
					orderMoney2 = add(orderMoney2,o.getOrderMoney());
					//
					totalNum2++;
				}
				
			}
			
			//设置评价单数
			shopAppraise.setAppraiseNum(appraiseNum2);
			//设置评价率
			shopAppraise.setAppraiseRatio(topercent(appraiseNum2, totalNum2));
			//设置订单总额
			shopAppraise.setTotalMoney(orderMoney2);
			
			//设置红包的总额
			shopAppraise.setRedMoney(redMoney2);
			
			//设置星的数量
			shopAppraise.setOnestar(oneStart2);
			shopAppraise.setTwostar(twoStart2);
			shopAppraise.setThreestar(threeStart2);
			shopAppraise.setFourstar(fourStart2);
			shopAppraise.setFivestar(fiveStart2);
			shopAppraiseList.add(shopAppraise);
		}

		//把店铺和品牌的数据封装成map返回给前台
		
		Map<String,Object> map = new HashMap<>();
		map.put("brandAppraise", brandAppraise);
		map.put("shopAppraise", shopAppraiseList);
		return map;
	}

	private BigDecimal add(BigDecimal orderMoney, BigDecimal orderMoney2) {
		
		return orderMoney.add(orderMoney2);
	}
	
	//两个int类型的数据相除并得到百分比数据
	private static String topercent(int num1,int num2){
		double d1 = num1;
		double d2 = num2;
		double d3 = d1/d2;
		NumberFormat num = NumberFormat.getPercentInstance();   
	    num.setMaximumIntegerDigits(3);   
	    num.setMaximumFractionDigits(2);   
	    String result=num.format(d3);  
	    return result;
	}
	
	
	@RequestMapping("shopReport")
	public String showModal(String beginDate,String endDate,String shopId,HttpServletRequest request){
		request.setAttribute("beginDate", beginDate);
		request.setAttribute("endDate", endDate);
		ShopDetail shop = shopDetailService.selectById(shopId);
		if(shopId!=null){
			request.setAttribute("shopId", shopId);
			request.setAttribute("shopName", shop.getName());
		}
		return "appraiseReport/shopReport";
	}
	
	
	
	@RequestMapping("shop_data")
	@ResponseBody
	public List<Order> selectAppraiseByShopId(String beginDate,String endDate,String shopId){
		
		return orderService.selectAppraiseByShopId(beginDate,endDate,shopId);
	}
	
	
	@RequestMapping("brand_excel")
	@ResponseBody
	public void report_brandExcel (String beginDate,String endDate,HttpServletRequest request, HttpServletResponse response){
		
		//导出文件名
				String fileName = "品牌评论报表"+beginDate+"至"+endDate+".xls";
				//定义读取文件的路径
				String path = request.getSession().getServletContext().getRealPath(fileName);
				//定义列
				String[]columns={"name","appraiseNum","appraiseRatio","redMoney","totalMoney","fivestar","fourstar","threestar","twostar","onestar"};
				//定义数据
				 
				//定义一个map用来存数据表格的前四项,1.报表类型,2.品牌名称3,.店铺名称4.日期
				Map<String,String> map = new HashMap<>();
				Brand brand = brandService.selectById(getCurrentBrandId());
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
				map.put("reportType", "品牌评论报表");//表的头，第一行内容
				map.put("endDate", endDate);
				map.put("num", "9");//显示的位置
				map.put("reportTitle", "品牌评论");//表的名字
				map.put("timeType", "yyyy-MM-dd");
				Map<String, Object> appraiseMap = this.getSuccess(beginDate, endDate);
				List<AppraiseDto> result = new LinkedList<>();
				@SuppressWarnings("unchecked")
				List<AppraiseDto> list  =  (List<AppraiseDto>) appraiseMap.get("shopAppraise");
				for (AppraiseDto appraiseDto : list) {
					appraiseDto.setName(appraiseDto.getShopName());
				}
				result.addAll(list);
				AppraiseDto a = (AppraiseDto) appraiseMap.get("brandAppraise");
				a.setName(a.getBrandName());
				result.add(a);
				String[][] headers = {{"品牌","25"},{"评价单数","25"},{"评价率","25"},{"评论红包总额","25"},{"订单总额(元)","25"},{"五星评价","25"},{"四星评价","25"},{"三星评价","25"},{"二星评价","25"},{"一星评价","25"}};
				//定义excel工具类对象
				ExcelUtil<AppraiseDto> excelUtil=new ExcelUtil<AppraiseDto>();
				OutputStream out  = null;
				try{
					out = new FileOutputStream(path);
					excelUtil.ExportExcel(headers, columns, result, out, map);
					out.close();
					excelUtil.download(path, response);
					JOptionPane.showMessageDialog(null, "导出成功！");
					log.info("excel导出成功");
				}catch(Exception e){
					e.printStackTrace();
				}finally {  
		            if (out != null) {  
		                try {  
		                    out.close();  
		                } catch (IOException io) {  
		//log  
		                }  
		            }  
		
				}
	
	  }
	
	
	@RequestMapping("shop_excel")
	@ResponseBody
	public void report_shopExcel (String beginDate,String endDate,String shopId,HttpServletRequest request, HttpServletResponse response){
		//获取店铺名称
				ShopDetail s = shopDetailService.selectById(shopId);
		//导出文件名
		String fileName = "店铺评论报表"+beginDate+"至"+endDate+".xls";
		//定义读取文件的路径
		String path = request.getSession().getServletContext().getRealPath(fileName);
		//定义列
		String[]columns={"levelName","feedBack","createTime","telephone","orderMoney","redMoney","content"};
		//定义数据
		
		//定义一个map用来存数据表格的前四项,1.报表类型,2.品牌名称3,.店铺名称4.日期
		Map<String,String> map = new HashMap<>();
		Brand brand = brandService.selectById(getCurrentBrandId());
		
		//去掉最后一个逗号
		map.put("brandName", brand.getBrandName());
		map.put("shops", s.getName());
		map.put("beginDate", beginDate);
		map.put("reportType", "店铺评论报表");//表的头，第一行内容
		map.put("endDate", endDate);
		map.put("num", "6");//显示的位置
		map.put("reportTitle", "店铺评论");//表的名字
		map.put("timeType", "yyyy-MM-dd");
		List<AppraiseShopDto> result = new LinkedList<>();
		
		List<Order>  orderList = orderService.selectAppraiseByShopId(beginDate,endDate,shopId);
		
		for (Order order : orderList) {
			
			AppraiseShopDto a = new AppraiseShopDto();
			
			if(order.getAppraise().getLevel()!=null){
				switch (order.getAppraise().getLevel()) {
				case 1:
					a.setLevelName("一星");
					break;
				case 2:
					a.setLevelName("二星");
					break;
				case 3:
					a.setLevelName("三星");
					break;
				case 4:
					a.setLevelName("四星");
					break;
				case 5:
					a.setLevelName("五星");
					break;

				default:
					break;
				}
			}
			
			
			if(order.getCustomer().getTelephone()!=null){
				a.setTelephone(order.getCustomer().getTelephone());
			}
			
			
			if(order.getOrderMoney()!=null){
				a.setOrderMoney(order.getOrderMoney());
			}
			
			if(order.getAppraise().getFeedback()!=null){
				a.setFeedBack(order.getAppraise().getFeedback());
			}
			
			if(order.getAppraise().getContent()!=null){
				a.setContent(order.getAppraise().getContent());
			}
			
			if(order.getAppraise().getRedMoney()!=null){
				a.setRedMoney(order.getAppraise().getRedMoney());
			}
			
			if(order.getAppraise().getCreateTime()!=null){
				a.setCreateTime(DateUtil.formatDate(order.getAppraise().getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
			}
			
			result.add(a);
			
		}
		
		
		String[][] headers = {{"评分","25"},{"评论对象","25"},{"评论时间","25"},{"手机号","25"},{"订单金额(元)","25"},{"评论金额","25"},{"评论内容","25"}};
		//定义excel工具类对象
		ExcelUtil<AppraiseShopDto> excelUtil=new ExcelUtil<AppraiseShopDto>();
		OutputStream out  = null;
		try{
			out = new FileOutputStream(path);
			excelUtil.ExportExcel(headers, columns, result, out, map);
			out.close();
			excelUtil.download(path, response);
			JOptionPane.showMessageDialog(null, "导出成功！");
			log.info("excel导出成功");
		}catch(Exception e){
			e.printStackTrace();
		}finally {  
			if (out != null) {  
				try {  
					out.close();  
				} catch (IOException io) {  
					//log  
				}  
			}  
			
		}
		
	}
	
	
	
	
	
	
}
