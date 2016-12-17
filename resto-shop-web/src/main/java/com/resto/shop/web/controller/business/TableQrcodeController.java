package com.resto.shop.web.controller.business;

import com.resto.brand.core.entity.Result;
import com.resto.brand.core.util.QRCodeUtil;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.TableQrcode;
import com.resto.shop.web.service.TableQrcodeService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Date;
import java.util.List;

/**
 * Created by carl on 2016/12/16.
 */
@Controller
@RequestMapping("tableQrcode")
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
    public Result create( Integer beginTableNumber, Integer endTableNumber, String ignoreNumber ){
        for (int i = beginTableNumber; i <= endTableNumber; i++) {//循环生成二维码
            if (ignoreNumber(i,ignoreNumber)) {
                TableQrcode tableQrcode = new TableQrcode();
                tableQrcode.setBrandId(getCurrentBrandId());
                tableQrcode.setShopDetailId(getCurrentShopId());
                tableQrcode.setTableNumber(i);
                tableQrcode.setCreateTime(new Date());
                tableQrcodeService.insert(tableQrcode);
            }
        }
        return Result.getSuccess();
    }

    @RequestMapping("modify")
    @ResponseBody
    public Result modify(@Valid TableQrcode tableQrcode){
        tableQrcodeService.update(tableQrcode);
        return Result.getSuccess();
    }

    /**
     * 判断是否包含 要忽略的值
     * @param index
     * @param ignoreNumber
     * @return
     */
    public boolean ignoreNumber(int index,String ignoreNumber){
        boolean flag = true;
        if(ignoreNumber!=null && !("").equals(ignoreNumber)){
            ignoreNumber = ignoreNumber.replaceAll("，", ",");
            String[] ignoreNumbers = ignoreNumber.split(",");//保存当前要忽略的值
            for(String number : ignoreNumbers){
                if((index + "").indexOf(number) != -1){
                    flag = false;
                    break;
                }
            }
        }
        return flag;
    }

}
