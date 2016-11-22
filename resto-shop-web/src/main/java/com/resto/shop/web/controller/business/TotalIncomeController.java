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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resto.brand.core.util.JsonUtils;
import com.resto.shop.web.model.OrderPaymentItem;
import com.resto.shop.web.service.ChargeOrderService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.brand.core.util.ExcelUtil;
import com.resto.brand.web.dto.BrandIncomeDto;
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

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @RequestMapping("/list")
    public void list() {
    }

    @RequestMapping("reportIncome")
    @ResponseBody
    public Map<String, Object> selectIncomeReportList(@RequestParam("beginDate") String beginDate,@RequestParam("endDate") String endDate) {
        return getIncomeReportList(beginDate, endDate);
    }

    /**
     * 2016-10-29
     * 查询收入报表（品牌+店铺）
     * @param beginDate
     * @param endDate
     * @return
     */
    private Map<String,Object> getIncomeReportList(String beginDate, String endDate) {
        //从session中获取该品牌的信息
        List<ShopDetail> shopDetailList = getCurrentShopDetails();
        if(shopDetailList==null){
                shopDetailList = shopDetailService.selectByBrandId(getCurrentBrandId());
        }
        //封装店铺的信息
        List<ShopIncomeDto> shopIncomeDtos = new ArrayList<>();
        //给每个店铺的订单总额  红包支付总额 微信支付总额 ...附初始值
        for(ShopDetail s : shopDetailList){
            ShopIncomeDto sin = new ShopIncomeDto(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,BigDecimal.ZERO,s.getName(), s.getId());
            shopIncomeDtos.add(sin);
        }
        //1.订单总额  2.红包收入 3.优惠券收入 4.微信收入 5.充值账号收入 6.充值赠送账号收入 7.等位红包支付 7.店铺名字 8 店铺 ID
        //   BigDecimal totalIncome, BigDecimal redIncome, BigDecimal couponIncome, BigDecimal wechatIncome,
        //     BigDecimal chargeAccountIncome, BigDecimal chargeGifAccountIncome,  String shopName,
        //     String shopDetailId
        //查询订单支付
        Map<String,OrderPaymentItem> orderPaymentItemMap = new HashMap<>();
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
                            case PayMode.WAIT_MONEY:
                                si.setWaitNumberIncome(oi.getPayValue());
                                break;
                            default:
                                break;
                        }
                        si.setTotalIncome(si.getWechatIncome(),si.getRedIncome(),si.getCouponIncome(),si.getChargeAccountIncome(),si.getChargeGifAccountIncome(),si.getWaitNumberIncome());
                    }
                }
            }
        }




//        if(!orderPaymentItemList.isEmpty()){
//            for(ShopIncomeDto si :shopIncomeDtos){
//                for(OrderPaymentItem oi : orderPaymentItemList){
//                    if(si.getShopDetailId().equals(oi.getShopDetailId())){
//                        switch (oi.getPaymentModeId()) {
//                            case PayMode.WEIXIN_PAY:
//                                si.setWechatIncome(oi.getPayValue());
//                                break;
//                            case PayMode.ACCOUNT_PAY:
//                                si.setRedIncome(oi.getPayValue());
//                                break;
//                            case PayMode.COUPON_PAY:
//                                si.setCouponIncome(oi.getPayValue());
//                                break;
//                            case PayMode.CHARGE_PAY:
//                                si.setChargeAccountIncome(oi.getPayValue());
//                                break;
//                            case PayMode.REWARD_PAY:
//                                si.setChargeGifAccountIncome(oi.getPayValue());
//                                break;
//                            case PayMode.WAIT_MONEY:
//                                si.setWaitNumberIncome(oi.getPayValue());
//                                break;
//                            default:
//                                break;
//                        }
//                        si.setTotalIncome(si.getWechatIncome(),si.getRedIncome(),si.getCouponIncome(),si.getChargeAccountIncome(),si.getChargeGifAccountIncome(),si.getWaitNumberIncome());
//                    }
//                }
//            }
//        }

        List<BrandIncomeDto> brandIncomeDtos = new ArrayList<>();
        //封装品牌的数据
        // 初始化品牌的信息
        BigDecimal wechatIncome = BigDecimal.ZERO;
        BigDecimal redIncome = BigDecimal.ZERO;
        BigDecimal couponIncome = BigDecimal.ZERO;
        BigDecimal chargeAccountIncome = BigDecimal.ZERO;
        BigDecimal chargeGifAccountIncome = BigDecimal.ZERO;
        BigDecimal waitNumberIncome = BigDecimal.ZERO;
        if (!shopIncomeDtos.isEmpty()) {
            for (ShopIncomeDto sdto : shopIncomeDtos) {
                wechatIncome = wechatIncome.add(sdto.getWechatIncome());
                redIncome = redIncome.add(sdto.getRedIncome());
                couponIncome = couponIncome.add(sdto.getCouponIncome());
                chargeAccountIncome=chargeAccountIncome.add(sdto.getChargeAccountIncome());
                chargeGifAccountIncome = chargeGifAccountIncome.add(sdto.getChargeGifAccountIncome());
                waitNumberIncome = waitNumberIncome.add(sdto.getWaitNumberIncome());
            }
        }
        BrandIncomeDto brandIncomeDto = new BrandIncomeDto();
        brandIncomeDto.setWechatIncome(wechatIncome);
        brandIncomeDto.setRedIncome(redIncome);
        brandIncomeDto.setCouponIncome(couponIncome);
        brandIncomeDto.setChargeAccountIncome(chargeAccountIncome);
        brandIncomeDto.setChargeGifAccountIncome(chargeGifAccountIncome);
        brandIncomeDto.setBrandName(getBrandName());
        brandIncomeDto.setWaitNumberIncome(waitNumberIncome);
        brandIncomeDto.setTotalIncome(wechatIncome,redIncome,couponIncome,chargeAccountIncome,chargeGifAccountIncome,waitNumberIncome);
        brandIncomeDtos.add(brandIncomeDto);
        Map<String, Object> map = new HashMap<>();
        map.put("shopIncome", shopIncomeDtos);
        map.put("brandIncome", brandIncomeDtos);
        return map;
    }

    /**
     * 2016-10-29
     * 收入报表导出excel
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

        List<ShopDetail> shopDetailList = getCurrentShopDetails();
        if(shopDetailList==null){
            shopDetailList = shopDetailService.selectByBrandId(getCurrentBrandId());
        }
        // 导出文件名
        String str = "营业总额报表"+beginDate+"至"+endDate+".xls";
        String path = request.getSession().getServletContext().getRealPath(str);
        String shopName = "";
        for (ShopDetail shopDetail : shopDetailList) {
            shopName += shopDetail.getName() + ",";
        }
        // 去掉最后一个逗号
        shopName.substring(0, shopName.length() - 1);

        Map<String, String> map = new HashMap<>();
        map.put("brandName", getBrandName());
        map.put("shops", shopName);
        map.put("beginDate", beginDate);
        map.put("reportType", "品牌营业额报表");// 表的头，第一行内容
        map.put("endDate", endDate);
        map.put("num", "7");// 显示的位置
        map.put("reportTitle", "品牌收入条目");// 表的名字
        map.put("timeType", "yyyy-MM-dd");

        String[][] headers = { { "品牌", "20" },{ "订单总额(元)", "16" }, { "微信支付(元)", "16" },{ "充值账户支付(元)", "19" },{ "红包支付(元)", "16" }, { "优惠券支付(元)", "17" },
                { "充值赠送支付(元)", "23" },{"等位红包支付","23"} };
        String[] columns = { "name", "totalIncome","wechatIncome", "chargeAccountIncome","redIncome", "couponIncome",
                "chargeGifAccountIncome","waitNumberIncome" };

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
            rt.setWaitNumberIncome(shopIncomeDto.getWaitNumberIncome());
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
            rt.setWaitNumberIncome(brandIncomeDto.getWaitNumberIncome());
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
