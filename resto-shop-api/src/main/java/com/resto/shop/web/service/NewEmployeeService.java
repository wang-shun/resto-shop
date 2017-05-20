package com.resto.shop.web.service;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.NewEmployee;

import java.util.List;

public interface NewEmployeeService extends GenericService<NewEmployee, String> {

    List<NewEmployee> selectByIds(List<String> ids);

    List<NewEmployee> selectByShopId(String shopId);
}
