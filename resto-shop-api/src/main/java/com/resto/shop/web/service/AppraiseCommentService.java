package com.resto.shop.web.service;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.AppraiseComment;

import java.util.List;

/**
 * Created by carl on 2016/11/20.
 */
public interface AppraiseCommentService extends GenericService<AppraiseComment, String> {

    List<AppraiseComment> appraiseCommentList(String appraiseId);

    AppraiseComment insertComment(AppraiseComment appraiseComment);
}
