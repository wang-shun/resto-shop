package com.resto.shop.web.report;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.web.dto.ArticleSellDto;
import com.resto.shop.web.model.ArticleFamily;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ArticleFamilyMapper  extends GenericDao<ArticleFamily,String> {
    int deleteByPrimaryKey(String id);

    int insert(ArticleFamily record);

    int insertSelective(ArticleFamily record);

    ArticleFamily selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ArticleFamily record);

    int updateByPrimaryKey(ArticleFamily record);

	List<ArticleFamily> selectList(@Param("shopId") String shopId);

    List<ArticleFamily> selectListBySort(@Param("shopId") String shopId, @Param("currentPage") Integer currentPage, @Param("showCount") Integer showCount);
	
	List<ArticleFamily> selectListByDistributionModeId(@Param("currentShopId") String currentShopId, @Param("distributionModeId") Integer distributionModeId);

	String selectByName(String name);

    void copyBrandArticleFamily(ArticleFamily articleFamily);

    ArticleFamily checkSame(@Param("shopId") String shopId, @Param("name") String name);

    List<ArticleSellDto> selectByShopId(@Param("shopId") String shopId);

    /**
     * 新版pos查询菜品
     * @param shopDetailId
     * @param distributionModeId
     * @return
     */
    List<ArticleFamily> selectnewPosListPage(@Param("shopId") String shopDetailId, @Param("distributionModeId") Integer distributionModeId);

    List<ArticleFamily> selectArticleSort(List<String> articleIds);
}
