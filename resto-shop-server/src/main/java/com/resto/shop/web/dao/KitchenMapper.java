package com.resto.shop.web.dao;

import java.util.List;

import com.resto.shop.web.model.OrderItem;
import org.apache.ibatis.annotations.Param;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.Kitchen;

public interface KitchenMapper  extends GenericDao<Kitchen,Integer> {
    int deleteByPrimaryKey(Integer id);

    int insert(Kitchen record);

    int insertSelective(Kitchen record);

    Kitchen selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Kitchen record);

    int updateByPrimaryKey(Kitchen record);
    
    /**
     * 根据店铺ID查询信息
     * @return
     */
    List<Kitchen> selectListByShopId(@Param(value = "shopId") String currentShopId);
    
    /**
     * 删除 菜品和厨房的关联信息
     */
    void deleteArticleKitchen(@Param("articleId")String articleId);
    
    /**
     * 添加 菜品和厨房的关联信息
     * @param articleId
     * @param kitchenList
     */
    void insertArticleKitchen(@Param("articleId")String articleId,@Param("kitchenList")Integer[] kitchenList);
    
    /**
     * 根据 菜品Id 查询出和菜品关联的厨房Id
     * @param articleId
     * @return
     */
    List<Integer> selectIdsByArticleId(@Param("articleId") String articleId);
    
    
    /**
     * 根据 菜品Id 查询出和菜品相关厨房的信息
     * @param articleId
     * @return
     */
    List<Kitchen> selectInfoByArticleId(@Param("articleId") String articleId);

	Kitchen selectKitchenByMealsItemId(String id);

    Kitchen selectKitchenByOrderItem(@Param("item") OrderItem orderItem,@Param("mealAttrId") Long mealAttrId);

    Long getMealAttrId(OrderItem item);
}
