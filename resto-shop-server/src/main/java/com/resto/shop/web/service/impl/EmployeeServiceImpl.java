package com.resto.shop.web.service.impl;

import javax.annotation.Resource;

import cn.restoplus.rpc.common.util.StringUtil;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.ApplicationUtils;
import com.resto.brand.web.model.BrandUser;
import com.resto.shop.web.dao.EmployeeMapper;
import com.resto.shop.web.dao.EmployeeRoleMapper;
import com.resto.shop.web.model.Employee;
import com.resto.shop.web.model.EmployeeRole;
import com.resto.shop.web.service.EmployeeService;
import cn.restoplus.rpc.server.RpcService;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static cn.restoplus.rpc.common.util.StringUtil.split;

/**
 *
 */
@RpcService
public class EmployeeServiceImpl extends GenericServiceImpl<Employee, Long> implements EmployeeService {

    @Resource
    private EmployeeMapper employeeMapper;

    @Resource
    private EmployeeRoleMapper employeeRoleMapper;

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

    @Override
    public Employee selectEmployeeInfo(Long id) {
        return employeeMapper.selectByPrimaryKey(id);
    }

    @Override
    public Employee selectOneById(Long id) {
        return employeeMapper.selectOneById(id);
    }

    @Override
    public void updateSelected(Long employeeId, String id, BrandUser brandUser) {

        //查询员工信息
        Employee employee = employeeMapper.selectByPrimaryKey(employeeId);
        employee.setUpdateTime(new Date());
        employee.setUpdateUser(brandUser.getUsername());
        //更新员工基本信息
        employeeMapper.updateByPrimaryKeySelective(employee);

        //更新员工的角色信息
        if(id!=null&&id!=""){
          String[]  arr =  StringUtil.split(id,",");
            for(String shopId_roleId : arr){
                    String[] arr2 =StringUtil.split(shopId_roleId,"_");
                    List<String> arr3 = Arrays.asList(arr2);
                for (int i = 0; i <arr3.size(); i++) {
                    //
                    EmployeeRole er = new EmployeeRole();
                    er.setEmployeeId(employeeId);
                    er.setShopId(arr3.get(0));
                    er.setRoleId(Long.parseLong(arr3.get(1)));
                    employeeRoleMapper.insertSelective(er);
                }

            }


        }


    }
}
