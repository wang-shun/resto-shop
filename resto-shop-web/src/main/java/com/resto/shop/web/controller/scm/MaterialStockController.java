package com.resto.shop.web.controller.scm;

import com.resto.brand.core.entity.Result;
import com.resto.scm.web.dto.MaterialStockDo;
import com.resto.scm.web.service.MaterialStockService;
import com.resto.shop.web.controller.GenericController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

/**
 *
 * 原料库存
 *
 *
 */
@Controller
@RequestMapping("scmMaterialStock")
public class MaterialStockController extends GenericController {

    @Resource
    private MaterialStockService materialstockService;

    @RequestMapping("/list_all")
    @ResponseBody
    public Result listData() {
        List<MaterialStockDo> list = materialstockService.queryJoin4Page(this.getCurrentShopId());
        return getSuccessResult(list);

    }


}
