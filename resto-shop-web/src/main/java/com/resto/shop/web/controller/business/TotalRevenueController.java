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
import javax.swing.JOptionPane;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.brand.core.util.ExcelUtil;
import com.resto.brand.web.dto.BrandIncomeDto;
import com.resto.brand.web.dto.IncomeReportDto;
import com.resto.brand.web.dto.ShopIncomeDto;
import com.resto.brand.web.model.Brand;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.shop.web.constant.PayMode;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.service.OrderPaymentItemService;

@Controller
@RequestMapping("totalRevenue")
public class TotalRevenueController extends GenericController {

	@Resource
	BrandService brandService;

	@Resource
	ShopDetailService shopDetailService;

	@Resource
	OrderPaymentItemService orderpaymentitemService;

	@RequestMapping("/list")
	public void list() {
	}

	// 封装品牌和店铺收入需要的数据
	public Map<String, Object> getIncomeReportList(String beginDate, String endDate) {
		// 查询品牌和店铺的收入情况
		List<IncomeReportDto> incomeReportList = orderpaymentitemService.selectIncomeList(getCurrentBrandId(),
				beginDate, endDate);
		// 封装店铺所需要的数据结构
		List<ShopDetail> listShop = shopDetailService.selectByBrandId(getCurrentBrandId());
		List<ShopIncomeDto> shopIncomeList = new ArrayList<>();
		Map<String, ShopIncomeDto> hm = new HashMap<>();
		for (int i = 0; i < listShop.size(); i++) {// 实际有多少个店铺显示多少个数据
			ShopIncomeDto sin = new ShopIncomeDto();
			sin.setShopDetailId(listShop.get(i).getId());
			sin.setShopName(listShop.get(i).getName());
			// 设置每个店铺初始营业额为零
			BigDecimal temp = BigDecimal.ZERO;
			sin.setWechatIncome(temp);
			sin.setRedIncome(temp);
			sin.setCouponIncome(temp);
			sin.setChargeAccountIncome(temp);
			sin.setChargeGifAccountIncome(temp);
			sin.setTotalIncome(temp, temp, temp, temp, temp);
			String s = "" + i;
			hm.put(s, sin);
			if (!incomeReportList.isEmpty()) {
				for (IncomeReportDto in : incomeReportList) {
					if (hm.get(s).getShopDetailId().equals(in.getShopDetailId())) {
						switch (in.getPayMentModeId()) {
						case PayMode.WEIXIN_PAY:
							hm.get(s).setWechatIncome(in.getPayValue());
							break;
						case PayMode.ACCOUNT_PAY:
							hm.get(s).setRedIncome(in.getPayValue());
							break;
						case PayMode.COUPON_PAY:
							hm.get(s).setCouponIncome(in.getPayValue());
							break;
						case PayMode.CHARGE_PAY:
							hm.get(s).setChargeAccountIncome(in.getPayValue());
							break;
						case PayMode.REWARD_PAY:
							hm.get(s).setChargeGifAccountIncome(in.getPayValue());
							break;

						default:
							break;
						}
						hm.get(s).setTotalIncome(hm.get(s).getWechatIncome(), hm.get(s).getRedIncome(),
								hm.get(s).getCouponIncome(), hm.get(s).getChargeAccountIncome(),
								hm.get(s).getChargeGifAccountIncome());
					}
				}
			}
			shopIncomeList.add(hm.get(s));
		}
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

		if (!incomeReportList.isEmpty()) {
			for (IncomeReportDto income : incomeReportList) {
				if (income.getPaymentModeId() == PayMode.WEIXIN_PAY) {
					wechatIncome = wechatIncome.add(income.getPayValue()).setScale(2);
				} else if (income.getPayMentModeId() == PayMode.ACCOUNT_PAY) {
					redIncome = redIncome.add(income.getPayValue()).setScale(2);
				} else if (income.getPayMentModeId() == PayMode.COUPON_PAY) {
					couponIncome = couponIncome.add(income.getPayValue()).setScale(2);
				} else if (income.getPaymentModeId() == PayMode.CHARGE_PAY) {
					chargeAccountIncome = chargeAccountIncome.add(income.getPayValue()).setScale(2);
				} else if (income.getPayMentModeId() == PayMode.REWARD_PAY) {
					chargeGifAccountIncome = chargeGifAccountIncome.add(income.getPayValue()).setScale(2);
				}
			}
		}
		in.setBrandName(brand.getBrandName());
		in.setWechatIncome(wechatIncome);
		in.setRedIncome(redIncome);
		in.setCouponIncome(couponIncome);
		in.setChargeAccountIncome(chargeAccountIncome);
		in.setChargeGifAccountIncome(chargeGifAccountIncome);
		in.setTotalIncome(in.getWechatIncome(), in.getRedIncome(), in.getCouponIncome(), in.getChargeAccountIncome(),
				in.getChargeGifAccountIncome());
		brandIncomeList.add(in);
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
		// 导出文件名
		String str = "brandInCome.xls";
		String path = request.getSession().getServletContext().getRealPath(str);
		Brand brand = brandService.selectById(getCurrentBrandId());
		List<ShopDetail> shopDetails = shopDetailService.selectByBrandId(getCurrentBrandId());
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
		map.put("num", "6");// 显示的位置
		map.put("reportTitle", "品牌收入条目");// 表的名字
		map.put("timeType", "yyyy-MM-dd");

		String[][] headers = { { "品牌", "20" }, { "营收总额(元)", "16" }, { "红包支付(元)", "16" }, { "优惠券支付(元)", "17" },
				{ "微信支付(元)", "16" }, { "充值账户支付(元)", "19" }, { "充值赠送账户支付(元)", "23" } };
		String[] columns = { "brandName", "totalIncome", "redIncome", "couponIncome", "wechatIncome",
				"chargeAccountIncome", "chargeGifAccountIncome" };
		List<BrandIncomeDto> result = (List<BrandIncomeDto>) getIncomeReportList(beginDate, endDate).get("brandIncome");
		ExcelUtil<BrandIncomeDto> excelUtil = new ExcelUtil<BrandIncomeDto>();
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
		map.put("num", "6");// 显示的位置
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
