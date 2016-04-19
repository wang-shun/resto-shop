package com.resto.shop.web.service.impl;

import java.util.List;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.ArticlePriceMapper;
import com.resto.shop.web.model.ArticlePrice;
import com.resto.shop.web.service.ArticlePriceService;
import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class ArticlePriceServiceImpl extends GenericServiceImpl<ArticlePrice, String> implements ArticlePriceService {

    @Resource
    private ArticlePriceMapper articlepriceMapper;

    @Override
    public GenericDao<ArticlePrice, String> getDao() {
        return articlepriceMapper;
    }

	@Override
	public void saveArticlePrices(String articleId,List<ArticlePrice> articlePrises) {
		articlepriceMapper.deleteArticlePrices(articleId);
		for(ArticlePrice price:articlePrises){
			price.setArticleId(articleId);
			articlepriceMapper.insertSelective(price);
		}
	}

	@Override
	public List<ArticlePrice> selectByArticleId(String articleId) {
		return articlepriceMapper.selectByArticleId(articleId);
	}

	@Override
	public List<ArticlePrice> selectList(String shopDetailId) {
		return articlepriceMapper.selectList(shopDetailId);
	} 

}
