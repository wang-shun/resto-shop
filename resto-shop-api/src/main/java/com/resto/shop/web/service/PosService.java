package com.resto.shop.web.service;

import com.resto.shop.web.model.Article;

import java.util.Map;

/**
 * Created by KONATA on 2017/8/9.
 */
public interface PosService {
    /**
     * 同步店铺菜品库存
     * @param shopId 店铺id
     * @return
     */
    String syncArticleStock(String shopId);

}
