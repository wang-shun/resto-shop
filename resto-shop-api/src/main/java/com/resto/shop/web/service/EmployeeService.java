package com.resto.shop.web.service;

import com.resto.brand.core.generic.GenericService;
import com.resto.brand.web.model.BrandUser;
import com.resto.shop.web.model.Employee;

public interface EmployeeService extends GenericService<Employee, Long> {

    /**
     * 创建一个员工的信息
     * @param employee
     * @param currentBrandUser
     */
    void insertOne(Employee employee, BrandUser currentBrandUser);
}
