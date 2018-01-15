package com.resto.shop.web.report;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.SupportTime;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SupportTimeMapper  extends GenericDao<SupportTime,Integer> {
    int deleteByPrimaryKey(Integer id);

    int insert(SupportTime record);

    int insertSelective(SupportTime record);

    SupportTime selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(SupportTime record);

    int updateByPrimaryKey(SupportTime record);

    List<SupportTime> selectList(String shopDetailId);

	void insertArticleSupportTime(@Param("articleId") String articleId, @Param("supportTimes") Integer[] supportTimes);

	void deleteArticleSupportTime(String articleId);

	List<Integer> selectByArticleId(String articleId);
}
