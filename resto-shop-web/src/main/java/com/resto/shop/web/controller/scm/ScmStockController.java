package com.resto.shop.web.controller.scm;

import com.resto.brand.core.entity.Result;
import com.resto.scm.web.dto.DocStockCountHeaderDo;
import com.resto.scm.web.dto.DocStockInput;
import com.resto.scm.web.dto.MaterialStockDo;
import com.resto.scm.web.service.StockCountCheckService;
import com.resto.shop.web.controller.GenericController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.util.List;

/**
 * Created by bruce on 2017-09-18 13:41
 * 库存盘点
 */
@Controller
@RequestMapping("scmStockCount")
public class ScmStockController extends GenericController{

    @Autowired
    private StockCountCheckService stockCountCheckService;

    @RequestMapping("/list_default")
    @ResponseBody
    public Result list(){
        List<MaterialStockDo> list = stockCountCheckService.findDefaultStock(getCurrentShopId());
        return getSuccessResult(list);
    }

    @RequestMapping("/list_all")
    @ResponseBody
    public Result listData(){
        List<DocStockCountHeaderDo> list = stockCountCheckService.findStockList(getCurrentShopId());
        return getSuccessResult(list);
    }

    @RequestMapping("create")
    @ResponseBody

    public Result create(@Valid DocStockInput docStockInput){
        docStockInput.setShopId(getCurrentShopId());
        docStockInput.setCreateId(getCurrentUserId());
        docStockInput.setCreateName(getCurrentBrandUser().getUsername());
        try {
            stockCountCheckService.saveStock(docStockInput);
            return Result.getSuccess();
        }catch (Exception e){
            return new Result("保存失败", 5000,false);
        }
    }

}
