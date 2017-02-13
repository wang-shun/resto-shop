package com.resto.shop.web.controller.business;

import com.alibaba.fastjson.JSONObject;
import com.resto.brand.core.entity.Result;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import com.resto.shop.web.controller.GenericController;

@Controller
@RequestMapping("/brandMarketing")
public class BrandMarketingController extends GenericController{

//	@Resource
//	private AccountLogService accountLogService;

//	@RequestMapping("/list")
//	public void list(){}

//	@RequestMapping("/selectAll")
//	@ResponseBody
//	public Result list_all(String beginDate, String endDate){
//		Result result = new Result();
//		try{
//			Map<String, String> selectMap = new HashMap<String, String>();
//			selectMap.put("beginDate", beginDate);
//			selectMap.put("endDate", endDate);
//			JSONObject object = new JSONObject();
//			object.put("brandName", getBrandName());
//			object.put("plRedMoney", 0);
//			object.put("czRedMoney", 0);
//			object.put("fxRedMoney", 0);
//			object.put("dwRedMoney", 0);
//			object.put("tcRedMoney", 0);
//			object.put("zcCouponMoney", 0);
//			object.put("yqCouponMoney", 0);
//            object.put("birthCouponMoney",0);
//			BigDecimal redMoneyAll = new BigDecimal(0);
//			BigDecimal couponAllMoney = new BigDecimal(0);
//			List<String> brandMarketings = accountLogService.selectBrandMarketing(selectMap);
//			for(String brandMarketing : brandMarketings){
//				String[] results = brandMarketing.split(":");
//				if(results[0].equalsIgnoreCase("plRedMoney")){
//					object.put("plRedMoney", results[1]);
//					redMoneyAll = redMoneyAll.add(new BigDecimal(results[1]));
//				}else if(results[0].equalsIgnoreCase("czRedMoney")){
//					object.put("czRedMoney", results[1]);
//					redMoneyAll = redMoneyAll.add(new BigDecimal(results[1]));
//				}else if(results[0].equalsIgnoreCase("fxRedMoney")){
//					object.put("fxRedMoney", results[1]);
//					redMoneyAll = redMoneyAll.add(new BigDecimal(results[1]));
//				}else if(results[0].equalsIgnoreCase("dwRedMoney")){
//					object.put("dwRedMoney", results[1]);
//					redMoneyAll = redMoneyAll.add(new BigDecimal(results[1]));
//				}else if(results[0].equalsIgnoreCase("tcRedMoney")){
//					object.put("tcRedMoney", results[1]);
//					redMoneyAll = redMoneyAll.add(new BigDecimal(results[1]));
//				}else if(results[0].equalsIgnoreCase("zcCouponMoney")){
//					object.put("zcCouponMoney", results[1]);
//					couponAllMoney = couponAllMoney.add(new BigDecimal(results[1]));
//				}else if(results[0].equalsIgnoreCase("yqCouponMoney")){
//					object.put("yqCouponMoney", results[1]);
//					couponAllMoney = couponAllMoney.add(new BigDecimal(results[1]));
//				}else if(results[0].equalsIgnoreCase("birthCouponMoney")){
//				    object.put("birthCouponMoney",results[1]);
//                    couponAllMoney = couponAllMoney.add(new BigDecimal(results[1]));
//                }
//			}
//			object.put("redMoneyAll", redMoneyAll);
//			object.put("couponAllMoney", couponAllMoney);
//			return getSuccessResult(object);
//		}catch (Exception ex) {
//			log.error(ex.getMessage()+"查询营销报表出错!");
//			log.debug("查询营销报表出错!");
//			result.setSuccess(false);
//		}
//		return result;
//	}
//
//
//	@RequestMapping("/downloadBrandExcel")
//	@ResponseBody
//	public void downloadBrandExcel(String brandJson, String beginDate, String endDate, HttpServletRequest request, HttpServletResponse response){
//		//导出文件名
//		String fileName = "品牌营销报表"+beginDate+"至"+endDate+".xls";
//		//定义读取文件的路径
//		String path = request.getSession().getServletContext().getRealPath(fileName);
//		//定义数据
//		List<BrandMarketing> result = new ArrayList<BrandMarketing>();
//		result.add(JSON.parseObject(brandJson, BrandMarketing.class));
//		//定义列
//		String[]columns={"brandName","redMoneyAll","plRedMoney","czRedMoney","fxRedMoney","dwRedMoney","tcRedMoney","couponAllMoney","zcCouponMoney","yqCouponMoney","birthCouponMoney"};
//		//定义一个map用来存数据表格的前四项 1.报表类型,2.品牌名称,3.店铺名称4.日期
//		Map<String,String> map = new HashMap<>();
//		String shopName="";
//		for (ShopDetail shopDetail : getCurrentShopDetails()) {
//			shopName += shopDetail.getName()+",";
//		}
//		//去掉最后一个逗号
//		shopName.substring(0, shopName.length()-1);
//		map.put("brandName", getBrandName());
//		map.put("shops", shopName);
//		map.put("beginDate", beginDate);
//		map.put("reportType", "品牌营销报表");//表的头,第一行内容
//		map.put("endDate", endDate);
//		map.put("num", "10");//显示的位置
//		map.put("reportTitle", "品牌营销报表");//表的名字
//		map.put("timeType", "yyyy-MM-dd");
//
//		String[][] headers = {{"品牌名称","25"},{"红包总额(元)","25"},{"评论红包(元)","25"},{"充值赠送红包(元)","25"},{"分享返利红包(元)","25"},{"等位红包(元)","25"},{"退菜红包(元)","25"},{"优惠券总额(元)","25"},{"注册优惠券(元)","25"},{"邀请优惠券(元)","25"},{"生日优惠券(元)","25"}};
//
//		//定义excel工具类对象
//		ExcelUtil<BrandMarketing> excelUtil=new ExcelUtil<BrandMarketing>();
//		try{
//			OutputStream out = new FileOutputStream(path);
//			excelUtil.ExportExcel(headers, columns, result, out, map);
//			out.close();
//			excelUtil.download(path, response);
//			JOptionPane.showMessageDialog(null, "导出成功！");
//			log.debug("excel导出成功");
//		}catch(Exception e){
//			JOptionPane.showMessageDialog(null, "导出失败！");
//			log.error(e.getMessage()+"excel导出失败");
//			e.printStackTrace();
//		}
//	}

    @RequestMapping("/redList")
    public void redList(){}

    @RequestMapping("/selectRedList")
    public Result selectRedList(String grantBeginDate, String grantEndDate, String useBeginDate, String useEndDate, Integer redType){
        JSONObject object = new JSONObject();
        try{

        }catch (Exception e){
            return new Result(false);
        }
        return getSuccessResult(object);
    }

    @RequestMapping("/couponList")
    public void couponList(){}
}
