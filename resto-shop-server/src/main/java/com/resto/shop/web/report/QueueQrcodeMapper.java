package com.resto.shop.web.report;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.QueueQrcode;

public interface QueueQrcodeMapper  extends GenericDao<QueueQrcode,String> {
    int deleteByPrimaryKey(String id);

    int insert(QueueQrcode record);

    int insertSelective(QueueQrcode record);

    QueueQrcode selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(QueueQrcode record);

    int updateByPrimaryKey(QueueQrcode record);

    QueueQrcode selectByIdEndtime(String id);

    QueueQrcode selectLastQRcode(String shopId);
}
