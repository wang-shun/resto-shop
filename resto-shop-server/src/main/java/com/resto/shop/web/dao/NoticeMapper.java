package com.resto.shop.web.dao;

import com.resto.shop.web.model.Notice;
import com.resto.brand.core.generic.GenericDao;

public interface NoticeMapper  extends GenericDao<Notice,String> {
    int deleteByPrimaryKey(String id);

    int insert(Notice record);

    int insertSelective(Notice record);

    Notice selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(Notice record);

    int updateByPrimaryKey(Notice record);
}
