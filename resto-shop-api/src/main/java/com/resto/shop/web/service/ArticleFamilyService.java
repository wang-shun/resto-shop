package com.resto.shop.web.service;

import java.util.List;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.ArticleFamily;

public interface ArticleFamilyService extends GenericService<ArticleFamily, String> {

	List<ArticleFamily> selectList(String currentShopId);
    
	List<ArticleFamily> selectListByDistributionModeId(String currentShopId,Integer distributionModeId);

	String selectByName(String Name);
}
