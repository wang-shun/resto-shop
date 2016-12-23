package com.resto.shop.web.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.ApplicationUtils;
import com.resto.brand.core.util.DateUtil;
import com.resto.brand.web.model.Brand;
import com.resto.brand.web.model.ShopDetail;
import com.resto.shop.web.constant.PayMode;
import com.resto.shop.web.dao.ChargeOrderMapper;
import com.resto.shop.web.dao.ChargeSettingMapper;
import com.resto.shop.web.model.AccountLog;
import com.resto.shop.web.model.ChargeOrder;
import com.resto.shop.web.model.ChargePayment;
import com.resto.shop.web.model.ChargeSetting;
import com.resto.shop.web.model.Customer;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.model.OrderPaymentItem;
import com.resto.shop.web.service.AccountService;
import com.resto.shop.web.service.ChargeOrderService;
import com.resto.shop.web.service.ChargePaymentService;
import com.resto.shop.web.service.CustomerService;
import com.resto.shop.web.service.OrderPaymentItemService;

import cn.restoplus.rpc.server.RpcService;

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
			accountService.addAccount(chargeMoney, customer.getAccountId(), "自助充值",AccountLog.SOURCE_CHARGE);
			accountService.addAccount(reward, customer.getAccountId(), "充值赠送",AccountLog.SOURCE_CHARGE_REWARD);
			// 添加充值记录
			chargeOrder.setOrderState((byte) 1);
			chargeOrder.setFinishTime(new Date());
			chargeOrder.setChargeBalance(chargeMoney);
			chargeOrder.setRewardBalance(reward);
			chargeOrder.setTotalBalance(chargeMoney.add(reward));
			chargePaymentService.insert(cp);
			update(chargeOrder);// 只能更新状态和结束时间
		}


	}

	@Override
	public BigDecimal selectTotalBalance(String customerId) {
		return chargeorderMapper.selectTotalBalance(customerId);
	}

	@Override
	public void useChargePay(BigDecimal remainPay,String customerId,Order order) {
		BigDecimal[] result = new BigDecimal[]{BigDecimal.ZERO,BigDecimal.ZERO};
		useBalance(result,remainPay,customerId,order);
		
	}

	private void useBalance(BigDecimal[] result, BigDecimal remindPay, String customerId, Order order) {
		ChargeOrder chargeOrder = chargeorderMapper.selectFirstBalanceOrder(customerId);
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
			}
			if(remindPay.compareTo(totalPay)>0){
				remindPay = remindPay.subtract(totalPay).setScale(2, BigDecimal.ROUND_HALF_UP);
				useBalance(result,remindPay,customerId,order);
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
			if(rewardPay.compareTo(rewardBalance) < 0 && useReward.doubleValue() < 0.01){
				return rewardPay;
			}
			return rewardBalance;
		}
		return useReward; //否则返回需要支付的金额
	}

	@Override
	public void refundCharge(BigDecimal payValue, String id) {
		ChargeOrder chargeOrder= selectById(id);
		if(chargeOrder!=null){
			Customer customer = customerService.selectById(chargeOrder.getCustomerId());
			chargeorderMapper.refundCharge(payValue,id);
			accountService.addAccount(payValue, customer.getAccountId(), "退还充值金额", AccountLog.CHARGE_PAY_REFUND);
		}
	}

	@Override
	public void refundReward(BigDecimal payValue, String id) {
		ChargeOrder chargeOrder= selectById(id);
		if(chargeOrder!=null){
			Customer customer = customerService.selectById(chargeOrder.getCustomerId());
			chargeorderMapper.refundReward(payValue,id);
			accountService.addAccount(payValue, customer.getAccountId(), "退还充值赠送金额", AccountLog.REWARD_PAY_REFUND);
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
}