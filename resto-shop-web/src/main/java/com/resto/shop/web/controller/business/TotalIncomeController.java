package com.resto.shop.web.controller.business;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.JOptionPane;

import com.resto.shop.web.model.ChargeOrder;
import com.resto.shop.web.model.OrderPaymentItem;
import com.resto.shop.web.service.ChargeOrderService;
import com.resto.shop.web.service.RedisService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.brand.core.util.ExcelUtil;
import com.resto.brand.web.dto.BrandIncomeDto;
import com.resto.brand.web.dto.IncomeReportDto;
import com.resto.brand.web.dto.ReportIncomeDto;
import com.resto.brand.web.dto.ShopIncomeDto;
import com.resto.brand.web.model.Brand;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.shop.web.constant.PayMode;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.service.OrderPaymentItemService;

@Controller
@RequestMapping("totalIncome")
public class TotalIncomeController extends GenericController {

    @Resource
    BrandService brandService;

    @Resource
    ShopDetailService shopDetailService;

    @Resource
    OrderPaymentItemService orderpaymentitemService;

    @Resource
    ChargeOrderService chargeOrderService;

    @Resource
    RedisService redisService;

    @RequestMapping("/list")
    public void list() {
    }

    // 封装品牌和店铺收入需要的数据
//	public Map<String, Object> getIncomeReportList(String beginDate, String endDate) {
//		// 查询品牌和店铺的收入情况
//		List<IncomeReportDto> incomeReportList = orderpaymentitemService.selectIncomeList(getCurrentBrandId(),beginDate, endDate);
//		// 封装店铺所需要的数据结构
//		List<ShopDetail> listShop = shopDetailService.selectByBrandId(getCurrentBrandId());
//		List<ShopIncomeDto> shopIncomeList = new ArrayList<>();
//		Map<String, ShopIncomeDto> hm = new HashMap<>();
//		for (int i = 0; i < listShop.size(); i++) {// 实际有多少个店铺显示多少个数据
//			ShopIncomeDto sin = new ShopIncomeDto();
//			sin.setShopDetailId(listShop.get(i).getId());
//			sin.setShopName(listShop.get(i).getName());
//			// 设置每个店铺初始营业额为零
//			BigDecimal temp = BigDecimal.ZERO;
//			sin.setWechatIncome(temp);
//			sin.setRedIncome(temp);
//			sin.setCouponIncome(temp);
//			sin.setChargeAccountIncome(temp);
//			sin.setChargeGifAccountIncome(temp);
//			sin.setTotalIncome(temp, temp, temp, temp, temp);
//
//            //查询此店铺的充值金额
//            List<ChargeOrder> chargeOrderList = chargeOrderService.selectByDateAndShopId( beginDate,endDate,sin.getShopDetailId());
//            BigDecimal factShopTemp = temp;
//
//            for (ChargeOrder co : chargeOrderList) {
//                factShopTemp = factShopTemp.add(co.getChargeMoney());
//            }
//
//			String s = "" + i;
//			hm.put(s, sin);
//			if (!incomeReportList.isEmpty()) {
//				for (IncomeReportDto in : incomeReportList) {
//					if (hm.get(s).getShopDetailId().equals(in.getShopDetailId())) {
//						switch (in.getPayMentModeId()) {
//						case PayMode.WEIXIN_PAY:
//							hm.get(s).setWechatIncome(in.getPayValue());
//							break;
//						case PayMode.ACCOUNT_PAY:
//							hm.get(s).setRedIncome(in.getPayValue());
//							break;
//						case PayMode.COUPON_PAY:
//							hm.get(s).setCouponIncome(in.getPayValue());
//							break;
//						case PayMode.CHARGE_PAY:
//							hm.get(s).setChargeAccountIncome(in.getPayValue());
//							break;
//						case PayMode.REWARD_PAY:
//							hm.get(s).setChargeGifAccountIncome(in.getPayValue());
//							break;
//
//						default:
//							break;
//						}
//						hm.get(s).setTotalIncome(hm.get(s).getWechatIncome(), hm.get(s).getRedIncome(),
//								hm.get(s).getCouponIncome(), hm.get(s).getChargeAccountIncome(),
//								hm.get(s).getChargeGifAccountIncome());
//                        hm.get(s).setFactIncome(factShopTemp.add(hm.get(s).getWechatIncome()));
//					}
//				}
//			}
//			shopIncomeList.add(hm.get(s));
//		}
//		// 封装brand所需要的数据结构
//
//		Brand brand = brandService.selectById(getCurrentBrandId());
//		List<BrandIncomeDto> brandIncomeList = new ArrayList<>();
//		BrandIncomeDto in = new BrandIncomeDto();
//		// 初始化品牌的信息
//		BigDecimal wechatIncome = BigDecimal.ZERO;
//		BigDecimal redIncome = BigDecimal.ZERO;
//		BigDecimal couponIncome = BigDecimal.ZERO;
//		BigDecimal chargeAccountIncome = BigDecimal.ZERO;
//		BigDecimal chargeGifAccountIncome = BigDecimal.ZERO;
//        BigDecimal factBrandIncome = BigDecimal.ZERO;
//
//		if (!incomeReportList.isEmpty()) {
//			for (IncomeReportDto income : incomeReportList) {
//				if (income.getPaymentModeId() == PayMode.WEIXIN_PAY) {
//					wechatIncome = wechatIncome.add(income.getPayValue()).setScale(2);
//				} else if (income.getPayMentModeId() == PayMode.ACCOUNT_PAY) {
//					redIncome = redIncome.add(income.getPayValue()).setScale(2);
//				} else if (income.getPayMentModeId() == PayMode.COUPON_PAY) {
//					couponIncome = couponIncome.add(income.getPayValue()).setScale(2);
//				} else if (income.getPaymentModeId() == PayMode.CHARGE_PAY) {
//					chargeAccountIncome = chargeAccountIncome.add(income.getPayValue()).setScale(2);
//				} else if (income.getPayMentModeId() == PayMode.REWARD_PAY) {
//					chargeGifAccountIncome = chargeGifAccountIncome.add(income.getPayValue()).setScale(2);
//				}
//			}
//		}
//        in.setBrandId(brand.getId());
//		in.setBrandName(brand.getBrandName());
//		in.setWechatIncome(wechatIncome);
//		in.setRedIncome(redIncome);
//		in.setCouponIncome(couponIncome);
//		in.setChargeAccountIncome(chargeAccountIncome);
//		in.setChargeGifAccountIncome(chargeGifAccountIncome);
//		in.setTotalIncome(in.getWechatIncome(), in.getRedIncome(), in.getCouponIncome(), in.getChargeAccountIncome(),
//				in.getChargeGifAccountIncome());
//
//        //查询品牌的充值记录
//        List<ChargeOrder> brandChargeList = chargeOrderService.selectByDateAndBrandId(beginDate,endDate,brand.getId());
//        for(ChargeOrder co : brandChargeList){
//           factBrandIncome = factBrandIncome.add(co.getChargeMoney());
//        }
//        factBrandIncome = factBrandIncome.add(in.getWechatIncome());
//        in.setFactIncome(factBrandIncome);
//
//		brandIncomeList.add(in);
//		Map<String, Object> map = new HashMap<>();
//		map.put("shopIncome", shopIncomeList);
//		map.put("brandIncome", brandIncomeList);
//		return map;
//	}


    @RequestMapping("reportIncome")
    @ResponseBody
    public Map<String, Object> selectIncomeReportList(@RequestParam("beginDate") String beginDate,
                                                      @RequestParam("endDate") String endDate) {

        return getIncomeReportList(beginDate, endDate);
    }

    private Map<String,Object> getIncomeReportList(String beginDate, String endDate) {
        try{
            //添加缓存逻辑
            //从缓存中获取品牌和店铺信息


        }catch (Exception e){
            e.printStackTrace();
        }



        //查询所有的店铺
        List<ShopDetail> shopDetailList = shopDetailService.selectByBrandId(getCurrentBrandId());

        List<ShopIncomeDto> shopIncomeDtos = new ArrayList<>();

        //给每个店铺的订单总额  红包支付总额 微信支付总额 ...附初始值
        for(ShopDetail s : shopDetailList){
            ShopIncomeDto sin = new ShopIncomeDto(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, s.getName(), s.getId());
            shopIncomeDtos.add(sin);
        }
        //1.订单总额  2.红包收入 3.优惠券收入 4.微信收入 5.充值账号收入 6.充值赠送账号收入 7.店铺名字 8 店铺 ID
        //   BigDecimal totalIncome, BigDecimal redIncome, BigDecimal couponIncome, BigDecimal wechatIncome,
        //     BigDecimal chargeAccountIncome, BigDecimal chargeGifAccountIncome,  String shopName,
        //     String shopDetailId
        //查询订单支付
        List<OrderPaymentItem> orderPaymentItemList = orderpaymentitemService.selectShopIncomeList(beginDate,endDate,getCurrentBrandId());

        if(!orderPaymentItemList.isEmpty()){
            for(ShopIncomeDto si :shopIncomeDtos){
                for(OrderPaymentItem oi : orderPaymentItemList){
                    if(si.getShopDetailId().equals(oi.getShopDetailId())){
                        switch (oi.getPaymentModeId()) {
                            case PayMode.WEIXIN_PAY:
                                si.setWechatIncome(oi.getPayValue());
                                break;
                            case PayMode.ACCOUNT_PAY:
                                si.setRedIncome(oi.getPayValue());
                                break;
                            case PayMode.COUPON_PAY:
                                si.setCouponIncome(oi.getPayValue());
                                break;
                            case PayMode.CHARGE_PAY:
                                si.setChargeAccountIncome(oi.getPayValue());
                                break;
                            case PayMode.REWARD_PAY:
                                si.setChargeGifAccountIncome(oi.getPayValue());
                                break;
                            default:
                                break;
                        }
                        // BigDecimal wechatIncome, BigDecimal redIncome, BigDecimal couponIncome, BigDecimal chargeAccountIncome, BigDecimal chargeGifAccountIncome
                        si.setTotalIncome(si.getWechatIncome(),si.getRedIncome(),si.getCouponIncome(),si.getChargeAccountIncome(),si.getChargeGifAccountIncome());
                    }
                }
            }
        }


        List<BrandIncomeDto> brandIncomeDtos = new ArrayList<>();
        //封装品牌的数据
        Brand brand = brandService.selectById(getCurrentBrandId());
        // 初始化品牌的信息
        BigDecimal wechatIncome = BigDecimal.ZERO;
        BigDecimal redIncome = BigDecimal.ZERO;
        BigDecimal couponIncome = BigDecimal.ZERO;
        BigDecimal chargeAccountIncome = BigDecimal.ZERO;
        BigDecimal chargeGifAccountIncome = BigDecimal.ZERO;

        if (!shopIncomeDtos.isEmpty()) {
            for (ShopIncomeDto sdto : shopIncomeDtos) {
                wechatIncome = wechatIncome.add(sdto.getWechatIncome());
                redIncome = redIncome.add(sdto.getRedIncome());
                couponIncome = couponIncome.add(sdto.getCouponIncome());
                chargeAccountIncome=chargeAccountIncome.add(sdto.getChargeAccountIncome());
                chargeGifAccountIncome = chargeGifAccountIncome.add(sdto.getChargeGifAccountIncome());
            }
        }

        BrandIncomeDto brandIncomeDto = new BrandIncomeDto();
        brandIncomeDto.setWechatIncome(wechatIncome);
        brandIncomeDto.setRedIncome(redIncome);
        brandIncomeDto.setCouponIncome(couponIncome);
        brandIncomeDto.setChargeAccountIncome(chargeAccountIncome);
        brandIncomeDto.setChargeGifAccountIncome(chargeGifAccountIncome);
        brandIncomeDto.setBrandName(brand.getBrandName());
        //BigDecimal wechatIncome,BigDecimal accountIncome,BigDecimal couponIncome,BigDecimal chargeAccountIncome,BigDecimal chargeGifAccountIncome

        brandIncomeDto.setTotalIncome(wechatIncome,redIncome,couponIncome,chargeAccountIncome,chargeGifAccountIncome);
        brandIncomeDtos.add(brandIncomeDto);

        Map<String, Object> map = new HashMap<>();
        map.put("shopIncome", shopIncomeDtos);
        map.put("brandIncome", brandIncomeDtos);
        return map;
    }

    /**
     * 品牌数据导出excel
     *
     * @param beginDate
     * @param endDate
     * @param request
     * @param response
     * @throws IOException
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/brandExprotExcel")
    @ResponseBody
    public void exprotBrandExcel(@RequestParam("beginDate") String beginDate, @RequestParam("endDate") String endDate,
                                 HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {

        Brand brand = brandService.selectById(getCurrentBrandId());
        List<ShopDetail> shopDetails = shopDetailService.selectByBrandId(getCurrentBrandId());
        // 导出文件名
        String str = "营业总额报表"+beginDate+"至"+endDate+".xls";
        String path = request.getSession().getServletContext().getRealPath(str);
        String shopName = "";
        for (ShopDetail shopDetail : shopDetails) {
            shopName += shopDetail.getName() + ",";
        }
        // 去掉最后一个逗号
        shopName.substring(0, shopName.length() - 1);

        Map<String, String> map = new HashMap<>();
        map.put("brandName", brand.getBrandName());
        map.put("shops", shopName);
        map.put("beginDate", beginDate);
        map.put("reportType", "品牌营业额报表");// 表的头，第一行内容
        map.put("endDate", endDate);
        map.put("num", "7");// 显示的位置
        map.put("reportTitle", "品牌收入条目");// 表的名字
        map.put("timeType", "yyyy-MM-dd");

        String[][] headers = { { "品牌", "20" }, {"营收总额(元)","16"},{ "订单总额(元)", "16" }, { "微信支付(元)", "16" },{ "充值账户支付(元)", "19" },{ "红包支付(元)", "16" }, { "优惠券支付(元)", "17" },
                { "充值赠送支付(元)", "23" } };
        String[] columns = { "name","factIncome", "totalIncome","wechatIncome", "chargeAccountIncome","redIncome", "couponIncome",
                "chargeGifAccountIncome" };

        List<ReportIncomeDto> result = new LinkedList<>();
        List<BrandIncomeDto> brandresult = (List<BrandIncomeDto>) getIncomeReportList(beginDate, endDate).get("brandIncome");
        List<ShopIncomeDto> shopresult = (List<ShopIncomeDto>) getIncomeReportList(beginDate, endDate).get("shopIncome");
        for (ShopIncomeDto shopIncomeDto : shopresult) {
            ReportIncomeDto rt = new ReportIncomeDto();
            rt.setTotalIncome(shopIncomeDto.getTotalIncome());
            rt.setWechatIncome(shopIncomeDto.getWechatIncome());
            rt.setChargeAccountIncome(shopIncomeDto.getChargeAccountIncome());
            rt.setChargeGifAccountIncome(shopIncomeDto.getChargeGifAccountIncome());
            rt.setCouponIncome(shopIncomeDto.getCouponIncome());
            rt.setName(shopIncomeDto.getShopName());
            rt.setRedIncome(shopIncomeDto.getRedIncome());
            rt.setFactIncome(shopIncomeDto.getFactIncome());
            result.add(rt);
        }
        for (BrandIncomeDto brandIncomeDto : brandresult) {
            ReportIncomeDto rt = new ReportIncomeDto();
            rt.setTotalIncome(brandIncomeDto.getTotalIncome());
            rt.setWechatIncome(brandIncomeDto.getWechatIncome());
            rt.setChargeAccountIncome(brandIncomeDto.getChargeAccountIncome());
            rt.setChargeGifAccountIncome(brandIncomeDto.getChargeGifAccountIncome());
            rt.setCouponIncome(brandIncomeDto.getCouponIncome());
            rt.setName(brandIncomeDto.getBrandName());
            rt.setRedIncome(brandIncomeDto.getRedIncome());
            rt.setFactIncome(brandIncomeDto.getFactIncome());
            result.add(rt);
        }
        ExcelUtil<ReportIncomeDto> excelUtil = new ExcelUtil<ReportIncomeDto>();
        try {
            OutputStream out = new FileOutputStream(path);
            excelUtil.ExportExcel(headers, columns, result, out,map);
            out.close();
            excelUtil.download(path, response);
            JOptionPane.showMessageDialog(null, "导出成功！");
            log.info("excel导出成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 店铺数据导出excel
     *
     * @param beginDate
     * @param endDate
     * @param request
     * @param response
     * @throws IOException
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/shopExprotExcel")
    @ResponseBody
    public void exprotShopExcel(@RequestParam("beginDate") String beginDate, @RequestParam("endDate") String endDate,
                                HttpServletRequest request, HttpServletResponse response) {
        // 导出文件名
        String str = "shopInCome.xls";
        String path = request.getSession().getServletContext().getRealPath(str);
        String[][] headers = { { "店铺", "20" }, { "营收总额(元)", "16" }, { "红包支付(元)", "16" }, { "优惠券支付(元)", "17" },
                { "微信支付(元)", "16" }, { "充值账户支付(元)", "19" }, { "充值赠送账户支付(元)", "23" } };
        String[] columns = { "shopName", "totalIncome", "redIncome", "couponIncome", "wechatIncome",
                "chargeAccountIncome", "chargeGifAccountIncome" };
        List<ShopIncomeDto> result = (List<ShopIncomeDto>) getIncomeReportList(beginDate, endDate).get("shopIncome");
        Map<String, String> map = new HashMap<>();
        Brand brand = brandService.selectById(getCurrentBrandId());
        ShopDetail shopDetail = shopDetailService.selectById(getCurrentShopId());
        map.put("brandName", brand.getBrandName());
        map.put("shops", shopDetail.getName());
        map.put("beginDate", beginDate);
        map.put("reportType", "店铺营业额报表");// 表的头，第一行内容
        map.put("endDate", endDate);
        map.put("num", "5");// 显示的位置
        map.put("reportTitle", "店铺收入条目");// 表的名字
        map.put("timeType", "yyyy-MM-dd");

        ExcelUtil<ShopIncomeDto> excelUtil = new ExcelUtil<ShopIncomeDto>();
        try {
            OutputStream out = new FileOutputStream(path);
            excelUtil.ExportExcel(headers, columns, result, out,map);
            out.close();
            excelUtil.download(path, response);
            JOptionPane.showMessageDialog(null, "导出成功！");
            log.info("excel导出成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
