package com.resto.shop.web.controller.business;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;


import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resto.brand.core.entity.Result;
import com.resto.shop.web.constant.Common;
import com.resto.shop.web.constant.OfflineOrderSource;
import com.resto.shop.web.dto.OrderNumDto;
import com.resto.shop.web.model.OffLineOrder;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.service.ChargeOrderService;
import com.resto.shop.web.service.OffLineOrderService;
import com.resto.shop.web.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.brand.core.util.ExcelUtil;
import com.resto.brand.web.dto.ShopIncomeDto;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.ShopDetailService;
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
    OrderService orderService;

    @Resource
    OffLineOrderService offLineOrderService;

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
        //封装品牌信息
        ShopIncomeDto brandIncomeDto = new ShopIncomeDto(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, getBrandName(), getCurrentBrandId(),BigDecimal.ZERO,BigDecimal.ZERO, BigDecimal.ZERO);
        //封装店铺的信息
        List<ShopIncomeDto> shopIncomeDtos = new ArrayList<>();

        //初始化连接率
        Map<String,String> mapRatio = new HashMap<>(16);
        //给每个店铺赋初始值
        for (ShopDetail s : shopDetailList) {
            ShopIncomeDto sin = new ShopIncomeDto(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, s.getName(), s.getId(),BigDecimal.ZERO,BigDecimal.ZERO, BigDecimal.ZERO);
            shopIncomeDtos.add(sin);
            mapRatio.put(s.getId(),"");

        }
        //用来接收分段查询出来的订单金额信息
        List<ShopIncomeDto> shopIncomeDtosItem = new ArrayList<>();
        shopIncomeDtosItem.add(new ShopIncomeDto());
        //用来累加分段查询出来的订单金额信息
        List<ShopIncomeDto> shopIncomeDtosItems = new ArrayList<>();
        //用来接收分段查询出来的订单支付项信息
        List<ShopIncomeDto> shopIncomeDtosPayMent = new ArrayList<>();
        shopIncomeDtosPayMent.add(new ShopIncomeDto());
        //用来累加分段查询出来的订单支付项信息
        List<ShopIncomeDto> shopIncomeDtosPayMents = new ArrayList<>();
        Map<String, Object> selectMap = new HashMap<>();
        selectMap.put("beginDate", beginDate);
        selectMap.put("endDate", endDate);
        for (int pageNo = 0; (shopIncomeDtosItem != null && !shopIncomeDtosItem.isEmpty())
                || (shopIncomeDtosPayMent != null && !shopIncomeDtosPayMent.isEmpty()); pageNo ++){
            selectMap.put("pageNo", pageNo * 1000);
            shopIncomeDtosItem = orderService.callProcDayAllOrderItem(selectMap);
            shopIncomeDtosPayMent = orderService.callProcDayAllOrderPayMent(selectMap);
            shopIncomeDtosItems.addAll(shopIncomeDtosItem);
            shopIncomeDtosPayMents.addAll(shopIncomeDtosPayMent);
        }


        //查询店铺Resto交易笔数
        List<OrderNumDto> orderNumDtoRestoList = orderService.selectOrderNumByTimeAndBrandId(getCurrentBrandId(),beginDate,endDate);

        //查询店铺线下交易笔数
        List<OrderNumDto> orderNumDtoOffLineList = offLineOrderService.selectOrderNumByTimeAndBrandId(getCurrentBrandId(),beginDate,endDate);


        //得到店铺营业信息
        for (ShopIncomeDto shopIncomeDto : shopIncomeDtos) {
                if(orderNumDtoRestoList !=null && !orderNumDtoRestoList.isEmpty()){
                    for(OrderNumDto orderNumDto:orderNumDtoRestoList){
                        if(shopIncomeDto.getShopDetailId().equals(orderNumDto.getShopId())){
                            shopIncomeDto.setRestoOrderNum(orderNumDto.getNum());
                            break;
                        }

                    }
                }

                if(orderNumDtoOffLineList !=null && !orderNumDtoOffLineList.isEmpty()){
                    for(OrderNumDto orderNumDto:orderNumDtoOffLineList){
                        if(orderNumDto.getShopId().equals(shopIncomeDto.getShopDetailId())){
                            shopIncomeDto.setOffLineOrderNum(orderNumDto.getNum());
                            break;
                        }
                    }
                }


            //循环累加店铺订单总额、原价金额
            for (ShopIncomeDto shopIncomeDtoItem : shopIncomeDtosItems){
                if (shopIncomeDto.getShopDetailId().equalsIgnoreCase(shopIncomeDtoItem.getShopDetailId())){
                    shopIncomeDto.setOriginalAmount(shopIncomeDto.getOriginalAmount().add(shopIncomeDtoItem.getOriginalAmount()));
                    shopIncomeDto.setTotalIncome(shopIncomeDto.getTotalIncome().add(shopIncomeDtoItem.getTotalIncome()));
                }
            }
            //循环累加得到店铺各个支付项的值
            for (ShopIncomeDto shopIncomeDtoPayMent : shopIncomeDtosPayMents){
                if (shopIncomeDto.getShopDetailId().equalsIgnoreCase(shopIncomeDtoPayMent.getShopDetailId())){
                    shopIncomeDto.setWechatIncome(shopIncomeDto.getWechatIncome().add(shopIncomeDtoPayMent.getWechatIncome()));
                    shopIncomeDto.setChargeAccountIncome(shopIncomeDto.getChargeAccountIncome().add(shopIncomeDtoPayMent.getChargeAccountIncome()));
                    shopIncomeDto.setRedIncome(shopIncomeDto.getRedIncome().add(shopIncomeDtoPayMent.getRedIncome()));
                    shopIncomeDto.setCouponIncome(shopIncomeDto.getCouponIncome().add(shopIncomeDtoPayMent.getCouponIncome()));
                    shopIncomeDto.setChargeGifAccountIncome(shopIncomeDto.getChargeGifAccountIncome().add(shopIncomeDtoPayMent.getChargeGifAccountIncome()));
                    shopIncomeDto.setWaitNumberIncome(shopIncomeDto.getWaitNumberIncome().add(shopIncomeDtoPayMent.getWaitNumberIncome()));
                    shopIncomeDto.setAliPayment(shopIncomeDto.getAliPayment().add(shopIncomeDtoPayMent.getAliPayment()));
                    shopIncomeDto.setBackCartPay(shopIncomeDto.getBackCartPay().add(shopIncomeDtoPayMent.getBackCartPay()));
                    shopIncomeDto.setMoneyPay(shopIncomeDto.getMoneyPay().add(shopIncomeDtoPayMent.getMoneyPay()));
                    shopIncomeDto.setShanhuiPayment(shopIncomeDto.getShanhuiPayment().add(shopIncomeDtoPayMent.getShanhuiPayment()));
                    shopIncomeDto.setIntegralPayment(shopIncomeDto.getIntegralPayment().add(shopIncomeDtoPayMent.getIntegralPayment()));
                    shopIncomeDto.setArticleBackPay(shopIncomeDto.getArticleBackPay().add(shopIncomeDtoPayMent.getArticleBackPay()));
                    shopIncomeDto.setOtherPayment(shopIncomeDto.getOtherPayment().add(shopIncomeDtoPayMent.getOtherPayment()));
                }
            }
            brandIncomeDto.setOriginalAmount(brandIncomeDto.getOriginalAmount().add(shopIncomeDto.getOriginalAmount()));
            brandIncomeDto.setTotalIncome(brandIncomeDto.getTotalIncome().add(shopIncomeDto.getTotalIncome()));
            brandIncomeDto.setWechatIncome(brandIncomeDto.getWechatIncome().add(shopIncomeDto.getWechatIncome()));
            brandIncomeDto.setChargeAccountIncome(brandIncomeDto.getChargeAccountIncome().add(shopIncomeDto.getChargeAccountIncome()));
            brandIncomeDto.setRedIncome(brandIncomeDto.getRedIncome().add(shopIncomeDto.getRedIncome()));
            brandIncomeDto.setCouponIncome(brandIncomeDto.getCouponIncome().add(shopIncomeDto.getCouponIncome()));
            brandIncomeDto.setChargeGifAccountIncome(brandIncomeDto.getChargeGifAccountIncome().add(shopIncomeDto.getChargeGifAccountIncome()));
            brandIncomeDto.setWaitNumberIncome(brandIncomeDto.getWaitNumberIncome().add(shopIncomeDto.getWaitNumberIncome()));
            brandIncomeDto.setAliPayment(brandIncomeDto.getAliPayment().add(shopIncomeDto.getAliPayment()));
            brandIncomeDto.setBackCartPay(brandIncomeDto.getBackCartPay().add(shopIncomeDto.getBackCartPay()));
            brandIncomeDto.setMoneyPay(brandIncomeDto.getMoneyPay().add(shopIncomeDto.getMoneyPay()));
            brandIncomeDto.setShanhuiPayment(brandIncomeDto.getShanhuiPayment().add(shopIncomeDto.getShanhuiPayment()));
            brandIncomeDto.setIntegralPayment(brandIncomeDto.getIntegralPayment().add(shopIncomeDto.getIntegralPayment()));
            brandIncomeDto.setArticleBackPay(brandIncomeDto.getArticleBackPay().add(shopIncomeDto.getArticleBackPay()));
            brandIncomeDto.setOtherPayment(brandIncomeDto.getOtherPayment().add(shopIncomeDto.getOtherPayment()));
        }


        for(ShopIncomeDto shopIncomeDto:shopIncomeDtos){
            BigDecimal restoOrderNum = new BigDecimal(shopIncomeDto.getRestoOrderNum() == null ? 0 : shopIncomeDto.getRestoOrderNum());
            BigDecimal offLineOrderNum = new BigDecimal(shopIncomeDto.getOffLineOrderNum() == null ? 0 : shopIncomeDto.getOffLineOrderNum());
            BigDecimal totalOrderNum = restoOrderNum.add(offLineOrderNum);
            String connectRatio = totalOrderNum.compareTo(BigDecimal.ZERO) == 0? "无" : restoOrderNum.divide(totalOrderNum, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100))+"%";
            shopIncomeDto.setConnctRatio(connectRatio);
        }


        List<ShopIncomeDto> brandIncomeDtos = new ArrayList<>();
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
                {"充值赠送支付(元)", "23"}, {"等位红包支付(元)", "23"}, {"支付宝支付(元)", "23"}, {"银联支付(元)", "23"}, {"现金实收(元)", "23"}, {"闪惠支付(元)", "23"}, {"会员支付(元)", "23"}
                , {"退菜返还红包(元)", "23"}, {"其它支付(元)", "23"}};
        String[] columns = {"shopName", "originalAmount", "totalIncome", "wechatIncome", "chargeAccountIncome", "redIncome", "couponIncome",
                "chargeGifAccountIncome", "waitNumberIncome", "aliPayment", "backCartPay", "moneyPay", "shanhuiPayment","integralPayment"
                ,"articleBackPay", "otherPayment"};

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

    @RequestMapping("/createMonthDto")
    @ResponseBody
    public Result createMonthDto(String year, String month, Integer type, HttpServletRequest request){
        Integer monthDay = getMonthDay(year, month);
        // 导出文件名
        String typeName = type.equals(Common.YES) ? "店铺营业总额月报表" : "品牌营业总额月报表" ;
        String str = typeName + year.concat("-").concat(month).concat("-01") + "至"
                + year.concat("-").concat(month).concat("-").concat(String.valueOf(monthDay)) + ".xls";
        String path = request.getSession().getServletContext().getRealPath(str);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String beginTime = year.concat("-").concat(month).concat("-01") ;
        String endTime = year.concat("-").concat(month).concat("-").concat(String.valueOf(monthDay));
        try {
            List<ShopDetail> shopDetails = getCurrentShopDetails();
            if (shopDetails == null) {
                shopDetails = shopDetailService.selectByBrandId(getCurrentBrandId());
            }
            String[] shopNames = new String[1];
            ShopIncomeDto[][] result = new ShopIncomeDto[1][monthDay];
            Map<String, Object> selectMap = new HashMap<>();
            selectMap.put("beginDate",beginTime);
            selectMap.put("endDate",endTime);
            if (type.equals(Common.YES)) {
                shopNames = new String[shopDetails.size()];
                result = new ShopIncomeDto[shopDetails.size()][monthDay];
                int i = 0;
                int j = 0;

                //todo 需要优化
                for (ShopDetail shopDetail : shopDetails) {
                    selectMap.put("shopId", shopDetail.getId());
                    //用来接收分段查询出来的订单金额信息
                    List<ShopIncomeDto> shopIncomeDtosItem = new ArrayList<>();
                    shopIncomeDtosItem.add(new ShopIncomeDto());
                    //用来累加分段查询出来的订单金额信息
                    List<ShopIncomeDto> shopIncomeDtosItems = new ArrayList<>();
                    //用来接收分段查询出来的订单支付项信息
                    List<ShopIncomeDto> shopIncomeDtosPayMent = new ArrayList<>();
                    shopIncomeDtosPayMent.add(new ShopIncomeDto());
                    //用来累加分段查询出来的订单支付项信息
                    List<ShopIncomeDto> shopIncomeDtosPayMents = new ArrayList<>();
                    for (int pageNo = 0; (shopIncomeDtosItem != null && !shopIncomeDtosItem.isEmpty())
                            || (shopIncomeDtosPayMent != null && !shopIncomeDtosPayMent.isEmpty()); pageNo ++){
                        selectMap.put("pageNo", pageNo * 1000);
                        shopIncomeDtosItem = orderService.callProcDayAllOrderItem(selectMap);
                        shopIncomeDtosPayMent = orderService.callProcDayAllOrderPayMent(selectMap);
                        shopIncomeDtosItems.addAll(shopIncomeDtosItem);
                        shopIncomeDtosPayMents.addAll(shopIncomeDtosPayMent);
                    }

                    /**
                     * 查询resto订单
                     */
                    List<Order> orderList = orderService.selectListByShopId(beginTime,endTime,shopDetail.getId());
                    //查询线下订单
                    List<OffLineOrder> offLineOrderList = offLineOrderService.selectlistByTimeSourceAndShopId(shopDetail.getId(),beginTime,endTime,OfflineOrderSource.OFFLINE_POS);

                    for (int day = 0; day < monthDay; day++) {
                        Date beginDate = getBeginDay(year, month, day);
                        Date endDate = getEndDay(year, month, day);
                        ShopIncomeDto shopIncomeDto = new ShopIncomeDto(format.format(beginDate), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, shopDetail.getName(), shopDetail.getId(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
                        for (ShopIncomeDto incomeDto : shopIncomeDtosItems){
                            if (endDate.getTime() >= incomeDto.getCreateTime().getTime() && beginDate.getTime() <= incomeDto.getCreateTime().getTime()) {
                                shopIncomeDto.setOriginalAmount(shopIncomeDto.getOriginalAmount().add(incomeDto.getOriginalAmount()));
                                shopIncomeDto.setTotalIncome(shopIncomeDto.getTotalIncome().add(incomeDto.getTotalIncome()));
                            }
                        }
                        for (ShopIncomeDto incomeDto : shopIncomeDtosPayMents){
                            if (endDate.getTime() >= incomeDto.getCreateTime().getTime() && beginDate.getTime() <= incomeDto.getCreateTime().getTime()) {
                                shopIncomeDto.setWechatIncome(shopIncomeDto.getWechatIncome().add(incomeDto.getWechatIncome()));
                                shopIncomeDto.setChargeAccountIncome(shopIncomeDto.getChargeAccountIncome().add(incomeDto.getChargeAccountIncome()));
                                shopIncomeDto.setRedIncome(shopIncomeDto.getRedIncome().add(incomeDto.getRedIncome()));
                                shopIncomeDto.setCouponIncome(shopIncomeDto.getCouponIncome().add(incomeDto.getCouponIncome()));
                                shopIncomeDto.setChargeGifAccountIncome(shopIncomeDto.getChargeGifAccountIncome().add(incomeDto.getChargeGifAccountIncome()));
                                shopIncomeDto.setWaitNumberIncome(shopIncomeDto.getWaitNumberIncome().add(incomeDto.getWaitNumberIncome()));
                                shopIncomeDto.setAliPayment(shopIncomeDto.getAliPayment().add(incomeDto.getAliPayment()));
                                shopIncomeDto.setBackCartPay(shopIncomeDto.getBackCartPay().add(incomeDto.getBackCartPay()));
                                shopIncomeDto.setMoneyPay(shopIncomeDto.getMoneyPay().add(incomeDto.getMoneyPay()));
                                shopIncomeDto.setShanhuiPayment(shopIncomeDto.getShanhuiPayment().add(incomeDto.getShanhuiPayment()));
                                shopIncomeDto.setIntegralPayment(shopIncomeDto.getIntegralPayment().add(incomeDto.getIntegralPayment()));
                                shopIncomeDto.setArticleBackPay(shopIncomeDto.getArticleBackPay().add(incomeDto.getArticleBackPay()));
                                shopIncomeDto.setOtherPayment(shopIncomeDto.getOtherPayment().add(incomeDto.getOtherPayment()));
                            }
                        }
                        int restoNum = 0;
                        int offlineNum = 0;

                        if(orderList != null && !orderList.isEmpty()){
                            for(Order o:orderList){
                                if(endDate.getTime() >= o.getCreateTime().getTime() && beginDate.getTime() <= o.getCreateTime().getTime()){
                                   restoNum++;
                                }
                            }
                        }
                        shopIncomeDto.setRestoOrderNum(restoNum);

                        if(offLineOrderList !=null && !offLineOrderList.isEmpty()){
                            for(OffLineOrder offLineOrder : offLineOrderList){
                                if(endDate.getTime() >= offLineOrder.getCreateTime().getTime() && beginDate.getTime() <= offLineOrder.getCreateTime().getTime()){
                                    offlineNum += offLineOrder.getEnterCount();
                                }
                            }
                        }
                        shopIncomeDto.setOffLineOrderNum(offlineNum);
                        BigDecimal restoOrderNum = new BigDecimal(shopIncomeDto.getRestoOrderNum() == null ? 0 : shopIncomeDto.getRestoOrderNum());
                        BigDecimal offLineOrderNum = new BigDecimal(shopIncomeDto.getOffLineOrderNum() == null ? 0 : shopIncomeDto.getOffLineOrderNum());
                        BigDecimal totalOrderNum = restoOrderNum.add(offLineOrderNum);
                        String connectRatio = totalOrderNum.compareTo(BigDecimal.ZERO) == 0? "无" : restoOrderNum.divide(totalOrderNum, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100))+"%";
                        shopIncomeDto.setConnctRatio(connectRatio);

                        result[i][j] = shopIncomeDto;
                        j++;
                    }
                    shopNames[i] = shopDetail.getName();
                    i++;
                    j = 0;
                }
            }else {
                String shopName = "";
                for (ShopDetail shopDetail : shopDetails){
                    shopName = shopName.concat(shopDetail.getName()).concat(",");
                }
                shopName = shopName.substring(0, shopName.length() - 1);
                shopNames[0] = shopName;
                //用来接收分段查询出来的订单金额信息
                List<ShopIncomeDto> shopIncomeDtosItem = new ArrayList<>();
                shopIncomeDtosItem.add(new ShopIncomeDto());
                //用来累加分段查询出来的订单金额信息
                List<ShopIncomeDto> shopIncomeDtosItems = new ArrayList<>();
                //用来接收分段查询出来的订单支付项信息
                List<ShopIncomeDto> shopIncomeDtosPayMent = new ArrayList<>();
                shopIncomeDtosPayMent.add(new ShopIncomeDto());
                //用来累加分段查询出来的订单支付项信息
                List<ShopIncomeDto> shopIncomeDtosPayMents = new ArrayList<>();
                for (int pageNo = 0; (shopIncomeDtosItem != null && !shopIncomeDtosItem.isEmpty())
                        || (shopIncomeDtosPayMent != null && !shopIncomeDtosPayMent.isEmpty()); pageNo ++){
                    selectMap.put("pageNo", pageNo * 1000);
                    shopIncomeDtosItem = orderService.callProcDayAllOrderItem(selectMap);
                    shopIncomeDtosPayMent = orderService.callProcDayAllOrderPayMent(selectMap);
                    shopIncomeDtosItems.addAll(shopIncomeDtosItem);
                    shopIncomeDtosPayMents.addAll(shopIncomeDtosPayMent);
                }
                int j = 0;
                for (int day = 0; day < monthDay; day++) {
                    Date beginDate = getBeginDay(year, month, day);
                    Date endDate = getEndDay(year, month, day);
                    ShopIncomeDto shopIncomeDto = new ShopIncomeDto(format.format(beginDate), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, getBrandName(), getCurrentBrandId(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
                    for (ShopIncomeDto incomeDto : shopIncomeDtosItems){
                        if (endDate.getTime() >= incomeDto.getCreateTime().getTime() && beginDate.getTime() <= incomeDto.getCreateTime().getTime()) {
                            shopIncomeDto.setOriginalAmount(shopIncomeDto.getOriginalAmount().add(incomeDto.getOriginalAmount()));
                            shopIncomeDto.setTotalIncome(shopIncomeDto.getTotalIncome().add(incomeDto.getTotalIncome()));
                        }
                    }
                    for (ShopIncomeDto incomeDto : shopIncomeDtosPayMents){
                        if (endDate.getTime() >= incomeDto.getCreateTime().getTime() && beginDate.getTime() <= incomeDto.getCreateTime().getTime()) {
                            shopIncomeDto.setWechatIncome(shopIncomeDto.getWechatIncome().add(incomeDto.getWechatIncome()));
                            shopIncomeDto.setChargeAccountIncome(shopIncomeDto.getChargeAccountIncome().add(incomeDto.getChargeAccountIncome()));
                            shopIncomeDto.setRedIncome(shopIncomeDto.getRedIncome().add(incomeDto.getRedIncome()));
                            shopIncomeDto.setCouponIncome(shopIncomeDto.getCouponIncome().add(incomeDto.getCouponIncome()));
                            shopIncomeDto.setChargeGifAccountIncome(shopIncomeDto.getChargeGifAccountIncome().add(incomeDto.getChargeGifAccountIncome()));
                            shopIncomeDto.setWaitNumberIncome(shopIncomeDto.getWaitNumberIncome().add(incomeDto.getWaitNumberIncome()));
                            shopIncomeDto.setAliPayment(shopIncomeDto.getAliPayment().add(incomeDto.getAliPayment()));
                            shopIncomeDto.setBackCartPay(shopIncomeDto.getBackCartPay().add(incomeDto.getBackCartPay()));
                            shopIncomeDto.setMoneyPay(shopIncomeDto.getMoneyPay().add(incomeDto.getMoneyPay()));
                            shopIncomeDto.setShanhuiPayment(shopIncomeDto.getShanhuiPayment().add(incomeDto.getShanhuiPayment()));
                            shopIncomeDto.setIntegralPayment(shopIncomeDto.getIntegralPayment().add(incomeDto.getIntegralPayment()));
                            shopIncomeDto.setArticleBackPay(shopIncomeDto.getArticleBackPay().add(incomeDto.getArticleBackPay()));
                            shopIncomeDto.setOtherPayment(shopIncomeDto.getOtherPayment().add(incomeDto.getOtherPayment()));
                        }
                    }
                    result[0][j] = shopIncomeDto;
                    j++;
                }
            }
            Map<String, Object> map = new HashMap<>();
            map.put("brandName", getBrandName());
            map.put("beginDate", year.concat("-").concat(month).concat("-01"));
            map.put("reportType", typeName);// 表的头，第一行内容
            map.put("endDate", year.concat("-").concat(month).concat("-").concat(String.valueOf(monthDay)));
            map.put("num", "15");// 显示的位置
            map.put("timeType", "yyyy-MM-dd");
            map.put("reportTitle", shopNames);// 表的名字
            String[][] headers = {{"日期", "20"}, {"原价销售总额(元)", "20"}, {"订单总额(元)", "16"}, {"微信支付(元)", "16"}, {"充值账户支付(元)", "19"}, {"红包支付(元)", "16"}, {"优惠券支付(元)", "17"},
                    {"充值赠送支付(元)", "23"}, {"等位红包支付(元)", "23"}, {"支付宝支付(元)", "23"}, {"银联支付(元)", "23"}, {"现金实收(元)", "23"}, {"闪惠支付(元)", "23"}, {"会员支付(元)", "23"}
                    , {"退菜返还红包(元)", "23"}, {"其它支付(元)", "23"},{"连接率","23"}};
            String[] columns = {"date", "originalAmount", "totalIncome", "wechatIncome", "chargeAccountIncome", "redIncome", "couponIncome",
                    "chargeGifAccountIncome", "waitNumberIncome", "aliPayment", "backCartPay", "moneyPay", "shanhuiPayment","integralPayment"
                    ,"articleBackPay", "otherPayment","connctRatio"};
            ExcelUtil<ShopIncomeDto> excelUtil = new ExcelUtil<>();
            OutputStream out = new FileOutputStream(path);
            excelUtil.createMonthDtoExcel(headers, columns, result, out, map);
            out.close();
        }catch (Exception e){
            e.printStackTrace();
            log.error("生成月营业报表出错！");
            return new Result(false);
        }
        return getSuccessResult(path);
    }

    public Integer getMonthDay(String year, String month){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, Integer.parseInt(year));
        calendar.set(Calendar.MONTH, Integer.parseInt(month) - 1);
        return calendar.getActualMaximum(Calendar.DATE);
    }

    public Date getBeginDay(String year, String month, Integer day){
        Calendar beginDate = Calendar.getInstance();
        beginDate.set(Calendar.YEAR, Integer.parseInt(year));
        beginDate.set(Calendar.MONTH, Integer.parseInt(month) - 1);
        beginDate.set(Calendar.DATE, day + 1);
        beginDate.set(Calendar.HOUR_OF_DAY, 0);
        beginDate.set(Calendar.MINUTE, 0);
        beginDate.set(Calendar.SECOND,1);
        return beginDate.getTime();
    }

    public Date getEndDay(String year, String month, Integer day){
        Calendar endDate = Calendar.getInstance();
        endDate.set(Calendar.YEAR, Integer.parseInt(year));
        endDate.set(Calendar.MONTH, Integer.parseInt(month) - 1);
        endDate.set(Calendar.DATE, day + 1);
        endDate.set(Calendar.HOUR_OF_DAY, 23);
        endDate.set(Calendar.MINUTE, 59);
        endDate.set(Calendar.SECOND,59);
        return endDate.getTime();
    }
}
