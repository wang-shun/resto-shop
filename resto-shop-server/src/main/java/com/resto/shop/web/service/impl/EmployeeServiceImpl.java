package com.resto.shop.web.service.impl;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.web.model.BrandUser;
import com.resto.shop.web.dao.EmployeeMapper;
import com.resto.shop.web.model.Employee;
import com.resto.shop.web.service.EmployeeService;
import cn.restoplus.rpc.server.RpcService;

import java.util.Date;

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

    @Override
    public void insertOne(Employee employee, BrandUser brandUser) {
        //设置创建时间
        employee.setCreateTime(new Date());

        //设置创建人
        employee.setCreateUser(brandUser.getUsername());

        //设置状态为正常
        employee.setState((byte)1);

        //保存员工信息
        employeeMapper.insertSelective(employee);


    }
}
