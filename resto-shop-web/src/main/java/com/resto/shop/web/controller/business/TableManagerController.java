package com.resto.shop.web.controller.business;

import com.google.zxing.WriterException;
import com.resto.brand.core.entity.Result;
import com.resto.brand.core.util.Encrypter;
import com.resto.brand.core.util.QRCodeUtil;
import com.resto.brand.web.model.Brand;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.model.TableQrcode;
import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.brand.web.service.TableQrcodeService;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.Area;
import com.resto.shop.web.service.AreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

/**
 * Created by KONATA on 2017/4/6.
 */
@Controller
@RequestMapping("tablemanager")
public class TableManagerController extends GenericController {

    @Autowired
    TableQrcodeService tableQrcodeService;

    @Autowired
    ShopDetailService shopDetailService;

    @Autowired
    BrandService brandService;

    @Autowired
    AreaService areaService;

    @RequestMapping("/list")
    public void list(){
    }

    @RequestMapping("/list_all")
    @ResponseBody
    public List<TableQrcode> listData(){
        return tableQrcodeService.selectUsedByShopId(getCurrentShopId());
    }
//
//
//
//    @RequestMapping("list_one")
//    @ResponseBody
//    public Result list_one(Long id){
//        Area area = areaService.selectById(id);
//        return getSuccessResult(area);
//    }
//
//    @RequestMapping("create")
//    @ResponseBody
//    public Result create(@Valid Area area){
//        area.setShopDetailId(getCurrentShopId());
//        areaService.insert(area);
//        return Result.getSuccess();
//    }
//
    @RequestMapping("modify")
    @ResponseBody
    public Result modify(@Valid TableQrcode tableQrcode){
        Area area = areaService.selectById(tableQrcode.getAreaId());
        tableQrcode.setAreaName(area.getName());
        tableQrcodeService.update(tableQrcode);
        return Result.getSuccess();
    }
//
    @RequestMapping("download")
    public void download(Long id, HttpServletRequest request, HttpServletResponse response) throws IOException, WriterException {
        ShopDetail shopDetail = shopDetailService.selectByPrimaryKey(getCurrentShopId());
        Brand brand = brandService.selectById(shopDetail.getBrandId());
        String conteng = "http://"+brand.getBrandSign()+".restoplus.cn/wechat/index?vv=" + Encrypter.encrypt(String.valueOf(id));
        QRCodeUtil.createQRCode(conteng,"jpg",response.getOutputStream());
    }
}
