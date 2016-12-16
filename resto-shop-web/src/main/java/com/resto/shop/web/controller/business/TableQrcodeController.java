package com.resto.shop.web.controller.business;

import com.resto.brand.core.entity.Result;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.TableQrcode;
import com.resto.shop.web.service.TableQrcodeService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * Created by carl on 2016/12/16.
 */
@Controller
@RequestMapping("qrCodeTable")
public class TableQrcodeController extends GenericController {

    @Resource
    private TableQrcodeService tableQrcodeService;

    @RequestMapping("/list")
    public void list(){
    }

    @RequestMapping("/list_all")
    @ResponseBody
    public List<TableQrcode> listData(){
        return tableQrcodeService.selectList();
    }

    @RequestMapping("list_one")
    @ResponseBody
    public Result list_one(Long id){
        TableQrcode tableQrcode = tableQrcodeService.selectById(id);
        return getSuccessResult(tableQrcode);
    }

    @RequestMapping("create")
    @ResponseBody
    public Result create(@Valid TableQrcode tableQrcode){
        tableQrcodeService.insert(tableQrcode);
        return Result.getSuccess();
    }

    @RequestMapping("modify")
    @ResponseBody
    public Result modify(@Valid TableQrcode tableQrcode){
        tableQrcodeService.update(tableQrcode);
        return Result.getSuccess();
    }

}
