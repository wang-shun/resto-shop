package com.resto.shop.web.service.impl;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.model.AppraiseComment;
import com.resto.shop.web.service.AppraiseCommentService;

/**
 * Created by carl on 2016/11/20.
 */
public class AppraiseCommentServiceImpl extends GenericServiceImpl<AppraiseComment, String> implements AppraiseCommentService {
    @Override
    public GenericDao<AppraiseComment, String> getDao() {
        return null;
    }
}
