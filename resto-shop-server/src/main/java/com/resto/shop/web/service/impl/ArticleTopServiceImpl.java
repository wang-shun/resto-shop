package com.resto.shop.web.service.impl;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.web.dto.ArticleTopDto;
import com.resto.shop.web.dao.ArticleTopMapper;
import com.resto.shop.web.model.ArticleTop;
import com.resto.shop.web.service.ArticleTopService;
import cn.restoplus.rpc.server.RpcService;

import java.util.Date;
import java.util.List;

/**
 *
 */
@RpcService
public class ArticleTopServiceImpl extends GenericServiceImpl<ArticleTop, Long> implements ArticleTopService {

    @Resource
    private ArticleTopMapper articletopMapper;

    @Override
    public GenericDao<ArticleTop, Long> getDao() {
        return articletopMapper;
    }

    @Override
    public int selectSumGoodByTime(Date begin, Date end,String shopId) {
        return articletopMapper.selectSumGoodByTime(begin,end,shopId);
    }

    @Override
    public  int selectSumBadByTime(Date begin,Date end,String shopId){
        return articletopMapper.selectSumBadByTime(begin,end,shopId);
    }

    @Override
    public List<ArticleTopDto> selectListByTimeAndGoodType(Date xunBegin, Date xunEnd, String shopId) {
        return articletopMapper.selectListByTimeAndGoodType(xunBegin,xunEnd,shopId);
    }

    @Override
    public List<ArticleTopDto> selectListByTimeAndBadType(Date xunBegin, Date xunEnd, String shopId) {
        return articletopMapper.selectListByTimeAndBadType(xunBegin,xunEnd,shopId);
    }


}
