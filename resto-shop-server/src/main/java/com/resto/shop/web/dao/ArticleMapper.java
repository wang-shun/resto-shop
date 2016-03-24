package com.resto.shop.web.dao;

import com.resto.shop.web.model.Article;
import com.resto.brand.core.generic.GenericDao;

public interface ArticleMapper  extends GenericDao<Article,String> {
    int deleteByPrimaryKey(String id);

    int insert(Article record);

    int insertSelective(Article record);

    Article selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(Article record);

    int updateByPrimaryKey(Article record);
}
