package com.resto.shop.web.service.impl;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.EmployeeMapper;
import com.resto.shop.web.model.Employee;
import com.resto.shop.web.service.EmployeeService;
import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class EmployeeServiceImpl extends GenericServiceImpl<Employee, Long> implements EmployeeService {

    @Resource
    private EmployeeMapper employeeMapper;

    @Override
    public GenericDao<Employee, Long> getDao() {
        return employeeMapper;
    } 

}
