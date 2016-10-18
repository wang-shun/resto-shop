package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.constant.WaitModerState;
import com.resto.shop.web.dao.GetNumberMapper;
import com.resto.shop.web.model.GetNumber;
import com.resto.shop.web.service.GetNumberService;

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
            getNumber.setCallNumber(getNumber.getCallNumber()+1);
            getNumber.setCallNumberTime(new Date());
        } else if(state == WaitModerState.WAIT_MODEL_NUMBER_ONE) {
            getNumber.setState(WaitModerState.WAIT_MODEL_NUMBER_ONE);
            getNumber.setEatTime(new Date());
            //计算最终等位红包价格
            Long tempTime = (getNumber.getEatTime().getTime() - getNumber.getCreateTime().getTime()) / 1000;  //等待的时间
            BigDecimal endMoney = getNumber.getFlowMoney().multiply(new BigDecimal(tempTime));             //最终价钱
            getNumber.setFinalMoney(endMoney.compareTo(getNumber.getHighMoney()) > 0 ? getNumber.getHighMoney() : endMoney);

        } else if(state == WaitModerState.WAIT_MODEL_NUMBER_TWO) {
            getNumber.setState(WaitModerState.WAIT_MODEL_NUMBER_TWO);
            getNumber.setPassNumberTime(new Date());
            //计算最终等位红包价格
            Long tempTime = (getNumber.getPassNumberTime().getTime()  - getNumber.getCreateTime().getTime()) / 1000;  //等待的时间
            BigDecimal endMoney = getNumber.getFlowMoney().multiply(new BigDecimal(tempTime));             //最终价钱
            getNumber.setFinalMoney(endMoney.compareTo(getNumber.getHighMoney()) > 0 ? getNumber.getHighMoney() : endMoney);
        }
        getNumberMapper.updateByPrimaryKeySelective(getNumber);
        return getNumber;
    }
}
