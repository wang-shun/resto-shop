package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.CustomerGroupMapper;
import com.resto.shop.web.model.CustomerGroup;
import com.resto.shop.web.service.CustomerGroupService;

import javax.annotation.Resource;

/**
 * Created by carl on 2017/9/25.
 */
@RpcService
public class CustomerGroupServiceImpl extends GenericServiceImpl<CustomerGroup, Long> implements CustomerGroupService {

    @Resource
    CustomerGroupMapper customerGroupMapper;

    @Override
    public GenericDao<CustomerGroup, Long> getDao() {
        return customerGroupMapper;
    }
}
