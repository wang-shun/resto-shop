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


    /**
     * 当门店后台数据发生变更时通知pos
     * @param shopId 发生信息变更的门店
     * @return 发生信息变更的门店
     */
    String shopMsgChange(String shopId);

}
