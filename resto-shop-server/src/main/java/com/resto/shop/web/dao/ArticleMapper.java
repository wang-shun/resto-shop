package com.resto.shop.web.dao;

import java.util.List;
import java.util.Map;

import com.resto.shop.web.model.ArticleStock;
import org.apache.ibatis.annotations.Param;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.web.dto.ArticleSellDto;
import com.resto.brand.web.dto.ShopArticleReportDto;
import com.resto.shop.web.model.Article;

public interface ArticleMapper extends GenericDao<Article, String>{
    int deleteByPrimaryKey(String id);

    int insert(Article record);

    int insertSelective(Article record);

    Article selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(Article record);

    int updateByPrimaryKey(Article record);

	List<Article> selectList(@Param(value = "shopId") String currentShopId);

	List<Article> selectListByShopIdAndDistributionId(String currentShopId, Integer distributionModeId);

	List<Article> selectBySupportTimeId(@Param("times") List<Integer> supportTimes,@Param("shopId") String currentShopId);
	
	/**
	 * 根据是否谷清 查询菜品信息
	 * @param isEmpty
	 * @return
	 */
	List<Article> selectListByIsEmpty(@Param("isEmpty") Integer isEmpty,@Param("shopId")String shopId);
	
	/**
	 * 根据菜品 Id 设置谷清
	 * @param articleId
	 */
	void setEmpty(@Param("isEmpty") Integer isEmpty,@Param("articleId") String articleId);

	void updateLikes(String articleId, Long likes);

	void addLikes(String articleId);

	void initSuitStock();

	void initSize();

	List<ArticleStock> getStock(@Param("shopId") String shopId, @Param("familyId") String familyId,
								@Param("empty") Integer empty,@Param("freeDay") Integer freeDay,@Param("activated")Integer activated);

	Integer clearStock(@Param("articleId")String articleId,@Param("emptyRemark") String emptyRemark);

	Integer clearPriceTotal(@Param("articleId")String articleId,@Param("emptyRemark") String emptyRemark);

	Integer clearPriceStock(@Param("articleId")String articleId,@Param("emptyRemark") String emptyRemark);

	Integer cleanPriceAll(@Param("articleId")String articleId,@Param("emptyRemark") String emptyRemark);

	Integer editStock(@Param("articleId")String articleId,@Param("count")Integer count,@Param("emptyRemark") String emptyRemark);

	Integer editPriceStock(@Param("articleId")String articleId,@Param("count")Integer count,@Param("emptyRemark") String emptyRemark);

	void initSizeCurrent();

	void clearMain(@Param("articleId")String articleId,@Param("emptyRemark") String emptyRemark);

	void initEmpty();
	
	/**
	 * 设置 菜品 下架（0）/上架（1） 
	 * @param articleId
	 * @param activated
	 * @return
	 */
	int setActivate(@Param("articleId")String articleId,@Param("activated")Integer activated);

	List<Article> getSingoArticle(String shopId);

	int deleteRecommendId(String recommendId);

	int saveLog(@Param("result") Integer result,@Param("taskId") String taskId);


	int selectPidAndShopId(@Param("shopId") String shopId,@Param("articleId") String articleId);


	/**
	 * 得到套餐下的全部子品
	 * @param articleId
	 * @return
     */
	List<Article> getArticleByMeal(String articleId);

	Article selectByPid(@Param("pId") String pId,@Param("shopId") String shopId);

	Article selectByName(@Param("name") String name,@Param("shopId") String shopId);

	List<Article> delCheckArticle(String id);

	void updatePhotoSquare(@Param("id") String id, @Param("photoSquare") String photoSquare);

	void addArticleLikes(String articleId);
	
	List<Article> selectsingleItem(@Param("shopId") String shopId);
	
	List<ArticleSellDto> queryOrderArtcile(Map<String, Object> selectMap);
	
	List<ArticleSellDto> queryArticleMealAttr(Map<String, Object> selectMap);

	/**
	 * 根据分类查询分类下的所有菜品
	 */
	List<Article> getArticleListByFamily(@Param("times") List<Integer> supportTimes, @Param("shopId")String shopId, @Param("articleFamilyId")String articleFamilyId, @Param("currentPage")Integer currentPage, @Param("showCount")Integer showCount);

	void updateInitialsById(@Param("initials") String initials, @Param("articleId") String articleId);

	List<Article> selectArticleList();

    List<ArticleSellDto> selectArticleByType(Map<String, Object> selectMap);

    Map<String, Object> selectArticleOrderCount(Map<String, Object> selectMap);

    List<String> selectArticleSort(Map<String, Object> selectMap);

    /**
     * 查询在供应时间内 查询分类所有菜品(分页)
     * @param supportTimes
     * @param shopId
     * @param familyId
     * @return
     */
    List<Article> selectnewPosListByFamillyId(@Param("times") List<Integer> supportTimes, @Param("shopId") String shopId,@Param("articleFamilyId") String familyId);

    /**
     * 查询菜品中图片是在资源服务器上的
     * @param currentBrandId
     * @return
     */
    List<Article> selectHasResourcePhotoList(String currentBrandId);

	/**
	 * 根据 店铺ID 查询店铺下的所有菜品数据
	 * Pos2.0 数据拉取接口			By___lmx
	 * @param shopId
	 * @return
	 */
	List<Article> selectArticleByShopId(@Param("shopId") String shopId);
}