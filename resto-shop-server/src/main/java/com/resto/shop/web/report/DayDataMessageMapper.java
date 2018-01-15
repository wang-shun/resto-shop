package com.resto.shop.web.report;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.DayDataMessage;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface DayDataMessageMapper  extends GenericDao<DayDataMessage,String> {
    int deleteByPrimaryKey(String id);

    int insert(DayDataMessage record);

    int insertSelective(DayDataMessage record);

    DayDataMessage selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(DayDataMessage record);

    int updateByPrimaryKey(DayDataMessage record);

    List<DayDataMessage> selectListByTime(@Param("state") int normal, @Param("type") int dayMessage, @Param("date") Date dateTime);
}
