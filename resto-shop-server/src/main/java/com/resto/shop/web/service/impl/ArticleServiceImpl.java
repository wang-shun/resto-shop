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
import com.resto.shop.web.service.KitchenService;
import com.resto.shop.web.service.SupportTimeService;

import cn.restoplus.rpc.common.util.StringUtil;
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
    
    @Resource
    private KitchenService kitchenService;
    
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
		articlePriceServer.saveArticlePrices(article.getId(),article.getArticlePrices());
		supportTimeService.saveSupportTimes(article.getId(),article.getSupportTimes());
		kitchenService.saveArticleKitchen(article.getId(), article.getKitchenList());
		return article;
	} 
	
	@Override
	public int update(Article article) {
		articlePriceServer.saveArticlePrices(article.getId(),article.getArticlePrices());
		supportTimeService.saveSupportTimes(article.getId(),article.getSupportTimes());
		kitchenService.saveArticleKitchen(article.getId(), article.getKitchenList());
		return super.update(article);
	}

	@Override
	public Article selectFullById(String id) {
		List<ArticlePrice> prices = articlePriceServer.selectByArticleId(id);
		List<Integer> supportTimesIds = supportTimeService.selectByIdsArticleId(id);
		List<Integer> kitchenList = kitchenService.selectIdsByArticleId(id);
		Article article  = selectById(id);
		article.setArticlePrices(prices);
		article.setSupportTimes(supportTimesIds.toArray(new Integer[0]));
		article.setKitchenList(kitchenList.toArray(new Integer[0]));
		return article;
	}

	@Override
	public List<Article> selectListFull(String currentShopId, Integer distributionModeId) {
		List<Article> articleList = articleMapper.selectListByShopIdAndDistributionId(currentShopId,distributionModeId);
		for (Article a: articleList) {
			if(!StringUtil.isEmpty(a.getHasUnit())){
				List<ArticlePrice> prices = articlePriceServer.selectByArticleId(a.getId());
				a.setArticlePrices(prices);
			}
		}
		return articleList;
	}

}
