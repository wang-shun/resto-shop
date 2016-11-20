package com.resto.shop.web.dao;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.AppraiseFile;

/**
 * Created by carl on 2016/11/20.
 */
public interface AppraiseFileMapper extends GenericDao<AppraiseFile,String> {
    int deleteByPrimaryKey(String id);

    int insert(AppraiseFile appraiseFile);

    int insertSelective(AppraiseFile appraiseFile);

    AppraiseFile selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(AppraiseFile appraiseFile);

    int updateByPrimaryKey(AppraiseFile appraiseFile);
}
