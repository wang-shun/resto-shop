package com.resto.shop.web.dao;

import com.resto.shop.web.model.SupportTime;

import java.util.List;

import com.resto.brand.core.generic.GenericDao;

public interface SupportTimeMapper  extends GenericDao<SupportTime,Integer> {
    int deleteByPrimaryKey(Integer id);

    int insert(SupportTime record);

    int insertSelective(SupportTime record);

    SupportTime selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(SupportTime record);

    int updateByPrimaryKey(SupportTime record);

    List<SupportTime> selectList(String shopDetailId);
}
