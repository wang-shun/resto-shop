 package com.resto.shop.web.controller.business;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.brand.web.dto.IncomeReportDto;
import com.resto.brand.web.model.Brand;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.shop.web.constant.PayMode;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.OrderPaymentItem;
import com.resto.shop.web.service.OrderPaymentItemService;

@Controller
@RequestMapping("totalRevenue")
public class TotalRevenueController extends GenericController{
	
	@Resource
	BrandService brandService;
	
	@Resource
	ShopDetailService shopDetailService;
	
	@Resource
	OrderPaymentItemService orderpaymentitemService;
	
	@RequestMapping("/list")
    public void list(){
    }
	
	
	@RequestMapping("/brandIncome")
	@ResponseBody
	/**
	 * 根据品牌查询不同支付类型的收入和总收入
	 * @return
	 */
	
	public List<IncomeReportDto> selectBrandIncomeList(@RequestParam("beginDate")String beginDate,@RequestParam("endDate")String endDate){
		Brand brand = brandService.selectById(getCurrentBrandId());
		List<OrderPaymentItem> opList = orderpaymentitemService.selectIncomeBybrandId(getCurrentBrandId(),beginDate,endDate);
		IncomeReportDto in = new IncomeReportDto();
		for(OrderPaymentItem op : opList){
			switch (op.getPaymentModeId()) {
			case PayMode.WEIXIN_PAY:  //微信支付
				in.setWechatIncome(op.getPayValue());
				break;
			case PayMode.ACCOUNT_PAY: //账户支付
				in.setAccountIncome(op.getPayValue());
				break;
			case PayMode.COUPON_PAY:  //优惠券支付
				in.setCouponIncome(op.getPayValue());
				break;
			default:
				break;
			}
		}
		in.setBrandName(brand.getBrandName());
		//总收入
		in.setTotalIncome(in.getWechatIncome().add(in.getAccountIncome()).add(in.getCouponIncome()));
		List<IncomeReportDto> list = new ArrayList<>();
		list.add(in);
		return list;
	}
	
	
	@RequestMapping("/shopIncome")
	@ResponseBody
	public List<IncomeReportDto> selectShopIncomeList(@RequestParam("beginDate")String beginDate,@RequestParam("endDate")String endDate){
		//查询所有的店铺
		List<ShopDetail> shopDetailList = shopDetailService.selectByBrandId(getCurrentBrandId());
		List<IncomeReportDto> list = new ArrayList<>();
		for(ShopDetail shop :shopDetailList){
			//查询每个店铺对应的收入
			IncomeReportDto in = new IncomeReportDto();
			List<OrderPaymentItem> opList = orderpaymentitemService.selectIncomeByShopId(shop.getId(),beginDate,endDate);
			for(OrderPaymentItem op : opList){
				switch (op.getPaymentModeId()) {
				case PayMode.WEIXIN_PAY:  //微信支付
					in.setWechatIncome(op.getPayValue());
					break;
				case PayMode.ACCOUNT_PAY: //账户支付
					in.setAccountIncome(op.getPayValue());
					break;
				case PayMode.COUPON_PAY:  //优惠券支付
					in.setCouponIncome(op.getPayValue());
					break;
				default:
					break;
				}
			}
			in.setShopName(shop.getName());
			in.setTotalIncome(in.getWechatIncome().add(in.getAccountIncome()).add(in.getCouponIncome()));
			list.add(in);
		}
		return list;
	}
	
	
}
