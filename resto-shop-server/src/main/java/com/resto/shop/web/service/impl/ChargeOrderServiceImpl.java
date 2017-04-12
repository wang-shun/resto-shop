package com.resto.shop.web.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.*;
import com.resto.brand.web.dto.RedPacketDto;
import com.resto.brand.web.dto.ShopDetailDto;
import com.resto.brand.web.model.Brand;
import com.resto.brand.web.service.BrandService;
import com.resto.shop.web.constant.PayMode;
import com.resto.shop.web.dao.ChargeOrderMapper;
import com.resto.shop.web.dao.ChargeSettingMapper;
import com.resto.shop.web.model.*;
import com.resto.shop.web.service.*;
import com.resto.shop.web.util.LogTemplateUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

import static com.resto.brand.core.util.HttpClient.doPost;

/**
 *
 */
@RpcService
public class ChargeOrderServiceImpl extends GenericServiceImpl<ChargeOrder, String> implements ChargeOrderService {


    @Resource
    private ChargeSettingMapper chargeSettingMapper;

	@Resource
	private ChargeOrderMapper chargeorderMapper;

	@Resource
	private ChargePaymentService chargePaymentService;

	@Resource
	private AccountService accountService;
	@Resource
	CustomerService customerService;
	@Resource
    OrderPaymentItemService orderPaymentItemService;
	@Resource
	BrandService brandService;

	@Resource
	ChargeOrderMapper chargeOrderMapper;


	@Override
	public GenericDao<ChargeOrder, String> getDao() {
		return chargeorderMapper;
	}

	@Override
	public ChargeOrder createChargeOrder(String settingId, String customerId, String shopId, String brandId) {
		ChargeSetting chargeSetting = chargeSettingMapper.selectByPrimaryKey(settingId);
		byte orderState = 0;
		ChargeOrder chargeOrder = new ChargeOrder(ApplicationUtils.randomUUID(),chargeSetting.getChargeMoney(),
				chargeSetting.getRewardMoney(),orderState,new Date(),customerId,shopId,brandId);
		chargeOrder.setChargeBalance(BigDecimal.ZERO);
		chargeOrder.setRewardBalance(BigDecimal.ZERO);
		chargeOrder.setTotalBalance(BigDecimal.ZERO);
		chargeOrder.setNumberDayNow(chargeSetting.getNumberDay() - 1);
		BigDecimal amount = chargeSetting.getRewardMoney().divide(new BigDecimal(chargeSetting.getNumberDay()),2,BigDecimal.ROUND_FLOOR);
		chargeOrder.setArrivalAmount(amount);
		BigDecimal endAmount = chargeSetting.getRewardMoney().subtract(amount.multiply(new BigDecimal(chargeSetting.getNumberDay() - 1)));
		chargeOrder.setEndAmount(endAmount);
		chargeOrder.setType(1);
		chargeorderMapper.insert(chargeOrder);
		return chargeOrder;
	}

	@Override
	public void chargeorderWxPaySuccess(ChargePayment cp) {
		ChargeOrder chargeOrder = selectById(cp.getChargeOrderId());
		if (chargeOrder != null && chargeOrder.getOrderState() == 0) {
			log.info("充值金额成功chargeId:"+chargeOrder.getId()+" paymentId:"+cp.getId());
			Customer customer = customerService.selectById(chargeOrder.getCustomerId());
			BigDecimal chargeMoney = chargeOrder.getChargeMoney();
			BigDecimal reward = chargeOrder.getRewardMoney();
			// 开始充值余额
			accountService.addAccount(chargeMoney, customer.getAccountId(), "自助充值",AccountLog.SOURCE_CHARGE,cp.getShopDetailId());
			accountService.addAccount(chargeOrder.getArrivalAmount(), customer.getAccountId(), "充值赠送",AccountLog.SOURCE_CHARGE_REWARD,cp.getShopDetailId());
			// 添加充值记录
			chargeOrder.setOrderState((byte) 1);
			chargeOrder.setFinishTime(new Date());
			chargeOrder.setChargeBalance(chargeMoney);
			chargeOrder.setRewardBalance(chargeOrder.getArrivalAmount());
			chargeOrder.setTotalBalance(chargeMoney.add(chargeOrder.getArrivalAmount()));
			chargePaymentService.insert(cp);
			update(chargeOrder);// 只能更新状态和结束时间
			//微信推送
			wxPush(chargeOrder);
		}


	}

	@Override
	public BigDecimal selectTotalBalance(String customerId) {
		return chargeorderMapper.selectTotalBalance(customerId);
	}

	@Override
	public void useChargePay(BigDecimal remainPay,String customerId,Order order,String brandName) {
		BigDecimal[] result = new BigDecimal[]{BigDecimal.ZERO,BigDecimal.ZERO};
		useBalance(result,remainPay,customerId,order,brandName);

	}

	private void useBalance(BigDecimal[] result, BigDecimal remindPay, String customerId, Order order,String brandName) {
		ChargeOrder chargeOrder = chargeorderMapper.selectFirstBalanceOrder(customerId);
		Customer c = customerService.selectById(customerId);
		if(chargeOrder!=null){
			BigDecimal useReward = useReward(chargeOrder,remindPay);  //使用返利支付
			BigDecimal useCharge = useCharge(chargeOrder,remindPay.subtract(useReward).setScale(2, BigDecimal.ROUND_HALF_UP));  //使用充值支付
			result[0] = useCharge;
			result[1] = useReward;
			chargeorderMapper.updateBalance(chargeOrder.getId(),useCharge,useReward);
			BigDecimal totalPay = result[0].add(result[1]);
			if(useCharge.compareTo(BigDecimal.ZERO)>0){
				OrderPaymentItem item = new OrderPaymentItem();
				item.setId(ApplicationUtils.randomUUID());
				item.setOrderId(order.getId());
				item.setPaymentModeId(PayMode.CHARGE_PAY);
				item.setPayTime(new Date());
				item.setPayValue(useCharge);
				item.setRemark("充值余额支付:" + item.getPayValue());
				item.setResultData(chargeOrder.getId());
				orderPaymentItemService.insert(item);
				//记录充值余额支付 orderAction
                LogTemplateUtils.getChargeByOrderType(brandName,item.getPayValue(),order.getId());
                LogTemplateUtils.getChargeByUserType(brandName,c,item.getPayValue());
			}
			if(useReward.compareTo(BigDecimal.ZERO)>0){
				OrderPaymentItem item = new OrderPaymentItem();
				item.setId(ApplicationUtils.randomUUID());
				item.setOrderId(order.getId());
				item.setPaymentModeId(PayMode.REWARD_PAY);
				item.setPayTime(new Date());
				item.setPayValue(useReward);
				item.setRemark("赠送余额支付:" + item.getPayValue());
				item.setResultData(chargeOrder.getId());
				orderPaymentItemService.insert(item);
				//记录充值赠送余额支付 orderActon
                LogTemplateUtils.getChargeRewardByOrderType(brandName,item.getPayValue(),order.getId());
                //记录充值赠送 userAction
                LogTemplateUtils.getChargeByUserType(brandName,c,item.getPayValue());
			}
			if(remindPay.compareTo(totalPay)>0){
				remindPay = remindPay.subtract(totalPay).setScale(2, BigDecimal.ROUND_HALF_UP);
				useBalance(result,remindPay,customerId,order,brandName);
			}
		}
	}

	private BigDecimal useCharge(ChargeOrder order, BigDecimal needToPay) {
		BigDecimal chargeBalance = order.getChargeBalance();
		if(chargeBalance.compareTo(needToPay)<0){ //如果余额不够支付剩余所需金额，则返回剩余余额
			return chargeBalance;
		}
		return needToPay; //否则返回需要支付的金额
	}

	private BigDecimal useReward(ChargeOrder order, BigDecimal rewardPay) {
		BigDecimal totalCharge = order.getChargeMoney().add(order.getRewardMoney());
		BigDecimal scalc = order.getRewardMoney().divide(totalCharge,2,BigDecimal.ROUND_HALF_UP); //支付比例
		BigDecimal useReward = rewardPay.multiply(scalc).setScale(2, BigDecimal.ROUND_HALF_UP);
		BigDecimal rewardBalance = order.getRewardBalance();
		if(rewardBalance.compareTo(useReward)<0 || useReward.doubleValue() < 0.01){  //如果剩余赠送金额不够支付，则返回剩余赠送金额
			if(rewardPay.compareTo(rewardBalance) < 0){
				return rewardPay;
			}
			return rewardBalance;
		}
		return useReward; //否则返回需要支付的金额
	}

	@Override
	public void refundCharge(BigDecimal payValue, String id,String shopDetailId) {
		ChargeOrder chargeOrder= selectById(id);
		if(chargeOrder!=null){
			Customer customer = customerService.selectById(chargeOrder.getCustomerId());
			chargeorderMapper.refundCharge(payValue,id);
			accountService.addAccount(payValue, customer.getAccountId(), "退还充值金额", AccountLog.CHARGE_PAY_REFUND,shopDetailId);
		}
	}

	@Override
	public void refundReward(BigDecimal payValue, String id,String shopDetailId) {
		ChargeOrder chargeOrder= selectById(id);
		if(chargeOrder!=null){
			Customer customer = customerService.selectById(chargeOrder.getCustomerId());
			chargeorderMapper.refundReward(payValue,id);
			accountService.addAccount(payValue, customer.getAccountId(), "退还充值赠送金额", AccountLog.REWARD_PAY_REFUND,shopDetailId);
		}
	}

    @Override
    public List<ChargeOrder> selectByDateAndShopId(String beginDate, String endDate, String shopId) {

       Date begin = DateUtil.getDateBegin(DateUtil.fomatDate(beginDate));
        Date end = DateUtil.getDateEnd(DateUtil.fomatDate(endDate));

        return chargeorderMapper.selectByDateAndShopId(begin,end,shopId);
    }

    @Override
    public List<ChargeOrder> selectByDateAndBrandId(String beginDate, String endDate, String brandId) {

        Date begin = DateUtil.getDateBegin(DateUtil.fomatDate(beginDate));
        Date end = DateUtil.getDateEnd(DateUtil.fomatDate(endDate));

        return chargeorderMapper.selectByDateAndBrandId(begin,end,brandId);
    }

    /**
	 *
	 * @param beginDate
	 * @param endDate
	 * @return
	 *
	 */
	@Override
	public List<ChargeOrder> shopChargeCodes(String shopDetailId, Date beginDate, Date endDate) {
		return chargeorderMapper.shopChargeCodes(shopDetailId,beginDate,endDate);
	}

	/**
	 * 下载报表
	 * @param shopDetailId
	 * @param beginDate
	 * @param endDate
	 * @param
	 * @return
	 */


	@Override
	public Map<String, Object> shopChargeCodesSetDto(String shopDetailId, String beginDate, String endDate, String shopname) {
		Date begin = DateUtil.getformatBeginDate(beginDate);
		Date end = DateUtil.getformatEndDate(endDate);
		List<ChargeOrder>  chargeList=chargeOrderMapper.shopChargeCodes(shopDetailId,begin,end);
		List<ShopDetailDto> ShopDetailDtoList=new ArrayList<>();
		if(chargeList!=null&&chargeList.size()>0){
	    for (ChargeOrder charge:chargeList) {
		   ShopDetailDto ShopDetailDto=new ShopDetailDto(
		   		   shopname
				   ,null==charge.getChargeMoney()?BigDecimal.ZERO:charge.getChargeMoney()
				   ,null==charge.getRewardMoney()?BigDecimal.ZERO:charge.getRewardMoney()
				   ,null==charge.getFinishTime()?new Date():charge.getFinishTime()
				   ,charge.getType()
				   ,charge.getChargelog().getOperationPhone()
				   ,charge.getChargelog().getCustomerPhone()
		           );
		   //	ShopDetailDto ShopDetailDto=new ShopDetailDto("ss",new BigDecimal(10),new BigDecimal(10),new Date(),1,"122333344","222");
		   ShopDetailDtoList.add(ShopDetailDto);
	   }

     }

		Map<String, Object> map = new HashMap<>();
		map.put("shopDetailMap", ShopDetailDtoList);
		return map;
	}

	public void wxPush(ChargeOrder chargeOrder){
		Brand brand = brandService.selectById(chargeOrder.getBrandId());
		Customer customer = customerService.selectById(chargeOrder.getCustomerId());
		//如果不是立即到账 优先推送一条提醒
		if(chargeOrder.getNumberDayNow() > 0){
			String msgFrist = "充值成功！充值赠送红包会在" + (chargeOrder.getNumberDayNow() + 1) + "天内分批返还给您，请注意查收～";
			WeChatUtils.sendCustomerMsg(msgFrist.toString(), customer.getWechatId(), brand.getWechatConfig().getAppid(), brand.getWechatConfig().getAppsecret());
            Map map = new HashMap(4);
            map.put("brandName", brand.getBrandName());
            map.put("fileName", customer.getId());
            map.put("type", "UserAction");
            map.put("content", "系统向用户:"+customer.getNickname()+"推送微信消息:"+msgFrist.toString()+",请求服务器地址为:" + MQSetting.getLocalIP());
            doPost(LogUtils.url, map);
		}
		StringBuffer msg = new StringBuffer();
		msg.append("今日充值余额已到账，快去看看吧~");
		String jumpurl = "http://" + brand.getBrandSign() + ".restoplus.cn/wechat/index?dialog=myYue&subpage=my";
		msg.append("<a href='" + jumpurl+ "'>查看账户</a>");
		WeChatUtils.sendCustomerMsg(msg.toString(), customer.getWechatId(), brand.getWechatConfig().getAppid(), brand.getWechatConfig().getAppsecret());
        Map map = new HashMap(4);
        map.put("brandName", brand.getBrandName());
        map.put("fileName", customer.getId());
        map.put("type", "UserAction");
        map.put("content", "系统向用户:"+customer.getNickname()+"推送微信消息:"+msg.toString()+",请求服务器地址为:" + MQSetting.getLocalIP());
        doPost(LogUtils.url, map);
	}
	
	@Override
	public List<Map<String, Object>> selectByShopToDay(Map<String, Object> selectMap) {
		return chargeorderMapper.selectByShopToDay(selectMap);
	}

    @Override
    public List<ChargeOrder> selectListByDateAndShopId(String zuoriDay, String zuoriDay1, String id) {
        Date begin = DateUtil.getDateBegin(DateUtil.fomatDate(zuoriDay));
        Date end = DateUtil.getDateEnd(DateUtil.fomatDate(zuoriDay1));

        return chargeorderMapper.selectListByDateAndShopId(begin,end,id);
    }

    @Override
    public List<ChargeOrder> selectByCustomerIdAndBrandId(String customerId, String brandId) {
        return chargeorderMapper.selectByCustomerIdAndBrandId(customerId,brandId);
    }

    @Override
    public List<RedPacketDto> selectChargeRedPacket(Map<String, Object> selectMap) {
        return chargeorderMapper.selectChargeRedPacket(selectMap);
    }

    @Override
    public List<ChargeOrder> selectMonthDto(Map<String, Object> selectMap) {
        return chargeOrderMapper.selectMonthDto(selectMap);
    }
}