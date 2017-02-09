package com.resto.shop.web.dao;

import com.resto.shop.web.model.WeReturnItem;

public interface WeReturnItemMapper {
    int deleteByPrimaryKey(Long id);

    int insert(WeReturnItem record);

    int insertSelective(WeReturnItem record);

    WeReturnItem selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(WeReturnItem record);

    int updateByPrimaryKey(WeReturnItem record);
}