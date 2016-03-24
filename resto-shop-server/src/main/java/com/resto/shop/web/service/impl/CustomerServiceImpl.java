package com.resto.shop.web.service.impl;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.CustomerMapper;
import com.resto.shop.web.model.Customer;
import com.resto.shop.web.service.CustomerService;
import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class CustomerServiceImpl extends GenericServiceImpl<Customer, String> implements CustomerService {

    @Resource
    private CustomerMapper customerMapper;

    @Override
    public GenericDao<Customer, String> getDao() {
        return customerMapper;
    } 

}
