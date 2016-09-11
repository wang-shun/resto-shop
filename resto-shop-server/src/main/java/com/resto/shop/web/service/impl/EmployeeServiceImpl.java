package com.resto.shop.web.service.impl;

import javax.annotation.Resource;

import cn.restoplus.rpc.common.util.StringUtil;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;

import com.resto.brand.web.model.BrandUser;

import com.resto.shop.web.dao.EmployeeMapper;
import com.resto.shop.web.dao.EmployeeRoleMapper;
import com.resto.shop.web.model.Employee;
import com.resto.shop.web.model.EmployeeRole;
import com.resto.shop.web.service.EmployeeService;
import cn.restoplus.rpc.server.RpcService;

import java.util.*;


/**
 *
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
@RpcService
public class EmployeeServiceImpl extends GenericServiceImpl<Employee, Long> implements EmployeeService {

    @Resource
    private EmployeeMapper employeeMapper;

    @Resource
    private EmployeeRoleMapper employeeRoleMapper;

    @Resource
    private com.resto.brand.web.service.EmployeeService employeeBrandService;

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

        //查询员工对应的员工角色
        Employee employee3 = employeeMapper.selectOneById(employeeId);
        //拼接店铺角色(如果有角色信息)
        List<Long> ids = new ArrayList<>();

        if(employee3!=null){
            if (employee3.getEmployeeRoleList() != null && employee3.getEmployeeRoleList().size() > 0) {
                for (EmployeeRole er : employee3.getEmployeeRoleList()) {
                    Long key = er.getId();
                    ids.add(key);
                }
                //删除掉所有该用户有的店铺角色
                employeeRoleMapper.deleteByIds(ids);
            }

        }

            //更新员工的角色信息
            if(id!=null||"".equals(id)){
                String[]  arr2 =  StringUtil.split(id,",");
                for(String shopId_roleId : arr2){
                    String[] arr3 =StringUtil.split(shopId_roleId,"_");
                    List<String> arr4 = Arrays.asList(arr3);
                    EmployeeRole er = new EmployeeRole();
                    for (int i = 0; i <arr4.size(); i++) {
                        er.setEmployeeId(employeeId);
                        er.setShopId(arr4.get(0));
                        er.setRoleId(Long.parseLong(arr4.get(1)));
                    }
                    employeeRoleMapper.insertSelective(er);

                }
                //设置brand端的employee状态为1
                com.resto.brand.web.model.Employee employee4 = employeeBrandService.selectOneBytelephone(employee.getTelephone());
                if (employee4 != null) {
                    employee4.setState(1);
                    employeeBrandService.updateSelect(employee4);
                }
            }
    }

}
