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




	@RequestMapping("/queryShopchargecord")
    @ResponseBody
	public  List<ChargeOrder>  queryShopchargecord(String shopdetailid, String beginDate, String endDate){
        List<ShopDetail> shopDetailList = getCurrentShopDetails();

        /*for (ShopDetail fa:shopDetailList
             ) {
            System.out.println(fa.getName()+fa.getId()+"---------------");
        }*/
        List<ChargeOrder>  chargeList=chargeorderService.shopChargeCodes("31164cebcc4b422685e8d9a32db12ab8",beginDate,endDate);

        return chargeList ;
	}



    /**
     * 店铺详细下载报表
     * @return
     */

    public Map<String,Object> getResultSetDto(String shopdetailid,String beginDate,String endDate,String shopname){
           if(shopname==null) { shopname= getShopName(shopdetailid);}
           Map<String,Object>  mapshopDetailDto= chargeorderService.shopChargeCodesSetDto(shopdetailid,beginDate,endDate,shopname);

        return  mapshopDetailDto;
    }
    @RequestMapping("shopDetail_excel")
    @ResponseBody
   public void reportShopDetail(String shopdetailid,String beginDate, String endDate, String
            shopId, HttpServletRequest request, HttpServletResponse response,String shopname){

        List<ShopDetailDto>  result = new LinkedList<>();
        Map<String,Object>  resultMap=this.getResultSetDto(shopdetailid,beginDate,endDate,shopname);
        result.addAll((Collection<? extends ShopDetailDto>) resultMap.get("shopdetailmap"));


        //导出文件名
        String fileName = "店铺订单列表"+beginDate+"至"+endDate
                +".xls";
        //定义读取文件的路径
        String path = request.getSession().getServletContext().getRealPath(fileName);
        //定义列
        String[]columns=
                         {"shopname","typeString","customerPhone","chargeMoney",
						"rewardMoney","finishTime","operationPhone    "};
                        //定义数据


      //获取店铺名称
       ShopDetail s = shopDetailService.selectById(shopId);
        Map<String,String> map = new HashMap<>();
        map.put("shops",getShopName(shopdetailid));
        map.put("beginDate", beginDate);
        map.put("reportType", "店铺充值记录");//表的头，第一行内容
        map.put("endDate", endDate);
        map.put("num", "11");//显示的位置
        map.put("reportTitle", "店铺充值记录");//表的名字
        map.put("timeType", "yyyy-MM-dd");
        String[][] headers = {{"名称","25"},{"订单总数(份)","25"},{"订单金额(元)","25"},{"订单平均金额(元)","25"},{"营销撬动率","25"}};
        //定义excel工具类对象
       ExcelUtil<ShopDetailDto> excelUtil=new ExcelUtil<ShopDetailDto>();
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

    private String getShopName(String shopdetailid) {
        String shopname=null;

        List<ShopDetail> shopDetailList = getCurrentShopDetails();
        if(shopDetailList==null){
            shopDetailList = shopDetailService.selectByBrandId(getCurrentBrandId());
            for (ShopDetail shop: shopDetailList
                    ) {
                shop.getId().equals(shopdetailid);
                shopname=shop.getName();
            }
        }

        return  shopname;
    }


    @RequestMapping("/list")
	public String  list(){

	    return "recharge/shopchargerecord";
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
		//初始化品牌充值记录
		RechargeLogDto brandInit = new RechargeLogDto(getBrandName(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
		RechargeLogDto rechargeLogDto=chargepaymentService.selectRechargeLog(beginDate, endDate,getCurrentBrandId());
		if(rechargeLogDto!=null){
			brandInit.setBrandName(getBrandName());
			brandInit.setRechargeCount(rechargeLogDto.getRechargeCount());
			brandInit.setRechargeCsNum(rechargeLogDto.getRechargeCsNum());
			brandInit.setRechargeGaCsNum(rechargeLogDto.getRechargeGaCsNum());
			brandInit.setRechargeGaNum(rechargeLogDto.getRechargeGaNum());
			brandInit.setRechargeGaSpNum(rechargeLogDto.getRechargeGaSpNum());
			brandInit.setRechargeNum(rechargeLogDto.getRechargeNum());
			brandInit.setRechargePos(rechargeLogDto.getRechargePos());
			brandInit.setRechargeSpNum(rechargeLogDto.getRechargeSpNum());
			brandInit.setRechargeWeChat(rechargeLogDto.getRechargeWeChat());
		}
		
		List<RechargeLogDto> shopRrchargeLogs=new ArrayList<>();
		
		List<ShopDetail> shoplist = getCurrentShopDetails();
        if(!shoplist.isEmpty()){
            shoplist = shopDetailService.selectByBrandId(getCurrentBrandId());
        }
        for (ShopDetail shopDetail : shoplist) {
        	//初始化店铺充值记录
        	RechargeLogDto shopInit=new RechargeLogDto(shopDetail.getId(),shopDetail.getName(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        	System.out.println(shopInit.getShopCsNum());
        	RechargeLogDto shopRechargeLogDto=chargepaymentService.selectShopRechargeLog(beginDate, endDate, shopDetail.getId());
        	shopInit.setShopId(shopDetail.getId());
        	shopInit.setShopName(shopDetail.getName());
        	shopInit.setShopCount(shopRechargeLogDto.getShopCount());
        	shopInit.setShopCsNum(shopRechargeLogDto.getShopCsNum());
        	shopInit.setShopGaNum(shopRechargeLogDto.getShopGaNum());
        	shopInit.setShopGaCsNum(shopRechargeLogDto.getShopGaCsNum());
        	shopInit.setShopNum(shopRechargeLogDto.getShopNum());
        	shopInit.setRechargePos(shopRechargeLogDto.getRechargePos());
        	shopInit.setShopWeChat(shopRechargeLogDto.getShopWeChat());
        	shopRrchargeLogs.add(shopInit);
		}
        
		Map<String, Object> map=new HashMap<>();
		map.put("brandInit", brandInit);
		map.put("shopRrchargeLogs", shopRrchargeLogs);
		
		return map;
	}

	
	
	


}