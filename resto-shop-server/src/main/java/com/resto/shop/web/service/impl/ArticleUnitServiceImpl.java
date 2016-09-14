package com.resto.shop.web.service.impl;

import java.util.List;

import javax.annotation.Resource;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.ArticleUnitMapper;
import com.resto.shop.web.model.ArticleUnit;
import com.resto.shop.web.service.ArticleUnitService;

import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class ArticleUnitServiceImpl extends GenericServiceImpl<ArticleUnit, Integer> implements ArticleUnitService {

    @Resource
    private ArticleUnitMapper articleunitMapper;

    @Override
    public GenericDao<ArticleUnit, Integer> getDao() {
        return articleunitMapper;
    }

	@Override
	public List<ArticleUnit> selectListByAttrId(Integer attrId) {
		return articleunitMapper.selectListByAttrId(attrId);
	}


    
}
