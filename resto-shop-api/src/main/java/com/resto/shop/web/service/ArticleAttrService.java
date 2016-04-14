package com.resto.shop.web.service;

import java.util.List;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.ArticleAttr;

public interface ArticleAttrService extends GenericService<ArticleAttr, Integer> {
	/**
	 * 根据店铺ID查询信息
	 * @return
	 */
	List<ArticleAttr> selectListByShopId(String shopId);
    
	/**
	 * 添加信息
	 * @param articleAttr
	 */
	void create(ArticleAttr articleAttr);
	
	/**
	 * 删除信息
	 * @param id
	 */
	void deleteInfo(Integer id);
}
