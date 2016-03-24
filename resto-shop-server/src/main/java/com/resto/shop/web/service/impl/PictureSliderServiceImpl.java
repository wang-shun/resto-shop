package com.resto.shop.web.service.impl;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.PictureSliderMapper;
import com.resto.shop.web.model.PictureSlider;
import com.resto.shop.web.service.PictureSliderService;
import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class PictureSliderServiceImpl extends GenericServiceImpl<PictureSlider, Integer> implements PictureSliderService {

    @Resource
    private PictureSliderMapper picturesliderMapper;

    @Override
    public GenericDao<PictureSlider, Integer> getDao() {
        return picturesliderMapper;
    } 

}
