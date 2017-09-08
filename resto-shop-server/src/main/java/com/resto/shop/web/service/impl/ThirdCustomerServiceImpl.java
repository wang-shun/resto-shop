package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.ThirdCustomerMapper;
import com.resto.shop.web.model.ThirdCustomer;
import com.resto.shop.web.service.ThirdCustomerService;

import javax.annotation.Resource;

/**
 * Created by carl on 2017/8/25.
 */
@RpcService
public class ThirdCustomerServiceImpl extends GenericServiceImpl<ThirdCustomer, Long> implements ThirdCustomerService {

    @Resource
    private ThirdCustomerMapper thirdCustomerMapper;

    @Override
    public GenericDao<ThirdCustomer, Long> getDao() {
        return thirdCustomerMapper;
    }

    @Override
    public ThirdCustomer selectByTelephone(String tel) {
        return thirdCustomerMapper.selectByTelephone(tel);
    }
}
