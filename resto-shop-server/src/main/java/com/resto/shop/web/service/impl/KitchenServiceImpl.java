package com.resto.shop.web.service.impl;

import java.util.List;

import javax.annotation.Resource;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.KitchenMapper;
import com.resto.shop.web.model.ArticleKitchen;
import com.resto.shop.web.model.Kitchen;
import com.resto.shop.web.model.OrderItem;
import com.resto.shop.web.service.KitchenService;

import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class KitchenServiceImpl extends GenericServiceImpl<Kitchen, Integer> implements KitchenService {

    @Resource
    private KitchenMapper kitchenMapper;

    @Override
    public GenericDao<Kitchen, Integer> getDao() {
        return kitchenMapper;
    }

	@Override
	public List<Kitchen> selectListByShopId(String shopId) {
		return kitchenMapper.selectListByShopId(shopId);
	}

	@Override
	public void insertSelective(Kitchen kitchen) {
		kitchenMapper.insertSelective(kitchen);
	}

	@Override
	public void saveArticleKitchen(String articleId, Integer[] kitchenList) {
		kitchenMapper.deleteArticleKitchen(articleId);
		if(kitchenList!=null&&kitchenList.length>0){
			kitchenMapper.insertArticleKitchen(articleId, kitchenList);;
		}
	}

	@Override
	public List<Integer> selectIdsByArticleId(String articleId) {
		return kitchenMapper.selectIdsByArticleId(articleId);
	}

	@Override
	public List<Kitchen> selectInfoByArticleId(String articleId) {
		return kitchenMapper.selectInfoByArticleId(articleId);
	}

	@Override
	public Kitchen selectMealKitchen(OrderItem mealItems) {
		Kitchen kitchen = kitchenMapper.selectKitchenByMealsItemId(mealItems.getId());
		return kitchen;
	}

	@Override
	public Kitchen selectKitchenByOrderItem(OrderItem item,List<Long> mealAttrId) {
		return kitchenMapper.selectKitchenByOrderItem(item,mealAttrId);
	}

	@Override
	public List<Long> getMealAttrId(OrderItem orderItem) {
		return kitchenMapper.getMealAttrId(orderItem);
	}

	@Override
	public Kitchen getItemKitchenId(OrderItem orderItem) {
		return kitchenMapper.getItemKitchenId(orderItem);
	}

	@Override
	public List<ArticleKitchen> selectArticleKitchenByShopId(String shopId) {
		return kitchenMapper.selectArticleKitchenByShopId(shopId);
	}
}
