package com.resto.shop.web.service;

import java.util.List;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.Article;

public interface ArticleService extends GenericService<Article, String> {

	List<Article> selectList(String currentShopId);

	Article save(Article article);

	Article selectFullById(String id);
    
}
