package com.resto.shop.web.dao;

import com.resto.shop.web.model.Advert;
import com.resto.brand.core.generic.GenericDao;

public interface AdvertMapper  extends GenericDao<Advert,Integer> {
    int deleteByPrimaryKey(Integer id);

    int insert(Advert record);

    int insertSelective(Advert record);

    Advert selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Advert record);

    int updateByPrimaryKeyWithBLOBs(Advert record);

    int updateByPrimaryKey(Advert record);
}
