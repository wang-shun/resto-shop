package com.resto.shop.web.dao;

import com.resto.shop.web.model.ShowPhoto;

import java.util.List;

import com.resto.brand.core.generic.GenericDao;

public interface ShowPhotoMapper  extends GenericDao<ShowPhoto,Integer> {
    int deleteByPrimaryKey(Integer id);

    int insert(ShowPhoto record);

    int insertSelective(ShowPhoto record);

    ShowPhoto selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ShowPhoto record);

    int updateByPrimaryKey(ShowPhoto record);

	List<ShowPhoto> selectListByShopId(String currentShopId);
}
