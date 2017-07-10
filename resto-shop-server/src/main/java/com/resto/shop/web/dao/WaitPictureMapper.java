package com.resto.shop.web.dao;

import com.resto.shop.web.model.WaitPicture;
import com.resto.brand.core.generic.GenericDao;

public interface WaitPictureMapper  extends GenericDao<WaitPicture,Integer> {
    int deleteByPrimaryKey(Integer id);

    int insert(WaitPicture record);

    int insertSelective(WaitPicture record);

    WaitPicture selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(WaitPicture record);

    int updateByPrimaryKey(WaitPicture record);

    int updateStateById(WaitPicture record);
}
