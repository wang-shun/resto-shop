package com.resto.shop.web.service;

import com.resto.brand.web.model.BrandSetting;

import java.util.Map;

/**
 * Created by KONATA on 2016/10/28.
 * 饿了吗接口
 */
public interface ThirdService {

    Boolean orderAccept(Map map, BrandSetting brandSetting);
}
