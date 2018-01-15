package com.resto.shop.web.report;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.FreeDay;

import java.util.Date;
import java.util.List;

public interface FreeDayMapper extends GenericDao<FreeDay, String> {
    int deleteByPrimaryKey(Date freeDay);

    int insert(FreeDay record);

    int insertSelective(FreeDay record);

    List<FreeDay> selectList(FreeDay day);

    int deleteByDateAndId(FreeDay day);

	FreeDay selectByDate(String format, String shopId);

}