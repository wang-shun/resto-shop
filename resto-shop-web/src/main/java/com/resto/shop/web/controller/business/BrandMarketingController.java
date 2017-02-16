package com.resto.shop.web.controller.business;

import com.alibaba.fastjson.JSONObject;
import com.resto.brand.core.entity.Result;
import com.resto.brand.web.dto.RedPacketDto;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.shop.web.constant.PayMode;
import com.resto.shop.web.model.RedPacket;
import com.resto.shop.web.service.ChargeOrderService;
import com.resto.shop.web.service.GetNumberService;
import com.resto.shop.web.service.RedPacketService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import com.resto.shop.web.controller.GenericController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
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

    @RequestMapping("/selectRedList")
    @ResponseBody
    public Result selectRedList(String grantBeginDate, String grantEndDate, String useBeginDate, String useEndDate, Integer redType){
        JSONObject object = new JSONObject();
        try{
            String shopName = null;
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
            Map<String, Object> selectMap = new HashMap<String, Object>();
            selectMap.put("grantBeginDate",grantBeginDate);
            selectMap.put("grantEndDate",grantEndDate);
            selectMap.put("useBeginDate",useBeginDate);
            selectMap.put("useEndDate",useEndDate);
            List<RedPacketDto> redPacketDtos = null;
            switch (redType){
                case 0:
                    break;
                case 1:
                    selectMap.put("redType",0);
                    selectMap.put("payMode", PayMode.APPRAISE_RED_PAY);
                    redPacketDtos = redPacketService.selectRedPacketLog(selectMap);
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
                    break;
                case 2:
                    selectMap.put("redType",1);
                    selectMap.put("payMode", PayMode.SHARE_RED_PAY);
                    redPacketDtos = redPacketService.selectRedPacketLog(selectMap);
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
                    break;
                case 3:
                    selectMap.put("redType",2);
                    selectMap.put("payMode", PayMode.REFUND_ARTICLE_RED_PAY);
                    redPacketDtos = redPacketService.selectRedPacketLog(selectMap);
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
                    break;
                case 4:
                    redPacketDtos = chargeOrderService.selectChargeRedPacket(selectMap);
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
                    break;
                case 5:
                    redPacketDtos = getNumberService.selectGetNumberRed(selectMap);
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
                    break;
                default:
                    break;
            }
            if(redPacketDtos == null){
                object.put("shopRedInfoList",redPacketDtoList);
            }else{
                List<RedPacketDto> shopRedInfoList = new ArrayList<>();
                for(RedPacketDto redPacket : redPacketDtoList){
                    for(RedPacketDto redPacketDto : redPacketDtos){
                        if (redPacket.getShopDetailId().equalsIgnoreCase(redPacketDto.getShopDetailId())){
                            shopName = redPacket.getShopName();
                            redPacket = redPacketDto;
                            redPacket.setShopName(shopName);
                            redPacket.setUseRedCountRatio((redPacket.getUseRedCount().divide(redPacket.getRedCount(),2,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)) +"%"));
                            redPacket.setUseRedMoneyRatio((redPacket.getUseRedMoney().divide(redPacket.getRedMoney(),2,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100))+ "%"));
                            redCount = redCount.add(redPacket.getRedCount());
                            redMoney = redMoney.add(redPacket.getRedMoney());
                            useRedCount = useRedCount.add(redPacket.getUseRedCount());
                            useRedMoney = useRedMoney.add(redPacket.getUseRedMoney());
                            useRedOrderCount = useRedOrderCount.add(redPacket.getUseRedOrderCount());
                            useRedOrderMoney = useRedOrderMoney.add(redPacket.getUseRedOrderMoney());
                        }
                    }
                    shopRedInfoList.add(redPacket);
                }
                object.put("shopRedInfoList",shopRedInfoList);
            }
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

    @RequestMapping("/downloadExcel")
    @ResponseBody
    public Result downloadExcel(RedPacketDto redPacketDto){
        return getSuccessResult();
    }

    @RequestMapping("/couponList")
    public void couponList(){}
}
