package com.resto.shop.web.dao;

import java.util.List;

import com.resto.shop.web.model.ArticleStock;
import com.resto.shop.web.model.FreeDay;
import org.apache.ibatis.annotations.Param;

import com.resto.brand.core.generic.GenericDao;
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
}