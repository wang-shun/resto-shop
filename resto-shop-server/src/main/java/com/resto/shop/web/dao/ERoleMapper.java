package com.resto.shop.web.dao;

import com.resto.shop.web.model.ERole;
import com.resto.brand.core.generic.GenericDao;

public interface ERoleMapper  extends GenericDao<ERole,Long> {
    int deleteByPrimaryKey(Long id);

    int insert(ERole record);

    int insertSelective(ERole record);

    ERole selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ERole record);

    int updateByPrimaryKey(ERole record);
}
