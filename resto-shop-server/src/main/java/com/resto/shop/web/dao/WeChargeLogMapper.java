package com.resto.shop.web.dao;

import com.resto.shop.web.model.WeChargeLog;

public interface WeChargeLogMapper {
    int deleteByPrimaryKey(Long id);

    int insert(WeChargeLog record);

    int insertSelective(WeChargeLog record);

    WeChargeLog selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(WeChargeLog record);

    int updateByPrimaryKey(WeChargeLog record);
}