package com.resto.shop.web.dao;

import com.resto.shop.web.model.Employee;
import com.resto.brand.core.generic.GenericDao;

public interface EmployeeMapper extends GenericDao<Employee,String> {
    int deleteByPrimaryKey(String id);

    int insert(Employee record);

    int insertSelective(Employee record);

    Employee selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(Employee record);

    int updateByPrimaryKey(Employee record);
}
