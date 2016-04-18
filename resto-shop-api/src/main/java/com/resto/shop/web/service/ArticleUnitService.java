package com.resto.shop.web.service;

import java.util.List;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.ArticleUnit;

public interface ArticleUnitService extends GenericService<ArticleUnit, Integer> {
	
	List<ArticleUnit> selectListByAttrId(Integer attrId);
}
