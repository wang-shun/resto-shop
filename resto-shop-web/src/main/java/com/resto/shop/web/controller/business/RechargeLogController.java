package com.resto.shop.web.controller.business;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.*;

import com.resto.brand.core.util.ExcelUtil;
import com.resto.brand.core.util.ExcelUtilShopDetail;
import com.resto.brand.web.dto.ShopDetailDto;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.brand.core.entity.Result;
import com.resto.brand.web.dto.RechargeLogDto;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.ChargeOrder;
import com.resto.shop.web.service.ChargeOrderService;
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
	ChargeOrderService chargeorderService;

	@Resource
	private CustomerService customerService;

	@RequestMapping("/list")
	public void  list(){
	}


	@RequestMapping("/shopRechargeLog")
	public String shopchargerecord(){

		return "recharge/shopchargerecord";
	}



	@RequestMapping("/queryShopchargecord")
    @ResponseBody
	public  List<ChargeOrder>  queryShopchargecord(String shopdetailid, String beginDate, String endDate){
        List<ShopDetail> shopDetailList = getCurrentShopDetails();

        for (ShopDetail fa:shopDetailList
             ) {
            System.out.println(fa.getName()+fa.getId()+"---------------");
        }
        List<ChargeOrder>  chargeList=chargeorderService.shopChargeCodes("31164cebcc4b422685e8d9a32db12ab8",beginDate,endDate);

        return chargeList ;
	}

    /**
     * 店铺详细下载报表
     * @return
     */

    public Map<String,Object> getResultSetDto(String shopDetailId,String beginDate,String endDate,String shopname){
           if(shopname==null) { shopname= getShopName(shopDetailId);}
		shopDetailId="31164cebcc4b422685e8d9a32db12ab8";
           Map<String,Object>  mapshopDetailDto= chargeorderService.shopChargeCodesSetDto(shopDetailId,beginDate,endDate,shopname);

        return  mapshopDetailDto;
    }
    @RequestMapping("shopDetail_excel")
	@ResponseBody
   public void reportShopDetail(String shopDetailId,String shopname,String beginDate, String endDate,
             HttpServletRequest request, HttpServletResponse response){
        shopDetailId="31164cebcc4b422685e8d9a32db12ab8";
        List<ShopDetailDto>  result = new LinkedList<>();

        Map<String,Object>  resultMap=this.getResultSetDto(shopDetailId,beginDate,endDate,shopname);
        result.addAll((Collection<? extends ShopDetailDto>) resultMap.get("shopDetailMap"));

        //导出文件名
        String fileName = "店铺充值记录"+beginDate+"至"+endDate
                +".xls";
        //定义读取文件的路径
        String path = request.getSession().getServletContext().getRealPath(fileName);
        //定义列
        String[]columns=
                         {"shopname","typeString","customerPhone","chargeMoney",
						"rewardMoney","finishTime","operationPhone"};
                        //定义数据


      //获取店铺名称
        Map<String,String> map = new HashMap<>();
        map.put("shops",getShopName(shopDetailId));
        String[][] headers = {{"店铺名字","25"},{"充值方式","25"},{"充值手机","25"},{"充值金额(元)","25"}
        ,{"充值赠送金额（元）","25"} ,{"充值时间（元）","25"} ,{"操作人手机","25"}};
        //定义excel工具类对象
		ExcelUtilShopDetail<ShopDetailDto> excelUtil=new ExcelUtilShopDetail<ShopDetailDto>();
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


    private String getShopName(String shopDetailId) {
        String shopname=null;

        List<ShopDetail> shopDetailList = getCurrentShopDetails();
        if(shopDetailList==null){
            shopDetailList = shopDetailService.selectByBrandId(getCurrentBrandId());

        }
	/*	for (ShopDetail shop: shopDetailList
				) {
			/*shop.getId().equals(shopdetailid);*/
			shopname="aa";

		/*}*/

        return  shopname;
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
        	RechargeLogDto shopRechargeLogDto=chargepaymentService.selectShopRechargeLog(beginDate, endDate, shopDetail.getId());
        	if(shopRechargeLogDto == null){
        		shopRechargeLogDto=new RechargeLogDto(shopDetail.getId(),shopDetail.getName(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        	}
        		shopRechargeLogDto.setShopId(shopDetail.getId());
        		shopRechargeLogDto.setShopName(shopDetail.getName());
	        	//判断为空则为0
	        	if(shopRechargeLogDto.getShopCount()==null){
	        		shopRechargeLogDto.setShopCount(initZero);
	        	}else{
	        		shopRechargeLogDto.setShopCount(shopRechargeLogDto.getShopCount());
	        	}
	        	if(shopRechargeLogDto.getShopGaNum()==null){
	        		shopRechargeLogDto.setShopNum(initZero);
	        	}else{
	        		shopRechargeLogDto.setShopNum(shopRechargeLogDto.getShopNum());
	        	}
	        	if(shopRechargeLogDto.getShopGaNum() == null){
	        		shopRechargeLogDto.setShopGaNum(initZero);
	        	}else{
	        		shopRechargeLogDto.setShopGaNum(shopRechargeLogDto.getShopGaNum());
	        	}
	        	if(shopRechargeLogDto.getShopWeChat() == null){
	        		shopRechargeLogDto.setShopWeChat(initZero);
	        	}else{
	        		shopRechargeLogDto.setShopWeChat(shopRechargeLogDto.getShopWeChat());
	        	}
	        	if(shopRechargeLogDto.getShopPos() == null){
	        		shopRechargeLogDto.setShopPos(initZero);
	        	}else{
	        		shopRechargeLogDto.setShopPos(shopRechargeLogDto.getShopPos());
	        	}
	        	if(shopRechargeLogDto.getShopCsNum() == null){
	        		shopRechargeLogDto.setShopCsNum(initZero);
	        	}else{
	        		shopRechargeLogDto.setShopCsNum(shopRechargeLogDto.getShopCsNum());
	        	}
	        	if(shopRechargeLogDto.getShopGaCsNum() == null){
	        		shopRechargeLogDto.setShopGaCsNum(initZero);
		    	}else{
		    		shopRechargeLogDto.setShopGaCsNum(shopRechargeLogDto.getShopGaCsNum());
	        	}
	        	shopRrchargeLogs.add(shopRechargeLogDto);
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