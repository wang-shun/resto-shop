package com.resto.shop.web.dao;

import com.resto.shop.web.model.Article;
import com.resto.shop.web.posDto.ArticleSupport;

import java.util.List;

/**
 * Created by KONATA on 2017/8/9.
 */
public interface PosMapper  {
   List<ArticleSupport>  selectArticleSupport(List<Article> articleList);
}
