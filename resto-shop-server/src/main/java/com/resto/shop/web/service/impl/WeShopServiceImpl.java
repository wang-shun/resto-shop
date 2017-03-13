package com.resto.shop.web.service.impl;

import javax.annotation.Resource;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.DateUtil;
import com.resto.shop.web.dao.WeShopMapper;
import com.resto.shop.web.model.WeShop;
import com.resto.shop.web.service.WeShopService;
import cn.restoplus.rpc.server.RpcService;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;

/**
 *
 */
@RpcService
public class WeShopServiceImpl extends GenericServiceImpl<WeShop, Integer> implements WeShopService {

    @Resource
    private WeShopMapper weshopMapper;

    @Override
    public GenericDao<WeShop, Integer> getDao() {
        return weshopMapper;
    }

    @Override
    public List<WeShop> selectWeShopListByBrandIdAndTime(String brandId, String createTime) {
//        Date date ;
//        if(StringUtils.isEmpty(createTime)|| DateUtil.formatDate(new Date(),"yyyy-MM-dd").equals(createTime)){
//            date = DateUtil.fomatDate(DateUtil.getAfterDayDateStr("-1")) ;
//        }else {
//            date = DateUtil.fomatDate(createTime);
//        }
        return weshopMapper.selectWeShopListByBrandIdAndTime(brandId,DateUtil.fomatDate(createTime));
    }

    @Override
    public WeShop selectWeShopByShopIdAndTime(String shopId, String createTime) {
        System.err.println("进入方法:shopId"+shopId+"createTime"+DateUtil.fomatDate(createTime));
        return weshopMapper.selectWeShopByShopIdAndTime(shopId,DateUtil.fomatDate(createTime));
    }
}
