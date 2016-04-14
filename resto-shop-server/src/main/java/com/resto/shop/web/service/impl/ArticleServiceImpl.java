package com.resto.shop.web.service.impl;

import java.util.List;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.ArticleMapper;
import com.resto.shop.web.model.Article;
import com.resto.shop.web.service.ArticleService;
import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class ArticleServiceImpl extends GenericServiceImpl<Article, String> implements ArticleService {

    @Resource
    private ArticleMapper articleMapper;

    @Override
    public GenericDao<Article, String> getDao() {
        return articleMapper;
    }

	@Override
	public List<Article> selectList(String currentShopId) {
		return articleMapper.selectList(currentShopId);
	} 

}
