package com.resto.shop.web.service;

import com.resto.brand.web.model.BrandSetting;

import java.util.List;
import java.util.Map;

/**
 * Created by KONATA on 2016/10/28.
 * 饿了吗接口
 */
public interface ThirdService {

    Boolean orderAccept(Map map, BrandSetting brandSetting);


    public List<Map<String, Object>> printOrderByPlatform(String platformId, Integer type);


}
