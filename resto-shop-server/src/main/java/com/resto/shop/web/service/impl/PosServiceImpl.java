package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.resto.shop.web.constant.Common;
import com.resto.shop.web.dao.PosMapper;
import com.resto.shop.web.model.Article;
import com.resto.shop.web.posDto.ArticleStockDto;
import com.resto.shop.web.service.ArticleService;
import com.resto.shop.web.service.PosService;
import com.resto.shop.web.util.RedisUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by KONATA on 2017/8/9.
 */
@RpcService
public class PosServiceImpl implements PosService {

    @Autowired
    private ArticleService articleService;

    @Override
    public String syncArticleStock(String shopId) {
        Map<String,Object> result = new HashMap<>();
        result.put("dataType","article");

        List<Article> articleList = articleService.selectList(shopId);
        List<ArticleStockDto> articleStockDtoList = new ArrayList<>();
        for(Article article : articleList){
            Integer count = (Integer) RedisUtil.get(article.getId() + Common.KUCUN);
            if (count != null) {
                article.setCurrentWorkingStock(count);
            }
            ArticleStockDto articleStockDto = new ArticleStockDto(article.getId(),article.getCurrentWorkingStock());
            articleStockDtoList.add(articleStockDto);
        }
        result.put("articleList",articleStockDtoList);
        return new JSONObject(result).toString();
    }

    @Override
    public String shopMsgChange(String shopId) {
        return shopId;
    }
}
