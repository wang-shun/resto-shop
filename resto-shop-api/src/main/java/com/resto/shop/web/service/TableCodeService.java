package com.resto.shop.web.service;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.TableCode;

import java.util.List;

public interface TableCodeService extends GenericService<TableCode, String> {

    void insertTableCode(TableCode tablecode,String brandId,String shopDetailId);

    void updateTableCode(TableCode tablecode);

    TableCode selectByName(String name);

    TableCode selectByCodeNumber(String codeNumber);

    List<TableCode> selectListByShopId(String shopId);
}
