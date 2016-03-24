package com.resto.shop.web.dao;

import com.resto.shop.web.model.FreeDay;
import java.util.Date;

public interface FreeDayMapper {
    int deleteByPrimaryKey(Date freeDay);

    int insert(FreeDay record);

    int insertSelective(FreeDay record);
}