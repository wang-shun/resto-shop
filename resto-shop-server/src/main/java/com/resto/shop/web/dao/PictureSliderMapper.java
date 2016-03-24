package com.resto.shop.web.dao;

import com.resto.shop.web.model.PictureSlider;
import com.resto.brand.core.generic.GenericDao;

public interface PictureSliderMapper  extends GenericDao<PictureSlider,Integer> {
    int deleteByPrimaryKey(Integer id);

    int insert(PictureSlider record);

    int insertSelective(PictureSlider record);

    PictureSlider selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(PictureSlider record);

    int updateByPrimaryKey(PictureSlider record);
}
