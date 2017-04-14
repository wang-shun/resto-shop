package com.resto.shop.web.controller.business;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resto.brand.core.entity.Result;
import com.resto.shop.web.service.ChargeOrderService;
import org.apache.poi.hssf.record.crypto.Biff8DecryptingStream;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.brand.core.util.ExcelUtil;
import com.resto.brand.web.dto.ShopIncomeDto;
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
    public Map<String, Object> selectIncomeReportList(@RequestParam("beginDate") String beginDate, @RequestParam("endDate") String endDate) {
        return getIncomeReportList(beginDate, endDate);
    }

    /**
     * 2016-10-29
     * 查询收入报表（品牌+店铺）
     *
     * @param beginDate
     * @param endDate
     * @return
     */
    private Map<String, Object> getIncomeReportList(String beginDate, String endDate) {
        //从session中获取该品牌的信息
        List<ShopDetail> shopDetailList = getCurrentShopDetails();
        if (shopDetailList == null) {
            shopDetailList = shopDetailService.selectByBrandId(getCurrentBrandId());
        }
        //封装店铺的信息
        List<ShopIncomeDto> shopIncomeDtos = new ArrayList<>();
        //给每个店铺赋初始值
        for (ShopDetail s : shopDetailList) {
            ShopIncomeDto sin = new ShopIncomeDto(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, s.getName(), s.getId(),BigDecimal.ZERO,BigDecimal.ZERO);
            shopIncomeDtos.add(sin);
        }
        Map<String, Object> selectMap = new HashMap<String, Object>();
        selectMap.put("beginDate", beginDate);
        selectMap.put("endDate", endDate);
        List<Map<String, Object>> selectList = new ArrayList<Map<String, Object>>();
        Map<String, Object> payModeMap = new HashMap<String, Object>();
        payModeMap.put("payMode", PayMode.WEIXIN_PAY);
        payModeMap.put("payName", "wechatIncome");
        selectList.add(payModeMap);
        payModeMap = new HashMap<String, Object>();
        payModeMap.put("payMode", PayMode.CHARGE_PAY);
        payModeMap.put("payName", "chargeAccountIncome");
        selectList.add(payModeMap);
        payModeMap = new HashMap<String, Object>();
        payModeMap.put("payMode", PayMode.ACCOUNT_PAY);
        payModeMap.put("payName", "redIncome");
        selectList.add(payModeMap);
        payModeMap = new HashMap<String, Object>();
        payModeMap.put("payMode", PayMode.COUPON_PAY);
        payModeMap.put("payName", "couponIncome");
        selectList.add(payModeMap);
        payModeMap = new HashMap<String, Object>();
        payModeMap.put("payMode", PayMode.REWARD_PAY);
        payModeMap.put("payName", "chargeGifAccountIncome");
        selectList.add(payModeMap);
        payModeMap = new HashMap<String, Object>();
        payModeMap.put("payMode", PayMode.WAIT_MONEY);
        payModeMap.put("payName", "waitNumberIncome");
        selectList.add(payModeMap);
        payModeMap = new HashMap<String, Object>();
        payModeMap.put("payMode", PayMode.ALI_PAY);
        payModeMap.put("payName", "aliPayment");
        selectList.add(payModeMap);
        payModeMap = new HashMap<String, Object>();
        payModeMap.put("payMode", PayMode.BANK_CART_PAY);
        payModeMap.put("payName", "bankCartPayment");
        selectList.add(payModeMap);
        payModeMap = new HashMap<String, Object>();
        payModeMap.put("payMode", PayMode.CRASH_PAY);
        payModeMap.put("payName", "crashPayment");
        selectList.add(payModeMap);
        payModeMap = new HashMap<String, Object>();
        payModeMap.put("payMode", PayMode.ARTICLE_BACK_PAY);
        payModeMap.put("payName", "articleBackPay");
        selectList.add(payModeMap);
        payModeMap = new HashMap<String, Object>();
        payModeMap.put("payMode", PayMode.MONEY_PAY);
        payModeMap.put("payName", "otherPayment");
        selectList.add(payModeMap);
        payModeMap = new HashMap<String, Object>();
        payModeMap.put("payMode", PayMode.INTEGRAL_PAY);
        payModeMap.put("payName", "integralPayment");
        selectList.add(payModeMap);
        payModeMap = new HashMap<String, Object>();
        payModeMap.put("payMode", PayMode.SHANHUI_PAY);
        payModeMap.put("payName", "shanhuiPayment");
        selectList.add(payModeMap);
        selectMap.put("payModeList", selectList);
        List<Map<String, Object>> orderPaymentItemList = orderpaymentitemService.selectShopIncomeList(selectMap);
        //得到店铺营业信息
        for (ShopIncomeDto shopIncomeDto : shopIncomeDtos) {
            for (Map shopIncomeMap : orderPaymentItemList) {
                if (shopIncomeDto.getShopDetailId().equals(shopIncomeMap.get("shopDetailId").toString())) {
                    shopIncomeDto.setOriginalAmount(new BigDecimal(shopIncomeMap.get("originalAmount").toString()));
                    shopIncomeDto.setTotalIncome(new BigDecimal(shopIncomeMap.get("orderMoney").toString()));
                    shopIncomeDto.setWechatIncome(new BigDecimal(shopIncomeMap.get("wechatIncome").toString()));
                    shopIncomeDto.setChargeAccountIncome(new BigDecimal(shopIncomeMap.get("chargeAccountIncome").toString()));
                    shopIncomeDto.setRedIncome(new BigDecimal(shopIncomeMap.get("redIncome").toString()));
                    shopIncomeDto.setCouponIncome(new BigDecimal(shopIncomeMap.get("couponIncome").toString()));
                    shopIncomeDto.setChargeGifAccountIncome(new BigDecimal(shopIncomeMap.get("chargeGifAccountIncome").toString()));
                    shopIncomeDto.setWaitNumberIncome(new BigDecimal(shopIncomeMap.get("waitNumberIncome").toString()));
                    shopIncomeDto.setAliPayment(new BigDecimal(shopIncomeMap.get("aliPayment").toString()));
                    shopIncomeDto.setBackCartPay(new BigDecimal(shopIncomeMap.get("bankCartPayment").toString()));
                    shopIncomeDto.setMoneyPay(new BigDecimal(shopIncomeMap.get("crashPayment").toString()));
                    shopIncomeDto.setArticleBackPay(new BigDecimal(shopIncomeMap.get("articleBackPay").toString()).abs());
                    shopIncomeDto.setOtherPayment(new BigDecimal(shopIncomeMap.get("otherPayment").toString()));
                    shopIncomeDto.setOtherPayment(new BigDecimal(shopIncomeMap.get("otherPayment").toString()));
                    shopIncomeDto.setIntegralPayment(new BigDecimal(shopIncomeMap.get("integralPayment").toString()));
                    shopIncomeDto.setShanhuiPayment(new BigDecimal(shopIncomeMap.get("shanhuiPayment").toString()));
                }
            }
        }
        //封装品牌的数据
        // 初始化品牌的信息
        BigDecimal originalAmount = BigDecimal.ZERO;
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal wechatIncome = BigDecimal.ZERO;
        BigDecimal redIncome = BigDecimal.ZERO;
        BigDecimal couponIncome = BigDecimal.ZERO;
        BigDecimal chargeAccountIncome = BigDecimal.ZERO;
        BigDecimal chargeGifAccountIncome = BigDecimal.ZERO;
        BigDecimal waitNumberIncome = BigDecimal.ZERO;
        BigDecimal otherPayment = BigDecimal.ZERO;
        BigDecimal aliPayment = BigDecimal.ZERO;
        BigDecimal articleBackPay = BigDecimal.ZERO;
        BigDecimal bankCartPayment = BigDecimal.ZERO;
        BigDecimal crashPayment = BigDecimal.ZERO;
        BigDecimal integralPayment = BigDecimal.ZERO;
        BigDecimal shanhuiPayment = BigDecimal.ZERO;
        if (!shopIncomeDtos.isEmpty()) {
            for (ShopIncomeDto sdto : shopIncomeDtos) {
                originalAmount = originalAmount.add(sdto.getOriginalAmount());
                totalIncome = totalIncome.add(sdto.getTotalIncome());
                wechatIncome = wechatIncome.add(sdto.getWechatIncome());
                redIncome = redIncome.add(sdto.getRedIncome());
                couponIncome = couponIncome.add(sdto.getCouponIncome());
                chargeAccountIncome = chargeAccountIncome.add(sdto.getChargeAccountIncome());
                chargeGifAccountIncome = chargeGifAccountIncome.add(sdto.getChargeGifAccountIncome());
                waitNumberIncome = waitNumberIncome.add(sdto.getWaitNumberIncome());
                otherPayment = otherPayment.add(sdto.getOtherPayment());
                aliPayment = aliPayment.add(sdto.getAliPayment());
                bankCartPayment = bankCartPayment.add(sdto.getBackCartPay());
                crashPayment = crashPayment.add(sdto.getMoneyPay());
                articleBackPay = articleBackPay.add(sdto.getArticleBackPay());
                integralPayment = integralPayment.add(sdto.getIntegralPayment());
                shanhuiPayment = shanhuiPayment.add(sdto.getShanhuiPayment());
            }
        }
        List<ShopIncomeDto> brandIncomeDtos = new ArrayList<ShopIncomeDto>();
        ShopIncomeDto brandIncomeDto = new ShopIncomeDto();
        brandIncomeDto.setOriginalAmount(originalAmount);
        brandIncomeDto.setTotalIncome(totalIncome);
        brandIncomeDto.setWechatIncome(wechatIncome);
        brandIncomeDto.setRedIncome(redIncome);
        brandIncomeDto.setCouponIncome(couponIncome);
        brandIncomeDto.setChargeAccountIncome(chargeAccountIncome);
        brandIncomeDto.setChargeGifAccountIncome(chargeGifAccountIncome);
        brandIncomeDto.setShopName(getBrandName());
        brandIncomeDto.setWaitNumberIncome(waitNumberIncome);
        brandIncomeDto.setOtherPayment(otherPayment);
        brandIncomeDto.setAliPayment(aliPayment);
        brandIncomeDto.setBackCartPay(bankCartPayment);
        brandIncomeDto.setMoneyPay(crashPayment);
        brandIncomeDto.setArticleBackPay(articleBackPay);
        brandIncomeDto.setIntegralPayment(integralPayment);
        brandIncomeDto.setShanhuiPayment(shanhuiPayment);
        brandIncomeDtos.add(brandIncomeDto);
        Map<String, Object> map = new HashMap<>();
        map.put("shopIncome", shopIncomeDtos);
        map.put("brandIncome", brandIncomeDtos);
        return map;
    }

    /**
     * 2016-10-29
     * 生成报表
     *
     * @param beginDate
     * @param endDate
     * @param request
     * @throws IOException
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/brandExprotExcel")
    @ResponseBody
    public Result exprotBrandExcel(@RequestParam("beginDate") String beginDate, @RequestParam("endDate") String endDate,
                                   ShopIncomeDto shopIncomeDto, HttpServletRequest request) throws IOException, Exception {

        List<ShopDetail> shopDetailList = getCurrentShopDetails();
        if (shopDetailList == null) {
            shopDetailList = shopDetailService.selectByBrandId(getCurrentBrandId());
        }
        // 导出文件名
        String str = "营业总额报表" + beginDate + "至" + endDate + ".xls";
        String path = request.getSession().getServletContext().getRealPath(str);
        String shopName = "";
        for (ShopDetail shopDetail : shopDetailList) {
            shopName += shopDetail.getName() + ",";
        }
        // 去掉最后一个逗号
        shopName = shopName.substring(0, shopName.length() - 1);

        Map<String, String> map = new HashMap<>();
        map.put("brandName", getBrandName());
        map.put("shops", shopName);
        map.put("beginDate", beginDate);
        map.put("reportType", "品牌营业额报表");// 表的头，第一行内容
        map.put("endDate", endDate);
        map.put("num", "15");// 显示的位置
        map.put("reportTitle", "品牌收入条目");// 表的名字
        map.put("timeType", "yyyy-MM-dd");

        String[][] headers = {{"品牌/店铺", "20"}, {"原价销售总额(元)", "20"}, {"订单总额(元)", "16"}, {"微信支付(元)", "16"}, {"充值账户支付(元)", "19"}, {"红包支付(元)", "16"}, {"优惠券支付(元)", "17"},
                {"充值赠送支付(元)", "23"}, {"等位红包支付(元)", "23"}, {"支付宝支付(元)", "23"}, {"银联支付(元)", "23"}, {"现金支付(元)", "23"}, {"闪惠支付(元)", "23"}, {"会员支付(元)", "23"}, {"退菜返还红包(元)", "23"}, {"其它支付(元)", "23"}};
        String[] columns = {"shopName", "originalAmount", "totalIncome", "wechatIncome", "chargeAccountIncome", "redIncome", "couponIncome",
                "chargeGifAccountIncome", "waitNumberIncome", "aliPayment", "backCartPay", "moneyPay", "shanhuiPayment","integralPayment" ,"articleBackPay", "otherPayment"};

        List<ShopIncomeDto> result = new ArrayList<>();
        SimplePropertyPreFilter filter = new SimplePropertyPreFilter();
        filter.getExcludes().add("brandIncomeDtos");
        filter.getExcludes().add("shopIncomeDtos");
        String json = JSON.toJSONString(shopIncomeDto.getBrandIncomeDtos(), filter);
        List<ShopIncomeDto> brandIncomeDto = JSON.parseObject(json, new TypeReference<List<ShopIncomeDto>>(){});
        result.addAll(brandIncomeDto);
        json = JSON.toJSONString(shopIncomeDto.getShopIncomeDtos(), filter);
        List<ShopIncomeDto> shopIncomeDtos = JSON.parseObject(json, new TypeReference<List<ShopIncomeDto>>(){});
        result.addAll(shopIncomeDtos);
        ExcelUtil<ShopIncomeDto> excelUtil = new ExcelUtil<>();
        try {
            OutputStream out = new FileOutputStream(path);
            excelUtil.ExportExcel(headers, columns, result, out, map);
            out.close();
        } catch (Exception e) {
            log.error("生成营业总额报表出错！");
            e.printStackTrace();
            return new Result(false);
        }
        return getSuccessResult(path);
    }

    /**
     * 下载报表
     * @param path
     * @param response
     */
    @RequestMapping("/downloadExcel")
    public void downloadExcel(String path, HttpServletResponse response){
        ExcelUtil<ShopIncomeDto> excelUtil = new ExcelUtil<>();
        try {
            excelUtil.download(path, response);
            JOptionPane.showMessageDialog(null, "导出成功！");
            log.info("excel导出成功");
        }catch (Exception e){
            log.error("下载营业总额报表出错！");
            JOptionPane.showMessageDialog(null, "导出失败！");
            e.printStackTrace();
            throw e;
        }
    }
}
