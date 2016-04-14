package com.resto.shop.web.service;

import java.util.List;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.ArticlePrice;

public interface ArticlePriceService extends GenericService<ArticlePrice, String> {


	void saveArticlePrices(String articleId, List<ArticlePrice> articlePrises);
    
}
