package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.CustomerDetailMapper;
import com.resto.shop.web.model.CustomerDetail;
import com.resto.shop.web.service.CustomerDetailService;

import javax.annotation.Resource;

/**
 * Created by carl on 2016/12/27.
 */
@RpcService
public class CustomerDetailServiceImpl extends GenericServiceImpl<CustomerDetail, String> implements CustomerDetailService {

    @Resource
    CustomerDetailMapper customerDetailMapper;

    @Override
    public GenericDao<CustomerDetail, String> getDao() {
        return customerDetailMapper;
    }
}
