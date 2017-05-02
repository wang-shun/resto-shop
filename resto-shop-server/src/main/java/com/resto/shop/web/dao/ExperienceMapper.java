package com.resto.shop.web.dao;

import com.resto.shop.web.model.Experience;
import com.resto.brand.core.generic.GenericDao;

public interface ExperienceMapper  extends GenericDao<Experience,Integer> {
    int deleteByPrimaryKey(Integer id);

    int insert(Experience record);

    int insertSelective(Experience record);

    Experience selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Experience record);

    int updateByPrimaryKey(Experience record);

    int deleteByTitle(String title);
}
