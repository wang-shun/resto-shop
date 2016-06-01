package com.resto.shop.web.service;

import java.util.List;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.Notice;

public interface NoticeService extends GenericService<Notice, String> {
	
	/**
	 * 根据店铺ID查询信息
	 * @param noticeType 
	 * @return
	 */
	List<Notice> selectListByShopId(String shopId, Integer noticeType);
	
	/**
	 * 添加通知
	 * @param notice
	 */
    void create(Notice notice);

	List<Notice> selectListByShopId(String currentShopId);
}
