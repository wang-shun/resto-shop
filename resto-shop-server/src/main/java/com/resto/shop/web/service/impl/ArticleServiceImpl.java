package com.resto.shop.web.service.impl;

import java.util.List;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.ArticleMapper;
import com.resto.shop.web.model.Article;
import com.resto.shop.web.model.ArticlePrice;
import com.resto.shop.web.service.ArticlePriceService;
import com.resto.shop.web.service.ArticleService;
import com.resto.shop.web.service.SupportTimeService;

import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class ArticleServiceImpl extends GenericServiceImpl<Article, String> implements ArticleService {

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private ArticlePriceService articlePriceServer;
    
    @Resource
    private SupportTimeService supportTimeService;
    
    @Override
    public GenericDao<Article, String> getDao() {
        return articleMapper;
    }

	@Override
	public List<Article> selectList(String currentShopId) {
		return articleMapper.selectList(currentShopId);
	}

	@Override
	public Article save(Article article) {
		this.insert(article);
		articlePriceServer.saveArticlePrices(article.getId(),article.getArticlePrises());
		supportTimeService.saveSupportTimes(article.getId(),article.getSupportTimes());
		return article;
	} 

}
