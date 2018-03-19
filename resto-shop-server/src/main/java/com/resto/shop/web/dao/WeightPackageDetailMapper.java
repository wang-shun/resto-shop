package com.resto.shop.web.dao;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.WeightPackageDetail;

public interface WeightPackageDetailMapper extends GenericDao<WeightPackageDetail,Long> {

    int deleteByPrimaryKey(Long id);

    int insert(WeightPackageDetail record);

    int insertSelective(WeightPackageDetail record);

    WeightPackageDetail selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(WeightPackageDetail record);

    int updateByPrimaryKey(WeightPackageDetail record);

}
