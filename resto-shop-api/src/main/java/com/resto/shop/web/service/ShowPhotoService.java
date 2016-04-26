package com.resto.shop.web.service;

import java.util.List;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.ShowPhoto;

public interface ShowPhotoService extends GenericService<ShowPhoto, Integer> {

	List<ShowPhoto> selectListByShopId(String currentShopId);
    
}
