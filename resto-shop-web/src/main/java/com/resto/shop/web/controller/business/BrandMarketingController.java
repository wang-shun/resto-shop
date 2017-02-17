package com.resto.shop.web.controller.business;

import com.alibaba.fastjson.JSONObject;
import com.resto.brand.core.entity.Result;
import com.resto.brand.core.util.ExcelUtil;
import com.resto.brand.web.dto.OrderDetailDto;
import com.resto.brand.web.dto.RedPacketDto;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.shop.web.constant.PayMode;
import com.resto.shop.web.constant.RedType;
import com.resto.shop.web.model.RedPacket;
import com.resto.shop.web.service.ChargeOrderService;
import com.resto.shop.web.service.GetNumberService;
import com.resto.shop.web.service.RedPacketService;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import com.resto.shop.web.controller.GenericController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.*;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/brandMarketing")
public class BrandMarketingController extends GenericController{

    @Resource
    private ShopDetailService shopDetailService;

    @Resource
    private RedPacketService redPacketService;

    @Resource
    private ChargeOrderService chargeOrderService;

    @Resource
    private GetNumberService getNumberService;

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

    /**
     * 查询红包报表
     * @param grantBeginDate
     * @param grantEndDate
     * @param useBeginDate
     * @param useEndDate
     * @param redType
     * @return
     */
    @RequestMapping("/selectRedList")
    @ResponseBody
    public Result selectRedList(String grantBeginDate, String grantEndDate, String useBeginDate, String useEndDate, Integer redType){
        JSONObject object = new JSONObject();
        try{
            BigDecimal redCount = new BigDecimal(0);
            BigDecimal redMoney = new BigDecimal(0);
            BigDecimal useRedCount = new BigDecimal(0);
            BigDecimal useRedMoney = new BigDecimal(0);
            BigDecimal useRedOrderCount = new BigDecimal(0);
            BigDecimal useRedOrderMoney = new BigDecimal(0);
            List<RedPacketDto> redPacketDtoList = new ArrayList<RedPacketDto>();
            List<ShopDetail> shopDetailList = getCurrentShopDetails();
            if(getCurrentShopDetails() == null){
                shopDetailList = shopDetailService.selectByBrandId(getCurrentBrandId());
            }
            for(ShopDetail shopDetail : shopDetailList){
                RedPacketDto redPacketDto = new RedPacketDto(shopDetail.getId(),shopDetail.getName(),new BigDecimal(0),new BigDecimal(0),new BigDecimal(0),new BigDecimal(0),"0.00%","0.00%",new BigDecimal(0),new BigDecimal(0));
                redPacketDtoList.add(redPacketDto);
            }
            List<RedPacketDto> selectRedPackets = null;
            switch (redType){
                case 0:
                    selectRedPackets = selectRedPacketLog(grantBeginDate,grantEndDate,useBeginDate,useEndDate,
                        RedType.APPRAISE_RED+","+RedType.SHARE_RED+","+RedType.REFUND_ARTICLE_RED ,
                        PayMode.APPRAISE_RED_PAY+","+PayMode.SHARE_RED_PAY+","+PayMode.REFUND_ARTICLE_RED_PAY);
                    redPacketDtoList = selectRedPackets(selectRedPackets,redPacketDtoList);
                    selectRedPackets = selectChargeRedPacket(grantBeginDate,grantEndDate,useBeginDate,useEndDate);
                    redPacketDtoList = selectRedPackets(selectRedPackets,redPacketDtoList);
                    selectRedPackets = selectGetNumberRed(grantBeginDate,grantEndDate,useBeginDate,useEndDate);
                    redPacketDtoList = selectRedPackets(selectRedPackets,redPacketDtoList);
                    break;
                case 1:
                    selectRedPackets = selectRedPacketLog(grantBeginDate,grantEndDate,useBeginDate,useEndDate, String.valueOf(RedType.APPRAISE_RED) ,String.valueOf(PayMode.APPRAISE_RED_PAY));
                    redPacketDtoList = selectRedPackets(selectRedPackets,redPacketDtoList);
                    break;
                case 2:
                    selectRedPackets = selectRedPacketLog(grantBeginDate,grantEndDate,useBeginDate,useEndDate,String.valueOf(RedType.SHARE_RED),String.valueOf(PayMode.SHARE_RED_PAY));
                    redPacketDtoList = selectRedPackets(selectRedPackets,redPacketDtoList);
                    break;
                case 3:
                    selectRedPackets = selectRedPacketLog(grantBeginDate,grantEndDate,useBeginDate,useEndDate,String.valueOf(RedType.REFUND_ARTICLE_RED),String.valueOf(PayMode.REFUND_ARTICLE_RED_PAY));
                    redPacketDtoList = selectRedPackets(selectRedPackets,redPacketDtoList);
                    break;
                case 4:
                    selectRedPackets = selectChargeRedPacket(grantBeginDate,grantEndDate,useBeginDate,useEndDate);
                    redPacketDtoList = selectRedPackets(selectRedPackets,redPacketDtoList);
                    break;
                case 5:
                    selectRedPackets = selectGetNumberRed(grantBeginDate,grantEndDate,useBeginDate,useEndDate);
                    redPacketDtoList = selectRedPackets(selectRedPackets,redPacketDtoList);
                    break;
                default:
                    break;
            }
            for(RedPacketDto redPacket : redPacketDtoList){
                if(redPacket.getRedCount().equals(BigDecimal.ZERO)){
                    redPacket.setUseRedCountRatio("0.00%");
                }else {
                    redPacket.setUseRedCountRatio((redPacket.getUseRedCount().divide(redPacket.getRedCount(), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)) + "%"));
                }
                if(redPacket.getRedMoney().equals(BigDecimal.ZERO)) {
                    redPacket.setUseRedMoneyRatio("0.00%");
                }else{
                    redPacket.setUseRedMoneyRatio((redPacket.getUseRedMoney().divide(redPacket.getRedMoney(), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)) + "%"));
                }
                redCount = redCount.add(redPacket.getRedCount());
                redMoney = redMoney.add(redPacket.getRedMoney());
                useRedCount = useRedCount.add(redPacket.getUseRedCount());
                useRedMoney = useRedMoney.add(redPacket.getUseRedMoney());
                useRedOrderCount = useRedOrderCount.add(redPacket.getUseRedOrderCount());
                useRedOrderMoney = useRedOrderMoney.add(redPacket.getUseRedOrderMoney());
            }
            object.put("shopRedInfoList",redPacketDtoList);
            JSONObject brandRedInfo = new JSONObject();
            brandRedInfo.put("brandName",getBrandName());
            brandRedInfo.put("redCount",redCount);
            brandRedInfo.put("redMoney",redMoney);
            brandRedInfo.put("useRedCount",useRedCount);
            brandRedInfo.put("useRedMoney",useRedMoney);
            if(redCount.equals(BigDecimal.ZERO)){
                brandRedInfo.put("useRedCountRatio","0.00%");
            }else{
                brandRedInfo.put("useRedCountRatio",useRedCount.divide(redCount,2,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)) + "%");
            }
            if(redMoney.equals(BigDecimal.ZERO)){
                brandRedInfo.put("useRedMoneyRatio","0.00%");
            }else{
                brandRedInfo.put("useRedMoneyRatio",useRedMoney.divide(redMoney,2,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)) + "%");}
            brandRedInfo.put("useRedOrderCount",useRedOrderCount);
            brandRedInfo.put("useRedOrderMoney",useRedOrderMoney);
            object.put("brandRedInfo",brandRedInfo);
        }catch (Exception e){
            log.info("查看红包报表出错！"+e.getMessage());
            return new Result(false);
        }
        return getSuccessResult(object);
    }

    /**
     * 查询评论红包、分享返利红包、退菜红包
     * @param grantBeginDate
     * @param grantEndDate
     * @param useBeginDate
     * @param useEndDate
     * @param redType
     * @param payMode
     * @return
     */
    private List<RedPacketDto> selectRedPacketLog(String grantBeginDate, String grantEndDate, String useBeginDate, String useEndDate,
                                                  String redType, String payMode){
        Map<String, Object> selectMap = new HashMap<>();
        selectMap.put("grantBeginDate",grantBeginDate);
        selectMap.put("grantEndDate",grantEndDate);
        selectMap.put("useBeginDate",useBeginDate);
        selectMap.put("useEndDate",useEndDate);
        selectMap.put("redType",redType);
        selectMap.put("payMode", payMode);
        List<RedPacketDto> redPacketDtos = redPacketService.selectRedPacketLog(selectMap);
        selectMap.put("redPacket","redPacket");
        for(RedPacketDto redPacketDto : redPacketDtos){
            selectMap.put("shopDetailId",redPacketDto.getShopDetailId());
            Map<String, Object> useOrder = redPacketService.selectUseRedOrder(selectMap);
            if(useOrder == null){
                redPacketDto.setUseRedOrderCount(BigDecimal.ZERO);
                redPacketDto.setUseRedOrderMoney(BigDecimal.ZERO);
            }else{
                String[] useRedOrder = useOrder.get("useOrder").toString().split(",");
                redPacketDto.setUseRedOrderCount(new BigDecimal(useRedOrder[0]));
                redPacketDto.setUseRedOrderMoney(new BigDecimal(useRedOrder[1]));
            }
        }
        return redPacketDtos;
    }

    /**
     * 查询充值赠送红包
     * @param grantBeginDate
     * @param grantEndDate
     * @param useBeginDate
     * @param useEndDate
     * @return
     */
    private List<RedPacketDto> selectChargeRedPacket(String grantBeginDate, String grantEndDate, String useBeginDate, String useEndDate){
        Map<String, Object> selectMap = new HashMap<>();
        selectMap.put("grantBeginDate",grantBeginDate);
        selectMap.put("grantEndDate",grantEndDate);
        selectMap.put("useBeginDate",useBeginDate);
        selectMap.put("useEndDate",useEndDate);
        List<RedPacketDto> redPacketDtos = chargeOrderService.selectChargeRedPacket(selectMap);
        selectMap.put("payMode", PayMode.REWARD_PAY);
        selectMap.put("chargeOrder","chargeOrder");
        for(RedPacketDto redPacketDto : redPacketDtos){
            selectMap.put("shopDetailId",redPacketDto.getShopDetailId());
            Map<String, Object> useOrder = redPacketService.selectUseRedOrder(selectMap);
            if(useOrder == null){
                redPacketDto.setUseRedOrderCount(BigDecimal.ZERO);
                redPacketDto.setUseRedOrderMoney(BigDecimal.ZERO);
            }else{
                String[] useRedOrder = useOrder.get("useOrder").toString().split(",");
                redPacketDto.setUseRedOrderCount(new BigDecimal(useRedOrder[0]));
                redPacketDto.setUseRedOrderMoney(new BigDecimal(useRedOrder[1]));
            }
        }
        return redPacketDtos;
    }

    /**
     * 查询等位红包
     * @param grantBeginDate
     * @param grantEndDate
     * @param useBeginDate
     * @param useEndDate
     * @return
     */
    private List<RedPacketDto> selectGetNumberRed(String grantBeginDate, String grantEndDate, String useBeginDate, String useEndDate){
        Map<String, Object> selectMap = new HashMap<>();
        selectMap.put("grantBeginDate",grantBeginDate);
        selectMap.put("grantEndDate",grantEndDate);
        selectMap.put("useBeginDate",useBeginDate);
        selectMap.put("useEndDate",useEndDate);
        List<RedPacketDto> redPacketDtos = getNumberService.selectGetNumberRed(selectMap);
        selectMap.put("payMode", PayMode.WAIT_MONEY);
        selectMap.put("getNumber","getNumber");
        for(RedPacketDto redPacketDto : redPacketDtos){
            selectMap.put("shopDetailId",redPacketDto.getShopDetailId());
            Map<String, Object> useOrder = redPacketService.selectUseRedOrder(selectMap);
            if(useOrder == null){
                redPacketDto.setUseRedOrderCount(BigDecimal.ZERO);
                redPacketDto.setUseRedOrderMoney(BigDecimal.ZERO);
            }else{
                String[] useRedOrder = useOrder.get("useOrder").toString().split(",");
                redPacketDto.setUseRedOrderCount(new BigDecimal(useRedOrder[0]));
                redPacketDto.setUseRedOrderMoney(new BigDecimal(useRedOrder[1]));
            }
        }
        return redPacketDtos;
    }

    /**
     * 查询全部红包
     * @param selectRedPackets
     * @param redPacketDtos
     * @return
     */
    private List<RedPacketDto> selectRedPackets(List<RedPacketDto> selectRedPackets,List<RedPacketDto> redPacketDtoList){
        if(selectRedPackets.isEmpty()){
            return redPacketDtoList;
        }
        for(RedPacketDto redPacketDto : redPacketDtoList){
            for(RedPacketDto redPacket : selectRedPackets){
                if(redPacketDto.getShopDetailId().equalsIgnoreCase(redPacket.getShopDetailId())){
                    redPacketDto.setRedCount(redPacketDto.getRedCount().add(redPacket.getRedCount()));
                    redPacketDto.setRedMoney(redPacketDto.getRedMoney().add(redPacket.getRedMoney()));
                    redPacketDto.setUseRedCount(redPacketDto.getUseRedCount().add(redPacket.getUseRedCount()));
                    redPacketDto.setUseRedMoney(redPacketDto.getUseRedMoney().add(redPacket.getUseRedMoney()));
                    redPacketDto.setUseRedOrderCount(redPacketDto.getUseRedOrderCount().add(redPacket.getUseRedOrderCount()));
                    redPacketDto.setUseRedOrderMoney(redPacketDto.getUseRedOrderMoney().add(redPacket.getUseRedOrderMoney()));
                    break;
                }
            }
        }
        return redPacketDtoList;
    }

    /**
     * 下载红包报表
     * @param redPacketDto
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/createExcel")
    @ResponseBody
    public Result createExcel(String grantBeginDate, String grantEndDate, String useBeginDate, String useEndDate, RedPacketDto redPacketDto,
                                HttpServletRequest request){
        //导出文件名
        String fileName = "红包报表"+grantBeginDate+"至"+grantEndDate+".xls";
        //定义读取文件的路径
        String path = request.getSession().getServletContext().getRealPath(fileName);
        //定义列
        String[]columns={"shopName","redCount","redMoney","useRedCount","useRedMoney","useRedCountRatio","useRedOrderCount","useRedOrderMoney"};
        //定义数据
        List<RedPacketDto> result = new ArrayList<>();
        RedPacketDto brandRedInfo = new RedPacketDto();
        brandRedInfo.setShopName(redPacketDto.getBrandRedInfo().get("brandName").toString());
        brandRedInfo.setRedCount(new BigDecimal(redPacketDto.getBrandRedInfo().get("redCount").toString()));
        brandRedInfo.setRedMoney(new BigDecimal(redPacketDto.getBrandRedInfo().get("redMoney").toString()));
        brandRedInfo.setUseRedCount(new BigDecimal(redPacketDto.getBrandRedInfo().get("useRedCount").toString()));
        brandRedInfo.setUseRedMoney(new BigDecimal(redPacketDto.getBrandRedInfo().get("useRedMoney").toString()));
        brandRedInfo.setUseRedCountRatio(redPacketDto.getBrandRedInfo().get("useRedCountRatio").toString());
        brandRedInfo.setUseRedOrderCount(new BigDecimal(redPacketDto.getBrandRedInfo().get("useRedOrderCount").toString()));
        brandRedInfo.setUseRedOrderMoney(new BigDecimal(redPacketDto.getBrandRedInfo().get("useRedOrderMoney").toString()));
        result.add(brandRedInfo);
        for (Map redMap : redPacketDto.getShopRedInfoList()){
            RedPacketDto redInfo = new RedPacketDto();
            redInfo.setShopName(redMap.get("shopName").toString());
            redInfo.setRedCount(new BigDecimal(redMap.get("redCount").toString()));
            redInfo.setRedMoney(new BigDecimal(redMap.get("redMoney").toString()));
            redInfo.setUseRedCount(new BigDecimal(redMap.get("useRedCount").toString()));
            redInfo.setUseRedMoney(new BigDecimal(redMap.get("useRedMoney").toString()));
            redInfo.setUseRedCountRatio(redMap.get("useRedCountRatio").toString());
            redInfo.setUseRedOrderCount(new BigDecimal(redMap.get("useRedOrderCount").toString()));
            redInfo.setUseRedOrderMoney(new BigDecimal(redMap.get("useRedOrderMoney").toString()));
            result.add(redInfo);
        }
        //获取店铺名称
        String shopName="";
		for (ShopDetail shopDetail : getCurrentShopDetails()) {
			shopName += shopDetail.getName()+",";
		}
		//去掉最后一个逗号
		shopName.substring(0, shopName.length()-1);
        Map<String,String> map = new HashMap<>();
        map.put("brandName", getBrandName());
        map.put("shops", shopName);
        map.put("grantBeginDate", grantBeginDate);
        map.put("grantEndDate", grantEndDate);
        map.put("useBeginDate", useBeginDate);
        map.put("useEndDate", useEndDate);
        map.put("reportType", "红包报表");//表的头，第一行内容
        map.put("num", "7");//显示的位置
        map.put("reportTitle", "红包报表");//表的名字
        map.put("timeType", "yyyy-MM-dd");

        String[][] headers = {{"品牌/店铺名称","35"},{"发放总数","35"},{"发放总额","35"},{"使用总数","35"},{"使用总额","35"},{"红包使用数占比","35"},{"拉动订单总数","35"},{"拉动订单总额","35"}};
        //定义excel工具类对象
        ExcelUtil<RedPacketDto> excelUtil=new ExcelUtil<RedPacketDto>();
        try{
            OutputStream out = new FileOutputStream(path);
            excelUtil.ExportRedPacketDtoExcel(headers, columns, result, out, map);
            out.close();
        }catch(Exception e){
            e.printStackTrace();
            return new Result("创建execl失败",false);
        }
        return getSuccessResult(path);
    }

    @RequestMapping("/downloadExcel")
    public void downloadExcel(String path, HttpServletResponse response){
        try {
            //定义excel工具类对象
            ExcelUtil<RedPacketDto> excelUtil=new ExcelUtil<>();
            excelUtil.download(path, response);
            JOptionPane.showMessageDialog(null, "导出成功！");
            log.info("excel导出成功");
        }catch (Exception e){
            log.error(e.getMessage()+"下载报表出错！");
        }
    }
    @RequestMapping("/couponList")
    public void couponList(){}
}
