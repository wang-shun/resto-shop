package com.resto.shop.web.dao;


import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.RecommendCategory;

public interface RecommendCategoryMapper extends GenericDao<RecommendCategory,String> {

    int deleteByPrimaryKey(String id);

    int insert(RecommendCategory record);

    int insertSelective(RecommendCategory record);

    RecommendCategory selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(RecommendCategory record);

    int updateByPrimaryKey(RecommendCategory record);
}