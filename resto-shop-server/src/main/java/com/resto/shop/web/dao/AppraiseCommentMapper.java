package com.resto.shop.web.dao;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.AppraiseComment;

/**
 * Created by carl on 2016/11/20.
 */
public interface AppraiseCommentMapper extends GenericDao<AppraiseComment,String> {
    int deleteByPrimaryKey(String id);

    int insert(AppraiseComment appraiseComment);

    int insertSelective(AppraiseComment appraiseComment);

    AppraiseComment selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(AppraiseComment appraiseComment);

    int updateByPrimaryKey(AppraiseComment appraiseComment);
}
