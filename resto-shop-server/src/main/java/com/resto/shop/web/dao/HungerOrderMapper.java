package com.resto.shop.web.dao;

import com.resto.shop.web.model.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by KONATA on 2016/10/31.
 */
public interface HungerOrderMapper {

    void insertHungerOrder(HungerOrder order);

    void insertHungerExtra(HungerOrderExtra extra);

    void insertHungerGroup(HungerOrderGroup group);

    void insertHungerOrderDetail(HungerOrderDetail orderDetail);

    void insertHungerOrderGarnish(HungerOrderGarnish garnish);

    void updateHungerOrder(@Param("orderId") String orderId,@Param("state") Integer state);

    List<HungerOrderDetail> selectDetailsById(String id);

    HungerOrder selectById(String orderId);

}
