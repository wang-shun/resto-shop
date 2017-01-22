package com.resto.shop.web.dao;

import com.resto.shop.web.model.WeItem;

public interface WeItemMapper {
    int deleteByPrimaryKey(Long id);

    int insert(WeItem record);

    int insertSelective(WeItem record);

    WeItem selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(WeItem record);

    int updateByPrimaryKey(WeItem record);
}