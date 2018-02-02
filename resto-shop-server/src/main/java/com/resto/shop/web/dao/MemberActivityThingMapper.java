package com.resto.shop.web.dao;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.MemberActivityThing;


public interface MemberActivityThingMapper extends GenericDao<MemberActivityThing,Integer> {

    int deleteByPrimaryKey(Integer id);

    int insert(MemberActivityThing record);

    int insertSelective(MemberActivityThing record);

    MemberActivityThing selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(MemberActivityThing record);

    int updateByPrimaryKey(MemberActivityThing record);

    MemberActivityThing selectByTelephone(String telephone);

}
