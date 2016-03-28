package com.resto.shop.web.service;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.Notice;

public interface NoticeService extends GenericService<Notice, String> {
	/**
	 * 添加通知
	 * @param notice
	 */
    void create(Notice notice);
}
