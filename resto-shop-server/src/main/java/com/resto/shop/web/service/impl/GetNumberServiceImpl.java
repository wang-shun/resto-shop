package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.constant.WaitModerState;
import com.resto.shop.web.dao.GetNumberMapper;
import com.resto.shop.web.model.GetNumber;
import com.resto.shop.web.service.GetNumberService;

import javax.annotation.Resource;
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
    public Integer selectCount(String tableType) {
        return getNumberMapper.selectCount(tableType).size();
    }

    @Override
    public void updateGetNumber(GetNumber getNumber,Integer state) {
        if (state == WaitModerState.WAIT_MODEL_NUMBER_ZERO){
            getNumber.setCallNumber(getNumber.getCallNumber()+1);
            getNumber.setCallNumberTime(new Date());
        } else if(state == WaitModerState.WAIT_MODEL_NUMBER_ONE) {
            getNumber.setState(1);
            getNumber.setEatTime(new Date());
        } else if(state == WaitModerState.WAIT_MODEL_NUMBER_TWO) {
            getNumber.setState(2);
            getNumber.setPassNumberTime(new Date());
        }
        getNumberMapper.updateByPrimaryKeySelective(getNumber);
    }
}
