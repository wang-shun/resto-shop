package com.resto.shop.web.service;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.AppraiseFile;

import java.util.List;

/**
 * Created by carl on 2016/11/20.
 */
public interface AppraiseFileService extends GenericService<AppraiseFile, String> {

    List<AppraiseFile> appraiseFileList(String appraiseId);

}
