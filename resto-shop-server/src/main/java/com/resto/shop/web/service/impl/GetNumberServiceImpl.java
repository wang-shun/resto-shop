package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.ApplicationUtils;
import com.resto.shop.web.constant.PayMode;
import com.resto.shop.web.constant.WaitModerState;
import com.resto.shop.web.dao.GetNumberMapper;
import com.resto.shop.web.model.GetNumber;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.model.OrderPaymentItem;
import com.resto.shop.web.service.GetNumberService;
import com.resto.shop.web.service.OrderPaymentItemService;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by carl on 2016/10/14.
 */
@RpcService
public class GetNumberServiceImpl extends GenericServiceImpl<GetNumber, String> implements GetNumberService {

    @Resource
    private GetNumberMapper getNumberMapper;


    @Resource
    OrderPaymentItemService orderPaymentItemService;

    @Override
    public GenericDao<GetNumber, String> getDao() {
        return getNumberMapper;
    }

    @Override
    public List<GetNumber> selectByTableTypeShopId(String tableType, String shopId) {
        return getNumberMapper.selectByTableTypeShopId(tableType, shopId);
    }

    @Override
    public Integer selectCount(String tableType,Date date) {
        return getNumberMapper.selectCount(tableType,date).size();
    }

    @Override
    public GetNumber updateGetNumber(GetNumber getNumber,Integer state) {
        if (state == WaitModerState.WAIT_MODEL_NUMBER_ZERO){
            getNumber.setCallNumberTime(new Date());
            //计算最终等位红包价格
            Long tempTime = (getNumber.getCallNumberTime().getTime() - getNumber.getCreateTime().getTime()) / 1000;  //等待的时间
            BigDecimal endMoney = getNumber.getFlowMoney().multiply(new BigDecimal(tempTime));             //最终价钱
            if(getNumber.getCallNumber() == 0){
                if(endMoney.subtract(getNumber.getHighMoney()).doubleValue() > 0){
                    getNumber.setFinalMoney(getNumber.getHighMoney());
                }else{
                    getNumber.setFinalMoney(endMoney);
                }
            }
            //其他修改
            getNumber.setCallNumber(getNumber.getCallNumber()+1);

        } else if(state == WaitModerState.WAIT_MODEL_NUMBER_ONE) {
            getNumber.setState(WaitModerState.WAIT_MODEL_NUMBER_ONE);
            getNumber.setEatTime(new Date());
            //计算最终等位红包价格
            Long tempTime = (getNumber.getEatTime().getTime() - getNumber.getCreateTime().getTime()) / 1000;  //等待的时间
            BigDecimal endMoney = getNumber.getFlowMoney().multiply(new BigDecimal(tempTime));             //最终价钱
            if(endMoney.subtract(getNumber.getHighMoney()).doubleValue() > 0){
                getNumber.setFinalMoney(getNumber.getHighMoney());
            }else{
                getNumber.setFinalMoney(endMoney);
            }
//            getNumber.setFinalMoney(endMoney.compareTo(getNumber.getHighMoney()) > 0 ? getNumber.getHighMoney() : endMoney);

        } else if(state == WaitModerState.WAIT_MODEL_NUMBER_TWO) {
            getNumber.setState(WaitModerState.WAIT_MODEL_NUMBER_TWO);
            getNumber.setPassNumberTime(new Date());
            //计算最终等位红包价格
            Long tempTime = (getNumber.getPassNumberTime().getTime()  - getNumber.getCreateTime().getTime()) / 1000;  //等待的时间
            BigDecimal endMoney = getNumber.getFlowMoney().multiply(new BigDecimal(tempTime));             //最终价钱
            //getNumber.setFinalMoney(endMoney.compareTo(getNumber.getHighMoney()) > 0 ? getNumber.getHighMoney() : endMoney);
            if(endMoney.subtract(getNumber.getHighMoney()).doubleValue() > 0){
                getNumber.setFinalMoney(getNumber.getHighMoney());
            }else{
                getNumber.setFinalMoney(endMoney);
            }
        }
        getNumberMapper.updateByPrimaryKeySelective(getNumber);
        return getNumber;
    }

    @Override
    public GetNumber getWaitInfoByCustomerId(String customerId,String shopId) {
        return getNumberMapper.getWaitInfoByCustomerId(customerId,shopId);
    }

    @Override
    public void refundWaitMoney(Order order) {
        GetNumber getNumber = getNumberMapper.getWaitInfoByCustomerId(order.getCustomerId(),order.getShopDetailId());
        getNumber.setState(WaitModerState.WAIT_MODEL_NUMBER_ONE);
        update(getNumber);

        OrderPaymentItem item = new OrderPaymentItem();
        item.setId(ApplicationUtils.randomUUID());
        item.setOrderId(order.getId());
        item.setPaymentModeId(PayMode.WAIT_MONEY);
        item.setPayTime(order.getCreateTime());
        item.setPayValue(getNumber.getFinalMoney());
        item.setRemark("退还等位红包:" + order.getWaitMoney());
        item.setResultData(getNumber.getId());
        orderPaymentItemService.insert(item);



    }
}
