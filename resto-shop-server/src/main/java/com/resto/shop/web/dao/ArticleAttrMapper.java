package com.resto.shop.web.dao;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.ArticleAttr;

public interface ArticleAttrMapper extends GenericDao<ArticleAttr, Integer>{
    int deleteByPrimaryKey(Integer id);

    int insert(ArticleAttr record);

    int insertSelective(ArticleAttr record);

    ArticleAttr selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ArticleAttr record);

    int updateByPrimaryKey(ArticleAttr record);
}