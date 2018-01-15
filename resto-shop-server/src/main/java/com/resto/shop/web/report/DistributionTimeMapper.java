package com.resto.shop.web.report;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.DistributionTime;

import java.util.List;

public interface DistributionTimeMapper  extends GenericDao<DistributionTime,Integer> {
    int deleteByPrimaryKey(Integer id);

    int insert(DistributionTime record);

    int insertSelective(DistributionTime record);

    DistributionTime selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(DistributionTime record);

    int updateByPrimaryKey(DistributionTime record);

    List<DistributionTime> selectListByShopId(String currentShopId);
}
