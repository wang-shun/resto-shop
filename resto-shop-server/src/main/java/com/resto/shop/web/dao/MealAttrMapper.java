package com.resto.shop.web.dao;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.web.dto.ArticleSellDto;
import com.resto.shop.web.model.MealAttr;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface MealAttrMapper extends GenericDao<MealAttr, Integer> {
    int deleteByPrimaryKey(Integer id);

    int insert(MealAttr record);

    int insertSelective(MealAttr record);

    MealAttr selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(MealAttr record);

    int updateByPrimaryKey(MealAttr record);

    List<MealAttr> selectList(@Param("articleId") String article_id);

    void deleteByIds(@Param("ids") List<Integer> ids);

    List<ArticleSellDto> queryArticleMealAttr(Map<String, Object> selectMap);
}
