package com.resto.shop.web.service;

import java.util.List;
import java.util.Map;

import com.resto.brand.core.generic.GenericService;
import com.resto.brand.web.dto.ArticleSellDto;
import com.resto.brand.web.dto.ShopArticleReportDto;
import com.resto.brand.web.model.BrandSetting;
import com.resto.shop.web.model.Article;
import com.resto.shop.web.model.ArticleStock;
import org.apache.ibatis.annotations.Param;

public interface ArticleService extends GenericService<Article, String> {

	List<Article> selectList(String currentShopId);

	Article save(Article article);

	Article selectFullById(String id,String show);

	/**
	 * 通过店铺Id以及配送模式查询
	 * @param currentShopId
	 * @param distributionModeId
	 * @return
	 */
	List<Article> selectListFull(String currentShopId, Integer distributionModeId,String show);
	
	/**
	 * 根据 是否 谷清 查询菜品信息
	 * @param isEmpty
	 * @return
	 */
	List<Article> selectListByIsEmpty(Integer isEmpty,String shopId);
	
	/**
	 * 根据 菜品Id 设置谷清
	 * @param articleId
	 */
	void setEmpty(Integer isEmpty,String articleId);
    
	void addLikes(String articleId);
	
	void updateLikes(String articleId,Long likes);

	/**
	 * 初始化库存
	 */
	void initStock();

	List<ArticleStock> getStock(String shopId,String familyId,Integer empty,Integer activated);

	Boolean clearStock(String articleId,String shopId);

	Boolean editStock(String articleId,Integer count,String shopId);

	Boolean setActivated(String articleId,Integer activated);

	List<Article> getSingoArticle(String shopId);

	void deleteRecommendId(String recommendId);

	void saveLog(Integer result,String taskId);


	/**
	 * 菜品库分配菜品
	 */
	void assignArticle(String [] shopList,String articleList[]);

	/**
	 * 分配套餐
	 * @param shopList
	 * @param articleList
     */
	void assignTotal(String [] shopList,String articleList[]);

	/**
	 * 删除单品时候校验
	 * @param id
	 * @return
     */
	List<Article> delCheckArticle(String id);

	void updatePhotoSquare(@Param("id") String id, @Param("photoSquare") String photoSquare);
	
	void updateArticleImg(Article article);

	void addArticleLikes(String articleId);
	
	List<Article> selectsingleItem(String shopId);
	
	List<ArticleSellDto> queryOrderArtcile(Map<String, Object> selectMap);
	
	List<ArticleSellDto> queryArticleMealAttr(Map<String, Object> selectMap);

	/**
	 * 根据分类查询分类下的所有菜品
	 */
	List<Article> getArticleListByFamily(String shopId, String articleFamilyId, Integer currentPage, Integer showCount);

	/**
	 * 修改菜品名称的首字母
	 * @param initials
	 * @param articleId
     */
	void updateInitialsById(String initials, String articleId);

	/**
	 * 不分条件下所有的菜品
	 * @return
     */
	List<Article> selectArticleList();
}
