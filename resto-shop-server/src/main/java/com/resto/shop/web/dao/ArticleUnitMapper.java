package com.resto.shop.web.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.ArticleUnit;

public interface ArticleUnitMapper extends GenericDao<ArticleUnit,Integer>{
    int deleteByPrimaryKey(Integer id);

    int insert(ArticleUnit record);

    int insertSelective(ArticleUnit record);

    ArticleUnit selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ArticleUnit record);

    int updateByPrimaryKey(ArticleUnit record);
    
    List<ArticleUnit> selectListByAttrId(@Param(value = "attrId") Integer attrId);
    
    /**
     * 根据 属性 ID 删除 规格信息(假删，修改状态)
     * @param id
     */
    void deleteByAttrId(Integer id);

    int insertByAuto(ArticleUnit articleUnit);

    ArticleUnit selectSame(@Param("name") String name,@Param("attrId") String attrId);
       
}