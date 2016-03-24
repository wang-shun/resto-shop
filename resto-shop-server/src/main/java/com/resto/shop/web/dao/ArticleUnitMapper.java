package com.resto.shop.web.dao;

import com.resto.shop.web.model.ArticleUnit;
import com.resto.brand.core.generic.GenericDao;

public interface ArticleUnitMapper  extends GenericDao<ArticleUnit,Integer> {
    int deleteByPrimaryKey(Integer id);

    int insert(ArticleUnit record);

    int insertSelective(ArticleUnit record);

    ArticleUnit selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ArticleUnit record);

    int updateByPrimaryKey(ArticleUnit record);
}
