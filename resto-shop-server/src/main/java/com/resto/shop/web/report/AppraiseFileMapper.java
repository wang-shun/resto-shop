package com.resto.shop.web.report;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.AppraiseFile;

import java.util.List;

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

    List<AppraiseFile> appraiseFileList(String appraiseId);
}
