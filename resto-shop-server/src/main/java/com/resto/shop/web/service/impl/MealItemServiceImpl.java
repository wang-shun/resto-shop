package com.resto.shop.web.service.impl;

import java.util.List;

import javax.annotation.Resource;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.MealItemMapper;
import com.resto.shop.web.model.MealItem;
import com.resto.shop.web.service.MealItemService;

import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class MealItemServiceImpl extends GenericServiceImpl<MealItem, Integer> implements MealItemService {

    @Resource
    private MealItemMapper mealitemMapper;

    @Override
    public GenericDao<MealItem, Integer> getDao() {
        return mealitemMapper;
    }

	@Override
	public void deleteByMealAttrIds(List<Integer> mealAttrIds) {
		mealitemMapper.deleteByMealAttrIds(mealAttrIds);
	}

	@Override
	public void insertBatch(List<MealItem> mealItems) {
		if(mealItems!=null&&mealItems.size()>0){
			mealitemMapper.insertBatch(mealItems);
		}
	}

	@Override
	public List<MealItem> selectByAttrIds(List<Integer> ids) {
		return mealitemMapper.selectByAttrIds(ids);
	}

	@Override
	public List<MealItem> selectByIds(Integer[] mealItemIds) {
		return mealitemMapper.selectByIds(mealItemIds);
	} 

}
