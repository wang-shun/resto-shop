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
import com.resto.shop.web.model.Order;
import com.resto.shop.web.model.OrderPaymentItem;
import com.resto.shop.web.service.ChargeOrderService;
import com.resto.shop.web.service.OrderService;
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
    private OrderService orderService;

	@RequestMapping("/list")
	public void list() {
	}

	// 封装品牌和店铺收入需要的数据
	public Map<String, Object> getIncomeReportList(String beginDate, String endDate) {
		// 查询品牌和店铺的收入情况
	//	List<IncomeReportDto> incomeReportList = orderpaymentitemService.selectIncomeList(getCurrentBrandId(), beginDate, endDate);

        //查询所有已消费的订单
        List<Order> list = orderService.selectAllAlreadyConsumed(getCurrentBrandId(),beginDate,endDate);

       // 封装brand所需要的数据结构

        Brand brand = brandService.selectById(getCurrentBrandId());
        List<BrandIncomeDto> brandIncomeList = new ArrayList<>();
        BrandIncomeDto in = new BrandIncomeDto();
        // 初始化品牌的信息
        BigDecimal wechatIncome = BigDecimal.ZERO;
        BigDecimal redIncome = BigDecimal.ZERO;
        BigDecimal couponIncome = BigDecimal.ZERO;
        BigDecimal chargeAccountIncome = BigDecimal.ZERO;
        BigDecimal chargeGifAccountIncome = BigDecimal.ZERO;
        BigDecimal totalIncome = BigDecimal.ZERO;

        if(!list.isEmpty()){
            for(Order o :list){
              totalIncome = totalIncome.add(o.getOrderMoney());
                if(!o.getOrderPaymentItems().isEmpty()){
                    for(OrderPaymentItem oi : o.getOrderPaymentItems()){
                        switch (oi.getPaymentModeId()) {
                            case PayMode.WEIXIN_PAY:
                                wechatIncome=wechatIncome.add(oi.getPayValue());
                                break;
                            case PayMode.ACCOUNT_PAY:
                                redIncome=redIncome.add(oi.getPayValue());
                                break;
                            case PayMode.COUPON_PAY:
                                couponIncome=couponIncome.add(oi.getPayValue());
                                break;
                            case PayMode.CHARGE_PAY:
                                chargeAccountIncome=chargeAccountIncome.add(oi.getPayValue());
                                break;
                            case PayMode.REWARD_PAY:
                                chargeGifAccountIncome=chargeGifAccountIncome.add(oi.getPayValue());
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        }
        in.setBrandId(brand.getId());
        in.setBrandName(brand.getBrandName());
        in.setWechatIncome(wechatIncome);
        in.setRedIncome(redIncome);
        in.setCouponIncome(couponIncome);
        in.setChargeAccountIncome(chargeAccountIncome);
        in.setChargeGifAccountIncome(chargeGifAccountIncome);
        in.setTotalIncome(totalIncome);
        brandIncomeList.add(in);

		// 封装店铺所需要的数据结构
		List<ShopDetail> listShop = shopDetailService.selectByBrandId(getCurrentBrandId());
		List<ShopIncomeDto> shopIncomeList = new ArrayList<>();
        for(ShopDetail s :listShop){
            ShopIncomeDto sin = new ShopIncomeDto();
            sin.setShopDetailId(s.getId());
            sin.setShopName(s.getName());
            List<Order> list2 = new ArrayList<>();
            if(!list.isEmpty()){
                for(Order so :list){
                    if(so.getShopDetailId().equals(sin.getShopDetailId())){
                        list2.add(so);
                    }
                }
            }
            // 设置每个店铺初始营业额为零
            BigDecimal sRedIncome = BigDecimal.ZERO;
            BigDecimal sWechatIncome = BigDecimal.ZERO;//
            BigDecimal sTotalIncome = BigDecimal.ZERO;
            BigDecimal sCouponIncome = BigDecimal.ZERO;
            BigDecimal sChargeAccountIncome = BigDecimal.ZERO;
            BigDecimal sChargeGifAccountIncome = BigDecimal.ZERO;

            if(!list2.isEmpty()){
                for(Order sso:list2){
                    sTotalIncome = sTotalIncome.add(sso.getOrderMoney());
                    if(!sso.getOrderPaymentItems().isEmpty()){
                        for(OrderPaymentItem soi :sso.getOrderPaymentItems()){
                            switch (soi.getPaymentModeId()) {
                                case PayMode.WEIXIN_PAY:
                                    sWechatIncome=sWechatIncome.add(soi.getPayValue());
                                    break;
                                case PayMode.ACCOUNT_PAY:
                                    sRedIncome=sRedIncome.add(soi.getPayValue());
                                    break;
                                case PayMode.COUPON_PAY:
                                    sCouponIncome=sCouponIncome.add(soi.getPayValue());
                                    break;
                                case PayMode.CHARGE_PAY:
                                    sChargeAccountIncome=sChargeAccountIncome.add(soi.getPayValue());
                                    break;
                                case PayMode.REWARD_PAY:
                                    sChargeGifAccountIncome=sChargeGifAccountIncome.add(soi.getPayValue());
                                    break;
                                default:
                                    break;
                            }
                        }

                    }
                }
            }
            sin.setWechatIncome(sWechatIncome);
            sin.setTotalIncome(sTotalIncome);
            sin.setChargeAccountIncome(sChargeAccountIncome);
            sin.setChargeGifAccountIncome(sChargeGifAccountIncome);
            sin.setCouponIncome(sCouponIncome);
            sin.setRedIncome(sRedIncome);
            shopIncomeList.add(sin);
        }
		Map<String, Object> map = new HashMap<>();
		map.put("shopIncome", shopIncomeList);
		map.put("brandIncome", brandIncomeList);
		return map;
	}

	@RequestMapping("reportIncome")
	@ResponseBody
	public Map<String, Object> selectIncomeReportList(@RequestParam("beginDate") String beginDate,
			@RequestParam("endDate") String endDate) {

		return getIncomeReportList(beginDate, endDate);
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

//		String[][] headers = { { "品牌", "20" }, {"营收总额(元)","16"},{ "订单总额(元)", "16" }, { "微信支付(元)", "16" },{ "充值账户支付(元)", "19" },{ "红包支付(元)", "16" }, { "优惠券支付(元)", "17" },
//				 { "充值赠送支付(元)", "23" } };

        String[][] headers = { { "品牌", "20" },{ "订单总额(元)", "16" }, { "微信支付(元)", "16" },{ "充值账户支付(元)", "19" },{ "红包支付(元)", "16" }, { "优惠券支付(元)", "17" },
                { "充值赠送支付(元)", "23" } };
//		String[] columns = { "name","totalIncome","wechatIncome", "chargeAccountIncome","redIncome", "couponIncome",
//				 "chargeGifAccountIncome" };

        String[] columns = { "name", "totalIncome","wechatIncome", "chargeAccountIncome","redIncome", "couponIncome",
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
