package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.AppraiseCommentMapper;
import com.resto.shop.web.model.AppraiseComment;
import com.resto.shop.web.service.AppraiseCommentService;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by carl on 2016/11/20.
 */
@RpcService
public class AppraiseCommentServiceImpl extends GenericServiceImpl<AppraiseComment, String> implements AppraiseCommentService {

    @Resource
    private AppraiseCommentMapper appraiseCommentMapper;

    @Override
    public GenericDao<AppraiseComment, String> getDao() {
        return appraiseCommentMapper;
    }

    @Override
    public List<AppraiseComment> appraiseCommentList(String appraiseId) {
        return appraiseCommentMapper.appraiseCommentList(appraiseId);
    }

    @Override
    public AppraiseComment insertComment(AppraiseComment appraiseComment) {
        appraiseCommentMapper.insertSelective(appraiseComment);
        return appraiseComment;
    }
}
