package com.resto.shop.web.service;

import java.util.List;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.FreeDay;

public interface FreedayService extends GenericService<FreeDay, String> {

    List<FreeDay> list(FreeDay day);
    
    
}
