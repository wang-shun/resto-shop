package com.resto.shop.web.dao;

import com.resto.shop.web.model.QueueQrcode;

public interface QueueQrcodeMapper {
    int deleteByPrimaryKey(String id);

    int insert(QueueQrcode record);

    int insertSelective(QueueQrcode record);

    QueueQrcode selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(QueueQrcode record);

    int updateByPrimaryKey(QueueQrcode record);
}