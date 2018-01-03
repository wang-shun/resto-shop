package com.resto.shop.web.service;

import java.util.List;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.ArticleKitchen;
import com.resto.shop.web.model.Kitchen;
import com.resto.shop.web.model.OrderItem;

public interface KitchenService extends GenericService<Kitchen, Integer> {
	/**
	 * 根据店铺ID查询信息
	 * @return
	 */
	List<Kitchen> selectListByShopId(String shopId);
	
	
	void insertSelective(Kitchen kitchen);
	
	void saveArticleKitchen(String articleId,Integer[] kitchenList);
	
	
	/**
     * 根据 菜品Id 查询出和菜品关联的厨房Id
     * @param articleId
     * @return
     */
	List<Integer> selectIdsByArticleId(String articleId);
	
	
	/**
	 * 根据 菜品Id 查询出和菜品关联的厨房信息
	 * @param articleId
	 * @return
	 */
	List<Kitchen> selectInfoByArticleId(String articleId);


	Kitchen selectMealKitchen(OrderItem mealItems);

	Kitchen selectKitchenByOrderItem(OrderItem item,List<Long> mealAttrId);

	List<Long> getMealAttrId(OrderItem orderItem);

	Kitchen getItemKitchenId(OrderItem orderItem);

	List<ArticleKitchen>  selectArticleKitchenByShopId(String shopId);
}
