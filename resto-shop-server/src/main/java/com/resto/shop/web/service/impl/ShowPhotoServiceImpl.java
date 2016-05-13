package com.resto.shop.web.service.impl;

import java.util.List;

import javax.annotation.Resource;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.ShowPhotoMapper;
import com.resto.shop.web.model.ShowPhoto;
import com.resto.shop.web.service.ShowPhotoService;

import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class ShowPhotoServiceImpl extends GenericServiceImpl<ShowPhoto, Integer> implements ShowPhotoService {

    @Resource
    private ShowPhotoMapper showphotoMapper;

    @Override
    public GenericDao<ShowPhoto, Integer> getDao() {
        return showphotoMapper;
    }

	@Override
	public List<ShowPhoto> selectListByShopId(String currentShopId) {
		return showphotoMapper.selectListByShopId(currentShopId);
	} 

}
