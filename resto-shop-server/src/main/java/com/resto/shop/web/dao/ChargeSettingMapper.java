package com.resto.shop.web.dao;

import com.resto.shop.web.model.ChargeSetting;

import java.util.List;

import com.resto.brand.core.generic.GenericDao;

public interface ChargeSettingMapper  extends GenericDao<ChargeSetting,String> {
    int deleteByPrimaryKey(String id);

    int insert(ChargeSetting record);

    int insertSelective(ChargeSetting record);

    ChargeSetting selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ChargeSetting record);

    int updateByPrimaryKey(ChargeSetting record);

    List<ChargeSetting> selectListByCustomerId(String currentUserId);
}
