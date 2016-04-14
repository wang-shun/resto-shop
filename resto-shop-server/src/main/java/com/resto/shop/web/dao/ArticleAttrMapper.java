package com.resto.shop.web.dao;

import java.util.List;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.ArticleAttr;

public interface ArticleAttrMapper extends GenericDao<ArticleAttr, Integer>{
    int deleteByPrimaryKey(Integer id);

    int insert(ArticleAttr record);

    int insertSelective(ArticleAttr record);

    ArticleAttr selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ArticleAttr record);

    int updateByPrimaryKey(ArticleAttr record);
    
    /**
     * 根据店铺ID查询信息
     * @return
     */
    List<ArticleAttr> selectListByShopId(String shopId);
    
    /**
     * 添加 信息 ，并返回此数据数据的 ID
     * @param record
     * @return
     */
    void insertInfo(ArticleAttr record);
}