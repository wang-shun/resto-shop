package com.resto.shop.web.dao;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.WeightPackage;

public interface WeightPackageMapper extends GenericDao<WeightPackage,Long> {

    int deleteByPrimaryKey(Long id);

    int insert(WeightPackage record);

    int insertSelective(WeightPackage record);

    WeightPackage selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(WeightPackage record);

    int updateByPrimaryKey(WeightPackage record);

}
