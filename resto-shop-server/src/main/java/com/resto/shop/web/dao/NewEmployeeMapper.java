package com.resto.shop.web.dao;

import com.resto.shop.web.model.NewEmployee;
import com.resto.brand.core.generic.GenericDao;

public interface NewEmployeeMapper  extends GenericDao<NewEmployee,String> {
    int deleteByPrimaryKey(String id);

    int insert(NewEmployee record);

    int insertSelective(NewEmployee record);

    NewEmployee selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(NewEmployee record);

    int updateByPrimaryKey(NewEmployee record);
}
