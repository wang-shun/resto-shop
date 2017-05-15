package com.resto.shop.web.service.impl;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.NewEmployeeMapper;
import com.resto.shop.web.model.NewEmployee;
import com.resto.shop.web.service.NewEmployeeService;
import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class NewEmployeeServiceImpl extends GenericServiceImpl<NewEmployee, String> implements NewEmployeeService {

    @Resource
    private NewEmployeeMapper newemployeeMapper;

    @Override
    public GenericDao<NewEmployee, String> getDao() {
        return newemployeeMapper;
    } 

}
