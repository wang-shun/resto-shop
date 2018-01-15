package com.resto.shop.web.report;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.AppraiseComment;

import java.util.List;

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

    List<AppraiseComment> appraiseCommentList(String appraiseId);

    int selectByCustomerCount(String customerId);
}
